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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SignalCellularAlt
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.mastermartini.model.Leccion
import com.example.mastermartini.model.Modulo
import com.example.mastermartini.ui.components.DatoConIcono
import com.example.mastermartini.ui.components.EtiquetaAcceso
import com.example.mastermartini.ui.components.EtiquetaVegana
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.GrisBorde
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.CapacitacionDetalleViewModel

// PANTALLA DE DETALLE DE UN CURSO ONLINE
// Muestra todo el curso: video resumen (desde Cloudinary), datos, qué se logra, módulos con
// sus lecciones, qué incluye, el producto principal y el botón para empezar.
// Solo DIBUJA lo que le entrega el CapacitacionDetalleViewModel (MVVM): incluso qué módulos
// están abiertos y qué lecciones llevan candado lo decide el ViewModel.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapacitacionDetalleScreen(
    // Id del curso, que llega por la ruta "capacitacion/{capacitacionId}" (lo saca MainActivity)
    capacitacionId: Int,
    viewModel: CapacitacionDetalleViewModel,
    // Vuelve a la lista de cursos (popBackStack en MainActivity)
    onVolver: () -> Unit,
    // Recibe el id del producto principal; MainActivity navega a "producto/{id}"
    onVerProducto: (Int) -> Unit,
    // Botón "Editar curso": recibe el id; MainActivity navega a "panel/editar-curso/{id}"
    onEditar: (Int) -> Unit,
    // Se llama cuando el servidor eliminó el curso: MainActivity vuelve a la lista de cursos
    // (popBackStack), que se recarga sola al entrar y ya no lo muestra.
    onEliminado: () -> Unit
) {
    // LaunchedEffect se ejecuta cada vez que la pantalla ENTRA a la composición.
    // Al ir a "Editar curso" esta pantalla sale, y al volver entra de nuevo (el ViewModel sobrevive):
    //   - Si el curso todavía no está cargado (primera vez): cargarCapacitacion.
    //   - Si ya estaba cargado (ej. al volver de Editar): recargarCapacitacion, que lo vuelve
    //     a pedir al servidor para mostrar los datos nuevos, sin cerrar los módulos abiertos.
    // Se lee viewModel.uiState.value (el valor de este momento), solo para elegir cuál llamar.
    LaunchedEffect(capacitacionId) {
        if (viewModel.uiState.value.capacitacion?.id == capacitacionId) {
            viewModel.recargarCapacitacion(capacitacionId)
        } else {
            viewModel.cargarCapacitacion(capacitacionId)
        }
    }

    // Escuchamos el estado: al llegar el curso o al abrir/cerrar un módulo, la pantalla se redibuja sola
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            // a. Barra verde con la flecha para volver (mismo estilo que DetalleScreen)
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
        // Variable local para que Kotlin sepa que no cambia entre el "if" y su uso
        val capacitacion = uiState.capacitacion

        if (capacitacion == null) {
            // Mientras llega la respuesta del servidor, o si el id no existe: se avisa en vez de cerrar la app
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
        } else {
            // ENLACE DEL VIDEO (subido a Cloudinary). Usa videoResumenUrl del curso o, si es null
            // o está vacío, el videoUrl de su primera lección. takeIf { it.isNotBlank() } convierte
            // un texto vacío "" en null, para que el ?: pase a la siguiente opción.
            // Si ninguno tiene video, queda null y el reproductor muestra un aviso en vez de fallar.
            val urlVideo = capacitacion.videoResumenUrl?.takeIf { it.isNotBlank() }
                ?: capacitacion.modulos.firstOrNull()?.lecciones?.firstOrNull()?.videoUrl
                    ?.takeIf { it.isNotBlank() }

            // Column con verticalScroll: el contenido es corto (máx. 4 módulos), no hace falta LazyColumn
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // a. Etiquetas y título completo (en la barra de arriba puede quedar cortado)
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

                // BOTONES DE GESTIÓN DEL CURSO. Solo aparecen si el ViewModel lo permite
                // (lo decide el Rol de la sesión con polimorfismo; la pantalla no pregunta qué rol es).
                // Ícono + texto, y al menos 48 dp de alto.
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
                    // Rojo (color de error) porque es una acción peligrosa, igual que "Cerrar sesión".
                    // No borra directo: abre el diálogo de confirmación.
                    // Se desactiva mientras se está eliminando, para no tocarlo dos veces.
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
                // Mensaje de error en rojo (ej. sin permiso para eliminar). Lo decide el ViewModel.
                // Se copia a una variable local para usarla sin "!!".
                val mensajeError = uiState.mensajeError
                if (mensajeError != null) {
                    Text(
                        text = mensajeError,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RojoItaliano
                    )
                }

                // b. Video resumen. Se ve SIEMPRE, aunque el curso sea del Plan Pro:
                //    sirve para que la persona conozca el curso antes de suscribirse.
                //    Se reproduce por internet desde Cloudinary (el video no va dentro de la app).
                TituloSeccion(texto = "🎥 Video resumen")
                ReproductorVideoLocal(videoUrl = urlVideo)

                // c. Datos con íconos (DatoConIcono, en ui/components, el mismo de DetalleScreen) y la descripción

                Card(
                    colors = CardDefaults.cardColors(containerColor = BlancoPuro),
                    border = BorderStroke(1.dp, DoradoCalidad.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // FlowRow: si no caben en una línea, bajan a la siguiente
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

                // d. Lo que la persona podrá hacer al terminar
                TituloSeccion(texto = "Al terminar este curso podrás:")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    capacitacion.loQueLograras.forEach { logro ->
                        FilaConCheck(texto = logro)
                    }
                }

                // e. Contenido del curso: un bloque desplegable por módulo.
                //    forEachIndexed entrega la posición (indice) que guarda el ViewModel.
                TituloSeccion(texto = "Contenido del curso")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    capacitacion.modulos.forEachIndexed { indiceModulo, modulo ->
                        BloqueModulo(
                            numero = indiceModulo + 1,
                            modulo = modulo,
                            // El estado abierto/cerrado viene del ViewModel, no de un remember
                            abierto = uiState.modulosAbiertos.contains(indiceModulo),
                            onAlternar = { viewModel.alternarModulo(indiceModulo) },
                            // Se le pasan las reglas del ViewModel ya respondidas para cada lección
                            esMuestraGratis = { indiceLeccion ->
                                viewModel.esMuestraGratis(indiceModulo, indiceLeccion)
                            },
                            estaBloqueada = { indiceLeccion ->
                                viewModel.estaBloqueada(indiceModulo, indiceLeccion)
                            },
                            // PROGRESO: solo si está inscrito las lecciones se pueden tocar
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

                // f. Qué incluye el curso
                TituloSeccion(texto = "Incluye")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    capacitacion.incluye.forEach { item ->
                        FilaConCheck(texto = item)
                    }
                }

                // g. Producto principal: si se encontró, es un botón que abre su detalle
                //    (igual que el insumo en DetalleScreen); si no, texto normal.
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
                        // Decorativo: el texto ya dice qué es; la flecha indica que abre otra pantalla
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

                // h. INSCRIPCIÓN: si ya está inscrito se muestra su avance; si no, el botón para inscribirse.
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
                    // Botón grande al final (56 dp de alto). Inscribe al usuario en el servidor,
                    // que revisa el Plan Pro en los cursos que no son gratis.
                    // Se desactiva mientras espera la respuesta, para no inscribirse dos veces.
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
                            // Ruedita mientras espera al servidor (igual que en Crear curso)
                            CircularProgressIndicator(color = BlancoPuro, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (capacitacion.esGratis) "Empezar curso gratis" else "Desbloquear con Plan Pro",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    // Error al inscribirse (ej. falta el Plan Pro). Lo decide el ViewModel.
                    val errorInscripcion = uiState.mensajeErrorInscripcion
                    if (errorInscripcion != null) {
                        Text(
                            text = errorInscripcion,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RojoItaliano
                        )
                    }
                    // Solo en los cursos del Plan Pro: la suscripción todavía no se paga en la app
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

    // DIÁLOGO "¿Cancelar tu inscripción?": evita perder el avance por un toque accidental.
    // Lo abre el TextButton de la tarjeta "Tu avance"; si está abierto o no lo guarda el ViewModel.
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
            // Botón que SÍ cancela (en rojo, porque se pierde el avance)
            confirmButton = {
                TextButton(
                    onClick = { viewModel.cancelarInscripcion() },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(text = "Cancelar inscripción", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RojoItaliano)
                }
            },
            // Botón que se arrepiente: solo cierra el diálogo
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

    // DIÁLOGO DE CONFIRMACIÓN: evita borrar el curso por un toque accidental.
    // Lo abre el botón "Eliminar curso"; si está abierto o no lo guarda el ViewModel.
    // onDismissRequest: se llama al tocar fuera del diálogo o con Atrás (= cancelar).
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
            // Botón que SÍ elimina (en rojo). Si sale bien, onEliminado vuelve a la lista.
            confirmButton = {
                TextButton(
                    onClick = { viewModel.eliminarCurso(onEliminado = onEliminado) },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(text = "Eliminar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RojoItaliano)
                }
            },
            // Botón que se arrepiente: solo cierra el diálogo
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

// TARJETA "TU AVANCE" (solo si está inscrito): "X de Y lecciones · Z%", la barra de progreso
// y el botón para cancelar la inscripción. No calcula nada: los números llegan listos del
// ViewModel (propiedades calculadas del estado).
@Composable
private fun TarjetaAvance(
    completadas: Int,
    total: Int,
    porcentaje: Int,
    mensajeError: String?,
    procesando: Boolean,
    onCancelar: () -> Unit
) {
    // La barra avanza suave al marcar una lección (igual que el "paso a paso" de las recetas).
    // LinearProgressIndicator usa un número de 0f a 1f: 37 % -> 0.37f
    val progresoAnimado by animateFloatAsState(
        targetValue = porcentaje / 100f,
        label = "progresoCurso"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        border = BorderStroke(2.dp, VerdeItaliano),
        shape = RoundedCornerShape(12.dp),
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
            // Error al marcar una lección o al cancelar (lo decide el ViewModel)
            if (mensajeError != null) {
                Text(
                    text = mensajeError,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RojoItaliano
                )
            }
            // Acción secundaria y "peligrosa": TextButton rojo (no compite con el contenido).
            // No cancela directo: abre el diálogo de confirmación. Mínimo 48 dp.
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

// TÍTULO DE CADA SECCIÓN DEL DETALLE (mismo tamaño que los títulos de DetalleScreen).
// Se escribe una vez para no repetir el estilo en las 4 secciones.
@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = TextoOscuro
    )
}

// UNA FILA CON ✓ VERDE Y SU TEXTO. La usan "Al terminar este curso podrás" e "Incluye".
// El ✓ es un texto (no un ícono), así TalkBack lo lee junto con la frase como un solo elemento.
@Composable
private fun FilaConCheck(texto: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "✓",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = VerdeItaliano
        )
        Spacer(modifier = Modifier.width(10.dp))
        // weight(1f): el texto usa el ancho que sobra y baja de línea si es largo
        Text(
            text = texto,
            fontSize = 16.sp,
            color = TextoOscuro,
            modifier = Modifier.weight(1f)
        )
    }
}

// UN MÓDULO DESPLEGABLE: encabezado que se toca + sus lecciones.
// No guarda nada: recibe si está abierto y avisa el toque con onAlternar (el ViewModel decide).
@Composable
private fun BloqueModulo(
    numero: Int,
    modulo: Modulo,
    abierto: Boolean,
    onAlternar: () -> Unit,
    // Funciones que reciben la posición de la lección y responden true/false (reglas del ViewModel)
    esMuestraGratis: (Int) -> Boolean,
    estaBloqueada: (Int) -> Boolean,
    // PROGRESO: si está inscrito, las lecciones se pueden tocar para marcarlas como vistas
    inscrito: Boolean,
    estaVista: (Int) -> Boolean,
    // Recibe la posición de la lección tocada; el ViewModel la marca o desmarca en el servidor
    onAlternarLeccion: (Int) -> Unit
) {
    // La flecha gira suave de 0° (cerrado, apunta abajo) a 180° (abierto, apunta arriba)
    val rotacionFlecha by animateFloatAsState(
        targetValue = if (abierto) 180f else 0f,
        label = "rotacionFlecha"
    )

    // Texto que lee TalkBack en el encabezado: dice todo el módulo y si está abierto o cerrado
    val estado = if (abierto) "Abierto" else "Cerrado"
    val descripcionEncabezado =
        "Módulo $numero: ${modulo.titulo}. ${modulo.lecciones.size} lecciones, " +
                "${modulo.totalMinutos} minutos. $estado"

    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        border = BorderStroke(1.dp, GrisBorde),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // ENCABEZADO: TODA la fila se puede tocar (no solo la flecha), mínimo 56 dp de alto.
        // clickable con Role.Button: TalkBack lo anuncia como botón.
        // onClickLabel: TalkBack dice "toca dos veces para abrir/cerrar el módulo".
        // semantics { contentDescription }: lo que TalkBack lee al llegar al encabezado.
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
            // Decorativo (null): el estado abierto/cerrado ya lo dice el contentDescription del encabezado
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = VerdeOscuro,
                modifier = Modifier
                    .size(32.dp)
                    .rotate(rotacionFlecha)
            )
        }

        // LECCIONES: AnimatedVisibility las muestra desplegándose al abrir y las esconde al cerrar
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
                        // null = no se puede tocar (no está inscrito)
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

// UNA LECCIÓN: número en un círculo, título, duración y, a la derecha,
// la etiqueta "Muestra gratis" o el candado (solo en cursos del Plan Pro, si no está inscrito).
// Si está INSCRITO (onAlternar no es null), toda la fila se toca para marcarla como vista:
// el número pasa a un ✓ verde, igual que los pasos de las recetas (FilaPaso en DetalleScreen).
@Composable
private fun FilaLeccion(
    numero: Int,
    leccion: Leccion,
    esMuestraGratis: Boolean,
    bloqueada: Boolean,
    // true = la lección está marcada como vista
    vista: Boolean,
    // Qué hacer al tocarla. null = no se puede tocar (no está inscrito)
    onAlternar: (() -> Unit)?
) {
    // animateColorAsState: al marcarla el color cambia de a poco (no de golpe).
    // No vista: círculo blanco con número verde. Vista: círculo verde relleno con ✓ blanco.
    val colorFondo by animateColorAsState(
        targetValue = if (vista) VerdeItaliano else BlancoPuro,
        label = "fondoLeccion"
    )
    val colorContenido by animateColorAsState(
        targetValue = if (vista) BlancoPuro else VerdeItaliano,
        label = "contenidoLeccion"
    )

    // Lo que lee TalkBack en la fila: la lección y si ya está vista o no
    val descripcion = "Lección $numero: ${leccion.titulo}, ${leccion.duracion}. " +
            if (vista) "Vista" else "No vista"

    // Si se puede tocar: toggleable con Role.Checkbox (TalkBack la anuncia como casilla
    // marcada/no marcada) en TODA la fila, de mínimo 48 dp de alto.
    // Modifier.then(...) agrega esos modificadores solo cuando onAlternar no es null.
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
        // Círculo con el número o el ✓ (mismo estilo que los pasos de la receta en DetalleScreen)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .background(colorFondo, CircleShape)
                .border(2.dp, VerdeItaliano, CircleShape)
        ) {
            Text(
                text = if (vista) "✓" else "$numero",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorContenido
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            // Vista: título en gris oscuro (0xFF666666), igual que los pasos completados de las recetas
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
            // Texto además del color: se entiende aunque no se distingan los colores
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
            // NO es decorativo: es la única forma de saber que la lección está bloqueada,
            // por eso lleva contentDescription para TalkBack
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Lección bloqueada",
                tint = TextoOscuro,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}