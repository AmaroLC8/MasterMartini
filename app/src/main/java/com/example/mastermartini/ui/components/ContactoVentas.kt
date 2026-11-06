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
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro

// TARJETA "¿NECESITAS ESTE PRODUCTO?" (componente reutilizable)
// La usan DetalleScreen (receta) y ProductoDetalleScreen (producto).
// Tiene 3 botones de contacto con ventas: WhatsApp, correo y teléfono.
//
// Los 3 usan INTENTS IMPLÍCITOS: no decimos QUÉ app abrir, sino QUÉ ACCIÓN queremos
// ("enviar un texto", "escribir un correo", "marcar un número"), y Android busca una app
// instalada que sepa hacerlo. Nuestra app no envía nada por sí misma: por eso no necesita
// permisos de internet, SMS ni llamadas.
@Composable
fun ContactoVentas(
    // Mensaje ya armado que se envía por WhatsApp o como cuerpo del correo.
    // Ej: "Hola, quiero información sobre Chocolate Ariba."
    mensaje: String
) {
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        border = BorderStroke(1.dp, DoradoCalidad.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
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

            // 1. WHATSAPP: botón relleno verde (la opción más usada)
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

            // 2. CORREO
            BotonContactoSecundario(
                texto = "Enviar correo",
                icono = { Icon(imageVector = Icons.Filled.Email, contentDescription = null) },
                onClick = { enviarCorreo(context, mensaje) }
            )

            // 3. TELÉFONO
            BotonContactoSecundario(
                texto = "Llamar a ventas",
                icono = { Icon(imageVector = Icons.Filled.Call, contentDescription = null) },
                onClick = { llamarAVentas(context) }
            )
        }
    }
}

// Botón con borde verde (correo y teléfono). Es private porque solo lo usa esta tarjeta.
// icono: @Composable () -> Unit recibe el ícono a dibujar, igual que "label" en un chip.
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

// --- FUNCIONES QUE ARMAN Y LANZAN CADA INTENT ---
// Son funciones normales (no @Composable): solo reciben el Context y lanzan el Intent.
// Todas tienen try/catch de ActivityNotFoundException: si el celular no tiene una app
// para esa acción, se avisa con un Toast y la app NO se cae.

// WHATSAPP
private fun consultarPorWhatsApp(context: Context, mensaje: String) {
    val numero = DatosEmpresa.WHATSAPP_VENTAS

    if (numero.isEmpty()) {
        // SIN NÚMERO (hoy): se abre WhatsApp con el mensaje escrito, y el usuario elige a quién mandarlo.
        // ACTION_SEND = "compartir este texto". setPackage("com.whatsapp") = "pero solo con WhatsApp".
        val intentWhatsApp = Intent(Intent.ACTION_SEND)
        intentWhatsApp.type = "text/plain"
        intentWhatsApp.putExtra(Intent.EXTRA_TEXT, mensaje)
        intentWhatsApp.setPackage("com.whatsapp")

        try {
            context.startActivity(intentWhatsApp)
        } catch (e: ActivityNotFoundException) {
            // No hay WhatsApp: se ofrece el mismo mensaje con CUALQUIER app que comparta texto
            // (SMS, Gmail, Telegram...). createChooser muestra la lista para que el usuario elija.
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
        // CON NÚMERO: wa.me abre el chat con ese número y el mensaje ya escrito.
        // Uri.encode convierte espacios, tildes y comillas en un formato seguro para una dirección web
        // (ej. " " -> "%20"). Sin esto, el mensaje se cortaría en el primer espacio.
        val direccion = "https://wa.me/$numero?text=${Uri.encode(mensaje)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(direccion))
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            mostrarAviso(context, "No encontramos WhatsApp ni un navegador para abrir el chat.")
        }
    }
}

// CORREO
// ACTION_SENDTO + "mailto:" = "escribir un correo a esta dirección". Solo responden apps de correo
// (Gmail, Outlook...), no WhatsApp ni otras apps de compartir.
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

// TELÉFONO
// ACTION_DIAL abre el MARCADOR con el número escrito, pero NO llama: el usuario decide si
// aprieta el botón verde. Por eso NO necesita permiso.
// ACTION_CALL sí llamaría directo, pero necesita el permiso peligroso CALL_PHONE
// (pedirlo en el Manifest y en ejecución). No lo usamos: es más simple y más seguro.
// Si TELEFONO_VENTAS está vacío, el marcador se abre vacío.
private fun llamarAVentas(context: Context) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + DatosEmpresa.TELEFONO_VENTAS))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Ej: una tablet sin teléfono
        mostrarAviso(context, "Este dispositivo no puede hacer llamadas.")
    }
}

// Aviso corto abajo de la pantalla (igual que en BotonCatalogoPdf)
private fun mostrarAviso(context: Context, texto: String) {
    Toast.makeText(context, texto, Toast.LENGTH_LONG).show()
}
