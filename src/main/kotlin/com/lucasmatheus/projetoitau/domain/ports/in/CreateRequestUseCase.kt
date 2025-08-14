package com.lucasmatheus.projetoitau.domain.ports.`in`

import java.time.Instant
import java.util.UUID

interface CreateRequestUseCase {
    fun create(cmd: CreateRequestCommand): CreatedRequest
}

data class CreateRequestCommand(
    val customerId: UUID,
    val productId: UUID,
    val category: String,
    val paymentMethod: String,
    val salesChannel: String,
    val totalMonthlyPremiumAmount: String,
    val insuredAmount: String,
    val coverages: Map<String, String>,
    val assistances: List<String>
)

data class CreatedRequest(
    val id: UUID,
    val createdAt: Instant
)