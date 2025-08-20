package com.lucasmatheus.projetoitau.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.annotation.Value

@Configuration
open class RabbitConfig(
    @Value("\${app.rabbit.exchange}") private val exchangeName: String,
    @Value("\${requests.queue}") private val queueName: String,
    @Value("\${app.rabbit.routing-key}") private val routingKey: String
) {

    @Bean
    open fun rabbitAdmin(cf: ConnectionFactory): AmqpAdmin =
        RabbitAdmin(cf).apply { setAutoStartup(true) }   // <- garante declaração no startup

    @Bean
    open fun policyExchange(): TopicExchange =
        TopicExchange(exchangeName, true, false)

    @Bean
    open fun policyQueue(): Queue =
        Queue(queueName, true, false, false)

    @Bean
    open fun binding(queue: Queue, exchange: TopicExchange): Binding =
        BindingBuilder.bind(queue).to(exchange).with(routingKey)

    @Bean
    open fun rabbitTemplate(cf: ConnectionFactory): RabbitTemplate =
        RabbitTemplate(cf) // se quiser, pode setar converter JSON aqui
}
