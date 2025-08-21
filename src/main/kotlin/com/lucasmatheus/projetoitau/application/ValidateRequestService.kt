package com.lucasmatheus.projetoitau.application

import com.lucasmatheus.projetoitau.adapters.events.RabbitPolicyRequestEventPublisher
import com.lucasmatheus.projetoitau.domain.model.HistoryEntry
import com.lucasmatheus.projetoitau.domain.model.PolicyRequest
import com.lucasmatheus.projetoitau.domain.model.Status
import com.lucasmatheus.projetoitau.domain.model.isFinal
import com.lucasmatheus.projetoitau.domain.ports.`in`.ValidateRequestUseCase
import com.lucasmatheus.projetoitau.domain.ports.`in`.ValidationResult
import com.lucasmatheus.projetoitau.domain.ports.out.*
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ValidateRequestService(
    private val clockConfig: ClockProvider,
    private val fraudClient: FraudClient,
    private val policyRequestRepository: PolicyRequestRepository,
    private val eventPublisher: RabbitPolicyRequestEventPublisher
) : ValidateRequestUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun validate(id: UUID): ValidationResult {
        log.info("▶️ Iniciando validação da PolicyRequest id={} (customerId será carregado do banco)", id)

        val req = policyRequestRepository.findById(id) ?: error("PolicyRequest $id not found")

        // Se já está num estado final, nada a fazer
        if (req.status.isFinal()) {
            return ValidationResult(
                id = req.id,
                previousStatus = req.status,
                newStatus = req.status,
                changed = false
            )
        }

        val result = fraudClient.check(req.id, req.customerId)
        log.info("🔎 Resposta da Fraud API para {}: {}", req.id, result)

        val newStatus = when (result.classification) {
            Classification.HIGH_RISK -> Status.REJECTED
            Classification.PREFERRED, Classification.REGULAR, Classification.UNKNOWN -> Status.PENDING
        }

        if (newStatus == req.status) {
            return ValidationResult(
                id = req.id,
                previousStatus = req.status,
                newStatus = req.status,
                changed = false
            )
        }

        val now = clockConfig.now()

        val updated: PolicyRequest = req.copy(
            status = newStatus,
            finishedAt = if (newStatus.isFinal()) now else null, // só finaliza se status final
            history = req.history + HistoryEntry(newStatus, now)
        )

        log.info("💾 Atualizando PolicyRequest: {}", updated)
        policyRequestRepository.save(updated)


        eventPublisher.publishStatusChanged(
            requestId = updated.id,
            previousStatus = req.status.name,
            newStatus = newStatus.name,
            changedAt = now
        )
        log.info("📣 Evento STATUS_CHANGED publicado para {}", updated.id)

        return ValidationResult(
            id = req.id,
            previousStatus = req.status,
            newStatus = newStatus,
            changed = true
        )
    }
}
