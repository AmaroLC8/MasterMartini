package com.example.mastermartini.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

// Ayudas compartidas para simular respuestas de error de los microservicios

fun cuerpoError(mensaje: String, estado: String = "ERROR"): String {
    return """{"mensaje":"$mensaje","estado":"$estado"}"""
}

fun <T> respuestaError(codigo: Int, json: String = ""): Response<T> {
    return Response.error(codigo, json.toResponseBody("application/json".toMediaType()))
}

fun errorHttp(codigo: Int, json: String = ""): HttpException {
    return HttpException(respuestaError<Any>(codigo, json))
}
