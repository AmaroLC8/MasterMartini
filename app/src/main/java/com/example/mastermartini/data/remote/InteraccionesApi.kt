package com.example.mastermartini.data.remote

import com.example.mastermartini.data.remote.dto.InscripcionRequest
import com.example.mastermartini.data.remote.dto.ProgresoLeccionRequest
import com.example.mastermartini.model.Inscripcion
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Endpoints Retrofit del microservicio de interacciones para inscripciones y progreso (puerto 9093)
interface InteraccionesApi {

    @POST("api/inscripciones")
    suspend fun inscribirse(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Body cuerpo: InscripcionRequest
    ): Response<Inscripcion>

    @GET("api/inscripciones/usuario/{usuarioId}")
    suspend fun misInscripciones(
        @Path("usuarioId") usuarioId: Long,
        @Header("X-Usuario-Id") idSolicitante: Long
    ): Response<List<Inscripcion>>

    @PUT("api/inscripciones/{id}/lecciones")
    suspend fun marcarLeccion(
        @Path("id") inscripcionId: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Body cuerpo: ProgresoLeccionRequest
    ): Response<Inscripcion>

    @DELETE("api/inscripciones/{id}")
    suspend fun cancelar(
        @Path("id") inscripcionId: Long,
        @Header("X-Usuario-Id") usuarioId: Long
    ): Response<Unit>
}
