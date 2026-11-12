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

// ETIQUETA "GRATIS" o "PLAN PRO" de un curso online (componente reutilizable).
// La usan la tarjeta de la lista de Cursos y el Detalle del curso.
// Accesibilidad: no depende solo del color, porque la etiqueta dice con TEXTO si el curso
// es gratis o del Plan Pro (una persona que no distingue colores igual lo entiende).
// Esquinas casi rectas (6 dp) para que no se confunda con la etiqueta redonda de la categoría.
// Solo informa: no se puede tocar.
@Composable
fun EtiquetaAcceso(
    esGratis: Boolean,
    // Tamaño de la letra. 16 por defecto: es lo más importante de la tarjeta
    // (dice si el curso se puede ver gratis), así que no va más chico.
    tamanoTexto: Int = 16
) {
    // GRATIS: verde oscuro con texto blanco (más contraste que el verde claro).
    // PLAN PRO: dorado de la marca con texto oscuro (el blanco casi no se lee sobre dorado).
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
