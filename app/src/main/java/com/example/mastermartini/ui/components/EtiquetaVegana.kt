package com.example.mastermartini.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro

// ETIQUETA "APTO VEGANO 🌱" (componente reutilizable)
// La usan TarjetaReceta (Recetas y Buscar) y DetalleScreen, junto a la categoría.
// Es verde CLARO con borde y texto verde oscuro, para que se distinga de la etiqueta de
// categoría (verde fuerte con texto blanco) y no parezca otra sección.
// Solo informa: no se puede tocar.
@Composable
fun EtiquetaVegana(
    // Tamaño de la letra: un poco más grande en el Detalle que en las tarjetas
    tamanoTexto: Int = 14
) {
    Text(
        text = "Apto vegano 🌱",
        fontSize = tamanoTexto.sp,
        fontWeight = FontWeight.Bold,
        color = VerdeOscuro,
        modifier = Modifier
            .background(VerdeItaliano.copy(alpha = 0.12f), RoundedCornerShape(50))
            .border(1.dp, VerdeItaliano, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}
