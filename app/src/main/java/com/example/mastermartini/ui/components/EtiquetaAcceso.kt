package com.example.mastermartini.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    val texto = if (esGratis) "GRATIS" else "★ PLAN PRO"
    val colorFondo = if (esGratis) VerdeOscuro else DoradoCalidad
    val colorTexto = if (esGratis) BlancoPuro else TextoOscuro

    Text(
        text = texto,
        fontSize = tamanoTexto.sp,
        fontWeight = FontWeight.Bold,
        color = colorTexto,
        modifier = Modifier
            .background(colorFondo, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
