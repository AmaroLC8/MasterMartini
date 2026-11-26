package com.example.mastermartini.data.remote.dto

// CUERPOS DE LAS PETICIONES AL MICROSERVICIO DE INTERACCIONES (@Body)
// Gson los convierte a JSON usando los nombres de las propiedades, que deben ser
// IGUALES a los del backend. Se llaman igual que en el backend para compararlos fácil.

// POST api/inscripciones -> {"cursoId": 4}
data class InscripcionRequest(
    val cursoId: Int
)

// PUT api/inscripciones/{id}/lecciones -> {"modulo": 0, "leccion": 2, "completada": true}
// modulo y leccion son POSICIONES desde 0 (Módulo 1 = 0). completada: true = vista, false = no vista.
data class ProgresoLeccionRequest(
    val modulo: Int,
    val leccion: Int,
    val completada: Boolean
)
