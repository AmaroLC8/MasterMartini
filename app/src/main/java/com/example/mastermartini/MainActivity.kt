package com.example.mastermartini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mastermartini.ui.components.BarraNavegacion
import com.example.mastermartini.ui.components.itemsNavegacion
import com.example.mastermartini.ui.screens.BuscarScreen
import com.example.mastermartini.ui.screens.CapacitacionDetalleScreen
import com.example.mastermartini.ui.screens.CapacitacionesScreen
import com.example.mastermartini.ui.screens.DetalleScreen
import com.example.mastermartini.ui.screens.EncuestaScreen
import com.example.mastermartini.ui.screens.FavoritosScreen
import com.example.mastermartini.ui.screens.GestionClientesScreen
import com.example.mastermartini.ui.screens.GestionUsuariosScreen
import com.example.mastermartini.ui.screens.RecuperarPasswordScreen
import com.example.mastermartini.ui.screens.HomeScreen
import com.example.mastermartini.ui.screens.LoginScreen
import com.example.mastermartini.ui.screens.PerfilScreen
import com.example.mastermartini.ui.screens.PreguntasPendientesScreen
import com.example.mastermartini.ui.screens.ProductoDetalleScreen
import com.example.mastermartini.ui.screens.ProductosScreen
import com.example.mastermartini.ui.screens.RecetasScreen
import com.example.mastermartini.ui.screens.RegistroScreen
import com.example.mastermartini.ui.theme.MasterMartiniTheme
import com.example.mastermartini.viewmodel.BuscarViewModel
import com.example.mastermartini.viewmodel.CapacitacionDetalleViewModel
import com.example.mastermartini.viewmodel.CapacitacionesViewModel
import com.example.mastermartini.viewmodel.DetalleViewModel
import com.example.mastermartini.viewmodel.EncuestaViewModel
import com.example.mastermartini.viewmodel.FavoritosViewModel
import com.example.mastermartini.viewmodel.GestionClientesViewModel
import com.example.mastermartini.viewmodel.GestionUsuariosViewModel
import com.example.mastermartini.viewmodel.RecuperarPasswordViewModel
import com.example.mastermartini.viewmodel.HomeViewModel
import com.example.mastermartini.viewmodel.LoginViewModel
import com.example.mastermartini.viewmodel.PerfilViewModel
import com.example.mastermartini.viewmodel.PreguntasPendientesViewModel
import com.example.mastermartini.viewmodel.ProductoDetalleViewModel
import com.example.mastermartini.viewmodel.ProductosViewModel
import com.example.mastermartini.viewmodel.RecetasViewModel
import com.example.mastermartini.viewmodel.RegistroViewModel

