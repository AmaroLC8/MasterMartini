package com.example.mastermartini.data

// Excepción HTTP personalizada para respuestas de error de los microservicios
class ServidorException(
    val codigo: Int,
    val mensajeServidor: String?
) : Exception("Error $codigo: verifica permisos o campos")
