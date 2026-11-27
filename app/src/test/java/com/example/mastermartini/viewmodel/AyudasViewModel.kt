package com.example.mastermartini.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.example.mastermartini.model.Rol
import com.example.mastermartini.model.Usuario
import io.mockk.every
import io.mockk.mockk
import java.io.File

// Ayudas compartidas por las pruebas de ViewModels

// Application de mentira: relaxed responde valores vacíos a todo lo que no se configura
fun aplicacionDePrueba(): Application {
    val carpetaTemporal = File(System.getProperty("java.io.tmpdir")!!)
    return mockk(relaxed = true) {
        every { filesDir } returns carpetaTemporal
        every { cacheDir } returns carpetaTemporal
    }
}

// Lo que Android le entrega a una Factory al crear un ViewModel
fun extrasConAplicacion(application: Application): CreationExtras {
    return MutableCreationExtras().apply { set(APPLICATION_KEY, application) }
}

fun usuarioDePrueba(
    id: Long,
    rol: Rol = Rol.Cliente,
    nombre: String = "Usuario $id",
    correo: String = "usuario$id@example.com",
    planPro: Boolean = false
): Usuario {
    return Usuario(
        id = id, nombre = nombre, rut = "11111111-1", correo = correo,
        telefono = "911111111", rubro = "Panadería", personasNegocio = "Solo yo",
        rol = rol, planPro = planPro
    )
}
