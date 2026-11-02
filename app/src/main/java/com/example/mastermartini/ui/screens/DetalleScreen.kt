package com.example.mastermartini.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onVolver: () -> Unit
) {
    // LaunchedEffect(cursoId) se ejecuta UNA vez al abrir la pantalla, y de nuevo solo si cambia cursoId.
    // Si llamáramos cargarCurso() directo en el composable, se repetiría en cada redibujo.
    LaunchedEffect(cursoId) {
        viewModel.cargarCurso(cursoId)
    }

    // Escuchamos el curso del ViewModel: cuando cargarCurso() lo encuentra, la pantalla se redibuja sola
    val curso by viewModel.curso.collectAsState()

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
                        Text(
                            text = "Insumo Master Martini: ${cursoActual.insumo}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VerdeItaliano
                        )
                        Text(
                            text = "Instructor: ${cursoActual.chef}",
                            fontSize = 16.sp,
                            color = TextoOscuro
                        )
                        Text(
                            text = "⏱️ Duración: ${cursoActual.duracion}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = RojoItaliano
                        )
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

                // 3. Ingredientes (lista simple con viñetas; el checklist será la siguiente tarea)
                Text(
                    text = "🧺 Ingredientes",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
                // forEach recorre la lista y dibuja una fila por cada ingrediente
                cursoActual.ingredientes.forEach { ingrediente ->
                    Row(verticalAlignment = Alignment.Top) {
                        // Viñeta "•" en verde, con el mismo tamaño de letra que el texto
                        Text(
                            text = "•",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = VerdeItaliano
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        // weight(1f): igual que en los pasos, el texto baja de línea si es largo
                        Text(
                            text = ingrediente,
                            fontSize = 16.sp,
                            color = TextoOscuro,
                            modifier = Modifier.weight(1f)
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
                // forEachIndexed entrega la posición (indice, parte en 0) y el texto de cada paso.
                // Se muestra indice + 1 para que la numeración parta en 1.
                cursoActual.pasos.forEachIndexed { indice, paso ->
                    Row(verticalAlignment = Alignment.Top) {
                        // Número dentro de un círculo verde
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .background(VerdeItaliano, CircleShape)
                        ) {
                            Text(
                                text = "${indice + 1}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlancoPuro
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        // weight(1f): el texto usa el ancho que sobra y baja de línea si es largo
                        Text(
                            text = paso,
                            fontSize = 16.sp,
                            color = TextoOscuro,
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
