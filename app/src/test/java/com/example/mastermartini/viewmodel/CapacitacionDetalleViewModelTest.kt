package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.InscripcionRepository
import com.example.mastermartini.data.ServidorException
import com.example.mastermartini.model.Inscripcion
import com.example.mastermartini.model.Rol
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class CapacitacionDetalleViewModelTest : StringSpec({

    val app = aplicacionDePrueba()
    // Cursos reales del catálogo local: el 1 es gratis y el 4 es Plan Pro
    val cursoGratis = CapacitacionRepository().obtenerCapacitaciones().first { it.id == 1 }
    val cursoPro = CapacitacionRepository().obtenerCapacitaciones().first { it.id == 4 }
    val miInscripcion = Inscripcion(id = 50, usuarioId = 5, cursoId = 1, leccionesCompletadas = listOf("0-0", "0-1"))

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        // Los repositorios y AlmacenamientoLocal se crean dentro del ViewModel
        mockkConstructor(CapacitacionRepository::class, InscripcionRepository::class, AlmacenamientoLocal::class)
        coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(1) } returns cursoGratis
        coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(4) } returns cursoPro
        coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(99) } returns null
        every { anyConstructed<AlmacenamientoLocal>().obtenerIdUsuario() } returns 5L
        every { anyConstructed<AlmacenamientoLocal>().obtenerRol() } returns Rol.Cliente
        coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns Result.success(emptyList())
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ---------- Cargar el curso ----------

    "cargarCapacitacion muestra Cargando y luego el curso con su producto" {
        runTest {
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            vm.uiState.value.cargando.shouldBeTrue()
            advanceUntilIdle()

            val estado = vm.uiState.value
            estado.cargando.shouldBeFalse()
            estado.capacitacion shouldBe cursoGratis
            estado.producto?.nombre shouldBe "Chocolate Ariba"
            estado.modulosAbiertos shouldBe setOf(0)
            estado.estaInscrito.shouldBeFalse()
            estado.porcentaje shouldBe 0
        }
    }

    "un Cliente no puede editar ni eliminar el curso" {
        runTest {
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            advanceUntilIdle()
            vm.uiState.value.puedeEditar.shouldBeFalse()
            vm.uiState.value.puedeEliminar.shouldBeFalse()
        }
    }

    "un Administrador puede editar y eliminar el curso" {
        runTest {
            every { anyConstructed<AlmacenamientoLocal>().obtenerRol() } returns Rol.Administrador
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            advanceUntilIdle()
            vm.uiState.value.puedeEditar.shouldBeTrue()
            vm.uiState.value.puedeEliminar.shouldBeTrue()
        }
    }

    "un Instructor solo puede editar su propio curso y no eliminarlo" {
        runTest {
            every { anyConstructed<AlmacenamientoLocal>().obtenerRol() } returns Rol.Instructor
            coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(1) } returns
                cursoGratis.copy(instructorId = 5)
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            advanceUntilIdle()
            vm.uiState.value.puedeEditar.shouldBeTrue()
            vm.uiState.value.puedeEliminar.shouldBeFalse()
        }
    }

    "si el curso no existe no hay curso, producto ni permisos" {
        runTest {
            every { anyConstructed<AlmacenamientoLocal>().obtenerRol() } returns Rol.Administrador
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(99)
            advanceUntilIdle()
            val estado = vm.uiState.value
            estado.cargando.shouldBeFalse()
            estado.capacitacion.shouldBeNull()
            estado.producto.shouldBeNull()
            estado.puedeEditar.shouldBeFalse()
            estado.puedeEliminar.shouldBeFalse()
            estado.totalLecciones shouldBe 0
        }
    }

    "si el usuario está inscrito se muestra su avance" {
        runTest {
            coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns
                Result.success(listOf(miInscripcion.copy(cursoId = 3), miInscripcion))
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            advanceUntilIdle()

            val estado = vm.uiState.value
            estado.inscripcion shouldBe miInscripcion
            estado.totalLecciones shouldBe 9
            estado.leccionesCompletadas shouldBe 2
            estado.porcentaje shouldBe 22
            vm.estaVista(0, 1).shouldBeTrue()
            vm.estaVista(1, 0).shouldBeFalse()
        }
    }

    "sin sesión no se buscan inscripciones" {
        runTest {
            every { anyConstructed<AlmacenamientoLocal>().obtenerIdUsuario() } returns null
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            advanceUntilIdle()
            vm.uiState.value.inscripcion.shouldBeNull()
            coVerify(exactly = 0) { anyConstructed<InscripcionRepository>().misInscripciones(any()) }
        }
    }

    "si falla la consulta de inscripciones el curso se muestra igual" {
        runTest {
            coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns
                Result.failure(Exception("sin conexión"))
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            advanceUntilIdle()
            vm.uiState.value.capacitacion shouldBe cursoGratis
            vm.uiState.value.inscripcion.shouldBeNull()
        }
    }

    "cargar el mismo curso dos veces no lo vuelve a pedir" {
        runTest {
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            advanceUntilIdle()
            vm.cargarCapacitacion(1)
            advanceUntilIdle()
            coVerify(exactly = 1) { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(1) }
        }
    }

    "recargar actualiza el curso y conserva los módulos abiertos" {
        runTest {
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            advanceUntilIdle()
            vm.alternarModulo(2)
            coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(1) } returns
                cursoGratis.copy(titulo = "Título editado")

            vm.recargarCapacitacion(1)
            advanceUntilIdle()

            vm.uiState.value.capacitacion?.titulo shouldBe "Título editado"
            vm.uiState.value.modulosAbiertos shouldBe setOf(0, 2)
        }
    }

    "recargar un curso que ya no existe lo deja vacío" {
        runTest {
            val vm = CapacitacionDetalleViewModel(app)
            vm.recargarCapacitacion(99)
            advanceUntilIdle()
            vm.uiState.value.capacitacion.shouldBeNull()
            vm.uiState.value.producto.shouldBeNull()
        }
    }

    "alternarModulo abre y cierra un módulo" {
        val vm = CapacitacionDetalleViewModel(app)
        vm.alternarModulo(1)
        vm.uiState.value.modulosAbiertos shouldContain 1
        vm.alternarModulo(1)
        vm.uiState.value.modulosAbiertos.contains(1).shouldBeFalse()
    }

    // ---------- Eliminar el curso ----------

    "el diálogo de eliminar se abre y se cierra" {
        val vm = CapacitacionDetalleViewModel(app)
        vm.abrirConfirmacionEliminar()
        vm.uiState.value.mostrarConfirmacionEliminar.shouldBeTrue()
        vm.cerrarConfirmacionEliminar()
        vm.uiState.value.mostrarConfirmacionEliminar.shouldBeFalse()
    }

    "eliminar sin curso cargado no hace nada" {
        runTest {
            val vm = CapacitacionDetalleViewModel(app)
            vm.eliminarCurso {}
            advanceUntilIdle()
            coVerify(exactly = 0) { anyConstructed<CapacitacionRepository>().eliminarCapacitacion(any(), any()) }
        }
    }

    "eliminar sin sesión pide iniciar sesión" {
        runTest {
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            advanceUntilIdle()
            every { anyConstructed<AlmacenamientoLocal>().obtenerIdUsuario() } returns null
            vm.eliminarCurso {}
            vm.uiState.value.mensajeError shouldBe "Debes iniciar sesión como administrador"
        }
    }

    "eliminar con éxito avisa a la pantalla" {
        runTest {
            coEvery { anyConstructed<CapacitacionRepository>().eliminarCapacitacion(5, 1) } returns Result.success(Unit)
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            advanceUntilIdle()
            var eliminado = false

            vm.eliminarCurso { eliminado = true }
            vm.uiState.value.eliminando.shouldBeTrue()
            advanceUntilIdle()

            eliminado.shouldBeTrue()
            vm.uiState.value.eliminando.shouldBeFalse()
        }
    }

    "eliminar muestra un mensaje distinto según el error del servidor" {
        runTest {
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            advanceUntilIdle()

            val casos = listOf(
                ServidorException(403, null) to "Solo el administrador puede eliminar cursos",
                ServidorException(503, null) to "El servicio de usuarios no está disponible",
                ServidorException(404, "Curso no encontrado") to "Curso no encontrado",
                ServidorException(500, null) to "Error 500: verifica permisos o campos",
                Exception("No se pudo conectar con el servidor de cursos (9092)") to
                    "No se pudo conectar con el servidor de cursos (9092)",
                Exception() to "No se pudo eliminar el curso"
            )
            for ((error, mensaje) in casos) {
                coEvery { anyConstructed<CapacitacionRepository>().eliminarCapacitacion(any(), any()) } returns
                    Result.failure(error)
                vm.eliminarCurso {}
                advanceUntilIdle()
                vm.uiState.value.mensajeError shouldBe mensaje
                vm.uiState.value.eliminando.shouldBeFalse()
            }
        }
    }

    // ---------- Inscripción ----------

    "inscribirse con éxito deja al usuario inscrito" {
        runTest {
            val nueva = Inscripcion(id = 60, usuarioId = 5, cursoId = 4)
            coEvery { anyConstructed<InscripcionRepository>().inscribirse(5, 4) } returns Result.success(nueva)
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(4)
            advanceUntilIdle()

            vm.inscribirse()
            vm.uiState.value.procesando.shouldBeTrue()
            advanceUntilIdle()

            vm.uiState.value.procesando.shouldBeFalse()
            vm.uiState.value.inscripcion shouldBe nueva
            vm.uiState.value.estaInscrito.shouldBeTrue()
        }
    }

    "inscribirse sin curso o mientras procesa no hace nada" {
        runTest {
            val vm = CapacitacionDetalleViewModel(app)
            vm.inscribirse()
            coEvery { anyConstructed<InscripcionRepository>().inscribirse(any(), any()) } returns
                Result.success(Inscripcion(id = 60, cursoId = 4))
            vm.cargarCapacitacion(4)
            advanceUntilIdle()
            vm.inscribirse()
            vm.inscribirse()
            advanceUntilIdle()
            coVerify(exactly = 1) { anyConstructed<InscripcionRepository>().inscribirse(any(), any()) }
        }
    }

    "inscribirse sin sesión pide iniciar sesión" {
        runTest {
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(4)
            advanceUntilIdle()
            every { anyConstructed<AlmacenamientoLocal>().obtenerIdUsuario() } returns null
            vm.inscribirse()
            vm.uiState.value.mensajeErrorInscripcion shouldBe "Debes iniciar sesión para inscribirte"
        }
    }

    "sin Plan Pro (403) se explica que hay que pedirlo a ventas" {
        runTest {
            coEvery { anyConstructed<InscripcionRepository>().inscribirse(any(), any()) } returns
                Result.failure(ServidorException(403, "Requiere Plan Pro"))
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(4)
            advanceUntilIdle()
            vm.inscribirse()
            advanceUntilIdle()
            vm.uiState.value.mensajeErrorInscripcion shouldBe
                "Necesitas el Plan Pro para este curso. Pídele a nuestro equipo de ventas que lo active"
        }
    }

    "si ya estaba inscrito (409) se carga la inscripción existente" {
        runTest {
            val existente = Inscripcion(id = 61, usuarioId = 5, cursoId = 4, leccionesCompletadas = listOf("0-0"))
            coEvery { anyConstructed<InscripcionRepository>().inscribirse(any(), any()) } returns
                Result.failure(ServidorException(409, "Ya inscrito"))
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(4)
            advanceUntilIdle()
            coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns Result.success(listOf(existente))

            vm.inscribirse()
            advanceUntilIdle()

            vm.uiState.value.inscripcion shouldBe existente
            vm.uiState.value.mensajeErrorInscripcion.shouldBeNull()
        }
    }

    "otros errores al inscribirse muestran el mensaje del servidor o de conexión" {
        runTest {
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(4)
            advanceUntilIdle()

            val casos = listOf(
                ServidorException(500, "Falla interna") to "Falla interna",
                ServidorException(400, null) to "Error 400: verifica permisos o campos",
                Exception("No se pudo conectar con el servidor de inscripciones (9093)") to
                    "No se pudo conectar con el servidor de inscripciones (9093)",
                Exception() to "No se pudo completar la acción"
            )
            for ((error, mensaje) in casos) {
                coEvery { anyConstructed<InscripcionRepository>().inscribirse(any(), any()) } returns
                    Result.failure(error)
                vm.inscribirse()
                advanceUntilIdle()
                vm.uiState.value.mensajeErrorInscripcion shouldBe mensaje
            }
        }
    }

    // ---------- Lecciones y cancelar ----------

    "marcar una lección pendiente la envía como completada" {
        runTest {
            coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns Result.success(listOf(miInscripcion))
            val actualizada = miInscripcion.copy(leccionesCompletadas = listOf("0-0", "0-1", "1-0"))
            coEvery { anyConstructed<InscripcionRepository>().marcarLeccion(5, 50, 1, 0, true) } returns
                Result.success(actualizada)
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            advanceUntilIdle()

            vm.alternarLeccion(modulo = 1, leccion = 0)
            advanceUntilIdle()

            vm.uiState.value.inscripcion shouldBe actualizada
            vm.estaVista(1, 0).shouldBeTrue()
        }
    }

    "si falla marcar la lección se muestra el error" {
        runTest {
            coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns Result.success(listOf(miInscripcion))
            coEvery { anyConstructed<InscripcionRepository>().marcarLeccion(5, 50, 0, 0, false) } returns
                Result.failure(ServidorException(404, "Inscripción no encontrada"))
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            advanceUntilIdle()

            vm.alternarLeccion(modulo = 0, leccion = 0)
            advanceUntilIdle()

            vm.uiState.value.mensajeErrorInscripcion shouldBe "Inscripción no encontrada"
            vm.uiState.value.inscripcion shouldBe miInscripcion
        }
    }

    "sin inscripción no se pueden marcar lecciones ni cancelar" {
        runTest {
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            advanceUntilIdle()
            vm.alternarLeccion(0, 0)
            vm.cancelarInscripcion()
            advanceUntilIdle()
            coVerify(exactly = 0) { anyConstructed<InscripcionRepository>().marcarLeccion(any(), any(), any(), any(), any()) }
            coVerify(exactly = 0) { anyConstructed<InscripcionRepository>().cancelar(any(), any()) }
        }
    }

    "el diálogo de cancelar se abre y se cierra" {
        val vm = CapacitacionDetalleViewModel(app)
        vm.abrirConfirmacionCancelar()
        vm.uiState.value.mostrarConfirmacionCancelar.shouldBeTrue()
        vm.cerrarConfirmacionCancelar()
        vm.uiState.value.mostrarConfirmacionCancelar.shouldBeFalse()
    }

    "cancelar con éxito quita la inscripción" {
        runTest {
            coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns Result.success(listOf(miInscripcion))
            coEvery { anyConstructed<InscripcionRepository>().cancelar(5, 50) } returns Result.success(Unit)
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            advanceUntilIdle()

            vm.cancelarInscripcion()
            advanceUntilIdle()

            vm.uiState.value.inscripcion.shouldBeNull()
            vm.uiState.value.procesando.shouldBeFalse()
        }
    }

    "si falla cancelar se mantiene la inscripción y se muestra el error" {
        runTest {
            coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns Result.success(listOf(miInscripcion))
            coEvery { anyConstructed<InscripcionRepository>().cancelar(any(), any()) } returns
                Result.failure(Exception("No se pudo conectar con el servidor de inscripciones (9093)"))
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(1)
            advanceUntilIdle()

            vm.cancelarInscripcion()
            advanceUntilIdle()

            vm.uiState.value.inscripcion shouldBe miInscripcion
            vm.uiState.value.mensajeErrorInscripcion shouldBe "No se pudo conectar con el servidor de inscripciones (9093)"
        }
    }

    // ---------- Lecciones bloqueadas (freemium) ----------

    "en un curso Plan Pro sin inscripción solo la primera lección es muestra gratis" {
        runTest {
            val vm = CapacitacionDetalleViewModel(app)
            vm.cargarCapacitacion(4)
            advanceUntilIdle()
            vm.esMuestraGratis(0, 0).shouldBeTrue()
            vm.estaBloqueada(0, 0).shouldBeFalse()
            vm.esMuestraGratis(0, 1).shouldBeFalse()
            vm.estaBloqueada(0, 1).shouldBeTrue()
            vm.estaBloqueada(1, 0).shouldBeTrue()
        }
    }

    "en un curso gratis o con inscripción nada está bloqueado" {
        runTest {
            val vm = CapacitacionDetalleViewModel(app)
            vm.esMuestraGratis(0, 0).shouldBeFalse()
            vm.estaBloqueada(0, 1).shouldBeFalse()

            vm.cargarCapacitacion(1)
            advanceUntilIdle()
            vm.estaBloqueada(0, 1).shouldBeFalse()
            vm.esMuestraGratis(0, 0).shouldBeFalse()

            coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns
                Result.success(listOf(Inscripcion(id = 70, cursoId = 4)))
            vm.cargarCapacitacion(4)
            advanceUntilIdle()
            vm.estaBloqueada(0, 1).shouldBeFalse()
            vm.esMuestraGratis(0, 0).shouldBeFalse()
        }
    }
})
