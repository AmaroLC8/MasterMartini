package com.example.mastermartini.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.FavoritosViewModel

// PANTALLA DE FAVORITOS
// Muestra los cursos que el usuario marcó con el corazón (guardados en Room).
// Solo DIBUJA la lista que le entrega el FavoritosViewModel (MVVM): no filtra ni guarda nada.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen(
    // ViewModel con los cursos favoritos (lo crea MainActivity)
    viewModel: FavoritosViewModel,
    // Recibe el id del curso tocado; MainActivity navega a "detalle/{id}", igual que desde Home
    onVerDetalle: (Int) -> Unit,
    // Botón "Ver recetas" cuando la lista está vacía; MainActivity navega a la sección Recetas
    onIrARecetas: () -> Unit
) {
    // Escuchamos los favoritos del ViewModel. Al quitar uno, Room avisa el cambio,
    // el ViewModel vuelve a filtrar y esta lista se actualiza sola.
    val cursosFavoritos by viewModel.cursosFavoritos.collectAsState()

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            // Barra verde con el título y cuántos favoritos hay (sin flecha: se llega desde la barra inferior)
            TopAppBar(
                title = {
                    Text(
                        text = "Mis favoritos (${cursosFavoritos.size})",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdeItaliano,
                    titleContentColor = BlancoPuro
                )
            )
        }
    ) { innerPadding ->
        if (cursosFavoritos.isEmpty()) {
            // ESTADO VACÍO: en vez de una pantalla en blanco, se explica qué pasa y qué hacer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            ) {
                // Corazón vacío grande. contentDescription = null: es solo decorativo,
                // el texto de abajo ya explica la situación a TalkBack
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = RojoItaliano,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Aún no tienes recetas favoritas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Toca el corazón de una receta para guardarla aquí.",
                    fontSize = 16.sp,
                    color = TextoOscuro,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                // Botón grande (56 dp) con ícono y texto, para ir a buscar recetas
                Button(
                    onClick = onIrARecetas,
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeItaliano),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Ver recetas", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // LISTA DE FAVORITOS
            // contentPadding: margen alrededor de la lista que se desplaza junto con ella
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // key = id: Compose sabe qué tarjeta es cada curso. Es OBLIGATORIO para que
                // animateItem() sepa cuál tarjeta se fue y anime solo esa.
                items(cursosFavoritos, key = { curso -> curso.id }) { curso ->
                    // Card con onClick: toda la tarjeta abre la receta (igual que en Home).
                    // El corazón tiene su propio onClick, por eso tocarlo NO abre el detalle.
                    Card(
                        onClick = { onVerDetalle(curso.id) },
                        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
                        border = BorderStroke(1.dp, DoradoCalidad.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        // animateItem(): al quitar un favorito la tarjeta se desvanece
                        // y las de abajo suben suavemente a ocupar su lugar (en vez de saltar)
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // weight(1f): la columna de textos usa el ancho que deja el corazón
                                Column(modifier = Modifier.weight(1f)) {
                                    // Etiqueta con la categoría (mismo estilo que en el Detalle)
                                    Text(
                                        text = curso.categoria,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BlancoPuro,
                                        modifier = Modifier
                                            .background(VerdeItaliano, RoundedCornerShape(50))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = curso.titulo,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextoOscuro
                                    )
                                }

                                // Corazón rojo para quitar de favoritos.
                                // 56.dp: área táctil grande, pensada para usuarios mayores.
                                // contentDescription dice el curso y la acción, para que TalkBack lea qué hace.
                                IconButton(
                                    onClick = { viewModel.quitarFavorito(curso.id) },
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = "Quitar ${curso.titulo} de favoritos",
                                        tint = RojoItaliano
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Instructor: ${curso.chef}",
                                fontSize = 15.sp,
                                color = TextoOscuro
                            )
                            Text(
                                text = "⏱️ Duración: ${curso.duracion}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = RojoItaliano
                            )

                            // Botón visible "Ver receta": hace lo mismo que tocar la tarjeta,
                            // pero deja claro que se puede abrir (48 dp de alto mínimo)
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
                                    color = VerdeOscuro
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = VerdeOscuro
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
