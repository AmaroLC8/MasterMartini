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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mastermartini.R
import com.example.mastermartini.ui.components.IconoValidacion // Ícono ✓/⚠ compartido con LoginScreen
import com.example.mastermartini.ui.theme.*
import com.example.mastermartini.viewmodel.RegistroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel,
    onRegistroCompletado: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // RECURSO NATIVO 1: Cámara fotográfica mediante ActivityResultLauncher
    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { foto ->
        if (foto != null) {
            fotoBitmap = foto
            viewModel.registrarFotoCapturada()
        }
    }

    fun abrirCamara() {
        try {
            camaraLauncher.launch(null)
        } catch (e: ActivityNotFoundException) {
            viewModel.onCamaraNoDisponible()
        }
    }

    // PERMISO DE CÁMARA EN TIEMPO DE EJECUCIÓN
    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            viewModel.onPermisoCamaraDenegado()
        }
    }

    // RECURSO NATIVO 2: Motor de vibración física
    fun ejecutarVibracion() {
        val vibrador = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        @Suppress("DEPRECATION")
        vibrador?.vibrate(100)
    }

    // Estilo unificado para los campos de texto con los colores corporativos
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = VerdeItaliano,
        unfocusedBorderColor = GrisBorde,
        focusedLabelColor = VerdeItaliano,
        unfocusedLabelColor = TextoOscuro.copy(alpha = 0.7f),
        focusedLeadingIconColor = VerdeItaliano,
        unfocusedLeadingIconColor = TextoOscuro.copy(alpha = 0.6f),
        focusedTextColor = TextoOscuro,
        unfocusedTextColor = TextoOscuro,
        cursorColor = VerdeItaliano,
        errorBorderColor = RojoItaliano,
        errorLabelColor = RojoItaliano,
        errorLeadingIconColor = RojoItaliano
    )

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Master Martini Academia B2B",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = BlancoPuro
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdeItaliano,
                    titleContentColor = BlancoPuro
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

            // Detalle tricolor de la bandera italiana
            Row(
                modifier = Modifier
                    .width(120.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(VerdeItaliano))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(BlancoPuro))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(RojoItaliano))
            }

            Text(
                text = "Registro de Emprendedor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
            Text(
                text = "Ingresa los datos de tu taller para acceder a recetas técnicas.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoOscuro.copy(alpha = 0.7f)
            )

            // PREVISUALIZACIÓN DE FOTO
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(BlancoPuro)
                    .border(2.dp, DoradoCalidad, CircleShape)
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

            // BOTÓN CÁMARA
            OutlinedButton(
                onClick = {
                    val tienePermiso = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (tienePermiso) {
                        abrirCamara()
                    } else {
                        permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = VerdeItaliano
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(VerdeItaliano)
                ),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Tomar foto del local", fontWeight = FontWeight.SemiBold)
            }

            // AVISO DE CÁMARA
            AnimatedVisibility(visible = uiState.avisoCamara != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Aviso",
                        tint = RojoItaliano
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.avisoCamara ?: "",
                        color = RojoItaliano,
                        fontSize = 14.sp
                    )
                }
            }

            // CAMPO 1: NOMBRE
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = { viewModel.onNombreChange(it) },
                label = { Text("Nombre del Local o Dueño") },
                leadingIcon = { Icon(Icons.Filled.Store, contentDescription = "Nombre del local") },
                trailingIcon = { IconoValidacion(valor = uiState.nombre, error = uiState.errorNombre) },
                isError = uiState.errorNombre != null,
                colors = textFieldColors,
                supportingText = {
                    AnimatedVisibility(visible = uiState.errorNombre != null) {
                        Text(uiState.errorNombre ?: "", color = RojoItaliano)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // CAMPO 2: RUT
            OutlinedTextField(
                value = uiState.rut,
                onValueChange = { viewModel.onRutChange(it) },
                label = { Text("RUT Empresa (Ej: 12345678-5)") },
                leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = "RUT") },
                trailingIcon = { IconoValidacion(valor = uiState.rut, error = uiState.errorRut) },
                isError = uiState.errorRut != null,
                colors = textFieldColors,
                supportingText = {
                    AnimatedVisibility(visible = uiState.errorRut != null) {
                        Text(uiState.errorRut ?: "", color = RojoItaliano)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // CAMPO 3: CORREO
            OutlinedTextField(
                value = uiState.correo,
                onValueChange = { viewModel.onCorreoChange(it) },
                label = { Text("Correo Electrónico de Contacto") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Correo") },
                trailingIcon = { IconoValidacion(valor = uiState.correo, error = uiState.errorCorreo) },
                isError = uiState.errorCorreo != null,
                colors = textFieldColors,
                supportingText = {
                    AnimatedVisibility(visible = uiState.errorCorreo != null) {
                        Text(uiState.errorCorreo ?: "", color = RojoItaliano)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // CAMPO CONTRASENA EN REGISTRO
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Crea una Contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Contraseña") },
                trailingIcon = { IconoValidacion(valor = uiState.password, error = uiState.errorPassword) },
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.errorPassword != null,
                supportingText = {
                    AnimatedVisibility(visible = uiState.errorPassword != null) {
                        Text(uiState.errorPassword ?: "", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // CAMPO 4: TELÉFONO
            OutlinedTextField(
                value = uiState.telefono,
                onValueChange = { viewModel.onTelefonoChange(it) },
                label = { Text("Celular (Ej: 9 1234 5678)") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Teléfono") },
                trailingIcon = { IconoValidacion(valor = uiState.telefono, error = uiState.errorTelefono) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = uiState.errorTelefono != null,
                colors = textFieldColors,
                supportingText = {
                    AnimatedVisibility(visible = uiState.errorTelefono != null) {
                        Text(uiState.errorTelefono ?: "", color = RojoItaliano)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // SELECTOR DE ESPECIALIDAD TÉCNICA
            Text(
                text = "Especialidad Técnica:",
                fontWeight = FontWeight.SemiBold,
                color = TextoOscuro,
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
                        label = { Text(rubroOpcion) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VerdeItaliano,
                            selectedLabelColor = BlancoPuro,
                            containerColor = BlancoPuro,
                            labelColor = TextoOscuro
                        )
                    )
                }
            }

            // SELECTOR DE TAMAÑO DEL NEGOCIO
            Text(
                text = "Tamaño del Negocio:",
                fontWeight = FontWeight.SemiBold,
                color = TextoOscuro,
                modifier = Modifier.align(Alignment.Start)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Micro", "Pequeño", "Mediano").forEach { tamanoOpcion ->
                    FilterChip(
                        selected = uiState.tamanoNegocio == tamanoOpcion,
                        onClick = { viewModel.onTamanoNegocioChange(tamanoOpcion) },
                        label = { Text(tamanoOpcion) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VerdeItaliano,
                            selectedLabelColor = BlancoPuro,
                            containerColor = BlancoPuro,
                            labelColor = TextoOscuro
                        )
                    )
                }
            }

            // BANNER DE VALIDACIÓN
            AnimatedVisibility(
                visible = uiState.esFormularioValido,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = VerdeItaliano.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ Formulario validado correctamente",
                        modifier = Modifier.padding(12.dp),
                        color = VerdeOscuro,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // BOTÓN PRINCIPAL
            Button(
                onClick = {
                    ejecutarVibracion()
                    viewModel.guardarPerfil {
                        onRegistroCompletado()
                    }
                },
                enabled = uiState.esFormularioValido,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeItaliano,
                    contentColor = BlancoPuro,
                    disabledContainerColor = GrisBorde,
                    disabledContentColor = TextoOscuro.copy(alpha = 0.4f)
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