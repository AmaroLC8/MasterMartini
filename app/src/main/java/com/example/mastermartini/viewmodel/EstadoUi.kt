package com.example.mastermartini.viewmodel

// ESTADO DE UNA CARGA (lista que viene del repositorio / backend)
//
// Una lista que se pide al servidor siempre está en UNO de estos 3 casos:
//   Cargando -> todavía no llega la respuesta
//   Exito    -> llegó bien y trae los datos
//   Error    -> falló y trae el mensaje para mostrar
//
// "sealed interface" (interfaz sellada): los 3 casos están escritos aquí y no puede haber otro.
// Es otro ejemplo de POLIMORFISMO: la pantalla recibe un EstadoUi sin saber cuál es,
// hace un "when (estado)" y dibuja algo distinto en cada caso:
//     when (estado) {
//         is EstadoUi.Cargando -> CircularProgressIndicator()
//         is EstadoUi.Error    -> Text(estado.mensaje)   // + botón "Reintentar"
//         is EstadoUi.Exito    -> LazyColumn { items(estado.datos) ... }
//     }
// Como es sellada, Kotlin sabe que solo hay 3 casos: el when no necesita "else",
// y si mañana se agrega un cuarto caso, no compila hasta que la pantalla lo dibuje.
//
// <out T>: T es el tipo de los datos (ej. List<Usuario>). "out" permite que Cargando y Error,
// que no traen datos (son EstadoUi<Nothing>), sirvan como estado de CUALQUIER lista.
// Además evita tener dos variables sueltas (cargando: Boolean + error: String?) que podrían
// contradecirse (ej. cargando = true y error al mismo tiempo).
sealed interface EstadoUi<out T> {

    // Esperando la respuesta. data object: hay uno solo, no trae datos
    data object Cargando : EstadoUi<Nothing>

    // Llegó bien: "datos" es lo que devolvió el repositorio (ej. la lista de usuarios)
    data class Exito<T>(val datos: T) : EstadoUi<T>

    // Falló: "mensaje" es el texto para mostrar (normalmente el de la UsuarioException)
    data class Error(val mensaje: String) : EstadoUi<Nothing>
}
