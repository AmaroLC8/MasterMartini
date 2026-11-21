package com.example.mastermartini.data

import com.example.mastermartini.model.Curso
import com.example.mastermartini.model.Producto

// REPOSITORIO DE PRODUCTOS
// Es la única parte de la app que sabe de dónde salen los productos Master Martini.
// Igual que CursoRepository, hoy los datos están escritos aquí con listOf().
// Las descripciones son las del catálogo de la marca; los usos son ejemplos coherentes con ellas.
// IMPORTANTE: los nombres deben ser IGUALES a los de CursoRepository (con mayúsculas y tildes),
// porque obtenerRecetasQueUsan() los busca dentro del insumo y los ingredientes de cada receta.
class ProductoRepository {

    // Para buscar las recetas que usan cada producto
    private val cursoRepository = CursoRepository()

    // Devuelve todos los productos, ordenados por línea
    fun obtenerProductos(): List<Producto> {
        return listOf(
            // --- MARGARINAS ---
            Producto(
                id = 1,
                nombre = "Gran Master Hoja",
                linea = "Margarinas",
                descripcion = "Margarina italiana de alto rendimiento para masas de hoja y danesas: croissants, palmeras y vol au vent.",
                usos = listOf("Croissants y masas danesas", "Palmeras", "Vol au vent")
            ),
            Producto(
                id = 2,
                nombre = "Master Export Horneo",
                linea = "Margarinas",
                descripcion = "Margarina para horneo de masas dulces y saladas.",
                usos = listOf("Brioche y panes dulces", "Queques y masas dulces", "Masas saladas horneadas")
            ),

            // --- MANTECAS ---
            Producto(
                id = 3,
                nombre = "Manteca Unigral",
                linea = "Mantecas",
                descripcion = "Grasa vegetal que reemplaza total o parcialmente la grasa habitual, sin grasas trans, y alarga la vida útil del producto.",
                usos = listOf(
                    "Reemplazar la grasa habitual en panes y masas",
                    "Productos sin grasas trans",
                    "Alargar la vida útil de lo que se vende"
                )
            ),

            // --- CREMAS VEGETALES ---
            Producto(
                id = 4,
                nombre = "Bravo Crem",
                linea = "Cremas vegetales",
                descripcion = "Crema vegetal azucarada, lista para batir.",
                usos = listOf("Crema batida para decorar tortas", "Rellenos batidos")
            ),
            Producto(
                id = 5,
                nombre = "Decor Up",
                linea = "Cremas vegetales",
                descripcion = "Crema vegetal azucarada para rellenos y pastelería tradicional.",
                usos = listOf("Rellenos de tortas", "Ganache y pastelería tradicional")
            ),
            Producto(
                id = 6,
                nombre = "Monnalisa",
                linea = "Cremas vegetales",
                descripcion = "Crema vegetal para decoraciones de alta precisión y coberturas.",
                usos = listOf("Decoraciones con manga de alta precisión", "Coberturas de tortas")
            ),
            Producto(
                id = 7,
                nombre = "Decor Art",
                linea = "Cremas vegetales",
                descripcion = "Crema vegetal azucarada para rellenos y decoraciones tradicionales.",
                usos = listOf("Rellenos de tortas", "Decoraciones tradicionales")
            ),
            Producto(
                id = 8,
                nombre = "Orasì",
                linea = "Cremas vegetales",
                descripcion = "Crema vegetal sabor vainilla para pastelería vegana.",
                usos = listOf("Mousses veganas", "Ganache vegana", "Rellenos sin leche"),
                esVegano = true
            ),

            // --- CHOCOLATES Y COBERTURAS ---
            Producto(
                id = 9,
                nombre = "Chocolate Ariba",
                linea = "Chocolates y coberturas",
                descripcion = "Chocolate puro con cacao de Costa de Marfil, en discos, cacao en polvo y gotas.",
                usos = listOf("Bombones templados", "Ganache y rellenos", "Repostería con gotas o cacao en polvo")
            ),
            Producto(
                id = 10,
                nombre = "Ariba Zero Latto",
                linea = "Chocolates y coberturas",
                descripcion = "Chocolate amargo 60% vegano, sin leche, soya ni gluten.",
                usos = listOf("Postres veganos", "Sorbetes sin leche", "Trufas y ganache vegana"),
                esVegano = true
            ),
            Producto(
                id = 11,
                nombre = "Cobertura Caravella",
                linea = "Chocolates y coberturas",
                descripcion = "Sucedáneo de chocolate para bañar, cubrir y rellenar; no necesita templado.",
                usos = listOf("Bañar galletas y frutas", "Cubrir tortas y glaseados", "Rellenar")
            ),
            Producto(
                id = 12,
                nombre = "Coberturas de color Caravella",
                linea = "Chocolates y coberturas",
                descripcion = "Coberturas de color con colorantes y saborizantes naturales, para decorar, rellenar y cubrir.",
                usos = listOf("Decorar tortas y galletas", "Bañar con colores", "Rellenar")
            ),

            // --- PASTELERÍA INSTANTÁNEA (mezclas en polvo) ---
            Producto(
                id = 13,
                nombre = "Crema Pastelera",
                linea = "Pastelería instantánea",
                descripcion = "Mezcla en polvo lista para usar.",
                usos = listOf("Rellenar tartaletas", "Rellenar berlines y pasteles")
            ),
            Producto(
                id = 14,
                nombre = "Crema Chantilly",
                linea = "Pastelería instantánea",
                descripcion = "Mezcla en polvo lista para usar.",
                usos = listOf("Decorar postres", "Rellenar tortas")
            ),
            Producto(
                id = 15,
                nombre = "Merengue",
                linea = "Pastelería instantánea",
                descripcion = "Mezcla en polvo lista para usar.",
                usos = listOf("Decorar tortas", "Suspiros horneados")
            ),
            Producto(
                id = 16,
                nombre = "Polvo de Hornear",
                linea = "Pastelería instantánea",
                descripcion = "Mezcla en polvo lista para usar.",
                usos = listOf("Queques y bizcochos", "Galletas")
            ),
            Producto(
                id = 17,
                nombre = "Azúcar Glass",
                linea = "Pastelería instantánea",
                descripcion = "Mezcla en polvo lista para usar.",
                usos = listOf("Espolvorear panes y postres", "Glaseados")
            ),

            // --- FRITURA ---
            Producto(
                id = 18,
                nombre = "Vita Frit",
                linea = "Fritura",
                descripcion = "Aceite para freír.",
                usos = listOf("Sopaipillas y churros", "Empanadas fritas")
            )
        )
    }

