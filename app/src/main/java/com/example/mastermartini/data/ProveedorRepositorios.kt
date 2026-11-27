package com.example.mastermartini.data

// Proveedor único del repositorio de usuarios: la app siempre usa el backend real
object ProveedorRepositorios {

    // Es un object para que las pruebas puedan reemplazarlo con mockkObject
    val usuarioRepository: UsuarioRepository = UsuarioRepositoryRemoto()
}
