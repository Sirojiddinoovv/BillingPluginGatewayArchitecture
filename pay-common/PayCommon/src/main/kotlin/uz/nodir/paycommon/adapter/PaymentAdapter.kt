package uz.nodir.paycommon.adapter

import uz.nodir.paycommon.enums.Processor
import uz.nodir.paycommon.model.PaymentRequest
import uz.nodir.paycommon.model.PaymentResponse

interface PaymentAdapter {

    val id: Processor

    fun debit(request: PaymentRequest): PaymentResponse

    fun reverse(request: PaymentRequest): PaymentResponse

    fun check(request: PaymentRequest): PaymentResponse
}

