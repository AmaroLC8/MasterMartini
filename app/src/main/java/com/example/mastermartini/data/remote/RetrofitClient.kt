package com.example.mastermartini.data.remote

import android.os.Build
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Cliente Retrofit singleton que centraliza las instancias API de los microservicios
object RetrofitClient {

    private val isEmulator: Boolean by lazy {
        Build.FINGERPRINT?.startsWith("generic") == true ||
            Build.FINGERPRINT?.startsWith("unknown") == true ||
            Build.MODEL?.contains("google_sdk") == true ||
            Build.MODEL?.contains("Emulator") == true ||
            Build.MODEL?.contains("Android SDK built for x86") == true ||
            Build.MANUFACTURER?.contains("Genymotion") == true ||
            (Build.BRAND?.startsWith("generic") == true && Build.DEVICE?.startsWith("generic") == true) ||
            Build.PRODUCT?.contains("sdk_gphone") == true ||
            Build.PRODUCT == "google_sdk" ||
            Build.HARDWARE?.contains("goldfish") == true ||
            Build.HARDWARE?.contains("ranchu") == true
    }

    // 10.0.2.2 apunta al PC desde el emulador; localhost funciona en dispositivo físico vía adb reverse
    private val host: String by lazy {
        if (isEmulator) "10.0.2.2" else "localhost"
    }

    private val BASE_URL_CURSOS get() = "http://$host:9092/"
    private val BASE_URL_USUARIOS get() = "http://$host:9091/"
    private val BASE_URL_INTERACCIONES get() = "http://$host:9093/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val cursosApi: CursosApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_CURSOS)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CursosApiService::class.java)
    }

    val usuariosApi: UsuarioApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_USUARIOS)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuarioApi::class.java)
    }

    val interaccionesApi: InteraccionesApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_INTERACCIONES)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InteraccionesApi::class.java)
    }

    val climaApi: ClimaApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClimaApiService::class.java)
    }
}