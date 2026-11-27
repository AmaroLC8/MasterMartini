package com.example.mastermartini.model

// Pregunta de un alumno al instructor de un curso, con su respuesta si ya la tiene
data class Pregunta(
    val id: Long = 0,
    val cursoId: Int = 0,
    val cursoTitulo: String? = null,
    val usuarioId: Long = 0,
    val autorNombre: String? = null,
    val texto: String = "",
    val respuesta: String? = null,
    val respondidaPor: String? = null,
    val fechaPregunta: String? = null,
    val fechaRespuesta: String? = null
) {
    // Una pregunta está respondida cuando el servidor trae un texto de respuesta
    val estaRespondida: Boolean
        get() = !respuesta.isNullOrBlank()

    // Fecha de la pregunta lista para mostrar, por ejemplo "27 nov 2026"
    val fechaLegible: String
        get() = formatearFecha(fechaPregunta)

    companion object {
        const val MAX_CARACTERES = 500

        private val MESES = listOf(
            "ene", "feb", "mar", "abr", "may", "jun",
            "jul", "ago", "sep", "oct", "nov", "dic"
        )

        // Convierte "2026-11-27T10:30:00" en "27 nov 2026"; si el texto no sirve devuelve ""
        fun formatearFecha(fechaIso: String?): String {
            if (fechaIso == null || fechaIso.length < 10) return ""
            val anio = fechaIso.substring(0, 4).toIntOrNull()
            val mes = fechaIso.substring(5, 7).toIntOrNull()
            val dia = fechaIso.substring(8, 10).toIntOrNull()
            if (anio == null || mes == null || dia == null || mes !in 1..12) return ""
            return "$dia ${MESES[mes - 1]} $anio"
        }
    }
}
