package com.lucasmatheus.projetoitau.unit.application

import com.lucasmatheus.projetoitau.application.GetRequestService
import com.lucasmatheus.projetoitau.domain.model.*
import com.lucasmatheus.projetoitau.domain.ports.out.PolicyRequestRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@DisplayName("GetRequestService")
class GetRequestServiceTest {

    private val repo = mock<PolicyRequestRepository>()
    private val svc = GetRequestService(repo)

    private fun sampleRequest(
        id: UUID = UUID.randomUUID(),
        customerId: UUID = UUID.randomUUID(),
    ) = PolicyRequest(
        id = id,
        customerId = customerId,
        productId = UUID.randomUUID(),
        category = Category.AUTO,
        paymentMethod = PaymentMethod.CREDIT_CARD,
        totalMonthlyPremiumAmount = BigDecimal("150.00"),
        insuredAmount = BigDecimal("50000.00"),
        coverages = mapOf("BASIC" to BigDecimal("150.00")),
        assistances = listOf("TOW", "GLASS"),
        status = Status.RECEIVED,
        createdAt = Instant.parse("2025-08-16T01:00:00Z"),
        finishedAt = null,
        history = listOf(HistoryEntry(Status.RECEIVED, Instant.parse("2025-08-16T01:00:00Z"))),
        salesChannel = SalesChannel.ONLINE
    )

    @Test
    @DisplayName("getById deve retornar o PolicyRequest quando existir")
    fun getById_found() {
        val id = UUID.randomUUID()
        val req = sampleRequest(id = id)

        whenever(repo.findById(id)).thenReturn(req)

        val out = svc.getById(id)

        assertNotNull(out)
        assertEquals(id, out!!.id)

        verify(repo).findById(id)
        verifyNoMoreInteractions(repo)
    }

    @Test
    @DisplayName("getById deve retornar null quando não existir")
    fun getById_notFound() {
        val id = UUID.randomUUID()
        whenever(repo.findById(id)).thenReturn(null)

        val out = svc.getById(id)

        assertNull(out)

        verify(repo).findById(id)
        verifyNoMoreInteractions(repo)
    }

    @Test
    @DisplayName("getByCustomer deve retornar a lista de pedidos do cliente")
    fun getByCustomer() {
        val customerId = UUID.randomUUID()
        val r1 = sampleRequest(customerId = customerId)
        val r2 = sampleRequest(customerId = customerId)

        whenever(repo.findByCustomerId(customerId)).thenReturn(listOf(r1, r2))

        val out = svc.getByCustomer(customerId)

        assertEquals(2, out.size)
        assertTrue(out.all { it.customerId == customerId })

        verify(repo).findByCustomerId(customerId)
        verifyNoMoreInteractions(repo)
    }
}