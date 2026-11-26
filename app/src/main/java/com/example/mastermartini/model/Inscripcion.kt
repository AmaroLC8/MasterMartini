package com.example.mastermartini.model

// MOLDE DE DATOS DE UNA INSCRIPCIÓN A UN CURSO ONLINE
// Es lo que devuelve el microservicio de interacciones (puerto 9093). Ejemplo de JSON:
//     {"id": 7, "usuarioId": 1, "cursoId": 4, "cursoTitulo": "Hojaldre y croissants",
//      "fechaInscripcion": "2026-11-26T10:30:00", "leccionesCompletadas": ["0-0", "0-2"]}
// Gson lo convierte usando los NOMBRES de las propiedades (deben ser iguales a los del JSON).
// Todas tienen un valor por defecto: si el servidor no manda alguno, Gson usa ese valor.
data class Inscripcion(
    // Id de la inscripción (no del curso). Se usa para marcar lecciones y para cancelar.
    // Long, igual que los ids de usuario, porque así lo maneja la base de datos del servidor.
    val id: Long = 0,
    // Quién se inscribió (el usuario de la sesión)
    val usuarioId: Long = 0,
    // En qué curso. Int, igual que Capacitacion.id, para poder compararlos directo.
    val cursoId: Int = 0,
    // Título del curso al momento de inscribirse (lo guarda el servidor)
    val cursoTitulo: String? = null,
    // Fecha y hora como texto ("2026-11-26T10:30:00"). Solo se guarda, no se muestra por ahora.
    val fechaInscripcion: String? = null,
    // PROGRESO: lecciones vistas como textos "modulo-leccion", con posiciones desde 0.
    // Ej: "0-2" = Módulo 1, Lección 3. Así basta una lista de textos para guardar el avance.
    val leccionesCompletadas: List<String> = emptyList()
) {
    // ¿Esta lección está marcada como vista? Arma su clave ("0-2") y la busca en la lista.
    fun estaCompletada(modulo: Int, leccion: Int): Boolean {
        return leccionesCompletadas.contains(claveLeccion(modulo, leccion))
    }

    // Porcentaje de avance (de 0 a 100) según el total de lecciones del curso.
    // Si el total es 0 (curso sin lecciones) devuelve 0, para no dividir por cero.
    // Primero se multiplica por 100 y después se divide: con números enteros, 3 / 8 da 0,
    // pero 300 / 8 da 37. coerceAtMost(100): nunca pasa de 100 (por si al curso le quitaron lecciones).
    fun porcentaje(totalLecciones: Int): Int {
        if (totalLecciones == 0) return 0
        return (leccionesCompletadas.size * 100 / totalLecciones).coerceAtMost(100)
    }

    // companion object = lo que pertenece a la CLASE y no a una inscripción en particular.
    companion object {
        // Clave de una lección: módulo 0 y lección 2 -> "0-2". Es el formato que usa el servidor.
        fun claveLeccion(modulo: Int, leccion: Int): String {
            return "$modulo-$leccion"
        }
    }
}
