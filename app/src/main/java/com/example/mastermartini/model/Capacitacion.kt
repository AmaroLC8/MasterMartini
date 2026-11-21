package com.example.mastermartini.model

// MOLDE DE DATOS DE UN CURSO ONLINE
// OJO con los nombres: en esta app "Curso" ya significa RECETA (model/Curso.kt).
// Por eso los cursos online se llaman "Capacitacion", para no confundirlos.
// Un curso online se arma así: Capacitacion -> Modulos -> Lecciones.
data class Capacitacion(
    // Identificador único. Se usa en la ruta "capacitacion/{capacitacionId}" para abrir su detalle.
    val id: Int,
    val titulo: String,
    // Una o dos frases que cuentan de qué trata el curso
    val descripcion: String,
    // Las mismas secciones de las recetas: "Chocolatería", "Pastelería", "Panadería",
    // "Heladería" o "Decoración". Se usa para filtrar en la pantalla de Cursos.
    val categoria: String,
    // "Básico", "Intermedio" o "Avanzado". Texto simple (no enum) porque solo se muestra.
    val nivel: String,
    // MODELO FREEMIUM: true = curso gratis (los básicos).
    // false = se desbloquea con la suscripción "Plan Pro".
    // Los cursos NO tienen precio propio: el precio es el del Plan Pro (DatosEmpresa.PRECIO_PLAN_PRO).
    val esGratis: Boolean,
    // Nombre del instructor (inventado, ver CapacitacionRepository)
    val instructor: String,
    // Duración de todo el curso como texto, ej. "2 h 30 min"
    val duracionTotal: String,
    // Nombre EXACTO de un producto de ProductoRepository. Así el Detalle lo encuentra
    // con obtenerProductoPorNombre() y abre su ficha, igual que el insumo de una receta.
    val insumoPrincipal: String,
    // Video corto que presenta el curso. null por ahora: se agrega en la fase 4 (AWS S3).
    val videoResumenUrl: String? = null,
    // Cosas CONCRETAS que la persona podrá hacer al terminar (4 o 5)
    val loQueLograras: List<String>,
    // Lo que trae el curso (certificado, acceso sin límite, recetario...)
    val incluye: List<String>,
    // Módulos del curso, en orden. Cada uno trae sus lecciones.
    val modulos: List<Modulo>,
    // true si el curso usa solo ingredientes veganos. Por defecto false.
    val esVegano: Boolean = false
) {
    // PROPIEDAD CALCULADA: cuántas lecciones tiene el curso en total.
    // sumOf suma el tamaño de la lista de lecciones de cada módulo.
    // Ej: módulos con 3, 3 y 2 lecciones -> 8 lecciones.
    val totalLecciones: Int
        get() = modulos.sumOf { modulo -> modulo.lecciones.size }
}
