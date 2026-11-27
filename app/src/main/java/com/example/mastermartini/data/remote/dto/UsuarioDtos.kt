package com.example.mastermartini.data.remote.dto

import com.example.mastermartini.model.Rol
import com.example.mastermartini.model.Usuario

// DTOs de respuesta y petición para el microservicio de usuarios
data class UsuarioDto(
    val id: Long? = null,
    val nombre: String? = null,
    val rut: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    val rubro: String? = null,
    val personasNegocio: String? = null,
    val rol: String? = null,
    val planPro: Boolean? = null
)

data class MensajeDto(
    val mensaje: String? = null
)

// Mapea el DTO recibido desde el servidor al modelo de dominio Usuario
fun UsuarioDto.aModelo(): Usuario {
    return Usuario(
        id = id ?: 0L,
        nombre = nombre ?: "",
        rut = rut ?: "",
        correo = correo ?: "",
        telefono = telefono ?: "",
        rubro = rubro ?: "",
        personasNegocio = personasNegocio ?: "",
        rol = Rol.desdeCodigo(rol ?: Rol.Cliente.codigo),
        planPro = planPro ?: false
    )
}

data class LoginRequest(
    val usuario: String,
    val password: String
)

data class RegistroRequest(
    val nombre: String,
    val rut: String,
    val correo: String,
    val telefono: String,
    val rubro: String,
    val personasNegocio: String,
    val password: String
)

data class RecuperarRequest(
    val correo: String
)

data class RestablecerRequest(
    val correo: String,
    val codigo: String,
    val nuevaPassword: String
)

data class ActualizarPerfilRequest(
    val nombre: String,
    val telefono: String,
    val rubro: String,
    val personasNegocio: String
)

data class PlanProRequest(
    val activo: Boolean
)

data class CambiarRolRequest(
    val rol: String
)
