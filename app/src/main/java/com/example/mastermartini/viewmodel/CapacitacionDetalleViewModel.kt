package com.example.mastermartini.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.CursoServidorException
import com.example.mastermartini.data.ProductoRepository
import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ESTADO DE LA PANTALLA DETALLE DEL CURSO
data class CapacitacionDetalleUiState(
    // Curso que se muestra. null mientras carga o si el id no existe
    val capacitacion: Capacitacion? = null,
    // Producto Master Martini principal del curso. null si no se encontró por nombre
    val producto: Producto? = null,
    // Posiciones de los módulos DESPLEGADOS (abiertos). Ej: {0, 2} = el 1° y el 3° están abiertos.
    // Es un Set (conjunto) y no una List porque un módulo está abierto o no:
    // no tiene sentido que aparezca dos veces.
    val modulosAbiertos: Set<Int> = emptySet(),
    // ¿Se muestran los botones "Editar curso" y "Eliminar curso"? Los calcula el Rol
    // de quien está conectado (polimorfismo, ver calcularPermisos). El servidor igual revisa (403).
    val puedeEditar: Boolean = false,
    val puedeEliminar: Boolean = false,
    // true = se está mostrando el diálogo "¿Eliminar este curso?"
    val mostrarConfirmacionEliminar: Boolean = false,
    // true mientras se espera la respuesta del servidor al eliminar (el botón se desactiva)
    val eliminando: Boolean = false,
    // Texto del error para mostrar en rojo (ej. sin permiso). null = no hay error
    val mensajeError: String? = null
)

// VIEWMODEL DEL DETALLE DE UN CURSO ONLINE
// Busca el curso por su id (en el microservicio de cursos, con respaldo local), su producto
// principal, y recuerda qué módulos están abiertos.
// El estado abierto/cerrado vive AQUÍ y no en la pantalla (MVVM): la pantalla solo dibuja.
// También decide si se ven los botones Editar/Eliminar y elimina el curso.
// Es AndroidViewModel porque necesita el Context (application) para leer la SESIÓN
// (id y rol) en SharedPreferences con AlmacenamientoLocal. viewModel() en MainActivity
// le pasa la Application solo, igual que a PerfilViewModel.
class CapacitacionDetalleViewModel(application: Application) : AndroidViewModel(application) {

    private val capacitacionRepository = CapacitacionRepository()

    // Sesión guardada: quién está conectado (id) y qué puede hacer (rol)
    private val almacenamiento = AlmacenamientoLocal(application)

    // Para encontrar el producto principal del curso (igual que DetalleViewModel con el insumo)
    private val productoRepository = ProductoRepository()

    // Estado privado y mutable: solo este ViewModel lo cambia
    private val _uiState = MutableStateFlow(CapacitacionDetalleUiState())

    // Versión pública de solo lectura: CapacitacionDetalleScreen la observa con collectAsState()
    val uiState: StateFlow<CapacitacionDetalleUiState> = _uiState.asStateFlow()

    // La llama la pantalla con el id que llegó por la ruta ("capacitacion/4" -> id = 4).
    fun cargarCapacitacion(id: Int) {
        // Si ese curso ya está cargado, no se hace nada. Pasa al girar el celular: la pantalla
        // se vuelve a crear y llama otra vez a esta función, pero el ViewModel sobrevive.
        // Sin este if, los módulos que el usuario abrió se cerrarían solos.
        if (_uiState.value.capacitacion?.id == id) return

        // El curso se busca en el servidor (internet es lento), por eso va en una corrutina.
        // obtenerCapacitacionPorIdRemoto() pide ESE curso al microservicio (GET api/cursos/{id});
        // así también se abren los cursos creados por un Instructor o Administrador.
        // Si el servidor está apagado, busca en la lista local de respaldo (la app no se cae).
        viewModelScope.launch {
            val capacitacionEncontrada = capacitacionRepository.obtenerCapacitacionPorIdRemoto(id)

            // Se busca el producto por el nombre EXACTO del insumo principal.
            // Si no hay curso o ningún producto se llama así, queda en null.
            val productoEncontrado = if (capacitacionEncontrada != null) {
                productoRepository.obtenerProductoPorNombre(capacitacionEncontrada.insumoPrincipal)
            } else {
                null
            }

            _uiState.value = CapacitacionDetalleUiState(
                capacitacion = capacitacionEncontrada,
                producto = productoEncontrado,
                // Al abrir el curso, el Módulo 1 (posición 0) parte desplegado,
                // así el usuario ve de inmediato cómo son las lecciones.
                modulosAbiertos = setOf(0),
                puedeEditar = calcularPuedeEditar(capacitacionEncontrada),
                puedeEliminar = calcularPuedeEliminar(capacitacionEncontrada)
            )
        }
    }

