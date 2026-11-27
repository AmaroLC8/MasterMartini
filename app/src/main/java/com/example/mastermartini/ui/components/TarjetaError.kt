package com.example.mastermartini.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro

// Tarjeta para un error de conexión o del servidor, con el botón "Reintentar"
@Composable
fun TarjetaError(
    mensaje: String,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaAviso(
        icono = Icons.Filled.WifiOff,
        colorIcono = RojoItaliano,
        titulo = "No se pudo cargar",
        mensaje = mensaje,
        textoBoton = "Reintentar",
        iconoBoton = Icons.Filled.Refresh,
        onClick = onReintentar,
        modifier = modifier
    )
}

// Tarjeta para un curso que el servidor ya no tiene (404), con el botón "Volver"
@Composable
fun TarjetaCursoNoExiste(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaAviso(
        icono = Icons.Filled.SearchOff,
        colorIcono = VerdeOscuro,
        titulo = "Este curso ya no existe",
        mensaje = "Puede que lo hayan eliminado. Vuelve al catálogo para ver los cursos disponibles.",
        textoBoton = "Volver",
        iconoBoton = null,
        onClick = onVolver,
        modifier = modifier
    )
}

// Tarjeta para una receta o producto que el servidor ya no tiene (404), con el botón "Volver"
@Composable
fun TarjetaNoDisponible(
    titulo: String,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaAviso(
        icono = Icons.Filled.SearchOff,
        colorIcono = VerdeOscuro,
        titulo = titulo,
        mensaje = "Puede que la hayan quitado del catálogo. Vuelve atrás para ver lo que hay disponible.",
        textoBoton = "Volver",
        iconoBoton = null,
        onClick = onVolver,
        modifier = modifier
    )
}

// Ruedita de carga centrada, mientras llegan los datos del servidor
@Composable
fun CargandoContenido(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp)
    ) {
        CircularProgressIndicator(color = VerdeItaliano)
    }
}

// Diseño común de las tarjetas: ícono, título, mensaje y un botón grande
@Composable
private fun TarjetaAviso(
    icono: ImageVector,
    colorIcono: Color,
    titulo: String,
    mensaje: String,
    textoBoton: String,
    iconoBoton: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        border = BorderStroke(1.dp, colorIcono.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = titulo,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro,
                textAlign = TextAlign.Center
            )
            Text(
                text = mensaje,
                fontSize = 17.sp,
                color = TextoOscuro,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = VerdeItaliano, contentColor = BlancoPuro),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                if (iconoBoton != null) {
                    Icon(imageVector = iconoBoton, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = textoBoton, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
