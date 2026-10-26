package com.example.mastermartini.model

// MOLDE DE DATOS (MODELO)
// Representa cada curso o ficha técnica del catálogo de Master Martini.
// Vive en "model" y no en la pantalla, porque es un dato de la app, no algo visual:
// lo pueden usar el repositorio, el ViewModel y cualquier pantalla.
data class Curso(
    // Identificador único de cada curso. Más adelante servirá para guardar
    // los cursos favoritos con Room (la tabla guardaría solo este número).
    val id: Int,
    val titulo: String,
    val insumo: String,
    val chef: String,
    val duracion: String
)
