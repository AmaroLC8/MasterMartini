package com.example.mastermartini.data.remote.dto

import com.example.mastermartini.model.Rol
import com.example.mastermartini.model.Usuario

// DTOs DEL MICROSERVICIO DE USUARIOS
// DTO = "Data Transfer Object": un molde con la FORMA EXACTA del JSON que viaja por internet.
// Gson convierte el JSON en estos objetos (y al revés) usando los NOMBRES de las propiedades,
// por eso cada nombre debe ser IGUAL al del backend (ej. "personasNegocio", "nuevaPassword").
// Los nombres de las clases son los mismos del backend (LoginRequest, RegistroRequest...)
// para que sea fácil compararlos con el código del servidor.
//
// ¿Por qué no usar directo model/Usuario? Porque el JSON trae el rol como texto ("CLIENTE")
// y la app lo usa como Rol (sealed class). El DTO recibe el texto y aModelo() lo convierte.

// ---------- LO QUE RESPONDE EL SERVIDOR ----------

// Un usuario tal como lo manda el backend:
// {"id":1,"nombre":"...","rut":"...","correo":"...","telefono":"...","rubro":"...",
//  "personasNegocio":"...","rol":"CLIENTE","planPro":false}
// Todo es opcional ("?" y null por defecto): si el servidor no manda algún dato,
// la app no se cae. aModelo() pone un valor seguro en su lugar.
data class UsuarioDto(
    val id: Long? = null,
    val nombre: String? = null,
    val rut: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    val rubro: String? = null,
    val personasNegocio: String? = null,
    // "CLIENTE", "INSTRUCTOR", "VENTAS" o "ADMINISTRADOR"
    val rol: String? = null,
    val planPro: Boolean? = null
)

// Respuesta de /recuperar y /restablecer: {"mensaje": "...", "estado": 200}
data class MensajeDto(
    val mensaje: String? = null
)

// Convierte el usuario del servidor (DTO) en el Usuario que usa la app (model/Usuario).
// Es una "función de extensión": se escribe "UsuarioDto.aModelo()" y se llama como si fuera
// propia de la clase: usuarioDto.aModelo(). Dentro, "id", "nombre"... son los de ese DTO.
// "?:" (elvis) = "si viene null, usa este otro valor".
fun UsuarioDto.aModelo(): Usuario {
    return Usuario(
        id = id ?: 0L,
        nombre = nombre ?: "",
        rut = rut ?: "",
        correo = correo ?: "",
        telefono = telefono ?: "",
        rubro = rubro ?: "",
        personasNegocio = personasNegocio ?: "",
        // Rol.desdeCodigo pasa de "VENTAS" a Rol.Ventas (si el texto no se reconoce, queda Cliente)
        rol = Rol.desdeCodigo(rol ?: Rol.Cliente.codigo),
        planPro = planPro ?: false
    )
}

// ---------- LO QUE SE ENVÍA AL SERVIDOR (cuerpo de cada petición, @Body) ----------

// POST /api/usuarios/login. "usuario" puede ser el RUT o el correo.
data class LoginRequest(
    val usuario: String,
    val password: String
)

// POST /api/usuarios/registro
data class RegistroRequest(
    val nombre: String,
    val rut: String,
    val correo: String,
    // Solo números: el backend exige 9 dígitos que empiecen con 9 (ej. "912345678")
    val telefono: String,
    val rubro: String,
    val personasNegocio: String,
    val password: String
)

// POST /api/usuarios/recuperar
data class RecuperarRequest(
    val correo: String
)

// POST /api/usuarios/restablecer
data class RestablecerRequest(
    val correo: String,
    val codigo: String,
    val nuevaPassword: String
)

// PUT /api/usuarios/{id}. Solo estos 4 datos se pueden cambiar desde el perfil.
data class ActualizarPerfilRequest(
    val nombre: String,
    val telefono: String,
    val rubro: String,
    val personasNegocio: String
)

// PUT /api/usuarios/{id}/plan-pro
data class PlanProRequest(
    val activo: Boolean
)

// PUT /api/usuarios/{id}/rol. El rol viaja como texto ("VENTAS"), no como Rol.
data class CambiarRolRequest(
    val rol: String
)
