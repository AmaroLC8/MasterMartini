package com.example.mastermartini.model

// Modelo de producto del catálogo comercial de Master Martini
data class Producto(
    val id: Int,
    val nombre: String,
    val linea: String,
    val descripcion: String,
    val usos: List<String>,
    val esVegano: Boolean = false
)
