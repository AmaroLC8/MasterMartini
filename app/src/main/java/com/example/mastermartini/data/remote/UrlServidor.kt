package com.example.mastermartini.data.remote

// Reglas puras (sin Android) para armar la URL de cada microservicio; así se pueden probar
object UrlServidor {

    // Usa el host de local.properties si existe; si no, 10.0.2.2 en emulador o localhost en celular (adb reverse)
    fun elegirHost(hostLocal: String, esEmulador: Boolean): String {
        if (hostLocal.isNotBlank()) return hostLocal.trim()
        return if (esEmulador) "10.0.2.2" else "localhost"
    }

    // En debug cada microservicio va en su puerto; en release todos usan la única dirección HTTPS
    fun armarUrl(esDebug: Boolean, host: String, puerto: Int, urlServidor: String): String {
        return if (esDebug) "http://$host:$puerto/" else urlServidor
    }
}
