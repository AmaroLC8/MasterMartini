package com.example.mastermartini.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.VerdeItaliano

// Ícono que va al final de un campo de texto (trailingIcon) y muestra si el dato está bien o mal.
// Es público y está en ui/components para reutilizarlo en Registro y en Login.
// Solo DIBUJA: la decisión de si hay error la toma el ViewModel y llega en el parámetro "error".
//  - Si hay error   -> triángulo rojo de advertencia.
//  - Si no hay error y el campo tiene texto -> check verde.
//  - Si el campo está vacío y sin error     -> no muestra nada.
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
