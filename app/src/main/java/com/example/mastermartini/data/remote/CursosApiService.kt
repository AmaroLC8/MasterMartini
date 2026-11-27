package com.example.mastermartini.data.remote

import com.example.mastermartini.model.Capacitacion
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Endpoints Retrofit del microservicio de cursos online (puerto 9092)
interface CursosApiService {

    @GET("api/cursos")
    suspend fun obtenerCapacitaciones(): List<Capacitacion>

    @GET("api/cursos/{id}")
    suspend fun obtenerCapacitacion(
        @Path("id") id: Int
    ): Capacitacion

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
}