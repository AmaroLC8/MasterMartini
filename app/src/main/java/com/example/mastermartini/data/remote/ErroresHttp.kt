package com.example.mastermartini.data.remote

import com.example.mastermartini.model.ErrorDto
import com.google.gson.Gson
import retrofit2.Response

// AYUDANTES PARA LOS ERRORES HTTP DE LOS MICROSERVICIOS
// Es una función "suelta" (top-level): no está dentro de ninguna clase, así cualquier
// repositorio la usa directo: leerMensajeError(response). Antes era privada dentro de
// CapacitacionRepository; se movió aquí para que InscripcionRepository no la repita.

// Lee el "mensaje" del JSON de error que manda el servidor ({"mensaje","estado"}).
// Response<*>: el "*" significa "un Response de cualquier tipo" (Capacitacion, Inscripcion, Unit...).
// errorBody() es el cuerpo de la respuesta cuando el código es de error (4xx o 5xx).
// Tiene su propio try/catch: si el cuerpo viene vacío o no es un JSON válido, devuelve null
// (el ViewModel usará otro texto), pero la app nunca se cae.
fun leerMensajeError(response: Response<*>): String? {
    return try {
        Gson().fromJson(response.errorBody()?.string(), ErrorDto::class.java)?.mensaje
    } catch (e: Exception) {
        null
    }
}
