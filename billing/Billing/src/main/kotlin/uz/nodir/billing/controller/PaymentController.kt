package uz.nodir.billing.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uz.nodir.billing.service.plugin.PluginLoader
import uz.nodir.billing.service.registry.PaymentPluginRegistry
import uz.nodir.paycommon.enums.Processor
import uz.nodir.paycommon.model.PaymentRequest
import uz.nodir.paycommon.model.PaymentResponse


/**
@author: Nodir
@date: 22.09.2025
@group: Meloman

 **/

@RestController
@RequestMapping("/api/v1/pay")
class PaymentController(
    private val registry: PaymentPluginRegistry,
    private val pluginLoader: PluginLoader,
) {
    @GetMapping("/adapters")
    fun adapters() = registry.listIds()

    @PostMapping("/{adapter}/debit")
    fun debit(@PathVariable adapter: Processor, @RequestBody req: PaymentRequest): ResponseEntity<PaymentResponse> {
        val impl = registry.getIfEnabled(adapter) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(impl.debit(req))
    }

    @PatchMapping("/reload")
    fun reload() {
        pluginLoader.reload()
    }
}