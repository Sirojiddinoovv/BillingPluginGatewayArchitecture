package uz.nodir.billing.service.plugin

import mu.KotlinLogging
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import uz.nodir.billing.model.property.PluginProperty
import uz.nodir.billing.model.property.PluginPropertyView
import java.nio.file.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/**
@author: Nodir
@date: 22.09.2025
@group: Meloman

 **/


@Component
class PluginWatchService(
    private val props: PluginPropertyView,
    private val loader: PluginLoader
) : SmartLifecycle {

    private val log = KotlinLogging.logger {}

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "plugin-watcher")
            .apply { isDaemon = true }
    }

    @Volatile
    private var running = false

    private var watchService: WatchService? = null

    override fun start() {
        log.info("Starting watch service")

        if (running) {
            log.info("Plugin Watch service is already running")
            return
        }

        running = true

        loader.loadAll()

        if (!props.watch) {
            log.info("Plugin watcher disabled")
            return
        }

        val dir = Paths.get(props.dir).toAbsolutePath()
        log.info("Plugin watcher directory: $dir")

        if (!Files.isDirectory(dir)) {
            log.warn("Watch path missing or not a directory: {}", dir)
            return
        }

        watchService = FileSystems
            .getDefault()
            .newWatchService()
            .also {
                dir
                    .register(
                        it,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.OVERFLOW
                    )
            }
        log.info("Prepared watch service: $watchService")

        executor
            .submit {
                var lastReload = 0L
                val debounceMs = 1500L
                val matcher = dir.fileSystem.getPathMatcher("glob:**.jar")

                try {
                    while (running) {
                        val key = watchService?.poll(2, TimeUnit.SECONDS) ?: break

                        var needReload = false
                        for (event in key.pollEvents()) {
                            val kind = event.kind()
                            if (kind == StandardWatchEventKinds.OVERFLOW) {
                                log.warn("WatchService overflow: scheduling reload")
                                needReload = true
                                continue
                            }
                            val relative = event.context() as? Path ?: continue
                            val changed = dir.resolve(relative)
                            if (matcher.matches(changed)) {
                                log.info("Detected {} for {}", kind.name(), changed.fileName)
                                needReload = true
                            }
                        }

                        val now = System.currentTimeMillis()
                        if (needReload && now - lastReload > debounceMs) {
                            lastReload = now
                            Thread.sleep(1000) // Time for reading file for OS
                            loader.reload()
                        }

                        if (!key.reset()) break
                    }
                } catch (ie: InterruptedException) {
                    log.error("Interrupted watch service with message: {}", ie.message)
                    Thread.currentThread().interrupt()
                } catch (t: Throwable) {
                    log.error("Plugin watcher error with message: ${t.message}")
                } finally {
                    try {
                        watchService?.close()
                    } catch (e: Throwable) {
                        log.error("Plugin watcher closed with message: ${e.message}")
                    }
                }
            }
    }

    override fun stop() {
        if (!running) return
        running = false
        try {
            watchService?.close()
        } catch (e: Throwable) {
            log.error("Plugin watcher stop error with message: ${e.message}")
        }
        executor.shutdownNow()
    }

    override fun isRunning(): Boolean = running

    override fun isAutoStartup(): Boolean = true

    override fun stop(callback: Runnable) {
        log.info("Stopping watch service")
        stop()
        callback.run()
    }

    override fun getPhase(): Int = Integer.MAX_VALUE
}
