package com.lucasmatheus.projetoitau.adapters.fraud

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.lucasmatheus.projetoitau.domain.ports.`in`.ValidateRequestUseCase
import com.lucasmatheus.projetoitau.domain.ports.out.Classification
import com.lucasmatheus.projetoitau.domain.ports.out.FraudClient
import com.lucasmatheus.projetoitau.domain.ports.out.FraudResult
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Instant
import java.util.UUID

@Component
open class FraudHttpClient(
    private val webClient: WebClient,
    @Value("\${fraud.base-url}") private val baseUrl: String
) : FraudClient {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun check(orderId: UUID, customerId: UUID): FraudResult {
        val resp = webClient.post()
            .uri("$baseUrl/fraud/check")
            .bodyValue(mapOf("orderId" to orderId, "customerId" to customerId))
            .retrieve()
            .bodyToMono(FraudResponse::class.java)
            .block()!!   // ok para este caso síncrono

        return FraudResult(
            classification = resp.classification,
            occurrences = resp.occurrences.map {
                FraudResult.Occurrence(
                    id = it.id,
                    productId = it.productId,
                    type = it.type,
                    description = it.description,
                    createdAt = it.createdAt, updatedAt = it.updatedAt
                )
            }
        )
    }

    // DTOs do mock
    data class FraudResponse(
        val orderId: UUID,
        val customerId: UUID,
        val analyzedAt: String,
        val classification: Classification,
        val occurrences: List<Occurrence>
    ) {
        data class Occurrence(
            val id: UUID,
            val productId: Long,
            val type: String,
            val description: String,
            val createdAt: Instant,
            val updatedAt: Instant
        )
    }
}

@Component
open class PolicyRequestListener(
    private val validateRequest: ValidateRequestUseCase
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val om = jacksonObjectMapper()

    data class RequestCreatedMessage(
        val event: String,
        val requestId: UUID,
        val customerId: UUID,
        val createdAt: String
    )

    @RabbitListener(queues = ["\${requests.queue}"])
    open fun handleRequestCreated(message: String) {
        log.info("Mensagem recebida: $message")
        val m: RequestCreatedMessage = om.readValue(message)
        try {
        val result = validateRequest.validate(m.requestId)
        log.info(
            "ValidateRequestUseCase concluiu: previous=${result.previousStatus} " +
                    "new=${result.newStatus} changed=${result.changed}"
        ) }
        catch (e: Exception) {
            log.error("Erro ao validar requisição: ${m.requestId}", e)
            return
        }

    }
}