package com.example.mastermartini.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.TextoOscuro

// UN DATO CON SU ÍCONO (ej: 🕒 15 min) (componente reutilizable)
// Lo usan:
// - DetalleScreen: duración, rendimiento y dificultad de la receta.
// - CapacitacionDetalleScreen: nivel, duración, módulos e instructor del curso online.
// Está en un solo lugar para que los datos se vean iguales en las dos pantallas.
@Composable
fun DatoConIcono(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Decorativo (contentDescription = null): el texto de al lado ya dice el dato
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = RojoItaliano,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = texto,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextoOscuro
        )
    }
}
