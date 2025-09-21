package uz.nodir.billing.model.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.ScopedProxyMode
import uz.nodir.paycommon.enums.Processor


/**
@author: Nodir
@date: 22.09.2025
@group: Meloman

 **/

@RefreshScope(proxyMode = ScopedProxyMode.INTERFACES)
@ConfigurationProperties(prefix = "internal.plugins")
data class PluginProperty(
    override val dir: String,
    override val watch: Boolean = true,
    override val adapters: Adapters = Adapters()
): PluginPropertyView {


    class Adapters(
        var enabled: List<Processor> = emptyList(),
        var disabled: List<Processor> = emptyList()
    ) {
        override fun toString(): String {
            return "Adapters(enabled=$enabled, disabled=$disabled)"
        }
    }


    override fun isEnabled(id: Processor): Boolean {
        return if (adapters.enabled.isNotEmpty()) {
            adapters.enabled.any { it == id }
        } else {
            adapters.disabled.none { it == id }
        }
    }

    override fun toString(): String {
        return "PluginProperty(dir='$dir', watch=$watch, adapters=$adapters)"
    }


}