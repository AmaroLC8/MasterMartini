package com.example.mastermartini.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.viewmodel.RegistroViewModel
import androidx.compose.ui.res.painterResource
import com.example.mastermartini.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel,
    onRegistroCompletado: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Colores corporativos Master Martini (Estilo bandera italiana)
    val verdeItaliano = Color(0xFF008C45)
    val rojoItaliano = Color(0xFFCD212A)

    // RECURSO NATIVO 1: Cámara fotográfica mediante ActivityResultLauncher (IE 2.4.1)
    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { foto ->
        if (foto != null) {
            fotoBitmap = foto
            viewModel.registrarFotoCapturada()
        }
    }

    // RECURSO NATIVO 2: Motor de vibración física del celular (IE 2.4.1)
    fun ejecutarVibracion() {
        val vibrador = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        @Suppress("DEPRECATION")
        vibrador?.vibrate(100) // Pulso háptico de 100 ms al validar
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Master Martini Academia B2B",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_master_martini),
                contentDescription = "Logo Master Martini",
                modifier = Modifier
                    .width(220.dp)
                    .height(70.dp)
                    .padding(bottom = 6.dp)
            )

            // Detalle tricolor de la bandera italiana (corporativo Master Martini)
            Row(
                modifier = Modifier
                    .width(120.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(verdeItaliano))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.White))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(rojoItaliano))
            }

            Text(
                text = "Registro de Emprendedor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ingresa los datos de tu taller para acceder a recetas técnicas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // PREVISUALIZACIÓN DE FOTO TOMADA CON LA CÁMARA NATIVA
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (fotoBitmap != null) {
                    Image(
                        bitmap = fotoBitmap!!.asImageBitmap(),
                        contentDescription = "Foto capturada",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("📷", fontSize = 36.sp)
                }
            }

            // BOTÓN DE ACCESO NATIVO A LA CÁMARA
            OutlinedButton(
                onClick = { camaraLauncher.launch(null) },
                modifier = Modifier.height(48.dp)
            ) {
                Text("Tomar foto del local")
            }

            // CAMPO 1: NOMBRE DEL LOCAL O DUEÑO
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = { viewModel.onNombreChange(it) },
                label = { Text("Nombre del Local o Dueño") },
                isError = uiState.errorNombre != null,
                supportingText = {
                    // ANIMACIÓN (IE 2.2.2): Transición suave para el mensaje de error
                    AnimatedVisibility(visible = uiState.errorNombre != null) {
                        Text(uiState.errorNombre ?: "", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // CAMPO 2: RUT EMPRESA (SINTÉTICO)
            OutlinedTextField(
                value = uiState.rut,
                onValueChange = { viewModel.onRutChange(it) },
                label = { Text("RUT Empresa (Ej: 12345678-9)") },
                isError = uiState.errorRut != null,
                supportingText = {
                    AnimatedVisibility(visible = uiState.errorRut != null) {
                        Text(uiState.errorRut ?: "", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // CAMPO 3: CORREO ELECTRÓNICO
            OutlinedTextField(
                value = uiState.correo,
                onValueChange = { viewModel.onCorreoChange(it) },
                label = { Text("Correo Electrónico de Contacto") },
                isError = uiState.errorCorreo != null,
                supportingText = {
                    AnimatedVisibility(visible = uiState.errorCorreo != null) {
                        Text(uiState.errorCorreo ?: "", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // SELECTOR DE ESPECIALIDAD TÉCNICA (FilterChips)
            Text(
                text = "Especialidad Técnica:",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Panadería", "Pastelería", "Chocolatería").forEach { rubroOpcion ->
                    FilterChip(
                        selected = uiState.rubro == rubroOpcion,
                        onClick = { viewModel.onRubroChange(rubroOpcion) },
                        label = { Text(rubroOpcion) }
                    )
                }
            }

            // ANIMACIÓN FUNCIONAL: Banner reactivo cuando todos los datos son válidos
            AnimatedVisibility(
                visible = uiState.esFormularioValido,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ Formulario validado correctamente",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // BOTÓN PRINCIPAL (Altura 54dp para accesibilidad de mayores de 50 años)
            Button(
                onClick = {
                    ejecutarVibracion() // Dispara la vibración física
                    viewModel.guardarPerfil {
                        onRegistroCompletado() // Navega hacia HomeScreen
                    }
                },
                enabled = uiState.esFormularioValido,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.esFormularioValido) verdeItaliano else Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("REGISTRAR Y ACCEDER", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}