package com.lucasmatheus.projetoitau.adapters.web.controller

import com.lucasmatheus.projetoitau.adapters.web.dto.CreatePolicyRequestBody
import com.lucasmatheus.projetoitau.adapters.web.dto.CreatePolicyRequestResponse
import com.lucasmatheus.projetoitau.adapters.web.dto.RequestSummary
import com.lucasmatheus.projetoitau.adapters.web.dto.ValidateResponse
import com.lucasmatheus.projetoitau.adapters.web.mapper.toCommand
import com.lucasmatheus.projetoitau.adapters.web.mapper.toResponse
import com.lucasmatheus.projetoitau.adapters.web.mapper.toSummary
import com.lucasmatheus.projetoitau.application.ValidateRequestService
import com.lucasmatheus.projetoitau.domain.ports.`in`.CancelRequestUseCase
import com.lucasmatheus.projetoitau.domain.ports.`in`.CreateRequestUseCase
import com.lucasmatheus.projetoitau.domain.ports.`in`.GetRequestQuery
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/requests")
class RequestController(
    private val createUseCase: CreateRequestUseCase,
    private val repo: GetRequestQuery,
    private val validateRequest: ValidateRequestService,
    private val cancelRequestService: CancelRequestUseCase
) {

    @PostMapping("/policy")
    @ResponseStatus(HttpStatus.CREATED)
    fun createPolicyRequest(@Valid @RequestBody body: CreatePolicyRequestBody): CreatePolicyRequestResponse {
        val out = createUseCase.create(body.toCommand())
        return CreatePolicyRequestResponse(
            id = out.id.toString(),
            createdAt = out.createdAt.toString()
        )
    }

    @GetMapping("{id}")
    fun getPolicyById(@PathVariable id: UUID): RequestSummary =
        repo.getById(id)!!.toSummary()


    @GetMapping
    fun getPolicyByCustommer(@RequestParam customerId: UUID): List<RequestSummary> =
        repo.getByCustomer(customerId).map { it.toSummary() }


    @PostMapping("/{id}/validate")
    fun validate(@PathVariable id: UUID): ResponseEntity<ValidateResponse> {
        val res = validateRequest.validate(id)
        val status = if (res.changed) HttpStatus.ACCEPTED else HttpStatus.OK
        return ResponseEntity.status(status).body(res.toResponse())
    }


    @PostMapping("/{id}/cancel")
    open fun cancelById(@PathVariable id: UUID): ResponseEntity<RequestSummary> {
        val updated = cancelRequestService.cancelById(id)           // lança exceções para cenários inválidos
        return ResponseEntity.ok(updated.toSummary())
    }

    @PostMapping("/{id}/cancel-by-customer")
    open fun cancelByCustomer(@PathVariable id: UUID): ResponseEntity<RequestSummary> {
        val updated = cancelRequestService.cancelByCustomer(id)     // idem
        return ResponseEntity.ok(updated.toSummary())
    }

}






