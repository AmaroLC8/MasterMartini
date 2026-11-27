package com.example.mastermartini.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.data.DatosEmpresa
import com.example.mastermartini.model.Leccion
import com.example.mastermartini.model.Modulo
import com.example.mastermartini.model.Pregunta
import com.example.mastermartini.ui.components.DatoConIcono
import com.example.mastermartini.ui.components.ErrorConReintentar
import com.example.mastermartini.ui.components.EtiquetaAcceso
import com.example.mastermartini.ui.components.EtiquetaVegana
import com.example.mastermartini.ui.components.IconoValidacion
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.GrisBorde
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.CapacitacionDetalleViewModel

// Pantalla de detalle de un curso online con video, módulos, lecciones e inscripción
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapacitacionDetalleScreen(
    capacitacionId: Int,
    viewModel: CapacitacionDetalleViewModel,
    onVolver: () -> Unit,
    onVerProducto: (Int) -> Unit,
    onEditar: (Int) -> Unit,
    onEliminado: () -> Unit
) {
    // Recarga el curso al volver de edición o lo carga por primera vez
    LaunchedEffect(capacitacionId) {
        if (viewModel.uiState.value.capacitacion?.id == capacitacionId) {
            viewModel.recargarCapacitacion(capacitacionId)
        } else {
            viewModel.cargarCapacitacion(capacitacionId)
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Muestra una sola vez los avisos de éxito o error de las preguntas
    LaunchedEffect(uiState.mensajeSnackbar) {
        val mensaje = uiState.mensajeSnackbar
        if (mensaje != null) {
            snackbarHostState.showSnackbar(mensaje)
            viewModel.snackbarMostrado()
        }
    }

    Scaffold(
        containerColor = BlancoHueso,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.capacitacion?.titulo ?: "Curso",
                        fontWeight = FontWeight.Bold,
                        color = BlancoPuro,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                    titleContentColor = BlancoPuro,
                    navigationIconContentColor = BlancoPuro
                )
            )
        }
    ) { innerPadding ->
        val capacitacion = uiState.capacitacion

        when {
            uiState.cargando -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    CircularProgressIndicator(color = VerdeItaliano)
                }
            }
            capacitacion == null -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Curso no encontrado",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro
                    )
                }
            }
            else -> {
                // Prioriza el video resumen y usa la primera lección o el video maestro como respaldo
                val urlVideo = capacitacion.videoResumenUrl?.takeIf { it.isNotBlank() }
                    ?: capacitacion.modulos.firstOrNull()?.lecciones?.firstOrNull()?.videoUrl
                        ?.takeIf { it.isNotBlank() }
                    ?: DatosEmpresa.URL_VIDEO_MASTERCLASS

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EtiquetaAcceso(esGratis = capacitacion.esGratis)
                    Text(
                        text = capacitacion.categoria,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlancoPuro,
                        modifier = Modifier
                            .background(VerdeItaliano, RoundedCornerShape(50))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                    if (capacitacion.esVegano) {
                        EtiquetaVegana(tamanoTexto = 15)
                    }
                }
                Text(
                    text = capacitacion.titulo,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeOscuro
                )

                if (uiState.puedeEditar) {
                    OutlinedButton(
                        onClick = { onEditar(capacitacion.id) },
                        border = BorderStroke(2.dp, VerdeItaliano),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeOscuro),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Editar curso", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (uiState.puedeEliminar) {
                    OutlinedButton(
                        onClick = { viewModel.abrirConfirmacionEliminar() },
                        enabled = !uiState.eliminando,
                        border = BorderStroke(2.dp, RojoItaliano),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RojoItaliano),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.eliminando) "Eliminando..." else "Eliminar curso",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                val mensajeError = uiState.mensajeError
                if (mensajeError != null) {
                    Text(
                        text = mensajeError,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RojoItaliano
                    )
                }

                TituloSeccion(texto = "Video resumen")
                ReproductorVideoLocal(videoUrl = urlVideo)

                Card(
                    colors = CardDefaults.cardColors(containerColor = BlancoPuro),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DatoConIcono(
                                icono = Icons.Filled.SignalCellularAlt,
                                texto = "Nivel: ${capacitacion.nivel}"
                            )
                            DatoConIcono(icono = Icons.Filled.Schedule, texto = capacitacion.duracionTotal)
                            DatoConIcono(
                                icono = Icons.AutoMirrored.Filled.MenuBook,
                                texto = "${capacitacion.modulos.size} módulos · ${capacitacion.totalLecciones} lecciones"
                            )
                            DatoConIcono(icono = Icons.Filled.Person, texto = capacitacion.instructor)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = capacitacion.descripcion,
                            fontSize = 16.sp,
                            color = TextoOscuro
                        )
                    }
                }

                TituloSeccion(texto = "Al terminar este curso podrás:")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    capacitacion.loQueLograras.forEach { logro ->
                        FilaConCheck(texto = logro)
                    }
                }

                TituloSeccion(texto = "Contenido del curso")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    capacitacion.modulos.forEachIndexed { indiceModulo, modulo ->
                        BloqueModulo(
                            numero = indiceModulo + 1,
                            modulo = modulo,
                            abierto = uiState.modulosAbiertos.contains(indiceModulo),
                            onAlternar = { viewModel.alternarModulo(indiceModulo) },
                            esMuestraGratis = { indiceLeccion ->
                                viewModel.esMuestraGratis(indiceModulo, indiceLeccion)
                            },
                            estaBloqueada = { indiceLeccion ->
                                viewModel.estaBloqueada(indiceModulo, indiceLeccion)
                            },
                            inscrito = uiState.estaInscrito,
                            estaVista = { indiceLeccion ->
                                viewModel.estaVista(indiceModulo, indiceLeccion)
                            },
                            onAlternarLeccion = { indiceLeccion ->
                                viewModel.alternarLeccion(indiceModulo, indiceLeccion)
                            }
                        )
                    }
                }

                TituloSeccion(texto = "Preguntas al instructor")
                SeccionPreguntas(
                    preguntas = uiState.preguntas,
                    cargando = uiState.cargandoPreguntas,
                    errorCarga = uiState.errorPreguntas,
                    inscrito = uiState.estaInscrito,
                    textoPregunta = uiState.textoPregunta,
                    errorPregunta = uiState.errorPregunta,
                    enviando = uiState.enviandoPregunta,
                    onTextoChange = { texto -> viewModel.onTextoPreguntaChange(texto) },
                    onEnviar = { viewModel.enviarPregunta() },
                    onReintentar = { viewModel.cargarPreguntas(capacitacion.id) },
                    puedeEliminar = { pregunta -> viewModel.puedeEliminarPregunta(pregunta) },
                    onEliminar = { pregunta -> viewModel.abrirConfirmacionEliminarPregunta(pregunta) }
                )

                TituloSeccion(texto = "Incluye")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    capacitacion.incluye.forEach { item ->
                        FilaConCheck(texto = item)
                    }
                }

                val producto = uiState.producto
                if (producto != null) {
                    OutlinedButton(
                        onClick = { onVerProducto(producto.id) },
                        border = BorderStroke(1.dp, VerdeItaliano),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeOscuro),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = "Producto principal: ${capacitacion.insumoPrincipal}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null
                        )
                    }
                } else {
                    Text(
                        text = "Producto principal: ${capacitacion.insumoPrincipal}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VerdeOscuro
                    )
                }

                if (uiState.estaInscrito) {
                    TarjetaAvance(
                        completadas = uiState.leccionesCompletadas,
                        total = uiState.totalLecciones,
                        porcentaje = uiState.porcentaje,
                        mensajeError = uiState.mensajeErrorInscripcion,
                        procesando = uiState.procesando,
                        onCancelar = { viewModel.abrirConfirmacionCancelar() }
                    )
                } else {
                    Button(
                        onClick = { viewModel.inscribirse() },
                        enabled = !uiState.procesando,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeItaliano,
                            contentColor = BlancoPuro
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                    ) {
                        if (uiState.procesando) {
                            CircularProgressIndicator(color = BlancoPuro, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (capacitacion.esGratis) "Empezar curso gratis" else "Desbloquear con Plan Pro",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    val errorInscripcion = uiState.mensajeErrorInscripcion
                    if (errorInscripcion != null) {
                        Text(
                            text = errorInscripcion,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RojoItaliano
                        )
                    }
                    if (!capacitacion.esGratis) {
                        Text(
                            text = "La suscripción se coordina con nuestro equipo de ventas",
                            fontSize = 16.sp,
                            color = TextoOscuro,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            }
        }
    }

    if (uiState.mostrarConfirmacionCancelar) {
        AlertDialog(
            onDismissRequest = { viewModel.cerrarConfirmacionCancelar() },
            title = { Text(text = "Cancelar inscripción", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "¿Quieres cancelar tu inscripción? Se borrará el avance que llevas en este curso.",
                    fontSize = 17.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.cancelarInscripcion() },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(text = "Cancelar inscripción", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RojoItaliano)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.cerrarConfirmacionCancelar() },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(text = "Seguir inscrito", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VerdeOscuro)
                }
            }
        )
    }

    if (uiState.preguntaAEliminar != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cerrarConfirmacionEliminarPregunta() },
            title = { Text(text = "Eliminar pregunta", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "¿Eliminar esta pregunta? Esta acción no se puede deshacer",
                    fontSize = 17.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.eliminarPregunta() },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(text = "Eliminar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RojoItaliano)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.cerrarConfirmacionEliminarPregunta() },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(text = "Cancelar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VerdeOscuro)
                }
            }
        )
    }

    if (uiState.mostrarConfirmacionEliminar) {
        AlertDialog(
            onDismissRequest = { viewModel.cerrarConfirmacionEliminar() },
            title = { Text(text = "Eliminar curso", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "¿Eliminar este curso? Esta acción no se puede deshacer",
                    fontSize = 17.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.eliminarCurso(onEliminado = onEliminado) },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(text = "Eliminar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RojoItaliano)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.cerrarConfirmacionEliminar() },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(text = "Cancelar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VerdeOscuro)
                }
            }
        )
    }
}

