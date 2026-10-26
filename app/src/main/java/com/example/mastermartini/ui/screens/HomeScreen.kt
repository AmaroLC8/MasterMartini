package com.example.mastermartini.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.mastermartini.R
import com.example.mastermartini.viewmodel.HomeViewModel

// El molde de datos (Curso) está en model/Curso.kt y la lista en data/CursoRepository.kt.
// Esta pantalla solo DIBUJA los cursos que le entrega el HomeViewModel (MVVM).

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // ViewModel que entrega el catálogo de cursos y los favoritos (lo crea MainActivity)
    viewModel: HomeViewModel,
    // Datos del perfil y la acción del botón, que manda el MainActivity
    nombreUsuario: String,
    rubroUsuario: String,
    rutUsuario: String,
    correoUsuario: String,
    onEditarDatos: () -> Unit // MainActivity decide a dónde navegar (abre "registro")
) {
    // Colores estilo bandera italiana de Master Martini
    val verdeItaliano = Color(0xFF008C45)
    val rojoItaliano = Color(0xFFCD212A)

    // Escuchamos la lista de cursos del ViewModel. collectAsState() convierte el StateFlow
    // en un estado de Compose: si la lista cambia, la pantalla se vuelve a dibujar sola.
    val catalogoCursos by viewModel.cursos.collectAsState()

    // Favoritos guardados en Room: los ids marcados y cuántos son.
    // Cuando se toca un corazón, Room avisa el cambio y estos valores se actualizan solos.
    val favoritos by viewModel.favoritos.collectAsState()
    val totalFavoritos by viewModel.totalFavoritos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Catálogo Master Martini B2B",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        // 3. LazyColumn: Es una columna con scroll automático ideal para listas de elementos
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item{
                Text(
                    text = "MasterClass en video (Offline):",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                ReproductorVideoLocal()
            }
            // Bloque 1: Franja tricolor italiana y Tarjeta con los datos guardados en el celular
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_master_martini),
                        contentDescription = "Logo Master Martini",
                        modifier = Modifier
                            .width(200.dp)
                            .height(65.dp)
                            .padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .width(120.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(verdeItaliano))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.White))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(rojoItaliano))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tarjeta que demuestra al profesor que los datos se leyeron desde AlmacenamientoLocal
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "¡Bienvenido, $nombreUsuario!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Rubro del Taller: $rubroUsuario", fontWeight = FontWeight.SemiBold, color = verdeItaliano)
                        Text(text = "RUT Registrado: $rutUsuario", fontSize = 14.sp)
                        Text(text = "Correo: $correoUsuario", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ Datos recuperados del almacenamiento local",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Bloque 2: Título de la sección de cursos y contador de favoritos
            item {
                Text(
                    text = "Masterclasses y Recetas Técnicas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Ícono + texto (accesibilidad): el número sale de Room y se actualiza solo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null, // el texto de al lado ya lo explica
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mis favoritos: $totalFavoritos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Bloque 3: Equivalente al ciclo 'for (curso in catalogoCursos)'
            // Por cada curso en la lista, dibuja una Card en la pantalla.
            // key = id: Compose sabe qué tarjeta es cuál, así cada corazón mantiene su propia animación.
            items(catalogoCursos, key = { curso -> curso.id }) { curso ->
                // ¿Este curso está en la lista de favoritos que entrega Room?
                val esFavorito = favoritos.contains(curso.id)

                // ANIMACIÓN DEL CORAZÓN: al marcarlo crece a 1.3 veces su tamaño con un
                // pequeño rebote (spring); al desmarcarlo vuelve a su tamaño normal (1).
                val escalaCorazon by animateFloatAsState(
                    targetValue = if (esFavorito) 1.3f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "escalaCorazon"
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Row: los datos del curso a la izquierda y el corazón a la derecha
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // weight(1f): los textos ocupan todo el espacio que deja libre el corazón
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = curso.titulo,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Insumo principal: ${curso.insumo}", fontSize = 14.sp)
                            Text(text = "Instructor: ${curso.chef}", fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⏱️ Duración: ${curso.duracion}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = rojoItaliano
                            )
                        }

                        // Botón de favorito: la pantalla solo avisa al ViewModel; él decide si agrega o quita.
                        // 56 dp: área táctil grande para usuarios mayores.
                        IconButton(
                            onClick = { viewModel.alternarFavorito(curso.id) },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                // Corazón lleno si es favorito, solo el borde si no
                                imageVector = if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                // TalkBack lee qué pasará al tocarlo
                                contentDescription = if (esFavorito) {
                                    "Quitar ${curso.titulo} de favoritos"
                                } else {
                                    "Agregar ${curso.titulo} a favoritos"
                                },
                                // Colores del tema: rojo (secondary) si es favorito, gris si no
                                tint = if (esFavorito) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .scale(escalaCorazon)
                            )
                        }
                    }
                }
            }

            // Bloque 4: Botón para regresar al formulario (Navegación funcional)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onEditarDatos,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Editar Datos / Volver al Registro", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}