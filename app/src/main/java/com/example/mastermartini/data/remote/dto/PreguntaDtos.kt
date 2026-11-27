package com.example.mastermartini.data.remote.dto

// DTOs de petición para hacer una pregunta y para responderla
data class PreguntaRequest(
    val texto: String
)

data class RespuestaRequest(
    val respuesta: String
)
