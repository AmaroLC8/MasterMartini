package com.example.mastermartini.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.R
import com.example.mastermartini.data.DatosEmpresa
import com.example.mastermartini.ui.components.CargandoContenido
import com.example.mastermartini.ui.components.ImagenCabeceraCatalogo
import com.example.mastermartini.ui.components.TarjetaError
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.EstadoUi
import com.example.mastermartini.viewmodel.HomeViewModel

// Pantalla principal con MasterClass destacada, perfil del taller y catálogo de recetas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    nombreUsuario: String,
    rubroUsuario: String,
    @Suppress("UNUSED_PARAMETER") rutUsuario: String,
    @Suppress("UNUSED_PARAMETER") correoUsuario: String,
    @Suppress("UNUSED_PARAMETER") onEditarDatos: () -> Unit,
    onIrAEncuesta: () -> Unit,
    onVerDetalle: (Int) -> Unit,
    onVerProductos: () -> Unit,
    onBuscar: () -> Unit
) {
    val estadoRecetas by viewModel.cursos.collectAsState()
    // Recetas para la lista; vacía mientras carga o si hubo error
    val catalogoCursos = (estadoRecetas as? EstadoUi.Exito)?.datos ?: emptyList()
    val favoritos by viewModel.favoritos.collectAsState()
    val totalFavoritos by viewModel.totalFavoritos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = DoradoCalidad,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Master Martini B2B",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = BlancoPuro
                            )
                        }

                        if (totalFavoritos > 0) {
                            Surface(
                                color = RojoItaliano,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = BlancoPuro,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$totalFavoritos",
                                        color = BlancoPuro,
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
                            tint = BlancoPuro
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdeItaliano,
                    titleContentColor = BlancoPuro
                )
            )
        },
        containerColor = BlancoHueso
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
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(VerdeItaliano))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(BlancoPuro))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(RojoItaliano))
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.PlayCircle,
                        contentDescription = null,
                        tint = VerdeItaliano,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Masterclass destacada",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextoOscuro
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                ReproductorVideoLocal(
                    videoUrl = DatosEmpresa.URL_VIDEO_MASTERCLASS
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BlancoPuro),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "¡Bienvenido, $nombreUsuario!",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = VerdeItaliano
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Storefront,
                                contentDescription = null,
                                tint = DoradoCalidad,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Especialidad: $rubroUsuario",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextoOscuro
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    onClick = onVerProductos,
                    colors = CardDefaults.cardColors(containerColor = VerdeItaliano),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(14.dp),
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
                            tint = BlancoPuro,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Nuestros productos",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlancoPuro
                            )
                            Text(
                                text = "Margarinas, cremas, chocolates y más",
                                fontSize = 15.sp,
                                color = BlancoPuro
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = BlancoPuro
                        )
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = null,
                        tint = VerdeItaliano,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Catálogo de Recetas Técnicas e Insumos",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro
                    )
                }
            }

            // Mientras llegan las recetas se ve la ruedita; si fallan, la tarjeta con Reintentar
            when (val estado = estadoRecetas) {
                is EstadoUi.Cargando -> item { CargandoContenido() }
                is EstadoUi.Error -> item {
                    TarjetaError(mensaje = estado.mensaje, onReintentar = { viewModel.reintentar() })
                }
                is EstadoUi.Exito -> Unit
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
                    colors = CardDefaults.cardColors(containerColor = BlancoPuro),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Cabecera visual de la receta con categoría sobre la imagen
                        ImagenCabeceraCatalogo(
                            imagenUrl = curso.imagenUrl,
                            descripcion = curso.titulo,
                            categoriaOTema = curso.categoria,
                            alto = 135.dp,
                            contentScale = ContentScale.Crop,
                            insigniaTexto = curso.categoria
                        )

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
                                        tint = DoradoCalidad,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = curso.titulo,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextoOscuro
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
                                        tint = if (esFavorito) RojoItaliano else Color.Gray,
                                        modifier = Modifier.scale(escalaCorazon)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Insumo Master Martini: ${curso.insumo}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VerdeOscuro
                            )
                            Text(
                                text = "Instructor: ${curso.chef}",
                                fontSize = 14.sp,
                                color = TextoOscuro
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
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
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RojoItaliano
                                    )
                                }
                                Text(
                                    text = "Certificación B2B",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DoradoCalidad
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
                                    color = VerdeItaliano
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = VerdeItaliano
                                )
                            }
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
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeItaliano)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Assignment,
                        contentDescription = null,
                        tint = BlancoPuro,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Encuesta de Preferencias y Ubicación (GPS)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = BlancoPuro
                    )
                }
            }
        }
    }
}

