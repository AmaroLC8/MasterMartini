package com.example.mastermartini.data

import com.example.mastermartini.data.local.FavoritoDao
import com.example.mastermartini.data.local.FavoritoEntity
import kotlinx.coroutines.flow.Flow

// Repositorio para gestionar recetas favoritas almacenadas en Room
class FavoritoRepository(private val dao: FavoritoDao) {

    fun obtenerIdsFavoritos(): Flow<List<Int>> {
        return dao.obtenerIdsFavoritos()
    }

    fun contarFavoritos(): Flow<Int> {
        return dao.contarFavoritos()
    }

    suspend fun agregar(cursoId: Int) {
        dao.agregar(FavoritoEntity(cursoId = cursoId))
    }

    suspend fun quitar(cursoId: Int) {
        dao.quitar(cursoId)
    }
}
