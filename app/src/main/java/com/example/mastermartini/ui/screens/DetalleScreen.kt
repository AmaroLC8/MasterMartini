package com.example.mastermartini.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.components.ContactoVentas
import com.example.mastermartini.ui.components.EtiquetaVegana
import com.example.mastermartini.ui.theme.*
import com.example.mastermartini.viewmodel.DetalleViewModel

// PANTALLA DE DETALLE DE UN CURSO
// Muestra la receta paso a paso del curso que se tocó en Home, con el video local.
// Solo DIBUJA el curso que le entrega el DetalleViewModel (MVVM): no busca datos por su cuenta.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    // Id del curso, que llega por la ruta "detalle/{cursoId}" (lo saca MainActivity)
    cursoId: Int,
    viewModel: DetalleViewModel,
    // MainActivity decide qué hacer al volver (popBackStack: regresa a Home)
    onVolver: () -> Unit,
    // Recibe el id del producto del insumo; MainActivity navega a "producto/{id}"
    onVerProducto: (Int) -> Unit
) {
    // LaunchedEffect(cursoId) se ejecuta UNA vez al abrir la pantalla, y de nuevo solo si cambia cursoId.
    // Si llamáramos cargarCurso() directo en el composable, se repetiría en cada redibujo.
    LaunchedEffect(cursoId) {
        viewModel.cargarCurso(cursoId)
    }

    // Escuchamos el curso del ViewModel: cuando cargarCurso() lo encuentra, la pantalla se redibuja sola
    val curso by viewModel.curso.collectAsState()

    // Posiciones de los ingredientes marcados. Vienen de Room a través del ViewModel:
    // al marcar uno, Room avisa el cambio y esta lista se actualiza sola.
    val marcados by viewModel.marcados.collectAsState()

    // Posiciones de los pasos completados (también vienen de Room, tabla "checklist_pasos")
    val pasosCompletados by viewModel.pasosCompletados.collectAsState()

    // Producto Master Martini del insumo (null si no se encontró por nombre)
    val productoInsumo by viewModel.productoInsumo.collectAsState()

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            // Barra verde con flecha para volver (mismo estilo que EncuestaScreen)
            TopAppBar(
                title = {
                    Text(
                        // Mientras no hay curso (o no existe) se muestra un título genérico
                        text = curso?.titulo ?: "Receta",
                        fontWeight = FontWeight.Bold,
                        color = BlancoPuro,
                        // Los títulos largos se cortan con "..." para no romper la barra
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
        // Se guarda en una variable local para que Kotlin sepa que no cambia entre el "if" y su uso
        val cursoActual = curso

        if (cursoActual == null) {
            // Id que no existe en el repositorio: se avisa en vez de cerrar la app
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Receta no encontrada",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
            }
        } else {
            // Column con verticalScroll: el contenido es corto y fijo, no hace falta LazyColumn
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Datos del curso (mismo estilo de tarjeta que en Home)
                Card(
                    colors = CardDefaults.cardColors(containerColor = BlancoPuro),
                    border = BorderStroke(1.dp, DoradoCalidad.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Etiqueta (chip) con la sección de la receta: texto blanco sobre verde
                            // para buen contraste. No es un botón, solo informa, por eso no usa onClick.
                            Text(
                                text = cursoActual.categoria,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlancoPuro,
                                modifier = Modifier
                                    .background(VerdeItaliano, RoundedCornerShape(50))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                            // "Apto vegano 🌱" al lado, solo si la receta es vegana
                            if (cursoActual.esVegano) {
                                EtiquetaVegana(tamanoTexto = 15)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = cursoActual.titulo,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = VerdeOscuro
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Descripción breve: qué se aprende en este curso
                        Text(
                            text = cursoActual.descripcion,
                            fontSize = 16.sp,
                            color = TextoOscuro
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // INSUMO: si el ViewModel encontró el producto, es un botón que abre su detalle;
                        // si no, se muestra como texto normal (como antes).
                        // Se copia a una variable local para poder revisar si es null y usarla sin "!!".
                        val producto = productoInsumo
                        if (producto != null) {
                            // Botón a todo el ancho (mínimo 48 dp) con borde: se nota que se puede tocar
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
                                    text = "Insumo Master Martini: ${cursoActual.insumo}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                // Flecha: indica que abre otra pantalla. Decorativa: el texto ya dice qué es
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            }
                        } else {
                            Text(
                                text = "Insumo Master Martini: ${cursoActual.insumo}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VerdeItaliano
                            )
                        }
                        Text(
                            text = "Instructor: ${cursoActual.chef}",
                            fontSize = 16.sp,
                            color = TextoOscuro
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Fila con duración, rendimiento y dificultad, cada uno con su ícono.
                        // FlowRow: si no caben en una línea (celular angosto o letra grande),
                        // los que sobran bajan a la línea siguiente en vez de cortarse.
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DatoConIcono(icono = Icons.Filled.Schedule, texto = cursoActual.duracion)
                            DatoConIcono(icono = Icons.Filled.Restaurant, texto = cursoActual.rendimiento)
                            DatoConIcono(
                                icono = Icons.Filled.SignalCellularAlt,
                                texto = "Dificultad: ${cursoActual.dificultad}"
                            )
                        }
                    }
                }

                // 2. Video de la receta (se reutiliza el mismo reproductor de Home)
                Text(
                    text = "🎥 Video de la receta",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
                // Por ahora todos los cursos tienen videoUrl = null (aún no se suben a AWS),
                // así que siempre se muestra el video local de res/raw, igual que antes.
                // Cuando un curso tenga videoUrl, aquí se elegirá entre el video en la nube y el local.
                ReproductorVideoLocal()

                // 3. Ingredientes como checklist (las marcas se guardan en Room)
                Text(
                    text = "🧺 Ingredientes",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )

                // Cuántos hay listos y cuántos son en total (solo para dibujar; la lista la maneja el ViewModel)
                val totalIngredientes = cursoActual.ingredientes.size
                val totalListos = marcados.size

                // Progreso de 0f (ninguno) a 1f (todos). Se revisa que el total no sea 0 para no dividir por cero.
                val progreso = if (totalIngredientes > 0) {
                    totalListos.toFloat() / totalIngredientes
                } else {
                    0f
                }
                // animateFloatAsState: al marcar un ingrediente la barra avanza suave en vez de saltar
                // (mismo tipo de animación que el corazón de favoritos en Home)
                val progresoAnimado by animateFloatAsState(
                    targetValue = progreso,
                    label = "progresoIngredientes"
                )

                Text(
                    text = "$totalListos de $totalIngredientes ingredientes listos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VerdeOscuro
                )
                LinearProgressIndicator(
                    // progress se pasa como función { } para que Compose lea el valor animado en cada cuadro
                    progress = { progresoAnimado },
                    color = VerdeItaliano,
                    trackColor = GrisBorde,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                // Column con espacio 0: las filas ya miden 48 dp, no hace falta separarlas más
                Column {
                    // forEachIndexed entrega la posición (indice) que se guarda en Room
                    cursoActual.ingredientes.forEachIndexed { indice, ingrediente ->
                        val estaMarcado = marcados.contains(indice)

                        // Toda la fila se puede tocar (no solo la casilla pequeña), más fácil para usuarios mayores.
                        // toggleable con Role.Checkbox: TalkBack lee la fila como una casilla "marcada/no marcada".
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .toggleable(
                                    value = estaMarcado,
                                    role = Role.Checkbox,
                                    onValueChange = { viewModel.alternarIngrediente(indice) }
                                )
                        ) {
                            // onCheckedChange = null: el toque lo maneja la fila completa (toggleable),
                            // así no se registra dos veces al tocar justo la casilla
                            Checkbox(
                                checked = estaMarcado,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = VerdeItaliano,
                                    uncheckedColor = VerdeOscuro,
                                    checkmarkColor = BlancoPuro
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            // Marcado: texto tachado y gris, para ver de un vistazo lo que ya está listo.
                            // Gris oscuro (0xFF666666) y no Color.Gray: el gris claro casi no se lee
                            // sobre el fondo blanco hueso (poco contraste para usuarios mayores).
                            Text(
                                text = ingrediente,
                                fontSize = 16.sp,
                                color = if (estaMarcado) Color(0xFF666666) else TextoOscuro,
                                textDecoration = if (estaMarcado) TextDecoration.LineThrough else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // Botón para desmarcar todo: solo aparece si hay al menos un ingrediente marcado
                if (totalListos > 0) {
                    TextButton(
                        onClick = { viewModel.reiniciarChecklist() },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = "Reiniciar lista",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = RojoItaliano
                        )
                    }
                }

                // 4. Paso a paso
                Text(
                    text = "📖 Paso a paso",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )

                // Cuántos pasos están hechos y cuántos son en total (igual que con los ingredientes)
                val totalPasos = cursoActual.pasos.size
                val totalCompletados = pasosCompletados.size

                // Progreso de 0f a 1f, revisando que no se divida por cero
                val progresoPasos = if (totalPasos > 0) {
                    totalCompletados.toFloat() / totalPasos
                } else {
                    0f
                }
                // La barra avanza suave al completar un paso
                val progresoPasosAnimado by animateFloatAsState(
                    targetValue = progresoPasos,
                    label = "progresoPasos"
                )

                Text(
                    text = "$totalCompletados de $totalPasos pasos completados",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VerdeOscuro
                )
                LinearProgressIndicator(
                    progress = { progresoPasosAnimado },
                    color = VerdeItaliano,
                    trackColor = GrisBorde,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                // forEachIndexed entrega la posición (indice, parte en 0) y el texto de cada paso.
                // El indice es lo que se guarda en Room; en pantalla se muestra indice + 1.
                Column {
                    cursoActual.pasos.forEachIndexed { indice, paso ->
                        FilaPaso(
                            numero = indice + 1,
                            texto = paso,
                            completado = pasosCompletados.contains(indice),
                            onAlternar = { viewModel.alternarPaso(indice) }
                        )
                    }
                }

                // Botón para desmarcar todos los pasos: solo aparece si hay al menos uno completado
                if (totalCompletados > 0) {
                    TextButton(
                        onClick = { viewModel.reiniciarPasos() },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = "Reiniciar pasos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = RojoItaliano
                        )
                    }
                }

                // Mensaje de felicitación cuando se completan TODOS los pasos.
                // AnimatedVisibility: en vez de aparecer de golpe, el mensaje entra con una
                // animación (se despliega) y sale igual al desmarcar un paso o reiniciar.
                // totalPasos > 0: una receta sin pasos no debe mostrarse como "terminada".
                AnimatedVisibility(visible = totalPasos > 0 && totalCompletados == totalPasos) {
                    Text(
                        text = "¡Receta terminada! 🎉",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlancoPuro,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VerdeItaliano, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    )
                }

                // 5. Contacto con ventas, al final de la receta.
                // \" escribe una comilla dentro del texto: ...para preparar "Brioche Tradicional Italiano".
                ContactoVentas(
                    mensaje = "Hola, quiero información sobre ${cursoActual.insumo} para preparar \"${cursoActual.titulo}\"."
                )
            }
        }
    }
}

// UNA FILA DEL "PASO A PASO" (número o ✓ + texto del paso).
// Es private porque solo la usa esta pantalla. No guarda nada: recibe si está completado
// y avisa el toque con onAlternar; el ViewModel decide qué hacer (MVVM).
@Composable
private fun FilaPaso(
    numero: Int,
    texto: String,
    completado: Boolean,
    onAlternar: () -> Unit
) {
    // animateColorAsState: al completar el paso el color cambia de a poco (no de golpe).
    // Sin completar: círculo blanco con borde y número verde.
    // Completado: círculo verde relleno con ✓ blanco (como una casilla marcada).
    val colorFondo by animateColorAsState(
        targetValue = if (completado) VerdeItaliano else BlancoPuro,
        label = "fondoPaso"
    )
    val colorContenido by animateColorAsState(
        targetValue = if (completado) BlancoPuro else VerdeItaliano,
        label = "contenidoPaso"
    )

    // Toda la fila se puede tocar (mínimo 48 dp de alto), más fácil para usuarios mayores.
    // toggleable con Role.Checkbox: TalkBack lee el paso como una casilla "marcada/no marcada".
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(
                value = completado,
                role = Role.Checkbox,
                onValueChange = { onAlternar() }
            )
            .padding(vertical = 6.dp)
    ) {
        // Círculo de 36 dp: muestra el número o el ✓ según el estado
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .background(colorFondo, CircleShape)
                .border(2.dp, VerdeItaliano, CircleShape)
        ) {
            Text(
                text = if (completado) "✓" else "$numero",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorContenido
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        // Completado: texto en gris oscuro (0xFF666666), igual que los ingredientes marcados.
        // weight(1f): el texto usa el ancho que sobra y baja de línea si es largo.
        Text(
            text = texto,
            fontSize = 16.sp,
            color = if (completado) Color(0xFF666666) else TextoOscuro,
            modifier = Modifier.weight(1f)
        )
    }
}

// UN DATO CON SU ÍCONO (ej: 🕒 15 min). Lo usan duración, rendimiento y dificultad.
// Es private porque solo lo usa esta pantalla.
@Composable
private fun DatoConIcono(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Decorativo (contentDescription = null): el texto de al lado ya dice el dato
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = RojoItaliano,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = texto,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextoOscuro
        )
    }
}
