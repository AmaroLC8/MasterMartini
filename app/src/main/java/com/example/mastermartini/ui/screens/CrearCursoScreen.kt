package com.example.mastermartini.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.components.ChipOpcion
import com.example.mastermartini.ui.components.IconoValidacion
import com.example.mastermartini.ui.components.TarjetaCursoNoExiste
import com.example.mastermartini.ui.components.TarjetaError
import com.example.mastermartini.ui.theme.*
import com.example.mastermartini.viewmodel.AVISO_AVANCE_ALUMNOS
import com.example.mastermartini.viewmodel.AccionPendiente
import com.example.mastermartini.viewmodel.ClaveError
import com.example.mastermartini.viewmodel.CrearCursoUiState
import com.example.mastermartini.viewmodel.CrearCursoViewModel
import com.example.mastermartini.viewmodel.LeccionEditable
import com.example.mastermartini.viewmodel.ModuloEditable
import com.example.mastermartini.viewmodel.formatearMinutos

// Formulario para crear o editar un curso online con sus módulos, lecciones y videos
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearCursoScreen(
    viewModel: CrearCursoViewModel,
    onVolver: () -> Unit,
    capacitacionId: Int? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Lección que pidió elegir un video (rememberSaveable la guarda al girar la pantalla)
    var moduloEligiendoVideo by rememberSaveable { mutableIntStateOf(-1) }
    var leccionEligiendoVideo by rememberSaveable { mutableIntStateOf(-1) }

    // Selector de videos del sistema para el video de presentación del curso
    val selectorVideoCurso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.subirVideoCurso(context, uri)
        }
    }

    // Selector de videos del sistema para la lección que se eligió
    val selectorVideoLeccion = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && leccionEligiendoVideo >= 0) {
            viewModel.subirVideoLeccion(context, uri, moduloEligiendoVideo, leccionEligiendoVideo)
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
                CampoConError(
                    valor = uiState.titulo,
                    onCambio = viewModel::onTituloChange,
                    etiqueta = "Título del curso",
                    icono = Icons.Filled.Title,
                    clave = ClaveError.TITULO,
                    uiState = uiState
                )

                CampoConError(
                    valor = uiState.descripcion,
                    onCambio = viewModel::onDescripcionChange,
                    etiqueta = "Descripción",
                    icono = Icons.Filled.Description,
                    clave = ClaveError.DESCRIPCION,
                    uiState = uiState,
                    minLines = 2
                )

                CampoConError(
                    valor = uiState.insumoPrincipal,
                    onCambio = viewModel::onInsumoChange,
                    etiqueta = "Insumo Master Martini (ej: Chocolate Ariba)",
                    icono = Icons.Filled.Inventory2,
                    clave = ClaveError.INSUMO,
                    uiState = uiState
                )

                TarjetaVideoCurso(
                    uiState = uiState,
                    onElegirVideo = {
                        selectorVideoCurso.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }
                )

                Text("Categoría", fontWeight = FontWeight.Bold, color = TextoOscuro)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .irAlError(ClaveError.CATEGORIA, uiState)
                ) {
                    viewModel.categorias.forEach { cat ->
                        ChipOpcion(
                            texto = cat,
                            seleccionado = uiState.categoria == cat,
                            onClick = { viewModel.onCategoriaChange(cat) }
                        )
                    }
                }
                TextoError(uiState.errores[ClaveError.CATEGORIA])

                Text("Nivel", fontWeight = FontWeight.Bold, color = TextoOscuro)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.irAlError(ClaveError.NIVEL, uiState)
                ) {
                    viewModel.niveles.forEach { niv ->
                        ChipOpcion(
                            texto = niv,
                            seleccionado = uiState.nivel == niv,
                            onClick = { viewModel.onNivelChange(niv) }
                        )
                    }
                }
                TextoError(uiState.errores[ClaveError.NIVEL])

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

                HorizontalDivider(color = GrisBorde)

                SeccionModulos(
                    uiState = uiState,
                    viewModel = viewModel,
                    onElegirVideoLeccion = { idModulo, idLeccion ->
                        moduloEligiendoVideo = idModulo
                        leccionEligiendoVideo = idLeccion
                        selectorVideoLeccion.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }
                )

                if (uiState.mensajeError != null) {
                    Text(
                        text = uiState.mensajeError!!,
                        color = RojoItaliano,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                BotonGuardar(uiState = uiState, onClick = viewModel::publicarCurso)
            }
        }
    }

    // Confirmación de eliminar o reordenar; al editar incluye el aviso para los alumnos
    val accion = uiState.accionPendiente
    if (accion != null) {
        DialogoConfirmacion(
            accion = accion,
            uiState = uiState,
            onConfirmar = viewModel::confirmarAccion,
            onCancelar = viewModel::cancelarAccion
        )
    }
}

