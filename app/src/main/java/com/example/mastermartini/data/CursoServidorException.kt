package com.example.mastermartini.data

// ERROR DEL MICROSERVICIO DE CURSOS AL CREAR UN CURSO (excepción personalizada)
// El servidor SÍ respondió, pero con un código de error (403, 503, 400...).
// Guarda el código HTTP y el mensaje del ErrorDto (si vino), para que CrearCursoViewModel
// decida qué texto mostrar según el código:
//     403 -> el usuario no tiene permiso      503 -> el servicio de usuarios está caído
// message (el texto por defecto) se usa solo si el servidor no mandó ningún mensaje.
class CursoServidorException(
    val codigo: Int,
    val mensajeServidor: String?
) : Exception("Error $codigo: verifica permisos o campos")
