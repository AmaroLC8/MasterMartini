package com.example.mastermartini.data.remote

import com.example.mastermartini.data.remote.dto.InscripcionRequest
import com.example.mastermartini.data.remote.dto.PreguntaRequest
import com.example.mastermartini.data.remote.dto.ProgresoLeccionRequest
import com.example.mastermartini.data.remote.dto.RespuestaRequest
import com.example.mastermartini.model.Inscripcion
import com.example.mastermartini.model.Pregunta
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Endpoints Retrofit del microservicio de interacciones: inscripciones, progreso y preguntas (puerto 9093)
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

    // ---------- Preguntas al instructor ----------

    // Público: no necesita el header X-Usuario-Id
    @GET("api/cursos/{cursoId}/preguntas")
    suspend fun preguntasDelCurso(
        @Path("cursoId") cursoId: Int
    ): Response<List<Pregunta>>

    @POST("api/cursos/{cursoId}/preguntas")
    suspend fun preguntar(
        @Path("cursoId") cursoId: Int,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Body cuerpo: PreguntaRequest
    ): Response<Pregunta>

    @GET("api/preguntas/pendientes")
    suspend fun preguntasPendientes(
        @Header("X-Usuario-Id") usuarioId: Long
    ): Response<List<Pregunta>>

    @PUT("api/preguntas/{id}/respuesta")
    suspend fun responderPregunta(
        @Path("id") preguntaId: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Body cuerpo: RespuestaRequest
    ): Response<Pregunta>

    @DELETE("api/preguntas/{id}")
    suspend fun eliminarPregunta(
        @Path("id") preguntaId: Long,
        @Header("X-Usuario-Id") usuarioId: Long
    ): Response<Unit>
}