// Colores de los campos de texto, iguales a los del registro
@Composable
private fun coloresCampo() = OutlinedTextFieldDefaults.colors(
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

// Si este campo es el primer error, lleva la pantalla hasta él cada vez que se intenta guardar
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Modifier.irAlError(clave: String, uiState: CrearCursoUiState): Modifier {
    val requester = remember { BringIntoViewRequester() }
    LaunchedEffect(uiState.pedidoIrAlError) {
        if (uiState.pedidoIrAlError > 0 && uiState.primerError == clave) {
            requester.bringIntoView()
        }
    }
    return this.bringIntoViewRequester(requester)
}

// Campo de texto con ícono, ✓/⚠ de validación y mensaje de error debajo (mismo estilo del registro)
@Composable
private fun CampoConError(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    clave: String,
    uiState: CrearCursoUiState,
    minLines: Int = 1
) {
    val error = uiState.errores[clave]
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = { IconoValidacion(valor = valor, error = error) },
        isError = error != null,
        colors = coloresCampo(),
        minLines = minLines,
        supportingText = {
            AnimatedVisibility(visible = error != null) {
                Text(error ?: "", color = RojoItaliano)
            }
        },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .irAlError(clave, uiState)
    )
}

// Mensaje de error suelto (para chips o listas), con ícono de advertencia
@Composable
private fun TextoError(error: String?) {
    AnimatedVisibility(visible = error != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = RojoItaliano, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(text = error ?: "", color = RojoItaliano, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// Indicador de que se está subiendo un video a Cloudinary
@Composable
private fun SubiendoVideo() {
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

// Mensaje verde cuando el video ya tiene enlace
@Composable
private fun VideoListo(texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = VerdeItaliano,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = texto, color = VerdeItaliano, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

// Botón verde de subir video (56 dp de alto)
@Composable
private fun BotonSubirVideo(texto: String, habilitado: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        colors = ButtonDefaults.buttonColors(containerColor = VerdeOscuro, contentColor = BlancoPuro),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    ) {
        Icon(imageVector = Icons.Filled.VideoLibrary, contentDescription = null, tint = BlancoPuro)
        Spacer(Modifier.width(8.dp))
        Text(text = texto, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

// Card del video de presentación del curso (opcional)
@Composable
private fun TarjetaVideoCurso(uiState: CrearCursoUiState, onElegirVideo: () -> Unit) {
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
                text = "Video de presentación (opcional)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )

            BotonSubirVideo(
                texto = if (uiState.videoUrl.isNotBlank()) "Cambiar video" else "Subir video desde el teléfono",
                habilitado = !uiState.subiendoVideo,
                onClick = onElegirVideo
            )

            if (uiState.subiendoVideo) {
                SubiendoVideo()
            }

            TextoError(uiState.errorVideo)

            if (uiState.videoUrl.isNotBlank()) {
                VideoListo("Video cargado en la nube")
                key(uiState.videoUrl) {
                    ReproductorVideoLocal(videoUrl = uiState.videoUrl)
                }
            }
        }
    }
}

// Sección "Módulos y lecciones": duración total, una Card por módulo y botón para agregar
@Composable
private fun SeccionModulos(
    uiState: CrearCursoUiState,
    viewModel: CrearCursoViewModel,
    onElegirVideoLeccion: (idModulo: Int, idLeccion: Int) -> Unit
) {
    Text(
        text = "Módulos y lecciones",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = TextoOscuro
    )

    // Duración total calculada sola con los minutos de cada lección
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Schedule, contentDescription = null, tint = VerdeItaliano)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Duración total: ${uiState.duracionTotalTexto}",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextoOscuro
        )
    }

    Box(modifier = Modifier.irAlError(ClaveError.MODULOS, uiState)) {
        TextoError(uiState.errores[ClaveError.MODULOS])
    }

    uiState.modulos.forEachIndexed { indice, modulo ->
        // key mantiene el estado de cada Card ligado a su módulo aunque cambie el orden
        key(modulo.idLocal) {
            TarjetaModulo(
                modulo = modulo,
                numero = indice + 1,
                esPrimero = indice == 0,
                esUltimo = indice == uiState.modulos.lastIndex,
                uiState = uiState,
                viewModel = viewModel,
                onElegirVideoLeccion = onElegirVideoLeccion
            )
        }
    }

    OutlinedButton(
        onClick = viewModel::agregarModulo,
        border = BorderStroke(2.dp, VerdeItaliano),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, tint = VerdeItaliano)
        Spacer(Modifier.width(8.dp))
        Text("Agregar módulo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VerdeItaliano)
    }
}

// Card desplegable de un módulo con su título, sus lecciones y los botones de orden y eliminar
@Composable
private fun TarjetaModulo(
    modulo: ModuloEditable,
    numero: Int,
    esPrimero: Boolean,
    esUltimo: Boolean,
    uiState: CrearCursoUiState,
    viewModel: CrearCursoViewModel,
    onElegirVideoLeccion: (idModulo: Int, idLeccion: Int) -> Unit
) {
    val tieneErrores = modulo.clavesDeError().any { it in uiState.errores }
    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (tieneErrores) BorderStroke(2.dp, RojoItaliano) else null,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Encabezado: al tocarlo se abre o se cierra el módulo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.alternarModulo(modulo.idLocal) }
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = VerdeItaliano)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Módulo $numero: ${modulo.titulo.ifBlank { "Sin título" }}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
                Text(
                    text = "${textoCantidadLecciones(modulo.lecciones.size)} · ${formatoMinutosModulo(modulo)}",
                    fontSize = 15.sp,
                    color = TextoOscuro.copy(alpha = 0.75f)
                )
                if (tieneErrores) {
                    Text("Tiene datos por corregir", fontSize = 15.sp, color = RojoItaliano, fontWeight = FontWeight.SemiBold)
                }
            }
            Icon(
                imageVector = if (modulo.expandido) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (modulo.expandido) "Cerrar módulo" else "Abrir módulo",
                tint = TextoOscuro
            )
        }

        AnimatedVisibility(visible = modulo.expandido) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                CampoConError(
                    valor = modulo.titulo,
                    onCambio = { viewModel.onTituloModuloChange(modulo.idLocal, it) },
                    etiqueta = "Título del módulo $numero",
                    icono = Icons.Filled.Title,
                    clave = ClaveError.tituloModulo(modulo.idLocal),
                    uiState = uiState
                )

                val claveLecciones = ClaveError.leccionesModulo(modulo.idLocal)
                Box(modifier = Modifier.irAlError(claveLecciones, uiState)) {
                    TextoError(uiState.errores[claveLecciones])
                }

                modulo.lecciones.forEachIndexed { indice, leccion ->
                    key(leccion.idLocal) {
                        TarjetaLeccion(
                            leccion = leccion,
                            numero = indice + 1,
                            esPrimera = indice == 0,
                            esUltima = indice == modulo.lecciones.lastIndex,
                            idModulo = modulo.idLocal,
                            uiState = uiState,
                            viewModel = viewModel,
                            onElegirVideo = { onElegirVideoLeccion(modulo.idLocal, leccion.idLocal) }
                        )
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.agregarLeccion(modulo.idLocal) },
                    border = BorderStroke(1.dp, VerdeItaliano),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = VerdeItaliano)
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar lección", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VerdeItaliano)
                }

                BotonesOrden(
                    textoEliminar = "Eliminar módulo",
                    puedeSubir = !esPrimero,
                    puedeBajar = !esUltimo,
                    onSubir = { viewModel.moverModulo(modulo.idLocal, haciaArriba = true) },
                    onBajar = { viewModel.moverModulo(modulo.idLocal, haciaArriba = false) },
                    onEliminar = { viewModel.pedirEliminarModulo(modulo.idLocal) }
                )
            }
        }
    }
}

