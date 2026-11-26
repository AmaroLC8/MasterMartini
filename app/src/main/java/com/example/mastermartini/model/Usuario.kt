package com.example.mastermartini.model

// Modelo de usuario autenticado con su perfil, rol y estado de Plan Pro
data class Usuario(
    val id: Long,
    val nombre: String,
    val rut: String,
    val correo: String,
    val telefono: String,
    val rubro: String,
    val personasNegocio: String,
    val rol: Rol,
    val planPro: Boolean
)
