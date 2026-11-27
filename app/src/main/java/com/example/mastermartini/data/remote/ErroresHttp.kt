package com.example.mastermartini.data.remote

import com.example.mastermartini.model.ErrorDto
import com.google.gson.Gson
import retrofit2.Response

// Extrae el mensaje del cuerpo ErrorDto ante respuestas HTTP de error en Retrofit
// En un 400 de validación prefiere el primer error de "campos" (más claro que "Datos inválidos")
fun leerMensajeError(response: Response<*>): String? {
    return try {
        val error = Gson().fromJson(response.errorBody()?.string(), ErrorDto::class.java)
        error?.campos?.values?.firstOrNull() ?: error?.mensaje
    } catch (e: Exception) {
        null
    }
}
