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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

// Pantalla de búsqueda en tiempo real sobre el catálogo de recetas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscarScreen(
    viewModel: BuscarViewModel,
    onVerDetalle: (Int) -> Unit,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = BlancoHueso,
        topBar = {
            TopAppBar(
                title = { Text(text = "Buscar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = BlancoPuro
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = uiState.texto,
                onValueChange = { nuevoTexto -> viewModel.onTextoChange(nuevoTexto) },
                placeholder = { Text("Busca por receta, sección o ingrediente", fontSize = 16.sp) },
                leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
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
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
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
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Sugerencias",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerdeOscuro
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.categorias.forEach { categoria ->
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
                                modifier = Modifier.height(48.dp)
                            )
                        }
                    }
                }
            } else if (uiState.resultados.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
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
                val cantidad = uiState.resultados.size
                Text(
                    text = if (cantidad == 1) "1 resultado" else "$cantidad resultados",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeOscuro,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
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
