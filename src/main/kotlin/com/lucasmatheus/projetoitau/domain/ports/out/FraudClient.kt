package com.lucasmatheus.projetoitau.domain.ports.out


import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import java.time.Instant
import java.util.UUID

interface FraudClient {
    fun check(orderId: UUID, customerId: UUID): FraudResult
}

data class FraudResult(
    val classification: Classification,
    val occurrences: List<Occurrence>
) {
    data class Occurrence(
        val id: UUID,
        val productId: Long,
        val type: String,
        val description: String,
        val createdAt: Instant,
        val updatedAt: Instant
    )
}
enum class Classification {
    REGULAR, HIGH_RISK, PREFERRED,
    @JsonEnumDefaultValue
    UNKNOWN
}