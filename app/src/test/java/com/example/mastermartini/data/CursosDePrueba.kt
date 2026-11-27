package com.example.mastermartini.data

import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.model.Leccion
import com.example.mastermartini.model.Modulo

// Cursos inventados para las pruebas: reemplazan al catálogo local que se eliminó de la app

// Arma un módulo con lecciones de 10 minutos cada una
private fun moduloDePrueba(numero: Int, cantidadLecciones: Int): Modulo {
    val lecciones = (1..cantidadLecciones).map { n ->
        Leccion(titulo = "Lección $n", descripcion = "Práctica $n", duracion = "10 min")
    }
    return Modulo(titulo = "Módulo $numero", lecciones = lecciones)
}

// Curso gratis de Chocolatería: 3 módulos de 3 lecciones (9 lecciones en total)
val cursoGratisDePrueba = Capacitacion(
    id = 1,
    titulo = "Bombones caseros para empezar",
    descripcion = "Templado básico y primeros bombones rellenos.",
    categoria = "Chocolatería",
    nivel = "Básico",
    esGratis = true,
    instructor = "Instructor de prueba",
    instructorId = 20,
    duracionTotal = "1 h 30 min",
    insumoPrincipal = "Chocolate Ariba",
    videoResumenUrl = "https://videos.example.com/bombones.mp4",
    loQueLograras = listOf("Templar chocolate"),
    incluye = listOf("Certificado"),
    modulos = listOf(moduloDePrueba(1, 3), moduloDePrueba(2, 3), moduloDePrueba(3, 3)),
    imagenUrl = "https://imagenes.example.com/bombones.jpg"
)

// Curso Plan Pro de Panadería: 2 módulos de 2 lecciones
val cursoProDePrueba = Capacitacion(
    id = 4,
    titulo = "Masa laminada para vender",
    descripcion = "Croissants y masa de hoja con técnica profesional.",
    categoria = "Panadería",
    nivel = "Avanzado",
    esGratis = false,
    instructor = "Instructor de prueba",
    instructorId = 21,
    duracionTotal = "2 h",
    insumoPrincipal = "Master Export Horneo",
    videoResumenUrl = null,
    loQueLograras = listOf("Laminar masa"),
    incluye = listOf("Certificado", "Recetario"),
    modulos = listOf(moduloDePrueba(1, 2), moduloDePrueba(2, 2)),
    imagenUrl = "https://imagenes.example.com/laminada.jpg"
)

// Copia de un curso de prueba con otro id, categoría o acceso (para armar catálogos)
fun cursoDePrueba(
    id: Int,
    categoria: String = "Chocolatería",
    esGratis: Boolean = true
): Capacitacion {
    return cursoGratisDePrueba.copy(
        id = id,
        titulo = "Curso de prueba $id",
        categoria = categoria,
        esGratis = esGratis
    )
}
