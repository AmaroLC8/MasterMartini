package com.example.mastermartini.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.FotoRepository
import com.example.mastermartini.data.UsuarioException
import com.example.mastermartini.data.UsuarioRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
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
class RegistroViewModelTest : StringSpec({

    val app = aplicacionDePrueba()
    val repo = mockk<UsuarioRepository>()
    val almacenamiento = mockk<AlmacenamientoLocal>(relaxed = true)

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        clearMocks(repo, almacenamiento)
        every { almacenamiento.obtenerIdUsuario() } returns null
        every { almacenamiento.existePerfil() } returns false
        // FotoRepository se crea dentro del ViewModel: mockkConstructor lo reemplaza
        mockkConstructor(FotoRepository::class)
        coEvery { anyConstructed<FotoRepository>().obtenerFoto() } returns null
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    fun crearViewModel() = RegistroViewModel(app, repo, almacenamiento)

    // Llena todos los campos con datos válidos de un registro nuevo
    fun RegistroViewModel.llenarFormulario() {
        onNombreChange("Rosa Pérez")
        onRutChange("12.345.678-5")
        onCorreoChange("rosa@example.com")
        onTelefonoChange("9 1234 5678")
        onPasswordChange("Clave123")
        onPersonasNegocioChange("Solo yo")
    }

    // ---------- Validaciones ----------

    "el nombre debe tener al menos 3 letras" {
        val vm = crearViewModel()
        vm.onNombreChange(" Al ")
        vm.uiState.value.errorNombre shouldBe "El nombre debe tener al menos 3 caracteres"
        vm.onNombreChange("Ana")
        vm.uiState.value.errorNombre.shouldBeNull()
    }

    "el RUT vacío o sin guion muestra el error correspondiente" {
        val vm = crearViewModel()
        vm.onRutChange("")
        vm.uiState.value.errorRut shouldBe "Ingresa el RUT de la empresa"
        vm.onRutChange("12345678")
        vm.uiState.value.errorRut shouldBe "Formato incorrecto: usa números, guion y dígito (Ej: 12345678-5)"
        vm.onRutChange("12AB5678-5")
        vm.uiState.value.errorRut.shouldNotBeNull()
    }

    "el RUT con dígito verificador incorrecto muestra error" {
        val vm = crearViewModel()
        vm.onRutChange("12345678-9")
        vm.uiState.value.errorRut shouldBe "Dígito verificador incorrecto, revisa el RUT"
    }

    "acepta RUT válidos con puntos, con K y con dígito 0" {
        val vm = crearViewModel()
        vm.onRutChange("12.345.678-5")
        vm.uiState.value.errorRut.shouldBeNull()
        vm.onRutChange("76354771-k")
        vm.uiState.value.errorRut.shouldBeNull()
        vm.onRutChange("10000004-0")
        vm.uiState.value.errorRut.shouldBeNull()
    }

    "el correo debe tener @ y punto" {
        val vm = crearViewModel()
        vm.onCorreoChange("rosa.example.com")
        vm.uiState.value.errorCorreo shouldBe "Correo no válido (debe incluir @ y punto)"
        vm.onCorreoChange("rosa@example.com")
        vm.uiState.value.errorCorreo.shouldBeNull()
    }

    "el teléfono muestra un error distinto según el problema" {
        val vm = crearViewModel()
        vm.onTelefonoChange("")
        vm.uiState.value.errorTelefono shouldBe "Ingresa tu número de celular"
        vm.onTelefonoChange("9123abc78")
        vm.uiState.value.errorTelefono shouldBe "El teléfono solo debe tener números"
        vm.onTelefonoChange("812345678")
        vm.uiState.value.errorTelefono shouldBe "El celular debe empezar con 9"
        vm.onTelefonoChange("91234")
        vm.uiState.value.errorTelefono shouldBe "El celular debe tener 9 dígitos (Ej: 912345678)"
        vm.onTelefonoChange("9 1234 5678")
        vm.uiState.value.errorTelefono.shouldBeNull()
    }

    "la contraseña debe tener al menos 4 caracteres" {
        val vm = crearViewModel()
        vm.onPasswordChange("123")
        vm.uiState.value.errorPassword shouldBe "La contraseña debe tener al menos 4 caracteres"
        vm.onPasswordChange("1234")
        vm.uiState.value.errorPassword.shouldBeNull()
    }

    "con todos los campos válidos el formulario queda listo para enviar" {
        val vm = crearViewModel()
        vm.llenarFormulario()
        vm.onRubroChange("Chocolatería")
        vm.uiState.value.esFormularioValido.shouldBeTrue()
        vm.uiState.value.rubro shouldBe "Chocolatería"
    }

    "sin elegir personas se puede intentar registrar pero el formulario no es válido" {
        val vm = crearViewModel()
        vm.onNombreChange("Rosa Pérez")
        vm.onRutChange("12345678-5")
        vm.onCorreoChange("rosa@example.com")
        vm.onTelefonoChange("912345678")
        vm.onPasswordChange("Clave123")
        vm.uiState.value.puedeIntentarRegistrar.shouldBeTrue()
        vm.uiState.value.esFormularioValido.shouldBeFalse()
    }

    // ---------- Guardar ----------

    "guardar sin elegir personas muestra el error y no llama al repositorio" {
        val vm = crearViewModel()
        vm.guardarPerfil {}
        vm.uiState.value.errorPersonas shouldBe "Elige una opción (puedes elegir \"Prefiero no decirlo\")"
        coVerify(exactly = 0) { repo.registrar(any(), any(), any(), any(), any(), any(), any()) }
    }

    "guardar con el formulario inválido no hace nada" {
        val vm = crearViewModel()
        vm.onPersonasNegocioChange("Solo yo")
        vm.guardarPerfil {}
        vm.uiState.value.cargando.shouldBeFalse()
    }

    "registro exitoso guarda la sesión y avisa a la pantalla" {
        runTest {
            val nuevo = usuarioDePrueba(5)
            coEvery { repo.registrar(any(), any(), any(), any(), any(), any(), any()) } returns nuevo
            val vm = crearViewModel()
            vm.llenarFormulario()
            var exito = false

            vm.guardarPerfil { exito = true }
            vm.uiState.value.cargando.shouldBeTrue()
            advanceUntilIdle()

            exito.shouldBeTrue()
            verify { almacenamiento.guardarSesion(nuevo) }
            coVerify {
                repo.registrar(
                    "Rosa Pérez", "12.345.678-5", "rosa@example.com", "9 1234 5678",
                    "Panadería", "Solo yo", "Clave123"
                )
            }
        }
    }

    "si el correo ya existe se muestra el mensaje del repositorio" {
        runTest {
            coEvery { repo.registrar(any(), any(), any(), any(), any(), any(), any()) } throws
                UsuarioException.DatoDuplicado("correo")
            val vm = crearViewModel()
            vm.llenarFormulario()
            vm.guardarPerfil {}
            advanceUntilIdle()

            vm.uiState.value.cargando.shouldBeFalse()
            vm.uiState.value.errorGeneral shouldBe
                "Ya existe una cuenta con ese correo. Inicia sesión o usa otro correo."
        }
    }

    "mientras se está guardando no se envía dos veces" {
        runTest {
            coEvery { repo.registrar(any(), any(), any(), any(), any(), any(), any()) } returns usuarioDePrueba(5)
            val vm = crearViewModel()
            vm.llenarFormulario()
            vm.guardarPerfil {}
            vm.guardarPerfil {}
            advanceUntilIdle()
            coVerify(exactly = 1) { repo.registrar(any(), any(), any(), any(), any(), any(), any()) }
        }
    }

    // ---------- Edición de perfil ----------

    "con sesión iniciada el formulario se llena con el perfil guardado" {
        every { almacenamiento.obtenerIdUsuario() } returns 7L
        every { almacenamiento.existePerfil() } returns true
        every { almacenamiento.obtenerNombre() } returns "Rosa Pérez"
        every { almacenamiento.obtenerRut() } returns "12345678-5"
        every { almacenamiento.obtenerCorreo() } returns "rosa@example.com"
        every { almacenamiento.obtenerRubro() } returns "Pastelería"
        every { almacenamiento.obtenerTelefono() } returns "912345678"
        every { almacenamiento.obtenerPersonasNegocio() } returns "2 a 5 personas"

        val vm = crearViewModel()
        val estado = vm.uiState.value

        estado.esEdicion.shouldBeTrue()
        estado.nombre shouldBe "Rosa Pérez"
        estado.rubro shouldBe "Pastelería"
        estado.personasNegocio shouldBe "2 a 5 personas"
        // En edición no se pide contraseña
        estado.esFormularioValido.shouldBeTrue()
        vm.hayPerfilGuardado().shouldBeTrue()
        vm.obtenerNombreGuardado() shouldBe "Rosa Pérez"
        vm.obtenerRutGuardado() shouldBe "12345678-5"
        vm.obtenerCorreoGuardado() shouldBe "rosa@example.com"
        vm.obtenerRubroGuardado() shouldBe "Pastelería"
    }

    "una opción de personas antigua que ya no existe no se carga" {
        every { almacenamiento.existePerfil() } returns true
        every { almacenamiento.obtenerPersonasNegocio() } returns "Mediana empresa"
        crearViewModel().uiState.value.personasNegocio shouldBe ""
    }

    "en edición guardar actualiza el perfil en vez de registrar" {
        runTest {
            every { almacenamiento.obtenerIdUsuario() } returns 7L
            coEvery { repo.actualizarPerfil(any(), any(), any(), any(), any()) } returns usuarioDePrueba(7)
            val vm = crearViewModel()
            vm.llenarFormulario()
            vm.guardarPerfil {}
            advanceUntilIdle()

            coVerify { repo.actualizarPerfil(7, "Rosa Pérez", "9 1234 5678", "Panadería", "Solo yo") }
            coVerify(exactly = 0) { repo.registrar(any(), any(), any(), any(), any(), any(), any()) }
        }
    }

    "si la sesión se pierde al guardar la edición se muestra NoEncontrado" {
        runTest {
            // Primero hay sesión (al abrir) y luego ya no (al guardar)
            every { almacenamiento.obtenerIdUsuario() } returnsMany listOf(7L, null)
            val vm = crearViewModel()
            vm.llenarFormulario()
            vm.guardarPerfil {}
            advanceUntilIdle()
            vm.uiState.value.errorGeneral shouldBe "No encontramos ese usuario."
        }
    }

    // ---------- Foto del local ----------

    "al abrir se muestra la foto guardada" {
        runTest {
            val foto = mockk<Bitmap>()
            coEvery { anyConstructed<FotoRepository>().obtenerFoto() } returns foto
            val vm = crearViewModel()
            advanceUntilIdle()
            vm.uiState.value.fotoLocal shouldBe foto
        }
    }

    "una foto tomada con la cámara se muestra y borra el aviso" {
        runTest {
            val foto = mockk<Bitmap>()
            coEvery { anyConstructed<FotoRepository>().guardarFoto(foto) } returns foto
            val vm = crearViewModel()
            vm.onPermisoCamaraDenegado()
            vm.onFotoTomada(foto)
            advanceUntilIdle()
            vm.uiState.value.fotoLocal shouldBe foto
            vm.uiState.value.avisoCamara.shouldBeNull()
        }
    }

    "si la imagen elegida no se puede abrir se muestra un aviso" {
        runTest {
            val uri = mockk<Uri>()
            coEvery { anyConstructed<FotoRepository>().guardarFotoDesdeUri(uri) } returns null
            val vm = crearViewModel()
            vm.onFotoElegida(uri)
            advanceUntilIdle()
            vm.uiState.value.avisoCamara shouldBe "No se pudo abrir la imagen. Prueba con otra o regístrate igual."
        }
    }

    "sin permiso o sin cámara se avisa y se puede seguir" {
        val vm = crearViewModel()
        vm.onPermisoCamaraDenegado()
        vm.uiState.value.avisoCamara shouldBe
            "Sin permiso de cámara no podemos tomar la foto. Puedes registrarte igual."
        vm.onCamaraNoDisponible()
        vm.uiState.value.avisoCamara shouldBe
            "No se encontró una cámara en este dispositivo. Puedes registrarte igual."
    }

    "la Factory crea el ViewModel con sus dependencias" {
        RegistroViewModel.Factory
            .create(RegistroViewModel::class.java, extrasConAplicacion(app))
            .shouldBeInstanceOf<RegistroViewModel>()
    }
})
