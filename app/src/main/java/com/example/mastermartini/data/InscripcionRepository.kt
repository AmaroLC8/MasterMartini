package com.example.mastermartini.data

import com.example.mastermartini.data.remote.RetrofitClient
import com.example.mastermartini.data.remote.dto.InscripcionRequest
import com.example.mastermartini.data.remote.dto.ProgresoLeccionRequest
import com.example.mastermartini.data.remote.leerMensajeError
import com.example.mastermartini.model.Inscripcion

// REPOSITORIO DE INSCRIPCIONES (habla con el microservicio de interacciones, puerto 9093)
// Los ViewModels (detalle del curso y "Mis cursos") le piden las cosas aquí y no hablan
// directo con Retrofit (MVVM). Si mañana cambia el servidor, solo cambia esta clase.
//
// Cada función devuelve Result<...>, igual que CapacitacionRepository:
//   Result.success(dato)                 -> salió bien
//   Result.failure(ServidorException)    -> el servidor respondió con error (código + mensaje)
//   Result.failure(Exception(sin conexión)) -> no se pudo hablar con el servidor
// Así el ViewModel usa fold(onSuccess, onFailure) y la app nunca se cae.
class InscripcionRepository {

    // Las funciones de Retrofit (InteraccionesApi), creadas una sola vez en RetrofitClient
    private val api = RetrofitClient.interaccionesApi

    // Mensaje cuando no se puede hablar con el servidor (apagado, sin internet, IP mal...)
    private val mensajeSinConexion = "No se pudo conectar con el servidor de inscripciones (9093)"

    // MANEJO DE EXCEPCIONES (igual en las 4 funciones):
    //   try   -> se hace la petición. Si el servidor responde, se revisa el código:
    //            isSuccessful (2xx) = salió bien; si no, ServidorException con el código y el
    //            "mensaje" del ErrorDto (leerMensajeError, en data/remote/ErroresHttp.kt).
    //   catch -> si la petición ni siquiera llegó (sin internet, servidor apagado), Retrofit
    //            LANZA una excepción. Se atrapa y se devuelve como Result.failure, así el
    //            error no se escapa al ViewModel y la app no se cae.

    // Inscribe al usuario en un curso (POST api/inscripciones).
    // El servidor revisa el Plan Pro: 403 si el curso no es gratis y no lo tiene; 409 si ya estaba inscrito.
    suspend fun inscribirse(usuarioId: Long, cursoId: Int): Result<Inscripcion> {
        return try {
            val response = api.inscribirse(usuarioId, InscripcionRequest(cursoId = cursoId))
            val inscripcion = response.body()
            if (response.isSuccessful && inscripcion != null) {
                Result.success(inscripcion)
            } else {
                Result.failure(ServidorException(response.code(), leerMensajeError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(mensajeSinConexion))
        }
    }

    // Todas las inscripciones del usuario (GET api/inscripciones/usuario/{usuarioId}).
    // Se manda el mismo id en la ruta y en el header: el usuario pide SUS inscripciones.
    suspend fun misInscripciones(usuarioId: Long): Result<List<Inscripcion>> {
        return try {
            val response = api.misInscripciones(usuarioId = usuarioId, idSolicitante = usuarioId)
            val lista = response.body()
            if (response.isSuccessful && lista != null) {
                Result.success(lista)
            } else {
                Result.failure(ServidorException(response.code(), leerMensajeError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(mensajeSinConexion))
        }
    }

    // Marca (completada = true) o desmarca (false) una lección (PUT api/inscripciones/{id}/lecciones).
    // modulo y leccion son posiciones desde 0. Devuelve la inscripción con la lista actualizada.
    suspend fun marcarLeccion(
        usuarioId: Long,
        inscripcionId: Long,
        modulo: Int,
        leccion: Int,
        completada: Boolean
    ): Result<Inscripcion> {
        return try {
            val cuerpo = ProgresoLeccionRequest(modulo = modulo, leccion = leccion, completada = completada)
            val response = api.marcarLeccion(inscripcionId, usuarioId, cuerpo)
            val inscripcion = response.body()
            if (response.isSuccessful && inscripcion != null) {
                Result.success(inscripcion)
            } else {
                Result.failure(ServidorException(response.code(), leerMensajeError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(mensajeSinConexion))
        }
    }

    // Cancela la inscripción (DELETE api/inscripciones/{id}). El servidor responde 204 sin cuerpo:
    // si salió bien no hay nada que devolver, por eso Result<Unit> (Unit = "nada").
    suspend fun cancelar(usuarioId: Long, inscripcionId: Long): Result<Unit> {
        return try {
            val response = api.cancelar(inscripcionId, usuarioId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(ServidorException(response.code(), leerMensajeError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(mensajeSinConexion))
        }
    }
}
