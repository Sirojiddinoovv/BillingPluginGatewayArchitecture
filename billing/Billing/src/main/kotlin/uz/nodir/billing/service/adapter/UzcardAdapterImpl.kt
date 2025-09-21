package uz.nodir.billing.service.adapter

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uz.nodir.paycommon.adapter.PaymentAdapter
import uz.nodir.paycommon.enums.Processor
import uz.nodir.paycommon.model.PaymentRequest
import uz.nodir.paycommon.model.PaymentResponse


/**
@author: Nodir
@date: 21.09.2025
@group: Meloman

 **/

@Service
class UzcardAdapterImpl : PaymentAdapter {

    private val log = KotlinLogging.logger {}

    override val id: Processor = Processor.UZCARD

    override fun check(request: PaymentRequest): PaymentResponse {
        log.info("Uzcard checking request: $request")

        val response = PaymentResponse(
            true,
            "0",
            "SUCCESS",
            mapOf(
                "rrn" to "015352",
                "status" to "OK"
            )
        )

        log.info("Uzcard checking response: $response")
        return response
    }

    override fun debit(request: PaymentRequest): PaymentResponse {
        log.info("Uzcard debit request: $request")

        val response = PaymentResponse(
            true,
            "0",
            "SUCCESS",
            mapOf(
                "rrn" to "015352"
            )
        )

        log.info("Uzcard debit response: $response")
        return response
    }

    override fun reverse(request: PaymentRequest): PaymentResponse {
        log.info("Uzcard reverse request: $request")

        val response = PaymentResponse(
            true,
            "0",
            "SUCCESS",
            mapOf(
                "rrn" to "015352",
                "status" to "ROK"
            )
        )

        log.info("Uzcard reverse response: $response")
        return response
    }
}