    // Vuelve a pedir el curso al servidor AUNQUE sea el mismo id (cargarCapacitacion no lo hace).
    // La usa la pantalla cada vez que vuelve a entrar con el curso ya cargado, por ejemplo al
    // volver de "Editar curso": así se ven los datos nuevos.
    // A diferencia de cargarCapacitacion, usa copy(): CONSERVA los módulos que el usuario
    // tenía abiertos y solo cambia el curso, su producto y los permisos.
    fun recargarCapacitacion(id: Int) {
        viewModelScope.launch {
            val capacitacionActualizada = capacitacionRepository.obtenerCapacitacionPorIdRemoto(id)
            val productoEncontrado = if (capacitacionActualizada != null) {
                productoRepository.obtenerProductoPorNombre(capacitacionActualizada.insumoPrincipal)
            } else {
                null
            }
            _uiState.update {
                it.copy(
                    capacitacion = capacitacionActualizada,
                    producto = productoEncontrado,
                    puedeEditar = calcularPuedeEditar(capacitacionActualizada),
                    puedeEliminar = calcularPuedeEliminar(capacitacionActualizada)
                )
            }
        }
    }

    // PERMISOS CON POLIMORFISMO: no hay ningún "if (rol == ...)". Se le PREGUNTA al Rol
    // de la sesión y cada rol (Cliente, Instructor, Ventas, Administrador) responde lo suyo.
    // Si no hay curso (null), no se muestra ningún botón.

    // ¿Se muestra "Editar curso"? Se le pasa el dueño del curso y el id de quien está conectado.
    private fun calcularPuedeEditar(capacitacion: Capacitacion?): Boolean {
        if (capacitacion == null) return false
        val rol = almacenamiento.obtenerRol()
        return rol.puedeEditarCurso(capacitacion.instructorId, almacenamiento.obtenerIdUsuario())
    }

    // ¿Se muestra "Eliminar curso"?
    private fun calcularPuedeEliminar(capacitacion: Capacitacion?): Boolean {
        if (capacitacion == null) return false
        return almacenamiento.obtenerRol().puedeEliminarCursos
    }

    // DIÁLOGO DE CONFIRMACIÓN: el botón "Eliminar curso" no borra directo, primero pregunta.
    // Se abre al tocar el botón y se cierra con "Cancelar" o tocando fuera del diálogo.
    fun abrirConfirmacionEliminar() {
        _uiState.update { it.copy(mostrarConfirmacionEliminar = true, mensajeError = null) }
    }

    fun cerrarConfirmacionEliminar() {
        _uiState.update { it.copy(mostrarConfirmacionEliminar = false) }
    }

