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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.material.icons.filled.Thermostat

// Pantalla de perfil del usuario con datos del taller, intereses, clima y panel de rol
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    onEditarDatos: () -> Unit,
    onIrAEncuesta: () -> Unit,
    onCerrarSesion: () -> Unit,
    onAbrirPanel: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    // Recarga el perfil al volver de edición o encuesta
    LaunchedEffect(Unit) {
        viewModel.cargarPerfil()
    }

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            TopAppBar(
                title = { Text(text = "Mi perfil", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdeItaliano,
                    titleContentColor = BlancoPuro
                )
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
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
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
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
                Text(
                    text = "Rol: ${uiState.rol}",
                    fontSize = 16.sp,
                    color = TextoOscuro
                )
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

            if (uiState.opcionesPanel.isNotEmpty()) {
                TarjetaPerfil(titulo = "Panel de trabajo") {
                    uiState.opcionesPanel.forEach { opcion ->
                        BotonPanel(
                            titulo = opcion.titulo,
                            descripcion = opcion.descripcion,
                            onClick = { onAbrirPanel(opcion.ruta) }
                        )
                    }
                }
            }

            if (uiState.condicionesTaller.isNotEmpty()) {
                TarjetaPerfil(titulo = "Condiciones del taller hoy") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Thermostat,
                            contentDescription = null,
                            tint = DoradoCalidad,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.condicionesTaller,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextoOscuro
                        )
                    }
                }
            }

            TarjetaPerfil(titulo = "Datos del negocio") {
                FilaDato(icono = Icons.Filled.Badge, etiqueta = "RUT", valor = uiState.rut)
                FilaDato(icono = Icons.Filled.Email, etiqueta = "Correo", valor = uiState.correo)
                FilaDato(icono = Icons.Filled.Phone, etiqueta = "Celular", valor = uiState.telefono)
                FilaDato(icono = Icons.Filled.Groups, etiqueta = "Personas en el negocio", valor = uiState.personasNegocio)
            }

            TarjetaPerfil(titulo = "Mis intereses") {
                if (uiState.respondioEncuesta) {
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = RojoItaliano,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
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

    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            title = { Text(text = "Cerrar sesión", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "¿Quieres cerrar sesión? Para volver a entrar necesitarás tu RUT o correo y tu contraseña.",
                    fontSize = 17.sp
                )
            },
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

@Composable
private fun BotonPanel(
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = VerdeItaliano.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, VerdeItaliano),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VerdeOscuro)
                Text(text = descripcion, fontSize = 16.sp, color = TextoOscuro)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = VerdeOscuro
            )
        }
    }
}

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
            Text(
                text = if (valor.isBlank()) "Sin registrar" else valor,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoOscuro
            )
        }
    }
}
