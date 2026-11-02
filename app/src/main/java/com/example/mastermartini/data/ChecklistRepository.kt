package com.example.mastermartini.data

import com.example.mastermartini.data.local.ChecklistDao
import com.example.mastermartini.data.local.ChecklistIngredienteEntity
import kotlinx.coroutines.flow.Flow

// REPOSITORIO DEL CHECKLIST DE INGREDIENTES
// Igual que FavoritoRepository: recibe el DAO y le ofrece al ViewModel funciones simples.
// El ViewModel no conoce ChecklistIngredienteEntity ni el SQL; solo pide y guarda números.
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
}
