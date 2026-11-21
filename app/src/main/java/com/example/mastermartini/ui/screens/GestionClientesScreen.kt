package com.example.mastermartini.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.model.Usuario
import com.example.mastermartini.ui.components.ErrorConReintentar
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.EstadoUi
import com.example.mastermartini.viewmodel.GestionClientesViewModel

// PANEL "CLIENTES Y PLAN PRO" (ruta "panel/clientes", para Ventas y Administrador)
// Lista de clientes con buscador y un Switch para activar o quitar el Plan Pro.
// Solo DIBUJA el estado del GestionClientesViewModel y le avisa lo que toca el usuario.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionClientesScreen(
    viewModel: GestionClientesViewModel,
    // Flecha para volver al Perfil (popBackStack en MainActivity)
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // El Snackbar es el aviso que aparece abajo unos segundos. SnackbarHostState lo controla.
    // remember: se crea una vez y se conserva entre redibujos.
    val snackbarHostState = remember { SnackbarHostState() }

    // Cuando el ViewModel pone un mensaje (ej. falló cambiar el Plan Pro), se muestra el Snackbar.
    // LaunchedEffect(clave) se vuelve a ejecutar cada vez que cambia el mensaje.
    // showSnackbar espera hasta que el aviso se va; después se avisa al ViewModel para borrarlo.
    LaunchedEffect(uiState.mensajeSnackbar) {
        val mensaje = uiState.mensajeSnackbar
        if (mensaje != null) {
            snackbarHostState.showSnackbar(mensaje)
            viewModel.snackbarMostrado()
        }
    }

    Scaffold(
        containerColor = BlancoHueso,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Clientes y Plan Pro", fontWeight = FontWeight.Bold) },
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
            // BUSCADOR por nombre o correo (el filtro lo hace el ViewModel, sin tildes)
            OutlinedTextField(
                value = uiState.busqueda,
                onValueChange = { viewModel.onBusquedaChange(it) },
                placeholder = { Text("Buscar por nombre o correo", fontSize = 16.sp) },
                // Decorativo: el texto de ayuda ya explica el campo
                leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                // X para borrar, solo si hay algo escrito (IconButton mide 48 dp)
                trailingIcon = {
                    if (uiState.busqueda.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onBusquedaChange("") }) {
                            Icon(imageVector = Icons.Filled.Clear, contentDescription = "Borrar búsqueda")
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeItaliano,
                    cursorColor = VerdeItaliano,
                    focusedContainerColor = BlancoPuro,
                    unfocusedContainerColor = BlancoPuro
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // POLIMORFISMO con EstadoUi: se dibuja algo distinto en cada caso.
            // "when (val estado = ...)": se guarda en una variable para que Kotlin sepa,
            // dentro de cada caso, si es Exito (y tiene .datos) o Error (y tiene .mensaje).
            when (val estado = uiState.estado) {
                is EstadoUi.Cargando -> {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(color = VerdeItaliano)
                    }
                }

                is EstadoUi.Error -> {
                    // Ej. "No tienes permiso para hacer esta acción." si alguien llega sin permiso
                    ErrorConReintentar(mensaje = estado.mensaje, onReintentar = { viewModel.cargar() })
                }

                is EstadoUi.Exito -> {
                    if (estado.datos.isEmpty()) {
                        Text(
                            text = "No hay usuarios que coincidan",
                            fontSize = 18.sp,
                            color = TextoOscuro,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // key = id: al filtrar, las filas se animan a su lugar (animateItem)
                            items(estado.datos, key = { cliente -> cliente.id }) { cliente ->
                                FilaCliente(
                                    cliente = cliente,
                                    guardando = cliente.id in uiState.guardando,
                                    onCambiarPlanPro = { activo -> viewModel.cambiarPlanPro(cliente, activo) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// UNA FILA DE CLIENTE: nombre, correo, rubro y el Switch "Plan Pro".
// No guarda nada: avisa el cambio con onCambiarPlanPro y el ViewModel decide.
@Composable
private fun FilaCliente(
    cliente: Usuario,
    // true mientras se guarda el cambio de ESTE cliente
    guardando: Boolean,
    onCambiarPlanPro: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        border = BorderStroke(1.dp, DoradoCalidad.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = cliente.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
            Text(text = cliente.correo, fontSize = 16.sp, color = TextoOscuro)
            Text(text = "Rubro: ${cliente.rubro}", fontSize = 16.sp, color = VerdeOscuro)

            // FILA DEL SWITCH: toda la fila se puede tocar (no solo el interruptor), mínimo 48 dp.
            // toggleable con Role.Switch: TalkBack la lee como "interruptor, activado/desactivado".
            // enabled = !guardando: mientras se guarda no se puede volver a tocar.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .toggleable(
                        value = cliente.planPro,
                        enabled = !guardando,
                        role = Role.Switch,
                        onValueChange = { activo -> onCambiarPlanPro(activo) }
                    )
            ) {
                // El estado también se dice con TEXTO, no solo con la posición del Switch
                Text(
                    text = if (cliente.planPro) "Plan Pro: activo" else "Plan Pro: inactivo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoOscuro,
                    modifier = Modifier.weight(1f)
                )
                // Indicador pequeño mientras se guarda (solo en esta fila)
                if (guardando) {
                    CircularProgressIndicator(
                        color = VerdeItaliano,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                // onCheckedChange = null: el toque lo maneja la fila completa (toggleable),
                // así no se registra dos veces
                Switch(
                    checked = cliente.planPro,
                    onCheckedChange = null,
                    enabled = !guardando,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BlancoPuro,
                        checkedTrackColor = VerdeItaliano
                    )
                )
            }
        }
    }
}
