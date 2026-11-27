package com.example.mastermartini.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.components.CargandoContenido
import com.example.mastermartini.ui.components.TarjetaError
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.EstadoUi
import com.example.mastermartini.viewmodel.FavoritosViewModel

// Pantalla que muestra las recetas guardadas como favoritas en Room
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen(
    viewModel: FavoritosViewModel,
    onVerDetalle: (Int) -> Unit,
    onIrARecetas: () -> Unit
) {
    val estadoFavoritos by viewModel.cursosFavoritos.collectAsState()
    // Favoritos ya cruzados con las recetas del servidor; vacía mientras carga o si hubo error
    val cursosFavoritos = (estadoFavoritos as? EstadoUi.Exito)?.datos ?: emptyList()

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (estadoFavoritos is EstadoUi.Exito) {
                            "Mis favoritos (${cursosFavoritos.size})"
                        } else {
                            "Mis favoritos"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdeItaliano,
                    titleContentColor = BlancoPuro
                )
            )
        }
    ) { innerPadding ->
        val estado = estadoFavoritos
        if (estado is EstadoUi.Cargando) {
            CargandoContenido(modifier = Modifier.padding(innerPadding))
        } else if (estado is EstadoUi.Error) {
            // Sin servidor no se sabe qué recetas son: se avisa en vez de mostrar una lista vacía
            TarjetaError(
                mensaje = estado.mensaje,
                onReintentar = { viewModel.reintentar() },
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        } else if (cursosFavoritos.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = RojoItaliano,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Aún no tienes recetas favoritas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Toca el corazón de una receta para guardarla aquí.",
                    fontSize = 16.sp,
                    color = TextoOscuro,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onIrARecetas,
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeItaliano),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Ver recetas", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(cursosFavoritos, key = { curso -> curso.id }) { curso ->
                    Card(
                        onClick = { onVerDetalle(curso.id) },
                        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = curso.categoria,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BlancoPuro,
                                        modifier = Modifier
                                            .background(VerdeItaliano, RoundedCornerShape(50))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = curso.titulo,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextoOscuro
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.quitarFavorito(curso.id) },
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = "Quitar ${curso.titulo} de favoritos",
                                        tint = RojoItaliano
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Instructor: ${curso.chef}",
                                fontSize = 15.sp,
                                color = TextoOscuro
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint = DoradoCalidad,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Duración: ${curso.duracion}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RojoItaliano
                                )
                            }

                            TextButton(
                                onClick = { onVerDetalle(curso.id) },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .heightIn(min = 48.dp)
                            ) {
                                Text(
                                    text = "Ver receta",
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
        }
    }
}
