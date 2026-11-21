package com.example.mastermartini.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.CapacitacionDetalleViewModel

// PANTALLA DE DETALLE DE UN CURSO ONLINE
// Muestra todo el curso: video resumen, datos, qué se logra, módulos con sus lecciones,
// qué incluye, el producto principal y el botón para empezar.
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
    onVerProducto: (Int) -> Unit
) {
    // Se ejecuta UNA vez al abrir la pantalla (y de nuevo solo si cambia el id),
    // igual que en DetalleScreen
    LaunchedEffect(capacitacionId) {
        viewModel.cargarCapacitacion(capacitacionId)
    }

    // Escuchamos el estado: al abrir o cerrar un módulo, la pantalla se redibuja sola
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
            // Id que no existe en el repositorio: se avisa en vez de cerrar la app
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

                // b. Video resumen. Se ve SIEMPRE, aunque el curso sea del Plan Pro:
                //    sirve para que la persona conozca el curso antes de suscribirse.
                TituloSeccion(texto = "🎥 Video resumen")
                // FASE 4: aquí se usará videoResumenUrl desde AWS S3.
                // Por ahora se reutiliza el video local de res/raw como reemplazo temporal.
                ReproductorVideoLocal()

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

                // h. Botón grande al final (56 dp de alto)
                Button(
                    // FASE 2: aquí se inscribirá al usuario (o se abrirá el Plan Pro).
                    // Por ahora no hace nada.
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdeItaliano,
                        contentColor = BlancoPuro
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                ) {
                    Text(
                        text = if (capacitacion.esGratis) "Empezar curso gratis" else "Desbloquear con Plan Pro",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
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
    estaBloqueada: (Int) -> Boolean
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
                        bloqueada = estaBloqueada(indiceLeccion)
                    )
                }
            }
        }
    }
}

// UNA LECCIÓN: número en un círculo, título, duración y, a la derecha,
// la etiqueta "Muestra gratis" o el candado (solo en cursos del Plan Pro).
// FASE 2: las lecciones se podrán tocar para ver su video. Por ahora solo se muestran.
@Composable
private fun FilaLeccion(
    numero: Int,
    leccion: Leccion,
    esMuestraGratis: Boolean,
    bloqueada: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(vertical = 6.dp)
    ) {
        // Círculo con el número (mismo estilo que los pasos de la receta en DetalleScreen)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .background(BlancoPuro, CircleShape)
                .border(2.dp, VerdeItaliano, CircleShape)
        ) {
            Text(
                text = "$numero",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeItaliano
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = leccion.titulo,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoOscuro
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
