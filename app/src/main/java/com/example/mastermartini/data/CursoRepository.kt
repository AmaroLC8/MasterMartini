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
                titulo = "Técnica del Glaseado Espejo Brillante",
                insumo = "Cobertura Caravella",
                chef = "Chef Sofía Lombardo",
                duracion = "25 min",
                pasos = listOf(
                    "Congela el postre que vas a cubrir por al menos 4 horas.",
                    "Hidrata la gelatina en agua fría por 5 minutos.",
                    "Hierve el agua con el azúcar y la glucosa. Retira del fuego y agrega la leche condensada y la gelatina.",
                    "Vierte la mezcla caliente sobre la Cobertura Caravella picada y mezcla sin formar burbujas.",
                    "Deja enfriar el glaseado hasta 32 °C.",
                    "Vierte el glaseado sobre el postre congelado, de una sola vez.",
                    "Retira el exceso con una espátula y refrigera."
                ),
                categoria = "Decoración",
                descripcion = "Aprende a cubrir postres congelados con un glaseado de chocolate liso y brillante como un espejo, usando una cobertura que no necesita templado.",
                ingredientes = listOf(
                    "200 g de Cobertura Caravella",
                    "150 g de azúcar",
                    "150 g de glucosa",
                    "75 ml de agua",
                    "100 g de leche condensada",
                    "10 g de gelatina sin sabor",
                    "1 torta de mousse congelada de 20 cm"
                ),
                rendimiento = "Cubre 1 torta de 20 cm",
                dificultad = "Avanzada",
                videoUrl = DatosEmpresa.URL_VIDEO_MASTERCLASS,
                imagenUrl = DatosEmpresa.URL_IMG_CHOCOLATERIA
            ),
            Curso(
                id = 5,
                titulo = "Sorbete Vegano de Chocolate Amargo",
                insumo = "Ariba Zero Latto",
                chef = "Maestro Matteo Bianco",
                duracion = "40 min",
                pasos = listOf(
                    "Calienta el agua con el azúcar hasta que el azúcar se disuelva por completo.",
                    "Agrega el cacao en polvo y la sal, y revuelve hasta que no queden grumos.",
                    "Retira del fuego, agrega el Ariba Zero Latto picado y mezcla hasta que se derrita.",
                    "Enfría la mezcla y déjala reposar 4 horas en el refrigerador.",
                    "Bate en la máquina de helados hasta que quede cremoso.",
                    "Guarda el sorbete en el congelador a -18 °C."
                ),
                categoria = "Heladería",
                descripcion = "Aprende a preparar un sorbete cremoso de chocolate amargo hecho con agua, sin leche ni huevo, apto para clientes veganos.",
                ingredientes = listOf(
                    "600 ml de agua",
                    "180 g de azúcar",
                    "150 g de Ariba Zero Latto",
                    "40 g de cacao en polvo sin azúcar",
                    "1 pizca de sal"
                ),
                rendimiento = "Rinde 1 litro (8 porciones)",
                dificultad = "Media",
                esVegano = true,
                videoUrl = DatosEmpresa.URL_VIDEO_MASTERCLASS,
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
                titulo = "Mousse Vegana de Fruta de la Pasión",
                insumo = "Orasì",
                chef = "Chef Valentina Morales",
                duracion = "20 min",
                pasos = listOf(
                    "Enfría la Orasì en el refrigerador a 4 °C desde el día anterior.",
                    "Hierve la pulpa de fruta de la pasión con el azúcar y el agar-agar por 2 minutos, sin dejar de revolver.",
                    "Deja entibiar la mezcla hasta unos 40 °C.",
                    "Bate la Orasì a medio punto, que todavía se mueva al inclinar el bol.",
                    "Une la crema batida a la pulpa con movimientos envolventes.",
                    "Rellena los vasitos y refrigera por 4 horas."
                ),
                categoria = "Pastelería",
                descripcion = "Aprende a preparar una mousse liviana de maracuyá sin leche, huevo ni gelatina animal, apta para clientes veganos.",
                ingredientes = listOf(
                    "400 ml de Orasì",
                    "200 ml de pulpa de fruta de la pasión",
                    "60 g de azúcar",
                    "2 g de agar-agar en polvo",
                    "Semillas de fruta de la pasión para decorar"
                ),
                rendimiento = "Rinde 8 vasitos de 150 ml",
                dificultad = "Media",
                esVegano = true,
                videoUrl = DatosEmpresa.URL_VIDEO_MASTERCLASS,
                imagenUrl = DatosEmpresa.URL_IMG_PASTELERIA
            ),
            Curso(
                id = 9,
                titulo = "Tartaletas de Fruta con Crema Pastelera",
                insumo = "Crema Pastelera",
                chef = "Chef Carlos Gutiérrez",
                duracion = "30 min",
                pasos = listOf(
                    "Hornea las bases de tartaleta a 170 °C hasta que estén doradas y déjalas enfriar.",
                    "Pon la leche fría en un bol.",
                    "Agrega el polvo de Crema Pastelera de a poco (revisa la proporción del envase).",
                    "Bate 3 a 4 minutos hasta que espese. No hay que cocinarla.",
                    "Refrigera la crema 10 minutos y rellena las tartaletas con una manga.",
                    "Decora con fruta fresca."
                ),
                categoria = "Pastelería",
                descripcion = "Aprende a rellenar tartaletas de venta diaria con crema pastelera instantánea: se prepara en frío y queda siempre igual.",
                ingredientes = listOf(
                    "12 bases de tartaleta",
                    "500 ml de leche fría",
                    "150 g de Crema Pastelera",
                    "Fruta fresca para decorar (frutillas, kiwi o durazno)"
                ),
                rendimiento = "Rinde 12 tartaletas",
                dificultad = "Fácil",
                videoUrl = DatosEmpresa.URL_VIDEO_MASTERCLASS,
                imagenUrl = DatosEmpresa.URL_IMG_PASTELERIA
            ),
            Curso(
                id = 10,
                titulo = "Trufas Veganas de Cacao Intenso",
                insumo = "Ariba Zero Latto",
                chef = "Maestro Andrea Conti",
                duracion = "15 min",
                pasos = listOf(
                    "Pica el Ariba Zero Latto en trozos pequeños.",
                    "Calienta la Orasì hasta que hierva suavemente.",
                    "Vierte la Orasì sobre el chocolate, espera 1 minuto y mezcla hasta lograr una ganache lisa.",
                    "Refrigera la ganache 2 horas, hasta que esté firme.",
                    "Forma bolitas del mismo tamaño con una cuchara o con las manos frías.",
                    "Pasa cada bolita por el cacao en polvo y guárdalas en un lugar fresco."
                ),
                categoria = "Chocolatería",
                descripcion = "Aprende a formar trufas de ganache vegana del mismo tamaño y a cubrirlas con cacao para venderlas en cajas de regalo.",
                ingredientes = listOf(
                    "250 g de Ariba Zero Latto",
                    "125 ml de Orasì",
                    "60 g de cacao en polvo sin azúcar",
                    "50 g de nueces picadas para cubrir (opcional)"
                ),
                rendimiento = "Rinde 30 trufas",
                dificultad = "Fácil",
                esVegano = true,
                videoUrl = DatosEmpresa.URL_VIDEO_MASTERCLASS,
                imagenUrl = DatosEmpresa.URL_IMG_CHOCOLATERIA
            )
        )
    }

    fun obtenerCategorias(): List<String> {
        return listOf("Chocolatería", "Pastelería", "Panadería", "Heladería", "Decoración")
    }

    fun obtenerCursoPorId(id: Int): Curso? {
        return obtenerCursos().find { curso -> curso.id == id }
    }
}
