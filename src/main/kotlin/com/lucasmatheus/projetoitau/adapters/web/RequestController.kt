package com.lucasmatheus.projetoitau.adapters.web

import com.lucasmatheus.projetoitau.domain.ports.`in`.CreateRequestCommand
import com.lucasmatheus.projetoitau.domain.ports.`in`.CreateRequestUseCase
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping()
class RequestController(
    private val createUseCase: CreateRequestUseCase
) {

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody body: CreateRequestBody): CreateRequestResponse {
        val out = createUseCase.create(
            CreateRequestCommand(
                customerId = UUID.fromString(body.customerId),
                productId = UUID.fromString(body.productId),
                category = body.category,
                paymentMethod = body.paymentMethod,
                salesChannel = body.salesChannel,
                totalMonthlyPremiumAmount = body.totalMonthlyPremiumAmount,
                insuredAmount = body.insuredAmount,
                coverages = body.coverages,
                assistances = body.assistances
            )
        )
        return CreateRequestResponse(out.id.toString(), out.createdAt.toString())
    }

    data class CreateRequestBody(
        @field:NotBlank val customerId: String,
        @field:NotBlank val productId: String,
        @field:NotBlank val category: String,
        @field:NotBlank val paymentMethod: String,
        @field:NotBlank val salesChannel: String,
        @field:NotBlank val totalMonthlyPremiumAmount: String,
        @field:NotBlank val insuredAmount: String,
        @field:Size(min = 1) val coverages: Map<String, String>,
        val assistances: List<String> = emptyList()
    )

    data class CreateRequestResponse(
        val id: String,
        val createdAt: String
    )
}