package com.example.mastermartini.data.remote

import com.example.mastermartini.model.ErrorDto
import com.google.gson.Gson
import retrofit2.Response

// Extrae el mensaje del cuerpo ErrorDto ante respuestas HTTP de error en Retrofit
fun leerMensajeError(response: Response<*>): String? {
    return try {
        Gson().fromJson(response.errorBody()?.string(), ErrorDto::class.java)?.mensaje
    } catch (e: Exception) {
        null
    }
}
