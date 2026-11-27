package com.example.mastermartini.data

// Proveedor singleton de repositorios con conmutador entre backend real o datos locales
object ProveedorRepositorios {

    const val USAR_BACKEND = true

    val usuarioRepository: UsuarioRepository = if (USAR_BACKEND) {
        UsuarioRepositoryRemoto()
    } else {
        UsuarioRepositoryFalso()
    }
}
