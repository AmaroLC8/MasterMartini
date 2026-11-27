package com.example.mastermartini.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.data.DatosEmpresa
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro

// Tarjeta reutilizable de contacto con ventas por WhatsApp, correo y teléfono
@Composable
fun ContactoVentas(
    mensaje: String
) {
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "¿Necesitas este producto?",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeOscuro
            )
            Text(
                text = "Escríbenos o llámanos y te ayudamos con tu pedido.",
                fontSize = 16.sp,
                color = TextoOscuro
            )

            Button(
                onClick = { consultarPorWhatsApp(context, mensaje) },
                colors = ButtonDefaults.buttonColors(containerColor = VerdeItaliano),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Consultar por WhatsApp", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }

            BotonContactoSecundario(
                texto = "Enviar correo",
                icono = { Icon(imageVector = Icons.Filled.Email, contentDescription = null) },
                onClick = { enviarCorreo(context, mensaje) }
            )

            BotonContactoSecundario(
                texto = "Llamar a ventas",
                icono = { Icon(imageVector = Icons.Filled.Call, contentDescription = null) },
                onClick = { llamarAVentas(context) }
            )
        }
    }
}

@Composable
private fun BotonContactoSecundario(
    texto: String,
    icono: @Composable () -> Unit,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(2.dp, VerdeItaliano),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeOscuro),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        icono()
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = texto, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

// Abre WhatsApp con el mensaje prellenado o selector de mensajería si no está instalado
private fun consultarPorWhatsApp(context: Context, mensaje: String) {
    val numero = DatosEmpresa.WHATSAPP_VENTAS

    if (numero.isEmpty()) {
        val intentWhatsApp = Intent(Intent.ACTION_SEND)
        intentWhatsApp.type = "text/plain"
        intentWhatsApp.putExtra(Intent.EXTRA_TEXT, mensaje)
        intentWhatsApp.setPackage("com.whatsapp")

        try {
            context.startActivity(intentWhatsApp)
        } catch (e: ActivityNotFoundException) {
            val intentCompartir = Intent(Intent.ACTION_SEND)
            intentCompartir.type = "text/plain"
            intentCompartir.putExtra(Intent.EXTRA_TEXT, mensaje)
            try {
                context.startActivity(Intent.createChooser(intentCompartir, "Enviar consulta con..."))
            } catch (e2: ActivityNotFoundException) {
                mostrarAviso(context, "No encontramos una app para enviar mensajes.")
            }
        }
    } else {
        val direccion = "https://wa.me/$numero?text=${Uri.encode(mensaje)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(direccion))
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            mostrarAviso(context, "No encontramos WhatsApp ni un navegador para abrir el chat.")
        }
    }
}

// Abre la app de correo predeterminada con destinatario, asunto y mensaje
private fun enviarCorreo(context: Context, mensaje: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + DatosEmpresa.CORREO_VENTAS))
    intent.putExtra(Intent.EXTRA_SUBJECT, "Consulta desde la app Master Martini")
    intent.putExtra(Intent.EXTRA_TEXT, mensaje)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        mostrarAviso(context, "No encontramos una app de correo en este celular.")
    }
}

// Abre el marcador telefónico sin iniciar la llamada automáticamente
private fun llamarAVentas(context: Context) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + DatosEmpresa.TELEFONO_VENTAS))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        mostrarAviso(context, "Este dispositivo no puede hacer llamadas.")
    }
}

private fun mostrarAviso(context: Context, texto: String) {
    Toast.makeText(context, texto, Toast.LENGTH_LONG).show()
}
