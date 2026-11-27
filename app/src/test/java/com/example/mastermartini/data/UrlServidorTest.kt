package com.example.mastermartini.data

import com.example.mastermartini.data.remote.UrlServidor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

// Prueba las reglas que eligen el host y la URL de cada microservicio
class UrlServidorTest : StringSpec({

    "sin host en local.properties, el emulador usa 10.0.2.2" {
        UrlServidor.elegirHost(hostLocal = "", esEmulador = true) shouldBe "10.0.2.2"
    }

    "sin host en local.properties, el celular físico usa localhost (adb reverse)" {
        UrlServidor.elegirHost(hostLocal = "  ", esEmulador = false) shouldBe "localhost"
    }

    "el host de local.properties manda sobre la detección y se le quitan los espacios" {
        UrlServidor.elegirHost(hostLocal = " 192.168.1.15 ", esEmulador = true) shouldBe "192.168.1.15"
        UrlServidor.elegirHost(hostLocal = "192.168.1.15", esEmulador = false) shouldBe "192.168.1.15"
    }

    "en debug cada microservicio usa host y su propio puerto" {
        val url = "https://PENDIENTE_AWS/"
        UrlServidor.armarUrl(esDebug = true, host = "10.0.2.2", puerto = 9091, urlServidor = url) shouldBe "http://10.0.2.2:9091/"
        UrlServidor.armarUrl(esDebug = true, host = "localhost", puerto = 9092, urlServidor = url) shouldBe "http://localhost:9092/"
        UrlServidor.armarUrl(esDebug = true, host = "192.168.1.15", puerto = 9093, urlServidor = url) shouldBe "http://192.168.1.15:9093/"
    }

    "en release los tres microservicios usan la misma URL del servidor" {
        val url = "https://PENDIENTE_AWS/"
        UrlServidor.armarUrl(esDebug = false, host = "10.0.2.2", puerto = 9091, urlServidor = url) shouldBe url
        UrlServidor.armarUrl(esDebug = false, host = "10.0.2.2", puerto = 9092, urlServidor = url) shouldBe url
        UrlServidor.armarUrl(esDebug = false, host = "10.0.2.2", puerto = 9093, urlServidor = url) shouldBe url
    }
})
