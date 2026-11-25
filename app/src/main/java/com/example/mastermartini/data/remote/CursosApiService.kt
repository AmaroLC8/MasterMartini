package com.example.mastermartini.data.remote

import com.example.mastermartini.model.Capacitacion
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface CursosApiService {

    @GET("api/cursos")
    suspend fun obtenerCapacitaciones(): List<Capacitacion>

    @POST("api/cursos")
    suspend fun crearCapacitacion(
        @Header("X-Usuario-Id") usuarioId: Int,
        @Body nuevaCapacitacion: Capacitacion
    ): Response<Capacitacion>
}