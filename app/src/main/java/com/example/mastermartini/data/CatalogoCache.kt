package com.example.mastermartini.data

import com.example.mastermartini.model.Curso
import com.example.mastermartini.model.Producto

// Memoria compartida por toda la app con la última lista de recetas y productos que llegó bien del servidor
// Así Inicio, Recetas, Buscar, Productos y Favoritos no vuelven a pedir lo mismo en cada pantalla
object CatalogoCache {

    // null = todavía no se ha descargado (o se borró)
    var recetas: List<Curso>? = null
    var productos: List<Producto>? = null

    // Olvida todo; la próxima lectura irá al servidor (se usa en las pruebas)
    fun limpiar() {
        recetas = null
        productos = null
    }
}
