package com.example.mastermartini.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.mastermartini.ui.components.ChipOpcion
import com.example.mastermartini.ui.components.TarjetaReceta
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.OrdenRecetas
import com.example.mastermartini.viewmodel.RecetasViewModel

// Catálogo de recetas agrupadas por sección con filtros y ordenamiento
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecetasScreen(
    viewModel: RecetasViewModel,
    onVerDetalle: (Int) -> Unit,
    onBuscar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            TopAppBar(
                title = { Text(text = "Recetas", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onBuscar) {
                        Icon(
                            imageVector = Icons.Filled.Search,
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ChipOpcion(
                    texto = "Todas",
                    seleccionado = uiState.categoriaSeleccionada == null,
                    onClick = { viewModel.seleccionarCategoria(null) }
                )
                uiState.categorias.forEach { categoria ->
                    ChipOpcion(
                        texto = categoria,
                        seleccionado = uiState.categoriaSeleccionada == categoria,
                        onClick = { viewModel.seleccionarCategoria(categoria) }
                    )
                }
            }

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

            ChipOpcion(
                texto = "Solo veganas",
                seleccionado = uiState.soloVeganas,
                onClick = { viewModel.alternarSoloVeganas() },
                icono = Icons.Outlined.Eco,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.recetas.isEmpty()) {
                    item {
                        Text(
                            text = if (uiState.soloVeganas) {
                                "Todavía no hay recetas veganas en esta sección."
                            } else {
                                "Todavía no hay recetas en esta sección."
                            },
                            fontSize = 18.sp,
                            color = TextoOscuro
                        )
                    }
                } else if (uiState.categoriaSeleccionada == null) {
                    uiState.secciones.forEach { (categoria, recetasDeLaSeccion) ->
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
                        items(recetasDeLaSeccion, key = { curso -> curso.id }) { curso ->
                            TarjetaReceta(
                                curso = curso,
                                onVerDetalle = onVerDetalle,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                } else {
                    items(uiState.recetas, key = { curso -> curso.id }) { curso ->
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
