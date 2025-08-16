package com.lucasmatheus.projetoitau.adapters.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class CreatePolicyRequestBody(
    @field:NotBlank val customerId: String,
    @field:NotBlank val productId: String,
    @field:NotBlank val category: String,
    @field:NotBlank val paymentMethod: String,
    @field:NotBlank val salesChannel: String,
    @field:Positive val totalMonthlyPremiumAmount: BigDecimal,
    @field:Positive val insuredAmount: BigDecimal,
    @field:Size(min = 1) val coverages: Map<String, String>,
    val assistances: List<String> = emptyList()
)

data class CreatePolicyRequestResponse(
    val id: String,
    val createdAt: String
)
