package com.example.mastermartini.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro

// Etiqueta visual para recetas y productos aptos veganos
@Composable
fun EtiquetaVegana(
    tamanoTexto: Int = 14
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(VerdeItaliano.copy(alpha = 0.12f), RoundedCornerShape(50))
            .border(1.dp, VerdeItaliano, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Eco,
            contentDescription = null,
            tint = VerdeItaliano,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Apto vegano",
            fontSize = tamanoTexto.sp,
            fontWeight = FontWeight.Bold,
            color = VerdeOscuro
        )
    }
}