// Minutos del módulo con Modulo.totalMinutos, por ejemplo "1 h 5 min"
private fun formatoMinutosModulo(modulo: ModuloEditable): String {
    return formatearMinutos(modulo.aModulo().totalMinutos)
}

// "1 lección" o "3 lecciones"
private fun textoCantidadLecciones(cantidad: Int): String {
    return if (cantidad == 1) "1 lección" else "$cantidad lecciones"
}

// Bloque de una lección: título, descripción, duración y video (subido o pegado)
@Composable
private fun TarjetaLeccion(
    leccion: LeccionEditable,
    numero: Int,
    esPrimera: Boolean,
    esUltima: Boolean,
    idModulo: Int,
    uiState: CrearCursoUiState,
    viewModel: CrearCursoViewModel,
    onElegirVideo: () -> Unit
) {
    Surface(
        color = BlancoHueso,
        border = BorderStroke(1.dp, GrisBorde),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = VerdeOscuro)
                Spacer(Modifier.width(8.dp))
                Text("Lección $numero", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
            }

            CampoConError(
                valor = leccion.titulo,
                onCambio = { viewModel.onTituloLeccionChange(idModulo, leccion.idLocal, it) },
                etiqueta = "Título de la lección",
                icono = Icons.Filled.Title,
                clave = ClaveError.tituloLeccion(leccion.idLocal),
                uiState = uiState
            )

            CampoConError(
                valor = leccion.descripcion,
                onCambio = { viewModel.onDescripcionLeccionChange(idModulo, leccion.idLocal, it) },
                etiqueta = "Descripción de la lección",
                icono = Icons.Filled.Description,
                clave = ClaveError.descripcionLeccion(leccion.idLocal),
                uiState = uiState,
                minLines = 2
            )

            CampoConError(
                valor = leccion.duracion,
                onCambio = { viewModel.onDuracionLeccionChange(idModulo, leccion.idLocal, it) },
                etiqueta = "Duración en minutos (ej: 15 min)",
                icono = Icons.Filled.Schedule,
                clave = ClaveError.duracionLeccion(leccion.idLocal),
                uiState = uiState
            )

            BotonSubirVideo(
                texto = if (leccion.videoUrl.isNotBlank()) "Cambiar video" else "Subir video de la lección",
                habilitado = !leccion.subiendoVideo,
                onClick = onElegirVideo
            )

            if (leccion.subiendoVideo) {
                SubiendoVideo()
            }

            TextoError(leccion.errorSubida)

            CampoConError(
                valor = leccion.videoUrl,
                onCambio = { viewModel.onVideoLeccionChange(idModulo, leccion.idLocal, it) },
                etiqueta = "O pega el enlace del video (https://...)",
                icono = Icons.Filled.Link,
                clave = ClaveError.videoLeccion(leccion.idLocal),
                uiState = uiState
            )

            val claveVideo = ClaveError.videoLeccion(leccion.idLocal)
            if (leccion.videoUrl.isNotBlank() && uiState.errores[claveVideo] == null && !leccion.subiendoVideo) {
                VideoListo("La lección tiene video")
            }

            BotonesOrden(
                textoEliminar = "Eliminar",
                puedeSubir = !esPrimera,
                puedeBajar = !esUltima,
                onSubir = { viewModel.moverLeccion(idModulo, leccion.idLocal, haciaArriba = true) },
                onBajar = { viewModel.moverLeccion(idModulo, leccion.idLocal, haciaArriba = false) },
                onEliminar = { viewModel.pedirEliminarLeccion(idModulo, leccion.idLocal) }
            )
        }
    }
}

