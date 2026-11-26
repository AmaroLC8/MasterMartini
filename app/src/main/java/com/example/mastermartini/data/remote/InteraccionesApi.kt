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

// ENDPOINTS DEL MICROSERVICIO DE INTERACCIONES (puerto 9093): INSCRIPCIONES A CURSOS
// CRUD de inscripciones:
//   Crear    -> POST   api/inscripciones                     (inscribirse)
//   Leer     -> GET    api/inscripciones/usuario/{usuarioId} ("Mis cursos")
//   Editar   -> PUT    api/inscripciones/{id}/lecciones      (marcar una lección como vista)
//   Eliminar -> DELETE api/inscripciones/{id}                (cancelar la inscripción)
//
// TODAS llevan @Header("X-Usuario-Id"): el id de QUIÉN pide la acción. El servidor revisa
// con él que solo el dueño vea o cambie su inscripción, y si tiene Plan Pro al inscribirse.
//
// Todas devuelven Response<...>: así Retrofit NO lanza error con los códigos 4xx/5xx y el
// repositorio puede leer el código (403, 409...) y el mensaje del servidor.
interface InteraccionesApi {

    // INSCRIBIRSE en un curso. Responde 201 con la inscripción nueva.
    // @POST: petición POST = crear algo nuevo. La ruta se suma a BASE_URL_INTERACCIONES.
    // @Header("X-Usuario-Id"): quién se inscribe.
    // @Body: {"cursoId": 4} convertido a JSON.
    @POST("api/inscripciones")
    suspend fun inscribirse(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Body cuerpo: InscripcionRequest
    ): Response<Inscripcion>

    // MIS INSCRIPCIONES: la lista de cursos en los que está inscrito un usuario.
    // @GET: petición GET = leer. {usuarioId} es un hueco en la ruta.
    // @Path("usuarioId"): pone el id en ese hueco (api/inscripciones/usuario/1).
    // @Header("X-Usuario-Id"): quién pregunta; el servidor solo deja ver las PROPIAS (403 si no).
    @GET("api/inscripciones/usuario/{usuarioId}")
    suspend fun misInscripciones(
        @Path("usuarioId") usuarioId: Long,
        @Header("X-Usuario-Id") idSolicitante: Long
    ): Response<List<Inscripcion>>

    // MARCAR O DESMARCAR UNA LECCIÓN. Responde la inscripción actualizada (con la lista nueva).
    // @PUT: petición PUT = modificar algo que ya existe.
    // @Path("id"): el id de la INSCRIPCIÓN (no del curso).
    // @Header("X-Usuario-Id"): solo el dueño de la inscripción puede marcar (403 si no).
    // @Body: {"modulo": 0, "leccion": 2, "completada": true}
    @PUT("api/inscripciones/{id}/lecciones")
    suspend fun marcarLeccion(
        @Path("id") inscripcionId: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Body cuerpo: ProgresoLeccionRequest
    ): Response<Inscripcion>

    // CANCELAR LA INSCRIPCIÓN (se borra junto con su progreso).
    // @DELETE: petición DELETE = borrar.
    // @Path("id") y @Header("X-Usuario-Id"): igual que en marcarLeccion.
    // Response<Unit>: el servidor responde 204 "No Content" (sin cuerpo), no hay JSON que convertir.
    @DELETE("api/inscripciones/{id}")
    suspend fun cancelar(
        @Path("id") inscripcionId: Long,
        @Header("X-Usuario-Id") usuarioId: Long
    ): Response<Unit>
}
