package uz.nodir.paycommon.model

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull


/**
@author: Nodir
@date: 21.09.2025
@group: Meloman

 **/

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PaymentRequest(
    @field:NotBlank
    val operationId: String,

    @field:NotNull
    @field:Min(1)
    val amount: Long = 0L,

    @field:NotNull
    val currency: String,

    @field:NotBlank
    val from: String,

    @field:NotBlank
    val to: String,

    /** Free-form string map for adapter-specific parameters. */
    val metadata: Map<String, String> = emptyMap()
)
