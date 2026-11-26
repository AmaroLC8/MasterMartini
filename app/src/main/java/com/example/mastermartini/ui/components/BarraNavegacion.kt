package com.example.mastermartini.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro

// Modelo de cada ítem de la barra inferior
data class ItemNavegacion(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
)

// Opciones principales de la barra inferior en orden de visualización
val itemsNavegacion = listOf(
    ItemNavegacion(ruta = "home", titulo = "Inicio", icono = Icons.Filled.Home),
    ItemNavegacion(ruta = "recetas", titulo = "Recetas", icono = Icons.AutoMirrored.Filled.MenuBook),
    ItemNavegacion(ruta = "cursos", titulo = "Cursos", icono = Icons.Filled.School),
    ItemNavegacion(ruta = "favoritos", titulo = "Favoritos", icono = Icons.Filled.Favorite),
    ItemNavegacion(ruta = "perfil", titulo = "Perfil", icono = Icons.Filled.Person)
)

// Barra de navegación inferior compartida entre las pantallas principales
@Composable
fun BarraNavegacion(
    rutaActual: String?,
    onNavegar: (String) -> Unit
) {
    NavigationBar(containerColor = BlancoPuro) {
        itemsNavegacion.forEach { item ->
            NavigationBarItem(
                selected = rutaActual == item.ruta,
                onClick = { onNavegar(item.ruta) },
                icon = {
                    Icon(imageVector = item.icono, contentDescription = null)
                },
                label = {
                    Text(
                        text = item.titulo,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BlancoPuro,
                    selectedTextColor = VerdeOscuro,
                    indicatorColor = VerdeItaliano,
                    unselectedIconColor = TextoOscuro,
                    unselectedTextColor = TextoOscuro
                )
            )
        }
    }
}
