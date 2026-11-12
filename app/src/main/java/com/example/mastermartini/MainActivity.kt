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
import com.example.mastermartini.viewmodel.ProductoDetalleViewModel
import com.example.mastermartini.viewmodel.ProductosViewModel
import com.example.mastermartini.viewmodel.RecetasViewModel
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
                // Registro (formulario) y Home (lee los datos guardados del perfil).
                // factory = RegistroViewModel.Factory: la "receta" que le pasa al constructor
                // el repositorio de usuarios y el almacenamiento (inyección por constructor).
                val registroViewModel: RegistroViewModel = viewModel(factory = RegistroViewModel.Factory)

                // Pantalla que se está viendo ahora. currentBackStackEntryAsState() es un estado:
                // cada vez que se navega, cambia y este bloque se vuelve a dibujar (así la barra
                // marca el botón correcto). Es null al partir, antes de cargar la primera pantalla.
                val entradaActual by navController.currentBackStackEntryAsState()
                // La ruta de esa pantalla, ej. "home", "recetas" o "detalle/{cursoId}"
                val rutaActual = entradaActual?.destination?.route

                // La barra inferior se muestra SOLO en las 5 pantallas que tienen botón en ella.
                // Se arma la lista de rutas desde itemsNavegacion, así si se agrega un botón
                // la barra aparece sola en esa pantalla. En login, registro, detalle y encuesta no se ve.
                val rutasConBarra = itemsNavegacion.map { item -> item.ruta }
                val mostrarBarra = rutaActual in rutasConBarra

                // Navega a una de las 5 secciones de la barra inferior (home, recetas, cursos, favoritos, perfil).
                // Es una función para escribir estas opciones UNA sola vez: la usan la barra
                // y el botón "Ver recetas" de Favoritos, y así las dos navegan igual.
                fun navegarASeccion(ruta: String) {
                    navController.navigate(ruta) {
                        // Deja "home" en la base de la pila y saca lo que haya encima.
                        // saveState = true: antes de sacarlas, GUARDA el estado de esas
                        // pantallas (ej. hasta dónde bajó la lista) para recuperarlo después.
                        popUpTo("home") { saveState = true }
                        // Si ya estás en esa pantalla, no apila una copia encima
                        launchSingleTop = true
                        // Si esa pantalla tenía un estado guardado (por saveState), lo recupera
                        restoreState = true
                    }
                }

                // Scaffold EXTERNO: envuelve todo el NavHost para poner la barra abajo.
                // contentWindowInsets = WindowInsets(0, 0, 0, 0): este Scaffold NO agrega el espacio
                // de las barras del sistema (hora arriba, gestos abajo). Así su innerPadding es solo
                // el alto de la barra inferior, y cada pantalla sigue manejando sus propios bordes
                // como antes. Si no, se sumaría el espacio del sistema dos veces.
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
                    // UN SOLO NavHost con todas las pantallas registradas.
                    // La app siempre parte en "login"; desde ahí se puede ir a "registro".
                    // padding(innerPadding): deja libre el espacio de la barra inferior (0 si no se ve).
                    // consumeWindowInsets(innerPadding): avisa a los Scaffold de cada pantalla que ese
                    // espacio de abajo ya está cubierto, para que no le sumen otra vez el de la barra de gestos.
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                    ) {
                        // 1. LOGIN
                        composable("login") {
                            // El Login tiene su propio ViewModel, con sus validaciones.
                            // Se crea aquí dentro, igual que HomeViewModel en "home".
                            // Usa su Factory para recibir el repositorio y el almacenamiento por el constructor.
                            val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginExitoso = {
                                    // La sesión ya se guardó: el formulario de "Editar datos" se
                                    // recarga con los datos de quien acaba de entrar
                                    registroViewModel.cargarPerfilGuardado()
                                    navController.navigate("home") {
                                        // Saca "login" de la pila: el botón Atrás en Home cierra la app
                                        // en vez de volver al login
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onIrARegistro = {
                                    navController.navigate("registro")
                                },
                                // Pila [login, recuperar]: al terminar, popBackStack vuelve al login
                                onOlvidePassword = {
                                    navController.navigate("recuperar")
                                }
                            )
                        }

                        // RECUPERAR CONTRASEÑA (se abre desde el login, sin barra inferior)
                        composable("recuperar") {
                            val recuperarViewModel: RecuperarPasswordViewModel =
                                viewModel(factory = RecuperarPasswordViewModel.Factory)
                            RecuperarPasswordScreen(
                                viewModel = recuperarViewModel,
                                // Debajo de "recuperar" siempre está "login"
                                onVolver = { navController.popBackStack() }
                            )
                        }

                        // 2. REGISTRO (Al completar navega a Home)
                        composable("registro") {
                            RegistroScreen(
                                viewModel = registroViewModel,
                                onRegistroCompletado = {
                                    // ¿Desde qué pantalla se abrió el registro?
                                    // previousBackStackEntry es la pantalla que quedó DEBAJO en la pila.
                                    val rutaAnterior = navController.previousBackStackEntry?.destination?.route

                                    if (rutaAnterior == null || rutaAnterior == "login") {
                                        // REGISTRO NUEVO (se llegó desde el login): entra a Home y saca
                                        // "registro" de la pila para que Atrás no vuelva al formulario.
                                        // (null no debería pasar, pero así nunca queda la pila vacía)
                                        navController.navigate("home") {
                                            popUpTo("registro") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        // EDITAR DATOS (se llegó desde Home o desde Perfil):
                                        // popBackStack() saca "registro" y vuelve a la pantalla de abajo.
                                        // Así, si editaste desde Perfil, vuelves a Perfil (y no a Home).
                                        navController.popBackStack()
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
                                onVerDetalle = { id -> navController.navigate("detalle/$id") },
                                // Abre el catálogo de productos encima de Home: pila [home, productos]
                                onVerProductos = { navController.navigate("productos") },
                                // Lupa: abre Buscar encima de Home: pila [home, buscar]
                                onBuscar = { navController.navigate("buscar") }
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
                                // popBackStack() vuelve a la pantalla desde donde se abrió la receta
                                // (Home, Recetas, Buscar, Favoritos o un producto), que siempre queda debajo de "detalle"
                                onVolver = { navController.popBackStack() },
                                // El botón del insumo abre el detalle de ese producto: ej. "producto/9"
                                onVerProducto = { id -> navController.navigate("producto/$id") }
                            )
                        }

                        // 7. CATÁLOGO DE PRODUCTOS MASTER MARTINI (se abre desde Home).
                        // No tiene barra inferior porque "productos" no está en itemsNavegacion.
                        composable("productos") {
                            // Se crea aquí dentro porque solo lo usa esta pantalla (ViewModel normal)
                            val productosViewModel: ProductosViewModel = viewModel()
                            ProductosScreen(
                                viewModel = productosViewModel,
                                // Debajo de "productos" siempre está "home"
                                onVolver = { navController.popBackStack() },
                                onVerProducto = { id -> navController.navigate("producto/$id") }
                            )
                        }

                        // 8. DETALLE DE UN PRODUCTO. Igual que "detalle/{cursoId}": {productoId} es un hueco
                        // en la ruta que se llena con el número, y navArgument dice que es un Int.
                        // Se abre desde "productos" o desde el detalle de una receta (botón del insumo).
                        composable(
                            route = "producto/{productoId}",
                            arguments = listOf(navArgument("productoId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            // Si no viene el argumento se usa 0 (no existe): la pantalla dice "Producto no encontrado"
                            val productoId = backStackEntry.arguments?.getInt("productoId") ?: 0
                            val productoDetalleViewModel: ProductoDetalleViewModel = viewModel()
                            ProductoDetalleScreen(
                                productoId = productoId,
                                viewModel = productoDetalleViewModel,
                                // Vuelve a donde se abrió (la lista de productos o la receta)
                                onVolver = { navController.popBackStack() },
                                // Las recetas que usan el producto abren su detalle: ej. "detalle/1"
                                onVerReceta = { id -> navController.navigate("detalle/$id") }
                            )
                        }

                        // 4. ENCUESTA (intereses, nivel y ubicación GPS; se guarda en SharedPreferences)
                        composable("encuesta") {
                            // Se crea aquí dentro porque solo la usa esta pantalla.
                            // EncuestaViewModel es AndroidViewModel: viewModel() le pasa la Application solo
                            val encuestaViewModel: EncuestaViewModel = viewModel()
                            EncuestaScreen(
                                encuestaViewModel = encuestaViewModel,
                                // Aquí sí sirve popBackStack(): debajo de "encuesta" siempre hay una pantalla
                                // ("home" o "perfil", según desde dónde se abrió), y se vuelve a esa
                                onVolverHome = { navController.popBackStack() }
                            )
                        }

                        // 6. PANTALLAS DE LA BARRA INFERIOR (Recetas, Cursos, Favoritos y Perfil).
                        // Recetas y Favoritos abren el detalle igual que Home: "detalle/3".
                        // Pila: [home, recetas, detalle] → Atrás vuelve a Recetas.
                        composable("recetas") {
                            // Se crea aquí dentro porque solo lo usa esta pantalla.
                            // Es un ViewModel normal (no usa Room), así que viewModel() no necesita la Application.
                            // Como la barra navega con saveState/restoreState, la sección y el orden
                            // elegidos se conservan al ir a otra pestaña y volver.
                            val recetasViewModel: RecetasViewModel = viewModel()
                            RecetasScreen(
                                viewModel = recetasViewModel,
                                onVerDetalle = { id -> navController.navigate("detalle/$id") },
                                // Lupa: abre Buscar encima de Recetas: pila [home, recetas, buscar]
                                onBuscar = { navController.navigate("buscar") }
                            )
                        }

                        // BUSCAR: ya NO es una pestaña de la barra (su lugar lo tomó "Cursos").
                        // Se abre con la lupa de Inicio o de Recetas, y como "buscar" no está en
                        // itemsNavegacion, aquí no se ve la barra inferior. Por eso tiene flecha para volver.
                        composable("buscar") {
                            // Se crea aquí dentro porque solo lo usa esta pantalla.
                            // Es un ViewModel normal (no usa Room), igual que RecetasViewModel.
                            val buscarViewModel: BuscarViewModel = viewModel()
                            BuscarScreen(
                                viewModel = buscarViewModel,
                                onVerDetalle = { id -> navController.navigate("detalle/$id") },
                                // Vuelve a la pantalla de abajo en la pila (Inicio o Recetas)
                                onVolver = { navController.popBackStack() }
                            )
                        }

                        // CURSOS ONLINE (pestaña "Cursos" de la barra inferior)
                        composable("cursos") {
                            // ViewModel normal (no usa Room). Con saveState/restoreState de la barra,
                            // el filtro elegido se conserva al ir a otra pestaña y volver.
                            val capacitacionesViewModel: CapacitacionesViewModel = viewModel()
                            CapacitacionesScreen(
                                viewModel = capacitacionesViewModel,
                                // Arma la ruta con el id del curso tocado: ej. "capacitacion/4".
                                // Pila [home, cursos, capacitacion] → Atrás vuelve a Cursos
                                onVerCapacitacion = { id -> navController.navigate("capacitacion/$id") }
                            )
                        }

                        // DETALLE DE UN CURSO ONLINE. Igual que "detalle/{cursoId}": {capacitacionId}
                        // es un hueco en la ruta que se llena con el número, y navArgument dice que es un Int.
                        // Se llama "capacitacion" (y no "curso") porque en esta app "curso" es una receta.
                        composable(
                            route = "capacitacion/{capacitacionId}",
                            arguments = listOf(navArgument("capacitacionId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            // Si no viene el argumento se usa 0 (no existe): la pantalla dice "Curso no encontrado"
                            val capacitacionId = backStackEntry.arguments?.getInt("capacitacionId") ?: 0
                            val capacitacionDetalleViewModel: CapacitacionDetalleViewModel = viewModel()
                            CapacitacionDetalleScreen(
                                capacitacionId = capacitacionId,
                                viewModel = capacitacionDetalleViewModel,
                                onVolver = { navController.popBackStack() },
                                // El producto principal abre su detalle: ej. "producto/9"
                                onVerProducto = { id -> navController.navigate("producto/$id") }
                            )
                        }

                        composable("favoritos") {
                            // Se crea aquí dentro porque solo lo usa esta pantalla.
                            // Es AndroidViewModel: viewModel() le pasa la Application solo.
                            val favoritosViewModel: FavoritosViewModel = viewModel()
                            FavoritosScreen(
                                viewModel = favoritosViewModel,
                                onVerDetalle = { id -> navController.navigate("detalle/$id") },
                                // Mismas opciones que la barra: así "Recetas" queda marcada abajo
                                // y Atrás desde Recetas vuelve a Inicio (no a Favoritos)
                                onIrARecetas = { navegarASeccion("recetas") }
                            )
                        }

                        composable("perfil") {
                            // Se crea aquí dentro porque solo lo usa esta pantalla.
                            // Es AndroidViewModel: viewModel() le pasa la Application solo.
                            val perfilViewModel: PerfilViewModel = viewModel()
                            PerfilScreen(
                                viewModel = perfilViewModel,
                                // Pila [home, perfil, registro]: al guardar, popBackStack() vuelve a Perfil
                                onEditarDatos = { navController.navigate("registro") },
                                // Pila [home, perfil, encuesta]: el botón volver de la encuesta regresa a Perfil
                                onIrAEncuesta = { navController.navigate("encuesta") },
                                onCerrarSesion = {
                                    // Borra la sesión guardada (id, rol, Plan Pro y datos del usuario)
                                    perfilViewModel.cerrarSesion()
                                    // Deja vacío el formulario de Registro, para que la próxima
                                    // persona no vea los datos de la anterior
                                    registroViewModel.cargarPerfilGuardado()
                                    navController.navigate("login") {
                                        // graph.id = el NavHost completo: saca TODAS las pantallas de la pila
                                        // (home, perfil...). inclusive = true saca también la raíz.
                                        // Queda solo [login]: Atrás desde el login cierra la app,
                                        // no vuelve a Perfil. La encuesta y la foto NO se borran.
                                        popUpTo(navController.graph.id) { inclusive = true }
                                    }
                                },
                                // Botones del "Panel de trabajo": la ruta viene de rol.opcionesPanel()
                                // (ej. "panel/clientes"). Pila [home, perfil, panel/...] → Atrás vuelve a Perfil
                                onAbrirPanel = { ruta -> navController.navigate(ruta) }
                            )
                        }

                        // PANELES DE TRABAJO (se abren desde Perfil, sin barra inferior porque
                        // sus rutas no están en itemsNavegacion). Los permisos los revisa el repositorio.
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
                    }
                }
            }
        }
    }
}
