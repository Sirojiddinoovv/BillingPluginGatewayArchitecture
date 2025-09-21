package uz.nodir.billing.service.registry

import mu.KotlinLogging
import org.springframework.stereotype.Component
import uz.nodir.billing.model.property.PluginPropertyView
import uz.nodir.paycommon.adapter.PaymentAdapter
import uz.nodir.paycommon.enums.Processor
import java.util.concurrent.ConcurrentHashMap


/**
@author: Nodir
@date: 22.09.2025
@group: Meloman

 **/

@Component
class PaymentPluginRegistry(
    private val props: PluginPropertyView,
    private val paymentAdapters: List<PaymentAdapter>,
    ) {

    private val log = KotlinLogging.logger {}
    private val adapters: MutableMap<Processor, PaymentAdapter> = ConcurrentHashMap()

    init {
        registerInsideAdapters()
    }

     private fun registerInsideAdapters() {
        log.info("Registering adapter: $paymentAdapters")

        paymentAdapters
            .forEach {
                log.info("Add adapter: ${it.javaClass.simpleName}")
                adapters.putIfAbsent(it.id, it)
            }
    }

    fun register(adapter: PaymentAdapter) {

        val key = adapter.id
        val prev = adapters.put(key, adapter)
        if (prev == null)
            log.info("Registered adapter '{}'", key)
        else log.info("Replaced adapter '{}'", key)

    }

    fun getIfEnabled(id: Processor): PaymentAdapter? {

        val impl = adapters[id]
        return if (impl != null && props.isEnabled(id)) impl else null
    }

    fun listIds(): List<Processor> =
        adapters
            .keys
            .filter { props.isEnabled(it) }
            .sorted()

    fun clear() {
        val keep: Set<Processor> = paymentAdapters.map { it.id }.toSet()
        adapters.keys.removeIf { it !in keep }
    }
}