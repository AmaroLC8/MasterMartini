package com.example.mastermartini.data.remote

import com.example.mastermartini.model.Curso
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface CursosApiService {

    // Trae el catálogo desde el microservicio cursos (puerto 9092)
    @GET("api/cursos")
    suspend fun obtenerCursos(): List<Curso>

    // Crea un curso nuevo (requiere header X-Usuario-Id con rol INSTRUCTOR o ADMINISTRADOR)
    @POST("api/cursos")
    suspend fun crearCurso(
        @Header("X-Usuario-Id") usuarioId: Int,
        @Body nuevoCurso: Curso
    ): Response<Curso>
}