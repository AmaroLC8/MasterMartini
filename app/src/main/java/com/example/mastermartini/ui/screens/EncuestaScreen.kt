package com.example.mastermartini.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mastermartini.ui.theme.*
import com.example.mastermartini.viewmodel.EncuestaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncuestaScreen(
    encuestaViewModel: EncuestaViewModel,
    onVolverHome: () -> Unit
) {
    val state by encuestaViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            encuestaViewModel.obtenerUbicacion()
        } else {
            encuestaViewModel.registrarRespuestaSinGps()
        }
    }

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Encuesta de Intereses",
                        fontWeight = FontWeight.Bold,
                        color = BlancoPuro
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolverHome) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = BlancoPuro
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdeItaliano,
                    titleContentColor = BlancoPuro,
                    navigationIconContentColor = BlancoPuro
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Personaliza tus capacitaciones Master Martini",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeOscuro
            )
            Text(
                text = "Indica tus áreas de preferencia y nivel técnico para sugerirte recetas e insumos.",
                fontSize = 14.sp,
                color = TextoOscuro.copy(alpha = 0.7f)
            )

            HorizontalDivider(color = GrisBorde)

            // 1. Áreas de Interés
            Card(
                colors = CardDefaults.cardColors(containerColor = BlancoPuro),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = DoradoCalidad)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1. Áreas de Interés (Selecciona al menos una):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextoOscuro
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    encuestaViewModel.opcionesIntereses.forEach { interes ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = state.interesesSeleccionados.contains(interes),
                                onCheckedChange = { encuestaViewModel.toggleInteres(interes) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = VerdeItaliano,
                                    uncheckedColor = TextoOscuro.copy(alpha = 0.6f)
                                )
                            )
                            Text(
                                text = interes,
                                fontSize = 14.sp,
                                color = TextoOscuro
                            )
                        }
                    }

                    state.errorIntereses?.let { error ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = RojoItaliano,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = error,
                                color = RojoItaliano,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 2. Nivel de Experiencia
            Card(
                colors = CardDefaults.cardColors(containerColor = BlancoPuro),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = DoradoCalidad)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. Nivel de Experiencia Técnica:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextoOscuro
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    encuestaViewModel.opcionesNivel.forEach { nivel ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = state.nivelExperiencia == nivel,
                                onClick = { encuestaViewModel.seleccionarNivel(nivel) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = VerdeItaliano,
                                    unselectedColor = TextoOscuro.copy(alpha = 0.6f)
                                )
                            )
                            Text(
                                text = nivel,
                                fontSize = 14.sp,
                                color = TextoOscuro
                            )
                        }
                    }

                    state.errorNivel?.let { error ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = RojoItaliano,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = error,
                                color = RojoItaliano,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 3. GPS Nativo
            Card(
                colors = CardDefaults.cardColors(containerColor = BlancoPuro),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = VerdeItaliano
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ubicación del Taller / Negocio (GPS Nativo)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextoOscuro
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val hasFine = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            val hasCoarse = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasFine || hasCoarse) {
                                encuestaViewModel.obtenerUbicacion()
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeItaliano,
                            contentColor = BlancoPuro
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Usar mi ubicación actual", fontWeight = FontWeight.SemiBold)
                    }

                    if (state.cargandoUbicacion) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = VerdeItaliano,
                            trackColor = GrisBorde
                        )
                    }

                    if (state.ubicacionGps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "📍 ${state.ubicacionGps}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VerdeOscuro
                        )
                    }

                    // AVISO DEL GPS (sin permiso o sin señal). Igual que el aviso de la cámara
                    // en el registro: aparece con animación y desaparece al volver a intentar.
                    AnimatedVisibility(visible = state.avisoGps != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Aviso",
                                tint = RojoItaliano
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.avisoGps ?: "",
                                color = RojoItaliano,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Mensaje de éxito
            state.mensajeExito?.let { mensaje ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = VerdeItaliano.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = VerdeOscuro
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = mensaje,
                            color = VerdeOscuro,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Botón Guardar
            Button(
                onClick = {
                    encuestaViewModel.guardarRespuesta()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeItaliano,
                    contentColor = BlancoPuro
                )
            ) {
                Text(
                    text = "GUARDAR PREFERENCIAS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}