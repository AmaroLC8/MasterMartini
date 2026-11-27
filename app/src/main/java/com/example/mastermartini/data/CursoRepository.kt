package com.example.mastermartini.data

import com.example.mastermartini.data.remote.RetrofitClient
import com.example.mastermartini.data.remote.leerMensajeError
import com.example.mastermartini.model.Curso
import kotlin.coroutines.cancellation.CancellationException

// Repositorio de recetas y fichas técnicas: todo viene del microservicio de cursos (api/recetas)
class CursoRepository {

    // Devuelve las recetas guardadas en memoria; si no hay (o forzarRecarga = true) las pide al servidor
    suspend fun obtenerRecetas(forzarRecarga: Boolean = false): Result<List<Curso>> {
        val guardadas = CatalogoCache.recetas
        if (!forzarRecarga && guardadas != null) {
            return Result.success(guardadas)
        }
        return try {
            val response = RetrofitClient.cursosApi.obtenerRecetas()
            val lista = response.body()
            if (response.isSuccessful && lista != null) {
                // Solo se guarda una lista que llegó bien
                CatalogoCache.recetas = lista
                Result.success(lista)
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

    // Busca una receta por id: primero en memoria y, si no está, en el servidor (404 = ya no existe)
    suspend fun obtenerRecetaPorId(id: Int, forzarRecarga: Boolean = false): Result<Curso> {
        val enMemoria = CatalogoCache.recetas?.find { receta -> receta.id == id }
        if (!forzarRecarga && enMemoria != null) {
            return Result.success(enMemoria)
        }
        return try {
            val response = RetrofitClient.cursosApi.obtenerReceta(id)
            val receta = response.body()
            if (response.isSuccessful && receta != null) {
                Result.success(receta)
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

// Categorías distintas de las recetas que llegaron, en el orden en que aparecen
fun categoriasDeRecetas(recetas: List<Curso>): List<String> {
    return recetas
        .map { receta -> receta.categoria }
        .filter { categoria -> categoria.isNotBlank() }
        .distinct()
}