// Botones Subir, Bajar y Eliminar con ícono y texto (48 dp de alto)
@Composable
private fun BotonesOrden(
    textoEliminar: String,
    puedeSubir: Boolean,
    puedeBajar: Boolean,
    onSubir: () -> Unit,
    onBajar: () -> Unit,
    onEliminar: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onSubir,
                enabled = puedeSubir,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = if (puedeSubir) VerdeOscuro else GrisBorde)
                Spacer(Modifier.width(4.dp))
                Text("Subir", fontSize = 15.sp, color = if (puedeSubir) VerdeOscuro else GrisBorde)
            }
            OutlinedButton(
                onClick = onBajar,
                enabled = puedeBajar,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = if (puedeBajar) VerdeOscuro else GrisBorde)
                Spacer(Modifier.width(4.dp))
                Text("Bajar", fontSize = 15.sp, color = if (puedeBajar) VerdeOscuro else GrisBorde)
            }
        }
        OutlinedButton(
            onClick = onEliminar,
            border = BorderStroke(1.dp, RojoItaliano),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null, tint = RojoItaliano)
            Spacer(Modifier.width(6.dp))
            Text(textoEliminar, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = RojoItaliano)
        }
    }
}

// Botón Guardar: si hay errores muestra cuántos son y, al tocarlo, lleva al primero
@Composable
private fun BotonGuardar(uiState: CrearCursoUiState, onClick: () -> Unit) {
    val hayErrores = uiState.cantidadErrores > 0
    Button(
        onClick = onClick,
        enabled = !uiState.cargando && !uiState.hayVideoSubiendo,
        colors = ButtonDefaults.buttonColors(containerColor = if (hayErrores) RojoItaliano else VerdeItaliano),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    ) {
        when {
            uiState.cargando -> CircularProgressIndicator(color = BlancoPuro, modifier = Modifier.size(24.dp))
            hayErrores -> {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = BlancoPuro)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (uiState.cantidadErrores == 1) "Revisar 1 error" else "Revisar ${uiState.cantidadErrores} errores",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            else -> {
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

// AlertDialog para confirmar eliminar o reordenar
@Composable
private fun DialogoConfirmacion(
    accion: AccionPendiente,
    uiState: CrearCursoUiState,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    // Al editar un curso existente, eliminar también muestra el aviso para los alumnos
    val avisoEdicion = if (uiState.esEdicion) "\n\n$AVISO_AVANCE_ALUMNOS" else ""
    val titulo: String
    val mensaje: String
    val textoConfirmar: String
    when (accion) {
        is AccionPendiente.EliminarModulo -> {
            val modulo = uiState.modulos.firstOrNull { it.idLocal == accion.idModulo }
            titulo = "Eliminar módulo"
            mensaje = "¿Eliminar el módulo \"${modulo?.titulo?.ifBlank { "Sin título" }}\" y todas sus lecciones?$avisoEdicion"
            textoConfirmar = "Eliminar"
        }
        is AccionPendiente.EliminarLeccion -> {
            val leccion = uiState.modulos.firstOrNull { it.idLocal == accion.idModulo }
                ?.lecciones?.firstOrNull { it.idLocal == accion.idLeccion }
            titulo = "Eliminar lección"
            mensaje = "¿Eliminar la lección \"${leccion?.titulo?.ifBlank { "Sin título" }}\"?$avisoEdicion"
            textoConfirmar = "Eliminar"
        }
        is AccionPendiente.MoverModulo, is AccionPendiente.MoverLeccion -> {
            titulo = "Cambiar el orden"
            mensaje = "$AVISO_AVANCE_ALUMNOS ¿Quieres continuar?"
            textoConfirmar = "Sí, cambiar orden"
        }
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = RojoItaliano) },
        title = { Text(text = titulo, fontWeight = FontWeight.Bold) },
        text = { Text(text = mensaje, fontSize = 17.sp) },
        confirmButton = {
            TextButton(onClick = onConfirmar, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(text = textoConfirmar, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RojoItaliano)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(text = "Cancelar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VerdeOscuro)
            }
        }
    )
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
