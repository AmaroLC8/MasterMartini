package com.example.mastermartini.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.DatosEmpresa
import com.example.mastermartini.data.InscripcionRepository
import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.model.Inscripcion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

// Textos de los 3 primeros filtros. Se guardan en constantes para escribirlos UNA vez:
// el ViewModel y la lista de filtros usan exactamente el mismo texto (si no, un error
// de tipeo como "Plan pro" haría que el filtro no funcione).
const val FILTRO_TODOS = "Todos"
const val FILTRO_GRATIS = "Gratis"
const val FILTRO_PLAN_PRO = "Plan Pro"

// UNA TARJETA DE "MIS CURSOS", ya lista para dibujar: a qué curso lleva, su título y el avance.
// El porcentaje se calcula en el ViewModel (no en la pantalla).
data class MiCurso(
    // Id del curso: al tocar la tarjeta se abre "capacitacion/{cursoId}"
    val cursoId: Int,
    val titulo: String,
    // De 0 a 100
    val porcentaje: Int
)

// ESTADO DE LA PANTALLA CURSOS
// Junta en un solo objeto todo lo que CapacitacionesScreen necesita para dibujarse.
data class CapacitacionesUiState(
    // Textos de los botones de filtro: "Todos", "Gratis", "Plan Pro" y las 5 categorías
    val filtros: List<String> = emptyList(),
    // Filtro elegido. Parte en "Todos"
    val filtroSeleccionado: String = FILTRO_TODOS,
    // Cursos que cumplen el filtro, listos para dibujar
    val capacitaciones: List<Capacitacion> = emptyList(),
    // Precio del Plan Pro ya escrito en pesos chilenos, ej. "$9.990"
    val precioPlanPro: String = "",
    // Cursos en los que el usuario está inscrito, con su avance. Vacía = "Aún no te inscribes..."
    val misCursos: List<MiCurso> = emptyList()
)

// VIEWMODEL DE LA PANTALLA CURSOS
// Misma estructura que RecetasViewModel: guarda la lista completa y, al tocar un filtro,
// arma un estado nuevo partiendo siempre de esa lista.
// Los cursos vienen del MICROSERVICIO de cursos (Retrofit, puerto 9092), así aparecen
// también los que crea un Instructor o Administrador. Si el servidor no responde, el
// repositorio entrega la lista local de respaldo y la pantalla sigue funcionando.
// También arma "Mis cursos" con las inscripciones del usuario (microservicio de interacciones, 9093).
// Es AndroidViewModel porque necesita el Context (application) para leer el id de la SESIÓN
// con AlmacenamientoLocal. viewModel() en MainActivity le pasa la Application solo.
class CapacitacionesViewModel(application: Application) : AndroidViewModel(application) {

    // Fuente de los cursos online (servidor o lista local de respaldo)
    private val capacitacionRepository = CapacitacionRepository()

    // Inscripciones del usuario y sesión (para saber su id)
    private val inscripcionRepository = InscripcionRepository()
    private val almacenamiento = AlmacenamientoLocal(application)

    // Inscripciones del usuario tal como vienen del servidor. Parte vacía.
    // OJO: va ANTES de _uiState, porque armarEstado() la usa al crear el estado inicial
    // (Kotlin crea las propiedades en el orden en que están escritas).
    private var inscripciones: List<Inscripcion> = emptyList()

    // Catálogo completo. Es "var" porque cambia: parte con la lista LOCAL (para que la
    // pantalla nunca aparezca vacía) y se reemplaza por la del servidor al recargar.
    private var todasLasCapacitaciones: List<Capacitacion> = capacitacionRepository.obtenerCapacitaciones()
    // Las categorías no cambian: se piden UNA vez y se guardan
    private val categorias: List<String> = capacitacionRepository.obtenerCategorias()

    // Lista de filtros: los 3 fijos y después las categorías.
    // "+" une dos listas en una nueva: ["Todos", "Gratis", "Plan Pro", "Chocolatería", ...]
    private val filtros: List<String> = listOf(FILTRO_TODOS, FILTRO_GRATIS, FILTRO_PLAN_PRO) + categorias

    // Estado privado y mutable: solo este ViewModel lo cambia. Parte en "Todos".
    private val _uiState = MutableStateFlow(armarEstado(FILTRO_TODOS))

    // Versión pública de solo lectura: CapacitacionesScreen la observa con collectAsState()
    val uiState: StateFlow<CapacitacionesUiState> = _uiState.asStateFlow()

    // init se ejecuta una vez, al crearse el ViewModel: pide al servidor los cursos actualizados
    init {
        recargarCapacitaciones()
    }

