package ru.raskraska.kids

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Фоновая музыка. Играет по кругу, пока приложение на экране; при сворачивании
 * (и когда открывается реклама на весь экран) — на паузе. Включена/выключена — запоминается.
 */
object Music {
    private const val PREFS = "settings"
    private const val KEY = "music_on"
    private const val VOLUME = 0.35f

    private var player: MediaPlayer? = null
    private var resumed = false

    /** Включена ли музыка (кнопка 🔊/🔇). */
    var enabled by mutableStateOf(true)
        private set

    fun init(context: Context) {
        enabled = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, true)
    }

    /** Вызывается из onResume активити. */
    fun onResume(context: Context) {
        resumed = true
        if (enabled) play(context)
    }

    /** Вызывается из onPause активити. */
    fun onPause() {
        resumed = false
        player?.takeIf { it.isPlaying }?.pause()
    }

    fun toggle(context: Context) {
        enabled = !enabled
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY, enabled).apply()
        if (enabled && resumed) play(context) else player?.takeIf { it.isPlaying }?.pause()
    }

    private fun play(context: Context) {
        val p = player ?: try {
            MediaPlayer.create(context.applicationContext, R.raw.music)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                setVolume(VOLUME, VOLUME)
            }
        } catch (e: Exception) {
            null
        }
        player = p
        if (p != null && !p.isPlaying) p.start()
    }
}

/** Круглая кнопка включения/выключения музыки. */
@Composable
fun MusicButton(modifier: Modifier = Modifier, size: Dp = 52.dp) {
    val context = LocalContext.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, label = "press")
    Box(
        modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF1EBFF))))
            .border(3.dp, Color.White, CircleShape)
            .clickable(interactionSource = interaction, indication = null) { Music.toggle(context) },
        contentAlignment = Alignment.Center
    ) {
        Text(if (Music.enabled) "🔊" else "🔇", fontSize = (size.value * 0.45f).sp)
    }
}
