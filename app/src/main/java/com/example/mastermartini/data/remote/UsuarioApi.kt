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

// Endpoints Retrofit del microservicio de usuarios (puerto 9091)
interface UsuarioApi {

    @POST("api/usuarios/registro")
    suspend fun registrar(@Body cuerpo: RegistroRequest): UsuarioDto

    @POST("api/usuarios/login")
    suspend fun login(@Body cuerpo: LoginRequest): UsuarioDto

    @POST("api/usuarios/recuperar")
    suspend fun recuperar(@Body cuerpo: RecuperarRequest): MensajeDto

    @POST("api/usuarios/restablecer")
    suspend fun restablecer(@Body cuerpo: RestablecerRequest): MensajeDto

    @GET("api/usuarios/{id}")
    suspend fun obtenerPorId(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") idSolicitante: Long
    ): UsuarioDto

    @PUT("api/usuarios/{id}")
    suspend fun actualizarPerfil(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") idSolicitante: Long,
        @Body cuerpo: ActualizarPerfilRequest
    ): UsuarioDto

    @GET("api/usuarios")
    suspend fun listar(
        @Header("X-Usuario-Id") idSolicitante: Long,
        @Query("rol") rol: String?
    ): List<UsuarioDto>

    @PUT("api/usuarios/{id}/plan-pro")
    suspend fun cambiarPlanPro(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") idSolicitante: Long,
        @Body cuerpo: PlanProRequest
    ): UsuarioDto

    @PUT("api/usuarios/{id}/rol")
    suspend fun cambiarRol(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") idSolicitante: Long,
        @Body cuerpo: CambiarRolRequest
    ): UsuarioDto

    @DELETE("api/usuarios/{id}")
    suspend fun eliminar(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") idSolicitante: Long
    ): Response<Unit>
}
