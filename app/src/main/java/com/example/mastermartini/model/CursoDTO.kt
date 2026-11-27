package com.example.mastermartini.model

// DTO para mapear cursos del microservicio al modelo Curso
data class CursoDto(
    val id: Int = 0,
    val titulo: String? = null,
    val descripcion: String? = null,
    val categoria: String? = null,
    val nivel: String? = null,
    val instructor: String? = null,
    val duracionTotal: String? = null,
    val insumoPrincipal: String? = null,
    val videoResumenUrl: String? = null,
    val esVegano: Boolean = false,
    val loQueLograras: List<String>? = null,
    val incluye: List<String>? = null,
    val modulos: List<ModuloDto>? = null
) {
    // Convierte el DTO del microservicio al modelo de dominio con valores por defecto
    fun toCurso(): Curso {
        val pasosExtraidos = modulos
            ?.flatMap { it.lecciones.orEmpty() }
            ?.mapNotNull { it.descripcion ?: it.titulo }
            .orEmpty()

        val videoFinal = videoResumenUrl
            ?: modulos?.firstOrNull()?.lecciones?.firstOrNull()?.videoUrl

        return Curso(
            id = id,
            titulo = titulo ?: "Curso Master Martini",
            insumo = insumoPrincipal ?: "Insumo Master Martini",
            chef = instructor ?: "Instructor Master Martini",
            duracion = duracionTotal ?: "20 min",
            pasos = pasosExtraidos.ifEmpty {
                listOf("Revisa la ficha técnica y sigue las instrucciones del video.")
            },
            categoria = categoria ?: "Pastelería",
            descripcion = descripcion ?: "",
            ingredientes = incluye.orEmpty(),
            rendimiento = loQueLograras?.firstOrNull() ?: "Uso profesional B2B",
            dificultad = nivel ?: "Intermedio",
            esVegano = esVegano,
            videoUrl = videoFinal
        )
    }
}

data class ModuloDto(
    val titulo: String? = null,
    val lecciones: List<LeccionDto>? = null
)

data class LeccionDto(
    val titulo: String? = null,
    val descripcion: String? = null,
    val duracion: String? = null,
    val videoUrl: String? = null
)