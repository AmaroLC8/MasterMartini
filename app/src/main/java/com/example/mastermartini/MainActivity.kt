package com.example.mastermartini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

                NavHost(
                    navController = navController,
                    startDestination = "registro"
                ) {
                    composable("registro") {
                        RegistroScreen(
                            viewModel = registroViewModel,
                            onRegistroCompletado = {
                                navController.navigate("home")
                            }
                        )
                    }
                    composable("home") {
                        HomeScreen(
                            nombreUsuario = registroViewModel.obtenerNombreGuardado(),
                            rubroUsuario = registroViewModel.obtenerRubroGuardado(),
                            rutUsuario = registroViewModel.obtenerRutGuardado(),
                            correoUsuario = registroViewModel.obtenerCorreoGuardado(),
                            onCerrarSesion = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}