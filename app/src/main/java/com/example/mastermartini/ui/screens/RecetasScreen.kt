package com.example.mastermartini.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.components.TarjetaReceta
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.OrdenRecetas
import com.example.mastermartini.viewmodel.RecetasViewModel

// PANTALLA DE RECETAS POR SECCIÓN
// Muestra todo el catálogo agrupado por sección, con botones para filtrar y ordenar.
// Solo DIBUJA lo que le entrega el RecetasViewModel (MVVM): el filtro, el orden y la
// agrupación se hacen en el ViewModel. Aquí solo se avisa qué botón tocó el usuario.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecetasScreen(
    // ViewModel con las recetas filtradas y ordenadas (lo crea MainActivity)
    viewModel: RecetasViewModel,
    // Recibe el id de la receta tocada; MainActivity navega a "detalle/{id}", igual que desde Home
    onVerDetalle: (Int) -> Unit
) {
    // Escuchamos el estado del ViewModel. Cada vez que se toca un filtro o un orden,
    // el ViewModel arma un estado nuevo y esta pantalla se vuelve a dibujar sola.
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            // Barra verde con el título (sin flecha: se llega desde la barra inferior, no desde otra pantalla)
            TopAppBar(
                title = { Text(text = "Recetas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdeItaliano,
                    titleContentColor = BlancoPuro
                )
            )
        }
    ) { innerPadding ->
        // Column: arriba los filtros (quedan fijos) y abajo la lista (es lo único que se desplaza)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // FILA DE SECCIONES: "Todas" + las 5 categorías.
            // horizontalScroll: si los botones no caben en el ancho del celular,
            // se pueden deslizar hacia el lado en vez de cortarse.
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // "Todas" está elegida cuando no hay ninguna categoría (null)
                ChipOpcion(
                    texto = "Todas",
                    seleccionado = uiState.categoriaSeleccionada == null,
                    onClick = { viewModel.seleccionarCategoria(null) }
                )
                // Un botón por cada categoría que entrega el ViewModel
                uiState.categorias.forEach { categoria ->
                    ChipOpcion(
                        texto = categoria,
                        seleccionado = uiState.categoriaSeleccionada == categoria,
                        onClick = { viewModel.seleccionarCategoria(categoria) }
                    )
                }
            }

            // FILA DE ORDEN: "Ordenar por:" y dos botones
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Ordenar por:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoOscuro
                )
                ChipOpcion(
                    texto = "A - Z",
                    seleccionado = uiState.orden == OrdenRecetas.ALFABETICO,
                    onClick = { viewModel.cambiarOrden(OrdenRecetas.ALFABETICO) }
                )
                ChipOpcion(
                    texto = "Más cortas",
                    seleccionado = uiState.orden == OrdenRecetas.DURACION,
                    onClick = { viewModel.cambiarOrden(OrdenRecetas.DURACION) }
                )
            }

            // LISTA DE RECETAS
            // weight(1f): la lista ocupa todo el alto que dejan libre las dos filas de arriba
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.recetas.isEmpty()) {
                    // Si una sección no tiene recetas, se avisa en vez de dejar la pantalla en blanco
                    item {
                        Text(
                            text = "Todavía no hay recetas en esta sección.",
                            fontSize = 18.sp,
                            color = TextoOscuro
                        )
                    }
                } else if (uiState.categoriaSeleccionada == null) {
                    // "TODAS": un título por sección y sus recetas debajo.
                    // secciones es un Map; forEach entrega cada par (categoría, lista de recetas).
                    uiState.secciones.forEach { (categoria, recetasDeLaSeccion) ->
                        // Título de la sección, ej: "Chocolatería (2)".
                        // key = "seccion_..." : un texto, para que no choque con los ids (números) de las recetas
                        item(key = "seccion_$categoria") {
                            Text(
                                text = "$categoria (${recetasDeLaSeccion.size})",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerdeOscuro,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .animateItem()
                            )
                        }
                        // Las recetas de esa sección, en el orden elegido
                        items(recetasDeLaSeccion, key = { curso -> curso.id }) { curso ->
                            TarjetaReceta(
                                curso = curso,
                                onVerDetalle = onVerDetalle,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                } else {
                    // UNA SECCIÓN ELEGIDA: solo sus recetas, sin títulos (el botón marcado ya dice cuál es)
                    items(uiState.recetas, key = { curso -> curso.id }) { curso ->
                        // animateItem(): como cada tarjeta tiene su key (el id), al cambiar de filtro
                        // o de orden las tarjetas se deslizan a su nuevo lugar en vez de saltar,
                        // y las que salen o entran se desvanecen suavemente.
                        TarjetaReceta(
                            curso = curso,
                            onVerDetalle = onVerDetalle,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

// BOTÓN DE OPCIÓN (FilterChip) que usan las dos filas: secciones y orden.
// Se escribe UNA vez aquí para no repetir los colores y el tamaño en los 8 botones.
// Es private porque solo lo usa esta pantalla.
@Composable
private fun ChipOpcion(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(text = texto, fontSize = 16.sp) },
        // Check cuando está elegido: así se nota cuál está marcado sin depender solo del color.
        // Si no está elegido, se pasa null y no se dibuja ningún ícono.
        leadingIcon = if (seleccionado) {
            { Icon(imageVector = Icons.Filled.Check, contentDescription = null) }
        } else {
            null
        },
        // Mismos colores que los chips del Registro
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = VerdeItaliano,
            selectedLabelColor = BlancoPuro,
            selectedLeadingIconColor = BlancoPuro,
            containerColor = BlancoPuro,
            labelColor = TextoOscuro
        ),
        // 48 dp de alto: botón grande y fácil de tocar (el normal mide 32 dp)
        modifier = Modifier.height(48.dp)
    )
}
