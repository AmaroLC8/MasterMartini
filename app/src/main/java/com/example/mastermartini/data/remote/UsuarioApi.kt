package com.example.mastermartini.data.remote

import com.example.mastermartini.data.remote.dto.ActualizarPerfilRequest
import com.example.mastermartini.data.remote.dto.CambiarRolRequest
import com.example.mastermartini.data.remote.dto.LoginRequest
import com.example.mastermartini.data.remote.dto.MensajeDto
import com.example.mastermartini.data.remote.dto.PlanProRequest
import com.example.mastermartini.data.remote.dto.RecuperarRequest
import com.example.mastermartini.data.remote.dto.RegistroRequest
import com.example.mastermartini.data.remote.dto.RestablecerRequest
import com.example.mastermartini.data.remote.dto.UsuarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// ENDPOINTS DEL MICROSERVICIO DE USUARIOS (Spring Boot, puerto 9091)
// Es una INTERFAZ: aquí solo se DESCRIBE cada petición (método, ruta, datos).
// Retrofit escribe el código que de verdad se conecta (RetrofitClient.usuariosApi).
//
// Las anotaciones dicen cómo se arma cada petición:
//   @GET / @POST / @PUT / @DELETE -> el método HTTP y la ruta (se suma a la BASE_URL)
//   @Path("id")   -> reemplaza {id} en la ruta:  api/usuarios/{id} -> api/usuarios/4
//   @Body         -> el objeto se convierte a JSON y va en el cuerpo de la petición
//   @Query("rol") -> se agrega al final de la ruta: api/usuarios?rol=CLIENTE (si es null, no se agrega)
//   @Header("X-Usuario-Id") -> id de QUIEN PIDE la acción; el backend revisa su rol (403 si no puede)
//
// Son "suspend": esperan la respuesta sin congelar la pantalla.
// Si el servidor responde con error (4xx o 5xx), Retrofit LANZA HttpException;
// si no hay conexión, lanza IOException. UsuarioRepositoryRemoto las atrapa.
interface UsuarioApi {

    // Crea una cuenta nueva (siempre como Cliente y sin Plan Pro). Responde 201 con el usuario.
    @POST("api/usuarios/registro")
    suspend fun registrar(@Body cuerpo: RegistroRequest): UsuarioDto

    // Inicia sesión con RUT o correo. 401 si no coinciden.
    @POST("api/usuarios/login")
    suspend fun login(@Body cuerpo: LoginRequest): UsuarioDto

    // Pide el código de recuperación de contraseña (el backend lo envía al correo)
    @POST("api/usuarios/recuperar")
    suspend fun recuperar(@Body cuerpo: RecuperarRequest): MensajeDto

    // Cambia la contraseña con el código. 400 si el código está mal o venció.
    @POST("api/usuarios/restablecer")
    suspend fun restablecer(@Body cuerpo: RestablecerRequest): MensajeDto

    // Perfil de un usuario
    @GET("api/usuarios/{id}")
    suspend fun obtenerPorId(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") idSolicitante: Long
    ): UsuarioDto

    // Actualiza nombre, teléfono, rubro y personas del negocio
    @PUT("api/usuarios/{id}")
    suspend fun actualizarPerfil(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") idSolicitante: Long,
        @Body cuerpo: ActualizarPerfilRequest
    ): UsuarioDto

    // Lista de usuarios, filtrada por rol si se indica (solo Ventas o Administrador)
    @GET("api/usuarios")
    suspend fun listar(
        @Header("X-Usuario-Id") idSolicitante: Long,
        @Query("rol") rol: String?
    ): List<UsuarioDto>

    // Activa o desactiva el Plan Pro (solo Ventas o Administrador)
    @PUT("api/usuarios/{id}/plan-pro")
    suspend fun cambiarPlanPro(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") idSolicitante: Long,
        @Body cuerpo: PlanProRequest
    ): UsuarioDto

    // Cambia el rol (solo Administrador)
    @PUT("api/usuarios/{id}/rol")
    suspend fun cambiarRol(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") idSolicitante: Long,
        @Body cuerpo: CambiarRolRequest
    ): UsuarioDto

    // Elimina una cuenta (solo Administrador). El backend responde 204 "No Content" (sin cuerpo),
    // por eso devuelve Response<Unit>: no hay JSON que convertir. Ojo: con Response, Retrofit
    // NO lanza HttpException solo; el repositorio revisa isSuccessful.
    @DELETE("api/usuarios/{id}")
    suspend fun eliminar(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") idSolicitante: Long
    ): Response<Unit>
}
