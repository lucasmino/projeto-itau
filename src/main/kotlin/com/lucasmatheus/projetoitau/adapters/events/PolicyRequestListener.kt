package com.lucasmatheus.projetoitau.adapters.events

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.lucasmatheus.projetoitau.domain.ports.out.FraudClient
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.util.*

@Component
open class PolicyRequestListener(
    private val fraudClient: FraudClient
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val objectMapper = jacksonObjectMapper()

    data class RequestCreatedMessage(
        val event: String,
        val requestId: UUID,
        val customerId: UUID,
        val createdAt: String
    )

    @RabbitListener(queues = ["\${requests.queue}"])
    open fun handleRequestCreated(message: String) {
        log.info("Mensagem recebida: $message")

        try {
            // desserializa o JSON recebido do Rabbit
            val payload: RequestCreatedMessage = objectMapper.readValue(message)

            // chama o FraudClient já implementado
            val fraudResult = fraudClient.check(
                orderId = payload.requestId,
                customerId = payload.customerId
            )

            log.info("FraudClient respondeu: $fraudResult")

        } catch (ex: Exception) {
            log.error("Erro ao processar mensagem: $message", ex)
            throw ex // deixa o Rabbit saber que falhou
        }
    }
}
