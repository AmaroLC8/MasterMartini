package com.example.mastermartini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mastermartini.ui.screens.EncuestaScreen
import com.example.mastermartini.ui.screens.HomeScreen
import com.example.mastermartini.ui.screens.LoginScreen
import com.example.mastermartini.ui.screens.RegistroScreen
import com.example.mastermartini.ui.theme.MasterMartiniTheme
import com.example.mastermartini.viewmodel.EncuestaViewModel
import com.example.mastermartini.viewmodel.HomeViewModel
import com.example.mastermartini.viewmodel.RegistroViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración de bordes para pantalla completa
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MasterMartiniTheme {
                val navController = rememberNavController()
                val registroViewModel: RegistroViewModel = viewModel()

                // UN SOLO NavHost con todas las pantallas registradas.
                // La app siempre parte en "login"; desde ahí se puede ir a "registro".
                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    // 1. LOGIN
                    composable("login") {
                        LoginScreen(
                            viewModel = registroViewModel, // 👈 Se pasa el ViewModel aquí
                            onLoginExitoso = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onIrARegistro = {
                                navController.navigate("registro")
                            }
                        )
                    }

                    // 2. REGISTRO (Al completar navega a Home)
                    composable("registro") {
                        RegistroScreen(
                            viewModel = registroViewModel,
                            onRegistroCompletado = {
                                // Entra directo a Home y saca "registro" de la pila para que
                                // el botón Atrás no vuelva al formulario
                                navController.navigate("home"){
                                    popUpTo("registro") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // 3. HOME
                    composable("home") {
                        val homeViewModel: HomeViewModel = viewModel()
                        HomeScreen(
                            viewModel = homeViewModel,
                            nombreUsuario = registroViewModel.obtenerNombreGuardado(),
                            rubroUsuario = registroViewModel.obtenerRubroGuardado(),
                            rutUsuario = registroViewModel.obtenerRutGuardado(),
                            correoUsuario = registroViewModel.obtenerCorreoGuardado(),
                            onEditarDatos = { navController.navigate("registro") },
                            onIrAEncuesta = { navController.navigate("encuesta") }
                        )
                    }

                    // 4. ENCUESTA
                    composable("encuesta") {
                        val encuestaViewModel: EncuestaViewModel = viewModel()
                        EncuestaScreen(
                            encuestaViewModel = encuestaViewModel,
                            onVolverHome = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}