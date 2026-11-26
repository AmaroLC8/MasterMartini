package com.example.mastermartini.model

// Modelo de módulo que agrupa lecciones dentro de una capacitación
data class Modulo(
    val titulo: String,
    val lecciones: List<Leccion>
) {
    val totalMinutos: Int
        get() = lecciones.sumOf { leccion -> leccion.minutos }
}
