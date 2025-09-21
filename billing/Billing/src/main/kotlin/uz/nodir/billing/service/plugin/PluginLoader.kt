package uz.nodir.billing.service.plugin


/**
@author: Nodir
@date: 22.09.2025
@group: Meloman

 **/


import mu.KotlinLogging
import org.springframework.stereotype.Component
import uz.nodir.billing.model.property.PluginPropertyView
import uz.nodir.billing.service.registry.PaymentPluginRegistry
import uz.nodir.paycommon.adapter.PaymentAdapter
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicReference

@Component
class PluginLoader(
    private val registry: PaymentPluginRegistry,
    private val props: PluginPropertyView
) {

    private val log = KotlinLogging.logger {}
    private val classLoaderRef = AtomicReference<URLClassLoader?>()

    fun loadAll() {
        log.info("Loading plugins by property: $props")

        val dir = File(props.dir)
        log.info("Loading plugins from: ${dir.absolutePath}")

        if (!dir.exists() || !dir.isDirectory) {
            log.warn("Plugins directory not found or not a directory: {}", dir.absolutePath)
            return
        }

        val jars = dir.listFiles {
            f -> f.isFile && f.name.endsWith(".jar", ignoreCase = true) }
            ?.sortedBy { it.name.lowercase()
            }
            .orEmpty()

        if (jars.isEmpty()) {
            log.info("No plugin jars found in {}", dir.absolutePath)
            return
        }

        closePrevClassLoader()

        val urls: Array<URL> = jars
            .map {
                it
                    .toURI()
                    .toURL()
            }.toTypedArray()


        val loader = URLClassLoader.newInstance(urls, javaClass.classLoader)
        classLoaderRef.set(loader)

        val adapters = loadWithRetry(loader, retries = 2, pause = Duration.ofMillis(400))

        if (adapters.isEmpty()) {
            log.warn("No PaymentAdapter implementations discovered via ServiceLoader in {}", dir.absolutePath)
            return
        }

        adapters
            .forEach {
            try {
                registry.register(it)
                log.info("Loaded adapter: {} ({})", it.id, it.javaClass.name)
            } catch (t: Throwable) {
                log.error("Failed to register adapter {}", it.javaClass.name, t)
            }
        }

        log.info("Loaded {} adapters: {}", registry.listIds().size, registry.listIds())
    }


    fun reload() {
        log.info("Reloading plugins...")
        registry.clear()
        loadAll()
        log.info("Reload complete. Active adapters: {}", registry.listIds())
    }

    private fun loadWithRetry(
        loader: ClassLoader,
        retries: Int,
        pause: Duration
    ): List<PaymentAdapter> {
        repeat(retries) { attempt ->
            val list = ServiceLoader
                .load(PaymentAdapter::class.java, loader)
                .iterator().asSequence().toList()

            if (list.isNotEmpty()) return list

            try {
                Thread.sleep(pause.toMillis())
            } catch (e: InterruptedException) {
                log.error("Occurred error while loading with retry with message: ${e.message}")
                Thread.currentThread().interrupt()
                return emptyList()
            }
            log.debug("Retry loading adapters via ServiceLoader (attempt {}/{})", attempt + 2, retries + 1)
        }
        return ServiceLoader
            .load(PaymentAdapter::class.java, loader)
            .iterator()
            .asSequence()
            .toList()
    }

    private fun closePrevClassLoader() {
        val prev = classLoaderRef.getAndSet(null)
        if (prev != null) {
            try {
                prev.close()
                log.debug("Previous URLClassLoader closed")
            } catch (t: Throwable) {
                log.warn("Failed to close previous URLClassLoader", t)
            }
        }
    }
}
