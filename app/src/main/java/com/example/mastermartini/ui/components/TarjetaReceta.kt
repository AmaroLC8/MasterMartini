package com.example.mastermartini.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.model.Curso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro

// Tarjeta reutilizable para mostrar una receta en listados y búsquedas
@Composable
fun TarjetaReceta(
    curso: Curso,
    onVerDetalle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onVerDetalle(curso.id) },
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        border = BorderStroke(1.dp, DoradoCalidad.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = curso.categoria,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlancoPuro,
                    modifier = Modifier
                        .background(VerdeItaliano, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
                if (curso.esVegano) {
                    EtiquetaVegana()
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = curso.titulo,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Insumo: ${curso.insumo}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = VerdeOscuro
            )
            Text(
                text = "Instructor: ${curso.chef}",
                fontSize = 15.sp,
                color = TextoOscuro
            )
            Text(
                text = "⏱️ Duración: ${curso.duracion}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = RojoItaliano
            )

            TextButton(
                onClick = { onVerDetalle(curso.id) },
                modifier = Modifier
                    .align(Alignment.End)
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    text = "Ver receta",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeOscuro
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = VerdeOscuro
                )
            }
        }
    }
}
