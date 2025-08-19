package com.lucasmatheus.projetoitau.jpa

import com.lucasmatheus.projetoitau.adapters.persistence.entity.HistoryEntryEmbeddable
import com.lucasmatheus.projetoitau.adapters.persistence.entity.PolicyRequestEntity
import com.lucasmatheus.projetoitau.adapters.persistence.spring.PolicyJpaRepository
import com.lucasmatheus.projetoitau.domain.model.Category
import com.lucasmatheus.projetoitau.domain.model.PaymentMethod
import com.lucasmatheus.projetoitau.domain.model.SalesChannel
import com.lucasmatheus.projetoitau.domain.model.Status
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@DataJpaTest
@DisplayName("PolicyJpaRepository (JPA)")
class PolicyRequestRepositoryJpaTest @Autowired constructor(
    private val jpa: PolicyJpaRepository
) {

    private fun entitySample(
        id: UUID = UUID.randomUUID(),
        customerId: UUID = UUID.randomUUID(),
        createdAt: Instant = Instant.parse("2025-08-16T01:00:00Z"),
    ): PolicyRequestEntity {
        val e = PolicyRequestEntity(
            id = id,
            customerId = customerId,
            productId = UUID.randomUUID(),
            category = Category.AUTO,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            salesChannel = SalesChannel.ONLINE,
            totalMonthlyPremiumAmount = BigDecimal("150.00"),
            insuredAmount = BigDecimal("50000.00"),
            status = Status.RECEIVED,
            createdAt = createdAt,
            finishedAt = null
        )

        // preencher coleções (não estão no construtor)
        e.coverages["BASIC"] = BigDecimal("150.00")
        e.assistances.addAll(listOf("TOW", "GLASS"))
        e.history.add(HistoryEntryEmbeddable(Status.RECEIVED, createdAt))

        return e
    }

    @Test
    @DisplayName("deve salvar e recuperar por id")
    fun save_and_findById() {
        val e = entitySample()
        jpa.saveAndFlush(e)

        val found = jpa.findById(e.id).orElse(null)
        assertNotNull(found)
        assertEquals(e.id, found!!.id)
        assertEquals(Status.RECEIVED, found.status)
        assertEquals(BigDecimal("150.00"), found.totalMonthlyPremiumAmount)
        assertEquals(BigDecimal("50000.00"), found.insuredAmount)
        assertEquals(1, found.coverages.size)
        assertEquals(2, found.assistances.size)
        assertEquals(1, found.history.size)
        assertNull(found.finishedAt)
    }

    @Test
    @DisplayName("deve consultar por customerId")
    fun findByCustomerId() {
        val customerA = UUID.randomUUID()
        val e1 = entitySample(customerId = customerA)
        val e2 = entitySample(customerId = customerA)
        val eOther = entitySample(customerId = UUID.randomUUID())

        jpa.saveAll(listOf(e1, e2, eOther))
        jpa.flush()

        val list = jpa.findByCustomerId(customerA)
        assertEquals(2, list.size)
        assertTrue(list.all { it.customerId == customerA })
    }
}
