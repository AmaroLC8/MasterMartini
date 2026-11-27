package com.example.mastermartini.model

// Modelo de curso online compuesto por módulos y lecciones
data class Capacitacion(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val nivel: String,
    val esGratis: Boolean,
    val instructor: String,
    val instructorId: Long? = null,
    val duracionTotal: String,
    val insumoPrincipal: String,
    val videoResumenUrl: String? = null,
    val loQueLograras: List<String>,
    val incluye: List<String>,
    val modulos: List<Modulo>,
    val esVegano: Boolean = false
) {
    val totalLecciones: Int
        get() = modulos.sumOf { modulo -> modulo.lecciones.size }
}
