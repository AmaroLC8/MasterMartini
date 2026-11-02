package com.example.mastermartini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mastermartini.ui.screens.DetalleScreen
import com.example.mastermartini.ui.screens.EncuestaScreen
import com.example.mastermartini.ui.screens.HomeScreen
import com.example.mastermartini.ui.screens.LoginScreen
import com.example.mastermartini.ui.screens.RegistroScreen
import com.example.mastermartini.ui.theme.MasterMartiniTheme
import com.example.mastermartini.viewmodel.DetalleViewModel
import com.example.mastermartini.viewmodel.EncuestaViewModel
import com.example.mastermartini.viewmodel.HomeViewModel
import com.example.mastermartini.viewmodel.LoginViewModel
import com.example.mastermartini.viewmodel.RegistroViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración exacta proyectada en la Parte 5 de la clase:
        // la app dibuja de borde a borde (pantalla completa)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MasterMartiniTheme {
                // Controla la navegación entre pantallas (qué pantalla se ve y la pila para "Atrás")
                val navController = rememberNavController()
                // Se crea afuera del NavHost porque lo usan dos pantallas:
                // Registro (formulario) y Home (lee los datos guardados del perfil)
                val registroViewModel: RegistroViewModel = viewModel()

                // UN SOLO NavHost con todas las pantallas registradas.
                // La app siempre parte en "login"; desde ahí se puede ir a "registro".
                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    // 1. LOGIN
                    composable("login") {
                        // El Login tiene su propio ViewModel, con sus validaciones.
                        // Se crea aquí dentro, igual que HomeViewModel en "home".
                        val loginViewModel: LoginViewModel = viewModel()
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginExitoso = {
                                navController.navigate("home") {
                                    // Saca "login" de la pila: el botón Atrás en Home cierra la app
                                    // en vez de volver al login
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
                                    // Si "home" ya estaba debajo (caso "Editar Datos"), reutiliza esa
                                    // pantalla en vez de apilar un segundo "home" encima.
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // 3. HOME
                    composable("home") {
                        // ViewModel del catálogo de cursos. Se crea aquí dentro porque solo
                        // lo usa la pantalla Home (viewModel() lo conserva al rotar la pantalla).
                        // Es AndroidViewModel: viewModel() le pasa la Application solo, igual que al RegistroViewModel.
                        val homeViewModel: HomeViewModel = viewModel()
                        HomeScreen(
                            viewModel = homeViewModel,
                            nombreUsuario = registroViewModel.obtenerNombreGuardado(),
                            rubroUsuario = registroViewModel.obtenerRubroGuardado(),
                            rutUsuario = registroViewModel.obtenerRutGuardado(),
                            correoUsuario = registroViewModel.obtenerCorreoGuardado(),
                            // navigate y no popBackStack(): si se entró desde el login, "login" se sacó
                            // de la pila, no hay nada debajo de Home y popBackStack() dejaría la app sin pantalla.
                            onEditarDatos = { navController.navigate("registro") },
                            // Abre la encuesta encima de Home: pila [home, encuesta]
                            onIrAEncuesta = { navController.navigate("encuesta") },
                            // Arma la ruta con el id del curso tocado: ej. "detalle/3". Pila [home, detalle]
                            onVerDetalle = { id -> navController.navigate("detalle/$id") }
                        )
                    }

                    // 5. DETALLE DE UN CURSO (receta paso a paso + video)
                    // {cursoId} es un "hueco" en la ruta: se llena con el número que manda Home.
                    // navArgument dice que ese hueco es un Int (IntType); así llega como número y no como texto.
                    composable(
                        route = "detalle/{cursoId}",
                        arguments = listOf(navArgument("cursoId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        // Se lee el argumento por su nombre. Si por algún motivo no viene, se usa 0
                        // (no existe ningún curso con id 0, así que la pantalla dice "Receta no encontrada").
                        val cursoId = backStackEntry.arguments?.getInt("cursoId") ?: 0
                        // Se crea aquí dentro porque solo lo usa esta pantalla
                        val detalleViewModel: DetalleViewModel = viewModel()
                        DetalleScreen(
                            cursoId = cursoId,
                            viewModel = detalleViewModel,
                            // Debajo de "detalle" siempre está "home", así que popBackStack() vuelve a Home
                            onVolver = { navController.popBackStack() }
                        )
                    }

                    // 4. ENCUESTA (intereses, nivel y ubicación GPS; se guarda en SharedPreferences)
                    composable("encuesta") {
                        // Se crea aquí dentro porque solo la usa esta pantalla.
                        // EncuestaViewModel es AndroidViewModel: viewModel() le pasa la Application solo
                        val encuestaViewModel: EncuestaViewModel = viewModel()
                        EncuestaScreen(
                            encuestaViewModel = encuestaViewModel,
                            // Aquí sí sirve popBackStack(): debajo de "encuesta" siempre está "home"
                            onVolverHome = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}