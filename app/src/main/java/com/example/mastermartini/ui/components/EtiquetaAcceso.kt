package com.example.mastermartini.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeOscuro

// Etiqueta visual que indica si un curso es gratuito o Plan Pro
@Composable
fun EtiquetaAcceso(
    esGratis: Boolean,
    tamanoTexto: Int = 16
) {
    val colorFondo = if (esGratis) VerdeOscuro else DoradoCalidad
    val colorTexto = if (esGratis) BlancoPuro else TextoOscuro

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(colorFondo, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        if (!esGratis) {
            Icon(
                imageVector = Icons.Rounded.WorkspacePremium,
                contentDescription = null,
                tint = colorTexto,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = if (esGratis) "GRATIS" else "PLAN PRO",
            fontSize = tamanoTexto.sp,
            fontWeight = FontWeight.Bold,
            color = colorTexto
        )
    }
}
