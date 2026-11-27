package com.example.mastermartini.data


import com.example.mastermartini.model.Curso

// Repositorio del catálogo local de recetas y fichas técnicas
class CursoRepository {

    fun obtenerCursos(): List<Curso> {
        return listOf(
            Curso(
                id = 1,
                titulo = "Curva de Templado en Chocolatería",
                insumo = "Chocolate Ariba",
                chef = "Chef Roberto Valenzuela",
                duracion = "15 min",
                pasos = listOf(
                    "Pica el Chocolate Ariba amargo en trozos pequeños y parejos.",
                    "Derrite dos tercios a baño maría, sin que entre agua, hasta llegar a 45-50 °C.",
                    "Agrega el tercio restante picado y revuelve hasta bajar a 28-29 °C.",
                    "Vuelve a calentar suavemente hasta 31-32 °C. Esa es la temperatura de trabajo.",
                    "Prueba en la punta de una espátula: debe quedar brillante y firme en unos 5 minutos.",
                    "Llena los moldes de bombones, retira el exceso y deja cristalizar a temperatura ambiente."
                ),
                categoria = "Chocolatería",
                descripcion = "Aprende la curva de templado del chocolate puro para lograr bombones brillantes, crujientes y que no se derriten en la mano.",
                ingredientes = listOf(
                    "600 g de Chocolate Ariba amargo",
                    "150 g de ganache o praliné para rellenar (opcional)",
                    "50 g de almendras tostadas para decorar (opcional)",
                    "1 pizca de sal de mar para decorar (opcional)"
                ),
                rendimiento = "Rinde 40 bombones",
                dificultad = "Avanzada",
                videoUrl = DatosEmpresa.URL_VIDEO_MASTERCLASS,
                imagenUrl = DatosEmpresa.URL_IMG_CHOCOLATERIA
            ),
            Curso(
                id = 2,
                titulo = "Estabilización y Batido de Cremas",
                insumo = "Bravo Crem",
                chef = "Chef Elena Rossi",
                duracion = "20 min",
                pasos = listOf(
                    "Enfría la Bravo Crem en el refrigerador a 4 °C desde el día anterior.",
                    "Enfría también el bol y las varillas de la batidora.",
                    "Bate a velocidad media hasta que la crema empiece a espesar.",
                    "No agregues azúcar: Bravo Crem ya viene azucarada.",
                    "Detén el batido cuando la crema forme picos firmes.",
                    "Úsala de inmediato o guárdala tapada en el refrigerador."
                ),
                categoria = "Decoración",
                descripcion = "Aprende a batir y estabilizar crema vegetal para decorar tortas con una crema firme que dura varios días.",
                ingredientes = listOf(
                    "1 litro de Bravo Crem",
                    "1 cucharadita de esencia de vainilla (opcional)",
                    "Colorante en gel (opcional)"
                ),
                rendimiento = "Decora 2 tortas de 22 cm",
                dificultad = "Fácil",
                videoUrl = DatosEmpresa.URL_VIDEO_MASTERCLASS,
                imagenUrl = DatosEmpresa.URL_IMG_PASTELERIA
            ),
            Curso(
                id = 3,
                titulo = "Laminado de Masa de Hoja Artesanal",
                insumo = "Gran Master Hoja",
                chef = "Maestro Marco Zanetti",
                duracion = "35 min",
                pasos = listOf(
                    "Prepara la masa base con harina, agua y sal. Deja reposar 30 minutos.",
                    "Estira la masa en forma de cruz.",
                    "Pon la Gran Master Hoja al centro y envuélvela con la masa.",
                    "Estira y dobla la masa en tres. Repite este pliegue tres veces.",
                    "Refrigera 20 minutos entre cada pliegue.",
                    "Corta las piezas y hornea a 200 °C hasta que estén doradas."
                ),
                categoria = "Panadería",
                descripcion = "Aprende a laminar masa de hoja con margarina especial para hojaldre y lograr capas crujientes y bien separadas.",
                ingredientes = listOf(
                    "1 kg de harina sin polvos de hornear",
                    "500 ml de agua fría",
                    "20 g de sal",
                    "500 g de Gran Master Hoja",
                    "1 huevo para pintar"
                ),
                rendimiento = "Rinde 30 palmeras",
                dificultad = "Avanzada",
                videoUrl = DatosEmpresa.URL_VIDEO_MASTERCLASS,
                imagenUrl = DatosEmpresa.URL_IMG_PANADERIA
            ),
            Curso(
                id = 4,
                titulo = "Torta Chocolate Avellana Caravella",
                insumo = "Caravella Escamas Chocolate",
                chef = "Chef Master Martini",
                duracion = "35 min",
                pasos = listOf(
                    "Divide el bizcochuelo de chocolate en 3 partes parejas.",
                    "Humedece la primera capa con el jarabe de remojo.",
                    "Rellena con chocolate avellana y mermelada de guinda, e incorpora el segundo bizcochuelo.",
                    "Repite el remojo y el relleno, y coloca la última capa de bizcochuelo.",
                    "Cubre la superficie y el contorno de la torta con ganache de chocolate negro.",
                    "Decora con Caravella escamas de chocolate."
                ),
                categoria = "Decoración",
                descripcion = "Aprende a armar, rellenar y cubrir una torta de chocolate y avellana con ganache y escamas Caravella.",
                ingredientes = listOf(
                    "1 bizcochuelo de chocolate (15 personas)",
                    "160 g de chocolate avellana",
                    "200 g de mermelada de guinda",
                    "300 g de jarabe de remojo",
                    "200 g de ganache de chocolate negro",
                    "100 g de Caravella escamas de chocolate"
                ),
                rendimiento = "Rinde 15 porciones",
                dificultad = "Media",
                videoUrl = DatosEmpresa.URL_VIDEO_TORTA_CHOCOLATE_AVELLANA,
                imagenUrl = DatosEmpresa.URL_IMG_CHOCOLATERIA
            ),
            Curso(
                id = 5,
                titulo = "Helado Artesanal de Mix de Berries",
                insumo = "Orasì Soia",
                chef = "Chef Ignacio Bravo Durán",
                duracion = "20 min",
                pasos = listOf(
                    "Vierte 1 litro de base Orasì Soia en un jarro medidor.",
                    "Agrega los 400 g de mix de berries sobre la base vegetal.",
                    "Mixea hasta lograr una mezcla completamente homogénea.",
                    "Lleva la mezcla a la máquina mantecadora de helados.",
                    "Espera 12 minutos de mantecado, retira y monta para servir."
                ),
                categoria = "Heladería",
                descripcion = "Prepara un helado artesanal vegano de frutos rojos usando base Orasì Soia y mix de berries.",
                ingredientes = listOf(
                    "1 litro de base Orasì Soia",
                    "400 g de mix de berries"
                ),
                rendimiento = "Rinde 1,4 litros aprox.",
                dificultad = "Fácil",
                esVegano = true,
                videoUrl = DatosEmpresa.URL_VIDEO_HELADO_BERRIES,
                imagenUrl = DatosEmpresa.URL_IMG_CHOCOLATERIA
            ),
            Curso(
                id = 6,
                titulo = "Ganache y Relleno para Macarons",
                insumo = "Chocolate Ariba",
                chef = "Chef Lucía Fernández",
                duracion = "30 min",
                pasos = listOf(
                    "Pica el Chocolate Ariba de leche en trozos pequeños.",
                    "Calienta la Decor Up hasta que hierva suavemente.",
                    "Vierte la crema sobre el chocolate y espera 1 minuto.",
                    "Mezcla desde el centro hasta lograr una ganache lisa y agrega la mantequilla.",
                    "Deja que tome cuerpo y rellena las tapas de los macarons."
                ),
                categoria = "Pastelería",
                descripcion = "Aprende a preparar una ganache de chocolate de leche suave y firme, ideal para rellenar macarons.",
                ingredientes = listOf(
                    "300 g de Chocolate Ariba de leche",
                    "150 ml de Decor Up",
                    "20 g de mantequilla sin sal",
                    "40 tapas de macarons",
                    "1 pizca de sal"
                ),
                rendimiento = "Rellena 20 macarons",
                dificultad = "Media",
                videoUrl = DatosEmpresa.URL_VIDEO_MASTERCLASS,
                imagenUrl = DatosEmpresa.URL_IMG_PASTELERIA
            ),
            Curso(
                id = 7,
                titulo = "Brioche Tradicional Italiano",
                insumo = "Master Export Horneo",
                chef = "Maestro Giovanni Rossi",
                duracion = "45 min",
                pasos = listOf(
                    "Mezcla harina, azúcar, levadura y huevos.",
                    "Amasa durante 10 minutos hasta que la masa esté elástica.",
                    "Incorpora la Master Export Horneo de a poco.",
                    "Deja leudar hasta que la masa duplique su tamaño.",
                    "Forma los panes, píntalos con huevo y hornea a 180 °C.",
                    "Deja enfriar y espolvorea Azúcar Glass si quieres."
                ),
                categoria = "Panadería",
                descripcion = "Aprende a hacer un brioche esponjoso y dorado, con el amasado y el leudado que pide la receta italiana.",
                ingredientes = listOf(
                    "500 g de harina",
                    "60 g de azúcar",
                    "10 g de levadura seca",
                    "5 huevos (4 para la masa y 1 para pintar)",
                    "200 g de Master Export Horneo",
                    "1 pizca de sal",
                    "Azúcar Glass para espolvorear (opcional)"
                ),
                rendimiento = "Rinde 12 bollos",
                dificultad = "Media",
                videoUrl = DatosEmpresa.URL_VIDEO_MASTERCLASS,
                imagenUrl = DatosEmpresa.URL_IMG_PANADERIA
            ),
            Curso(
                id = 8,
                titulo = "Arroz con Leche con Orasì Almendra",
                insumo = "Orasì Bio Almendra",
                chef = "Chef Master Martini",
                duracion = "25 min",
                pasos = listOf(
                    "En una cacerola, incorpora el arroz y el agua junto con la canela en rama y las cáscaras de naranja.",
                    "Cocina a fuego medio hasta que el arroz comience a ablandarse.",
                    "Incorpora los 800 ml de Orasì Bio Almendra y sigue revolviendo constantemente.",
                    "Añade el endulzante a gusto y continúa revolviendo hasta obtener cremosidad.",
                    "Sirve tibio o frío en pocillos y decora con canela."
                ),
                categoria = "Pastelería",
                descripcion = "Prepara un arroz con leche cremoso y 100% vegetal usando Orasì Bio Almendra, canela y cáscara de naranja.",
                ingredientes = listOf(
                    "250 g de arroz",
                    "500 ml de agua",
                    "Canela en rama",
                    "Cáscaras de naranja",
                    "Endulzante a gusto",
                    "800 ml de Orasì Bio Almendra"
                ),
                rendimiento = "Rinde 6 porciones",
                dificultad = "Fácil",
                esVegano = true,
                videoUrl = DatosEmpresa.URL_VIDEO_ARROZ_CON_LECHE,
                imagenUrl = DatosEmpresa.URL_IMG_PASTELERIA
            ),
            Curso(
                id = 9,
                titulo = "Fetuccini en Salsa de Setas con Orasì Avena",
                insumo = "Orasì Bio Avena",
                chef = "Chef Master Martini",
                duracion = "25 min",
                pasos = listOf(
                    "En un sartén con aceite, agrega las setas y los aliños para saltear.",
                    "Prepara la salsa añadiendo los 300 ml de Orasì Bio Avena y la harina blanca.",
                    "Agrega algo de aliño y revuelve hasta obtener una consistencia cremosa.",
                    "Cuece la pasta al dente y escúrrela.",
                    "Adhiere los fetuccini a la salsa de setas y combina bien antes de servir."
                ),
                categoria = "Cocina",
                descripcion = "Prepara fetuccini en salsa cremosa de setas sin lácteos utilizando Orasì Bio Avena.",
                ingredientes = listOf(
                    "120 g de pasta (fetuccini)",
                    "150 g de setas",
                    "50 g de harina blanca",
                    "100 ml de aceite",
                    "Aliños a gusto",
                    "300 ml de Orasì Bio Avena"
                ),
                rendimiento = "Rinde 2 porciones",
                dificultad = "Fácil",
                esVegano = true,
                videoUrl = DatosEmpresa.URL_VIDEO_FETUCCINI_SETAS,
                imagenUrl = DatosEmpresa.URL_IMG_PASTELERIA
            ),
            Curso(
                id = 10,
                titulo = "Helado Artesanal de Avellana",
                insumo = "Pasta de Avellana Master Martini",
                chef = "Chef Ignacio Bravo Durán",
                duracion = "20 min",
                pasos = listOf(
                    "Añade 1 litro de base blanca y 100 g de pasta de avellana en un jarro.",
                    "Mixea hasta integrar por completo la pasta de avellana.",
                    "Vierte la mezcla en la mantecadora (equipo Frigomat G10) hasta lograr cremosidad.",
                    "Sirve en copa acompañado de brownie y avellana trozada."
                ),
                categoria = "Heladería",
                descripcion = "Elabora un helado artesanal de avellana cremoso con base blanca, pasta de avellana, brownie y avellanas trozadas.",
                ingredientes = listOf(
                    "1 litro de base blanca",
                    "100 g de pasta de avellana",
                    "100 g de brownie",
                    "20 g de avellana trozada"
                ),
                rendimiento = "Rinde 1,2 litros aprox.",
                dificultad = "Fácil",
                videoUrl = DatosEmpresa.URL_VIDEO_HELADO_AVELLANA,
                imagenUrl = DatosEmpresa.URL_IMG_CHOCOLATERIA
            )
        )
    }

    fun obtenerCategorias(): List<String> {
        return listOf("Chocolatería", "Pastelería", "Panadería", "Heladería", "Decoración", "Cocina")
    }

    fun obtenerCursoPorId(id: Int): Curso? {
        return obtenerCursos().find { curso -> curso.id == id }
    }
}
