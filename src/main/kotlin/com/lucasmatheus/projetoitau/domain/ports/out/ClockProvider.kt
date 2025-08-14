package com.lucasmatheus.projetoitau.domain.ports.out

import java.time.Instant

interface ClockProvider {
    fun now(): Instant
}