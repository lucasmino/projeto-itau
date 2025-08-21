package com.lucasmatheus.projetoitau.domain.ports.`in`

import java.time.Instant
import java.util.UUID

interface ProcessPaymentDecisionUseCase {
    fun processPaymentDecision(requestId: UUID, approved: Boolean, decidedAt: Instant)
}