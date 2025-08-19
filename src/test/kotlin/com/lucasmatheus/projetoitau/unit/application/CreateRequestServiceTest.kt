package com.lucasmatheus.projetoitau.unit.application

import com.lucasmatheus.projetoitau.application.CreateRequestService
import com.lucasmatheus.projetoitau.domain.model.Category
import com.lucasmatheus.projetoitau.domain.model.PaymentMethod
import com.lucasmatheus.projetoitau.domain.model.PolicyRequest
import com.lucasmatheus.projetoitau.domain.model.SalesChannel
import com.lucasmatheus.projetoitau.domain.model.Status
import com.lucasmatheus.projetoitau.domain.ports.`in`.CreateRequestCommand
import com.lucasmatheus.projetoitau.domain.ports.out.ClockProvider
import com.lucasmatheus.projetoitau.domain.ports.out.PolicyRequestRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

import java.time.Instant
import java.util.UUID

class CreateRequestServiceTest {

    private val repo = mock<PolicyRequestRepository>()
    private val clock = mock<ClockProvider>()
    private val svc = CreateRequestService( repo, clock)

    @Test
    @DisplayName("create() deve persistir RECEIVED e retornar id/createdAt")
    fun create_persists_and_returns_created() {
        val now = Instant.parse("2025-08-16T01:00:00Z")
        whenever(clock.now()).thenReturn(now)

        argumentCaptor<PolicyRequest>().apply {
            whenever(repo.save(capture())).thenAnswer { firstValue }
        }

        val cmd = CreateRequestCommand(
            customerId = UUID.randomUUID(),
            productId = UUID.randomUUID(),
            category = Category.AUTO.name,
            paymentMethod = PaymentMethod.CREDIT_CARD.name,
            salesChannel = SalesChannel.ONLINE.name,
            totalMonthlyPremiumAmount = "150.00",
            insuredAmount = "50000.00",
            coverages = mapOf("BASIC" to "150.00"),
            assistances = listOf("TOW", "GLASS")
        )

        val out = svc.create(cmd)

        assertTrue(out.id.toString().isNotBlank())
        assertEquals(now.toString(), out.createdAt.toString())

        argumentCaptor<PolicyRequest>().apply {
            verify(repo).save(capture())
            val saved = firstValue
            assertEquals(Status.RECEIVED, saved.status)
            assertEquals(now, saved.createdAt)
            assertEquals(null, saved.finishedAt)
            assertEquals(2, saved.assistances.size)
            assertTrue(saved.history.last().status == Status.RECEIVED)
        }

        verifyNoMoreInteractions(repo)
    }
}