package com.lucasmatheus.projetoitau.domain.ports.out

import java.time.Instant
import java.util.UUID

interface PolicyRequestEventPublisher {
    fun publishCreated(requestId: UUID, customerId: UUID, createdAt: Instant)
    fun publishStatusChanged(requestId: String, previousStatus: String, newStatus: String, changedAt: Instant)
}
