package com.example.mastermartini.ui.screens

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.mastermartini.R

@OptIn(UnstableApi::class)
@Composable
fun ReproductorVideoLocal(
    videoUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Si hay URL de Cloudinary la usa; si no, usa el video local de res/raw
    val uriVideo = remember(videoUrl) {
        if (!videoUrl.isNullOrBlank()) {
            Uri.parse(videoUrl)
        } else {
            Uri.parse("android.resource://${context.packageName}/${R.raw.video_receta_master}")
        }
    }

    val exoPlayer = remember(uriVideo) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uriVideo))
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
            playWhenReady = false // No arranca solo con sonido hasta que el usuario toque Play
        }
    }

    // Libera el reproductor al salir de la pantalla para no consumir memoria
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(12.dp)),
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
            }
        }
    )
}