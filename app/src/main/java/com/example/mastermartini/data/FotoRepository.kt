package com.example.mastermartini.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

// REPOSITORIO DE LA FOTO DEL LOCAL
// Es la única parte de la app que sabe DÓNDE y CÓMO se guarda la foto.
// - La imagen se guarda como archivo JPEG en la memoria interna de la app (context.filesDir):
//   es privada, ninguna otra app la puede ver, y se borra si se desinstala la app.
// - La RUTA de ese archivo se anota en SharedPreferences (con AlmacenamientoLocal),
//   igual que los demás datos del perfil.
// Una foto NO se guarda directo en SharedPreferences porque ese archivo es para datos
// chicos (textos, números); una imagen pesa cientos de KB.
//
// Todas las funciones públicas son "suspend" y usan withContext(Dispatchers.IO):
// leer y escribir archivos es lento, y si se hiciera en el hilo principal
// la pantalla se congelaría mientras tanto.
class FotoRepository(private val context: Context) {

    // Para guardar y leer la ruta del archivo en SharedPreferences
    private val almacenamiento = AlmacenamientoLocal(context)

    // El archivo donde vive la foto. Siempre es el mismo: una foto nueva reemplaza a la anterior.
    private val archivoFoto = File(context.filesDir, "foto_local.jpg")

    // Tamaño máximo (en píxeles) del lado más largo de la foto guardada.
    // Una foto de la galería puede medir 4000 px; para un círculo de 90 dp sobra con 800.
    // Así el archivo pesa poco y la app no se queda sin memoria.
    private val ladoMaximo = 800

    // 1. GUARDAR LA FOTO TOMADA CON LA CÁMARA (llega como Bitmap)
    // Devuelve la foto reducida que se guardó, o null si algo falló (la app no se cae).
    suspend fun guardarFoto(bitmap: Bitmap): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val reducida = reducir(bitmap)
            escribirArchivo(reducida)
            reducida
        } catch (e: Exception) {
            null
        }
    }

    // 2. GUARDAR UNA FOTO ELEGIDA DE LA GALERÍA O DE ARCHIVOS (llega como Uri)
    // Un Uri es una "dirección" a la imagen (ej. content://media/...), no la imagen en sí:
    // hay que leerla con el contentResolver.
    // Devuelve la foto reducida que se guardó, o null si no se pudo leer.
    suspend fun guardarFotoDesdeUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val original = leerDesdeUri(uri)
            if (original == null) {
                // El archivo no era una imagen o no se pudo abrir
                null
            } else {
                // Primero se endereza (por si la foto venía "acostada") y después se reduce
                val enderezada = girar(original, leerGradosDeGiro(uri))
                val reducida = reducir(enderezada)
                escribirArchivo(reducida)
                reducida
            }
        } catch (e: Exception) {
            // Ej: SecurityException si ya no tenemos acceso a ese Uri
            null
        }
    }

    // 3. LEER LA FOTO GUARDADA (al abrir la app)
    // Devuelve null si nunca se guardó una foto o si el archivo ya no existe.
    suspend fun obtenerFoto(): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val ruta = almacenamiento.obtenerRutaFoto()
            val archivo = File(ruta)
            if (ruta.isNotEmpty() && archivo.exists()) {
                // decodeFile convierte el archivo JPEG otra vez en un Bitmap para mostrarlo
                BitmapFactory.decodeFile(ruta)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // --- FUNCIONES PRIVADAS DE APOYO ---

    // Escribe el Bitmap como JPEG en filesDir y anota la ruta en SharedPreferences.
    // use { } cierra el archivo al terminar, aunque ocurra un error.
    // 85 = calidad del JPEG (de 0 a 100): se ve bien y pesa bastante menos que 100.
    private fun escribirArchivo(bitmap: Bitmap) {
        FileOutputStream(archivoFoto).use { salida ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, salida)
        }
        almacenamiento.guardarRutaFoto(archivoFoto.absolutePath)
    }

    // Achica la foto para que su lado mayor mida como máximo 800 px, SIN deformarla:
    // el ancho y el alto se multiplican por la misma escala.
    // Ej: 1000 x 750 -> escala 0,8 -> 800 x 600.
    // Si ya es chica (como la de TakePicturePreview), se devuelve igual.
    private fun reducir(bitmap: Bitmap): Bitmap {
        val ladoMayor = maxOf(bitmap.width, bitmap.height)
        if (ladoMayor <= ladoMaximo) {
            return bitmap
        }
        val escala = ladoMaximo.toFloat() / ladoMayor
        val ancho = (bitmap.width * escala).toInt()
        val alto = (bitmap.height * escala).toInt()
        // true = suavizado, para que la foto achicada no se vea pixelada
        return Bitmap.createScaledBitmap(bitmap, ancho, alto, true)
    }

    // Lee la imagen de un Uri en DOS pasadas, para no cargar en memoria una foto gigante:
    // 1ª pasada: solo pregunta las medidas (inJustDecodeBounds = true no carga los píxeles).
    // 2ª pasada: la carga ya achicada con inSampleSize (2 = la mitad, 4 = un cuarto...).
    private fun leerDesdeUri(uri: Uri): Bitmap? {
        val resolver = context.contentResolver

        // 1ª pasada: medidas
        val soloMedidas = BitmapFactory.Options()
        soloMedidas.inJustDecodeBounds = true
        resolver.openInputStream(uri)?.use { entrada ->
            BitmapFactory.decodeStream(entrada, null, soloMedidas)
        }

        // Se va dividiendo por 2 mientras el lado mayor siga quedando sobre 800 px.
        // Ej: 4000 px -> muestra 4 -> se carga de 1000 px, y después reducir() la deja en 800.
        val ladoMayor = maxOf(soloMedidas.outWidth, soloMedidas.outHeight)
        var muestra = 1
        while (ladoMayor / (muestra * 2) >= ladoMaximo) {
            muestra *= 2
        }

        // 2ª pasada: la imagen achicada. Si el Uri no se puede abrir, devuelve null.
        val conMuestra = BitmapFactory.Options()
        conMuestra.inSampleSize = muestra
        return resolver.openInputStream(uri)?.use { entrada ->
            BitmapFactory.decodeStream(entrada, null, conMuestra)
        }
    }

    // Muchas cámaras guardan la foto "acostada" y anotan aparte (en los datos EXIF del JPEG)
    // cuántos grados hay que girarla. BitmapFactory no lee ese dato, así que se lee aquí.
    // Si no hay dato (ej. un PNG), se asume que no hay que girar (0 grados).
    private fun leerGradosDeGiro(uri: Uri): Int {
        val orientacion = context.contentResolver.openInputStream(uri)?.use { entrada ->
            ExifInterface(entrada).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL

        return when (orientacion) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    // Gira el Bitmap los grados indicados. Matrix es la "instrucción" de giro que usa Android.
    private fun girar(bitmap: Bitmap, grados: Int): Bitmap {
        if (grados == 0) {
            return bitmap
        }
        val matriz = Matrix()
        matriz.postRotate(grados.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
    }
}
