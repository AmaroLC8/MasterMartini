package com.example.mastermartini.data.remote

import com.example.mastermartini.model.CursoDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface CursosApiService {

    @GET("api/cursos")
    suspend fun obtenerCursos(): List<CursoDto>

    @POST("api/cursos")
    suspend fun crearCurso(
        @Header("X-Usuario-Id") usuarioId: Int,
        @Body nuevoCurso: CursoDto
    ): Response<CursoDto>
}