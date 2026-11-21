package com.example.mastermartini.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.data.DatosEmpresa
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.TextoOscuro

// BOTÓN "VER CATÁLOGO COMPLETO (PDF)" (componente reutilizable)
// Lo usan ProductosScreen y ProductoDetalleScreen.
// Abre el catálogo público de Master Martini en el NAVEGADOR del celular (o en un lector de PDF).
@Composable
fun BotonCatalogoPdf(modifier: Modifier = Modifier) {
    // El Context hace falta para lanzar el Intent y para mostrar el Toast
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedButton(
            onClick = {
                // Intent ACTION_VIEW + una dirección web = "ábrele esto al usuario con la app que sepa".
                // Android elige el navegador (o un lector de PDF). Nuestra app NO descarga nada:
                // por eso NO necesita el permiso INTERNET (el que usa internet es el navegador).
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DatosEmpresa.URL_CATALOGO))
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // El celular no tiene ninguna app que abra páginas web: se avisa y la app sigue
                    Toast.makeText(
                        context,
                        "No encontramos un navegador para abrir el catálogo.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            border = BorderStroke(2.dp, RojoItaliano),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = RojoItaliano),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(imageVector = Icons.Filled.PictureAsPdf, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Ver catálogo completo (PDF)", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        // Aviso pequeño: así el usuario no se sorprende cuando la app abre el navegador
        Text(
            text = "Se abre en el navegador",
            fontSize = 14.sp,
            color = TextoOscuro.copy(alpha = 0.7f)
        )
    }
}
