package com.example.mastermartini.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Icecream
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro

// Cabecera visual con carga desde Cloudinary, respaldo degradado e insignia de contraste
@Composable
fun ImagenCabeceraCatalogo(
    imagenUrl: String?,
    descripcion: String,
    categoriaOTema: String,
    modifier: Modifier = Modifier,
    alto: Dp = 135.dp,
    contentScale: ContentScale = ContentScale.Crop,
    fondoContenedor: Color = BlancoPuro,
    insigniaTexto: String? = null,
    forma: Shape? = null
) {
    val iconoRespaldo = iconoPorTema(categoriaOTema)
    val contenedorBase = modifier
        .fillMaxWidth()
        .height(alto)
        .let { if (forma != null) it.clip(forma) else it }

    Box(modifier = contenedorBase) {
        if (imagenUrl.isNullOrBlank()) {
            FondoRespaldoImagen(icono = iconoRespaldo)
        } else {
            val paddingImagen = if (contentScale == ContentScale.Fit) 8.dp else 0.dp
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(fondoContenedor)
            ) {
                SubcomposeAsyncImage(
                    model = imagenUrl,
                    contentDescription = descripcion,
                    contentScale = contentScale,
                    loading = { FondoRespaldoImagen(icono = iconoRespaldo) },
                    error = { FondoRespaldoImagen(icono = iconoRespaldo) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingImagen)
                )
            }
        }

        // Insignia superior derecha sobre la imagen
        if (!insigniaTexto.isNullOrBlank()) {
            Surface(
                color = VerdeOscuro.copy(alpha = 0.90f),
                contentColor = BlancoPuro,
                shape = RoundedCornerShape(50),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                Text(
                    text = insigniaTexto,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// Fondo suave con degradado corporativo mientras carga o ante falta de conexión
@Composable
private fun FondoRespaldoImagen(icono: ImageVector) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        VerdeItaliano.copy(alpha = 0.22f),
                        DoradoCalidad.copy(alpha = 0.30f)
                    )
                )
            )
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = VerdeOscuro.copy(alpha = 0.75f),
            modifier = Modifier.size(44.dp)
        )
    }
}

// Selecciona un ícono temático según la categoría de receta, curso o línea de producto
private fun iconoPorTema(tema: String): ImageVector {
    val texto = tema.lowercase()
    return when {
        texto.contains("chocolate") || texto.contains("cacao") -> Icons.Outlined.LocalCafe
        texto.contains("pan") || texto.contains("margarina") || texto.contains("manteca") -> Icons.Outlined.BakeryDining
        texto.contains("helad") -> Icons.Outlined.Icecream
        texto.contains("pastel") || texto.contains("decora") || texto.contains("crema") -> Icons.Outlined.Cake
        texto.contains("fritura") -> Icons.Outlined.Inventory2
        else -> Icons.AutoMirrored.Outlined.MenuBook
    }
}
