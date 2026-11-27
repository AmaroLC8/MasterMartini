plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // KSP lee las anotaciones de Room (@Entity, @Dao, @Database) y genera el código de la base de datos
    alias(libs.plugins.ksp)
    // Kover mide qué porcentaje del código ejecutan las pruebas unitarias
    alias(libs.plugins.kover)
}

android {
    namespace = "com.example.mastermartini"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.mastermartini"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    // Las clases de Android (Log, etc.) devuelven valores por defecto en vez de fallar en las pruebas
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.compose.material:material-icons-extended:1.7.5")

    // Room: base de datos local sobre SQLite (favoritos de cursos)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Retrofit y Gson (conexión HTTP con los microservicios y Cloudinary)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Reproductor de video moderno (soporta videos locales y enlaces HTTPS de Cloudinary)
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")

    // Carga de imágenes desde Cloudinary en Compose
    implementation("io.coil-kt:coil-compose:2.7.0")
    // Kotest (Requerido por Guía 15)
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")

    // JUnit 5 y Coroutines Test
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    // MockK para simular repositorios y APIs
    testImplementation("io.mockk:mockk:1.13.10")
}
// Obligatorio al final de build.gradle.kts para ejecutar Kotest/JUnit 5
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// Cobertura: solo la lógica (viewmodel, data, model) y mínimo 80 % de líneas
kover {
    reports {
        filters {
            includes {
                packages(
                    "com.example.mastermartini.viewmodel",
                    "com.example.mastermartini.data",
                    "com.example.mastermartini.model"
                )
            }
            excludes {
                packages("com.example.mastermartini.ui")
                classes(
                    "com.example.mastermartini.MainActivity*",
                    // Código generado por Room
                    "*_Impl",
                    "*_Impl$*",
                    // Interfaces de Retrofit y su cliente: no tienen lógica propia
                    "com.example.mastermartini.data.remote.*Api",
                    "com.example.mastermartini.data.remote.*Api$*",
                    "com.example.mastermartini.data.remote.*ApiService",
                    "com.example.mastermartini.data.remote.*ApiService$*",
                    "com.example.mastermartini.data.remote.RetrofitClient*",
                    "com.example.mastermartini.data.local.MasterMartiniDatabase*",
                    // Solo envuelven cámara, GPS y subida de archivos de Android
                    "com.example.mastermartini.data.FotoRepository*",
                    "com.example.mastermartini.data.UbicacionRepository*",
                    "com.example.mastermartini.data.CloudinaryRepository*"
                )
            }
        }
        verify {
            rule {
                minBound(80)
            }
        }
    }
}
