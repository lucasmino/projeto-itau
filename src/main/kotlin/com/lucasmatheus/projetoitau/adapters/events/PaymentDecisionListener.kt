package com.lucasmatheus.projetoitau.adapters.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.lucasmatheus.projetoitau.domain.ports.`in`.ProcessPaymentDecisionUseCase
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class PaymentDecisionListener(
    private val useCase: ProcessPaymentDecisionUseCase,
    private val om: ObjectMapper = jacksonObjectMapper()
) {
    private val log = LoggerFactory.getLogger(javaClass)
    data class PaymentEvent(val requestId: UUID, val approved: Boolean, val decidedAt: Instant)

    @RabbitListener(queues = ["\${payments.queue}"])
    fun handle(message: String) {
        log.info("receiving message from payments queue $message")
        val e = om.readValue<PaymentEvent>(message)
        useCase.processPaymentDecision(e.requestId, e.approved, e.decidedAt)
    }
}