    // Trae los cursos desde el microservicio (incluyendo los creados por Admin) y las
    // inscripciones del usuario ("Mis cursos").
    // La llama init y también CapacitacionesScreen CADA VEZ que se entra a la pantalla:
    // así, al volver de "Crear curso" el curso nuevo ya aparece, y al volver de un detalle
    // "Mis cursos" muestra el avance nuevo (o el curso en que se acaba de inscribir).
    // Las funciones del repositorio son suspend (internet es lento), por eso va en una corrutina.
    // Si el servidor está apagado, el repositorio devuelve la lista local (la app no se cae).
    // Se vuelve a armar el estado con el filtro que ya estaba elegido, para no perderlo.
    fun recargarCapacitaciones() {
        viewModelScope.launch {
            todasLasCapacitaciones = capacitacionRepository.obtenerCapacitacionesRemotas()
            // Primero el catálogo y DESPUÉS las inscripciones: el avance necesita el total
            // de lecciones de cada curso, que sale del catálogo.
            cargarInscripciones()
            _uiState.value = armarEstado(_uiState.value.filtroSeleccionado)
        }
    }

    // Pide al servidor las inscripciones del usuario de la sesión.
    // Sin sesión: lista vacía. Si el servidor falla, se dejan las que ya había
    // (onSuccess solo se ejecuta si salió bien).
    private suspend fun cargarInscripciones() {
        val usuarioId = almacenamiento.obtenerIdUsuario()
        if (usuarioId == null) {
            inscripciones = emptyList()
            return
        }
        inscripcionRepository.misInscripciones(usuarioId).onSuccess { lista ->
            inscripciones = lista
        }
    }

    // Convierte cada inscripción en una tarjeta de "Mis cursos".
    // El total de lecciones sale del curso con el MISMO id en el catálogo (find).
    // Si el curso ya no está en el catálogo, el total es 0 y el porcentaje queda en 0.
    // El título: el que guardó el servidor al inscribirse o, si no vino, el del catálogo.
    private fun armarMisCursos(): List<MiCurso> {
        return inscripciones.map { inscripcion ->
            val curso = todasLasCapacitaciones.find { capacitacion -> capacitacion.id == inscripcion.cursoId }
            val totalLecciones = curso?.totalLecciones ?: 0
            MiCurso(
                cursoId = inscripcion.cursoId,
                titulo = inscripcion.cursoTitulo ?: curso?.titulo ?: "Curso Master Martini",
                porcentaje = inscripcion.porcentaje(totalLecciones)
            )
        }
    }

    // Se llama al tocar un botón de filtro
    fun seleccionarFiltro(filtro: String) {
        _uiState.value = armarEstado(filtro)
    }

    // Arma el estado completo a partir del filtro elegido
    private fun armarEstado(filtroSeleccionado: String): CapacitacionesUiState {
        return CapacitacionesUiState(
            filtros = filtros,
            filtroSeleccionado = filtroSeleccionado,
            capacitaciones = filtrar(filtroSeleccionado),
            misCursos = armarMisCursos(),
            precioPlanPro = formatearPesos(DatosEmpresa.PRECIO_PLAN_PRO)
        )
    }

    // Deja solo los cursos que cumplen el filtro.
    // when revisa el filtro: los 3 fijos tienen su propia regla y,
    // si no es ninguno de ellos (else), es el nombre de una categoría.
    // "it" es cada curso de la lista (el nombre que Kotlin le da por defecto dentro del filter).
    private fun filtrar(filtro: String): List<Capacitacion> {
        return when (filtro) {
            FILTRO_TODOS -> todasLasCapacitaciones
            FILTRO_GRATIS -> todasLasCapacitaciones.filter { it.esGratis }
            // ! = "lo contrario de": los que NO son gratis son los del Plan Pro
            FILTRO_PLAN_PRO -> todasLasCapacitaciones.filter { !it.esGratis }
            else -> todasLasCapacitaciones.filter { it.categoria == filtro }
        }
    }

    // Convierte un monto en el texto en pesos chilenos: 9990 -> "$9.990".
    // "%,d" escribe el número con separador de miles. Con Locale.US el separador es la coma
    // ("9,990"), y luego replace la cambia por el punto que se usa en Chile ("9.990").
    // Se fija Locale.US para que el resultado sea siempre el mismo, sin importar el idioma del celular.
    private fun formatearPesos(monto: Int): String {
        val conComas = String.format(Locale.US, "%,d", monto)
        return "$" + conComas.replace(",", ".")
    }
}