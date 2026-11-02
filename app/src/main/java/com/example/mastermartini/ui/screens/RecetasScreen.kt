package com.example.mastermartini.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano

// PANTALLA DE RECETAS POR SECCIÓN (EN CONSTRUCCIÓN)
// Por ahora solo muestra "Próximamente". Aquí irá el catálogo filtrado por categoría
// (usando CursoRepository.obtenerCategorias()).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecetasScreen(
    // Se llamará al tocar una receta, con su id. MainActivity ya navega a "detalle/{id}",
    // igual que desde Home. Todavía no se usa porque la lista aún no está hecha.
    onVerDetalle: (Int) -> Unit
) {
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
        // Box con contentAlignment = Center: deja el texto al medio de la pantalla
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "Próximamente",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
        }
    }
}
