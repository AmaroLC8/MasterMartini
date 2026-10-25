package com.example.mastermartini.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
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
import androidx.core.content.ContextCompat
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

    // Abre la cámara. El try/catch evita que la app se caiga si el
    // dispositivo no tiene una app de cámara instalada.
    fun abrirCamara() {
        try {
            camaraLauncher.launch(null)
        } catch (e: ActivityNotFoundException) {
            viewModel.onCamaraNoDisponible()
        }
    }

    // PERMISO DE CÁMARA EN TIEMPO DE EJECUCIÓN (IE 2.4.1)
    // Desde Android 6 los permisos "peligrosos" como CAMERA se piden mientras la app se usa.
    // Este launcher muestra el diálogo del sistema y nos devuelve true (aceptó) o false (rechazó).
    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            viewModel.onPermisoCamaraDenegado()
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
                onClick = {
                    // Si el permiso ya fue aceptado antes, abrimos la cámara directo;
                    // si no, primero lo pedimos con el diálogo del sistema.
                    val tienePermiso = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (tienePermiso) {
                        abrirCamara()
                    } else {
                        permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.height(48.dp)
            ) {
                Text("Tomar foto del local")
            }

            // AVISO DE CÁMARA: aparece con animación si se negó el permiso o no hay cámara
            AnimatedVisibility(visible = uiState.avisoCamara != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Aviso",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.avisoCamara ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 16.sp
                    )
                }
            }

            // CAMPO 1: NOMBRE DEL LOCAL O DUEÑO
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = { viewModel.onNombreChange(it) },
                label = { Text("Nombre del Local o Dueño") },
                // ÍCONOS (IE 2.1.2): a la izquierda indica qué dato va; a la derecha, si está bien o mal
                leadingIcon = { Icon(Icons.Filled.Store, contentDescription = "Nombre del local") },
                trailingIcon = { IconoValidacion(valor = uiState.nombre, error = uiState.errorNombre) },
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
                leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = "RUT") },
                trailingIcon = { IconoValidacion(valor = uiState.rut, error = uiState.errorRut) },
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
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Correo") },
                trailingIcon = { IconoValidacion(valor = uiState.correo, error = uiState.errorCorreo) },
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

// ÍCONO DE VALIDACIÓN AL FINAL DE CADA CAMPO (IE 2.1.2)
// No valida nada: solo muestra lo que el ViewModel ya decidió en el estado.
// - Si hay mensaje de error  -> ícono de advertencia rojo.
// - Si el campo tiene texto y no hay error -> ícono de check verde.
// - Si el campo está vacío -> no muestra nada (el usuario aún no escribe).
// Se usa en los 3 campos para no repetir el mismo código tres veces.
@Composable
private fun IconoValidacion(valor: String, error: String?) {
    if (error != null) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "Dato con error",
            tint = MaterialTheme.colorScheme.error
        )
    } else if (valor.isNotBlank()) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Dato correcto",
            tint = MaterialTheme.colorScheme.primary // Verde italiano, viene del tema (Theme.kt)
        )
    }
}