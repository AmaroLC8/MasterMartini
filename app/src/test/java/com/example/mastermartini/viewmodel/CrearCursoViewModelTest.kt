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
import com.example.mastermartini.model.Leccion
import com.example.mastermartini.model.Modulo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
    // Curso Plan Pro de CursosDePrueba.kt con id 2: 2 módulos de 2 lecciones de 10 min (40 min)
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

    // Ids locales del primer módulo y su primera lección
    fun CrearCursoViewModel.idModulo(indice: Int = 0) = uiState.value.modulos[indice].idLocal
    fun CrearCursoViewModel.idLeccion(indiceModulo: Int = 0, indice: Int = 0) =
        uiState.value.modulos[indiceModulo].lecciones[indice].idLocal

    // Llena una lección con datos válidos
    fun CrearCursoViewModel.llenarLeccion(idModulo: Int, idLeccion: Int, titulo: String, minutos: String = "15 min") {
        onTituloLeccionChange(idModulo, idLeccion, titulo)
        onDescripcionLeccionChange(idModulo, idLeccion, "Paso a paso de $titulo")
        onDuracionLeccionChange(idModulo, idLeccion, minutos)
    }

    // Deja el formulario completo: datos generales y el primer módulo con su lección
    fun CrearCursoViewModel.llenarCursoValido() {
        onTituloChange("Bombones rellenos")
        onDescripcionChange("Aprende a rellenar bombones")
        onTituloModuloChange(idModulo(), "Templado")
        llenarLeccion(idModulo(), idLeccion(), "Templar en mármol")
    }

    // ---------- Estado inicial y datos generales ----------

    "el formulario parte con valores por defecto, un módulo con una lección y las listas de opciones" {
        val vm = CrearCursoViewModel(app)
        val estado = vm.uiState.value
        estado.categoria shouldBe "Chocolatería"
        estado.esEdicion.shouldBeFalse()
        estado.modulos shouldHaveSize 1
        estado.modulos[0].lecciones shouldHaveSize 1
        estado.modulos[0].expandido.shouldBeTrue()
        estado.modulos[0].idLocal shouldNotBe estado.modulos[0].lecciones[0].idLocal
        estado.duracionTotalTexto shouldBe "0 min"
        estado.errores.shouldBeEmpty()
        estado.cantidadErrores shouldBe 0
        estado.primerError.shouldBeNull()
        estado.hayVideoSubiendo.shouldBeFalse()
        vm.categorias shouldBe listOf("Chocolatería", "Pastelería", "Panadería", "Heladería", "Decoración")
        vm.niveles shouldBe listOf("Básico", "Intermedio", "Avanzado")
    }

    "cada campo general del formulario actualiza el estado" {
        val vm = CrearCursoViewModel(app)
        vm.onTituloChange("Título")
        vm.onDescripcionChange("Descripción")
        vm.onCategoriaChange("Panadería")
        vm.onNivelChange("Avanzado")
        vm.onEsGratisChange(false)
        vm.onInsumoChange("Bravo Crem")

        val estado = vm.uiState.value
        estado.titulo shouldBe "Título"
        estado.descripcion shouldBe "Descripción"
        estado.categoria shouldBe "Panadería"
        estado.nivel shouldBe "Avanzado"
        estado.esGratis.shouldBeFalse()
        estado.insumoPrincipal shouldBe "Bravo Crem"
    }

    // ---------- Agregar y editar módulos y lecciones ----------

    "agregar módulo lo pone al final con una lección vacía e ids nuevos" {
        val vm = CrearCursoViewModel(app)
        vm.agregarModulo()
        vm.agregarModulo()

        val modulos = vm.uiState.value.modulos
        modulos shouldHaveSize 3
        modulos.forEach { it.lecciones shouldHaveSize 1 }
        val ids = modulos.map { it.idLocal } + modulos.flatMap { m -> m.lecciones.map { it.idLocal } }
        ids.toSet() shouldHaveSize ids.size
    }

    "agregar lección la pone al final de su módulo y abre el módulo" {
        val vm = CrearCursoViewModel(app)
        vm.agregarModulo()
        val idSegundo = vm.idModulo(1)
        vm.alternarModulo(idSegundo)
        vm.uiState.value.modulos[1].expandido.shouldBeFalse()

        vm.agregarLeccion(idSegundo)

        vm.uiState.value.modulos[0].lecciones shouldHaveSize 1
        vm.uiState.value.modulos[1].lecciones shouldHaveSize 2
        vm.uiState.value.modulos[1].expandido.shouldBeTrue()
    }

    "editar título de módulo y campos de lección cambia solo el elegido" {
        val vm = CrearCursoViewModel(app)
        vm.agregarModulo()
        val idModulo = vm.idModulo(1)
        val idLeccion = vm.idLeccion(1)

        vm.onTituloModuloChange(idModulo, "Rellenos")
        vm.onTituloLeccionChange(idModulo, idLeccion, "Ganache de frambuesa")
        vm.onDescripcionLeccionChange(idModulo, idLeccion, "Emulsión con crema")
        vm.onDuracionLeccionChange(idModulo, idLeccion, "20 min")
        vm.onVideoLeccionChange(idModulo, idLeccion, "https://videos.example.com/ganache.mp4")

        val modulos = vm.uiState.value.modulos
        modulos[0].titulo shouldBe ""
        modulos[0].lecciones[0].titulo shouldBe ""
        modulos[1].titulo shouldBe "Rellenos"
        val leccion = modulos[1].lecciones[0]
        leccion.titulo shouldBe "Ganache de frambuesa"
        leccion.descripcion shouldBe "Emulsión con crema"
        leccion.duracion shouldBe "20 min"
        leccion.videoUrl shouldBe "https://videos.example.com/ganache.mp4"
    }

    "alternar un módulo lo cierra y lo vuelve a abrir" {
        val vm = CrearCursoViewModel(app)
        vm.alternarModulo(vm.idModulo())
        vm.uiState.value.modulos[0].expandido.shouldBeFalse()
        vm.alternarModulo(vm.idModulo())
        vm.uiState.value.modulos[0].expandido.shouldBeTrue()
    }

    // ---------- Eliminar con confirmación ----------

    "eliminar un módulo pide confirmación; cancelar lo deja y confirmar lo borra" {
        val vm = CrearCursoViewModel(app)
        vm.agregarModulo()
        val idPrimero = vm.idModulo(0)
        val idSegundo = vm.idModulo(1)

        vm.pedirEliminarModulo(idPrimero)
        vm.uiState.value.accionPendiente shouldBe AccionPendiente.EliminarModulo(idPrimero)
        vm.cancelarAccion()
        vm.uiState.value.accionPendiente.shouldBeNull()
        vm.uiState.value.modulos shouldHaveSize 2

        vm.pedirEliminarModulo(idPrimero)
        vm.confirmarAccion()
        vm.uiState.value.accionPendiente.shouldBeNull()
        vm.uiState.value.modulos.map { it.idLocal } shouldContainExactly listOf(idSegundo)
    }

    "eliminar una lección pide confirmación y borra solo esa lección" {
        val vm = CrearCursoViewModel(app)
        val idModulo = vm.idModulo()
        vm.agregarLeccion(idModulo)
        val idPrimera = vm.idLeccion(0, 0)
        val idSegunda = vm.idLeccion(0, 1)

        vm.pedirEliminarLeccion(idModulo, idPrimera)
        vm.uiState.value.accionPendiente shouldBe AccionPendiente.EliminarLeccion(idModulo, idPrimera)
        vm.confirmarAccion()

        vm.uiState.value.modulos[0].lecciones.map { it.idLocal } shouldContainExactly listOf(idSegunda)
    }

    "confirmar sin una acción pendiente no cambia nada" {
        val vm = CrearCursoViewModel(app)
        val antes = vm.uiState.value
        vm.confirmarAccion()
        vm.uiState.value shouldBe antes
    }

    // ---------- Mover (cambiar el orden) ----------

    "en un curso nuevo mover módulos cambia el orden sin aviso y no pasa de los bordes" {
        val vm = CrearCursoViewModel(app)
        vm.agregarModulo()
        vm.agregarModulo()
        val (a, b, c) = vm.uiState.value.modulos.map { it.idLocal }

        vm.moverModulo(c, haciaArriba = true)
        vm.uiState.value.accionPendiente.shouldBeNull()
        vm.uiState.value.modulos.map { it.idLocal } shouldContainExactly listOf(a, c, b)

        vm.moverModulo(a, haciaArriba = false)
        vm.uiState.value.modulos.map { it.idLocal } shouldContainExactly listOf(c, a, b)

        // El primero no sube y el último no baja
        vm.moverModulo(c, haciaArriba = true)
        vm.moverModulo(b, haciaArriba = false)
        vm.uiState.value.modulos.map { it.idLocal } shouldContainExactly listOf(c, a, b)
    }

    "mover lecciones cambia el orden solo dentro de su módulo" {
        val vm = CrearCursoViewModel(app)
        val idModulo = vm.idModulo()
        vm.agregarLeccion(idModulo)
        vm.agregarLeccion(idModulo)
        val (x, y, z) = vm.uiState.value.modulos[0].lecciones.map { it.idLocal }

        vm.moverLeccion(idModulo, x, haciaArriba = false)
        vm.uiState.value.modulos[0].lecciones.map { it.idLocal } shouldContainExactly listOf(y, x, z)

        vm.moverLeccion(idModulo, z, haciaArriba = true)
        vm.uiState.value.modulos[0].lecciones.map { it.idLocal } shouldContainExactly listOf(y, z, x)

        vm.moverLeccion(idModulo, y, haciaArriba = true)
        vm.uiState.value.modulos[0].lecciones.map { it.idLocal } shouldContainExactly listOf(y, z, x)
    }

    "al editar, reordenar pide el aviso una vez; cancelar no mueve y confirmar mueve" {
        runTest {
            val vm = CrearCursoViewModel(app)
            vm.cargarParaEditar(2)
            advanceUntilIdle()
            val (m1, m2) = vm.uiState.value.modulos.map { it.idLocal }

            vm.moverModulo(m2, haciaArriba = true)
            vm.uiState.value.accionPendiente shouldBe AccionPendiente.MoverModulo(m2, true)
            vm.cancelarAccion()
            vm.uiState.value.modulos.map { it.idLocal } shouldContainExactly listOf(m1, m2)
            vm.uiState.value.avisoOrdenAceptado.shouldBeFalse()

            vm.moverModulo(m2, haciaArriba = true)
            vm.confirmarAccion()
            vm.uiState.value.modulos.map { it.idLocal } shouldContainExactly listOf(m2, m1)
            vm.uiState.value.avisoOrdenAceptado.shouldBeTrue()

            // Ya aceptado: el siguiente movimiento se hace sin volver a preguntar
            vm.moverModulo(m2, haciaArriba = false)
            vm.uiState.value.accionPendiente.shouldBeNull()
            vm.uiState.value.modulos.map { it.idLocal } shouldContainExactly listOf(m1, m2)
        }
    }

    "al editar, mover una lección también pide el aviso la primera vez" {
        runTest {
            val vm = CrearCursoViewModel(app)
            vm.cargarParaEditar(2)
            advanceUntilIdle()
            val idModulo = vm.idModulo()
            val (l1, l2) = vm.uiState.value.modulos[0].lecciones.map { it.idLocal }

            vm.moverLeccion(idModulo, l1, haciaArriba = false)
            vm.uiState.value.accionPendiente shouldBe AccionPendiente.MoverLeccion(idModulo, l1, false)
            vm.confirmarAccion()

            vm.uiState.value.modulos[0].lecciones.map { it.idLocal } shouldContainExactly listOf(l2, l1)
            vm.uiState.value.avisoOrdenAceptado.shouldBeTrue()
            AVISO_AVANCE_ALUMNOS shouldBe "Los alumnos inscritos podrían perder el avance marcado en este curso."
        }
    }

    "al editar, agregar al final no muestra aviso" {
        runTest {
            val vm = CrearCursoViewModel(app)
            vm.cargarParaEditar(2)
            advanceUntilIdle()
            vm.agregarModulo()
            vm.agregarLeccion(vm.idModulo())
            vm.uiState.value.accionPendiente.shouldBeNull()
            vm.uiState.value.modulos shouldHaveSize 3
            vm.uiState.value.modulos[0].lecciones shouldHaveSize 3
        }
    }

    // ---------- Validaciones ----------

    "un formulario vacío no se publica y marca cada error en el orden de la pantalla" {
        val vm = CrearCursoViewModel(app)
        vm.onInsumoChange(" ")
        val idModulo = vm.idModulo()
        val idLeccion = vm.idLeccion()

        vm.publicarCurso()

        val estado = vm.uiState.value
        estado.mostrarErrores.shouldBeTrue()
        estado.errores.keys.toList() shouldContainExactly listOf(
            ClaveError.TITULO,
            ClaveError.DESCRIPCION,
            ClaveError.INSUMO,
            ClaveError.tituloModulo(idModulo),
            ClaveError.tituloLeccion(idLeccion),
            ClaveError.descripcionLeccion(idLeccion),
            ClaveError.duracionLeccion(idLeccion)
        )
        estado.cantidadErrores shouldBe 7
        estado.primerError shouldBe ClaveError.TITULO
        estado.pedidoIrAlError shouldBe 1
        estado.errores[ClaveError.TITULO] shouldBe "Escribe el título del curso"
        estado.errores[ClaveError.duracionLeccion(idLeccion)] shouldBe "Escribe los minutos, por ejemplo: 15 min"
        coVerify(exactly = 0) { anyConstructed<CapacitacionRepository>().crearCapacitacion(any(), any()) }
    }

    "cada intento de guardar con errores vuelve a pedir ir al primero" {
        val vm = CrearCursoViewModel(app)
        vm.publicarCurso()
        vm.publicarCurso()
        vm.uiState.value.pedidoIrAlError shouldBe 2
    }

    "antes de guardar no se muestran errores; después se recalculan con cada cambio" {
        val vm = CrearCursoViewModel(app)
        vm.onTituloChange("")
        vm.uiState.value.errores.shouldBeEmpty()

        vm.publicarCurso()
        vm.uiState.value.errores shouldContainKey ClaveError.TITULO

        vm.onTituloChange("Trufas de chocolate")
        vm.uiState.value.errores shouldNotContainKey ClaveError.TITULO
        vm.uiState.value.primerError shouldBe ClaveError.DESCRIPCION
    }

    "sin módulos, o con un módulo sin lecciones, se pide agregarlos" {
        val vm = CrearCursoViewModel(app)
        vm.llenarCursoValido()
        val idModulo = vm.idModulo()

        vm.pedirEliminarLeccion(idModulo, vm.idLeccion())
        vm.confirmarAccion()
        vm.publicarCurso()
        vm.uiState.value.errores[ClaveError.leccionesModulo(idModulo)] shouldBe "Agrega al menos una lección a este módulo"

        vm.pedirEliminarModulo(idModulo)
        vm.confirmarAccion()
        vm.uiState.value.errores.keys.toList() shouldContainExactly listOf(ClaveError.MODULOS)
        vm.uiState.value.errores[ClaveError.MODULOS] shouldBe "Agrega al menos un módulo"
    }

    "la duración de la lección debe ser en minutos y mayor que cero" {
        val vm = CrearCursoViewModel(app)
        vm.llenarCursoValido()
        val clave = ClaveError.duracionLeccion(vm.idLeccion())

        for (invalida in listOf("1 h", "0 min", "quince", "", "15 minutos")) {
            vm.onDuracionLeccionChange(vm.idModulo(), vm.idLeccion(), invalida)
            vm.publicarCurso()
            vm.uiState.value.errores shouldContainKey clave
        }
        for (valida in listOf("15", "15 min", " 20 MIN ", "90min")) {
            vm.onDuracionLeccionChange(vm.idModulo(), vm.idLeccion(), valida)
            vm.uiState.value.errores shouldNotContainKey clave
        }
    }

    "un enlace de video de lección sin http se marca como error" {
        val vm = CrearCursoViewModel(app)
        vm.llenarCursoValido()
        val clave = ClaveError.videoLeccion(vm.idLeccion())

        vm.onVideoLeccionChange(vm.idModulo(), vm.idLeccion(), "videos.example.com/templado.mp4")
        vm.publicarCurso()
        vm.uiState.value.errores[clave] shouldBe "El enlace del video debe empezar con http:// o https://"

        vm.onVideoLeccionChange(vm.idModulo(), vm.idLeccion(), "http://videos.example.com/templado.mp4")
        vm.uiState.value.errores.shouldBeEmpty()
    }

    "una categoría o un nivel que no son de la lista se marcan como error" {
        val vm = CrearCursoViewModel(app)
        vm.llenarCursoValido()
        vm.onCategoriaChange("Cocina")
        vm.onNivelChange("Experto")
        vm.publicarCurso()
        vm.uiState.value.errores.keys.toList() shouldContainExactly listOf(ClaveError.CATEGORIA, ClaveError.NIVEL)
    }

    "al guardar con errores se abren solo los módulos que tienen errores" {
        val vm = CrearCursoViewModel(app)
        vm.llenarCursoValido()
        vm.agregarModulo()
        val idPrimero = vm.idModulo(0)
        val idSegundo = vm.idModulo(1)
        vm.alternarModulo(idPrimero)
        vm.alternarModulo(idSegundo)

        vm.publicarCurso()

        val modulos = vm.uiState.value.modulos
        modulos[0].expandido.shouldBeFalse()
        modulos[1].expandido.shouldBeTrue()
        vm.uiState.value.primerError shouldBe ClaveError.tituloModulo(idSegundo)
    }

    // ---------- Duración total ----------

    "la duración total suma los minutos de todas las lecciones" {
        val vm = CrearCursoViewModel(app)
        vm.llenarLeccion(vm.idModulo(), vm.idLeccion(), "Templado", "45 min")
        vm.uiState.value.duracionTotalTexto shouldBe "45 min"

        vm.agregarLeccion(vm.idModulo())
        vm.llenarLeccion(vm.idModulo(), vm.idLeccion(0, 1), "Moldeado", "75")
        vm.uiState.value.minutosTotales shouldBe 120
        vm.uiState.value.duracionTotalTexto shouldBe "2 h"

        vm.agregarModulo()
        vm.llenarLeccion(vm.idModulo(1), vm.idLeccion(1, 0), "Decorado", "80 min")
        vm.uiState.value.duracionTotalTexto shouldBe "3 h 20 min"

        // Una duración inválida no suma minutos
        vm.onDuracionLeccionChange(vm.idModulo(1), vm.idLeccion(1, 0), "1 h 20")
        vm.uiState.value.duracionTotalTexto shouldBe "2 h"
    }

    "las ayudas de duración y orden funcionan en los bordes" {
        formatearMinutos(0) shouldBe "0 min"
        formatearMinutos(59) shouldBe "59 min"
        formatearMinutos(60) shouldBe "1 h"
        formatearMinutos(65) shouldBe "1 h 5 min"
        normalizarDuracion(" 15 MIN ") shouldBe "15 min"
        normalizarDuracion("1 h") shouldBe ""
        duracionEsValida("000").shouldBeFalse()
        moverEnLista(listOf(1, 2, 3), indice = -1, haciaArriba = true) shouldBe listOf(1, 2, 3)
        moverEnLista(listOf(1, 2, 3), indice = 1, haciaArriba = true) shouldBe listOf(2, 1, 3)
    }

    // ---------- Subir video por lección ----------

    "subir el video de una lección muestra su progreso, bloquea guardar y luego guarda la URL" {
        runTest {
            coEvery { anyConstructed<CloudinaryRepository>().subirVideoDesdeUri(contexto, video) } coAnswers {
                delay(1_000)
                Result.success("https://res.cloudinary.com/demo/templado.mp4")
            }
            val vm = CrearCursoViewModel(app)
            vm.llenarCursoValido()
            vm.agregarLeccion(vm.idModulo())
            val idLeccion = vm.idLeccion(0, 0)

            vm.subirVideoLeccion(contexto, video, vm.idModulo(), idLeccion)
            runCurrent()
            vm.uiState.value.modulos[0].lecciones[0].subiendoVideo.shouldBeTrue()
            vm.uiState.value.modulos[0].lecciones[1].subiendoVideo.shouldBeFalse()
            vm.uiState.value.hayVideoSubiendo.shouldBeTrue()
            vm.publicarCurso()
            vm.uiState.value.mensajeError shouldBe "Espera a que termine de subirse el video antes de guardar"

            advanceUntilIdle()
            val leccion = vm.uiState.value.modulos[0].lecciones[0]
            leccion.subiendoVideo.shouldBeFalse()
            leccion.videoUrl shouldBe "https://res.cloudinary.com/demo/templado.mp4"
            leccion.errorSubida.shouldBeNull()
            vm.uiState.value.hayVideoSubiendo.shouldBeFalse()
        }
    }

    "si falla la subida del video de una lección se muestra el error en esa lección" {
        runTest {
            coEvery { anyConstructed<CloudinaryRepository>().subirVideoDesdeUri(any(), any()) } returns
                Result.failure(Exception("El video pesa más de 100 MB"))
            val vm = CrearCursoViewModel(app)
            vm.subirVideoLeccion(contexto, video, vm.idModulo(), vm.idLeccion())
            advanceUntilIdle()
            vm.uiState.value.modulos[0].lecciones[0].errorSubida shouldBe "El video pesa más de 100 MB"
            vm.uiState.value.modulos[0].lecciones[0].subiendoVideo.shouldBeFalse()

            coEvery { anyConstructed<CloudinaryRepository>().subirVideoDesdeUri(any(), any()) } returns
                Result.failure(Exception())
            vm.subirVideoLeccion(contexto, video, vm.idModulo(), vm.idLeccion())
            advanceUntilIdle()
            vm.uiState.value.modulos[0].lecciones[0].errorSubida shouldBe MENSAJE_ERROR_SUBIDA

            // Pegar un link borra el error de la subida
            vm.onVideoLeccionChange(vm.idModulo(), vm.idLeccion(), "https://videos.example.com/a.mp4")
            vm.uiState.value.modulos[0].lecciones[0].errorSubida.shouldBeNull()
        }
    }

    "si la lección se elimina mientras sube su video, el resultado se ignora" {
        runTest {
            coEvery { anyConstructed<CloudinaryRepository>().subirVideoDesdeUri(any(), any()) } coAnswers {
                delay(1_000)
                Result.success("https://res.cloudinary.com/demo/borrado.mp4")
            }
            val vm = CrearCursoViewModel(app)
            val idModulo = vm.idModulo()
            val idLeccion = vm.idLeccion()
            vm.subirVideoLeccion(contexto, video, idModulo, idLeccion)
            runCurrent()
            vm.pedirEliminarLeccion(idModulo, idLeccion)
            vm.confirmarAccion()

            advanceUntilIdle()
            vm.uiState.value.modulos[0].lecciones.shouldBeEmpty()
        }
    }

    // ---------- Video de presentación del curso ----------

    "mientras se sube el video del curso no se puede publicar y al terminar se guarda la URL" {
        runTest {
            coEvery { anyConstructed<CloudinaryRepository>().subirVideoDesdeUri(contexto, video) } coAnswers {
                delay(1_000)
                Result.success("https://res.cloudinary.com/video.mp4")
            }
            val vm = CrearCursoViewModel(app)
            vm.llenarCursoValido()

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

    "si falla la subida del video del curso se muestra el error" {
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
            vm.uiState.value.errorVideo shouldBe MENSAJE_ERROR_SUBIDA
        }
    }

    "un enlace del video del curso sin http se rechaza al publicar" {
        runTest {
            coEvery { anyConstructed<CloudinaryRepository>().subirVideoDesdeUri(any(), any()) } returns
                Result.success("res.cloudinary.com/video.mp4")
            val vm = CrearCursoViewModel(app)
            vm.llenarCursoValido()
            vm.subirVideoCurso(contexto, video)
            advanceUntilIdle()

            vm.publicarCurso()
            vm.uiState.value.mensajeError shouldBe "El enlace de Cloudinary debe empezar con http:// o https://"
        }
    }

    // ---------- Crear curso (POST) ----------

    "sin sesión pide iniciar sesión como instructor o administrador" {
        every { anyConstructed<AlmacenamientoLocal>().obtenerIdUsuario() } returns null
        val vm = CrearCursoViewModel(app)
        vm.llenarCursoValido()
        vm.publicarCurso()
        vm.uiState.value.mensajeError shouldBe "Debes iniciar sesión como instructor o administrador"
    }

    "crear un curso envía todos los módulos y lecciones limpios en el cuerpo" {
        runTest {
            val enviado = slot<Capacitacion>()
            coEvery { anyConstructed<CapacitacionRepository>().crearCapacitacion(2, capture(enviado)) } answers {
                Result.success(enviado.captured.copy(id = 30))
            }
            val vm = CrearCursoViewModel(app)
            vm.onTituloChange("  Bombones rellenos ")
            vm.onDescripcionChange("Aprende a rellenar bombones")
            vm.onCategoriaChange("Decoración")
            vm.onNivelChange("Intermedio")
            vm.onTituloModuloChange(vm.idModulo(), "  Templado ")
            vm.llenarLeccion(vm.idModulo(), vm.idLeccion(), "Templar en mármol", "20")
            vm.onTituloLeccionChange(vm.idModulo(), vm.idLeccion(), " Templar en mármol ")
            vm.onVideoLeccionChange(vm.idModulo(), vm.idLeccion(), " https://videos.example.com/templado.mp4 ")
            vm.agregarModulo()
            vm.onTituloModuloChange(vm.idModulo(1), "Rellenos")
            vm.llenarLeccion(vm.idModulo(1), vm.idLeccion(1, 0), "Ganache", "40 MIN")

            vm.publicarCurso()
            advanceUntilIdle()

            vm.uiState.value.guardadoExitoso.shouldBeTrue()
            vm.uiState.value.cargando.shouldBeFalse()
            vm.uiState.value.errores.shouldBeEmpty()
            val curso = enviado.captured
            curso.titulo shouldBe "Bombones rellenos"
            curso.categoria shouldBe "Decoración"
            curso.nivel shouldBe "Intermedio"
            curso.instructor shouldBe "Chef Ana"
            curso.duracionTotal shouldBe "1 h"
            curso.videoResumenUrl.shouldBeNull()
            curso.loQueLograras shouldBe listOf("Dominar el uso de Chocolate Ariba")
            curso.modulos shouldBe listOf(
                Modulo(
                    titulo = "Templado",
                    lecciones = listOf(
                        Leccion(
                            titulo = "Templar en mármol",
                            descripcion = "Paso a paso de Templar en mármol",
                            duracion = "20 min",
                            videoUrl = "https://videos.example.com/templado.mp4"
                        )
                    )
                ),
                Modulo(
                    titulo = "Rellenos",
                    lecciones = listOf(
                        Leccion(titulo = "Ganache", descripcion = "Paso a paso de Ganache", duracion = "40 min", videoUrl = null)
                    )
                )
            )
        }
    }

    "crear muestra un mensaje distinto según el error del servidor" {
        runTest {
            val vm = CrearCursoViewModel(app)
            vm.llenarCursoValido()
            val casos = listOf(
                ServidorException(400, "El título de la lección es obligatorio") to "El título de la lección es obligatorio",
                ServidorException(400, null) to "El servidor rechazó los datos del curso. Revísalos e inténtalo otra vez.",
                ServidorException(403, null) to "Solo instructores o administradores pueden crear cursos",
                ServidorException(503, null) to "El servicio de usuarios no está disponible",
                ServidorException(500, "Falla interna") to "Falla interna",
                Exception(MENSAJE_SIN_CONEXION) to MENSAJE_SIN_CONEXION,
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

    // ---------- Editar curso (PUT) ----------

    "cargarParaEditar llena el formulario con el curso y sus módulos reales" {
        runTest {
            val conVideo = cursoExistente.copy(
                modulos = listOf(
                    Modulo(
                        titulo = "Masa madre",
                        lecciones = listOf(
                            Leccion("Alimentar la masa", "Harina y agua", "25 min", "https://videos.example.com/masa.mp4"),
                            Leccion("Primer pan", "Formado y horneado", "35 min")
                        )
                    )
                )
            )
            coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(2) } returns Result.success(conVideo)
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
            estado.videoUrl shouldBe ""
            estado.modulos shouldHaveSize 1
            val modulo = estado.modulos[0]
            modulo.titulo shouldBe "Masa madre"
            modulo.expandido.shouldBeFalse()
            modulo.lecciones.map { it.titulo } shouldContainExactly listOf("Alimentar la masa", "Primer pan")
            modulo.lecciones[0].videoUrl shouldBe "https://videos.example.com/masa.mp4"
            modulo.lecciones[1].videoUrl shouldBe ""
            estado.duracionTotalTexto shouldBe "1 h"
            estado.errores.shouldBeEmpty()
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

    "guardar sin tocar los módulos envía los mismos módulos y la duración calculada" {
        runTest {
            val enviado = slot<Capacitacion>()
            coEvery { anyConstructed<CapacitacionRepository>().actualizarCapacitacion(2, 2, capture(enviado)) } answers {
                Result.success(enviado.captured)
            }
            val vm = CrearCursoViewModel(app)
            vm.cargarParaEditar(2)
            advanceUntilIdle()
            vm.onTituloChange("Pan amasado mejorado")

            vm.publicarCurso()
            advanceUntilIdle()

            vm.uiState.value.guardadoExitoso.shouldBeTrue()
            enviado.captured.titulo shouldBe "Pan amasado mejorado"
            enviado.captured.duracionTotal shouldBe "40 min"
            enviado.captured.modulos shouldBe cursoExistente.modulos
            enviado.captured.instructorId shouldBe cursoExistente.instructorId
            coVerify(exactly = 0) { anyConstructed<CapacitacionRepository>().crearCapacitacion(any(), any()) }
        }
    }

    "guardar cambios envía los módulos editados, reordenados y agregados" {
        runTest {
            val enviado = slot<Capacitacion>()
            coEvery { anyConstructed<CapacitacionRepository>().actualizarCapacitacion(2, 2, capture(enviado)) } answers {
                Result.success(enviado.captured)
            }
            val vm = CrearCursoViewModel(app)
            vm.cargarParaEditar(2)
            advanceUntilIdle()
            val (m1, m2) = vm.uiState.value.modulos.map { it.idLocal }

            // Borra la segunda lección del módulo 1, sube el módulo 2 y agrega un módulo nuevo
            vm.pedirEliminarLeccion(m1, vm.idLeccion(0, 1))
            vm.confirmarAccion()
            vm.moverModulo(m2, haciaArriba = true)
            vm.confirmarAccion()
            vm.agregarModulo()
            vm.onTituloModuloChange(vm.idModulo(2), "Decoración final")
            vm.llenarLeccion(vm.idModulo(2), vm.idLeccion(2, 0), "Glaseado", "30 min")

            vm.publicarCurso()
            advanceUntilIdle()

            vm.uiState.value.guardadoExitoso.shouldBeTrue()
            val modulos = enviado.captured.modulos
            modulos.map { it.titulo } shouldContainExactly listOf("Módulo 2", "Módulo 1", "Decoración final")
            modulos[1].lecciones.map { it.titulo } shouldContainExactly listOf("Lección 1")
            modulos[2].lecciones[0].duracion shouldBe "30 min"
            enviado.captured.duracionTotal shouldBe "1 h"
        }
    }

    "editar sin permiso (403) explica quién puede editar y un 400 muestra el mensaje del servidor" {
        runTest {
            coEvery { anyConstructed<CapacitacionRepository>().actualizarCapacitacion(any(), any(), any()) } returns
                Result.failure(ServidorException(403, null))
            val vm = CrearCursoViewModel(app)
            vm.cargarParaEditar(2)
            advanceUntilIdle()
            vm.publicarCurso()
            advanceUntilIdle()
            vm.uiState.value.mensajeError shouldBe "Solo el instructor del curso o un administrador pueden editarlo"

            coEvery { anyConstructed<CapacitacionRepository>().actualizarCapacitacion(any(), any(), any()) } returns
                Result.failure(ServidorException(400, "Cada módulo debe tener al menos 1 lección"))
            vm.publicarCurso()
            advanceUntilIdle()
            vm.uiState.value.mensajeError shouldBe "Cada módulo debe tener al menos 1 lección"
        }
    }

    "si no se pudo cargar el curso original no se guarda la edición" {
        runTest {
            coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionPorIdRemoto(99) } returns Result.failure(noEncontrado)
            val vm = CrearCursoViewModel(app)
            vm.cargarParaEditar(99)
            advanceUntilIdle()
            vm.llenarCursoValido()

            vm.publicarCurso()

            vm.uiState.value.mensajeError shouldBe "No se pudo cargar el curso. Vuelve atrás e inténtalo otra vez."
            coVerify(exactly = 0) { anyConstructed<CapacitacionRepository>().actualizarCapacitacion(any(), any(), any()) }
        }
    }
})
