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

// PANTALLA "CREAR CURSO ONLINE" / "EDITAR CURSO ONLINE" (la abre un Instructor o Administrador)
// Formulario para publicar un curso nuevo en el microservicio de cursos, con el enlace
// del video subido a Cloudinary. La MISMA pantalla sirve para editar un curso que ya existe:
// si llega un capacitacionId, se abre en modo edición con los datos del curso.
// Solo DIBUJA lo que le entrega CrearCursoViewModel (MVVM):
// las validaciones, el envío y los mensajes de error se deciden en el ViewModel.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearCursoScreen(
    // ViewModel con lo escrito en el formulario (lo crea MainActivity)
    viewModel: CrearCursoViewModel,
    // Flecha de volver y también al guardar con éxito: MainActivity vuelve atrás (popBackStack)
    onVolver: () -> Unit,
    // Id del curso a EDITAR (viene de la ruta "panel/editar-curso/{id}").
    // null (valor por defecto) = se está creando un curso nuevo ("panel/crear-curso").
    capacitacionId: Int? = null
) {
    // Escuchamos el estado: cada letra que se escribe genera un estado nuevo y la pantalla se redibuja
    val uiState by viewModel.uiState.collectAsState()

    // MODO EDICIÓN: al abrir la pantalla con un id, se le pide al ViewModel que cargue ese curso
    // y rellene el formulario (si ya lo cargó, no hace nada, ver cargarParaEditar).
    LaunchedEffect(capacitacionId) {
        if (capacitacionId != null) {
            viewModel.cargarParaEditar(capacitacionId)
        }
    }

    // Se ejecuta cada vez que cambia guardadoExitoso. Cuando el servidor guardó el curso
    // (pasa a true), se vuelve atrás sola. Al crear vuelve al Perfil/Cursos (la lista se recarga
    // y aparece el curso nuevo); al editar vuelve al detalle, que se recarga con los datos nuevos.
    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) {
            onVolver()
        }
    }

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            // Barra verde con la flecha para volver (mismo estilo que las demás pantallas)
            TopAppBar(
                // El título cambia según el modo (lo dice el estado del ViewModel)
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
        // verticalScroll: el formulario es largo y en celulares chicos no cabe completo
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // DATOS DEL CURSO. Cada campo muestra lo que tiene el estado (value) y, al escribir,
            // le avisa al ViewModel (onValueChange). viewModel::onTituloChange = "pásale el texto a esa función".
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

            // CATEGORÍA: botones de opción (ChipOpcion, el mismo de la encuesta).
            // La lista viene del ViewModel; horizontalScroll porque las 5 no caben a lo ancho.
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

            // NIVEL: Básico, Intermedio o Avanzado (solo uno queda seleccionado)
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

            // ACCESO: el interruptor elige si el curso es gratis o del Plan Pro.
            // El texto de al lado dice cuál está elegido (no depende solo de la posición del Switch)
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

            // PRIMER MÓDULO Y PRIMERA LECCIÓN (el backend exige al menos uno de cada uno).
            // Solo al CREAR. Al EDITAR se ocultan: los módulos y lecciones del curso se envían
            // tal como estaban (el ViewModel los copia del curso original), y se avisa con un texto.
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

            // Mensaje de error en rojo (lo decide el ViewModel: campos vacíos, sin sesión, 403, 503...)
            if (uiState.mensajeError != null) {
                Text(
                    text = uiState.mensajeError!!,
                    color = RojoItaliano,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // BOTÓN PUBLICAR. Se desactiva mientras se envía (cargando) para no crear el curso dos veces,
            // y en ese tiempo muestra una ruedita en vez del texto.
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