package com.example.mastermartini.model

// CUERPO DE ERROR QUE DEVUELVEN LOS MICROSERVICIOS (cursos y usuarios)
// Cuando el servidor rechaza una petición, responde un JSON como:
//     {"mensaje": "El título es obligatorio", "estado": 400}
// Gson lo convierte en este objeto (lo leen CapacitacionRepository.crearCapacitacion
// y UsuarioRepositoryRemoto.leerErrorDto).
// Todos los campos son opcionales (con "?" y null por defecto): si el servidor no manda
// alguno, la app no se cae. "estado" es String para aceptarlo venga como número o como texto.
data class ErrorDto(
    val mensaje: String? = null,
    val estado: String? = null,
    // Solo en errores de validación (400): qué dato está mal y por qué, ej.
    //     "campos": {"telefono": "El teléfono debe tener 9 dígitos y empezar con 9"}
    val campos: Map<String, String>? = null
)
