package com.example.mastermartini.data

import com.example.mastermartini.data.local.ChecklistDao
import com.example.mastermartini.data.local.ChecklistIngredienteEntity
import com.example.mastermartini.data.local.ChecklistPasoEntity
import kotlinx.coroutines.flow.Flow

// REPOSITORIO DE LOS CHECKLISTS DE LA RECETA (ingredientes y pasos)
// Igual que FavoritoRepository: recibe el DAO y le ofrece al ViewModel funciones simples.
// El ViewModel no conoce las entidades ni el SQL; solo pide y guarda números.
class ChecklistRepository(private val dao: ChecklistDao) {

    // Posiciones de los ingredientes marcados de un curso (se actualiza sola)
    fun obtenerMarcados(cursoId: Int): Flow<List<Int>> {
        return dao.obtenerMarcados(cursoId)
    }

    // Recibe los datos sueltos y arma la fila (entidad) que Room necesita.
    // guardar() usa @Upsert: inserta la fila si es nueva o la actualiza si ya existía.
    suspend fun marcar(cursoId: Int, indice: Int, marcado: Boolean) {
        dao.guardar(
            ChecklistIngredienteEntity(cursoId = cursoId, indice = indice, marcado = marcado)
        )
    }

    // Desmarca todos los ingredientes de un curso
    suspend fun reiniciar(cursoId: Int) {
        dao.reiniciar(cursoId)
    }

    // ----- CHECKLIST DE PASOS -----
    // Las mismas 3 funciones de arriba, pero para los pasos de la receta.

    // Posiciones de los pasos completados de un curso (se actualiza sola)
    fun obtenerPasosCompletados(cursoId: Int): Flow<List<Int>> {
        return dao.obtenerPasosCompletados(cursoId)
    }

    // Arma la fila (ChecklistPasoEntity) y la guarda con @Upsert (inserta o actualiza)
    suspend fun marcarPaso(cursoId: Int, indice: Int, completado: Boolean) {
        dao.guardarPaso(
            ChecklistPasoEntity(cursoId = cursoId, indice = indice, completado = completado)
        )
    }

    // Deja todos los pasos de un curso como no completados
    suspend fun reiniciarPasos(cursoId: Int) {
        dao.reiniciarPasos(cursoId)
    }
}
