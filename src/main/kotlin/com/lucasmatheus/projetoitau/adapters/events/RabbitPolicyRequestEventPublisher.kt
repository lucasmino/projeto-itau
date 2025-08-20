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
    @Value("\${app.rabbit.routing-key}") private val routingKey: String,
    @Value("\${app.rabbit.routing-key-policy.request.statusChanged}") private val statusChangedRoutingKey: String,
    ) : PolicyRequestEventPublisher {

    private data class RequestCreatedPayload(
        val event: String = "REQUEST_CREATED",
        val requestId: UUID,
        val customerId: UUID,
        val createdAt: Instant
    )

    private data class StatusChangedPayload(
        val event: String = "REQUEST_STATUS_CHANGED",
        val requestId: UUID,
        val previousStatus: String,
        val newStatus: String,
        val changedAt: Instant
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
        requestId: UUID,
        previousStatus: String,
        newStatus: String,
        changedAt: Instant
    ) {
        val payload = StatusChangedPayload(
            requestId = requestId,
            previousStatus = previousStatus,
            newStatus = newStatus,
            changedAt = changedAt
        )
        val json = objectMapper.writeValueAsString(payload)
        rabbitTemplate.convertAndSend(exchange, statusChangedRoutingKey, json)
    }


}
