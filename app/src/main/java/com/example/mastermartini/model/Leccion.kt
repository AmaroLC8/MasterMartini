package com.example.mastermartini.model

// MOLDE DE DATOS DE UNA LECCIÓN
// Es la parte más pequeña de un curso online: un video corto sobre un solo tema.
// Varias lecciones forman un Modulo, y varios módulos forman una Capacitacion.
data class Leccion(
    val titulo: String,
    // Qué se aprende en la lección (se mostrará en la fase 2, al abrir la lección)
    val descripcion: String,
    // Duración como texto, ej. "12 min"
    val duracion: String,
    // Dirección del video en la nube. Es String? porque todavía no hay videos:
    // "= null" es su valor por defecto. Se llenará en la fase 4 (AWS S3).
    val videoUrl: String? = null
) {
    // PROPIEDAD CALCULADA: no se guarda, se calcula cada vez que se pide.
    // Saca los minutos del texto: "12 min" -> 12. Igual que minutosDe() en RecetasViewModel:
    // filter { it.isDigit() } deja solo los números y toIntOrNull() lo pasa a Int.
    // Si el texto no tiene números, se usa 0 (con ?:) para que la app no se caiga.
    val minutos: Int
        get() = duracion.filter { it.isDigit() }.toIntOrNull() ?: 0
}
