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
                )
            )
        )
    }

    // Busca UN curso por su id (lo usa la pantalla de Detalle).
    // find recorre la lista y devuelve el primer curso que cumple la condición,
    // o null si ninguno la cumple (por ejemplo, un id que no existe).
    // Por eso devuelve Curso? (con signo de interrogación: puede ser null).
    fun obtenerCursoPorId(id: Int): Curso? {
        return obtenerCursos().find { curso -> curso.id == id }
    }
}
