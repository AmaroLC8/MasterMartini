package com.example.mastermartini.data

// ERROR QUE RESPONDE UN MICROSERVICIO (excepción personalizada)
// Antes se llamaba CursoServidorException, pero ahora la usan dos repositorios
// (CapacitacionRepository e InscripcionRepository), por eso el nombre ya no es solo de cursos.
//
// Significa: el servidor SÍ respondió, pero con un código de error (403, 404, 409, 503...).
// Guarda el código HTTP y el mensaje del ErrorDto (si vino), para que cada ViewModel
// decida qué texto mostrar según el código. Ejemplos:
//     403 -> no tiene permiso (o le falta el Plan Pro)    409 -> ya existe (ya está inscrito)
//     404 -> no existe                                     503 -> otro servicio está apagado
// message (el texto por defecto) se usa solo si el servidor no mandó ningún mensaje.
class ServidorException(
    val codigo: Int,
    val mensajeServidor: String?
) : Exception("Error $codigo: verifica permisos o campos")
