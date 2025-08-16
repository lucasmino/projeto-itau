package com.lucasmatheus.projetoitau.adapters.web.mapper

import com.lucasmatheus.projetoitau.adapters.web.dto.*
import com.lucasmatheus.projetoitau.domain.model.PolicyRequest
import com.lucasmatheus.projetoitau.domain.ports.`in`.CreateRequestCommand
import com.lucasmatheus.projetoitau.domain.ports.`in`.ValidationResult
import java.util.*

/* body -> command */
fun CreatePolicyRequestBody.toCommand(): CreateRequestCommand =
    CreateRequestCommand(
        customerId = UUID.fromString(customerId),
        productId  = UUID.fromString(productId),
        category   = category,
        paymentMethod = paymentMethod,
        salesChannel  = salesChannel,
        totalMonthlyPremiumAmount = totalMonthlyPremiumAmount.toString(),
        insuredAmount = insuredAmount.toString(),
        coverages  = coverages,
        assistances = assistances
    )

/* domain -> summaries */
fun PolicyRequest.toSummary(): RequestSummary =
    RequestSummary(
        id = id.toString(),
        status = status.name,
        category = category.name,
        paymentMethod = paymentMethod.name,
        salesChannel = salesChannel.name,
        totalMonthlyPremiumAmount = totalMonthlyPremiumAmount.toPlainString(),
        insuredAmount = insuredAmount.toPlainString(),
        createdAt = createdAt.toString(),
        finishedAt = finishedAt?.toString()
    )

/* use case result -> response */
fun ValidationResult.toResponse(): ValidateResponse =
    ValidateResponse(
        id = id.toString(),
        previousStatus = previousStatus.name,
        newStatus = newStatus.name,
        changed = changed
    )
