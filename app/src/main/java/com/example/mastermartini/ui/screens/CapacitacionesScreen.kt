package com.example.mastermartini.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.ui.components.ChipOpcion
import com.example.mastermartini.ui.components.EtiquetaAcceso
import com.example.mastermartini.ui.components.EtiquetaVegana
import com.example.mastermartini.ui.components.ImagenCabeceraCatalogo
import com.example.mastermartini.ui.components.TarjetaError
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.GrisBorde
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.CapacitacionesViewModel
import com.example.mastermartini.viewmodel.EstadoUi
import com.example.mastermartini.viewmodel.MiCurso
import androidx.compose.runtime.LaunchedEffect

// Pantalla del catálogo de cursos online con filtros, sección "Mis cursos" y Plan Pro
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapacitacionesScreen(
    viewModel: CapacitacionesViewModel,
    onVerCapacitacion: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Recarga catálogo y progreso al volver a la pantalla
    LaunchedEffect(Unit) {
        viewModel.recargarCapacitaciones()
    }

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            TopAppBar(
                title = { Text(text = "Cursos online", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdeItaliano,
                    titleContentColor = BlancoPuro
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Text(
                    text = "Aprende a tu ritmo con nuestros productos y obtén tu certificado",
                    fontSize = 18.sp,
                    color = TextoOscuro
                )
            }

            when (val estado = uiState.estado) {
                is EstadoUi.Cargando -> {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp)
                        ) {
                            CircularProgressIndicator(color = VerdeItaliano)
                        }
                    }
                }

                is EstadoUi.Error -> {
                    item {
                        TarjetaError(
                            mensaje = estado.mensaje,
                            onReintentar = { viewModel.recargarCapacitaciones() }
                        )
                    }
                }

                is EstadoUi.Exito -> {
                    item {
                        Text(
                            text = "Mis cursos",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = VerdeOscuro
                        )
                    }
                    val errorMisCursos = uiState.errorMisCursos
                    if (errorMisCursos != null) {
                        item {
                            TarjetaError(
                                mensaje = errorMisCursos,
                                onReintentar = { viewModel.recargarCapacitaciones() }
                            )
                        }
                    } else if (uiState.misCursos.isEmpty()) {
                        item {
                            Text(
                                text = "Aún no te inscribes en ningún curso",
                                fontSize = 16.sp,
                                color = TextoOscuro
                            )
                        }
                    } else {
                        items(uiState.misCursos, key = { miCurso -> "mi-${miCurso.cursoId}" }) { miCurso ->
                            TarjetaMiCurso(
                                miCurso = miCurso,
                                onAbrir = { onVerCapacitacion(miCurso.cursoId) }
                            )
                        }
                    }

                    item {
                        TarjetaPlanPro(precio = uiState.precioPlanPro)
                    }

                    if (uiState.totalCursos == 0) {
                        // El servidor respondió bien, pero todavía no hay cursos publicados
                        item {
                            Text(
                                text = "Aún no hay cursos publicados. Vuelve pronto.",
                                fontSize = 18.sp,
                                color = TextoOscuro
                            )
                        }
                    } else {
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                uiState.filtros.forEach { filtro ->
                                    ChipOpcion(
                                        texto = filtro,
                                        seleccionado = uiState.filtroSeleccionado == filtro,
                                        onClick = { viewModel.seleccionarFiltro(filtro) }
                                    )
                                }
                            }
                        }

                        if (estado.datos.isEmpty()) {
                            item {
                                Text(
                                    text = "Todavía no hay cursos en esta sección.",
                                    fontSize = 18.sp,
                                    color = TextoOscuro
                                )
                            }
                        } else {
                            items(estado.datos, key = { capacitacion -> capacitacion.id }) { capacitacion ->
                                TarjetaCapacitacion(
                                    capacitacion = capacitacion,
                                    onVerCapacitacion = onVerCapacitacion,
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Tarjeta resumen del avance en un curso inscrito
@Composable
private fun TarjetaMiCurso(
    miCurso: MiCurso,
    onAbrir: () -> Unit
) {
    Card(
        onClick = onAbrir,
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        border = BorderStroke(1.dp, VerdeItaliano),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = miCurso.titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
            LinearProgressIndicator(
                progress = { miCurso.porcentaje / 100f },
                color = VerdeItaliano,
                trackColor = GrisBorde,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            Text(
                text = "${miCurso.porcentaje}% completado",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = VerdeOscuro
            )
        }
    }
}

// Tarjeta informativa destacada de la suscripción Plan Pro
@Composable
private fun TarjetaPlanPro(precio: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        border = BorderStroke(2.dp, DoradoCalidad),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.WorkspacePremium,
                contentDescription = null,
                tint = DoradoCalidad,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Plan Pro: todos los cursos avanzados por $precio al mes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Los cursos básicos son gratis para siempre.",
                    fontSize = 16.sp,
                    color = TextoOscuro
                )
            }
        }
    }
}

// Tarjeta de curso online dentro del catálogo
@Composable
private fun TarjetaCapacitacion(
    capacitacion: Capacitacion,
    onVerCapacitacion: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onVerCapacitacion(capacitacion.id) },
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            // Cabecera visual del curso con categoría en esquina superior derecha
            ImagenCabeceraCatalogo(
                imagenUrl = capacitacion.imagenUrl,
                descripcion = capacitacion.titulo,
                categoriaOTema = capacitacion.categoria,
                alto = 135.dp,
                contentScale = ContentScale.Crop,
                insigniaTexto = capacitacion.categoria
            )

            Column(modifier = Modifier.padding(16.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EtiquetaAcceso(esGratis = capacitacion.esGratis)
                    if (capacitacion.esVegano) {
                        EtiquetaVegana()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = capacitacion.titulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Nivel: ${capacitacion.nivel}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VerdeOscuro
                )
                Text(
                    text = "${capacitacion.modulos.size} módulos · ${capacitacion.totalLecciones} lecciones",
                    fontSize = 16.sp,
                    color = TextoOscuro
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = DoradoCalidad,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Duración: ${capacitacion.duracionTotal}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RojoItaliano
                    )
                }
                Text(
                    text = "Instructor: ${capacitacion.instructor}",
                    fontSize = 16.sp,
                    color = TextoOscuro
                )

                TextButton(
                    onClick = { onVerCapacitacion(capacitacion.id) },
                    modifier = Modifier
                        .align(Alignment.End)
                        .heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "Ver curso",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerdeOscuro
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = VerdeOscuro
                    )
                }
            }
        }
    }
}
