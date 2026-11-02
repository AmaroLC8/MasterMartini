package com.example.mastermartini.data

import com.example.mastermartini.model.Curso

// REPOSITORIO DE CURSOS
// Es la única parte de la app que sabe DE DÓNDE salen los cursos.
// Hoy los datos son ficticios y están escritos aquí con listOf().
// En el futuro podrían venir de Room (base de datos del celular) o de una API en internet:
// el cambio grande sería en esta clase, y la pantalla seguiría igual, porque solo
// dibuja la lista que le entrega el ViewModel sin saber de dónde vienen los datos.
class CursoRepository {

    // Devuelve el catálogo completo (lista inmutable: nadie puede agregar ni borrar cursos desde afuera)
    fun obtenerCursos(): List<Curso> {
        return listOf(
            Curso(
                id = 1,
                titulo = "Curva de Templado en Chocolatería",
                insumo = "Cobertura Caravella Dark 54%",
                chef = "Chef Roberto Valenzuela",
                duracion = "15 min",
                pasos = listOf(
                    "Pica la cobertura Caravella Dark 54% en trozos pequeños.",
                    "Derrite dos tercios a baño maría hasta llegar a 45 °C.",
                    "Agrega el resto de la cobertura y mezcla hasta bajar a 28 °C.",
                    "Vuelve a calentar suavemente hasta 31 °C.",
                    "Prueba en una espátula: debe quedar brillante y firme en 5 minutos."
                ),
                categoria = "Chocolatería",
                descripcion = "Aprende a templar chocolate paso a paso para lograr bombones brillantes y crujientes que no se derriten en la mano.",
                ingredientes = listOf(
                    "600 g de Cobertura Caravella Dark 54%",
                    "150 g de ganache o praliné para rellenar (opcional)",
                    "50 g de almendras tostadas para decorar (opcional)",
                    "1 pizca de sal de mar para decorar (opcional)"
                )
            ),
            Curso(
                id = 2,
                titulo = "Estabilización y Batido de Cremas",
                insumo = "Crema Vegetal Decor Up",
                chef = "Chef Elena Rossi",
                duracion = "20 min",
                pasos = listOf(
                    "Enfría la Crema Vegetal Decor Up en el refrigerador a 4 °C.",
                    "Enfría también el bol y las varillas de la batidora.",
                    "Bate a velocidad media hasta que la crema empiece a espesar.",
                    "Agrega azúcar flor de a poco, sin dejar de batir.",
                    "Detén el batido cuando la crema forme picos firmes."
                ),
                categoria = "Decoración",
                descripcion = "Aprende a batir y estabilizar crema vegetal para decorar tortas con una crema firme que dura varios días.",
                ingredientes = listOf(
                    "1 litro de Crema Vegetal Decor Up",
                    "80 g de azúcar flor",
                    "1 cucharadita de esencia de vainilla",
                    "Colorante en gel (opcional)"
                )
            ),
            Curso(
                id = 3,
                titulo = "Laminado de Masa de Hoja Artesanal",
                insumo = "Margarina Master Martini Placa",
                chef = "Maestro Marco Zanetti",
                duracion = "35 min",
                pasos = listOf(
                    "Prepara la masa base con harina, agua y sal. Deja reposar 30 minutos.",
                    "Estira la masa en forma de cruz.",
                    "Pon la Margarina Master Martini Placa al centro y envuélvela con la masa.",
                    "Estira y dobla la masa en tres. Repite este pliegue tres veces.",
                    "Refrigera 20 minutos entre cada pliegue.",
                    "Corta las piezas y hornea a 200 °C hasta que estén doradas."
                ),
                categoria = "Panadería",
                descripcion = "Aprende a laminar masa de hoja con margarina en placa para lograr capas crujientes y bien separadas.",
                ingredientes = listOf(
                    "1 kg de harina sin polvos de hornear",
                    "500 ml de agua fría",
                    "20 g de sal",
                    "500 g de Margarina Master Martini Placa",
                    "1 huevo para pintar"
                )
            ),
            Curso(
                id = 4,
                titulo = "Técnica del Glaseado Espejo Brillante",
                insumo = "Glaseado Mirall Chocolate",
                chef = "Chef Sofía Lombardo",
                duracion ="25 min",
                pasos = listOf(
                    "Congela el postre que vas a cubrir por al menos 4 horas.",
                    "Calienta el Glaseado Mirall Chocolate hasta 40 °C.",
                    "Mezcla suavemente para no formar burbujas.",
                    "Deja enfriar el glaseado hasta 32 °C.",
                    "Vierte el glaseado sobre el postre congelado, de una sola vez.",
                    "Retira el exceso con una espátula y refrigera."
                ),
                categoria = "Decoración",
                descripcion = "Aprende a cubrir postres congelados con un glaseado de chocolate liso y brillante como un espejo.",
                ingredientes = listOf(
                    "500 g de Glaseado Mirall Chocolate",
                    "1 torta de mousse congelada de 20 cm",
                    "100 g de frutillas frescas para decorar",
                    "30 g de escamas de chocolate para el borde",
                    "Hojas de menta para decorar"
                )
            ),
            Curso(
                id = 5,
                titulo = "Gelato Artesanal de Avellana",
                insumo = "Pasta de Avellana Pura Ariba",
                chef = "Maestro Matteo Bianco",
                duracion ="40 min",
                pasos = listOf(
                    "Calienta leche, crema y azúcar hasta 85 °C.",
                    "Agrega la Pasta de Avellana Pura Ariba y mezcla bien.",
                    "Enfría la mezcla y déjala reposar 4 horas en el refrigerador.",
                    "Bate en la máquina de helados hasta que quede cremoso.",
                    "Guarda el gelato en el congelador a -18 °C."
                ),
                categoria = "Heladería",
                descripcion = "Aprende a preparar un gelato cremoso de avellana al estilo italiano, desde la mezcla base hasta la conservación.",
                ingredientes = listOf(
                    "700 ml de leche entera",
                    "200 ml de crema de leche",
                    "150 g de azúcar",
                    "120 g de Pasta de Avellana Pura Ariba",
                    "Avellanas tostadas picadas para decorar"
                )
            ),
            Curso(
                id = 6,
                titulo = "Ganache y Relleno para Macarons",
                insumo = "Cobertura Caravella Milk 34%",
                chef = "Chef Lucía Fernández",
                duracion = "30 min",
                pasos = listOf(
                    "Pica la Cobertura Caravella Milk 34% en trozos pequeños.",
                    "Calienta la crema hasta que hierva suavemente.",
                    "Vierte la crema sobre la cobertura y espera 1 minuto.",
                    "Mezcla desde el centro hasta lograr una ganache lisa.",
                    "Deja que tome cuerpo y rellena las tapas de los macarons."
                ),
                categoria = "Pastelería",
                descripcion = "Aprende a preparar una ganache de chocolate de leche suave y firme, ideal para rellenar macarons.",
                ingredientes = listOf(
                    "300 g de Cobertura Caravella Milk 34%",
                    "150 ml de crema de leche",
                    "20 g de mantequilla sin sal",
                    "40 tapas de macarons",
                    "1 pizca de sal"
                )
            ),
            Curso(
                id = 7,
                titulo = "Brioche Tradicional Italiano",
                insumo = "Margarina Maestro Martini Soft",
                chef = "Maestro Giovanni Rossi",
                duracion = "45 min",
                pasos = listOf(
                    "Mezcla harina, azúcar, levadura y huevos.",
                    "Amasa durante 10 minutos hasta que la masa esté elástica.",
                    "Incorpora la Margarina Maestro Martini Soft de a poco.",
                    "Deja leudar hasta que la masa duplique su tamaño.",
                    "Forma los panes, píntalos con huevo y hornea a 180 °C."
                ),
                categoria = "Panadería",
                descripcion = "Aprende a hacer un brioche esponjoso y dorado, con el amasado y el leudado que pide la receta italiana.",
                ingredientes = listOf(
                    "500 g de harina",
                    "60 g de azúcar",
                    "10 g de levadura seca",
                    "5 huevos (4 para la masa y 1 para pintar)",
                    "200 g de Margarina Maestro Martini Soft",
                    "1 pizca de sal"
                )
            ),
            Curso(
                id = 8,
                titulo = "Mousse de Fruta de la Pasión y Chocolate Blanco",
                insumo = "Cobertura Caravella White",
                chef = "Chef Valentina Morales",
                duracion = "20 min",
                pasos = listOf(
                    "Derrite la Cobertura Caravella White hasta 40 °C.",
                    "Agrega la pulpa de fruta de la pasión tibia y mezcla.",
                    "Bate la crema a medio punto, que todavía se mueva al inclinar el bol.",
                    "Une la crema a la mezcla con movimientos envolventes.",
                    "Rellena los moldes y refrigera por 4 horas."
                ),
                categoria = "Pastelería",
                descripcion = "Aprende a preparar una mousse liviana que combina el sabor ácido del maracuyá con el chocolate blanco.",
                ingredientes = listOf(
                    "250 g de Cobertura Caravella White",
                    "150 ml de pulpa de fruta de la pasión",
                    "300 ml de crema para batir",
                    "Semillas de fruta de la pasión para decorar"
                )
            ),
            Curso(
                id = 9,
                titulo = "Tartaletas con Crema Pastelera de Alto Rendimiento",
                insumo = "Crema Vegetal Master Gourmet",
                chef = "Chef Carlos Gutiérrez",
                duracion = "30 min",
                pasos = listOf(
                    "Hornea las bases de tartaleta a 170 °C hasta que estén doradas.",
                    "Calienta la Crema Vegetal Master Gourmet con leche y vainilla.",
                    "En un bol, mezcla yemas, azúcar y maicena.",
                    "Une todo y cocina sin dejar de revolver hasta que espese.",
                    "Deja enfriar la crema y rellena las tartaletas.",
                    "Decora con fruta fresca."
                ),
                categoria = "Pastelería",
                descripcion = "Aprende a hacer una crema pastelera que rinde más y no se corta, para rellenar tartaletas de venta diaria.",
                ingredientes = listOf(
                    "12 bases de tartaleta",
                    "250 ml de Crema Vegetal Master Gourmet",
                    "250 ml de leche",
                    "4 yemas de huevo",
                    "100 g de azúcar",
                    "40 g de maicena",
                    "1 cucharadita de esencia de vainilla"
                )
            ),
            Curso(
                id = 10,
                titulo = "Trufas Artesanales de Cacao Intenso",
                insumo = "Cacao en Polvo Master Martini 22/24",
                chef = "Maestro Andrea Conti",
                duracion = "15 min",
                pasos = listOf(
                    "Prepara una ganache con chocolate picado y crema caliente.",
                    "Refrigera la ganache 2 horas, hasta que esté firme.",
                    "Forma bolitas del mismo tamaño con las manos.",
                    "Pasa cada bolita por el Cacao en Polvo Master Martini 22/24.",
                    "Guarda las trufas en un lugar fresco hasta servirlas."
                ),
                categoria = "Chocolatería",
                descripcion = "Aprende a formar trufas de ganache del mismo tamaño y a cubrirlas con cacao para venderlas en cajas de regalo.",
                ingredientes = listOf(
                    "250 g de chocolate semiamargo",
                    "125 ml de crema de leche",
                    "80 g de Cacao en Polvo Master Martini 22/24",
                    "50 g de nueces picadas para cubrir (opcional)"
                )
            )
        )
    }

    // Devuelve las 5 secciones de recetas. Coinciden con las opciones de la encuesta
    // y con el campo "categoria" de cada curso. La pantalla de Recetas las usará
    // para mostrar los botones de filtro. Se escriben aquí (y no en la pantalla)
    // porque son datos de la app: si mañana se agrega una sección, se cambia solo este lugar.
    fun obtenerCategorias(): List<String> {
        return listOf("Chocolatería", "Pastelería", "Panadería", "Heladería", "Decoración")
    }

    // Busca UN curso por su id (lo usa la pantalla de Detalle).
    // find recorre la lista y devuelve el primer curso que cumple la condición,
    // o null si ninguno la cumple (por ejemplo, un id que no existe).
    // Por eso devuelve Curso? (con signo de interrogación: puede ser null).
    fun obtenerCursoPorId(id: Int): Curso? {
        return obtenerCursos().find { curso -> curso.id == id }
    }
}
