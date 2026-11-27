package com.example.mastermartini.viewmodel

import com.example.mastermartini.model.Leccion
import com.example.mastermartini.model.Modulo

// Lección mientras se edita en el formulario; idLocal solo sirve para ubicarla en la pantalla
data class LeccionEditable(
    val idLocal: Int,
    val titulo: String = "",
    val descripcion: String = "",
    val duracion: String = "",
    val videoUrl: String = "",
    val subiendoVideo: Boolean = false,
    val errorSubida: String? = null
) {
    // Convierte a la Leccion que se envía al backend (sin espacios extra y con la duración como "15 min")
    fun aLeccion(): Leccion {
        return Leccion(
            titulo = titulo.trim(),
            descripcion = descripcion.trim(),
            duracion = normalizarDuracion(duracion),
            videoUrl = videoUrl.trim().ifBlank { null }
        )
    }
}

// Módulo mientras se edita; expandido indica si su Card está abierta en la pantalla
data class ModuloEditable(
    val idLocal: Int,
    val titulo: String = "",
    val lecciones: List<LeccionEditable> = emptyList(),
    val expandido: Boolean = true
) {
    // Convierte al Modulo que se envía al backend
    fun aModulo(): Modulo {
        return Modulo(titulo = titulo.trim(), lecciones = lecciones.map { it.aLeccion() })
    }

    // Todas las claves de error que pertenecen a este módulo y a sus lecciones
    fun clavesDeError(): List<String> {
        val claves = mutableListOf(ClaveError.tituloModulo(idLocal), ClaveError.leccionesModulo(idLocal))
        for (leccion in lecciones) {
            claves.add(ClaveError.tituloLeccion(leccion.idLocal))
            claves.add(ClaveError.descripcionLeccion(leccion.idLocal))
            claves.add(ClaveError.duracionLeccion(leccion.idLocal))
            claves.add(ClaveError.videoLeccion(leccion.idLocal))
        }
        return claves
    }
}

// Acción que espera que el instructor la confirme en un AlertDialog
sealed class AccionPendiente {
    data class EliminarModulo(val idModulo: Int) : AccionPendiente()
    data class EliminarLeccion(val idModulo: Int, val idLeccion: Int) : AccionPendiente()
    data class MoverModulo(val idModulo: Int, val haciaArriba: Boolean) : AccionPendiente()
    data class MoverLeccion(val idModulo: Int, val idLeccion: Int, val haciaArriba: Boolean) : AccionPendiente()
}

// Claves del mapa de errores: cada una identifica un campo de la pantalla
object ClaveError {
    const val TITULO = "titulo"
    const val DESCRIPCION = "descripcion"
    const val INSUMO = "insumo"
    const val CATEGORIA = "categoria"
    const val NIVEL = "nivel"
    const val MODULOS = "modulos"

    fun tituloModulo(idModulo: Int) = "modulo-$idModulo-titulo"
    fun leccionesModulo(idModulo: Int) = "modulo-$idModulo-lecciones"
    fun tituloLeccion(idLeccion: Int) = "leccion-$idLeccion-titulo"
    fun descripcionLeccion(idLeccion: Int) = "leccion-$idLeccion-descripcion"
    fun duracionLeccion(idLeccion: Int) = "leccion-$idLeccion-duracion"
    fun videoLeccion(idLeccion: Int) = "leccion-$idLeccion-video"
}

// Duración válida: solo minutos, como "15" o "15 min"
private val formatoDuracion = Regex("""^\d{1,3}(\s*min)?$""", RegexOption.IGNORE_CASE)

// La duración debe tener el formato de minutos y ser mayor que 0
fun duracionEsValida(texto: String): Boolean {
    val limpio = texto.trim()
    if (!formatoDuracion.matches(limpio)) return false
    return (limpio.filter { it.isDigit() }.toIntOrNull() ?: 0) > 0
}

// "15" o "15 MIN" pasan a "15 min"; una duración inválida queda vacía (no suma minutos)
fun normalizarDuracion(texto: String): String {
    if (!duracionEsValida(texto)) return ""
    val minutos = texto.filter { it.isDigit() }.toInt()
    return "$minutos min"
}

// 45 → "45 min", 120 → "2 h", 200 → "3 h 20 min"
fun formatearMinutos(minutos: Int): String {
    val horas = minutos / 60
    val resto = minutos % 60
    return when {
        horas == 0 -> "$resto min"
        resto == 0 -> "$horas h"
        else -> "$horas h $resto min"
    }
}

// Intercambia el elemento con el de arriba o el de abajo; si ya está en el borde, no cambia nada
fun <T> moverEnLista(lista: List<T>, indice: Int, haciaArriba: Boolean): List<T> {
    val destino = if (haciaArriba) indice - 1 else indice + 1
    if (indice !in lista.indices || destino !in lista.indices) return lista
    val copia = lista.toMutableList()
    copia[indice] = lista[destino]
    copia[destino] = lista[indice]
    return copia
}