// Sección con el formulario para preguntar y la lista de preguntas del curso
@Composable
private fun SeccionPreguntas(
    preguntas: List<Pregunta>,
    cargando: Boolean,
    errorCarga: String?,
    inscrito: Boolean,
    textoPregunta: String,
    errorPregunta: String?,
    enviando: Boolean,
    onTextoChange: (String) -> Unit,
    onEnviar: () -> Unit,
    onReintentar: () -> Unit,
    puedeEliminar: (Pregunta) -> Boolean,
    onEliminar: (Pregunta) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (inscrito) {
            OutlinedTextField(
                value = textoPregunta,
                onValueChange = onTextoChange,
                label = { Text("Escribe tu pregunta", fontSize = 16.sp) },
                leadingIcon = { Icon(imageVector = Icons.Filled.Forum, contentDescription = null) },
                trailingIcon = { IconoValidacion(valor = textoPregunta, error = errorPregunta) },
                isError = errorPregunta != null,
                supportingText = {
                    // Error a la izquierda y contador de caracteres a la derecha
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = errorPregunta ?: "",
                            color = RojoItaliano,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${textoPregunta.length}/${Pregunta.MAX_CARACTERES}",
                            fontSize = 16.sp,
                            color = TextoOscuro
                        )
                    }
                },
                enabled = !enviando,
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeItaliano,
                    cursorColor = VerdeItaliano,
                    focusedContainerColor = BlancoPuro,
                    unfocusedContainerColor = BlancoPuro
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = onEnviar,
                enabled = !enviando,
                colors = ButtonDefaults.buttonColors(containerColor = VerdeItaliano, contentColor = BlancoPuro),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                if (enviando) {
                    CircularProgressIndicator(color = BlancoPuro, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Enviar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Text(
                text = "Inscríbete para hacer preguntas",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = VerdeOscuro
            )
        }

        when {
            cargando -> {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(color = VerdeItaliano)
                }
            }
            errorCarga != null -> {
                ErrorConReintentar(mensaje = errorCarga, onReintentar = onReintentar)
            }
            preguntas.isEmpty() -> {
                Text(
                    text = "Aún no hay preguntas en este curso",
                    fontSize = 16.sp,
                    color = TextoOscuro
                )
            }
            else -> {
                preguntas.forEach { pregunta ->
                    TarjetaPregunta(
                        pregunta = pregunta,
                        puedeEliminar = puedeEliminar(pregunta),
                        onEliminar = { onEliminar(pregunta) }
                    )
                }
            }
        }
    }
}

