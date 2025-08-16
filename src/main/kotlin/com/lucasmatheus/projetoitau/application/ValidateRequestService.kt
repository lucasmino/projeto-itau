package com.lucasmatheus.projetoitau.application

import com.lucasmatheus.projetoitau.domain.model.HistoryEntry
import com.lucasmatheus.projetoitau.domain.model.PolicyRequest
import com.lucasmatheus.projetoitau.domain.model.Status
import com.lucasmatheus.projetoitau.domain.model.isFinal
import com.lucasmatheus.projetoitau.domain.ports.`in`.ValidateRequestUseCase
import com.lucasmatheus.projetoitau.domain.ports.`in`.ValidationResult
import com.lucasmatheus.projetoitau.domain.ports.out.Classification
import com.lucasmatheus.projetoitau.domain.ports.out.ClockProvider
import com.lucasmatheus.projetoitau.domain.ports.out.FraudClient
import com.lucasmatheus.projetoitau.domain.ports.out.PolicyRequestRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

@Service
class ValidateRequestService(
    private val clockConfig: ClockProvider,
    private val fraudClient: FraudClient,
    private val policyRequestRepository: PolicyRequestRepository
) : ValidateRequestUseCase {
    @Transactional
    override fun validate(id: UUID): ValidationResult {
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

        policyRequestRepository.save(updated)

        return ValidationResult(
            id = req.id,
            req.status,
            newStatus,
            true
        )
    }
}