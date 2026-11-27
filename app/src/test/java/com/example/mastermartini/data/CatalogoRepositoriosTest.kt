package com.example.mastermartini.data

import com.example.mastermartini.data.remote.CursosApiService
import com.example.mastermartini.data.remote.RetrofitClient
import com.example.mastermartini.model.Curso
import com.example.mastermartini.model.Producto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import retrofit2.Response

// Recetas y productos vienen del microservicio de cursos (simulado) y se guardan en CatalogoCache
class CatalogoRepositoriosTest : StringSpec({

    val api = mockk<CursosApiService>()

    beforeTest {
        clearMocks(api)
        // Cada prueba parte sin nada en memoria
        CatalogoCache.limpiar()
        mockkObject(RetrofitClient)
        every { RetrofitClient.cursosApi } returns api
    }

    afterTest {
        unmockkObject(RetrofitClient)
        CatalogoCache.limpiar()
    }

    // ---------- CursoRepository: lista de recetas ----------

    "obtenerRecetas trae las recetas del servidor y las guarda en memoria" {
        runTest {
            coEvery { api.obtenerRecetas() } returns Response.success(recetasDePrueba)
            CursoRepository().obtenerRecetas().getOrThrow() shouldBe recetasDePrueba
            CatalogoCache.recetas shouldBe recetasDePrueba
        }
    }

    "la segunda vez usa la memoria y no vuelve a pedir; forzarRecarga sí pide de nuevo" {
        runTest {
            coEvery { api.obtenerRecetas() } returns Response.success(recetasDePrueba)
            // Otra pantalla crea su propio repositorio, pero la memoria es compartida
            CursoRepository().obtenerRecetas()
            CursoRepository().obtenerRecetas().getOrThrow() shouldBe recetasDePrueba
            coVerify(exactly = 1) { api.obtenerRecetas() }

            val nuevas = recetasDePrueba.take(2)
            coEvery { api.obtenerRecetas() } returns Response.success(nuevas)
            CursoRepository().obtenerRecetas(forzarRecarga = true).getOrThrow() shouldBe nuevas
            CatalogoCache.recetas shouldBe nuevas
            coVerify(exactly = 2) { api.obtenerRecetas() }
        }
    }

    "si el servidor falla al recargar, la memoria conserva la última lista buena" {
        runTest {
            coEvery { api.obtenerRecetas() } returns Response.success(recetasDePrueba)
            CursoRepository().obtenerRecetas()
            coEvery { api.obtenerRecetas() } returns respuestaError(500, cuerpoError("Falla interna"))

            val error = CursoRepository().obtenerRecetas(forzarRecarga = true).exceptionOrNull()

            error.shouldBeInstanceOf<ServidorException>()
            error.codigo shouldBe 500
            error.mensajeServidor shouldBe "Falla interna"
            CatalogoCache.recetas shouldBe recetasDePrueba
        }
    }

    "una lista exitosa sin cuerpo es error del servidor y no se guarda" {
        runTest {
            coEvery { api.obtenerRecetas() } returns Response.success(null)
            CursoRepository().obtenerRecetas().exceptionOrNull().shouldBeInstanceOf<ServidorException>()
            CatalogoCache.recetas.shouldBeNull()
        }
    }

    "sin conexión las recetas devuelven el mensaje compartido de conexión" {
        runTest {
            coEvery { api.obtenerRecetas() } throws IOException("sin red")
            CursoRepository().obtenerRecetas().exceptionOrNull()!!.message shouldBe MENSAJE_SIN_CONEXION
        }
    }

    "si la corrutina se cancela al pedir recetas, la cancelación se relanza" {
        runTest {
            coEvery { api.obtenerRecetas() } throws CancellationException("pantalla cerrada")
            shouldThrow<CancellationException> { CursoRepository().obtenerRecetas() }
        }
    }

    // ---------- CursoRepository: una receta ----------

    "obtenerRecetaPorId usa la receta en memoria sin pedirla al servidor" {
        runTest {
            CatalogoCache.recetas = recetasDePrueba
            CursoRepository().obtenerRecetaPorId(3).getOrThrow().titulo shouldBe "Pan de masa madre"
            coVerify(exactly = 0) { api.obtenerReceta(any()) }
        }
    }

    "si la receta no está en memoria (o se fuerza) la pide al servidor" {
        runTest {
            CatalogoCache.recetas = recetasDePrueba
            val nueva = recetasDePrueba[0].copy(id = 40, titulo = "Receta recién creada")
            coEvery { api.obtenerReceta(40) } returns Response.success(nueva)
            coEvery { api.obtenerReceta(1) } returns Response.success(recetasDePrueba[0])

            CursoRepository().obtenerRecetaPorId(40).getOrThrow() shouldBe nueva
            CursoRepository().obtenerRecetaPorId(1, forzarRecarga = true).getOrThrow().id shouldBe 1
            coVerify(exactly = 1) { api.obtenerReceta(1) }
        }
    }

    "una receta que no existe responde 404" {
        runTest {
            coEvery { api.obtenerReceta(99) } returns respuestaError(404, cuerpoError("Receta no encontrada"))
            val error = CursoRepository().obtenerRecetaPorId(99).exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.codigo shouldBe 404
            esNoEncontrado(error).shouldBeTrue()
        }
    }

    "una receta sin cuerpo, sin conexión o cancelada se maneja igual que la lista" {
        runTest {
            coEvery { api.obtenerReceta(5) } returns Response.success(null)
            CursoRepository().obtenerRecetaPorId(5).exceptionOrNull().shouldBeInstanceOf<ServidorException>()

            coEvery { api.obtenerReceta(6) } throws IOException("sin red")
            CursoRepository().obtenerRecetaPorId(6).exceptionOrNull()!!.message shouldBe MENSAJE_SIN_CONEXION

            coEvery { api.obtenerReceta(7) } throws CancellationException("pantalla cerrada")
            shouldThrow<CancellationException> { CursoRepository().obtenerRecetaPorId(7) }
        }
    }

    "las categorías salen de las recetas en el orden en que llegan, sin repetir ni vacías" {
        categoriasDeRecetas(recetasDePrueba) shouldContainExactly
            listOf("Chocolatería", "Pastelería", "Panadería", "Heladería")
        categoriasDeRecetas(listOf(recetasDePrueba[0].copy(categoria = " "))).shouldBeEmpty()
        categoriasDeRecetas(emptyList<Curso>()).shouldBeEmpty()
    }

    // ---------- ProductoRepository ----------

    "obtenerProductos trae los productos, los guarda y la segunda vez usa la memoria" {
        runTest {
            coEvery { api.obtenerProductos() } returns Response.success(productosDePrueba)
            ProductoRepository().obtenerProductos().getOrThrow() shouldBe productosDePrueba
            ProductoRepository().obtenerProductos().getOrThrow() shouldBe productosDePrueba
            CatalogoCache.productos shouldBe productosDePrueba
            coVerify(exactly = 1) { api.obtenerProductos() }

            ProductoRepository().obtenerProductos(forzarRecarga = true)
            coVerify(exactly = 2) { api.obtenerProductos() }
        }
    }

    "los productos con error, sin cuerpo, sin conexión o cancelados" {
        runTest {
            coEvery { api.obtenerProductos() } returns respuestaError(503, cuerpoError("En mantención"))
            val error = ProductoRepository().obtenerProductos().exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.mensajeServidor shouldBe "En mantención"

            coEvery { api.obtenerProductos() } returns Response.success(null)
            ProductoRepository().obtenerProductos().exceptionOrNull().shouldBeInstanceOf<ServidorException>()

            coEvery { api.obtenerProductos() } throws IOException("sin red")
            ProductoRepository().obtenerProductos().exceptionOrNull()!!.message shouldBe MENSAJE_SIN_CONEXION

            coEvery { api.obtenerProductos() } throws CancellationException("pantalla cerrada")
            shouldThrow<CancellationException> { ProductoRepository().obtenerProductos() }
            CatalogoCache.productos.shouldBeNull()
        }
    }

    "obtenerProductoPorId usa la memoria y, si no está o se fuerza, pide al servidor" {
        runTest {
            CatalogoCache.productos = productosDePrueba
            ProductoRepository().obtenerProductoPorId(2).getOrThrow().nombre shouldBe "Crema Vegetal Nube"
            coVerify(exactly = 0) { api.obtenerProducto(any()) }

            coEvery { api.obtenerProducto(2) } returns Response.success(productosDePrueba[1])
            ProductoRepository().obtenerProductoPorId(2, forzarRecarga = true).getOrThrow().id shouldBe 2
            coVerify(exactly = 1) { api.obtenerProducto(2) }
        }
    }

    "un producto que no existe responde 404; sin cuerpo, sin conexión o cancelado se manejan igual" {
        runTest {
            coEvery { api.obtenerProducto(99) } returns respuestaError(404, cuerpoError("Producto no encontrado"))
            esNoEncontrado(ProductoRepository().obtenerProductoPorId(99).exceptionOrNull()!!).shouldBeTrue()

            coEvery { api.obtenerProducto(98) } returns Response.success(null)
            ProductoRepository().obtenerProductoPorId(98).exceptionOrNull().shouldBeInstanceOf<ServidorException>()

            coEvery { api.obtenerProducto(97) } throws IOException("sin red")
            ProductoRepository().obtenerProductoPorId(97).exceptionOrNull()!!.message shouldBe MENSAJE_SIN_CONEXION

            coEvery { api.obtenerProducto(96) } throws CancellationException("pantalla cerrada")
            shouldThrow<CancellationException> { ProductoRepository().obtenerProductoPorId(96) }
        }
    }

    "obtenerProductoPorNombre busca el nombre exacto y devuelve null si no hay ninguno" {
        runTest {
            CatalogoCache.productos = productosDePrueba
            val repo = ProductoRepository()
            repo.obtenerProductoPorNombre("Harina Molino Sur").getOrThrow()!!.id shouldBe 3
            repo.obtenerProductoPorNombre("Producto inventado").getOrThrow().shouldBeNull()
        }
    }

    "obtenerProductoPorNombre devuelve el error si no se pudieron traer los productos" {
        runTest {
            coEvery { api.obtenerProductos() } throws IOException("sin red")
            ProductoRepository().obtenerProductoPorNombre("Harina Molino Sur").isFailure.shouldBeTrue()
        }
    }

    "las líneas salen de los productos en el orden en que llegan, sin repetir ni vacías" {
        lineasDeProductos(productosDePrueba) shouldContainExactly
            listOf("Chocolates y coberturas", "Cremas vegetales", "Harinas", "Mantecas")
        lineasDeProductos(listOf(productosDePrueba[0].copy(linea = ""))).shouldBeEmpty()
        lineasDeProductos(emptyList<Producto>()).shouldBeEmpty()
    }

    "recetasQueUsan encuentra el producto como insumo o como ingrediente" {
        val cobertura = productosDePrueba[0]
        recetasQueUsan(cobertura, recetasDePrueba).map { it.id } shouldContainExactly listOf(1, 6)
        recetasQueUsan(productosDePrueba[3], recetasDePrueba).shouldBeEmpty()
    }

    // ---------- Apoyo ----------

    "textoDeError usa el mensaje del servidor, el de la excepción o el de sin conexión" {
        textoDeError(ServidorException(500, "Falla interna")) shouldBe "Falla interna"
        textoDeError(ServidorException(503, null)) shouldBe "Error 503: verifica permisos o campos"
        textoDeError(Exception("Sin datos")) shouldBe "Sin datos"
        textoDeError(Exception()) shouldBe MENSAJE_SIN_CONEXION
        esNoEncontrado(ServidorException(500, null)).shouldBeFalse()
        esNoEncontrado(Exception()).shouldBeFalse()
    }

    "CatalogoCache.limpiar olvida las recetas y los productos" {
        CatalogoCache.recetas = recetasDePrueba
        CatalogoCache.productos = productosDePrueba
        CatalogoCache.limpiar()
        CatalogoCache.recetas.shouldBeNull()
        CatalogoCache.productos.shouldBeNull()
    }
})
