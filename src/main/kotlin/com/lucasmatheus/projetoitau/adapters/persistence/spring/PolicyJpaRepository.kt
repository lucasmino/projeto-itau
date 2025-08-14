package com.lucasmatheus.projetoitau.adapters.persistence.spring

import com.lucasmatheus.projetoitau.adapters.persistence.entity.PolicyRequestEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PolicyJpaRepository : JpaRepository<PolicyRequestEntity, UUID> {
    fun findByCustomerId(customerId: UUID): List<PolicyRequestEntity>
}