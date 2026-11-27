package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.PreguntaRepository
import com.example.mastermartini.data.ServidorException
import com.example.mastermartini.model.Pregunta
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class PreguntasPendientesViewModelTest : StringSpec({

    val repo = mockk<PreguntaRepository>()
    val almacenamiento = mockk<AlmacenamientoLocal>()
    val primera = Pregunta(id = 1, cursoId = 2, cursoTitulo = "Pan de masa madre", autorNombre = "Alumno Uno", texto = "¿Cuánto reposa?")
    val segunda = Pregunta(id = 2, cursoId = 4, cursoTitulo = "Bombones", autorNombre = "Alumno Dos", texto = "¿Qué cacao uso?")

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        clearMocks(repo, almacenamiento)
        every { almacenamiento.obtenerIdUsuario() } returns 3L
        coEvery { repo.pendientes(3) } returns Result.success(listOf(primera, segunda))
    }

    afterTest {
        Dispatchers.resetMain()
    }

    // Devuelve las preguntas que se ven en pantalla cuando el estado es Exito
    fun PreguntasPendientesViewModel.preguntasMostradas(): List<Pregunta> {
        val estado = uiState.value.estado
        estado.shouldBeInstanceOf<EstadoUi.Exito<List<Pregunta>>>()
        return estado.datos
    }

    // ---------- Cargar ----------

    "parte en Cargando y luego muestra las preguntas pendientes" {
        runTest {
            val vm = PreguntasPendientesViewModel(repo, almacenamiento)
            vm.uiState.value.estado shouldBe EstadoUi.Cargando
            advanceUntilIdle()
            vm.preguntasMostradas() shouldBe listOf(primera, segunda)
        }
    }

    "sin preguntas pendientes la lista queda vacía" {
        runTest {
            coEvery { repo.pendientes(3) } returns Result.success(emptyList())
            val vm = PreguntasPendientesViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.preguntasMostradas().shouldBeEmpty()
        }
    }

    "sin sesión se muestra un error y no se llama al servidor" {
        runTest {
            every { almacenamiento.obtenerIdUsuario() } returns null
            val vm = PreguntasPendientesViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.uiState.value.estado shouldBe EstadoUi.Error("Debes iniciar sesión para ver las preguntas")
            coVerify(exactly = 0) { repo.pendientes(any()) }
        }
    }

    "si el servidor rechaza la carga se muestra su mensaje o uno genérico" {
        runTest {
            val casos = listOf(
                ServidorException(403, "No tienes permiso") to "No tienes permiso",
                ServidorException(500, null) to "Error 500: verifica permisos o campos",
                Exception("No se pudo conectar con el servidor de inscripciones (9093)") to
                    "No se pudo conectar con el servidor de inscripciones (9093)",
                Exception() to "No se pudo completar la acción"
            )
            for ((error, mensaje) in casos) {
                coEvery { repo.pendientes(3) } returns Result.failure(error)
                val vm = PreguntasPendientesViewModel(repo, almacenamiento)
                advanceUntilIdle()
                vm.uiState.value.estado shouldBe EstadoUi.Error(mensaje)
            }
        }
    }

    "reintentar después de un error vuelve a cargar la lista" {
        runTest {
            coEvery { repo.pendientes(3) } returns Result.failure(Exception("sin red"))
            val vm = PreguntasPendientesViewModel(repo, almacenamiento)
            advanceUntilIdle()
            coEvery { repo.pendientes(3) } returns Result.success(listOf(segunda))

            vm.cargar()
            vm.uiState.value.estado shouldBe EstadoUi.Cargando
            advanceUntilIdle()

            vm.preguntasMostradas() shouldBe listOf(segunda)
        }
    }

    // ---------- Responder ----------

    "escribir una respuesta la guarda y borra el error de esa pregunta" {
        runTest {
            val vm = PreguntasPendientesViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.responder(primera)
            vm.uiState.value.errores[1L] shouldBe "Escribe una respuesta antes de enviarla"

            vm.onRespuestaChange(1, "Unas 12 horas")
            vm.uiState.value.respuestas[1L] shouldBe "Unas 12 horas"
            vm.uiState.value.errores[1L].shouldBeNull()
        }
    }

    "responder con solo espacios no llama al servidor" {
        runTest {
            val vm = PreguntasPendientesViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.onRespuestaChange(1, "   ")
            vm.responder(primera)
            advanceUntilIdle()
            vm.uiState.value.errores[1L] shouldBe "Escribe una respuesta antes de enviarla"
            coVerify(exactly = 0) { repo.responder(any(), any(), any()) }
        }
    }

    "responder con éxito saca la pregunta de la lista y muestra un Snackbar" {
        runTest {
            coEvery { repo.responder(3, 1, "Unas 12 horas") } returns
                Result.success(primera.copy(respuesta = "Unas 12 horas"))
            val vm = PreguntasPendientesViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.onRespuestaChange(1, "  Unas 12 horas  ")

            vm.responder(primera)
            vm.uiState.value.enviando shouldBe setOf(1L)
            advanceUntilIdle()

            vm.preguntasMostradas() shouldBe listOf(segunda)
            vm.uiState.value.enviando.shouldBeEmpty()
            vm.uiState.value.respuestas.containsKey(1L).shouldBeFalse()
            vm.uiState.value.mensajeSnackbar shouldBe "Respuesta enviada"
        }
    }

    "responder dos veces seguidas solo envía una" {
        runTest {
            coEvery { repo.responder(any(), any(), any()) } returns Result.success(primera)
            val vm = PreguntasPendientesViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.onRespuestaChange(1, "Respuesta")
            vm.responder(primera)
            vm.responder(primera)
            advanceUntilIdle()
            coVerify(exactly = 1) { repo.responder(any(), any(), any()) }
        }
    }

    "si falla responder la pregunta se queda y se muestra el error" {
        runTest {
            coEvery { repo.responder(any(), any(), any()) } returns
                Result.failure(ServidorException(403, "No tienes permiso"))
            val vm = PreguntasPendientesViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.onRespuestaChange(1, "Respuesta")

            vm.responder(primera)
            advanceUntilIdle()

            vm.preguntasMostradas() shouldBe listOf(primera, segunda)
            vm.uiState.value.respuestas[1L] shouldBe "Respuesta"
            vm.uiState.value.enviando.shouldBeEmpty()
            vm.uiState.value.mensajeSnackbar shouldBe "No tienes permiso"
        }
    }

    "responder cuando la lista no está cargada mantiene el estado actual" {
        runTest {
            coEvery { repo.pendientes(3) } returns Result.failure(Exception("sin red"))
            coEvery { repo.responder(any(), any(), any()) } returns Result.success(primera)
            val vm = PreguntasPendientesViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.onRespuestaChange(1, "Respuesta")

            vm.responder(primera)
            advanceUntilIdle()

            vm.uiState.value.estado shouldBe EstadoUi.Error("sin red")
            vm.uiState.value.mensajeSnackbar shouldBe "Respuesta enviada"
        }
    }

    "responder sin sesión avisa con un Snackbar" {
        runTest {
            val vm = PreguntasPendientesViewModel(repo, almacenamiento)
            advanceUntilIdle()
            every { almacenamiento.obtenerIdUsuario() } returns null
            vm.onRespuestaChange(1, "Respuesta")
            vm.responder(primera)
            vm.uiState.value.mensajeSnackbar shouldBe "Debes iniciar sesión para responder"
            coVerify(exactly = 0) { repo.responder(any(), any(), any()) }
        }
    }

    "snackbarMostrado limpia el mensaje" {
        runTest {
            val vm = PreguntasPendientesViewModel(repo, almacenamiento)
            advanceUntilIdle()
            every { almacenamiento.obtenerIdUsuario() } returns null
            vm.onRespuestaChange(1, "Respuesta")
            vm.responder(primera)
            vm.snackbarMostrado()
            vm.uiState.value.mensajeSnackbar.shouldBeNull()
        }
    }

    "la Factory crea el ViewModel con sus dependencias" {
        PreguntasPendientesViewModel.Factory
            .create(PreguntasPendientesViewModel::class.java, extrasConAplicacion(aplicacionDePrueba()))
            .shouldBeInstanceOf<PreguntasPendientesViewModel>()
    }
})
