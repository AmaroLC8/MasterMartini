package com.example.mastermartini.model

// Modelo de inscripción y progreso del usuario en un curso online
data class Inscripcion(
    val id: Long = 0,
    val usuarioId: Long = 0,
    val cursoId: Int = 0,
    val cursoTitulo: String? = null,
    val fechaInscripcion: String? = null,
    val leccionesCompletadas: List<String> = emptyList()
) {
    fun estaCompletada(modulo: Int, leccion: Int): Boolean {
        return leccionesCompletadas.contains(claveLeccion(modulo, leccion))
    }

    // Calcula el porcentaje de avance sin exceder el 100%
    fun porcentaje(totalLecciones: Int): Int {
        if (totalLecciones == 0) return 0
        return (leccionesCompletadas.size * 100 / totalLecciones).coerceAtMost(100)
    }

    companion object {
        fun claveLeccion(modulo: Int, leccion: Int): String {
            return "$modulo-$leccion"
        }
    }
}
