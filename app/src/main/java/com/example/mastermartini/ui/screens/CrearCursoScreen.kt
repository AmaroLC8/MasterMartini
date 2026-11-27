package com.example.mastermartini.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.components.ChipOpcion
import com.example.mastermartini.ui.components.TarjetaCursoNoExiste
import com.example.mastermartini.ui.components.TarjetaError
import com.example.mastermartini.ui.theme.*
import com.example.mastermartini.viewmodel.CrearCursoViewModel

// Formulario para crear o editar un curso online con carga de video a Cloudinary
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearCursoScreen(
    viewModel: CrearCursoViewModel,
    onVolver: () -> Unit,
    capacitacionId: Int? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Selector del sistema para elegir un archivo de video y subirlo a Cloudinary
    val selectorVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.subirVideoCurso(context, uri)
        }
    }

    LaunchedEffect(capacitacionId) {
        if (capacitacionId != null) {
            viewModel.cargarParaEditar(capacitacionId)
        }
    }

    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) {
            onVolver()
        }
    }

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.esEdicion) "Editar curso online" else "Crear curso online",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = BlancoPuro
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdeItaliano,
                    titleContentColor = BlancoPuro
                )
            )
        }
    ) { innerPadding ->
        // Al editar, primero se trae el curso del servidor: carga, 404 o error con Reintentar
        if (uiState.cargandoCurso || uiState.cursoNoExiste || uiState.errorCarga != null) {
            EstadoCargaEdicion(
                cargando = uiState.cargandoCurso,
                cursoNoExiste = uiState.cursoNoExiste,
                errorCarga = uiState.errorCarga,
                onReintentar = { viewModel.reintentarCarga() },
                onVolver = onVolver,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = uiState.titulo,
                    onValueChange = viewModel::onTituloChange,
                    label = { Text("Título del curso") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.descripcion,
                    onValueChange = viewModel::onDescripcionChange,
                    label = { Text("Descripción") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.insumoPrincipal,
                    onValueChange = viewModel::onInsumoChange,
                    label = { Text("Insumo Master Martini (ej: Chocolate Ariba)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.duracionTotal,
                    onValueChange = viewModel::onDuracionChange,
                    label = { Text("Duración total (ej: 1 h 30 min)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = BlancoPuro),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Video del curso",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoOscuro
                        )

                        Button(
                            onClick = { selectorVideoLauncher.launch("video/*") },
                            enabled = !uiState.subiendoVideo,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerdeOscuro,
                                contentColor = BlancoPuro
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VideoLibrary,
                                contentDescription = null,
                                tint = BlancoPuro
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (uiState.videoUrl.isNotBlank()) {
                                    "Cambiar video"
                                } else {
                                    "Subir video desde el teléfono"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (uiState.subiendoVideo) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    color = VerdeItaliano,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Subiendo video a la nube, espera un momento...",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextoOscuro
                                )
                            }
                        }

                        if (uiState.errorVideo != null) {
                            Text(
                                text = uiState.errorVideo!!,
                                color = RojoItaliano,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (uiState.videoUrl.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = VerdeItaliano,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Video cargado en la nube",
                                    color = VerdeItaliano,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            key(uiState.videoUrl) {
                                ReproductorVideoLocal(videoUrl = uiState.videoUrl)
                            }
                        }
                    }
                }

                Text("Categoría", fontWeight = FontWeight.Bold, color = TextoOscuro)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    viewModel.categorias.forEach { cat ->
                        ChipOpcion(
                            texto = cat,
                            seleccionado = uiState.categoria == cat,
                            onClick = { viewModel.onCategoriaChange(cat) }
                        )
                    }
                }

                Text("Nivel", fontWeight = FontWeight.Bold, color = TextoOscuro)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.niveles.forEach { niv ->
                        ChipOpcion(
                            texto = niv,
                            seleccionado = uiState.nivel == niv,
                            onClick = { viewModel.onNivelChange(niv) }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = uiState.esGratis,
                        onCheckedChange = viewModel::onEsGratisChange
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (uiState.esGratis) "Acceso: Curso Gratis" else "Acceso: Plan Pro",
                        fontWeight = FontWeight.SemiBold,
                        color = TextoOscuro
                    )
                }

                if (uiState.esEdicion) {
                    Text(
                        text = "Los módulos y lecciones del curso se mantienen sin cambios",
                        fontSize = 16.sp,
                        color = TextoOscuro
                    )
                } else {
                    OutlinedTextField(
                        value = uiState.tituloModulo,
                        onValueChange = viewModel::onTituloModuloChange,
                        label = { Text("Nombre del Módulo 1") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.tituloLeccion,
                        onValueChange = viewModel::onTituloLeccionChange,
                        label = { Text("Nombre de la Lección 1") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (uiState.mensajeError != null) {
                    Text(
                        text = uiState.mensajeError!!,
                        color = RojoItaliano,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = viewModel::publicarCurso,
                    enabled = !uiState.cargando && !uiState.subiendoVideo,
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeItaliano),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (uiState.cargando) {
                        CircularProgressIndicator(color = BlancoPuro, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = BlancoPuro)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (uiState.esEdicion) "Guardar cambios" else "Publicar curso",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Lo que se ve mientras se trae el curso a editar, o si no se pudo traer
@Composable
private fun EstadoCargaEdicion(
    cargando: Boolean,
    cursoNoExiste: Boolean,
    errorCarga: String?,
    onReintentar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            cargando -> CircularProgressIndicator(color = VerdeItaliano)
            cursoNoExiste -> TarjetaCursoNoExiste(onVolver = onVolver)
            else -> TarjetaError(mensaje = errorCarga ?: "", onReintentar = onReintentar)
        }
    }
}
