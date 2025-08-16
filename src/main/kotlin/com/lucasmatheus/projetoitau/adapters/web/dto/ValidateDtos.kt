package com.lucasmatheus.projetoitau.adapters.web.dto

data class ValidateResponse(
    val id: String,
    val previousStatus: String,
    val newStatus: String,
    val changed: Boolean
)
