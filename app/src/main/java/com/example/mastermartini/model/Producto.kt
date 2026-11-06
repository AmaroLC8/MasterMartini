package com.example.mastermartini.model

// MOLDE DE DATOS (MODELO) DE UN PRODUCTO MASTER MARTINI
// Representa un producto del catálogo real de la marca (ej. "Chocolate Ariba").
// Vive en "model" igual que Curso: es un dato de la app, no algo visual.
data class Producto(
    // Identificador único. Se usa en la ruta "producto/{productoId}" para abrir su detalle.
    val id: Int,
    // Nombre EXACTO del producto. Debe escribirse igual que en CursoRepository
    // (insumo e ingredientes), porque así se encuentran las recetas que lo usan.
    val nombre: String,
    // Línea a la que pertenece (ej. "Margarinas", "Cremas vegetales").
    // Se usa para agrupar la lista de productos.
    val linea: String,
    // Descripción del producto (tomada del catálogo de la marca)
    val descripcion: String,
    // Usos recomendados (2 o 3). Lista de textos porque cada producto tiene los suyos.
    val usos: List<String>,
    // true si el producto es vegano. Por defecto false: solo se escribe en los que sí lo son.
    val esVegano: Boolean = false
)
