package com.example.mastermartini.model

// MOLDE DE DATOS DE UN MÓDULO
// Un módulo agrupa varias lecciones de un mismo tema (ej. "Fundamentos del chocolate").
// Sirve para ordenar el curso en partes, como los capítulos de un libro.
data class Modulo(
    val titulo: String,
    // Lecciones del módulo, en el orden en que se ven
    val lecciones: List<Leccion>
) {
    // PROPIEDAD CALCULADA: suma los minutos de todas sus lecciones.
    // sumOf recorre la lista y va sumando el valor que se le indica (los minutos de cada lección).
    // La usa el Detalle para mostrar "3 lecciones · 30 min" en el encabezado del módulo.
    val totalMinutos: Int
        get() = lecciones.sumOf { leccion -> leccion.minutos }
}
