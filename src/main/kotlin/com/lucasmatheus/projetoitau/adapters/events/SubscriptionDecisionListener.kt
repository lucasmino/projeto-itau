package com.lucasmatheus.projetoitau.adapters.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.lucasmatheus.projetoitau.domain.ports.`in`.ProcessSubscriptionDecisionUseCase
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class SubscriptionDecisionListener(
    private val useCase: ProcessSubscriptionDecisionUseCase,
    private val om: ObjectMapper = jacksonObjectMapper()
) {
    private val log = LoggerFactory.getLogger(javaClass)

    data class SubEvent(val requestId: UUID, val authorized: Boolean, val decidedAt: Instant)

    @RabbitListener(queues = ["\${subscriptions.queue}"])
    fun handle(message: String) {
        log.info("receiving message from subscription queue $message")
        val e = om.readValue<SubEvent>(message)
        useCase.processSubscriptionDecision(e.requestId, e.authorized, e.decidedAt)
    }
}