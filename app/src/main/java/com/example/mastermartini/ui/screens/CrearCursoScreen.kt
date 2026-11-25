package com.example.mastermartini.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.components.ChipOpcion
import com.example.mastermartini.ui.theme.*
import com.example.mastermartini.viewmodel.CrearCursoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearCursoScreen(
    viewModel: CrearCursoViewModel,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) {
            onVolver()
        }
    }

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            TopAppBar(
                title = { Text("Crear curso online", fontWeight = FontWeight.Bold) },
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

            // Campo destacado para pegar el enlace .mp4 de Cloudinary
            OutlinedTextField(
                value = uiState.videoCloudinaryUrl,
                onValueChange = viewModel::onVideoUrlChange,
                label = { Text("URL video Cloudinary (.mp4)") },
                placeholder = { Text("https://res.cloudinary.com/.../video.mp4") },
                leadingIcon = {
                    Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = VerdeItaliano)
                },
                modifier = Modifier.fillMaxWidth()
            )

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

            if (uiState.mensajeError != null) {
                Text(
                    text = uiState.mensajeError!!,
                    color = RojoItaliano,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = viewModel::publicarCurso,
                enabled = !uiState.cargando,
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
                    Text("Publicar curso", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}