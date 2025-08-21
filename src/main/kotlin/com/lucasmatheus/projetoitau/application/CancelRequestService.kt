package com.lucasmatheus.projetoitau.application

import com.lucasmatheus.projetoitau.domain.model.PolicyRequest
import com.lucasmatheus.projetoitau.domain.model.Status
import com.lucasmatheus.projetoitau.domain.ports.`in`.CancelRequestUseCase
import com.lucasmatheus.projetoitau.domain.ports.out.ClockProvider
import com.lucasmatheus.projetoitau.domain.ports.out.PolicyRequestEventPublisher
import com.lucasmatheus.projetoitau.domain.ports.out.PolicyRequestRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class CancelRequestService(
    private val policyRequestRepository: PolicyRequestRepository,
    private val eventPublisher: PolicyRequestEventPublisher,
    private val clockConfig: ClockProvider
) : CancelRequestUseCase {
    override fun cancelById(id: UUID): PolicyRequest {
        val policy = policyRequestRepository.findById(id)
            ?: throw IllegalArgumentException("PolicyRequest $id not found")

        return when (policy.status) {
            Status.APPROVED -> throw IllegalStateException("Cannot cancel an approved policy")
            Status.CANCELED -> policy // já cancelada, retorna como está
            else -> {
                val canceled = policy.copy(status = Status.CANCELED)
                val saved = policyRequestRepository.save(canceled)
                eventPublisher.publishStatusChanged(saved.id, policy.status.name, saved.status.name, clockConfig.now())
                saved
            }
        }
    }


    override fun cancelByCustomer(id: UUID): PolicyRequest {
        val policy = policyRequestRepository.findById(id)
            ?: throw IllegalArgumentException("PolicyRequest $id not found")

        if (policy.status == Status.APPROVED) {
            throw IllegalStateException("Customer cannot cancel an approved policy")
        }

        val canceled = policy.copy(status = Status.CANCELED)
        val saved = policyRequestRepository.save(canceled)
        eventPublisher.publishStatusChanged(saved.id, policy.status.name, saved.status.name, clockConfig.now())
        return saved
    }

}