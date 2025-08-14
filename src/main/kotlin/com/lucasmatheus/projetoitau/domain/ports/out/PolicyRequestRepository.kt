package com.lucasmatheus.projetoitau.domain.ports.out

import com.lucasmatheus.projetoitau.domain.model.PolicyRequest
import java.util.UUID

interface PolicyRequestRepository {
    fun save(request: PolicyRequest): PolicyRequest
    fun findById(id: UUID): PolicyRequest?
    fun findByCustomerId(customerId: UUID): List<PolicyRequest>
}