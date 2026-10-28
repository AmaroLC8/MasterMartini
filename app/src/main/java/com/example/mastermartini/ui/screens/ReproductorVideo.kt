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
                // Ruta canónica del video alojado en res/raw/video_receta_master.mp4
                val rutaUri = Uri.parse("android.resource://${context.packageName}/${R.raw.video_receta_master}")
                setVideoURI(rutaUri)

                val controlador = MediaController(context)
                controlador.setAnchorView(this)
                setMediaController(controlador)

                // En emuladores como BlueStacks, dar start() directo al estar preparado fuerza la decodificación visual
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    start()
                }

                setOnErrorListener { _, _, _ ->
                    true
                }
            }
        }
    )
}
