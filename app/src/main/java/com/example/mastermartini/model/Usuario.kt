package com.example.mastermartini.model

// MOLDE DE DATOS DE UN USUARIO
// Es lo que devuelve el backend (o el repositorio falso) al iniciar sesión, registrarse
// o pedir un perfil. Tiene los mismos datos del formulario de Registro, más el rol y el Plan Pro.
// NO incluye la contraseña: la contraseña solo viaja al iniciar sesión o registrarse,
// y nunca se guarda en el celular ni se devuelve en un perfil.
data class Usuario(
    // Número único que le da el backend a cada usuario. Long (y no Int) porque así lo
    // manejan las bases de datos de los servidores: admite números mucho más grandes.
    val id: Long,
    val nombre: String,
    val rut: String,
    val correo: String,
    val telefono: String,
    // "Panadería", "Pastelería" o "Chocolatería"
    val rubro: String,
    // Respuesta a "¿Cuántas personas trabajan en tu negocio?"
    val personasNegocio: String,
    // Qué puede hacer en la app (ver model/Rol.kt)
    val rol: Rol,
    // true si tiene la suscripción Plan Pro activa (desbloquea los cursos intermedios y avanzados)
    val planPro: Boolean
)
