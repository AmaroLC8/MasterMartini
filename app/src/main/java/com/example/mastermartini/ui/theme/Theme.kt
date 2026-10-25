package com.example.mastermartini.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VerdeItaliano,
    secondary = RojoItaliano,
    tertiary = DoradoCalidad
)

private val LightColorScheme = lightColorScheme(
    primary = VerdeItaliano,
    secondary = RojoItaliano,
    tertiary = DoradoCalidad,
    background = BlancoHueso,
    surface = BlancoPuro,
    onPrimary = BlancoPuro,
    onSecondary = BlancoPuro,
    onBackground = TextoOscuro,
    onSurface = TextoOscuro
)

@Composable
fun MasterMartiniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Color dinámico (Android 12+): toma los colores del fondo de pantalla del usuario.
    // Lo dejamos en false para que la app use siempre la paleta de Master Martini
    // (verde y rojo italiano), sin importar el celular o el fondo que tenga la persona.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}