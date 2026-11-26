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

// Repositorio para guardar, redimensionar y leer la foto del local en almacenamiento interno
class FotoRepository(private val context: Context) {

    private val almacenamiento = AlmacenamientoLocal(context)
    private val archivoFoto = File(context.filesDir, "foto_local.jpg")
    private val ladoMaximo = 800

    // Reduce y guarda la foto capturada por cámara en almacenamiento interno
    suspend fun guardarFoto(bitmap: Bitmap): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val reducida = reducir(bitmap)
            escribirArchivo(reducida)
            reducida
        } catch (e: Exception) {
            null
        }
    }

    // Lee la imagen desde Uri, corrige su orientación EXIF, la reduce y la guarda
    suspend fun guardarFotoDesdeUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val original = leerDesdeUri(uri)
            if (original == null) {
                null
            } else {
                val enderezada = girar(original, leerGradosDeGiro(uri))
                val reducida = reducir(enderezada)
                escribirArchivo(reducida)
                reducida
            }
        } catch (e: Exception) {
            null
        }
    }

    // Carga el archivo JPEG guardado previamente desde la ruta registrada
    suspend fun obtenerFoto(): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val ruta = almacenamiento.obtenerRutaFoto()
            val archivo = File(ruta)
            if (ruta.isNotEmpty() && archivo.exists()) {
                BitmapFactory.decodeFile(ruta)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun escribirArchivo(bitmap: Bitmap) {
        FileOutputStream(archivoFoto).use { salida ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, salida)
        }
        almacenamiento.guardarRutaFoto(archivoFoto.absolutePath)
    }

    private fun reducir(bitmap: Bitmap): Bitmap {
        val ladoMayor = maxOf(bitmap.width, bitmap.height)
        if (ladoMayor <= ladoMaximo) {
            return bitmap
        }
        val escala = ladoMaximo.toFloat() / ladoMayor
        val ancho = (bitmap.width * escala).toInt()
        val alto = (bitmap.height * escala).toInt()
        return Bitmap.createScaledBitmap(bitmap, ancho, alto, true)
    }

    // Decodifica la imagen en dos pasadas con inSampleSize para evitar OutOfMemoryError
    private fun leerDesdeUri(uri: Uri): Bitmap? {
        val resolver = context.contentResolver

        val soloMedidas = BitmapFactory.Options()
        soloMedidas.inJustDecodeBounds = true
        resolver.openInputStream(uri)?.use { entrada ->
            BitmapFactory.decodeStream(entrada, null, soloMedidas)
        }

        val ladoMayor = maxOf(soloMedidas.outWidth, soloMedidas.outHeight)
        var muestra = 1
        while (ladoMayor / (muestra * 2) >= ladoMaximo) {
            muestra *= 2
        }

        val conMuestra = BitmapFactory.Options()
        conMuestra.inSampleSize = muestra
        return resolver.openInputStream(uri)?.use { entrada ->
            BitmapFactory.decodeStream(entrada, null, conMuestra)
        }
    }

    // Obtiene los grados de rotación desde los metadatos EXIF de la imagen
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

    private fun girar(bitmap: Bitmap, grados: Int): Bitmap {
        if (grados == 0) {
            return bitmap
        }
        val matriz = Matrix()
        matriz.postRotate(grados.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
    }
}
