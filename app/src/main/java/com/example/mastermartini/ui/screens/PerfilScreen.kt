package com.example.mastermartini.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.PerfilViewModel

// PANTALLA DE PERFIL
// Muestra los datos del emprendedor: foto, datos del negocio, encuesta y favoritos.
// Solo DIBUJA lo que le entrega el PerfilViewModel (MVVM) y avisa a MainActivity
// cuando se toca un botón (MainActivity decide a dónde navegar).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    // ViewModel con los datos guardados (lo crea MainActivity)
    viewModel: PerfilViewModel,
    // Botón "Editar datos": MainActivity abre "registro" con el formulario precargado
    onEditarDatos: () -> Unit,
    // Botón "Responder encuesta": MainActivity abre "encuesta"
    onIrAEncuesta: () -> Unit,
    // Confirmación de "Cerrar sesión": MainActivity vuelve al login y borra la pila
    onCerrarSesion: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // ¿Se está mostrando el diálogo "¿Quieres cerrar sesión?"?
    // Es estado SOLO visual (abrir/cerrar un diálogo), por eso puede vivir en la pantalla.
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    // LaunchedEffect(Unit) se ejecuta cada vez que esta pantalla ENTRA a la composición.
    // Al ir a "Editar datos" la pantalla Perfil sale, y al volver entra de nuevo:
    // así se vuelve a leer lo guardado y se ven los datos cambiados.
    LaunchedEffect(Unit) {
        viewModel.cargarPerfil()
    }

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            // Barra verde con el título (sin flecha: se llega desde la barra inferior)
            TopAppBar(
                title = { Text(text = "Mi perfil", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdeItaliano,
                    titleContentColor = BlancoPuro
                )
            )
        }
    ) { innerPadding ->
        // verticalScroll: en celulares chicos el contenido no cabe, así se puede bajar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. ENCABEZADO: foto, nombre y rubro
            // Se copia a una variable local para poder revisar si es null y usarla sin "!!"
            val foto = uiState.foto
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(BlancoPuro)
                    .border(3.dp, DoradoCalidad, CircleShape)
            ) {
                if (foto != null) {
                    Image(
                        bitmap = foto.asImageBitmap(),
                        contentDescription = "Foto del local",
                        // Crop: llena todo el círculo sin deformar la foto (igual que en Registro)
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Sin foto: ícono de persona. Decorativo, por eso contentDescription = null
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = VerdeItaliano,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.nombre,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
                Text(
                    text = uiState.rubro,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VerdeOscuro
                )
                // Rol de la cuenta (ej. "Cliente", "Ventas"). Viene de la sesión guardada
                Text(
                    text = "Rol: ${uiState.rol}",
                    fontSize = 16.sp,
                    color = TextoOscuro
                )
                // Etiqueta "Plan Pro activo" solo si tiene la suscripción.
                // Dorado con texto oscuro (mismo estilo que "★ PLAN PRO" en Cursos): dice con TEXTO
                // que está activo, no depende solo del color.
                if (uiState.planPro) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "★ Plan Pro activo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro,
                        modifier = Modifier
                            .background(DoradoCalidad, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // 2. TARJETA "DATOS DEL NEGOCIO"
            TarjetaPerfil(titulo = "Datos del negocio") {
                FilaDato(icono = Icons.Filled.Badge, etiqueta = "RUT", valor = uiState.rut)
                FilaDato(icono = Icons.Filled.Email, etiqueta = "Correo", valor = uiState.correo)
                FilaDato(icono = Icons.Filled.Phone, etiqueta = "Celular", valor = uiState.telefono)
                FilaDato(icono = Icons.Filled.Groups, etiqueta = "Personas en el negocio", valor = uiState.personasNegocio)
            }

            // 3. TARJETA "MIS INTERESES" (datos de la encuesta)
            TarjetaPerfil(titulo = "Mis intereses") {
                if (uiState.respondioEncuesta) {
                    // Los intereses como "pastillas" verdes. FlowRow las pasa a la línea
                    // de abajo si no caben. Son solo para leer, por eso no se pueden tocar.
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.intereses.forEach { interes ->
                            Text(
                                text = interes,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlancoPuro,
                                modifier = Modifier
                                    .background(VerdeItaliano, RoundedCornerShape(50))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                    FilaDato(icono = Icons.Filled.School, etiqueta = "Nivel", valor = uiState.nivel)
                    FilaDato(icono = Icons.Filled.LocationOn, etiqueta = "Ubicación", valor = uiState.ubicacion)
                } else {
                    // Todavía no responde la encuesta: se explica y se ofrece el botón
                    Text(
                        text = "Aún no respondes la encuesta",
                        fontSize = 16.sp,
                        color = TextoOscuro
                    )
                    OutlinedButton(
                        onClick = onIrAEncuesta,
                        border = BorderStroke(2.dp, VerdeItaliano),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeOscuro),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Quiz, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Responder encuesta", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 4. CANTIDAD DE FAVORITOS (viene de Room y se actualiza sola)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Decorativo: el texto de al lado ya dice lo que significa
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = RojoItaliano,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                // "1 receta favorita" en singular, "3 recetas favoritas" en plural
                Text(
                    text = if (uiState.totalFavoritos == 1) {
                        "1 receta favorita"
                    } else {
                        "${uiState.totalFavoritos} recetas favoritas"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
            }

            // 5. BOTONES
            // Botón principal (verde y relleno): la acción más común
            Button(
                onClick = onEditarDatos,
                colors = ButtonDefaults.buttonColors(containerColor = VerdeItaliano),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Editar datos", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // Botón secundario (solo borde rojo): se distingue del principal para no confundirlos.
            // No cierra la sesión directo: primero abre el diálogo de confirmación.
            OutlinedButton(
                onClick = { mostrarDialogoCerrarSesion = true },
                border = BorderStroke(2.dp, RojoItaliano),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RojoItaliano),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Cerrar sesión", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // DIÁLOGO DE CONFIRMACIÓN: evita cerrar la sesión por un toque accidental.
    // onDismissRequest: se llama al tocar fuera del diálogo o con Atrás (= cancelar).
    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            title = { Text(text = "Cerrar sesión", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    // Al cerrar sesión se borran los datos de la cuenta del celular
                    // (AlmacenamientoLocal.cerrarSesion), por eso se avisa qué se necesita para volver
                    text = "¿Quieres cerrar sesión? Para volver a entrar necesitarás tu RUT o correo y tu contraseña.",
                    fontSize = 17.sp
                )
            },
            // Botón que SÍ cierra la sesión (en rojo, porque es la acción "peligrosa")
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoCerrarSesion = false
                        onCerrarSesion()
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(text = "Cerrar sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RojoItaliano)
                }
            },
            // Botón que se arrepiente: solo cierra el diálogo
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogoCerrarSesion = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(text = "Cancelar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VerdeOscuro)
                }
            }
        )
    }
}

// TARJETA BLANCA CON TÍTULO, que usan "Datos del negocio" y "Mis intereses".
// contenido: @Composable () -> Unit recibe lo que va dentro (como hace Card o Column),
// así las dos tarjetas comparten el mismo borde, colores y título sin repetir código.
@Composable
private fun TarjetaPerfil(
    titulo: String,
    contenido: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        border = BorderStroke(1.dp, DoradoCalidad.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = titulo,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeOscuro
            )
            contenido()
        }
    }
}

// UNA FILA DE DATO: ícono + etiqueta pequeña arriba + valor grande abajo.
// Ej: [📛] RUT
//          12345678-5
@Composable
private fun FilaDato(
    icono: ImageVector,
    etiqueta: String,
    valor: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        // Decorativo: la etiqueta de al lado ya dice qué dato es
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = VerdeItaliano,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = etiqueta,
                fontSize = 14.sp,
                color = TextoOscuro.copy(alpha = 0.7f)
            )
            // Si el dato está vacío (ej. no se obtuvo la ubicación), se dice en vez de dejar un hueco
            Text(
                text = if (valor.isBlank()) "Sin registrar" else valor,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoOscuro
            )
        }
    }
}
