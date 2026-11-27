package com.example.mastermartini.data

import com.example.mastermartini.data.remote.RetrofitClient
import com.example.mastermartini.data.remote.leerMensajeError
import com.example.mastermartini.model.Capacitacion
import kotlin.coroutines.cancellation.CancellationException

// Repositorio de cursos online: todo viene del microservicio de cursos, sin datos locales
class CapacitacionRepository {

    // Trae el catálogo del servidor; un servidor sin cursos devuelve una lista vacía
    suspend fun obtenerCapacitacionesRemotas(): Result<List<Capacitacion>> {
        return try {
            val response = RetrofitClient.cursosApi.obtenerCapacitaciones()
            val lista = response.body()
            if (response.isSuccessful && lista != null) {
                Result.success(lista.map { completarImagen(it) })
            } else {
                Result.failure(ServidorException(response.code(), leerMensajeError(response)))
            }
        } catch (e: CancellationException) {
            // Si el usuario salió de la pantalla, la corrutina se cancela: no es un error de conexión
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(MENSAJE_SIN_CONEXION))
        }
    }

    // Trae un curso por id; si no existe llega ServidorException con código 404
    suspend fun obtenerCapacitacionPorIdRemoto(id: Int): Result<Capacitacion> {
        return try {
            val response = RetrofitClient.cursosApi.obtenerCapacitacion(id)
            val curso = response.body()
            if (response.isSuccessful && curso != null) {
                Result.success(completarImagen(curso))
            } else {
                Result.failure(ServidorException(response.code(), leerMensajeError(response)))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(MENSAJE_SIN_CONEXION))
        }
    }

    // Si el curso no trae imagen, usa una imagen de la empresa según su categoría
    private fun completarImagen(capacitacion: Capacitacion): Capacitacion {
        if (!capacitacion.imagenUrl.isNullOrBlank()) return capacitacion
        val respaldo = when (capacitacion.categoria) {
            "Chocolatería", "Heladería" -> DatosEmpresa.URL_IMG_CHOCOLATERIA
            "Panadería" -> DatosEmpresa.URL_IMG_PANADERIA
            else -> DatosEmpresa.URL_IMG_PASTELERIA
        }
        return capacitacion.copy(imagenUrl = respaldo)
    }

    // Crea un nuevo curso en el microservicio enviando X-Usuario-Id
    suspend fun crearCapacitacion(
        usuarioId: Int,
        nuevaCapacitacion: Capacitacion
    ): Result<Capacitacion> {
        return try {
            val response = RetrofitClient.cursosApi.crearCapacitacion(usuarioId, nuevaCapacitacion)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(ServidorException(response.code(), leerMensajeError(response)))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(MENSAJE_SIN_CONEXION))
        }
    }

    // Actualiza un curso existente en el microservicio enviando X-Usuario-Id
    suspend fun actualizarCapacitacion(
        usuarioId: Int,
        id: Int,
        capacitacion: Capacitacion
    ): Result<Capacitacion> {
        return try {
            val response = RetrofitClient.cursosApi.actualizarCapacitacion(id, usuarioId, capacitacion)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(ServidorException(response.code(), leerMensajeError(response)))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(MENSAJE_SIN_CONEXION))
        }
    }

    // Elimina un curso en el microservicio enviando X-Usuario-Id
    suspend fun eliminarCapacitacion(usuarioId: Int, id: Int): Result<Unit> {
        return try {
            val response = RetrofitClient.cursosApi.eliminarCapacitacion(id, usuarioId)
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
