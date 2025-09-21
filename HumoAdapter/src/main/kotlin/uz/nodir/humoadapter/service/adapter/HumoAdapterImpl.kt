package uz.nodir.humoadapter.service.adapter

import mu.KotlinLogging
import org.springframework.stereotype.Component
import uz.nodir.paycommon.adapter.PaymentAdapter
import uz.nodir.paycommon.enums.Processor
import uz.nodir.paycommon.model.PaymentRequest
import uz.nodir.paycommon.model.PaymentResponse


/**
@author: Nodir
@date: 22.09.2025
@group: Meloman

 **/

@Component
class HumoAdapterImpl: PaymentAdapter {
    private val log = KotlinLogging.logger {}

    override val id: Processor = Processor.HUMO

    override fun debit(request: PaymentRequest): PaymentResponse {
        log.info { "Humo debit request: $request" }
        val ok = request.amount > 0L

        val response = PaymentResponse(
            success = ok,
            code = if (ok) "OK" else "AMOUNT_INVALID",
            message = if (ok) "Debited via Humo" else "Amount must be > 0",
            payload = mapOf(
                "provider" to id,
                "operationId" to request.operationId
            )
        )

        log.info { "Humo debit response: $response" }
        return response
    }

    override fun reverse(request: PaymentRequest): PaymentResponse {
        log.info { "Humo reverse request: $request" }
        val response = PaymentResponse(
            success = true,
            code = "REV_OK",
            message = "Reverse accepted via Uzcard",
            payload = mapOf("provider" to id, "operationId" to request.operationId)
        )

        log.info { "Humo reverse response: $response" }
        return response
    }

    override fun check(request: PaymentRequest): PaymentResponse {
        log.info { "Humo check request: $request" }
        val response = PaymentResponse(
            success = true,
            code = "CHK_OK",
            message = "Payment is confirmed via Uzcard",
            payload = mapOf("provider" to id, "operationId" to request.operationId)
        )

        log.info { "Humo check response: $response" }
        return response
    }
}