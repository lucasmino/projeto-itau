package com.lucasmatheus.projetoitau.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RabbitConfig(
    @Value("\${app.rabbit.exchange}") private val exchangeName: String,
    @Value("\${requests.queue}") private val requestsQueueName: String,
    @Value("\${app.rabbit.routing-key}") private val routingKey: String
) {

    @Bean
    open fun rabbitAdmin(cf: ConnectionFactory): AmqpAdmin =
        RabbitAdmin(cf).apply { isAutoStartup = true }

    @Bean
    open fun policyExchange(): TopicExchange =
        TopicExchange(exchangeName, true, false)

    /** Fila que recebe eventos de criação de request */
    @Bean
    open fun policyQueue(): Queue =
        Queue(requestsQueueName, true, false, false)

    /** Binding EXPLÍCITO apenas da policyQueue com o exchange/routingKey */
    @Bean
    open fun bindingPolicyQueue(
        @Qualifier("policyQueue") queue: Queue,
        exchange: TopicExchange
    ): Binding = BindingBuilder.bind(queue).to(exchange).with(routingKey)

    /** Template padrão (pode configurar converter JSON se desejar) */
    @Bean
    open fun rabbitTemplate(cf: ConnectionFactory): RabbitTemplate =
        RabbitTemplate(cf)

    /** Filas de resultados externos (não precisam de binding explícito) */
    @Bean
    open fun paymentsQueue(@Value("\${payments.queue}") name: String): Queue =
        Queue(name, true, false, false)

    @Bean
    open fun subscriptionsQueue(@Value("\${subscriptions.queue}") name: String): Queue =
        Queue(name, true, false, false)
}