    // Se llama al confirmar "Eliminar" en el diálogo. Borra el curso en el servidor.
    // onEliminado: lo que hay que hacer si salió bien (la pantalla le pasa "volver a la lista",
    // que se recarga sola al entrar). Si falla, se muestra el error y se queda en el detalle.
    fun eliminarCurso(onEliminado: () -> Unit) {
        val capacitacion = _uiState.value.capacitacion ?: return
        // El servidor necesita saber quién pide borrar (header X-Usuario-Id)
        val usuarioId = almacenamiento.obtenerIdUsuario()
        if (usuarioId == null) {
            _uiState.update {
                it.copy(
                    mostrarConfirmacionEliminar = false,
                    mensajeError = "Debes iniciar sesión como administrador"
                )
            }
            return
        }

        _uiState.update {
            it.copy(mostrarConfirmacionEliminar = false, eliminando = true, mensajeError = null)
        }
        viewModelScope.launch {
            val resultado = capacitacionRepository.eliminarCapacitacion(usuarioId.toInt(), capacitacion.id)
            // fold: una función para cuando salió bien (onSuccess) y otra para cuando falló (onFailure)
            resultado.fold(
                onSuccess = {
                    _uiState.update { it.copy(eliminando = false) }
                    onEliminado()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(eliminando = false, mensajeError = textoDelError(error)) }
                }
            )
        }
    }

    // Elige el texto del error al eliminar (igual que CrearCursoViewModel.textoDelError):
    //   403 -> quien está conectado no es administrador
    //   503 -> el servidor de cursos no pudo consultar al microservicio de usuarios
    //   otro -> el "mensaje" que mandó el servidor (ej. 404 "Curso no encontrado")
    // Si no es un error del servidor (sin conexión), se usa el mensaje que trae el error.
    private fun textoDelError(error: Throwable): String {
        if (error is CursoServidorException) {
            return when (error.codigo) {
                403 -> "Solo el administrador puede eliminar cursos"
                503 -> "El servicio de usuarios no está disponible"
                else -> error.mensajeServidor ?: error.message ?: "No se pudo eliminar el curso"
            }
        }
        return error.message ?: "No se pudo eliminar el curso"
    }

    // Se llama al tocar el encabezado de un módulo: si estaba abierto lo cierra, y al revés.
    // "-" y "+" sobre un Set devuelven un Set NUEVO (sin o con ese número).
    // Se crea uno nuevo (en vez de cambiar el que hay) para que el StateFlow note el cambio.
    fun alternarModulo(indice: Int) {
        val abiertos = _uiState.value.modulosAbiertos
        val nuevosAbiertos = if (abiertos.contains(indice)) {
            abiertos - indice
        } else {
            abiertos + indice
        }
        // copy() crea un estado igual al actual, cambiando solo modulosAbiertos
        _uiState.value = _uiState.value.copy(modulosAbiertos = nuevosAbiertos)
    }

    // REGLAS DE LAS LECCIONES (freemium). Están aquí y no en la pantalla para que,
    // si cambian (ej. "las 2 primeras lecciones son muestra"), se toque un solo lugar.

    // ¿Es la 1ª lección del Módulo 1? (posiciones 0 y 0, porque las listas parten en 0)
    private fun esPrimeraLeccion(indiceModulo: Int, indiceLeccion: Int): Boolean {
        return indiceModulo == 0 && indiceLeccion == 0
    }

    // ¿Esta lección lleva la etiqueta "Muestra gratis"?
    // Solo en los cursos del Plan Pro, y solo la 1ª lección del Módulo 1.
    // (En un curso gratis no tiene sentido: todas las lecciones ya son gratis.)
    fun esMuestraGratis(indiceModulo: Int, indiceLeccion: Int): Boolean {
        val capacitacion = _uiState.value.capacitacion ?: return false
        return !capacitacion.esGratis && esPrimeraLeccion(indiceModulo, indiceLeccion)
    }

    // ¿Esta lección lleva candado? Solo en los cursos del Plan Pro, y nunca la muestra gratis.
    // En los cursos gratis ninguna lección tiene candado.
    // FASE 2: aquí también se revisará si el usuario tiene el Plan Pro activo.
    fun estaBloqueada(indiceModulo: Int, indiceLeccion: Int): Boolean {
        val capacitacion = _uiState.value.capacitacion ?: return false
        return !capacitacion.esGratis && !esPrimeraLeccion(indiceModulo, indiceLeccion)
    }
}