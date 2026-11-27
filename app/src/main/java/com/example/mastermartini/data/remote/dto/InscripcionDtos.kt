package com.example.mastermartini.data.remote.dto

// DTOs de petición para registrar inscripciones y actualizar progreso de lecciones
data class InscripcionRequest(
    val cursoId: Int
)

data class ProgresoLeccionRequest(
    val modulo: Int,
    val leccion: Int,
    val completada: Boolean
)
