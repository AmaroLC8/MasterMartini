package com.example.mastermartini.data

import com.example.mastermartini.model.Curso
import com.example.mastermartini.model.Producto

// Recetas y productos inventados para las pruebas: reemplazan a las listas locales que se eliminaron de la app

// Arma una receta con lo mínimo que usan las pruebas (filtros, búsqueda y orden)
private fun recetaDePrueba(
    id: Int,
    titulo: String,
    categoria: String,
    insumo: String,
    duracion: String,
    ingredientes: List<String>,
    esVegano: Boolean = false
): Curso {
    return Curso(
        id = id,
        titulo = titulo,
        insumo = insumo,
        chef = "Chef de prueba $id",
        duracion = duracion,
        pasos = listOf("Paso 1 de $titulo", "Paso 2 de $titulo"),
        categoria = categoria,
        descripcion = "Descripción de $titulo",
        ingredientes = ingredientes,
        rendimiento = "Rinde 10 porciones",
        dificultad = "Fácil",
        esVegano = esVegano,
        imagenUrl = "https://imagenes.example.com/receta$id.jpg"
    )
}

// 6 recetas; categorías en orden de llegada: Chocolatería, Pastelería, Panadería, Heladería
// Veganas: 3 y 4. Usan "Cobertura Selva Negra": 1 y 6
val recetasDePrueba = listOf(
    recetaDePrueba(1, "Bombones de maracuyá", "Chocolatería", "Cobertura Selva Negra", "40 min",
        listOf("300 g de Cobertura Selva Negra", "100 ml de pulpa de maracuyá")),
    recetaDePrueba(2, "Alfajores de maicena", "Pastelería", "Manjar Doña Rosa", "25 min",
        listOf("200 g de Manjar Doña Rosa", "Coco rallado")),
    recetaDePrueba(3, "Pan de masa madre", "Panadería", "Harina Molino Sur", "90 min",
        listOf("1 kg de Harina Molino Sur", "Sal de mar"), esVegano = true),
    recetaDePrueba(4, "Torta de zanahoria vegana", "Pastelería", "Crema Vegetal Nube", "60 min",
        listOf("200 ml de Crema Vegetal Nube", "Zanahoria rallada", "Almendras tostadas"), esVegano = true),
    recetaDePrueba(5, "Helado de lúcuma", "Heladería", "Base Blanca Polar", "15 min",
        listOf("1 litro de Base Blanca Polar", "Pulpa de lúcuma")),
    recetaDePrueba(6, "Trufas de cacao", "Chocolatería", "Cacao Amargo Andes", "20 min",
        listOf("200 g de Cobertura Selva Negra", "Cacao Amargo Andes"))
)

// Arma un producto inventado
private fun productoDePrueba(id: Int, nombre: String, linea: String, esVegano: Boolean = false): Producto {
    return Producto(
        id = id,
        nombre = nombre,
        linea = linea,
        descripcion = "Descripción de $nombre",
        usos = listOf("Uso 1 de $nombre", "Uso 2 de $nombre"),
        esVegano = esVegano,
        imagenUrl = "https://imagenes.example.com/producto$id.jpg"
    )
}

// 6 productos; líneas en orden de llegada: Chocolates y coberturas, Cremas vegetales, Harinas, Mantecas
// "Manteca Dorada" (4) no la usa ninguna receta
val productosDePrueba = listOf(
    productoDePrueba(1, "Cobertura Selva Negra", "Chocolates y coberturas"),
    productoDePrueba(2, "Crema Vegetal Nube", "Cremas vegetales", esVegano = true),
    productoDePrueba(3, "Harina Molino Sur", "Harinas"),
    productoDePrueba(4, "Manteca Dorada", "Mantecas"),
    productoDePrueba(5, "Cobertura Blanca Selva", "Chocolates y coberturas"),
    productoDePrueba(6, "Crema Batida Nube", "Cremas vegetales")
)