// Actividad principal que configura la navegación y la barra inferior de la app
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MasterMartiniTheme {
                val navController = rememberNavController()
                val registroViewModel: RegistroViewModel = viewModel(factory = RegistroViewModel.Factory)

                val entradaActual by navController.currentBackStackEntryAsState()
                val rutaActual = entradaActual?.destination?.route

                val rutasConBarra = itemsNavegacion.map { item -> item.ruta }
                val mostrarBarra = rutaActual in rutasConBarra

                fun navegarASeccion(ruta: String) {
                    navController.navigate(ruta) {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                Scaffold(
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (mostrarBarra) {
                            BarraNavegacion(
                                rutaActual = rutaActual,
                                onNavegar = { ruta -> navegarASeccion(ruta) }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                    ) {
                        composable("login") {
                            val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginExitoso = {
                                    registroViewModel.cargarPerfilGuardado()
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onIrARegistro = {
                                    navController.navigate("registro")
                                },
                                onOlvidePassword = {
                                    navController.navigate("recuperar")
                                }
                            )
                        }

                        composable("recuperar") {
                            val recuperarViewModel: RecuperarPasswordViewModel =
                                viewModel(factory = RecuperarPasswordViewModel.Factory)
                            RecuperarPasswordScreen(
                                viewModel = recuperarViewModel,
                                onVolver = { navController.popBackStack() }
                            )
                        }

                        composable("registro") {
                            RegistroScreen(
                                viewModel = registroViewModel,
                                onRegistroCompletado = {
                                    val rutaAnterior = navController.previousBackStackEntry?.destination?.route

                                    if (rutaAnterior == null || rutaAnterior == "login") {
                                        navController.navigate("home") {
                                            popUpTo("registro") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        navController.popBackStack()
                                    }
                                }
                            )
                        }

                        composable("home") {
                            val homeViewModel: HomeViewModel = viewModel()
                            HomeScreen(
                                viewModel = homeViewModel,
                                nombreUsuario = registroViewModel.obtenerNombreGuardado(),
                                rubroUsuario = registroViewModel.obtenerRubroGuardado(),
                                rutUsuario = registroViewModel.obtenerRutGuardado(),
                                correoUsuario = registroViewModel.obtenerCorreoGuardado(),
                                onEditarDatos = { navController.navigate("registro") },
                                onIrAEncuesta = { navController.navigate("encuesta") },
                                onVerDetalle = { id -> navController.navigate("detalle/$id") },
                                onVerProductos = { navController.navigate("productos") },
                                onBuscar = { navController.navigate("buscar") }
                            )
                        }

                        composable(
                            route = "detalle/{cursoId}",
                            arguments = listOf(navArgument("cursoId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val cursoId = backStackEntry.arguments?.getInt("cursoId") ?: 0
                            val detalleViewModel: DetalleViewModel = viewModel()
                            DetalleScreen(
                                cursoId = cursoId,
                                viewModel = detalleViewModel,
                                onVolver = { navController.popBackStack() },
                                onVerProducto = { id -> navController.navigate("producto/$id") }
                            )
                        }

                        composable("productos") {
                            val productosViewModel: ProductosViewModel = viewModel()
                            ProductosScreen(
                                viewModel = productosViewModel,
                                onVolver = { navController.popBackStack() },
                                onVerProducto = { id -> navController.navigate("producto/$id") }
                            )
                        }

                        composable(
                            route = "producto/{productoId}",
                            arguments = listOf(navArgument("productoId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val productoId = backStackEntry.arguments?.getInt("productoId") ?: 0
                            val productoDetalleViewModel: ProductoDetalleViewModel = viewModel()
                            ProductoDetalleScreen(
                                productoId = productoId,
                                viewModel = productoDetalleViewModel,
                                onVolver = { navController.popBackStack() },
                                onVerReceta = { id -> navController.navigate("detalle/$id") }
                            )
                        }

                        composable("encuesta") {
                            val encuestaViewModel: EncuestaViewModel = viewModel()
                            EncuestaScreen(
                                encuestaViewModel = encuestaViewModel,
                                onVolverHome = { navController.popBackStack() }
                            )
                        }

                        composable("recetas") {
                            val recetasViewModel: RecetasViewModel = viewModel()
                            RecetasScreen(
                                viewModel = recetasViewModel,
                                onVerDetalle = { id -> navController.navigate("detalle/$id") },
                                onBuscar = { navController.navigate("buscar") }
                            )
                        }

                        composable("buscar") {
                            val buscarViewModel: BuscarViewModel = viewModel()
                            BuscarScreen(
                                viewModel = buscarViewModel,
                                onVerDetalle = { id -> navController.navigate("detalle/$id") },
                                onVolver = { navController.popBackStack() }
                            )
                        }

                        composable("cursos") {
                            val capacitacionesViewModel: CapacitacionesViewModel = viewModel()
                            CapacitacionesScreen(
                                viewModel = capacitacionesViewModel,
                                onVerCapacitacion = { id -> navController.navigate("capacitacion/$id") }
                            )
                        }

                        composable(
                            route = "capacitacion/{capacitacionId}",
                            arguments = listOf(navArgument("capacitacionId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val capacitacionId = backStackEntry.arguments?.getInt("capacitacionId") ?: 0
                            val capacitacionDetalleViewModel: CapacitacionDetalleViewModel = viewModel()
                            CapacitacionDetalleScreen(
                                capacitacionId = capacitacionId,
                                viewModel = capacitacionDetalleViewModel,
                                onVolver = { navController.popBackStack() },
                                onVerProducto = { id -> navController.navigate("producto/$id") },
                                onEditar = { id -> navController.navigate("panel/editar-curso/$id") },
                                onEliminado = { navController.popBackStack() }
                            )
                        }

                        composable("favoritos") {
                            val favoritosViewModel: FavoritosViewModel = viewModel()
                            FavoritosScreen(
                                viewModel = favoritosViewModel,
                                onVerDetalle = { id -> navController.navigate("detalle/$id") },
                                onIrARecetas = { navegarASeccion("recetas") }
                            )
                        }

                        composable("perfil") {
                            val perfilViewModel: PerfilViewModel = viewModel()
                            PerfilScreen(
                                viewModel = perfilViewModel,
                                onEditarDatos = { navController.navigate("registro") },
                                onIrAEncuesta = { navController.navigate("encuesta") },
                                onCerrarSesion = {
                                    perfilViewModel.cerrarSesion()
                                    registroViewModel.cargarPerfilGuardado()
                                    navController.navigate("login") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                    }
                                },
                                onAbrirPanel = { ruta -> navController.navigate(ruta) }
                            )
                        }

                        composable("panel/clientes") {
                            val gestionClientesViewModel: GestionClientesViewModel =
                                viewModel(factory = GestionClientesViewModel.Factory)
                            GestionClientesScreen(
                                viewModel = gestionClientesViewModel,
                                onVolver = { navController.popBackStack() }
                            )
                        }

                        composable("panel/usuarios") {
                            val gestionUsuariosViewModel: GestionUsuariosViewModel =
                                viewModel(factory = GestionUsuariosViewModel.Factory)
                            GestionUsuariosScreen(
                                viewModel = gestionUsuariosViewModel,
                                onVolver = { navController.popBackStack() }
                            )
                        }
                        composable("panel/preguntas") {
                            val preguntasPendientesViewModel: PreguntasPendientesViewModel =
                                viewModel(factory = PreguntasPendientesViewModel.Factory)
                            PreguntasPendientesScreen(
                                viewModel = preguntasPendientesViewModel,
                                onVolver = { navController.popBackStack() }
                            )
                        }

                        composable("panel/crear-curso") {
                            val crearCursoViewModel: com.example.mastermartini.viewmodel.CrearCursoViewModel = viewModel()
                            com.example.mastermartini.ui.screens.CrearCursoScreen(
                                viewModel = crearCursoViewModel,
                                onVolver = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "panel/editar-curso/{capacitacionId}",
                            arguments = listOf(navArgument("capacitacionId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val capacitacionId = backStackEntry.arguments?.getInt("capacitacionId") ?: 0
                            val editarCursoViewModel: com.example.mastermartini.viewmodel.CrearCursoViewModel = viewModel()
                            com.example.mastermartini.ui.screens.CrearCursoScreen(
                                viewModel = editarCursoViewModel,
                                onVolver = { navController.popBackStack() },
                                capacitacionId = capacitacionId
                            )
                        }
                    }
                }
            }
        }
    }
}
