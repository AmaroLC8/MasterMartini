package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.model.Curso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Las dos formas de ordenar las recetas.
// Es un enum (y no un String como "alfabetico") para que solo existan estas dos opciones:
// si alguien escribe mal el nombre, no compila, en vez de fallar en silencio.
enum class OrdenRecetas {
    ALFABETICO, // Por título, de la A a la Z
    DURACION    // De la receta más corta a la más larga
}

// ESTADO DE LA PANTALLA RECETAS
// Junta en un solo objeto todo lo que RecetasScreen necesita para dibujarse.
data class RecetasUiState(
    // Las 5 secciones para los botones de filtro (vienen del repositorio)
    val categorias: List<String> = emptyList(),
    // Sección elegida. null = "Todas" (no hay ninguna sección elegida)
    val categoriaSeleccionada: String? = null,
    // Orden elegido. Parte en A - Z
    val orden: OrdenRecetas = OrdenRecetas.ALFABETICO,
    // true = mostrar solo las recetas veganas. Se COMBINA con la sección:
    // ej. "Pastelería" + "Solo veganas" = solo las pastelerías veganas.
    val soloVeganas: Boolean = false,
    // Recetas ya filtradas y ordenadas, listas para dibujar
    val recetas: List<Curso> = emptyList(),
    // Las mismas recetas, pero agrupadas por sección: clave = categoría, valor = sus recetas.
    // La usa la pantalla cuando está en "Todas" para poner un título por sección.
    // Se arma aquí (y no en la pantalla) porque la pantalla solo dibuja.
    val secciones: Map<String, List<Curso>> = emptyMap()
)

// VIEWMODEL DE LA PANTALLA RECETAS
// Filtra el catálogo por sección y lo ordena según lo que elija el usuario.
// Es un ViewModel normal (no AndroidViewModel) porque no usa Room ni el Context:
// los cursos salen de una lista fija del repositorio.
class RecetasViewModel : ViewModel() {

    // Fuente de los cursos (la misma que usan Home, Detalle y Favoritos)
    private val cursoRepository = CursoRepository()

    // Catálogo completo y secciones. Se piden UNA vez y se guardan:
    // al filtrar u ordenar siempre se parte de la lista completa, nunca de una ya filtrada.
    private val todosLosCursos: List<Curso> = cursoRepository.obtenerCursos()
    private val categorias: List<String> = cursoRepository.obtenerCategorias()

    // Estado privado y mutable: solo este ViewModel lo cambia.
    // Parte en "Todas", A - Z y sin el filtro vegano, con las recetas ya armadas
    // para la primera vez que se dibuja.
    private val _uiState = MutableStateFlow(
        armarEstado(categoriaSeleccionada = null, orden = OrdenRecetas.ALFABETICO, soloVeganas = false)
    )

    // Versión pública de solo lectura: RecetasScreen la observa con collectAsState()
    val uiState: StateFlow<RecetasUiState> = _uiState.asStateFlow()

    // Se llama al tocar un botón de sección. Recibe null cuando se toca "Todas".
    // Se mantienen el orden y el filtro vegano que ya estaban elegidos.
    fun seleccionarCategoria(categoria: String?) {
        _uiState.value = armarEstado(
            categoriaSeleccionada = categoria,
            orden = _uiState.value.orden,
            soloVeganas = _uiState.value.soloVeganas
        )
    }

    // Se llama al tocar "A - Z" o "Más cortas".
    // Se mantienen la sección y el filtro vegano que ya estaban elegidos.
    fun cambiarOrden(orden: OrdenRecetas) {
        _uiState.value = armarEstado(
            categoriaSeleccionada = _uiState.value.categoriaSeleccionada,
            orden = orden,
            soloVeganas = _uiState.value.soloVeganas
        )
    }

    // Se llama al tocar "Solo veganas": si estaba apagado lo enciende, y al revés
    // (! = "lo contrario de"). Se mantienen la sección y el orden.
    fun alternarSoloVeganas() {
        _uiState.value = armarEstado(
            categoriaSeleccionada = _uiState.value.categoriaSeleccionada,
            orden = _uiState.value.orden,
            soloVeganas = !_uiState.value.soloVeganas
        )
    }

