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
    val duracion: String,
    // Pasos de la receta, en orden. Es una lista de textos porque cada curso
    // tiene una cantidad distinta de pasos. Los muestra la pantalla de Detalle.
    val pasos: List<String>,
    // Sección a la que pertenece la receta ("Chocolatería", "Pastelería", etc.).
    // La usará la pantalla de Recetas para filtrar por sección.
    val categoria: String,
    // Una o dos frases que cuentan qué se aprende en el curso
    val descripcion: String,
    // Ingredientes con su cantidad (ej: "200 g de Cobertura Caravella Dark 54%").
    // Es una lista de textos, igual que los pasos, porque cada receta tiene una cantidad distinta.
    val ingredientes: List<String>,
    // Dirección del video en la nube (AWS). Es String? porque puede no existir:
    // "= null" es su valor por defecto, así que si no se escribe, queda en null.
    // Mientras sea null, el Detalle muestra el video local guardado en res/raw.
    val videoUrl: String? = null
)
