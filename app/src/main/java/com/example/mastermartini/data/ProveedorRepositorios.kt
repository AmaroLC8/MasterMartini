package com.example.mastermartini.data

// PROVEEDOR DE REPOSITORIOS
// "object" = existe UNA sola copia en toda la app. Así Login, Registro y Perfil usan
// el MISMO repositorio de usuarios (si cada uno creara el suyo, el repositorio falso
// tendría listas distintas y lo registrado en uno no existiría en el otro).
//
// Los Factory de los ViewModels sacan el repositorio de aquí y se lo pasan por el constructor.
object ProveedorRepositorios {

    // ¿Se usa el backend real?
    //   true  -> UsuarioRepositoryRemoto: habla con el microservicio de usuarios (Retrofit, puerto 9091)
    //   false -> UsuarioRepositoryFalso: usuarios de prueba en memoria (cliente@example.com, Demo1234...)
    // PLAN B PARA LA DEFENSA: si el backend no levanta o falla, cambiar a false y volver a
    // ejecutar la app. Todo funciona igual con los datos de prueba, sin tocar nada más.
    // "const val" = constante que se conoce al compilar.
    const val USAR_BACKEND = true

    // El tipo es la INTERFAZ (UsuarioRepository), no una clase concreta: el resto de la app
    // no sabe ni le importa cuál de los dos está detrás. Es la ÚNICA línea que decide cuál se usa.
    val usuarioRepository: UsuarioRepository = if (USAR_BACKEND) {
        UsuarioRepositoryRemoto()
    } else {
        UsuarioRepositoryFalso()
    }
}
