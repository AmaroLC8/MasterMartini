package com.example.mastermartini.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mastermartini.R

@Composable
fun ReproductorVideoLocal(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp)),
        factory = { context ->
            VideoView(context).apply {
                // Ruta canónica recomendada por Android Developers para recursos en res/raw
                val rutaUri = Uri.parse("android.resource://${context.packageName}/${R.raw.video_receta_master}")
                setVideoURI(rutaUri)

                val controlador = MediaController(context)
                controlador.setAnchorView(this)
                setMediaController(controlador)

                // Espera a que el emulador decodifique el video antes de interactuar
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = false
                    seekTo(100) // Muestra el primer fotograma sin reproducir de golpe
                }

                setOnErrorListener { _, what, extra ->
                    // Evita que la app se caiga si el emulador no soporta el formato
                    true
                }
            }
        }
    )
}