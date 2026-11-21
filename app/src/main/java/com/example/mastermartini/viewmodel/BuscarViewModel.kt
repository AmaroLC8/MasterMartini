package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.model.Curso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.Normalizer

// ESTADO DE LA PANTALLA BUSCAR
// Junta en un solo objeto todo lo que BuscarScreen necesita para dibujarse.
data class BuscarUiState(
    // Lo que el usuario escribió, TAL CUAL (con sus mayúsculas y tildes).
    // Es lo que se ve en el campo de texto.
    val texto: String = "",
    // Recetas que coinciden con el texto. Vacía si todavía no se escribe nada.
    val resultados: List<Curso> = emptyList(),
    // Las 5 secciones, para los botones de "Sugerencias" (vienen del repositorio)
    val categorias: List<String> = emptyList()
)

// VIEWMODEL DE LA PANTALLA BUSCAR
// Busca recetas MIENTRAS el usuario escribe: cada letra nueva vuelve a filtrar el catálogo.
// Es un ViewModel normal (no AndroidViewModel) porque no usa Room ni el Context:
// los cursos salen de una lista fija del repositorio.
class BuscarViewModel : ViewModel() {

    // Fuente de los cursos (la misma que usan Home, Detalle, Favoritos y Recetas)
    private val cursoRepository = CursoRepository()

    // Catálogo completo. Se pide UNA vez y se guarda: cada búsqueda parte de la lista
    // completa, nunca de los resultados de la búsqueda anterior.
    private val todosLosCursos: List<Curso> = cursoRepository.obtenerCursos()

    // Estado privado y mutable: solo este ViewModel lo cambia.
    // Parte con el campo vacío, sin resultados y con las categorías para las sugerencias.
    private val _uiState = MutableStateFlow(
        BuscarUiState(categorias = cursoRepository.obtenerCategorias())
    )

    // Versión pública de solo lectura: BuscarScreen la observa con collectAsState()
    val uiState: StateFlow<BuscarUiState> = _uiState.asStateFlow()

    // Se llama con CADA letra que se escribe o se borra (y al tocar una sugerencia).
    // Guarda el texto y vuelve a buscar en el mismo momento.
    fun onTextoChange(nuevoTexto: String) {
        _uiState.value = _uiState.value.copy(
            texto = nuevoTexto,
            resultados = buscar(nuevoTexto)
        )
    }

    // Botón X del campo: deja el campo vacío y sin resultados (vuelven las sugerencias)
    fun limpiar() {
        _uiState.value = _uiState.value.copy(texto = "", resultados = emptyList())
    }

    // Devuelve los cursos que contienen el texto buscado en su título, categoría,
    // insumo o en alguno de sus ingredientes.
    private fun buscar(texto: String): List<Curso> {
        // trim() quita los espacios del principio y del final ("  cacao " -> "cacao")
        val buscado = normalizar(texto).trim()

        // Si no hay nada escrito (o solo espacios), no se busca: así no salen los 10 cursos
        // de golpe, y la pantalla muestra las sugerencias.
        if (buscado.isEmpty()) {
            return emptyList()
        }

        // filter deja solo los cursos donde ALGUNO de los campos contiene el texto.
        // || significa "o": basta con que uno de los cuatro coincida.
        // Los dos lados se normalizan, así "Chocolatería" y "chocolateria" quedan iguales.
        return todosLosCursos.filter { curso ->
            normalizar(curso.titulo).contains(buscado) ||
                normalizar(curso.categoria).contains(buscado) ||
                normalizar(curso.insumo).contains(buscado) ||
                // ingredientes es una LISTA de textos: any devuelve true si
                // AL MENOS UNO de los ingredientes contiene el texto
                curso.ingredientes.any { ingrediente -> normalizar(ingrediente).contains(buscado) }
        }
    }

    // Deja un texto "sin tildes y en minúsculas" para poder compararlo:
    // "Chocolatería" -> "chocolateria", "CRÈME" -> "creme".
    // Pensado para usuarios mayores: si no ponen la tilde o escriben con mayúscula, igual encuentran.
    private fun normalizar(texto: String): String {
        // 1. Normalizer.normalize con NFD SEPARA cada letra con tilde en dos partes:
        //    la letra sola + la tilde como un carácter aparte. Ej: "í" -> "i" + "´".
        val separado = Normalizer.normalize(texto, Normalizer.Form.NFD)
        // 2. Se borran esas tildes sueltas. "\\p{Mn}" significa "cualquier marca que va encima
        //    de una letra" (tilde, diéresis, la colita de la ñ...). Quedan solo las letras.
        val sinTildes = separado.replace(Regex("\\p{Mn}"), "")
        // 3. Todo a minúsculas: "Cacao" y "cacao" quedan iguales
        return sinTildes.lowercase()
    }
}
