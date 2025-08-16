package com.lucasmatheus.projetoitau.adapters.web.dto

data class RequestSummary(
    val id: String,
    val status: String,
    val category: String,
    val paymentMethod: String,
    val salesChannel: String,
    val totalMonthlyPremiumAmount: String,
    val insuredAmount: String,
    val createdAt: String,
    val finishedAt: String?
)
