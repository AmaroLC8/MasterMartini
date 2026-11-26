package com.example.mastermartini.model

// DTO para respuestas de error HTTP devueltas por los microservicios
data class ErrorDto(
    val mensaje: String? = null,
    val estado: String? = null,
    val campos: Map<String, String>? = null
)
