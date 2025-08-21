package com.lucasmatheus.projetoitau.application

import com.lucasmatheus.projetoitau.domain.model.Status
import com.lucasmatheus.projetoitau.domain.model.isFinal
import com.lucasmatheus.projetoitau.domain.ports.`in`.ProcessPaymentDecisionUseCase
import com.lucasmatheus.projetoitau.domain.ports.`in`.ProcessSubscriptionDecisionUseCase
import com.lucasmatheus.projetoitau.domain.ports.out.PolicyRequestEventPublisher
import com.lucasmatheus.projetoitau.domain.ports.out.PolicyRequestRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
@Service
class ProcessExternalDecisionService(
    private val repo: PolicyRequestRepository,
    private val publisher: PolicyRequestEventPublisher,
) : ProcessPaymentDecisionUseCase, ProcessSubscriptionDecisionUseCase {

    @Transactional
    override fun processPaymentDecision(requestId: UUID, approved: Boolean, decidedAt: Instant) {
        val req = repo.findById(requestId) ?: error("PolicyRequest $requestId not found")

        if (req.status.isFinal()) return

        val next = when {
            !approved -> Status.REJECTED
            else -> Status.PENDING
        }

        val updated = req.copy(
            status = next,
            paymentConfirmedAt = decidedAt,
            history = req.history + com.lucasmatheus.projetoitau.domain.model.HistoryEntry(next, decidedAt)
        )

        repo.save(updated)
        publisher.publishStatusChanged(req.id, req.status.name, next.name, decidedAt)
    }

    @Transactional
    override fun processSubscriptionDecision(requestId: UUID, authorized: Boolean, decidedAt: Instant) {
        val req = repo.findById(requestId) ?: error("PolicyRequest $requestId not found")
        if (req.status.isFinal()) return

        val next = when {
            !authorized -> Status.REJECTED
            // pagamento + subscrição ok ⇒ aprovado; caso pagamento ainda não confirmado, manter PENDING
            authorized && req.paymentConfirmedAt != null -> Status.APPROVED
            else -> Status.PENDING
        }

        val finishedAt = if (next.isFinal()) decidedAt else req.finishedAt

        val updated = req.copy(
            status = next,
            subscriptionAuthorizedAt = decidedAt,
            finishedAt = finishedAt,
            history = req.history + com.lucasmatheus.projetoitau.domain.model.HistoryEntry(next, decidedAt)
        )

        repo.save(updated)
        publisher.publishStatusChanged(req.id, req.status.name, next.name, decidedAt)
    }
}
