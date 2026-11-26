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

// ENDPOINTS DEL MICROSERVICIO DE CURSOS (puerto 9092). CRUD completo:
//   Leer     -> GET    api/cursos  y  GET api/cursos/{id}
//   Crear    -> POST   api/cursos
//   Editar   -> PUT    api/cursos/{id}
//   Eliminar -> DELETE api/cursos/{id}
interface CursosApiService {

    @GET("api/cursos")
    suspend fun obtenerCapacitaciones(): List<Capacitacion>

    // UN curso por su id.
    // @GET("api/cursos/{id}"): petición GET; {id} es un hueco en la ruta.
    // @Path("id"): pone el valor del parámetro en ese hueco (api/cursos/4).
    // Si el curso no existe, el servidor responde 404 y Retrofit lanza HttpException.
    @GET("api/cursos/{id}")
    suspend fun obtenerCapacitacion(
        @Path("id") id: Int
    ): Capacitacion

    @POST("api/cursos")
    suspend fun crearCapacitacion(
        @Header("X-Usuario-Id") usuarioId: Int,
        @Body nuevaCapacitacion: Capacitacion
    ): Response<Capacitacion>

    // EDITA un curso. Solo el INSTRUCTOR dueño del curso o un ADMINISTRADOR (si no, 403).
    // @PUT: petición PUT = reemplazar los datos de algo que ya existe.
    // @Path("id"): el id del curso que se edita va en la ruta.
    // @Header("X-Usuario-Id"): quién pide el cambio; el servidor revisa su rol y si es el dueño.
    // @Body: el curso completo con los datos nuevos, convertido a JSON (mismo cuerpo que el POST).
    // Response<...>: así se puede revisar el código (200, 403, 404...) sin que Retrofit lance error.
    @PUT("api/cursos/{id}")
    suspend fun actualizarCapacitacion(
        @Path("id") id: Int,
        @Header("X-Usuario-Id") usuarioId: Int,
        @Body capacitacion: Capacitacion
    ): Response<Capacitacion>

    // ELIMINA un curso. Solo ADMINISTRADOR (si no, 403).
    // @DELETE: petición DELETE = borrar.
    // @Path("id") y @Header("X-Usuario-Id"): igual que en actualizarCapacitacion.
    // Response<Unit>: el servidor responde 204 "No Content" (sin cuerpo), no hay JSON que convertir.
    @DELETE("api/cursos/{id}")
    suspend fun eliminarCapacitacion(
        @Path("id") id: Int,
        @Header("X-Usuario-Id") usuarioId: Int
    ): Response<Unit>
}