package com.lucasmatheus.projetoitau.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PolicyRequest(
    val id: UUID,
    val customerId: UUID,
    val productId: UUID,
    val category: Category,
    val paymentMethod: PaymentMethod,
    val totalMonthlyPremiumAmount: BigDecimal,
    val insuredAmount: BigDecimal,
    val coverages: Map<String, BigDecimal>,
    val assistances: List<String>,
    val status: Status,
    val createdAt: Instant,
    val finishedAt: Instant?,
    val history: List<HistoryEntry>,
    val salesChannel: SalesChannel
)

data class HistoryEntry(
    val status: Status,
    val timestamp: Instant
)

enum class Status { RECEIVED, VALIDATED, PENDING, APPROVED, REJECTED, CANCELED }

fun Status.isFinal(): Boolean = this == Status.APPROVED || this == Status.REJECTED

enum class Category { AUTO, VIDA, RESIDENCIAL, EMPRESARIAL, OUTROS }
enum class PaymentMethod { CREDIT_CARD, DEBIT, BOLETO, PIX }
enum class SalesChannel {
    ONLINE,
    AGENCY,
    BROKER,
    PARTNER
}