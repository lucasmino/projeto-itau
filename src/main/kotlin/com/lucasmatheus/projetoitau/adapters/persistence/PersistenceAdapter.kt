package com.lucasmatheus.projetoitau.adapters.persistence

import com.lucasmatheus.projetoitau.adapters.persistence.mapper.PersistenceMapper
import com.lucasmatheus.projetoitau.adapters.persistence.spring.PolicyJpaRepository
import com.lucasmatheus.projetoitau.domain.model.PolicyRequest
import com.lucasmatheus.projetoitau.domain.ports.out.PolicyRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class PersistenceAdapter(private val jpa: PolicyJpaRepository) : PolicyRequestRepository {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun save(request: PolicyRequest): PolicyRequest {
        val e = PersistenceMapper.toEntity(request)
        val existingVersion = jpa.findById(request.id).map { it.version }.orElse(null)
        e.version = existingVersion
        val saved = jpa.save(e)
        log.info("SALVEI NO BANCO COM O ID: ${saved.id} and version: ${saved.version}")

        return PersistenceMapper.toDomain(saved)
    }

    override fun findById(id: java.util.UUID): PolicyRequest? =
        jpa.findById(id).map(PersistenceMapper::toDomain).orElse(null)


    override fun findByCustomerId(customerId: java.util.UUID): List<PolicyRequest> =
        jpa.findByCustomerId(customerId).map(PersistenceMapper::toDomain)


}