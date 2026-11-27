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
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
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
import com.example.mastermartini.data.DatosEmpresa
import com.example.mastermartini.viewmodel.HomeViewModel

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder

// Pantalla principal con MasterClass destacada, perfil del taller y catálogo de recetas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    nombreUsuario: String,
    rubroUsuario: String,
    rutUsuario: String,
    correoUsuario: String,
    onEditarDatos: () -> Unit,
    onIrAEncuesta: () -> Unit,
    onVerDetalle: (Int) -> Unit,
    onVerProductos: () -> Unit,
    onBuscar: () -> Unit
) {
    val verdeItaliano = Color(0xFF008C45)
    val rojoItaliano = Color(0xFFCD212A)
    val doradoCalidad = Color(0xFFC59B27)
    val blancoHueso = Color(0xFFF9F9F7)
    val textoOscuro = Color(0xFF1E232A)

    val catalogoCursos by viewModel.cursos.collectAsState()

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
                actions = {
                    IconButton(onClick = onBuscar) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar recetas",
                            tint = Color.White
                        )
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

            item {
                Text(
                    text = "🎥 MasterClass destacada",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textoOscuro
                )
                Spacer(modifier = Modifier.height(6.dp))
                ReproductorVideoLocal(
                    videoUrl = DatosEmpresa.URL_VIDEO_MASTERCLASS
                )
            }

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

            item {
                Card(
                    onClick = onVerProductos,
                    colors = CardDefaults.cardColors(containerColor = verdeItaliano),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Inventory2,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Nuestros productos",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Margarinas, cremas, chocolates y más",
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            item {
                Text(
                    text = "📖 Catálogo de Recetas Técnicas e Insumos",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textoOscuro
                )
            }

            items(catalogoCursos, key = { it.id }) { curso ->
                val esFavorito = favoritos.contains(curso.id)

                val escalaCorazon by animateFloatAsState(
                    targetValue = if (esFavorito) 1.3f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "escalaCorazon"
                )

                Card(
                    onClick = { onVerDetalle(curso.id) },
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

                            IconButton(
                                onClick = { viewModel.alternarFavorito(curso.id) },
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = if (esFavorito) {
                                        "Quitar ${curso.titulo} de favoritos"
                                    } else {
                                        "Agregar ${curso.titulo} a favoritos"
                                    },
                                    tint = if (esFavorito) rojoItaliano else Color.Gray,
                                    modifier = Modifier.scale(escalaCorazon)
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
                                color = verdeItaliano
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = verdeItaliano
                            )
                        }
                    }
                }
            }

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
                        text = "📝 Encuesta de Preferencias y Ubicación (GPS)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

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

