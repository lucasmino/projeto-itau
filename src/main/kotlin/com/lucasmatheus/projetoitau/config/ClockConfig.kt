package com.lucasmatheus.projetoitau.config

import com.lucasmatheus.projetoitau.domain.ports.out.ClockProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant

@Configuration
class ClockConfig {
    @Bean
    fun clockProvider(): ClockProvider = object : ClockProvider {
        override fun now(): Instant = Instant.now()
    }
}