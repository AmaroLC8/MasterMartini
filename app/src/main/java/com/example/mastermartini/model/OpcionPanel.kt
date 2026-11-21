package com.example.mastermartini.model

// UNA OPCIÓN DEL "PANEL DE TRABAJO" (se muestra como un botón grande en el Perfil)
// Cada Rol devuelve las suyas con opcionesPanel() (ver model/Rol.kt).
data class OpcionPanel(
    // Título del botón, ej. "Clientes y Plan Pro"
    val titulo: String,
    // Una frase que explica qué se hace ahí
    val descripcion: String,
    // Ruta registrada en el NavHost de MainActivity, ej. "panel/clientes"
    val ruta: String
)
