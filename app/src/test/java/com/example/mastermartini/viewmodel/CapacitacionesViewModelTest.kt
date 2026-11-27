package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.InscripcionRepository
import com.example.mastermartini.data.MENSAJE_SIN_CONEXION
import com.example.mastermartini.data.ServidorException
import com.example.mastermartini.data.cursoDePrueba
import com.example.mastermartini.data.cursoGratisDePrueba
import com.example.mastermartini.data.cursoProDePrueba
import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.model.Inscripcion
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
class CapacitacionesViewModelTest : StringSpec({

    val app = aplicacionDePrueba()
    // Catálogo inventado tal como lo devolvería el servidor (en este orden)
    val catalogo = listOf(
        cursoGratisDePrueba,                                        // 1: Chocolatería, gratis
        cursoDePrueba(2, categoria = "Panadería"),                  // 2: Panadería, gratis
        cursoProDePrueba,                                           // 4: Panadería, Plan Pro
        cursoDePrueba(5, categoria = "Heladería", esGratis = false) // 5: Heladería, Plan Pro
    )

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        mockkConstructor(CapacitacionRepository::class, InscripcionRepository::class, AlmacenamientoLocal::class)
        coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionesRemotas() } returns Result.success(catalogo)
        every { anyConstructed<AlmacenamientoLocal>().obtenerIdUsuario() } returns 5L
        coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns Result.success(emptyList())
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // Devuelve los cursos que se ven en pantalla cuando el estado es Exito
    fun CapacitacionesViewModel.cursosMostrados(): List<Capacitacion> {
        val estado = uiState.value.estado
        estado.shouldBeInstanceOf<EstadoUi.Exito<List<Capacitacion>>>()
        return estado.datos
    }

    // ---------- Cargando, Contenido y vacío ----------

    "antes de responder el servidor se ve Cargando, sin cursos locales" {
        val estado = CapacitacionesViewModel(app).uiState.value
        estado.estado shouldBe EstadoUi.Cargando
        estado.filtroSeleccionado shouldBe FILTRO_TODOS
        estado.filtros shouldBe listOf("Todos", "Gratis", "Plan Pro")
        estado.precioPlanPro shouldBe "$9.990"
    }

    "al responder el servidor se muestran sus cursos" {
        runTest {
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()
            vm.cursosMostrados().map { it.id } shouldBe listOf(1, 2, 4, 5)
            vm.uiState.value.totalCursos shouldBe 4
        }
    }

    "si el servidor no tiene cursos el estado queda vacío y solo con los filtros fijos" {
        runTest {
            coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionesRemotas() } returns
                Result.success(emptyList())
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()
            vm.cursosMostrados().shouldBeEmpty()
            vm.uiState.value.totalCursos shouldBe 0
            vm.uiState.value.filtros shouldBe listOf("Todos", "Gratis", "Plan Pro")
        }
    }

    // ---------- Categorías del servidor ----------

    "las categorías de los filtros salen de los cursos del servidor, sin repetir y en orden" {
        runTest {
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()
            vm.uiState.value.filtros shouldBe listOf(
                "Todos", "Gratis", "Plan Pro", "Chocolatería", "Panadería", "Heladería"
            )
        }
    }

    "una categoría nueva en el servidor aparece sola y las vacías se ignoran" {
        runTest {
            coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionesRemotas() } returns
                Result.success(listOf(cursoDePrueba(7, categoria = "Cocina"), cursoDePrueba(8, categoria = " ")))
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()
            vm.uiState.value.filtros shouldBe listOf("Todos", "Gratis", "Plan Pro", "Cocina")
        }
    }

    "si la categoría elegida desaparece del servidor se vuelve a Todos" {
        runTest {
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()
            vm.seleccionarFiltro("Heladería")
            coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionesRemotas() } returns
                Result.success(listOf(cursoGratisDePrueba))

            vm.recargarCapacitaciones()
            advanceUntilIdle()

            vm.uiState.value.filtroSeleccionado shouldBe FILTRO_TODOS
            vm.cursosMostrados().map { it.id } shouldBe listOf(1)
        }
    }

    // ---------- Filtros ----------

    "los filtros Gratis, Plan Pro y categoría muestran los cursos correctos" {
        runTest {
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()

            vm.seleccionarFiltro(FILTRO_GRATIS)
            vm.cursosMostrados().map { it.id } shouldBe listOf(1, 2)
            vm.seleccionarFiltro(FILTRO_PLAN_PRO)
            vm.cursosMostrados().map { it.id } shouldBe listOf(4, 5)
            vm.seleccionarFiltro("Panadería")
            vm.cursosMostrados().map { it.id } shouldBe listOf(2, 4)
            vm.uiState.value.filtroSeleccionado shouldBe "Panadería"
        }
    }

    "no se puede filtrar mientras el catálogo no ha llegado" {
        val vm = CapacitacionesViewModel(app)
        vm.seleccionarFiltro(FILTRO_GRATIS)
        vm.uiState.value.estado shouldBe EstadoUi.Cargando
        vm.uiState.value.filtroSeleccionado shouldBe FILTRO_TODOS
    }

    "recargar conserva el filtro elegido sin volver a mostrar Cargando" {
        runTest {
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()
            vm.seleccionarFiltro(FILTRO_PLAN_PRO)
            vm.recargarCapacitaciones()
            vm.uiState.value.estado.shouldBeInstanceOf<EstadoUi.Exito<List<Capacitacion>>>()
            advanceUntilIdle()
            vm.uiState.value.filtroSeleccionado shouldBe FILTRO_PLAN_PRO
            vm.cursosMostrados().map { it.id } shouldBe listOf(4, 5)
        }
    }

    // ---------- Error y Reintentar ----------

    "sin conexión se muestra el error con el mensaje compartido" {
        runTest {
            coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionesRemotas() } returns
                Result.failure(Exception(MENSAJE_SIN_CONEXION))
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()
            vm.uiState.value.estado shouldBe EstadoUi.Error(MENSAJE_SIN_CONEXION)
            coVerify(exactly = 0) { anyConstructed<InscripcionRepository>().misInscripciones(any()) }
        }
    }

    "un error del servidor muestra su mensaje o uno genérico" {
        runTest {
            val casos = listOf(
                ServidorException(500, "Falla interna") to "Falla interna",
                ServidorException(503, null) to "Error 503: verifica permisos o campos",
                Exception() to MENSAJE_SIN_CONEXION
            )
            for ((error, mensaje) in casos) {
                coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionesRemotas() } returns
                    Result.failure(error)
                val vm = CapacitacionesViewModel(app)
                advanceUntilIdle()
                vm.uiState.value.estado shouldBe EstadoUi.Error(mensaje)
            }
        }
    }

    "Reintentar muestra Cargando y luego los cursos si el servidor ya responde" {
        runTest {
            coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionesRemotas() } returns
                Result.failure(Exception(MENSAJE_SIN_CONEXION))
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()
            coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionesRemotas() } returns
                Result.success(catalogo)

            vm.recargarCapacitaciones()
            vm.uiState.value.estado shouldBe EstadoUi.Cargando
            advanceUntilIdle()

            vm.cursosMostrados().map { it.id } shouldBe listOf(1, 2, 4, 5)
        }
    }

    // ---------- Mis cursos ----------

    "Mis cursos muestra el título y el avance de cada inscripción" {
        runTest {
            coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns Result.success(
                listOf(
                    // Sin título del servidor: se usa el del catálogo. El curso 1 tiene 9 lecciones
                    Inscripcion(id = 1, cursoId = 1, leccionesCompletadas = listOf("0-0", "0-1", "0-2")),
                    Inscripcion(id = 2, cursoId = 4, cursoTitulo = "Título del servidor"),
                    // Curso que ya no está en el catálogo
                    Inscripcion(id = 3, cursoId = 77)
                )
            )
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()

            vm.uiState.value.misCursos shouldBe listOf(
                MiCurso(cursoId = 1, titulo = cursoGratisDePrueba.titulo, porcentaje = 33),
                MiCurso(cursoId = 4, titulo = "Título del servidor", porcentaje = 0),
                MiCurso(cursoId = 77, titulo = "Curso Master Martini", porcentaje = 0)
            )
            vm.uiState.value.errorMisCursos.shouldBeNull()
        }
    }

    "sin sesión Mis cursos queda vacío y no se consulta el servidor" {
        runTest {
            every { anyConstructed<AlmacenamientoLocal>().obtenerIdUsuario() } returns null
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()
            vm.uiState.value.misCursos.shouldBeEmpty()
            vm.uiState.value.errorMisCursos.shouldBeNull()
            coVerify(exactly = 0) { anyConstructed<InscripcionRepository>().misInscripciones(any()) }
        }
    }

    "si fallan las inscripciones el catálogo se ve igual y Mis cursos muestra su error" {
        runTest {
            coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns
                Result.failure(ServidorException(500, null))
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()
            vm.cursosMostrados().map { it.id } shouldBe listOf(1, 2, 4, 5)
            vm.uiState.value.misCursos.shouldBeEmpty()
            vm.uiState.value.errorMisCursos shouldBe "Error 500: verifica permisos o campos"
        }
    }

    "Reintentar también vuelve a cargar Mis cursos" {
        runTest {
            coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns
                Result.failure(Exception(MENSAJE_SIN_CONEXION))
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()
            vm.uiState.value.errorMisCursos shouldBe MENSAJE_SIN_CONEXION
            coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns
                Result.success(listOf(Inscripcion(id = 9, cursoId = 4)))

            vm.recargarCapacitaciones()
            advanceUntilIdle()

            vm.uiState.value.errorMisCursos.shouldBeNull()
            vm.uiState.value.misCursos.map { it.cursoId } shouldBe listOf(4)
        }
    }
})
