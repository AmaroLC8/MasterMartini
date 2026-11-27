package com.example.mastermartini.viewmodel

import android.content.Context
import android.net.Uri
import com.example.mastermartini.data.MENSAJE_SIN_CONEXION
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.CloudinaryRepository
import com.example.mastermartini.data.ServidorException
import com.example.mastermartini.data.cursoProDePrueba
import com.example.mastermartini.model.Capacitacion
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class CrearCursoViewModelTest : StringSpec({

    val app = aplicacionDePrueba()
    val contexto = mockk<Context>()
    val video = mockk<Uri>()
    // Curso Plan Pro de CursosDePrueba.kt con id 2 (Panadería, Master Export Horneo)
    val cursoExistente = cursoProDePrueba.copy(id = 2)
    val noEncontrado = ServidorException(404, "Curso no encontrado")

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        mockkConstructor(AlmacenamientoLocal::class, CapacitacionRepository::class, CloudinaryRepository::class)
        every { anyConstructed<AlmacenamientoLocal>().obtenerIdUsuario() } returns 2L
        every { anyConstructed<AlmacenamientoLocal>().obtenerNombre() } returns "Chef Ana"
        coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(2) } returns Result.success(cursoExistente)
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // Deja el formulario con los campos obligatorios llenos
    fun CrearCursoViewModel.llenarObligatorios() {
        onTituloChange("Bombones rellenos")
        onDescripcionChange("Aprende a rellenar bombones")
    }

    "el formulario parte con valores por defecto y las listas de opciones" {
        val vm = CrearCursoViewModel(app)
        vm.uiState.value.categoria shouldBe "Chocolatería"
        vm.uiState.value.esEdicion.shouldBeFalse()
        vm.categorias.size shouldBe 5
        vm.niveles shouldBe listOf("Básico", "Intermedio", "Avanzado")
    }

    "cada campo del formulario actualiza el estado" {
        val vm = CrearCursoViewModel(app)
        vm.onTituloChange("Título")
        vm.onDescripcionChange("Descripción")
        vm.onCategoriaChange("Panadería")
        vm.onNivelChange("Avanzado")
        vm.onEsGratisChange(false)
        vm.onDuracionChange("1 h")
        vm.onInsumoChange("Bravo Crem")
        vm.onTituloModuloChange("Módulo A")
        vm.onTituloLeccionChange("Lección A")

        val estado = vm.uiState.value
        estado.titulo shouldBe "Título"
        estado.descripcion shouldBe "Descripción"
        estado.categoria shouldBe "Panadería"
        estado.nivel shouldBe "Avanzado"
        estado.esGratis.shouldBeFalse()
        estado.duracionTotal shouldBe "1 h"
        estado.insumoPrincipal shouldBe "Bravo Crem"
        estado.tituloModulo shouldBe "Módulo A"
        estado.tituloLeccion shouldBe "Lección A"
    }

    // ---------- Validaciones al publicar ----------

    "sin título ni descripción no se publica" {
        val vm = CrearCursoViewModel(app)
        vm.publicarCurso()
        vm.uiState.value.mensajeError shouldBe "Completa título, descripción e insumo principal"
    }

    "sin sesión pide iniciar sesión como instructor o administrador" {
        every { anyConstructed<AlmacenamientoLocal>().obtenerIdUsuario() } returns null
        val vm = CrearCursoViewModel(app)
        vm.llenarObligatorios()
        vm.publicarCurso()
        vm.uiState.value.mensajeError shouldBe "Debes iniciar sesión como instructor o administrador"
    }

    // ---------- Subir video a Cloudinary ----------

    "mientras se sube el video no se puede publicar y al terminar se guarda la URL" {
        runTest {
            coEvery { anyConstructed<CloudinaryRepository>().subirVideoDesdeUri(contexto, video) } coAnswers {
                delay(1_000)
                Result.success("https://res.cloudinary.com/video.mp4")
            }
            val vm = CrearCursoViewModel(app)
            vm.llenarObligatorios()

            vm.subirVideoCurso(contexto, video)
            runCurrent()
            vm.uiState.value.subiendoVideo.shouldBeTrue()
            vm.publicarCurso()
            vm.uiState.value.mensajeError shouldBe "Espera a que termine de subirse el video antes de guardar"

            advanceUntilIdle()
            vm.uiState.value.subiendoVideo.shouldBeFalse()
            vm.uiState.value.videoUrl shouldBe "https://res.cloudinary.com/video.mp4"
        }
    }

    "si falla la subida del video se muestra el error" {
        runTest {
            coEvery { anyConstructed<CloudinaryRepository>().subirVideoDesdeUri(any(), any()) } returns
                Result.failure(Exception("El video pesa más de 100 MB"))
            val vm = CrearCursoViewModel(app)
            vm.subirVideoCurso(contexto, video)
            advanceUntilIdle()
            vm.uiState.value.errorVideo shouldBe "El video pesa más de 100 MB"

            coEvery { anyConstructed<CloudinaryRepository>().subirVideoDesdeUri(any(), any()) } returns
                Result.failure(Exception())
            vm.subirVideoCurso(contexto, video)
            advanceUntilIdle()
            vm.uiState.value.errorVideo shouldBe
                "No se pudo subir el video. Revisa tu conexión e inténtalo nuevamente."
        }
    }

    "un enlace de video sin http se rechaza al publicar" {
        runTest {
            coEvery { anyConstructed<CloudinaryRepository>().subirVideoDesdeUri(any(), any()) } returns
                Result.success("res.cloudinary.com/video.mp4")
            val vm = CrearCursoViewModel(app)
            vm.llenarObligatorios()
            vm.subirVideoCurso(contexto, video)
            advanceUntilIdle()

            vm.publicarCurso()
            vm.uiState.value.mensajeError shouldBe "El enlace de Cloudinary debe empezar con http:// o https://"
        }
    }

    // ---------- Crear curso ----------

    "crear un curso envía los datos limpios y marca guardado exitoso" {
        runTest {
            val enviado = slot<Capacitacion>()
            coEvery { anyConstructed<CapacitacionRepository>().crearCapacitacion(2, capture(enviado)) } answers {
                Result.success(enviado.captured.copy(id = 30))
            }
            val vm = CrearCursoViewModel(app)
            vm.onTituloChange("  Bombones rellenos ")
            vm.onDescripcionChange("Aprende a rellenar bombones")
            vm.onDuracionChange("  ")
            vm.onTituloModuloChange("")
            vm.onTituloLeccionChange("")

            vm.publicarCurso()
            advanceUntilIdle()

            vm.uiState.value.guardadoExitoso.shouldBeTrue()
            vm.uiState.value.cargando.shouldBeFalse()
            val curso = enviado.captured
            curso.titulo shouldBe "Bombones rellenos"
            curso.instructor shouldBe "Chef Ana"
            curso.duracionTotal shouldBe "30 min"
            curso.videoResumenUrl.shouldBeNull()
            curso.modulos.first().titulo shouldBe "Módulo 1"
            curso.modulos.first().lecciones.first().titulo shouldBe "Lección 1"
            curso.loQueLograras shouldBe listOf("Dominar el uso de Chocolate Ariba")
        }
    }

    "crear muestra un mensaje distinto según el error del servidor" {
        runTest {
            val vm = CrearCursoViewModel(app)
            vm.llenarObligatorios()
            val casos = listOf(
                ServidorException(403, null) to "Solo instructores o administradores pueden crear cursos",
                ServidorException(503, null) to "El servicio de usuarios no está disponible",
                ServidorException(400, "El título es muy corto") to "El título es muy corto",
                Exception(MENSAJE_SIN_CONEXION) to
                    MENSAJE_SIN_CONEXION,
                Exception() to "No se pudo crear el curso"
            )
            for ((error, mensaje) in casos) {
                coEvery { anyConstructed<CapacitacionRepository>().crearCapacitacion(any(), any()) } returns
                    Result.failure(error)
                vm.publicarCurso()
                advanceUntilIdle()
                vm.uiState.value.mensajeError shouldBe mensaje
                vm.uiState.value.guardadoExitoso.shouldBeFalse()
            }
        }
    }

    // ---------- Editar curso ----------

    "cargarParaEditar llena el formulario con el curso del servidor" {
        runTest {
            val vm = CrearCursoViewModel(app)
            vm.cargarParaEditar(2)
            vm.uiState.value.cargandoCurso.shouldBeTrue()
            advanceUntilIdle()

            val estado = vm.uiState.value
            estado.esEdicion.shouldBeTrue()
            estado.cargandoCurso.shouldBeFalse()
            estado.titulo shouldBe cursoExistente.titulo
            estado.categoria shouldBe "Panadería"
            estado.insumoPrincipal shouldBe "Master Export Horneo"
            estado.videoUrl shouldBe (cursoExistente.videoResumenUrl ?: "")
        }
    }

    "cargar el mismo curso para editar dos veces no lo vuelve a pedir" {
        runTest {
            val vm = CrearCursoViewModel(app)
            vm.cargarParaEditar(2)
            advanceUntilIdle()
            vm.cargarParaEditar(2)
            advanceUntilIdle()
            coVerify(exactly = 1) { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(2) }
        }
    }

    "si el curso a editar ya no existe (404) se avisa sin ofrecer Reintentar" {
        runTest {
            coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(99) } returns Result.failure(noEncontrado)
            val vm = CrearCursoViewModel(app)
            vm.cargarParaEditar(99)
            advanceUntilIdle()
            vm.uiState.value.cursoNoExiste.shouldBeTrue()
            vm.uiState.value.errorCarga.shouldBeNull()
            vm.uiState.value.cargandoCurso.shouldBeFalse()
        }
    }

    "sin conexión al traer el curso a editar se muestra el error para reintentar" {
        runTest {
            coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(2) } returns
                Result.failure(Exception(MENSAJE_SIN_CONEXION))
            val vm = CrearCursoViewModel(app)
            vm.cargarParaEditar(2)
            advanceUntilIdle()
            vm.uiState.value.errorCarga shouldBe MENSAJE_SIN_CONEXION
            vm.uiState.value.cursoNoExiste.shouldBeFalse()
        }
    }

    "un error del servidor al traer el curso muestra su mensaje o uno genérico" {
        runTest {
            val vm = CrearCursoViewModel(app)
            vm.cargarParaEditar(2)
            val casos = listOf(
                ServidorException(500, "Falla interna") to "Falla interna",
                ServidorException(503, null) to "Error 503: verifica permisos o campos",
                Exception() to MENSAJE_SIN_CONEXION
            )
            for ((error, mensaje) in casos) {
                coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(2) } returns
                    Result.failure(error)
                vm.reintentarCarga()
                advanceUntilIdle()
                vm.uiState.value.errorCarga shouldBe mensaje
            }
        }
    }

    "Reintentar vuelve a pedir el curso y llena el formulario" {
        runTest {
            coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(2) } returns
                Result.failure(Exception(MENSAJE_SIN_CONEXION))
            val vm = CrearCursoViewModel(app)
            vm.cargarParaEditar(2)
            advanceUntilIdle()
            coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(2) } returns
                Result.success(cursoExistente)

            vm.reintentarCarga()
            vm.uiState.value.errorCarga.shouldBeNull()
            advanceUntilIdle()

            vm.uiState.value.titulo shouldBe cursoExistente.titulo
            vm.uiState.value.errorCarga.shouldBeNull()
        }
    }

    "Reintentar al crear un curso nuevo no hace nada" {
        runTest {
            val vm = CrearCursoViewModel(app)
            vm.reintentarCarga()
            advanceUntilIdle()
            coVerify(exactly = 0) { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(any()) }
        }
    }

    "guardar cambios conserva los módulos del curso original" {
        runTest {
            val enviado = slot<Capacitacion>()
            coEvery { anyConstructed<CapacitacionRepository>().actualizarCapacitacion(2, 2, capture(enviado)) } answers {
                Result.success(enviado.captured)
            }
            val vm = CrearCursoViewModel(app)
            vm.cargarParaEditar(2)
            advanceUntilIdle()
            vm.onTituloChange("Pan amasado mejorado")
            vm.onDuracionChange("")

            vm.publicarCurso()
            advanceUntilIdle()

            vm.uiState.value.guardadoExitoso.shouldBeTrue()
            enviado.captured.titulo shouldBe "Pan amasado mejorado"
            enviado.captured.duracionTotal shouldBe cursoExistente.duracionTotal
            enviado.captured.modulos shouldBe cursoExistente.modulos
            coVerify(exactly = 0) { anyConstructed<CapacitacionRepository>().crearCapacitacion(any(), any()) }
        }
    }

    "editar sin permiso (403) explica quién puede editar" {
        runTest {
            coEvery { anyConstructed<CapacitacionRepository>().actualizarCapacitacion(any(), any(), any()) } returns
                Result.failure(ServidorException(403, null))
            val vm = CrearCursoViewModel(app)
            vm.cargarParaEditar(2)
            advanceUntilIdle()
            vm.publicarCurso()
            advanceUntilIdle()
            vm.uiState.value.mensajeError shouldBe "Solo el instructor del curso o un administrador pueden editarlo"
        }
    }

    "si no se pudo cargar el curso original no se guarda la edición" {
        runTest {
            coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(99) } returns Result.failure(noEncontrado)
            val vm = CrearCursoViewModel(app)
            vm.cargarParaEditar(99)
            advanceUntilIdle()
            vm.llenarObligatorios()

            vm.publicarCurso()

            vm.uiState.value.mensajeError shouldBe "No se pudo cargar el curso. Vuelve atrás e inténtalo otra vez."
            coVerify(exactly = 0) { anyConstructed<CapacitacionRepository>().actualizarCapacitacion(any(), any(), any()) }
        }
    }
})
