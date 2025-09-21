package uz.nodir.paycommon.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


/**
@author: Nodir
@date: 21.09.2025
@group: Meloman

 **/

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentResponse(

    val success: Boolean,

    val code: String? = null,

    val message: String? = null,
    /** Free-form payload for adapter-specific data. Stick to simple types for JSON compatibility. */
    val payload: Map<String, Any?> = emptyMap()
)
