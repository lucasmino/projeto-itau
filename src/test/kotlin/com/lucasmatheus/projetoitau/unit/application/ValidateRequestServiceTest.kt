package com.lucasmatheus.projetoitau.unit.application


import com.lucasmatheus.projetoitau.application.ValidateRequestService
import com.lucasmatheus.projetoitau.domain.model.*
import com.lucasmatheus.projetoitau.domain.ports.out.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*


class ValidateRequestServiceTest {

    private val repo = mock<PolicyRequestRepository>()
    private val fraud = mock<FraudClient>()
    private val clock = mock<ClockProvider>()
    private val publisher = mock<PolicyRequestEventPublisher>()
    private val svc = ValidateRequestService(clock, fraud,repo, publisher )

    @Test
    fun `HIGH_RISK - rejects and finishes`() {
        val id = UUID.randomUUID()
        val now = Instant.parse("2025-08-16T01:00:00Z")
        val req = sampleRequest(id = id)

        whenever(repo.findById(id)).thenReturn(req)
        whenever(clock.now()).thenReturn(now)
        whenever(fraud.check(any(), any())).thenReturn(
            FraudResult(Classification.HIGH_RISK, emptyList())
        )

        val out = svc.validate(id)


        assertEquals(Status.REJECTED, out.newStatus)
        assertTrue(out.changed)

        verify(repo).findById(id)
        verify(fraud).check(eq(id), eq(req.customerId))

        val captor = argumentCaptor<PolicyRequest>()
        verify(repo).save(captor.capture())

        val saved  = captor.firstValue   // estado intermediário

        assertEquals(Status.REJECTED, saved.status)
        assertEquals(now, saved.finishedAt)
        assertEquals(Status.REJECTED, saved.history.last().status)
        verifyNoMoreInteractions(repo, fraud)
    }

    @Test
    fun `idempotent - already final`() {
        val id = UUID.randomUUID()
        whenever(repo.findById(id)).thenReturn(sampleRequest(id, Status.APPROVED))

        val out = svc.validate(id)

        assertFalse(out.changed)
        verify(repo, never()).save(any())
        verify(fraud, never()).check(any(), any())
    }

    fun sampleRequest(
        id: UUID = UUID.randomUUID(),
        status: Status = Status.RECEIVED,
        customerId: UUID = UUID.randomUUID(),
        productId: UUID = UUID.randomUUID(),
        createdAt: Instant = Instant.now(),
        finishedAt: Instant? = null
    ): PolicyRequest {
        return PolicyRequest(
            id = id,
            customerId = customerId,
            productId = productId,
            category = Category.AUTO, // ou outro valor default
            paymentMethod = PaymentMethod.CREDIT_CARD,
            totalMonthlyPremiumAmount = BigDecimal("100.00"),
            insuredAmount = BigDecimal("1000.00"),
            coverages = mapOf("basic" to BigDecimal("500.00")),
            assistances = listOf("24h assistance"),
            status = status,
            createdAt = createdAt,
            finishedAt = finishedAt,
            history = listOf(), // vazio no começo, ou pode simular uma entrada
            salesChannel = SalesChannel.ONLINE
        )
    }


}
