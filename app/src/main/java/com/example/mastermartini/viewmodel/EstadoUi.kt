package com.example.mastermartini.viewmodel

// Estado sellado genérico para representar carga, éxito o error en operaciones asíncronas
sealed interface EstadoUi<out T> {
    data object Cargando : EstadoUi<Nothing>
    data class Exito<T>(val datos: T) : EstadoUi<T>
    data class Error(val mensaje: String) : EstadoUi<Nothing>
}