// Tarjeta con el autor, la fecha, el texto y la respuesta destacada si existe
@Composable
private fun TarjetaPregunta(
    pregunta: Pregunta,
    puedeEliminar: Boolean,
    onEliminar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = pregunta.autorNombre ?: "Alumno",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
            if (pregunta.fechaLegible.isNotEmpty()) {
                Text(text = pregunta.fechaLegible, fontSize = 16.sp, color = VerdeOscuro)
            }
            Text(text = pregunta.texto, fontSize = 16.sp, color = TextoOscuro)

            if (pregunta.estaRespondida) {
                // Respuesta destacada con fondo verde suave y borde
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VerdeItaliano.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
                        .border(1.dp, VerdeItaliano, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Respuesta de ${pregunta.respondidaPor ?: "el instructor"}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerdeOscuro
                    )
                    Text(text = pregunta.respuesta ?: "", fontSize = 16.sp, color = TextoOscuro)
                }
            } else {
                Text(
                    text = "Esperando respuesta del instructor",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoOscuro
                )
            }

            if (puedeEliminar) {
                TextButton(
                    onClick = onEliminar,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = RojoItaliano)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Eliminar pregunta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RojoItaliano
                    )
                }
            }
        }
    }
}

// Tarjeta de avance del usuario inscrito y opción para cancelar inscripción
@Composable
private fun TarjetaAvance(
    completadas: Int,
    total: Int,
    porcentaje: Int,
    mensajeError: String?,
    procesando: Boolean,
    onCancelar: () -> Unit
) {
    val progresoAnimado by animateFloatAsState(
        targetValue = porcentaje / 100f,
        label = "progresoCurso"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        border = BorderStroke(2.dp, VerdeItaliano),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tu avance",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeOscuro
            )
            Text(
                text = "$completadas de $total lecciones · $porcentaje%",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoOscuro
            )
            LinearProgressIndicator(
                progress = { progresoAnimado },
                color = VerdeItaliano,
                trackColor = GrisBorde,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            Text(
                text = "Toca una lección para marcarla como vista",
                fontSize = 16.sp,
                color = TextoOscuro
            )
            if (mensajeError != null) {
                Text(
                    text = mensajeError,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RojoItaliano
                )
            }
            TextButton(
                onClick = onCancelar,
                enabled = !procesando,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = "Cancelar inscripción",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RojoItaliano
                )
            }
        }
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = TextoOscuro
    )
}

