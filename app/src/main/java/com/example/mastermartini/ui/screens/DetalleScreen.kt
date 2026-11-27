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
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.components.ContactoVentas
import com.example.mastermartini.ui.components.DatoConIcono
import com.example.mastermartini.ui.components.EtiquetaVegana
import com.example.mastermartini.ui.components.ImagenCabeceraCatalogo
import com.example.mastermartini.ui.theme.*
import com.example.mastermartini.viewmodel.DetalleViewModel

// Pantalla de detalle de receta con video, checklist de ingredientes y pasos en Room
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    cursoId: Int,
    viewModel: DetalleViewModel,
    onVolver: () -> Unit,
    onVerProducto: (Int) -> Unit
) {
    LaunchedEffect(cursoId) {
        viewModel.cargarCurso(cursoId)
    }

    val curso by viewModel.curso.collectAsState()
    val marcados by viewModel.marcados.collectAsState()
    val pasosCompletados by viewModel.pasosCompletados.collectAsState()
    val productoInsumo by viewModel.productoInsumo.collectAsState()

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = curso?.titulo ?: "Receta",
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
        val cursoActual = curso

        if (cursoActual == null) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Imagen principal de la receta antes de la ficha descriptiva
                ImagenCabeceraCatalogo(
                    imagenUrl = cursoActual.imagenUrl,
                    descripcion = cursoActual.titulo,
                    categoriaOTema = cursoActual.categoria,
                    alto = 190.dp,
                    contentScale = ContentScale.Crop,
                    forma = RoundedCornerShape(14.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = BlancoPuro),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cursoActual.categoria,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlancoPuro,
                                modifier = Modifier
                                    .background(VerdeItaliano, RoundedCornerShape(50))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            )
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
                        Text(
                            text = cursoActual.descripcion,
                            fontSize = 16.sp,
                            color = TextoOscuro
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val producto = productoInsumo
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
                                    text = "Insumo Master Martini: ${cursoActual.insumo}",
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.PlayCircle,
                        contentDescription = null,
                        tint = VerdeItaliano,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Video de la receta",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro
                    )
                }
                ReproductorVideoLocal(videoUrl = cursoActual.videoUrl)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingBasket,
                        contentDescription = null,
                        tint = VerdeItaliano,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ingredientes",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro
                    )
                }

                val totalIngredientes = cursoActual.ingredientes.size
                val totalListos = marcados.size

                val progreso = if (totalIngredientes > 0) {
                    totalListos.toFloat() / totalIngredientes
                } else {
                    0f
                }
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
                    progress = { progresoAnimado },
                    color = VerdeItaliano,
                    trackColor = GrisBorde,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                Column {
                    cursoActual.ingredientes.forEachIndexed { indice, ingrediente ->
                        val estaMarcado = marcados.contains(indice)

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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = null,
                        tint = VerdeItaliano,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Paso a paso",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro
                    )
                }

                val totalPasos = cursoActual.pasos.size
                val totalCompletados = pasosCompletados.size

                val progresoPasos = if (totalPasos > 0) {
                    totalCompletados.toFloat() / totalPasos
                } else {
                    0f
                }
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

                AnimatedVisibility(visible = totalPasos > 0 && totalCompletados == totalPasos) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VerdeItaliano, RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Celebration,
                            contentDescription = null,
                            tint = DoradoCalidad,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "¡Receta terminada!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlancoPuro
                        )
                    }
                }

                ContactoVentas(
                    mensaje = "Hola, quiero información sobre ${cursoActual.insumo} para preparar \"${cursoActual.titulo}\"."
                )
            }
        }
    }
}

// Fila de paso de la receta con estado completado persistido en Room
@Composable
private fun FilaPaso(
    numero: Int,
    texto: String,
    completado: Boolean,
    onAlternar: () -> Unit
) {
    val colorFondo by animateColorAsState(
        targetValue = if (completado) VerdeItaliano else BlancoPuro,
        label = "fondoPaso"
    )
    val colorContenido by animateColorAsState(
        targetValue = if (completado) BlancoPuro else VerdeItaliano,
        label = "contenidoPaso"
    )

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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .background(colorFondo, CircleShape)
                .border(2.dp, VerdeItaliano, CircleShape)
        ) {
            if (completado) {
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
        Text(
            text = texto,
            fontSize = 16.sp,
            color = if (completado) Color(0xFF666666) else TextoOscuro,
            modifier = Modifier.weight(1f)
        )
    }
}
