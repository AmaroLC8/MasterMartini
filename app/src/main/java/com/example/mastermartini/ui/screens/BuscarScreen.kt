package com.example.mastermartini.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.components.TarjetaReceta
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.BuscarViewModel

// PANTALLA BUSCAR
// Campo de texto que busca recetas mientras se escribe.
// Solo DIBUJA lo que le entrega el BuscarViewModel (MVVM): la búsqueda (filtrar,
// quitar tildes, minúsculas) se hace en el ViewModel. Aquí solo se avisa qué se escribió.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscarScreen(
    // ViewModel con el texto escrito y los resultados (lo crea MainActivity)
    viewModel: BuscarViewModel,
    // Recibe el id de la receta tocada; MainActivity navega a "detalle/{id}", igual que desde Home
    onVerDetalle: (Int) -> Unit
) {
    // Escuchamos el estado del ViewModel. Con cada letra, el ViewModel busca otra vez
    // y esta pantalla se vuelve a dibujar con los resultados nuevos.
    val uiState by viewModel.uiState.collectAsState()

    // Sirve para quitar el foco del campo de texto, y así se cierra el teclado
    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            // Barra verde con el título (sin flecha: se llega desde la barra inferior)
            TopAppBar(
                title = { Text(text = "Buscar", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdeItaliano,
                    titleContentColor = BlancoPuro
                )
            )
        }
    ) { innerPadding ->
        // Column: arriba el buscador (queda fijo) y abajo lo que corresponda
        // (sugerencias, resultados o el aviso de "no encontramos")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // CAMPO DE BÚSQUEDA
            OutlinedTextField(
                value = uiState.texto,
                // Con cada letra se avisa al ViewModel, que guarda el texto y busca
                onValueChange = { nuevoTexto -> viewModel.onTextoChange(nuevoTexto) },
                placeholder = { Text("Busca por receta, sección o ingrediente", fontSize = 16.sp) },
                // Lupa a la izquierda. contentDescription = null: es decorativa, el texto de ayuda ya explica el campo
                leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                // Botón X a la derecha, SOLO si hay algo escrito (si el campo está vacío no hay nada que borrar).
                // IconButton mide 48 dp: área fácil de tocar.
                trailingIcon = {
                    if (uiState.texto.isNotEmpty()) {
                        IconButton(onClick = { viewModel.limpiar() }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Borrar búsqueda"
                            )
                        }
                    }
                },
                // Una sola línea: Enter no salta de línea, sino que ejecuta la acción del teclado
                singleLine = true,
                // ImeAction.Search: el botón Enter del teclado se ve como una LUPA
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                // Al tocar esa lupa del teclado, se cierra el teclado. No hace falta buscar aquí:
                // la búsqueda ya se hizo mientras se escribía.
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                textStyle = TextStyle(fontSize = 18.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeItaliano,
                    cursorColor = VerdeItaliano,
                    focusedLeadingIconColor = VerdeItaliano,
                    unfocusedLeadingIconColor = VerdeItaliano,
                    focusedContainerColor = BlancoPuro,
                    unfocusedContainerColor = BlancoPuro
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (uiState.texto.isBlank()) {
                // 1. CAMPO VACÍO: sugerencias para no dejar la pantalla en blanco
                //    y para quien no sabe qué escribir.
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Sugerencias",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerdeOscuro
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // FlowRow: pone los botones uno al lado del otro y, si no caben,
                    // pasa a la línea de abajo (así se ven las 5 secciones sin deslizar)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.categorias.forEach { categoria ->
                            // Al tocar una sugerencia se escribe esa sección en el buscador,
                            // igual que si el usuario la hubiera tecleado
                            AssistChip(
                                onClick = {
                                    viewModel.onTextoChange(categoria)
                                    focusManager.clearFocus()
                                },
                                label = { Text(text = categoria, fontSize = 16.sp) },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = BlancoPuro,
                                    labelColor = TextoOscuro,
                                    leadingIconContentColor = VerdeItaliano
                                ),
                                // 48 dp de alto: botón grande y fácil de tocar (el normal mide 32 dp)
                                modifier = Modifier.height(48.dp)
                            )
                        }
                    }
                }
            } else if (uiState.resultados.isEmpty()) {
                // 2. SE ESCRIBIÓ ALGO PERO NO HAY RESULTADOS: se avisa qué se buscó,
                //    para que el usuario vea si escribió mal
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // contentDescription = null: es decorativo, el texto de abajo lo explica
                    Icon(
                        imageVector = Icons.Filled.SearchOff,
                        contentDescription = null,
                        tint = RojoItaliano,
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No encontramos recetas para \"${uiState.texto}\"",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Prueba con otra palabra, por ejemplo \"chocolate\" o \"crema\".",
                        fontSize = 16.sp,
                        color = TextoOscuro,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // 3. HAY RESULTADOS: cuántos son y la lista
                // "1 resultado" en singular, "3 resultados" en plural
                val cantidad = uiState.resultados.size
                Text(
                    text = if (cantidad == 1) "1 resultado" else "$cantidad resultados",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeOscuro,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                // weight(1f): la lista ocupa todo el alto que queda libre
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // key = id + animateItem(): al escribir otra letra, las tarjetas que
                    // siguen coincidiendo se deslizan a su lugar y las que ya no, se desvanecen
                    items(uiState.resultados, key = { curso -> curso.id }) { curso ->
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
