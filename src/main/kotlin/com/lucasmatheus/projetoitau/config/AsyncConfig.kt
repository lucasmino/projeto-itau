// src/main/kotlin/com/lucasmatheus/projetoitau/config/AsyncConfig.kt
package com.lucasmatheus.projetoitau.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig {
    @Bean("validationExecutor")
    fun validationExecutor(): Executor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 2
            maxPoolSize = 4
            setQueueCapacity(100)
            setThreadNamePrefix("validation-")
            initialize()
        }
}
