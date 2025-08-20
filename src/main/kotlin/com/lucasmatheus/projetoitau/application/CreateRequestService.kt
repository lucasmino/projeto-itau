package com.lucasmatheus.projetoitau.application

import com.lucasmatheus.projetoitau.domain.model.Category
import com.lucasmatheus.projetoitau.domain.model.HistoryEntry
import com.lucasmatheus.projetoitau.domain.model.PaymentMethod
import com.lucasmatheus.projetoitau.domain.model.PolicyRequest
import com.lucasmatheus.projetoitau.domain.model.SalesChannel
import com.lucasmatheus.projetoitau.domain.model.Status
import com.lucasmatheus.projetoitau.domain.ports.`in`.CreateRequestCommand
import com.lucasmatheus.projetoitau.domain.ports.`in`.CreateRequestUseCase
import com.lucasmatheus.projetoitau.domain.ports.`in`.CreatedRequest
import com.lucasmatheus.projetoitau.domain.ports.out.ClockProvider
import com.lucasmatheus.projetoitau.domain.ports.out.PolicyRequestEventPublisher
import com.lucasmatheus.projetoitau.domain.ports.out.PolicyRequestRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.UUID

@Service
class CreateRequestService(
    private val repo: PolicyRequestRepository,
    private val clock: ClockProvider,
    private val publisher: PolicyRequestEventPublisher
): CreateRequestUseCase {
    private val log = LoggerFactory.getLogger(javaClass)
    @Transactional
    override fun create(cmd: CreateRequestCommand): CreatedRequest {
        val now = clock.now()
        val request = PolicyRequest(
            id = UUID.randomUUID(),
            customerId = cmd.customerId,
            productId = cmd.productId,
            category = Category.valueOf(cmd.category),
            paymentMethod = PaymentMethod.valueOf(cmd.paymentMethod),
            totalMonthlyPremiumAmount = cmd.totalMonthlyPremiumAmount.toBigDecimal(),
            insuredAmount = cmd.insuredAmount.toBigDecimal(),
            coverages = cmd.coverages.mapValues { it.value.toBigDecimal() },
            assistances = cmd.assistances,
            status = Status.RECEIVED,
            createdAt = now,
            finishedAt = null,
            history = listOf(HistoryEntry(Status.RECEIVED, now)),
            salesChannel = SalesChannel.valueOf(cmd.salesChannel.uppercase())
        )

        val savedRequest = repo.save(request)

        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                publisher.publishCreated(
                    requestId = savedRequest.id,
                    customerId = savedRequest.customerId,
                    createdAt = now
                )
            }
        })

        log.info("A POLICY FOI CRIADA E EU PUBLIQUEI O EVENTO DE CRIAÇÃO")
        return CreatedRequest(savedRequest.id,now)
    }


}