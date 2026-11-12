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

// MOLDE DE CADA BOTÓN DE LA BARRA INFERIOR
// ruta: a qué pantalla lleva (la misma ruta registrada en el NavHost de MainActivity).
// titulo: el texto que se ve bajo el ícono.
// icono: el dibujo del botón (ImageVector es el tipo de los íconos de Material).
data class ItemNavegacion(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
)

// LOS 5 BOTONES DE LA BARRA, en el orden en que se ven de izquierda a derecha.
// Está fuera del composable para que la lista se cree una sola vez y no en cada redibujo.
// MainActivity también la usa para saber en qué pantallas mostrar la barra.
val itemsNavegacion = listOf(
    ItemNavegacion(ruta = "home", titulo = "Inicio", icono = Icons.Filled.Home),
    // AutoMirrored: el ícono del libro se da vuelta solo en idiomas que se leen de derecha a izquierda
    ItemNavegacion(ruta = "recetas", titulo = "Recetas", icono = Icons.AutoMirrored.Filled.MenuBook),
    // "Cursos" reemplazó a "Buscar": la búsqueda ahora se abre con la lupa de Inicio y Recetas
    // (su ruta "buscar" y su pantalla siguen existiendo en MainActivity).
    ItemNavegacion(ruta = "cursos", titulo = "Cursos", icono = Icons.Filled.School),
    ItemNavegacion(ruta = "favoritos", titulo = "Favoritos", icono = Icons.Filled.Favorite),
    ItemNavegacion(ruta = "perfil", titulo = "Perfil", icono = Icons.Filled.Person)
)

// BARRA DE NAVEGACIÓN INFERIOR
// Solo DIBUJA los botones y avisa cuál se tocó: no navega por su cuenta.
// MainActivity, que tiene el navController, decide cómo navegar (igual que onVerDetalle en Home).
// rutaActual: la pantalla que se está viendo, para pintar su botón como seleccionado.
//   Es String? porque al partir la app todavía puede no haber ninguna pantalla cargada.
@Composable
fun BarraNavegacion(
    rutaActual: String?,
    onNavegar: (String) -> Unit
) {
    // NavigationBar de Material 3: ya trae la altura correcta (80 dp, más que los 48 dp mínimos)
    // y deja espacio para la barra de gestos del celular.
    NavigationBar(containerColor = BlancoPuro) {
        // Un NavigationBarItem por cada botón de la lista
        itemsNavegacion.forEach { item ->
            NavigationBarItem(
                // Está seleccionado si su ruta es la de la pantalla actual
                selected = rutaActual == item.ruta,
                onClick = { onNavegar(item.ruta) },
                icon = {
                    // contentDescription = null: el texto de abajo ya dice qué es el botón,
                    // así TalkBack no lo lee dos veces ("Inicio, Inicio")
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
                // Los textos se ven SIEMPRE, no solo en el botón seleccionado:
                // un usuario mayor no tiene que adivinar qué significa cada ícono
                alwaysShowLabel = true,
                // Colores de la marca: el seleccionado lleva una "píldora" verde con el ícono blanco
                // y el texto en verde oscuro; los demás van en texto oscuro (buen contraste sobre blanco)
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
