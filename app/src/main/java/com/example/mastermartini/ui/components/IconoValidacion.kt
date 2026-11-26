package com.example.mastermartini.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.VerdeItaliano

// Ícono de estado de validación para campos de texto
@Composable
fun IconoValidacion(valor: String, error: String?) {
    if (error != null) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "Dato con error",
            tint = RojoItaliano
        )
    } else if (valor.isNotBlank()) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Dato correcto",
            tint = VerdeItaliano
        )
    }
}
