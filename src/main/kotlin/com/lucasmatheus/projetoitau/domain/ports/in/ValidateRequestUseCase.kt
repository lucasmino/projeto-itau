package com.lucasmatheus.projetoitau.domain.ports.`in`

import com.lucasmatheus.projetoitau.domain.model.Status
import java.util.UUID

interface ValidateRequestUseCase {
    fun validate(id: UUID) : ValidationResult
}

data class ValidationResult(
    val id: UUID,
    val previousStatus: Status,
    val newStatus: Status,
    val changed: Boolean
)