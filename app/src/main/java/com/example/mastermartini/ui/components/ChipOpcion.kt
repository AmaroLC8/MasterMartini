package com.example.mastermartini.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano

// BOTÓN DE OPCIÓN (FilterChip) (componente reutilizable)
// Lo usan:
// - RecetasScreen: en sus tres filas (secciones, orden y "Solo veganas").
// - CapacitacionesScreen: en sus filtros "Todos", "Gratis", "Plan Pro" y las categorías.
// Se escribe UNA vez aquí para no repetir los colores y el tamaño en cada botón,
// y así los filtros se ven iguales en las dos pantallas.
@Composable
fun ChipOpcion(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    // Margen extra opcional (lo usa "Solo veganas", que no está dentro de una Row con padding)
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(text = texto, fontSize = 16.sp) },
        // Check cuando está elegido: así se nota cuál está marcado sin depender solo del color.
        // Si no está elegido, se pasa null y no se dibuja ningún ícono.
        leadingIcon = if (seleccionado) {
            { Icon(imageVector = Icons.Filled.Check, contentDescription = null) }
        } else {
            null
        },
        // Mismos colores que los chips del Registro
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = VerdeItaliano,
            selectedLabelColor = BlancoPuro,
            selectedLeadingIconColor = BlancoPuro,
            containerColor = BlancoPuro,
            labelColor = TextoOscuro
        ),
        // 48 dp de alto: botón grande y fácil de tocar (el normal mide 32 dp)
        modifier = modifier.height(48.dp)
    )
}
