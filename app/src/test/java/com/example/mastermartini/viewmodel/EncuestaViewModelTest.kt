package com.example.mastermartini.viewmodel

import android.content.Context
import android.location.Location
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.UbicacionRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class EncuestaViewModelTest : StringSpec({

    val app = aplicacionDePrueba()
    val clienteGps = mockk<FusedLocationProviderClient>()
    val tarea = mockk<Task<Location>>()
    val santiago = mockk<Location> {
        every { latitude } returns -33.4489
        every { longitude } returns -70.6693
    }
    val avisoSinUbicacion = "No se pudo obtener tu ubicación. Revisa que el GPS esté activado."

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        clearMocks(clienteGps, tarea)
        mockkConstructor(AlmacenamientoLocal::class, UbicacionRepository::class)
        every { anyConstructed<AlmacenamientoLocal>().existeEncuesta() } returns false
        // El GPS de Google Play Services se simula para no necesitar un celular
        mockkStatic(LocationServices::class)
        every { LocationServices.getFusedLocationProviderClient(any<Context>()) } returns clienteGps
        every { clienteGps.getCurrentLocation(any<Int>(), any()) } returns tarea
        every { tarea.addOnFailureListener(any()) } returns tarea
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // Hace que el GPS responda con la ubicación indicada
    fun gpsResponde(ubicacion: Location?) {
        every { tarea.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Location?>>().onSuccess(ubicacion)
            tarea
        }
    }

    "sin encuesta guardada el formulario parte vacío" {
        val estado = EncuestaViewModel(app).uiState.value
        estado.interesesSeleccionados shouldBe emptySet()
        estado.nivelExperiencia shouldBe ""
    }

    "si ya respondió la encuesta se cargan sus respuestas" {
        every { anyConstructed<AlmacenamientoLocal>().existeEncuesta() } returns true
        every { anyConstructed<AlmacenamientoLocal>().obtenerInteresesEncuesta() } returns setOf("Pastelería Fina")
        every { anyConstructed<AlmacenamientoLocal>().obtenerNivelEncuesta() } returns "Intermedio / Taller"
        every { anyConstructed<AlmacenamientoLocal>().obtenerUbicacionEncuesta() } returns "Providencia"

        val estado = EncuestaViewModel(app).uiState.value
        estado.interesesSeleccionados shouldBe setOf("Pastelería Fina")
        estado.nivelExperiencia shouldBe "Intermedio / Taller"
        estado.ubicacionGps shouldBe "Providencia"
    }

    "tocar un interés lo marca y tocarlo otra vez lo desmarca" {
        val vm = EncuestaViewModel(app)
        vm.opcionesIntereses.size shouldBe 5
        vm.toggleInteres("Heladería Artesanal")
        vm.uiState.value.interesesSeleccionados shouldBe setOf("Heladería Artesanal")
        vm.toggleInteres("Heladería Artesanal")
        vm.uiState.value.interesesSeleccionados shouldBe emptySet()
    }

    "guardar sin intereses ni nivel muestra los dos errores y no guarda" {
        val vm = EncuestaViewModel(app)
        vm.guardarRespuesta().shouldBeFalse()
        vm.uiState.value.errorIntereses shouldBe "Debes seleccionar al menos un área de interés."
        vm.uiState.value.errorNivel shouldBe "Debes seleccionar tu nivel de experiencia."
        vm.uiState.value.mensajeExito.shouldBeNull()
        verify(exactly = 0) { anyConstructed<AlmacenamientoLocal>().guardarEncuesta(any(), any(), any()) }
    }

    "al elegir un interés y un nivel se borran los errores" {
        val vm = EncuestaViewModel(app)
        vm.guardarRespuesta()
        vm.toggleInteres("Pastelería Fina")
        vm.seleccionarNivel(vm.opcionesNivel.first())
        vm.uiState.value.errorIntereses.shouldBeNull()
        vm.uiState.value.errorNivel.shouldBeNull()
    }

    "guardar con datos completos guarda la encuesta y muestra el éxito" {
        val vm = EncuestaViewModel(app)
        vm.toggleInteres("Pastelería Fina")
        vm.seleccionarNivel("Básico / Principiante")

        vm.guardarRespuesta().shouldBeTrue()

        vm.uiState.value.mensajeExito shouldBe "Encuesta registrada con éxito"
        verify {
            anyConstructed<AlmacenamientoLocal>().guardarEncuesta(setOf("Pastelería Fina"), "Básico / Principiante", "")
        }
    }

    "sin permiso de ubicación se avisa que puede guardar igual" {
        val vm = EncuestaViewModel(app)
        vm.registrarRespuestaSinGps()
        vm.uiState.value.avisoGps shouldBe "Sin permiso de ubicación. Puedes guardar la encuesta igual."
    }

    // ---------- GPS ----------

    "con GPS y nombre del lugar se guardan la comuna y las coordenadas" {
        runTest {
            gpsResponde(santiago)
            coEvery { anyConstructed<UbicacionRepository>().obtenerNombreLugar(-33.4489, -70.6693) } returns
                "Santiago, Región Metropolitana"
            val vm = EncuestaViewModel(app)

            vm.obtenerUbicacion()
            vm.uiState.value.cargandoUbicacion.shouldBeTrue()
            advanceUntilIdle()

            val estado = vm.uiState.value
            estado.ubicacionGps shouldBe "Santiago, Región Metropolitana"
            estado.coordenadasGps shouldBe "Lat: -33.4489, Lon: -70.6693"
            estado.cargandoUbicacion.shouldBeFalse()
            estado.avisoGps.shouldBeNull()
        }
    }

    "si no se encuentra el nombre del lugar se guardan las coordenadas" {
        runTest {
            gpsResponde(santiago)
            coEvery { anyConstructed<UbicacionRepository>().obtenerNombreLugar(any(), any()) } returns null
            val vm = EncuestaViewModel(app)
            vm.obtenerUbicacion()
            advanceUntilIdle()

            vm.uiState.value.ubicacionGps shouldBe "Lat: -33.4489, Lon: -70.6693"
            vm.uiState.value.avisoGps shouldBe
                "No pudimos obtener el nombre del lugar; se guardaron las coordenadas."
        }
    }

    "si el GPS no entrega ubicación se muestra un aviso" {
        gpsResponde(null)
        val vm = EncuestaViewModel(app)
        vm.obtenerUbicacion()
        vm.uiState.value.avisoGps shouldBe avisoSinUbicacion
        vm.uiState.value.cargandoUbicacion.shouldBeFalse()
    }

    "si el GPS falla se muestra un aviso" {
        every { tarea.addOnSuccessListener(any()) } returns tarea
        every { tarea.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(Exception("GPS apagado"))
            tarea
        }
        val vm = EncuestaViewModel(app)
        vm.obtenerUbicacion()
        vm.uiState.value.avisoGps shouldBe avisoSinUbicacion
    }

    "si Android niega el permiso la app no se cae y muestra un aviso" {
        every { clienteGps.getCurrentLocation(any<Int>(), any()) } throws SecurityException("sin permiso")
        val vm = EncuestaViewModel(app)
        vm.obtenerUbicacion()
        vm.uiState.value.avisoGps shouldBe avisoSinUbicacion
    }
})
