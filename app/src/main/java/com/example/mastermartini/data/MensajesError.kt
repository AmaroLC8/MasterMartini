package com.example.mastermartini.data

// Mensaje único que ve el usuario cuando la app no logra conectarse con un microservicio
const val MENSAJE_SIN_CONEXION = "No pudimos conectarnos. Revisa tu internet e inténtalo otra vez."

// Texto que ve el usuario para un error al leer del servidor: el mensaje del backend o el de sin conexión
fun textoDeError(error: Throwable): String {
    if (error is ServidorException) {
        return error.mensajeServidor ?: error.message ?: MENSAJE_SIN_CONEXION
    }
    return error.message ?: MENSAJE_SIN_CONEXION
}

// El servidor responde 404 cuando la receta o el producto no existe (o fue eliminado)
fun esNoEncontrado(error: Throwable): Boolean {
    return error is ServidorException && error.codigo == 404
}
