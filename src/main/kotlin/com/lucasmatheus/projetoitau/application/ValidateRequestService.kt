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
import java.util.*

@Service
class ValidateRequestService(
    private val clockConfig: ClockProvider,
    private val fraudClient: FraudClient,
    private val policyRequestRepository: PolicyRequestRepository,
    private val eventPublisher: PolicyRequestEventPublisher

) : ValidateRequestUseCase {
    private val log = LoggerFactory.getLogger(javaClass)
    @Transactional
    override fun validate(id: UUID): ValidationResult {
        log.info("▶️ Iniciando validação da PolicyRequest id={} (customerId será carregado do banco)", id)
        val req = policyRequestRepository.findById(id) ?: error("PolicyRequest $id not found")

        if (req.status.isFinal()) {
            return ValidationResult(
                id = req.id,
                previousStatus = req.status,
                newStatus = req.status,
                changed = false
            )
        }


        val resul = fraudClient.check(req.id, req.customerId)
        log.info("AQUI ESTA O RETORNO DO OBJETO CHAMADO POR FRAUDEE {}: {}", req.id, resul)

        val newStatus = when (resul.classification) {
            Classification.HIGH_RISK -> Status.REJECTED
            Classification.LOW_RISK, Classification.MEDIUM_RISK, Classification.UNKNOW -> Status.APPROVED
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
            finishedAt = now,
            history = req.history + HistoryEntry(newStatus, clockConfig.now())
        )
        log.info("AQUI ESTA O OBJETO POLICY REQUEST ATUALIZADO: {}", updated)
        policyRequestRepository.save(updated)

        eventPublisher.publishStatusChanged(
            requestId = updated.id,
            previousStatus = req.status.name,
            newStatus = newStatus.name,
            changedAt = now
        )
            log.info("AQUI ESTA O EVENTO DE STATUS CHANGED PUBLICADO: {}", updated.id)

        return ValidationResult(
            id = req.id,
            req.status,
            newStatus,
            true
        )
    }
}