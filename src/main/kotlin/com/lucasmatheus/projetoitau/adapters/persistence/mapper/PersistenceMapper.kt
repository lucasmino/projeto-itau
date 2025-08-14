package com.lucasmatheus.projetoitau.adapters.persistence.mapper

import com.lucasmatheus.projetoitau.adapters.persistence.entity.*
import com.lucasmatheus.projetoitau.domain.model.*

object PersistenceMapper {

    fun toEntity(d: PolicyRequest): PolicyRequestEntity =
        PolicyRequestEntity(
            id = d.id,
            customerId = d.customerId,
            productId = d.productId,
            category = d.category,
            paymentMethod = d.paymentMethod,
            salesChannel = d.salesChannel,
            totalMonthlyPremiumAmount = d.totalMonthlyPremiumAmount,
            insuredAmount = d.insuredAmount,
            status = d.status,
            createdAt = d.createdAt,
            finishedAt = d.finishedAt
        ).also { e ->
            e.coverages = d.coverages.toMutableMap()
            e.assistances = d.assistances.toMutableList()
            e.history = d.history
                .map { HistoryEntryEmbeddable(it.status, it.timestamp) }
                .toMutableList()
        }

    fun toDomain(e: PolicyRequestEntity): PolicyRequest =
        PolicyRequest(
            id = e.id,
            customerId = e.customerId,
            productId = e.productId,
            category = e.category,
            paymentMethod = e.paymentMethod,
            salesChannel = e.salesChannel,
            totalMonthlyPremiumAmount = e.totalMonthlyPremiumAmount,
            insuredAmount = e.insuredAmount,
            status = e.status,
            createdAt = e.createdAt,
            finishedAt = e.finishedAt,
            coverages = e.coverages.toMap(),
            assistances = e.assistances.toList(),
            history = e.history.map { HistoryEntry(it.status, it.timestamp) }
        )
}