package com.example.mastermartini.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.FavoritoRepository
import com.example.mastermartini.data.FotoRepository
import com.example.mastermartini.data.local.MasterMartiniDatabase
import com.example.mastermartini.model.OpcionPanel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ESTADO DE LA PANTALLA PERFIL
// Junta en un solo objeto todo lo que PerfilScreen muestra.
// Viene de 3 lugares: SharedPreferences (perfil y encuesta), el archivo de la foto y Room (favoritos).
data class PerfilUiState(
    // Datos del registro
    val nombre: String = "",
    val rut: String = "",
    val correo: String = "",
    val telefono: String = "",
    val rubro: String = "",
    // Cuántas personas trabajan en el negocio, ya listo para mostrar
    // ("No indicado" si eligió "Prefiero no decirlo" o si todavía no responde)
    val personasNegocio: String = "",
    // Foto del local (null = no hay foto; la pantalla muestra un ícono de persona)
    val foto: Bitmap? = null,
    // Datos de la encuesta
    val intereses: List<String> = emptyList(),
    val nivel: String = "",
    val ubicacion: String = "",
    // true si la encuesta ya se respondió. Con esto la pantalla elige si muestra
    // los intereses o el botón "Responder encuesta".
    val respondioEncuesta: Boolean = false,
    // Cantidad de recetas favoritas (viene de Room y se actualiza sola)
    val totalFavoritos: Int = 0,
    // Datos de la sesión: el rol ya como texto para mostrar ("Cliente", "Ventas"...)
    // y si tiene el Plan Pro activo (para mostrar la etiqueta "Plan Pro activo")
    val rol: String = "",
    val planPro: Boolean = false,
    // Botones del "Panel de trabajo". Vacía = no se muestra la tarjeta (ej. Cliente)
    val opcionesPanel: List<OpcionPanel> = emptyList()
)

// VIEWMODEL DE LA PANTALLA PERFIL
// Solo LEE datos que ya guardaron otras pantallas (Registro, Encuesta, Home/Favoritos):
// no valida ni guarda nada. Para cambiar los datos, el usuario va a "Editar datos" (Registro).
// Es AndroidViewModel porque necesita el Context (application) para abrir
// SharedPreferences, la carpeta de la foto y la base de datos Room.
class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    // Las 3 fuentes de datos
    private val almacenamiento = AlmacenamientoLocal(application)
    private val fotoRepository = FotoRepository(application)
    private val favoritoRepository = FavoritoRepository(
        MasterMartiniDatabase.getInstance(application).favoritoDao()
    )

    // Estado privado y mutable: solo este ViewModel lo cambia
    private val _uiState = MutableStateFlow(PerfilUiState())

    // Versión pública de solo lectura: PerfilScreen la observa con collectAsState()
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    // init se ejecuta una vez, al crearse el ViewModel.
    // Se queda ESCUCHANDO la cantidad de favoritos de Room: cada vez que se marca o quita
    // un corazón (en Home o Favoritos), Room emite el número nuevo y se copia al estado.
    // Se usa collect (como en DetalleViewModel) porque totalFavoritos va DENTRO de PerfilUiState.
    init {
        viewModelScope.launch {
            favoritoRepository.contarFavoritos().collect { total ->
                _uiState.update { it.copy(totalFavoritos = total) }
            }
        }
    }

    // Lee el perfil, la encuesta y la foto guardados.
    // La llama PerfilScreen con LaunchedEffect CADA VEZ que se abre la pantalla,
    // así al volver de "Editar datos" o de la encuesta se ven los datos nuevos.
    // copy() cambia solo estos campos y deja totalFavoritos como estaba.
    fun cargarPerfil() {
        _uiState.update {
            it.copy(
                nombre = almacenamiento.obtenerNombre(),
                rut = almacenamiento.obtenerRut(),
                correo = almacenamiento.obtenerCorreo(),
                telefono = almacenamiento.obtenerTelefono(),
                rubro = almacenamiento.obtenerRubro(),
                personasNegocio = textoPersonas(almacenamiento.obtenerPersonasNegocio()),
                // Los intereses se guardan como Set (sin orden fijo). sorted() los deja en
                // orden alfabético, para que no cambien de lugar cada vez que se abre la pantalla.
                intereses = almacenamiento.obtenerInteresesEncuesta().sorted(),
                nivel = almacenamiento.obtenerNivelEncuesta(),
                ubicacion = almacenamiento.obtenerUbicacionEncuesta(),
                respondioEncuesta = almacenamiento.existeEncuesta(),
                // obtenerRol() devuelve un Rol (Cliente, Ventas...); nombreVisible es su texto
                rol = almacenamiento.obtenerRol().nombreVisible,
                planPro = almacenamiento.tienePlanPro(),
                // POLIMORFISMO: se le pide al rol SUS opciones. No hay ningún if por rol:
                // Ventas devuelve 1, Administrador 2, Cliente e Instructor ninguna.
                opcionesPanel = almacenamiento.obtenerRol().opcionesPanel()
            )
        }

        // La foto se lee de un archivo (es lento): obtenerFoto() trabaja en Dispatchers.IO
        // y aquí, dentro de la corrutina, solo se espera el resultado.
        viewModelScope.launch {
            val fotoGuardada = fotoRepository.obtenerFoto()
            _uiState.update { it.copy(foto = fotoGuardada) }
        }
    }

    // Se llama al confirmar "Cerrar sesión": borra la sesión guardada (id, rol, Plan Pro y datos).
    // Después MainActivity vuelve al login. La encuesta y la foto no se borran.
    fun cerrarSesion() {
        almacenamiento.cerrarSesion()
    }

    // Convierte la respuesta guardada en el texto que se muestra en el Perfil.
    // "Prefiero no decirlo" se muestra como "No indicado" (más corto y neutro),
    // y si está vacío (perfil antiguo o sin responder), también.
    private fun textoPersonas(guardado: String): String {
        return if (guardado.isBlank() || guardado == "Prefiero no decirlo") {
            "No indicado"
        } else {
            guardado
        }
    }
}
