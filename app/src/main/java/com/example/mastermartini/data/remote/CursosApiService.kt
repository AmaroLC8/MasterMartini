package com.example.mastermartini.data.remote

import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.model.Curso
import com.example.mastermartini.model.Producto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Endpoints Retrofit del microservicio de cursos online, recetas y productos (puerto 9092)
interface CursosApiService {

    @GET("api/cursos")
    suspend fun obtenerCapacitaciones(): Response<List<Capacitacion>>

    @GET("api/cursos/{id}")
    suspend fun obtenerCapacitacion(
        @Path("id") id: Int
    ): Response<Capacitacion>

    @POST("api/cursos")
    suspend fun crearCapacitacion(
        @Header("X-Usuario-Id") usuarioId: Int,
        @Body nuevaCapacitacion: Capacitacion
    ): Response<Capacitacion>

    @PUT("api/cursos/{id}")
    suspend fun actualizarCapacitacion(
        @Path("id") id: Int,
        @Header("X-Usuario-Id") usuarioId: Int,
        @Body capacitacion: Capacitacion
    ): Response<Capacitacion>

    @DELETE("api/cursos/{id}")
    suspend fun eliminarCapacitacion(
        @Path("id") id: Int,
        @Header("X-Usuario-Id") usuarioId: Int
    ): Response<Unit>

    // Recetas (modelo Curso): mismos campos que el JSON del backend
    @GET("api/recetas")
    suspend fun obtenerRecetas(): Response<List<Curso>>

    @GET("api/recetas/{id}")
    suspend fun obtenerReceta(
        @Path("id") id: Int
    ): Response<Curso>

    // Productos del catálogo comercial
    @GET("api/productos")
    suspend fun obtenerProductos(): Response<List<Producto>>

    @GET("api/productos/{id}")
    suspend fun obtenerProducto(
        @Path("id") id: Int
    ): Response<Producto>
}
