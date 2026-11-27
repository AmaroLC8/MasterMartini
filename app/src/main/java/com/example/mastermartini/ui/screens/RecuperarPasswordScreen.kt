package com.example.mastermartini.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.ui.components.IconoValidacion
import com.example.mastermartini.ui.theme.*
import com.example.mastermartini.viewmodel.PasoRecuperar
import com.example.mastermartini.viewmodel.RecuperarPasswordViewModel

// Pantalla en pasos para solicitar código y restablecer la contraseña
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarPasswordScreen(
    viewModel: RecuperarPasswordViewModel,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
        containerColor = BlancoHueso,
        topBar = {
            TopAppBar(
                title = { Text(text = "Recuperar contraseña", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            AnimatedContent(
                targetState = uiState.paso,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "pasoRecuperar"
            ) { paso ->
                when (paso) {
                    PasoRecuperar.CORREO -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextoPaso(
                            titulo = "Paso 1 de 2",
                            texto = "Escribe el correo con el que te registraste. Te enviaremos un código para crear una contraseña nueva."
                        )
                        OutlinedTextField(
                            value = uiState.correo,
                            onValueChange = { viewModel.onCorreoChange(it) },
                            label = { Text("Correo electrónico") },
                            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Correo") },
                            trailingIcon = { IconoValidacion(valor = uiState.correo, error = uiState.errorCorreo) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            isError = uiState.errorCorreo != null,
                            supportingText = { TextoError(uiState.errorCorreo) },
                            colors = textFieldColors,
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        AvisoError(uiState.errorGeneral)
                        BotonPrincipal(
                            texto = "Enviar código",
                            textoCargando = "ENVIANDO...",
                            cargando = uiState.cargando,
                            onClick = { viewModel.enviarCodigo() }
                        )
                    }

                    PasoRecuperar.CODIGO -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextoPaso(titulo = "Paso 2 de 2", texto = "Correo: ${uiState.correo.trim()}")

                        val mensaje = uiState.mensaje
                        if (mensaje != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Info, contentDescription = null, tint = VerdeOscuro)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = mensaje, fontSize = 16.sp, color = VerdeOscuro, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        OutlinedTextField(
                            value = uiState.codigo,
                            onValueChange = { viewModel.onCodigoChange(it) },
                            label = { Text("Código de 6 números") },
                            leadingIcon = { Icon(Icons.Filled.Pin, contentDescription = "Código") },
                            trailingIcon = { IconoValidacion(valor = uiState.codigo, error = uiState.errorCodigo) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            isError = uiState.errorCodigo != null,
                            supportingText = { TextoError(uiState.errorCodigo) },
                            colors = textFieldColors,
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = uiState.nuevaPassword,
                            onValueChange = { viewModel.onNuevaPasswordChange(it) },
                            label = { Text("Nueva contraseña") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Nueva contraseña") },
                            trailingIcon = {
                                IconoValidacion(valor = uiState.nuevaPassword, error = uiState.errorNuevaPassword)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            isError = uiState.errorNuevaPassword != null,
                            supportingText = { TextoError(uiState.errorNuevaPassword) },
                            colors = textFieldColors,
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = uiState.repetirPassword,
                            onValueChange = { viewModel.onRepetirPasswordChange(it) },
                            label = { Text("Repite la nueva contraseña") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Repetir contraseña") },
                            trailingIcon = {
                                IconoValidacion(valor = uiState.repetirPassword, error = uiState.errorRepetirPassword)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            isError = uiState.errorRepetirPassword != null,
                            supportingText = { TextoError(uiState.errorRepetirPassword) },
                            colors = textFieldColors,
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        AvisoError(uiState.errorGeneral)
                        BotonPrincipal(
                            texto = "Cambiar contraseña",
                            textoCargando = "GUARDANDO...",
                            cargando = uiState.cargando,
                            onClick = { viewModel.restablecer() }
                        )
                        OutlinedButton(
                            onClick = { viewModel.reenviarCodigo() },
                            enabled = !uiState.cargando,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeOscuro),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(text = "Reenviar código", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "Mientras el servidor no envía correos, el código aparece en el Logcat (etiqueta RecuperarPassword)",
                            fontSize = 16.sp,
                            color = TextoOscuro.copy(alpha = 0.8f)
                        )
                    }

                    PasoRecuperar.LISTO -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = VerdeItaliano,
                            modifier = Modifier.size(72.dp)
                        )
                        Text(
                            text = "¡Listo! Tu contraseña fue cambiada.",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = VerdeOscuro,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Ya puedes iniciar sesión con tu nueva contraseña.",
                            fontSize = 16.sp,
                            color = TextoOscuro,
                            textAlign = TextAlign.Center
                        )
                        BotonPrincipal(
                            texto = "Volver a iniciar sesión",
                            textoCargando = "",
                            cargando = false,
                            onClick = onVolver
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextoPaso(titulo: String, texto: String) {
    Column {
        Text(text = titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VerdeOscuro)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = texto, fontSize = 16.sp, color = TextoOscuro)
    }
}

@Composable
private fun TextoError(error: String?) {
    AnimatedVisibility(visible = error != null) {
        Text(text = error ?: "", color = RojoItaliano, fontSize = 16.sp)
    }
}

@Composable
private fun AvisoError(error: String?) {
    AnimatedVisibility(visible = error != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Filled.Warning, contentDescription = "Aviso", tint = RojoItaliano)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = error ?: "", color = RojoItaliano, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun BotonPrincipal(
    texto: String,
    textoCargando: String,
    cargando: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !cargando,
        colors = ButtonDefaults.buttonColors(
            containerColor = VerdeItaliano,
            contentColor = BlancoPuro,
            disabledContainerColor = VerdeItaliano.copy(alpha = 0.7f),
            disabledContentColor = BlancoPuro
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(color = BlancoPuro, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = textoCargando, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        } else {
            Text(text = texto, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
