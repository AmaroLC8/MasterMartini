package com.example.mastermartini.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.model.Producto
import com.example.mastermartini.ui.components.BotonCatalogoPdf
import com.example.mastermartini.ui.components.EtiquetaVegana
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.ProductosViewModel

// PANTALLA "NUESTROS PRODUCTOS"
// Lista los productos Master Martini agrupados por línea. Tocar uno abre su detalle.
// Solo DIBUJA lo que le entrega el ProductosViewModel (MVVM).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    // ViewModel con los productos agrupados (lo crea MainActivity)
    viewModel: ProductosViewModel,
    // Flecha de volver: MainActivity hace popBackStack() (vuelve a Home)
    onVolver: () -> Unit,
    // Recibe el id del producto tocado; MainActivity navega a "producto/{id}"
    onVerProducto: (Int) -> Unit
) {
    val productosPorLinea by viewModel.productosPorLinea.collectAsState()

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            // Barra verde con flecha (se llega desde Home, no desde la barra inferior)
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
            // 1. Botón del catálogo PDF, arriba de todo
            item {
                BotonCatalogoPdf()
            }

            // 2. Por cada línea: un título y sus productos debajo.
            //    forEach entrega cada par (línea, lista de productos) del Map.
            productosPorLinea.forEach { (linea, productos) ->
                // key = "linea_..." : texto, para no chocar con los ids (números) de los productos
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

// TARJETA DE UN PRODUCTO: nombre, descripción corta y etiqueta vegana si corresponde.
// Toda la tarjeta se puede tocar. Es private porque solo la usa esta pantalla.
@Composable
private fun TarjetaProducto(
    producto: Producto,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        border = BorderStroke(1.dp, DoradoCalidad.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // weight(1f): los textos usan el ancho que deja la flecha
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
                Spacer(modifier = Modifier.height(4.dp))
                // maxLines = 2 + Ellipsis: si la descripción es larga, se corta con "..."
                // (la completa se ve en el detalle)
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
            // Flecha: indica que la tarjeta se abre. Decorativa (contentDescription = null)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = VerdeOscuro
            )
        }
    }
}
