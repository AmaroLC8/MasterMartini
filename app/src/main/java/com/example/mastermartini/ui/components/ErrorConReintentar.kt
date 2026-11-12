package com.example.mastermartini.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano

// LO QUE SE DIBUJA EN EL CASO EstadoUi.Error (componente reutilizable)
// Lo usan GestionClientesScreen y GestionUsuariosScreen: ícono ⚠, el mensaje del error
// (ej. "No tienes permiso para hacer esta acción.") y un botón grande "Reintentar".
@Composable
fun ErrorConReintentar(
    mensaje: String,
    // Vuelve a pedir la lista (el ViewModel llama otra vez al repositorio)
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Decorativo: el mensaje de abajo ya explica qué pasó
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = RojoItaliano,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = mensaje,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextoOscuro,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onReintentar,
            colors = ButtonDefaults.buttonColors(containerColor = VerdeItaliano),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
            Text(text = "  Reintentar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
