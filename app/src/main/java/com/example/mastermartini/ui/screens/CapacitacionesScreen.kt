package com.example.mastermartini.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.ui.components.EtiquetaAcceso
import com.example.mastermartini.ui.components.EtiquetaVegana
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.CapacitacionesViewModel

// PANTALLA "CURSOS ONLINE" (pestaña Cursos de la barra inferior)
// Muestra el catálogo de cursos online con filtros, igual que RecetasScreen.
// Solo DIBUJA lo que le entrega el CapacitacionesViewModel (MVVM): el filtro y el precio
// en pesos se arman en el ViewModel. Aquí solo se avisa qué botón tocó el usuario.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapacitacionesScreen(
    // ViewModel con los cursos filtrados (lo crea MainActivity)
    viewModel: CapacitacionesViewModel,
    // Recibe el id del curso tocado; MainActivity navega a "capacitacion/{id}"
    onVerCapacitacion: (Int) -> Unit
) {
    // Escuchamos el estado: al tocar un filtro, el ViewModel arma un estado nuevo
    // y esta pantalla se vuelve a dibujar sola
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            // Barra verde sin flecha: se llega desde la barra inferior, igual que Recetas
            TopAppBar(
                title = { Text(text = "Cursos online", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdeItaliano,
                    titleContentColor = BlancoPuro
                )
            )
        }
    ) { innerPadding ->
        // Todo va dentro de la LazyColumn (y no fijo arriba como en Recetas) porque el texto
        // y la tarjeta del Plan Pro ocupan bastante: así en un celular chico todo se puede desplazar.
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Texto de bienvenida
            item {
                Text(
                    text = "Aprende a tu ritmo con nuestros productos y obtén tu certificado",
                    fontSize = 18.sp,
                    color = TextoOscuro
                )
            }

            // FASE 2: aquí irá la sección "Mis cursos" (los cursos en los que el usuario
            // se inscribió, con su progreso). Por ahora no hay inscripción.

            // 2. Tarjeta destacada del Plan Pro
            item {
                TarjetaPlanPro(precio = uiState.precioPlanPro)
            }

            // 3. Filtros: "Todos", "Gratis", "Plan Pro" y las categorías.
            // horizontalScroll: si no caben, se deslizan hacia el lado en vez de cortarse.
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    // ChipOpcion es el mismo botón de filtro de RecetasScreen (misma apariencia)
                    uiState.filtros.forEach { filtro ->
                        ChipOpcion(
                            texto = filtro,
                            seleccionado = uiState.filtroSeleccionado == filtro,
                            onClick = { viewModel.seleccionarFiltro(filtro) }
                        )
                    }
                }
            }

            // 4. Lista de cursos
            if (uiState.capacitaciones.isEmpty()) {
                // Si un filtro no tiene cursos, se avisa en vez de dejar la pantalla en blanco
                item {
                    Text(
                        text = "Todavía no hay cursos en esta sección.",
                        fontSize = 18.sp,
                        color = TextoOscuro
                    )
                }
            } else {
                // key = el id: así, al cambiar de filtro, las tarjetas se animan a su nuevo lugar
                items(uiState.capacitaciones, key = { capacitacion -> capacitacion.id }) { capacitacion ->
                    TarjetaCapacitacion(
                        capacitacion = capacitacion,
                        onVerCapacitacion = onVerCapacitacion,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

// TARJETA DESTACADA DEL PLAN PRO
// Solo informa: la suscripción todavía no se puede contratar desde la app (fase 2).
// El precio llega YA escrito ("$9.990") desde el ViewModel.
@Composable
private fun TarjetaPlanPro(precio: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        // Borde dorado más grueso: la destaca de las tarjetas de curso
        border = BorderStroke(2.dp, DoradoCalidad),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Decorativo (contentDescription = null): el texto de al lado ya lo explica
            Icon(
                imageVector = Icons.Filled.WorkspacePremium,
                contentDescription = null,
                tint = DoradoCalidad,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Plan Pro: todos los cursos avanzados por $precio al mes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Los cursos básicos son gratis para siempre.",
                    fontSize = 16.sp,
                    color = TextoOscuro
                )
            }
        }
    }
}

// TARJETA DE UN CURSO ONLINE
// Mismo estilo que TarjetaReceta: toda la tarjeta se puede tocar y abre el detalle.
// Es private porque solo la usa esta pantalla.
@Composable
private fun TarjetaCapacitacion(
    capacitacion: Capacitacion,
    onVerCapacitacion: (Int) -> Unit,
    // Lo usa la LazyColumn para pasarle Modifier.animateItem() (igual que en Recetas)
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onVerCapacitacion(capacitacion.id) },
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        border = BorderStroke(1.dp, DoradoCalidad.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Etiquetas: GRATIS o PLAN PRO, la categoría y (si corresponde) "Apto vegano".
            // FlowRow: si no caben en una línea, la última baja a la siguiente en vez de cortarse.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EtiquetaAcceso(esGratis = capacitacion.esGratis)
                // Categoría con el mismo estilo que en TarjetaReceta
                Text(
                    text = capacitacion.categoria,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlancoPuro,
                    modifier = Modifier
                        .background(VerdeItaliano, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
                if (capacitacion.esVegano) {
                    EtiquetaVegana()
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = capacitacion.titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Nivel: ${capacitacion.nivel}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = VerdeOscuro
            )
            // modulos.size = cuántos módulos; totalLecciones es la propiedad calculada del modelo
            Text(
                text = "${capacitacion.modulos.size} módulos · ${capacitacion.totalLecciones} lecciones",
                fontSize = 16.sp,
                color = TextoOscuro
            )
            Text(
                text = "⏱️ Duración: ${capacitacion.duracionTotal}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = RojoItaliano
            )
            Text(
                text = "Instructor: ${capacitacion.instructor}",
                fontSize = 16.sp,
                color = TextoOscuro
            )

            // Botón visible "Ver curso": hace lo mismo que tocar la tarjeta (48 dp de alto mínimo)
            TextButton(
                onClick = { onVerCapacitacion(capacitacion.id) },
                modifier = Modifier
                    .align(Alignment.End)
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    text = "Ver curso",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeOscuro
                )
                Spacer(modifier = Modifier.width(4.dp))
                // Decorativo: el texto "Ver curso" ya dice qué hace el botón
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = VerdeOscuro
                )
            }
        }
    }
}
