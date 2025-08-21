package com.lucasmatheus.projetoitau.domain.ports.`in`

import java.time.Instant
import java.util.UUID

interface ProcessSubscriptionDecisionUseCase {
    fun processSubscriptionDecision(requestId: UUID, authorized: Boolean, decidedAt: Instant)
}