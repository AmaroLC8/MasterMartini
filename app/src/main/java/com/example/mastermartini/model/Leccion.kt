package com.example.mastermartini.model

// Modelo de lección individual perteneciente a un módulo de capacitación
data class Leccion(
    val titulo: String,
    val descripcion: String,
    val duracion: String,
    val videoUrl: String? = null
) {
    val minutos: Int
        get() = duracion.filter { it.isDigit() }.toIntOrNull() ?: 0
}
