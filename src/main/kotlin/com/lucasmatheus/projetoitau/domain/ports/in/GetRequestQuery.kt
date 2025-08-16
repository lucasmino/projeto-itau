package com.lucasmatheus.projetoitau.domain.ports.`in`

import com.lucasmatheus.projetoitau.domain.model.PolicyRequest
import java.util.UUID

interface GetRequestQuery {
    fun getById(id: UUID): PolicyRequest?
    fun getByCustomer(customerId: UUID): List<PolicyRequest>
}
