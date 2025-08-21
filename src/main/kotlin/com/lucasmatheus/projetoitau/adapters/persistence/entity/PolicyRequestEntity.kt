package com.lucasmatheus.projetoitau.adapters.persistence.entity

import com.lucasmatheus.projetoitau.domain.model.Category
import com.lucasmatheus.projetoitau.domain.model.PaymentMethod
import com.lucasmatheus.projetoitau.domain.model.SalesChannel
import com.lucasmatheus.projetoitau.domain.model.Status
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "policy_request")
open class PolicyRequestEntity(
    @Id
    var id: UUID,

    @Column(nullable = false)
    var customerId: UUID,

    @Column(nullable = false)
    var productId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: Category,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var paymentMethod: PaymentMethod,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var salesChannel: SalesChannel,

    @Column(nullable = false, precision = 19, scale = 2)
    var totalMonthlyPremiumAmount: BigDecimal,

    @Column(nullable = false, precision = 19, scale = 2)
    var insuredAmount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: Status,

    @Column(nullable = false)
    var createdAt: Instant,

    @Column(name = "finished_at")
    var finishedAt: Instant? = null,

    // novos campos para o fluxo de aprovação
    @Column(name = "payment_confirmed_at")
    var paymentConfirmedAt: Instant? = null,

    @Column(name = "subscription_authorized_at")
    var subscriptionAuthorizedAt: Instant? = null,
) {
    // Optimistic locking para eventos assíncronos concorrentes
    @Version
    var version: Long? = null

    // coverages: Map<String, BigDecimal>
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "policy_coverages", joinColumns = [JoinColumn(name = "policy_id")])
    @MapKeyColumn(name = "name")
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    var coverages: MutableMap<String, BigDecimal> = mutableMapOf()

    // assistances: List<String>
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "policy_assistances", joinColumns = [JoinColumn(name = "policy_id")])
    @Column(name = "assistance_value", nullable = false)
    var assistances: MutableList<String> = mutableListOf()

    // history: List<HistoryEntryEmbeddable>
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "policy_history", joinColumns = [JoinColumn(name = "policy_id")])
    var history: MutableList<HistoryEntryEmbeddable> = mutableListOf()
}

@Embeddable
class HistoryEntryEmbeddable(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: Status,

    @Column(nullable = false)
    var timestamp: Instant
)