@Composable
private fun FilaConCheck(texto: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = VerdeItaliano,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = texto,
            fontSize = 16.sp,
            color = TextoOscuro,
            modifier = Modifier.weight(1f)
        )
    }
}

// Módulo desplegable con su encabezado accesible y lista de lecciones
@Composable
private fun BloqueModulo(
    numero: Int,
    modulo: Modulo,
    abierto: Boolean,
    onAlternar: () -> Unit,
    esMuestraGratis: (Int) -> Boolean,
    estaBloqueada: (Int) -> Boolean,
    inscrito: Boolean,
    estaVista: (Int) -> Boolean,
    onAlternarLeccion: (Int) -> Unit
) {
    val rotacionFlecha by animateFloatAsState(
        targetValue = if (abierto) 180f else 0f,
        label = "rotacionFlecha"
    )

    val estado = if (abierto) "Abierto" else "Cerrado"
    val descripcionEncabezado =
        "Módulo $numero: ${modulo.titulo}. ${modulo.lecciones.size} lecciones, " +
                "${modulo.totalMinutos} minutos. $estado"

    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(
                    role = Role.Button,
                    onClickLabel = if (abierto) "cerrar el módulo" else "abrir el módulo",
                    onClick = onAlternar
                )
                .semantics { contentDescription = descripcionEncabezado }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Módulo $numero",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeItaliano
                )
                Text(
                    text = modulo.titulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
                Text(
                    text = "${modulo.lecciones.size} lecciones · ${modulo.totalMinutos} min",
                    fontSize = 16.sp,
                    color = TextoOscuro
                )
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = VerdeOscuro,
                modifier = Modifier
                    .size(32.dp)
                    .rotate(rotacionFlecha)
            )
        }

        AnimatedVisibility(visible = abierto) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                HorizontalDivider(color = GrisBorde)
                modulo.lecciones.forEachIndexed { indiceLeccion, leccion ->
                    FilaLeccion(
                        numero = indiceLeccion + 1,
                        leccion = leccion,
                        esMuestraGratis = esMuestraGratis(indiceLeccion),
                        bloqueada = estaBloqueada(indiceLeccion),
                        vista = estaVista(indiceLeccion),
                        onAlternar = if (inscrito) {
                            { onAlternarLeccion(indiceLeccion) }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }
}

// Fila de lección con estado visto/pendiente, muestra gratis o bloqueo
@Composable
private fun FilaLeccion(
    numero: Int,
    leccion: Leccion,
    esMuestraGratis: Boolean,
    bloqueada: Boolean,
    vista: Boolean,
    onAlternar: (() -> Unit)?
) {
    val colorFondo by animateColorAsState(
        targetValue = if (vista) VerdeItaliano else BlancoPuro,
        label = "fondoLeccion"
    )
    val colorContenido by animateColorAsState(
        targetValue = if (vista) BlancoPuro else VerdeItaliano,
        label = "contenidoLeccion"
    )

    val descripcion = "Lección $numero: ${leccion.titulo}, ${leccion.duracion}. " +
            if (vista) "Vista" else "No vista"

    val modificadorToque = if (onAlternar != null) {
        Modifier
            .toggleable(
                value = vista,
                role = Role.Checkbox,
                onValueChange = { onAlternar() }
            )
            .semantics { contentDescription = descripcion }
    } else {
        Modifier
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .then(modificadorToque)
            .padding(vertical = 6.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .background(colorFondo, CircleShape)
                .border(2.dp, VerdeItaliano, CircleShape)
        ) {
            if (vista) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = colorContenido,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "$numero",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorContenido
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = leccion.titulo,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (vista) Color(0xFF666666) else TextoOscuro
            )
            Text(
                text = leccion.duracion,
                fontSize = 16.sp,
                color = TextoOscuro
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (esMuestraGratis) {
            Text(
                text = "Muestra gratis",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = BlancoPuro,
                modifier = Modifier
                    .background(VerdeOscuro, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        } else if (bloqueada) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Lección bloqueada",
                tint = TextoOscuro,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}