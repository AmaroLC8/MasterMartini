package com.example.mastermartini.model

// CUERPO DE ERROR QUE DEVUELVE EL MICROSERVICIO DE CURSOS
// Cuando el servidor rechaza una petición, responde un JSON como:
//     {"mensaje": "El título es obligatorio", "estado": 400}
// Gson lo convierte en este objeto (lo lee CapacitacionRepository.crearCapacitacion).
// Los dos campos son opcionales (con "?" y null por defecto): si el servidor no manda
// alguno, la app no se cae. "estado" es String para aceptarlo venga como número o como texto.
data class ErrorDto(
    val mensaje: String? = null,
    val estado: String? = null
)
