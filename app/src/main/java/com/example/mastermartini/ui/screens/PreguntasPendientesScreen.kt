package com.example.mastermartini.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.model.Pregunta
import com.example.mastermartini.ui.components.ErrorConReintentar
import com.example.mastermartini.ui.components.IconoValidacion
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.EstadoUi
import com.example.mastermartini.viewmodel.PreguntasPendientesViewModel

// Panel para Instructor y Administrador con las preguntas que faltan por responder
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreguntasPendientesScreen(
    viewModel: PreguntasPendientesViewModel,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

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
                title = { Text(text = "Preguntas pendientes", fontWeight = FontWeight.Bold) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val estado = uiState.estado) {
                is EstadoUi.Cargando -> {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(color = VerdeItaliano)
                    }
                }

                is EstadoUi.Error -> {
                    ErrorConReintentar(mensaje = estado.mensaje, onReintentar = { viewModel.cargar() })
                }

                is EstadoUi.Exito -> {
                    if (estado.datos.isEmpty()) {
                        SinPreguntasPendientes()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(estado.datos, key = { pregunta -> pregunta.id }) { pregunta ->
                                TarjetaPreguntaPendiente(
                                    pregunta = pregunta,
                                    respuesta = uiState.respuestas[pregunta.id].orEmpty(),
                                    error = uiState.errores[pregunta.id],
                                    enviando = pregunta.id in uiState.enviando,
                                    onRespuestaChange = { texto -> viewModel.onRespuestaChange(pregunta.id, texto) },
                                    onResponder = { viewModel.responder(pregunta) },
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

// Estado vacío cuando no quedan preguntas por responder
@Composable
private fun SinPreguntasPendientes() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = VerdeItaliano,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "No hay preguntas pendientes por responder",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextoOscuro,
            textAlign = TextAlign.Center
        )
    }
}

// Tarjeta con los datos de la pregunta, el campo de respuesta y el botón Responder
@Composable
private fun TarjetaPreguntaPendiente(
    pregunta: Pregunta,
    respuesta: String,
    error: String?,
    enviando: Boolean,
    onRespuestaChange: (String) -> Unit,
    onResponder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        border = BorderStroke(1.dp, DoradoCalidad.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = pregunta.cursoTitulo ?: "Curso ${pregunta.cursoId}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeOscuro
            )
            val autor = pregunta.autorNombre ?: "Alumno"
            val fecha = pregunta.fechaLegible
            Text(
                text = if (fecha.isEmpty()) autor else "$autor · $fecha",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoOscuro
            )
            Text(text = pregunta.texto, fontSize = 16.sp, color = TextoOscuro)

            OutlinedTextField(
                value = respuesta,
                onValueChange = onRespuestaChange,
                label = { Text("Tu respuesta", fontSize = 16.sp) },
                trailingIcon = { IconoValidacion(valor = respuesta, error = error) },
                isError = error != null,
                supportingText = {
                    AnimatedVisibility(visible = error != null) {
                        Text(text = error ?: "", color = RojoItaliano, fontSize = 16.sp)
                    }
                },
                enabled = !enviando,
                minLines = 2,
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
                onClick = onResponder,
                enabled = !enviando,
                colors = ButtonDefaults.buttonColors(containerColor = VerdeItaliano, contentColor = BlancoPuro),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                if (enviando) {
                    CircularProgressIndicator(color = BlancoPuro, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Responder", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