    // Las líneas de productos, en el orden en que se muestran
    fun obtenerLineas(): List<String> {
        return listOf(
            "Margarinas",
            "Mantecas",
            "Cremas vegetales",
            "Chocolates y coberturas",
            "Pastelería instantánea",
            "Fritura"
        )
    }

    // Busca UN producto por su id (lo usa la pantalla de detalle del producto).
    // Devuelve null si no existe, y la pantalla dice "Producto no encontrado".
    fun obtenerProductoPorId(id: Int): Producto? {
        return obtenerProductos().find { producto -> producto.id == id }
    }

    // Busca UN producto por su nombre exacto (lo usa el Detalle de una receta:
    // con el insumo "Chocolate Ariba" encuentra el producto para abrirlo).
    // Devuelve null si ningún producto se llama así.
    fun obtenerProductoPorNombre(nombre: String): Producto? {
        return obtenerProductos().find { producto -> producto.nombre == nombre }
    }

    // Recetas que usan el producto: las que lo tienen como insumo principal
    // o lo nombran en alguno de sus ingredientes.
    // contains revisa si el nombre aparece DENTRO del texto:
    // "600 g de Chocolate Ariba amargo".contains("Chocolate Ariba") -> true.
    // Distingue mayúsculas y tildes: por eso los nombres deben ser iguales en los dos repositorios.
    fun obtenerRecetasQueUsan(producto: Producto): List<Curso> {
        return cursoRepository.obtenerCursos().filter { curso ->
            curso.insumo.contains(producto.nombre) ||
                curso.ingredientes.any { ingrediente -> ingrediente.contains(producto.nombre) }
        }
    }
}
