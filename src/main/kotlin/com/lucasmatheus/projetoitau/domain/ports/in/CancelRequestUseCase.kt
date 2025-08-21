package com.lucasmatheus.projetoitau.domain.ports.`in`

import com.lucasmatheus.projetoitau.domain.model.PolicyRequest
import java.util.UUID

interface CancelRequestUseCase {
    fun cancelById (id : UUID): PolicyRequest
    fun cancelByCustomer(id: UUID): PolicyRequest
}