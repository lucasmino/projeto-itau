package com.lucasmatheus.projetoitau.adapters.persistence

import com.lucasmatheus.projetoitau.adapters.persistence.mapper.PersistenceMapper
import com.lucasmatheus.projetoitau.adapters.persistence.spring.PolicyJpaRepository
import com.lucasmatheus.projetoitau.domain.model.PolicyRequest
import com.lucasmatheus.projetoitau.domain.ports.out.PolicyRequestRepository
import org.springframework.stereotype.Repository

@Repository
class PersistenceAdapter(private val jpa: PolicyJpaRepository) : PolicyRequestRepository {

    override fun save(request: PolicyRequest): PolicyRequest {
        val saved = jpa.save(PersistenceMapper.toEntity(request))
        return PersistenceMapper.toDomain(saved)
    }

    override fun findById(id: java.util.UUID): PolicyRequest? =
        jpa.findById(id).map(PersistenceMapper::toDomain).orElse(null)


    override fun findByCustomerId(customerId: java.util.UUID): List<PolicyRequest> =
        jpa.findByCustomerId(customerId).map(PersistenceMapper::toDomain)


}