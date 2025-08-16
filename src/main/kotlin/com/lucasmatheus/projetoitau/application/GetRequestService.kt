package com.lucasmatheus.projetoitau.application

import com.lucasmatheus.projetoitau.domain.model.PolicyRequest
import com.lucasmatheus.projetoitau.domain.ports.`in`.GetRequestQuery
import com.lucasmatheus.projetoitau.domain.ports.out.PolicyRequestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
@Service
class GetRequestService(private val repository: PolicyRequestRepository) : GetRequestQuery {

    @Transactional(readOnly = true)
    override fun getById(id: UUID): PolicyRequest? = repository.findById(id)

    @Transactional(readOnly = true)
    override fun getByCustomer(customerId: UUID): List<PolicyRequest> = repository.findByCustomerId(customerId)
}