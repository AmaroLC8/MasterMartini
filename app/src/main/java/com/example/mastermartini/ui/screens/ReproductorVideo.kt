package com.example.mastermartini.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.mastermartini.ui.theme.BlancoPuro
import com.example.mastermartini.ui.theme.DoradoCalidad
import com.example.mastermartini.ui.theme.TextoOscuro

@OptIn(UnstableApi::class)
@Composable
fun ReproductorVideoLocal(
    videoUrl: String? = null,
    modifier: Modifier = Modifier,
    permitirPantallaCompleta: Boolean = true,
    reproducirAlCargar: Boolean = false,
    repetirEnBucle: Boolean = true,
    silenciadoInicial: Boolean = false,
    velocidadInicial: Float = 1.0f,
    saltoSegundos: Long = 10L,
    modoAjusteInicial: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT,
    mostrarControles: Boolean = true,
    altoReproductor: Dp = 210.dp
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
                    text = "Video en la nube próximamente disponible",
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
    val incrementoSaltoMs = (saltoSegundos.coerceAtLeast(1L)) * 1000L

    val exoPlayer = remember(uriVideo) {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(incrementoSaltoMs)
            .setSeekForwardIncrementMs(incrementoSaltoMs)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(uriVideo))
                repeatMode = if (repetirEnBucle) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                volume = if (silenciadoInicial) 0f else 1f
                setPlaybackSpeed(velocidadInicial.coerceIn(0.25f, 2.0f))
                prepare()
                playWhenReady = reproducirAlCargar
            }
    }

    LaunchedEffect(exoPlayer, repetirEnBucle) {
        exoPlayer.repeatMode = if (repetirEnBucle) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    var enPantallaCompleta by remember { mutableStateOf(false) }
    var modoAjusteActual by remember(modoAjusteInicial) { mutableIntStateOf(modoAjusteInicial) }
    val activity = remember(context) { context.encontrarActivity() }

    // Al entrar en pantalla completa gira a horizontal y, al salir o cerrar la pantalla,
    // restaura la orientación automática del sistema.
    DisposableEffect(enPantallaCompleta, activity) {
        if (enPantallaCompleta) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        onDispose {
            if (enPantallaCompleta) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    if (!enPantallaCompleta) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(altoReproductor)
                .clip(RoundedCornerShape(12.dp)),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = mostrarControles
                    resizeMode = modoAjusteActual
                    setShowFastForwardButton(true)
                    setShowRewindButton(true)
                    if (permitirPantallaCompleta) {
                        setFullscreenButtonClickListener {
                            enPantallaCompleta = true
                        }
                    }
                }
            },
            update = { playerView ->
                if (playerView.player !== exoPlayer) {
                    playerView.player = exoPlayer
                }
                playerView.useController = mostrarControles
                playerView.resizeMode = modoAjusteActual
            },
            onRelease = { playerView ->
                playerView.player = null
            }
        )
    }

    if (enPantallaCompleta) {
        Dialog(
            onDismissRequest = { enPantallaCompleta = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            val vistaDialogo = LocalView.current
            DisposableEffect(vistaDialogo) {
                val ventanaDialogo = (vistaDialogo.parent as? DialogWindowProvider)?.window
                val controlador = ventanaDialogo?.let {
                    WindowInsetsControllerCompat(it, vistaDialogo)
                }
                controlador?.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controlador?.hide(WindowInsetsCompat.Type.systemBars())
                onDispose {
                    controlador?.show(WindowInsetsCompat.Type.systemBars())
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = mostrarControles
                            resizeMode = modoAjusteActual
                            setShowFastForwardButton(true)
                            setShowRewindButton(true)
                            setFullscreenButtonClickListener {
                                enPantallaCompleta = false
                            }
                        }
                    },
                    update = { playerView ->
                        if (playerView.player !== exoPlayer) {
                            playerView.player = exoPlayer
                        }
                        playerView.useController = mostrarControles
                        playerView.resizeMode = modoAjusteActual
                    },
                    onRelease = { playerView ->
                        playerView.player = null
                    }
                )

                // Botones rápidos en la esquina superior derecha para cambiar el ajuste de pantalla
                // (Ajustar / Zoom / Rellenar) y salir de pantalla completa.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = {
                            modoAjusteActual = when (modoAjusteActual) {
                                AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AspectRatio,
                            contentDescription = "Cambiar ajuste de video",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { enPantallaCompleta = false },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FullscreenExit,
                            contentDescription = "Salir de pantalla completa",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

private tailrec fun Context.encontrarActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.encontrarActivity()
    else -> null
}
