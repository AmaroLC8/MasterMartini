package com.example.mastermartini.data

import com.example.mastermartini.model.Curso

// REPOSITORIO DE CURSOS
// Es la única parte de la app que sabe DE DÓNDE salen los cursos.
// Hoy los datos son ficticios y están escritos aquí con listOf().
// En el futuro podrían venir de Room (base de datos del celular) o de una API en internet:
// el cambio grande sería en esta clase, y la pantalla seguiría igual, porque solo
// dibuja la lista que le entrega el ViewModel sin saber de dónde vienen los datos.
class CursoRepository {

    // Devuelve el catálogo completo (lista inmutable: nadie puede agregar ni borrar cursos desde afuera)
    fun obtenerCursos(): List<Curso> {
        return listOf(
            Curso(
                id = 1,
                titulo = "Curva de Templado en Chocolatería",
                insumo = "Cobertura Caravella Dark 54%",
                chef = "Chef Roberto Valenzuela",
                duracion = "15 min"
            ),
            Curso(
                id = 2,
                titulo = "Estabilización y Batido de Cremas",
                insumo = "Crema Vegetal Decor Up",
                chef = "Chef Elena Rossi",
                duracion = "20 min"
            ),
            Curso(
                id = 3,
                titulo = "Laminado de Masa de Hoja Artesanal",
                insumo = "Margarina Master Martini Placa",
                chef = "Maestro Marco Zanetti",
                duracion = "35 min"
            )
        )
    }
}
