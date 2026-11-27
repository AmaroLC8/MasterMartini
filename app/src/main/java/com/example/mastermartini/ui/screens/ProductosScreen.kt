package com.example.mastermartini.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.model.Producto
import com.example.mastermartini.ui.components.BotonCatalogoPdf
import com.example.mastermartini.ui.components.EtiquetaVegana
import com.example.mastermartini.ui.components.ImagenCabeceraCatalogo
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.GrisBorde
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.ProductosViewModel

// Catálogo de productos Master Martini agrupados por línea
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    viewModel: ProductosViewModel,
    onVolver: () -> Unit,
    onVerProducto: (Int) -> Unit
) {
    val productosPorLinea by viewModel.productosPorLinea.collectAsState()

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            TopAppBar(
                title = { Text(text = "Nuestros productos", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                BotonCatalogoPdf()
            }

            productosPorLinea.forEach { (linea, productos) ->
                item(key = "linea_$linea") {
                    Text(
                        text = linea,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerdeOscuro,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(productos, key = { producto -> producto.id }) { producto ->
                    TarjetaProducto(
                        producto = producto,
                        onClick = { onVerProducto(producto.id) }
                    )
                }
            }
        }
    }
}

// Tarjeta resumida de un producto dentro del listado
@Composable
private fun TarjetaProducto(
    producto: Producto,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Cabecera visual con ajuste Fit sobre fondo blanco para no recortar el envase
            ImagenCabeceraCatalogo(
                imagenUrl = producto.imagenUrl,
                descripcion = producto.nombre,
                categoriaOTema = producto.linea,
                alto = 135.dp,
                contentScale = ContentScale.Fit,
                fondoContenedor = BlancoPuro,
                insigniaTexto = producto.linea
            )
            HorizontalDivider(color = GrisBorde.copy(alpha = 0.4f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = producto.descripcion,
                        fontSize = 15.sp,
                        color = TextoOscuro,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (producto.esVegano) {
                        Spacer(modifier = Modifier.height(8.dp))
                        EtiquetaVegana()
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = VerdeOscuro
                )
            }
        }
    }
}
