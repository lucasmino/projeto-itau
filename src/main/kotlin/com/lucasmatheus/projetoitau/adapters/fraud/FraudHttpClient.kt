package com.lucasmatheus.projetoitau.adapters.fraud

import com.lucasmatheus.projetoitau.domain.ports.out.Classification
import com.lucasmatheus.projetoitau.domain.ports.out.FraudClient
import com.lucasmatheus.projetoitau.domain.ports.out.FraudResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.reactive.function.client.WebClient
import java.time.Instant
import java.util.*

class FraudHttpClient(
    private val webClient: WebClient,
    @Value("\${fraud.base-url}") private val baseUrl: String
) : FraudClient {
    override fun check(
        orderId: UUID,
        customerId: UUID
    ): FraudResult {
        val resp = webClient.get()
            .uri("$baseUrl/fraud/check?orderId=$orderId&customerId=$customerId")
            .retrieve()
            .bodyToMono(FraudResponse::class.java)
            .block()!!                          // simples: síncrono no use case
        return FraudResult(
            classification = resp.classification,
            occurrences = resp.occurrences.map {
                FraudResult.Occurrence(
                    id = it.id,
                    productId = it.productId,
                    type = it.type,
                    description = it.description,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            })
    }

    data class FraudResponse(
        val orderId: UUID,
        val customerId: UUID,
        val analyzedAt: Instant,
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