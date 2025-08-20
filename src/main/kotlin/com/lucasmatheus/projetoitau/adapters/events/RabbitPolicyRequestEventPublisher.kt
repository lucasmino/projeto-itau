package com.lucasmatheus.projetoitau.adapters.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.lucasmatheus.projetoitau.domain.ports.out.PolicyRequestEventPublisher
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class RabbitPolicyRequestEventPublisher(
    private val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${app.rabbit.exchange}") private val exchange: String,
    @Value("\${app.rabbit.routing-key}") private val routingKey: String
) : PolicyRequestEventPublisher {

    private data class RequestCreatedPayload(
        val event: String = "REQUEST_CREATED",
        val requestId: UUID,
        val customerId: UUID,
        val createdAt: Instant
    )

    override fun publishCreated(requestId: UUID, customerId: UUID, createdAt: Instant) {
        val payload = RequestCreatedPayload(
            requestId = requestId,
            customerId = customerId,
            createdAt = createdAt
        )
        val json = objectMapper.writeValueAsString(payload)
        rabbitTemplate.convertAndSend(exchange, routingKey, json)
    }

    override fun publishStatusChanged(
        requestId: String,
        previousStatus: String,
        newStatus: String,
        changedAt: Instant
    ) {
        TODO("Not yet implemented")
    }


}
