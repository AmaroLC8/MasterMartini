package com.example.mastermartini.data

import com.example.mastermartini.data.remote.RetrofitClient
import com.example.mastermartini.data.remote.leerMensajeError
import com.example.mastermartini.model.Curso
import com.example.mastermartini.model.Producto
import kotlin.coroutines.cancellation.CancellationException

// Repositorio del catálogo de productos Master Martini: todo viene del microservicio de cursos (api/productos)
class ProductoRepository {

    // Devuelve los productos guardados en memoria; si no hay (o forzarRecarga = true) los pide al servidor
    suspend fun obtenerProductos(forzarRecarga: Boolean = false): Result<List<Producto>> {
        val guardados = CatalogoCache.productos
        if (!forzarRecarga && guardados != null) {
            return Result.success(guardados)
        }
        return try {
            val response = RetrofitClient.cursosApi.obtenerProductos()
            val lista = response.body()
            if (response.isSuccessful && lista != null) {
                CatalogoCache.productos = lista
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

    // Busca un producto por id: primero en memoria y, si no está, en el servidor (404 = ya no existe)
    suspend fun obtenerProductoPorId(id: Int, forzarRecarga: Boolean = false): Result<Producto> {
        val enMemoria = CatalogoCache.productos?.find { producto -> producto.id == id }
        if (!forzarRecarga && enMemoria != null) {
            return Result.success(enMemoria)
        }
        return try {
            val response = RetrofitClient.cursosApi.obtenerProducto(id)
            val producto = response.body()
            if (response.isSuccessful && producto != null) {
                Result.success(producto)
            } else {
                Result.failure(ServidorException(response.code(), leerMensajeError(response)))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(MENSAJE_SIN_CONEXION))
        }
    }

    // Busca el producto con ese nombre exacto; success(null) si ninguno se llama así
    suspend fun obtenerProductoPorNombre(nombre: String): Result<Producto?> {
        return obtenerProductos().map { lista ->
            lista.find { producto -> producto.nombre == nombre }
        }
    }
}

// Líneas comerciales distintas de los productos que llegaron, en el orden en que aparecen
fun lineasDeProductos(productos: List<Producto>): List<String> {
    return productos
        .map { producto -> producto.linea }
        .filter { linea -> linea.isNotBlank() }
        .distinct()
}

// Recetas que usan el producto como insumo principal o como ingrediente
fun recetasQueUsan(producto: Producto, recetas: List<Curso>): List<Curso> {
    return recetas.filter { receta ->
        receta.insumo.contains(producto.nombre) ||
            receta.ingredientes.any { ingrediente -> ingrediente.contains(producto.nombre) }
    }
}
