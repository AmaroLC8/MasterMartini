package com.example.mastermartini.ui.screens

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.TextoOscuro

@OptIn(UnstableApi::class)
@Composable
fun ReproductorVideoLocal(
    videoUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (videoUrl.isNullOrBlank()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BlancoPuro),
            border = BorderStroke(1.dp, DoradoCalidad.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp),
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(16.dp)
            ) {
                Text(
                    text = "🎥 Video en la nube próximamente disponible",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoOscuro,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val uriVideo = remember(videoUrl) { Uri.parse(videoUrl) }

    val exoPlayer = remember(uriVideo) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uriVideo))
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
            playWhenReady = false
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
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