package com.example.mastermartini.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

// URL de respaldo (Cloudinary / CDN) por si un curso aún tiene videoUrl en null
const val VIDEO_DEFAULT_ONLINE =
    "https://res.cloudinary.com/demo/video/upload/elephants.mp4"

@Composable
fun ReproductorVideoOnline(
    urlVideo: String?,
    modifier: Modifier = Modifier
) {
    val urlFinal = if (!urlVideo.isNullOrBlank()) urlVideo.trim() else VIDEO_DEFAULT_ONLINE
    var cargando by remember(urlFinal) { mutableStateOf(true) }
    var hayError by remember(urlFinal) { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF14181F))
    ) {
        if (hayError) {
            Text(
                text = "⚠️ No se pudo reproducir el video.\nVerifica que sea un enlace directo .mp4 (ej. Cloudinary) y tengas internet.",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    VideoView(context).apply {
                        val controlador = MediaController(context)
                        controlador.setAnchorView(this)
                        setMediaController(controlador)

                        setOnPreparedListener { mediaPlayer ->
                            cargando = false
                            mediaPlayer.isLooping = true
                            start()
                        }

                        setOnErrorListener { _, _, _ ->
                            cargando = false
                            hayError = true
                            true
                        }
                    }
                },
                update = { videoView ->
                    val tagActual = videoView.tag as? String
                    if (tagActual != urlFinal) {
                        videoView.tag = urlFinal
                        cargando = true
                        hayError = false
                        videoView.setVideoURI(Uri.parse(urlFinal))
                    }
                }
            )

            if (cargando) {
                CircularProgressIndicator(color = Color(0xFF008C45))
            }
        }
    }
}