package com.example.mastermartini.data

import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.model.Leccion
import com.example.mastermartini.model.Modulo

// REPOSITORIO DE CURSOS ONLINE (Capacitaciones)
// DATOS DE EJEMPLO, no son cursos reales de la empresa.
// Los instructores tienen nombres INVENTADOS (no son personas reales).
// Igual que CursoRepository, hoy los datos están escritos aquí con listOf();
// más adelante podrían venir de internet sin cambiar las pantallas.
// MODELO FREEMIUM: los 3 cursos básicos son gratis (esGratis = true) y los 3 intermedios
// o avanzados se desbloquean con el Plan Pro (esGratis = false).
// IMPORTANTE: insumoPrincipal debe ser IGUAL al nombre de un producto de ProductoRepository
// (con mayúsculas y tildes), porque el Detalle lo busca por su nombre exacto.
// La duración total de cada curso es la suma de los minutos de sus lecciones.
class CapacitacionRepository {

    // Devuelve los 6 cursos: primero los gratis y luego los del Plan Pro
    fun obtenerCapacitaciones(): List<Capacitacion> {
        return listOf(
            // ===== CURSOS BÁSICOS (GRATIS) =====
            Capacitacion(
                id = 1,
                titulo = "Chocolatería desde cero: templado y bombones",
                descripcion = "Aprende a templar chocolate puro y a hacer tus primeros bombones rellenos, listos para vender.",
                categoria = "Chocolatería",
                nivel = "Básico",
                esGratis = true,
                instructor = "Chef Elena Quiroga",
                // 30 + 45 + 50 = 125 min
                duracionTotal = "2 h 5 min",
                insumoPrincipal = "Chocolate Ariba",
                loQueLograras = listOf(
                    "Templar chocolate sin termómetro usando el método de la tabla",
                    "Saber si el chocolate quedó bien templado con la prueba del papel",
                    "Moldear bombones y sellarlos con ganache adentro",
                    "Calcular cuántos bombones rinde 1 kg de chocolate",
                    "Guardar los bombones para que no pierdan su brillo"
                ),
                incluye = listOf(
                    "Certificado al aprobar la evaluación final",
                    "Acceso sin límite de tiempo",
                    "Recetario descargable"
                ),
                modulos = listOf(
                    Modulo(
                        titulo = "Fundamentos del chocolate",
                        lecciones = listOf(
                            Leccion(
                                titulo = "Chocolate puro y cobertura: la diferencia",
                                descripcion = "Por qué el chocolate puro necesita templado y la cobertura no.",
                                duracion = "10 min"
                            ),
                            Leccion(
                                titulo = "Derretir sin quemar",
                                descripcion = "Baño maría y microondas en tandas de 30 segundos.",
                                duracion = "12 min"
                            ),
                            Leccion(
                                titulo = "Cómo guardar el chocolate",
                                descripcion = "Temperatura y humedad para que no se ponga blanco.",
                                duracion = "8 min"
                            )
                        )
                    ),
                    Modulo(
                        titulo = "Templado paso a paso",
                        lecciones = listOf(
                            Leccion(
                                titulo = "El método de la tabla",
                                descripcion = "Enfriar el chocolate sobre mármol y volver a juntarlo.",
                                duracion = "20 min"
                            ),
                            Leccion(
                                titulo = "La prueba del papel",
                                descripcion = "Comprobar el templado sin termómetro en 5 minutos.",
                                duracion = "10 min"
                            ),
                            Leccion(
                                titulo = "Errores comunes y cómo arreglarlos",
                                descripcion = "Chocolate opaco, con vetas o que no se despega del molde.",
                                duracion = "15 min"
                            )
                        )
                    ),
                    Modulo(
                        titulo = "Tus primeros bombones",
                        lecciones = listOf(
                            Leccion(
                                titulo = "Moldear los cascarones",
                                descripcion = "Llenar, vaciar y raspar el molde para una capa pareja.",
                                duracion = "18 min"
                            ),
                            Leccion(
                                titulo = "Rellenar con ganache y sellar",
                                descripcion = "Ganache firme que no se sale y tapa lisa.",
                                duracion = "20 min"
                            ),
                            Leccion(
                                titulo = "Rendimiento y costo de 1 kg",
                                descripcion = "Cuántos bombones salen y cuánto cuesta cada uno.",
                                duracion = "12 min"
                            )
                        )
                    )
                )
            ),
            Capacitacion(
                id = 2,
                titulo = "Masas dulces: brioche y pan de leche",
                descripcion = "Prepara masas dulces suaves y esponjosas para vender en tu panadería todos los días.",
                categoria = "Panadería",
                nivel = "Básico",
                esGratis = true,
                instructor = "Maestro panadero Hugo Retamal",
                // 30 + 50 + 20 = 100 min
                duracionTotal = "1 h 40 min",
                insumoPrincipal = "Master Export Horneo",
                loQueLograras = listOf(
                    "Reconocer cuándo la masa está lista con la prueba de la ventana",
                    "Hacer fermentar la masa en casa, sin cámara de fermentación",
                    "Formar un brioche trenzado y un pan de leche en molde",
                    "Calcular el costo de cada pan para ponerle precio"
                ),
                incluye = listOf(
                    "Certificado al aprobar la evaluación final",
                    "Acceso sin límite de tiempo",
                    "Planilla de costos descargable"
                ),
                modulos = listOf(
                    Modulo(
                        titulo = "La masa base",
                        lecciones = listOf(
                            Leccion(
                                titulo = "Ingredientes y para qué sirve cada uno",
                                descripcion = "Harina, levadura, azúcar, huevo y margarina de horneo.",
                                duracion = "12 min"
                            ),
                            Leccion(
                                titulo = "Amasar a mano y la prueba de la ventana",
                                descripcion = "Cómo saber que la masa ya está lista para fermentar.",
                                duracion = "18 min"
                            )
                        )
                    ),
                    Modulo(
                        titulo = "Fermentar y formar",
                        lecciones = listOf(
                            Leccion(
                                titulo = "Fermentar sin cámara",
                                descripcion = "Usar el horno apagado con una olla de agua caliente.",
                                duracion = "15 min"
                            ),
                            Leccion(
                                titulo = "Trenzas, bollos y moldes",
                                descripcion = "Tres formas de presentar la misma masa.",
                                duracion = "20 min"
                            ),
                            Leccion(
                                titulo = "Pincelar y hornear",
                                descripcion = "Brillo dorado sin quemar la base.",
                                duracion = "15 min"
                            )
                        )
                    ),
                    Modulo(
                        titulo = "Vender en tu local",
                        lecciones = listOf(
                            Leccion(
                                titulo = "Costo por unidad",
                                descripcion = "Sumar ingredientes y dividir por las unidades que salen.",
                                duracion = "12 min"
                            ),
                            Leccion(
                                titulo = "Guardar y presentar el pan",
                                descripcion = "Que el pan siga blando al día siguiente.",
                                duracion = "8 min"
                            )
                        )
                    )
                )
            ),
            Capacitacion(
                id = 3,
                titulo = "Decoración de tortas con crema batida",
                descripcion = "Bate, cubre y decora tortas con crema vegetal, con técnicas simples que se ven profesionales.",
                categoria = "Decoración",
                nivel = "Básico",
                esGratis = true,
                instructor = "Chef Paula Ibarra",
                // 30 + 30 + 45 = 105 min
                duracionTotal = "1 h 45 min",
                insumoPrincipal = "Bravo Crem",
                loQueLograras = listOf(
                    "Batir Bravo Crem hasta el punto firme sin que se corte",
                    "Cubrir y alisar una torta de 22 cm con espátula",
                    "Hacer rosetas, conchitas y bordes con tres boquillas",
                    "Teñir la crema con colorante sin que pierda firmeza",
                    "Decorar una torta de cumpleaños completa en 15 minutos"
                ),
                incluye = listOf(
                    "Certificado al aprobar la evaluación final",
                    "Acceso sin límite de tiempo",
                    "Guía de boquillas descargable"
                ),
                modulos = listOf(
                    Modulo(
                        titulo = "Batir la crema vegetal",
                        lecciones = listOf(
                            Leccion(
                                titulo = "La temperatura antes de batir",
                                descripcion = "Por qué la crema y el bol deben estar bien fríos.",
                                duracion = "8 min"
                            ),
                            Leccion(
                                titulo = "Batir hasta el punto firme",
                                descripcion = "Reconocer el punto justo antes de que se corte.",
                                duracion = "12 min"
                            ),
                            Leccion(
                                titulo = "Teñir la crema",
                                descripcion = "Cuánto colorante usar y cuándo agregarlo.",
                                duracion = "10 min"
                            )
                        )
                    ),
                    Modulo(
                        titulo = "Manga y boquillas",
                        lecciones = listOf(
                            Leccion(
                                titulo = "Llenar y sostener la manga",
                                descripcion = "Postura de la mano para no cansarse.",
                                duracion = "10 min"
                            ),
                            Leccion(
                                titulo = "Rosetas, conchitas y bordes",
                                descripcion = "Tres boquillas básicas y lo que se hace con cada una.",
                                duracion = "20 min"
                            )
                        )
                    ),
                    Modulo(
                        titulo = "La torta terminada",
                        lecciones = listOf(
                            Leccion(
                                titulo = "Cubrir y alisar",
                                descripcion = "Capa delgada, frío y capa final lisa.",
                                duracion = "18 min"
                            ),
                            Leccion(
                                titulo = "Torta de cumpleaños en 15 minutos",
                                descripcion = "Un diseño rápido que se puede repetir todos los días.",
                                duracion = "15 min"
                            ),
                            Leccion(
                                titulo = "Transportar la torta sin dañarla",
                                descripcion = "Base, caja y temperatura durante el viaje.",
                                duracion = "12 min"
                            )
                        )
                    )
                )
            ),

            // ===== CURSOS DEL PLAN PRO (INTERMEDIOS Y AVANZADOS) =====
            Capacitacion(
                id = 4,
                titulo = "Hojaldre y croissants de vitrina",
                descripcion = "Domina el laminado para hacer croissants, palmeras y vol au vent con capas bien marcadas.",
                categoria = "Panadería",
                nivel = "Intermedio",
                esGratis = false,
                instructor = "Chef Tomás Riquelme",
                // 52 + 38 + 50 = 140 min
                duracionTotal = "2 h 20 min",
                insumoPrincipal = "Gran Master Hoja",
                loQueLograras = listOf(
                    "Laminar masa de hoja con 3 vueltas dobles sin que se rompa",
                    "Cortar y enrollar croissants de 60 g, todos del mismo tamaño",
                    "Hornear croissants con capas marcadas y sin que se derrame la margarina",
                    "Preparar palmeras y vol au vent con los recortes de masa",
                    "Congelar masa laminada para producir toda la semana"
                ),
                incluye = listOf(
                    "Certificado al aprobar la evaluación final",
                    "Acceso sin límite de tiempo",
                    "Recetario descargable",
                    "Tabla de tiempos de laminado"
                ),
                modulos = listOf(
                    Modulo(
                        titulo = "El laminado",
                        lecciones = listOf(
                            Leccion(
                                titulo = "La masa base o empaste",
                                descripcion = "Una masa firme que aguante las vueltas.",
                                duracion = "15 min"
                            ),
                            Leccion(
                                titulo = "Preparar la margarina para laminar",
                                descripcion = "Darle forma de lámina a Gran Master Hoja.",
                                duracion = "12 min"
                            ),
                            Leccion(
                                titulo = "Vueltas simples y dobles",
                                descripcion = "Cómo doblar y cuánto reposar entre vueltas.",
                                duracion = "25 min"
                            )
                        )
                    ),
                    Modulo(
                        titulo = "Croissants",
                        lecciones = listOf(
                            Leccion(
                                titulo = "Cortar y enrollar",
                                descripcion = "Triángulos parejos para croissants iguales.",
                                duracion = "20 min"
                            ),
                            Leccion(
                                titulo = "Fermentar y hornear",
                                descripcion = "Temperaturas para que la margarina no se derrita antes de tiempo.",
                                duracion = "18 min"
                            )
                        )
                    ),
                    Modulo(
                        titulo = "Más productos de hojaldre",
                        lecciones = listOf(
                            Leccion(
                                titulo = "Palmeras",
                                descripcion = "Aprovechar los recortes con azúcar.",
                                duracion = "15 min"
                            ),
                            Leccion(
                                titulo = "Vol au vent dulces y salados",
                                descripcion = "Cortar, hornear y rellenar.",
                                duracion = "20 min"
                            ),
                            Leccion(
                                titulo = "Congelar para producir por semana",
                                descripcion = "Envolver, rotular y descongelar la masa.",
                                duracion = "15 min"
                            )
                        )
                    )
                )
            ),
            Capacitacion(
                id = 5,
                titulo = "Heladería vegana: sorbetes sin leche",
                descripcion = "Prepara sorbetes cremosos de chocolate y de fruta, sin leche, para clientes veganos.",
                categoria = "Heladería",
                nivel = "Intermedio",
                esGratis = false,
                instructor = "Chef Carmen Sepúlveda",
                // 25 + 45 + 40 = 110 min
                duracionTotal = "1 h 50 min",
                insumoPrincipal = "Ariba Zero Latto",
                loQueLograras = listOf(
                    "Preparar sorbete vegano de chocolate amargo con Ariba Zero Latto",
                    "Hacer sorbete cremoso sin máquina, revolviendo cada 30 minutos",
                    "Ajustar el azúcar para que el sorbete no quede duro como hielo",
                    "Hacer sorbetes con la fruta de la temporada",
                    "Rotular tus helados con sus ingredientes para clientes veganos"
                ),
                incluye = listOf(
                    "Certificado al aprobar la evaluación final",
                    "Acceso sin límite de tiempo",
                    "Recetario vegano descargable"
                ),
                modulos = listOf(
                    Modulo(
                        titulo = "Bases veganas",
                        lecciones = listOf(
                            Leccion(
                                titulo = "Cremoso sin leche: cómo se logra",
                                descripcion = "El papel del azúcar, el agua y el chocolate.",
                                duracion = "10 min"
                            ),
                            Leccion(
                                titulo = "Equilibrar el azúcar",
                                descripcion = "Cuánto azúcar lleva un sorbete para que no se congele como hielo.",
                                duracion = "15 min"
                            )
                        )
                    ),
                    Modulo(
                        titulo = "Sorbete de chocolate amargo",
                        lecciones = listOf(
                            Leccion(
                                titulo = "La base con Ariba Zero Latto",
                                descripcion = "Derretir el chocolate en el almíbar caliente.",
                                duracion = "15 min"
                            ),
                            Leccion(
                                titulo = "Madurar la mezcla",
                                descripcion = "Enfriar en el refrigerador antes de congelar.",
                                duracion = "15 min"
                            ),
                            Leccion(
                                titulo = "Sin máquina heladera",
                                descripcion = "Congelar y revolver cada 30 minutos.",
                                duracion = "15 min"
                            )
                        )
                    ),
                    Modulo(
                        titulo = "Fruta y venta",
                        lecciones = listOf(
                            Leccion(
                                titulo = "Sorbetes de fruta de temporada",
                                descripcion = "Frutilla, mango y frambuesa con la misma base.",
                                duracion = "18 min"
                            ),
                            Leccion(
                                titulo = "Rotular para clientes veganos",
                                descripcion = "Qué escribir en la etiqueta del pote.",
                                duracion = "10 min"
                            ),
                            Leccion(
                                titulo = "Guardar y servir en vitrina",
                                descripcion = "Temperatura de la vitrina y cómo servir la bola.",
                                duracion = "12 min"
                            )
                        )
                    )
                ),
                esVegano = true
            ),
            Capacitacion(
                id = 6,
                titulo = "Tortas mousse y entremets de vitrina",
                descripcion = "Arma tortas mousse de varias capas con glaseado espejo, y organiza su producción para vender.",
                categoria = "Pastelería",
                nivel = "Avanzado",
                esGratis = false,
                instructor = "Chef Andrés Molina",
                // 27 + 53 + 45 + 35 = 160 min
                duracionTotal = "2 h 40 min",
                insumoPrincipal = "Decor Up",
                loQueLograras = listOf(
                    "Armar una torta mousse de 3 capas al revés, en molde con acetato",
                    "Preparar una mousse firme con Decor Up que se corte limpia",
                    "Bañar una torta congelada con glaseado espejo a 35 °C",
                    "Calcular el costo y el precio de venta de una torta de 20 porciones",
                    "Organizar la producción de 10 tortas en una semana"
                ),
                incluye = listOf(
                    "Certificado al aprobar la evaluación final",
                    "Acceso sin límite de tiempo",
                    "Recetario descargable",
                    "Planilla de producción semanal"
                ),
                modulos = listOf(
                    Modulo(
                        titulo = "Planificar un entremet",
                        lecciones = listOf(
                            Leccion(
                                titulo = "Las capas y el orden de armado",
                                descripcion = "Bizcocho, mousse, inserto y glaseado.",
                                duracion = "15 min"
                            ),
                            Leccion(
                                titulo = "Moldes, acetato y tiempos de frío",
                                descripcion = "Qué se prepara el día antes y qué el mismo día.",
                                duracion = "12 min"
                            )
                        )
                    ),
                    Modulo(
                        titulo = "Mousses y rellenos",
                        lecciones = listOf(
                            Leccion(
                                titulo = "Mousse con Decor Up",
                                descripcion = "Una mousse firme que no se desarma al cortar.",
                                duracion = "20 min"
                            ),
                            Leccion(
                                titulo = "Inserto de fruta",
                                descripcion = "Un centro de fruta congelado que da sabor y color.",
                                duracion = "18 min"
                            ),
                            Leccion(
                                titulo = "Ganache para relleno",
                                descripcion = "Proporciones para un relleno cremoso.",
                                duracion = "15 min"
                            )
                        )
                    ),
                    Modulo(
                        titulo = "Armado y terminación",
                        lecciones = listOf(
                            Leccion(
                                titulo = "Armar al revés y congelar",
                                descripcion = "Por qué la base queda arriba dentro del molde.",
                                duracion = "20 min"
                            ),
                            Leccion(
                                titulo = "Glaseado espejo",
                                descripcion = "Bañar la torta congelada a la temperatura justa.",
                                duracion = "25 min"
                            )
                        )
                    ),
                    Modulo(
                        titulo = "Producir para vender",
                        lecciones = listOf(
                            Leccion(
                                titulo = "Costear una torta de 20 porciones",
                                descripcion = "Costo por porción y precio de venta.",
                                duracion = "15 min"
                            ),
                            Leccion(
                                titulo = "10 tortas en una semana",
                                descripcion = "Un calendario de producción para no trabajar de noche.",
                                duracion = "20 min"
                            )
                        )
                    )
                )
            )
        )
    }

    // Las mismas 5 secciones de las recetas (ver CursoRepository.obtenerCategorias()).
    // Se usan para los botones de filtro de la pantalla de Cursos.
    fun obtenerCategorias(): List<String> {
        return listOf("Chocolatería", "Pastelería", "Panadería", "Heladería", "Decoración")
    }

    // Busca UN curso por su id (lo usa la pantalla de detalle del curso).
    // find devuelve el primero que cumple la condición, o null si ninguno la cumple:
    // por eso devuelve Capacitacion? y la pantalla dice "Curso no encontrado".
    fun obtenerCapacitacionPorId(id: Int): Capacitacion? {
        return obtenerCapacitaciones().find { capacitacion -> capacitacion.id == id }
    }
}
