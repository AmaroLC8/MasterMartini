package com.example.mastermartini.data

import com.example.mastermartini.model.Rol
import com.example.mastermartini.model.Usuario

// Contrato de operaciones de autenticación, perfil y gestión de usuarios
interface UsuarioRepository {

    suspend fun login(usuario: String, password: String): Usuario

    suspend fun registrar(
        nombre: String,
        rut: String,
        correo: String,
        telefono: String,
        rubro: String,
        personasNegocio: String,
        password: String
    ): Usuario

    suspend fun solicitarCodigo(correo: String)

    suspend fun restablecerPassword(correo: String, codigo: String, nuevaPassword: String)

    suspend fun obtenerPerfil(id: Long): Usuario

    suspend fun actualizarPerfil(
        id: Long,
        nombre: String,
        telefono: String,
        rubro: String,
        personasNegocio: String
    ): Usuario

    // Lista usuarios opcionalmente filtrados por rol (requiere Ventas o Administrador)
    suspend fun listarUsuarios(idSolicitante: Long, rol: Rol?): List<Usuario>

    // Activa o desactiva el Plan Pro de un usuario (requiere Ventas o Administrador)
    suspend fun cambiarPlanPro(idSolicitante: Long, idUsuario: Long, activo: Boolean): Usuario

    // Cambia el rol de un usuario (requiere Administrador)
    suspend fun cambiarRol(idSolicitante: Long, idUsuario: Long, rol: Rol): Usuario

    // Elimina una cuenta de usuario (requiere Administrador)
    suspend fun eliminarUsuario(idSolicitante: Long, idUsuario: Long)
}
