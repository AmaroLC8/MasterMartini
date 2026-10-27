package com.example.mastermartini.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.components.LogoMasterMartini
import com.example.mastermartini.ui.theme.*
import com.example.mastermartini.viewmodel.RegistroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: RegistroViewModel,
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit
) {
    var rutOCorreo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf<String?>(null) }

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

            // Campo RUT o Correo
            OutlinedTextField(
                value = rutOCorreo,
                onValueChange = {
                    rutOCorreo = it
                    mensajeError = null
                },
                label = { Text("RUT o Correo Electrónico") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Badge,
                        contentDescription = "Usuario",
                        tint = VerdeItaliano
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeItaliano,
                    focusedLabelColor = VerdeItaliano,
                    cursorColor = VerdeItaliano
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    mensajeError = null
                },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Contraseña",
                        tint = VerdeItaliano
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeItaliano,
                    focusedLabelColor = VerdeItaliano,
                    cursorColor = VerdeItaliano
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // Mensaje de error si falla la validación
            if (mensajeError != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = mensajeError!!,
                    color = RojoItaliano,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón principal de Iniciar Sesión con verde italiano
            Button(
                onClick = {
                    if (rutOCorreo.isBlank() || password.isBlank()) {
                        mensajeError = "Por favor ingresa tus credenciales"
                    } else {
                        val esValido = viewModel.validarLogin(rutOCorreo, password)
                        if (esValido) {
                            mensajeError = null
                            onLoginExitoso()
                        } else {
                            mensajeError = "Credenciales incorrectas o usuario no registrado"
                        }
                    }
                },
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