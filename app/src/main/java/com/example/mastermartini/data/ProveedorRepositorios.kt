package com.example.mastermartini.data

// PROVEEDOR DE REPOSITORIOS
// "object" = existe UNA sola copia en toda la app. Así Login, Registro y Perfil usan
// el MISMO repositorio de usuarios (si cada uno creara el suyo, el repositorio falso
// tendría listas distintas y lo registrado en uno no existiría en el otro).
//
// Los Factory de los ViewModels sacan el repositorio de aquí y se lo pasan por el constructor.
object ProveedorRepositorios {

    // El tipo es la INTERFAZ (UsuarioRepository), no la clase falsa: el resto de la app
    // no sabe ni le importa cuál de los dos está detrás.
    // Cuando el backend esté listo, cambiar SOLO esta línea por el repositorio remoto
    // (ej. "= UsuarioRepositoryRemoto()").
    val usuarioRepository: UsuarioRepository = UsuarioRepositoryFalso()
}
