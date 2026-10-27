package com.example.mastermartini.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.components.IconoValidacion
import com.example.mastermartini.ui.components.LogoMasterMartini
import com.example.mastermartini.ui.theme.*
import com.example.mastermartini.viewmodel.LoginViewModel

// Pantalla de inicio de sesión.
// No valida nada por su cuenta: lee el estado del LoginViewModel y le avisa
// cada vez que el usuario escribe o toca "INGRESAR" (MVVM, IE 2.2.1).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit
) {
    // collectAsState() convierte el StateFlow en estado de Compose:
    // cuando el ViewModel cambia algo (texto o error), la pantalla se redibuja sola.
    val uiState by viewModel.uiState.collectAsState()

    // Mismos colores para los dos campos: verde de la marca normal y rojo cuando hay error
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = VerdeItaliano,
        focusedLabelColor = VerdeItaliano,
        cursorColor = VerdeItaliano,
        focusedLeadingIconColor = VerdeItaliano,
        unfocusedLeadingIconColor = VerdeItaliano,
        errorBorderColor = RojoItaliano,
        errorLabelColor = RojoItaliano,
        errorLeadingIconColor = RojoItaliano
    )

    Scaffold(
        containerColor = BlancoHueso, // Fondo de la marca
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Master Martini Academia B2B",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = BlancoPuro
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdeItaliano
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Componente del Logo corporativo con estrellas y detalles italianos
            LogoMasterMartini()

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Iniciar Sesión ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Ingresa tus credenciales para acceder a los cursos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Campo RUT o Correo: el texto y el error vienen del ViewModel
            OutlinedTextField(
                value = uiState.usuario,
                onValueChange = { viewModel.onUsuarioChange(it) },
                label = { Text("RUT o Correo Electrónico") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "Usuario") },
                trailingIcon = { IconoValidacion(valor = uiState.usuario, error = uiState.errorUsuario) },
                isError = uiState.errorUsuario != null,
                supportingText = {
                    AnimatedVisibility(visible = uiState.errorUsuario != null) {
                        Text(uiState.errorUsuario ?: "", color = RojoItaliano)
                    }
                },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Contraseña
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Contraseña") },
                trailingIcon = { IconoValidacion(valor = uiState.password, error = uiState.errorPassword) },
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.errorPassword != null,
                supportingText = {
                    AnimatedVisibility(visible = uiState.errorPassword != null) {
                        Text(uiState.errorPassword ?: "", color = RojoItaliano)
                    }
                },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // Error general (RUT/correo o contraseña no coinciden con el registro).
            // Aparece y desaparece con animación; se borra solo cuando el usuario vuelve a escribir.
            AnimatedVisibility(visible = uiState.errorGeneral != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Aviso",
                        tint = RojoItaliano
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.errorGeneral ?: "",
                        color = RojoItaliano,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón principal de Iniciar Sesión con verde italiano.
            // Solo le pide al ViewModel que intente iniciar sesión; si todo está bien,
            // el ViewModel llama a onLoginExitoso y MainActivity navega a "home".
            Button(
                onClick = { viewModel.iniciarSesion(onExito = onLoginExitoso) },
                colors = ButtonDefaults.buttonColors(containerColor = VerdeItaliano),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp), // Altura para accesibilidad
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("INGRESAR", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BlancoPuro)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón secundario para ir al Registro
            TextButton(onClick = onIrARegistro) {
                Text(
                    "¿No tienes cuenta? Regístrate aquí",
                    color = VerdeItaliano,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}