package com.example.mastermartini.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.mastermartini.ui.components.BotonCatalogoPdf
import com.example.mastermartini.ui.components.ContactoVentas
import com.example.mastermartini.ui.components.EtiquetaVegana
import com.example.mastermartini.ui.components.TarjetaReceta
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.ProductoDetalleViewModel

// Pantalla de detalle de producto Master Martini, usos y recetas asociadas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoDetalleScreen(
    productoId: Int,
    viewModel: ProductoDetalleViewModel,
    onVolver: () -> Unit,
    onVerReceta: (Int) -> Unit
) {
    LaunchedEffect(productoId) {
        viewModel.cargarProducto(productoId)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.producto?.nombre ?: "Producto",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
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
        val producto = uiState.producto

        if (producto == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "Producto no encontrado",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = producto.nombre,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeOscuro
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = producto.linea,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlancoPuro,
                        modifier = Modifier
                            .background(VerdeItaliano, RoundedCornerShape(50))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                    if (producto.esVegano) {
                        EtiquetaVegana(tamanoTexto = 15)
                    }
                }

                Text(
                    text = producto.descripcion,
                    fontSize = 17.sp,
                    color = TextoOscuro
                )

                Text(
                    text = "Usos recomendados",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
                producto.usos.forEach { uso ->
                    Text(
                        text = "•  $uso",
                        fontSize = 17.sp,
                        color = TextoOscuro
                    )
                }

                Text(
                    text = "Recetas con este producto",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
                if (uiState.recetas.isEmpty()) {
                    Text(
                        text = "Todavía no hay recetas con este producto en la app.",
                        fontSize = 16.sp,
                        color = TextoOscuro
                    )
                } else {
                    uiState.recetas.forEach { curso ->
                        TarjetaReceta(curso = curso, onVerDetalle = onVerReceta)
                    }
                }

                ContactoVentas(mensaje = "Hola, quiero información sobre ${producto.nombre}.")

                BotonCatalogoPdf()
            }
        }
    }
}
