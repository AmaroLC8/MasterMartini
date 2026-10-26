package com.example.mastermartini.data

import com.example.mastermartini.data.local.FavoritoDao
import com.example.mastermartini.data.local.FavoritoEntity
import kotlinx.coroutines.flow.Flow

// REPOSITORIO DE FAVORITOS
// Recibe el DAO y le ofrece al ViewModel funciones simples.
// El ViewModel no habla directo con el DAO ni conoce FavoritoEntity:
// si mañana los favoritos se guardan en una API, solo cambia esta clase.
class FavoritoRepository(private val dao: FavoritoDao) {

    // Ids de los cursos favoritos (se actualiza solo cuando cambia la tabla)
    fun obtenerIdsFavoritos(): Flow<List<Int>> {
        return dao.obtenerIdsFavoritos()
    }

    // Cantidad de favoritos (también se actualiza sola)
    fun contarFavoritos(): Flow<Int> {
        return dao.contarFavoritos()
    }

    // Recibe solo el id y arma la fila (FavoritoEntity) que Room necesita
    suspend fun agregar(cursoId: Int) {
        dao.agregar(FavoritoEntity(cursoId = cursoId))
    }

    suspend fun quitar(cursoId: Int) {
        dao.quitar(cursoId)
    }
}