    // Arma el estado completo a partir de las tres elecciones del usuario.
    // Así las tres funciones de arriba no repiten el mismo código.
    private fun armarEstado(
        categoriaSeleccionada: String?,
        orden: OrdenRecetas,
        soloVeganas: Boolean
    ): RecetasUiState {
        val recetas = filtrarYOrdenar(categoriaSeleccionada, orden, soloVeganas)
        return RecetasUiState(
            categorias = categorias,
            categoriaSeleccionada = categoriaSeleccionada,
            orden = orden,
            soloVeganas = soloVeganas,
            recetas = recetas,
            secciones = agruparPorSeccion(recetas)
        )
    }

    // Paso 1 y 2: FILTRAR y luego ORDENAR.
    private fun filtrarYOrdenar(
        categoriaSeleccionada: String?,
        orden: OrdenRecetas,
        soloVeganas: Boolean
    ): List<Curso> {
        // 1a. FILTRAR POR SECCIÓN. filter recorre la lista y deja solo los cursos que cumplen la condición.
        //     Si es null ("Todas"), no se filtra y quedan los 10 cursos.
        val porSeccion = if (categoriaSeleccionada == null) {
            todosLosCursos
        } else {
            todosLosCursos.filter { curso -> curso.categoria == categoriaSeleccionada }
        }

        // 1b. FILTRAR VEGANAS (sobre el resultado anterior, así los dos filtros se suman).
        //     Si el filtro está apagado, quedan todas las de la sección.
        val filtradas = if (soloVeganas) {
            porSeccion.filter { curso -> curso.esVegano }
        } else {
            porSeccion
        }

        // 2. ORDENAR. sortedBy devuelve una lista NUEVA ordenada según el valor que se le indique
        //    (no cambia la original). when elige qué valor usar según el orden elegido.
        return when (orden) {
            // Por título: compara los textos letra por letra (A antes que B)
            OrdenRecetas.ALFABETICO -> filtradas.sortedBy { curso -> curso.titulo }
            // Por duración: se ordena por el NÚMERO de minutos, no por el texto
            // (como texto, "5 min" quedaría después de "15 min", porque "5" > "1")
            OrdenRecetas.DURACION -> filtradas.sortedBy { curso -> minutosDe(curso) }
        }
    }

    // Saca los minutos del texto de la duración: "15 min" -> 15.
    // filter { it.isDigit() } deja solo los números del texto ("15 min" -> "15")
    // y toIntOrNull() lo convierte a Int. Si el texto no tiene números, devuelve null,
    // y en ese caso se usa 0 (con ?:) para que la app no se caiga.
    private fun minutosDe(curso: Curso): Int {
        return curso.duracion.filter { it.isDigit() }.toIntOrNull() ?: 0
    }

    // Paso 3: AGRUPAR por sección (para la vista "Todas").
    private fun agruparPorSeccion(recetas: List<Curso>): Map<String, List<Curso>> {
        // groupBy arma un Map: clave = categoría, valor = lista de sus recetas.
        // Dentro de cada grupo las recetas quedan en el mismo orden en que venían (ya ordenadas).
        // Ej: {"Decoración" = [curso 2, curso 4], "Panadería" = [curso 7, curso 3], ...}
        val agrupadas = recetas.groupBy { curso -> curso.categoria }

        // groupBy pone las secciones en el orden en que aparecen en la lista, y ese orden
        // cambia al ordenar por título o por duración. Para que las secciones salgan SIEMPRE
        // en el mismo orden (el del repositorio), se recorren las categorías una por una.
        // mutableMapOf guarda las claves en el orden en que se agregan.
        val seccionesEnOrden = mutableMapOf<String, List<Curso>>()
        for (categoria in categorias) {
            val recetasDeLaSeccion = agrupadas[categoria]
            // Si una sección no tiene recetas, no se agrega (así no sale un título vacío)
            if (recetasDeLaSeccion != null) {
                seccionesEnOrden[categoria] = recetasDeLaSeccion
            }
        }
        return seccionesEnOrden
    }
}
