package com.example.mastermartini.model

// Modelo de receta o ficha técnica del catálogo
data class Curso(
    val id: Int,
    val titulo: String,
    val insumo: String,
    val chef: String,
    val duracion: String,
    val pasos: List<String>,
    val categoria: String,
    val descripcion: String,
    val ingredientes: List<String>,
    val rendimiento: String,
    val dificultad: String,
    val esVegano: Boolean = false,
    val videoUrl: String? = null,
    val imagenUrl: String? = null
)
