package com.example.mastermartini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mastermartini.ui.screens.HomeScreen
import com.example.mastermartini.ui.screens.RegistroScreen
import com.example.mastermartini.ui.theme.MasterMartiniTheme
import com.example.mastermartini.viewmodel.RegistroViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración exacta proyectada en la Parte 5 de la clase
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MasterMartiniTheme {
                val navController = rememberNavController()
                val registroViewModel: RegistroViewModel = viewModel()

                // PANTALLA INICIAL SEGÚN LA PERSISTENCIA LOCAL (IE 2.3.1)
                // Si ya se registró antes, la app abre directo en "home" con sus datos guardados.
                // remember hace que se calcule una sola vez: si se recalculara después de
                // registrarse, el NavHost cambiaría de pantalla inicial y se reiniciaría la navegación.
                val pantallaInicial = remember {
                    if (registroViewModel.hayPerfilGuardado()) "home" else "registro"
                }

                NavHost(
                    navController = navController,
                    startDestination = pantallaInicial
                ) {
                    composable("registro") {
                        RegistroScreen(
                            viewModel = registroViewModel,
                            onRegistroCompletado = {
                                navController.navigate("home") {
                                    // Saca "registro" de la pila: el botón atrás ya no vuelve al formulario
                                    popUpTo("registro") { inclusive = true }
                                    // Si "home" ya estaba debajo (caso "Editar Datos"), reutiliza esa
                                    // pantalla en vez de apilar un segundo "home" encima.
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable("home") {
                        HomeScreen(
                            nombreUsuario = registroViewModel.obtenerNombreGuardado(),
                            rubroUsuario = registroViewModel.obtenerRubroGuardado(),
                            rutUsuario = registroViewModel.obtenerRutGuardado(),
                            correoUsuario = registroViewModel.obtenerCorreoGuardado(),
                            onEditarDatos = {
                                // navigate y no popBackStack(): si "home" es la pantalla inicial,
                                // no hay nada debajo y popBackStack() dejaría la app sin pantalla.
                                navController.navigate("registro")
                            }
                        )
                    }
                }
            }
        }
    }
}