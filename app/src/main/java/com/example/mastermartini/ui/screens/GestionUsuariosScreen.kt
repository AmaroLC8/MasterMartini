package com.example.mastermartini.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.model.Rol
import com.example.mastermartini.model.Usuario
import com.example.mastermartini.ui.components.ChipOpcion
import com.example.mastermartini.ui.components.ErrorConReintentar
import com.example.mastermartini.ui.theme.BlancoHueso
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.RojoItaliano
import com.example.mastermartini.ui.theme.TextoOscuro
import com.example.mastermartini.ui.theme.VerdeItaliano
import com.example.mastermartini.ui.theme.VerdeOscuro
import com.example.mastermartini.viewmodel.EstadoUi
import com.example.mastermartini.viewmodel.GestionUsuariosViewModel

// PANEL "USUARIOS Y ROLES" (ruta "panel/usuarios", solo Administrador)
// Todos los usuarios con filtro por rol. En cada fila: cambiar el rol (menú desplegable)
// y eliminar la cuenta (con confirmación). Solo DIBUJA: las reglas están en el ViewModel.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    viewModel: GestionUsuariosViewModel,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Usuario que se quiere eliminar (null = no hay diálogo abierto).
    // Es estado SOLO visual (abrir/cerrar el diálogo), como el de "Cerrar sesión" en Perfil.
    var usuarioAEliminar by remember { mutableStateOf<Usuario?>(null) }

    // Muestra el Snackbar cuando el ViewModel pone un mensaje (igual que en GestionClientesScreen)
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
                title = { Text(text = "Usuarios y roles", fontWeight = FontWeight.Bold) },
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
            // FILTRO POR ROL: "Todos" + un chip por cada rol de Rol.todos.
            // Si mañana se agrega un rol, aparece aquí solo (no se escribe a mano).
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ChipOpcion(
                    texto = "Todos",
                    seleccionado = uiState.filtroRol == null,
                    onClick = { viewModel.seleccionarFiltro(null) }
                )
                Rol.todos.forEach { rol ->
                    ChipOpcion(
                        texto = rol.nombreVisible,
                        seleccionado = uiState.filtroRol == rol,
                        onClick = { viewModel.seleccionarFiltro(rol) }
                    )
                }
            }

            // POLIMORFISMO con EstadoUi: un dibujo distinto para cada caso
            when (val estado = uiState.estado) {
                is EstadoUi.Cargando -> {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(color = VerdeItaliano)
                    }
                }

                is EstadoUi.Error -> {
                    // Ej. "No tienes permiso para hacer esta acción." si no es Administrador
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
                            items(estado.datos, key = { usuario -> usuario.id }) { usuario ->
                                FilaUsuario(
                                    usuario = usuario,
                                    // La REGLA la decide el ViewModel; la pantalla solo la usa
                                    esPropio = viewModel.esPropio(usuario),
                                    guardando = usuario.id in uiState.guardando,
                                    onCambiarRol = { rol -> viewModel.cambiarRol(usuario, rol) },
                                    // No elimina directo: abre el diálogo de confirmación
                                    onEliminar = { usuarioAEliminar = usuario },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO DE CONFIRMACIÓN antes de eliminar (eliminar no se puede deshacer).
    // Se copia a una variable local para usarla sin "!!".
    val porEliminar = usuarioAEliminar
    if (porEliminar != null) {
        AlertDialog(
            onDismissRequest = { usuarioAEliminar = null },
            title = { Text(text = "Eliminar cuenta", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "¿Eliminar la cuenta de ${porEliminar.nombre}? Esta acción no se puede deshacer.",
                    fontSize = 17.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        usuarioAEliminar = null
                        viewModel.eliminarUsuario(porEliminar)
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(text = "Eliminar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RojoItaliano)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { usuarioAEliminar = null },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(text = "Cancelar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VerdeOscuro)
                }
            }
        )
    }
}

// UNA FILA DE USUARIO: nombre, correo, botón del rol con su menú y botón eliminar.
// En la fila del propio administrador (esPropio) los controles van desactivados y dice "Tú".
@Composable
private fun FilaUsuario(
    usuario: Usuario,
    esPropio: Boolean,
    guardando: Boolean,
    onCambiarRol: (Rol) -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ¿Está abierto el menú de roles de esta fila? Estado solo visual
    var menuAbierto by remember { mutableStateOf(false) }

    // Los controles se pueden usar si NO es su propia fila y no hay una acción en curso
    val controlesActivos = !esPropio && !guardando

    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        border = BorderStroke(1.dp, DoradoCalidad.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = usuario.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                    Text(text = usuario.correo, fontSize = 16.sp, color = TextoOscuro)
                }
                // Etiqueta "Tú": explica con TEXTO por qué sus controles están desactivados
                if (esPropio) {
                    Text(
                        text = "Tú",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlancoPuro,
                        modifier = Modifier
                            .background(VerdeOscuro, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // ROL ACTUAL + MENÚ DESPLEGABLE.
                // Box: el DropdownMenu se abre "pegado" al botón que está en el mismo Box.
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { menuAbierto = true },
                        enabled = controlesActivos,
                        border = BorderStroke(1.dp, VerdeItaliano),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeOscuro),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            // TalkBack: dice de quién es el rol y cuál tiene (hay un botón igual en cada fila)
                            .semantics {
                                contentDescription = "Rol de ${usuario.nombre}: ${usuario.rol.nombreVisible}. Toca para cambiarlo"
                            }
                    ) {
                        Text(text = "Rol: ${usuario.rol.nombreVisible}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        // Decorativo: la flecha indica que se despliega un menú
                        Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false }
                    ) {
                        // Una opción por rol. El rol actual lleva ✓ (no depende solo del color)
                        Rol.todos.forEach { rol ->
                            DropdownMenuItem(
                                text = { Text(text = rol.nombreVisible, fontSize = 16.sp) },
                                leadingIcon = if (rol == usuario.rol) {
                                    { Icon(imageVector = Icons.Filled.Check, contentDescription = "Rol actual") }
                                } else {
                                    null
                                },
                                onClick = {
                                    menuAbierto = false
                                    onCambiarRol(rol)
                                },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }
                }

                // Indicador mientras se cambia el rol o se elimina este usuario
                if (guardando) {
                    CircularProgressIndicator(
                        color = VerdeItaliano,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // ELIMINAR. NO es decorativo: el ícono es lo único que dice qué hace el botón,
                // por eso su contentDescription dice a quién elimina. IconButton mide 48 dp.
                IconButton(onClick = onEliminar, enabled = controlesActivos) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar a ${usuario.nombre}",
                        // Rojo si se puede usar; gris (por defecto) si está desactivado
                        tint = if (controlesActivos) RojoItaliano else TextoOscuro.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}
