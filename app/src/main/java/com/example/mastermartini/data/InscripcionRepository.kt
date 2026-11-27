package com.example.mastermartini.data

import com.example.mastermartini.data.remote.RetrofitClient
import com.example.mastermartini.data.remote.dto.InscripcionRequest
import com.example.mastermartini.data.remote.dto.ProgresoLeccionRequest
import com.example.mastermartini.data.remote.leerMensajeError
import com.example.mastermartini.model.Inscripcion
import kotlin.coroutines.cancellation.CancellationException

// Repositorio de inscripciones y avance de lecciones conectado al microservicio de interacciones
class InscripcionRepository {

    private val api = RetrofitClient.interaccionesApi

    // Registra la inscripción del usuario en un curso mediante Retrofit
    suspend fun inscribirse(usuarioId: Long, cursoId: Int): Result<Inscripcion> {
        return try {
            val response = api.inscribirse(usuarioId, InscripcionRequest(cursoId = cursoId))
            val inscripcion = response.body()
            if (response.isSuccessful && inscripcion != null) {
                Result.success(inscripcion)
            } else {
                Result.failure(ServidorException(response.code(), leerMensajeError(response)))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(MENSAJE_SIN_CONEXION))
        }
    }

    // Consulta todas las inscripciones activas del usuario
    suspend fun misInscripciones(usuarioId: Long): Result<List<Inscripcion>> {
        return try {
            val response = api.misInscripciones(usuarioId = usuarioId, idSolicitante = usuarioId)
            val lista = response.body()
            if (response.isSuccessful && lista != null) {
                Result.success(lista)
            } else {
                Result.failure(ServidorException(response.code(), leerMensajeError(response)))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(MENSAJE_SIN_CONEXION))
        }
    }

    // Actualiza el estado de completado de una lección en el servidor
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(MENSAJE_SIN_CONEXION))
        }
    }

    // Cancela una inscripción existente en el microservicio
    suspend fun cancelar(usuarioId: Long, inscripcionId: Long): Result<Unit> {
        return try {
            val response = api.cancelar(inscripcionId, usuarioId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(ServidorException(response.code(), leerMensajeError(response)))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(MENSAJE_SIN_CONEXION))
        }
    }
}
