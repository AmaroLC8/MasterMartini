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

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder

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
    onEditarDatos: () -> Unit,// MainActivity decide a dónde navegar (abre "registro"
    onIrAEncuesta: () -> Unit
) {
    val cursos by viewModel.cursos.collectAsState()

    // Colores estilo bandera italiana de Master Martini
    val verdeItaliano = Color(0xFF008C45)
    val rojoItaliano = Color(0xFFCD212A)
    val doradoCalidad = Color(0xFFC59B27)
    val blancoHueso = Color(0xFFF9F9F7)
    val textoOscuro = Color(0xFF1E232A)

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = doradoCalidad,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Master Martini B2B",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }

                        // Contador de Favoritos en la TopBar
                        if (totalFavoritos > 0) {
                        Surface(
                            color = rojoItaliano,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$totalFavoritos",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = verdeItaliano,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = blancoHueso
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Encabezado Corporativo (Logo + Franja Tricolor Italiana)
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_master_martini),
                        contentDescription = "Logo Master Martini",
                        modifier = Modifier
                            .width(200.dp)
                            .height(65.dp)
                            .padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .width(130.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(verdeItaliano))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.White))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(rojoItaliano))
                    }
                }
            }

            // 2. MasterClass Video Destacado (Reproductor Offline)
            item {
                Text(
                    text = "🎥 MasterClass Destacada (Video Offline):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textoOscuro
                )
                Spacer(modifier = Modifier.height(6.dp))
                ReproductorVideoLocal()
            }

            // 3. Tarjeta de Bienvenida con Datos del Perfil
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, verdeItaliano),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "¡Bienvenido, $nombreUsuario!",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = verdeItaliano
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Rubro del Taller: $rubroUsuario", fontWeight = FontWeight.SemiBold, color = textoOscuro)
                        Text(text = "RUT Empresa: $rutUsuario", fontSize = 14.sp, color = textoOscuro)
                        Text(text = "Contacto: $correoUsuario", fontSize = 14.sp, color = textoOscuro)
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = verdeItaliano.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "✓ Perfil verificado desde memoria interna (SharedPreferences)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = verdeItaliano,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 4. Título del Catálogo de Recetas
            item {
                Text(
                    text = "📖 Catálogo de Recetas Técnicas e Insumos",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textoOscuro
                )
            }

            // 5. Lista Dinámica de Cursos / Recetas
            items(catalogoCursos, key = { it.id }) { curso ->
                val esFavorito = favoritos.contains(curso.id)

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, doradoCalidad.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = doradoCalidad,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = curso.titulo,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textoOscuro
                                )
                            }

                            // Botón de Favorito (Corazón)
                            IconButton(onClick = { viewModel.alternarFavorito(curso.id) }) {
                                Icon(
                                    imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorito",
                                    tint = if (esFavorito) rojoItaliano else Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Insumo Master Martini: ${curso.insumo}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = verdeItaliano
                        )
                        Text(
                            text = "Instructor: ${curso.chef}",
                            fontSize = 14.sp,
                            color = textoOscuro
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⏱️ Duración: ${curso.duracion}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = rojoItaliano
                            )
                            Text(
                                text = "Certificación B2B",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = doradoCalidad
                            )
                        }
                    }
                }
            }

            // 6. Botón Encuesta de Preferencias &amp; GPS
            item {
                Button(
                    onClick = onIrAEncuesta,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = verdeItaliano)
                ) {
                    Text(
                        text = "📝 Encuesta de Preferencias &amp; Ubicación (GPS)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

            // 7. Botón Editar Datos
            item {
                OutlinedButton(
                    onClick = onEditarDatos,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, rojoItaliano)
                ) {
                    Text(
                        text = "Editar Datos del Registro",
                        fontWeight = FontWeight.Bold,
                        color = rojoItaliano
                    )
                }
            }
        }
    }
}

