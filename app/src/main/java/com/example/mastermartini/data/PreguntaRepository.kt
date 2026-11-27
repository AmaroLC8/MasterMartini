package com.example.mastermartini.data

import com.example.mastermartini.data.remote.RetrofitClient
import com.example.mastermartini.data.remote.dto.PreguntaRequest
import com.example.mastermartini.data.remote.dto.RespuestaRequest
import com.example.mastermartini.data.remote.leerMensajeError
import com.example.mastermartini.model.Pregunta

// Repositorio de preguntas al instructor conectado al microservicio de interacciones
class PreguntaRepository {

    private val api = RetrofitClient.interaccionesApi
    private val mensajeSinConexion = "No se pudo conectar con el servidor de inscripciones (9093)"

    // Trae las preguntas de un curso, de la más nueva a la más antigua
    suspend fun listarDelCurso(cursoId: Int): Result<List<Pregunta>> {
        return try {
            val response = api.preguntasDelCurso(cursoId)
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

    // Envía una pregunta nueva; el servidor exige que el usuario esté inscrito
    suspend fun preguntar(usuarioId: Long, cursoId: Int, texto: String): Result<Pregunta> {
        return try {
            val response = api.preguntar(cursoId, usuarioId, PreguntaRequest(texto = texto))
            val pregunta = response.body()
            if (response.isSuccessful && pregunta != null) {
                Result.success(pregunta)
            } else {
                Result.failure(ServidorException(response.code(), leerMensajeError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(mensajeSinConexion))
        }
    }

    // Trae las preguntas sin responder que el instructor o el admin pueden contestar
    suspend fun pendientes(usuarioId: Long): Result<List<Pregunta>> {
        return try {
            val response = api.preguntasPendientes(usuarioId)
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

    // Guarda la respuesta del instructor o del admin a una pregunta
    suspend fun responder(usuarioId: Long, preguntaId: Long, respuesta: String): Result<Pregunta> {
        return try {
            val response = api.responderPregunta(preguntaId, usuarioId, RespuestaRequest(respuesta = respuesta))
            val pregunta = response.body()
            if (response.isSuccessful && pregunta != null) {
                Result.success(pregunta)
            } else {
                Result.failure(ServidorException(response.code(), leerMensajeError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(mensajeSinConexion))
        }
    }

    // Borra una pregunta; solo puede hacerlo su autor o el admin
    suspend fun eliminar(usuarioId: Long, preguntaId: Long): Result<Unit> {
        return try {
            val response = api.eliminarPregunta(preguntaId, usuarioId)
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
