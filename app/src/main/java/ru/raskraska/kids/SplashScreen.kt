package ru.raskraska.kids

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin

val SplashTop = Color(0xFFCFE9FF)
private val SplashBottom = Color(0xFFFFD6E8)
private val Rainbow = listOf(
    Color(0xFFF44336), Color(0xFFFF9800), Color(0xFFFFEB3B),
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFF9C27B0),
)
private val LetterColors = listOf(
    Color(0xFFF44336), Color(0xFFFF9800), Color(0xFFFFC107), Color(0xFF4CAF50),
    Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFFE91E63),
)
/** Пружинка: буква слегка «перелетает» и возвращается. */
private val BackOut = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

/**
 * Анимированная заставка: рисуется радуга, подпрыгивают разноцветные буквы,
 * вверх плывут пузырьки. Длится ~2.8 с, по нажатию пропускается.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val finish by rememberUpdatedState(onFinished)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 2800, easing = LinearEasing))
        finish()
    }
    val infinite = rememberInfiniteTransition(label = "splash")
    val time by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "time"
    )
    val p = progress.value

    val screenWidth = LocalConfiguration.current.screenWidthDp
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SplashTop, SplashBottom)))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { finish() }
    ) {
        // На узком (вертикальном) экране буквы чуть меньше
        val k = (screenWidth / 600f).coerceIn(0.55f, 1f)
        Canvas(Modifier.fillMaxSize()) { drawSplashBackground(p, time) }
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BouncyWord("Весёлые", p, start = 0.18f, fontSize = 52.sp * k, colorShift = 0, time = time)
            BouncyWord("раскраски", p, start = 0.36f, fontSize = 64.sp * k, colorShift = 3, time = time)
            Spacer(Modifier.height(10.dp))
            val sub = ((p - 0.68f) / 0.18f).coerceIn(0f, 1f)
            Text(
                text = "Раскрашивай и фантазируй!",
                color = Violet,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer {
                    alpha = sub
                    translationY = (1f - sub) * 40f
                }
            )
        }
    }
}

@Composable
private fun BouncyWord(
    word: String,
    progress: Float,
    start: Float,
    fontSize: TextUnit,
    colorShift: Int,
    time: Float,
) {
    Row {
        word.forEachIndexed { i, ch ->
            val local = ((progress - start - i * 0.03f) / 0.2f).coerceIn(0f, 1f)
            val s = BackOut.transform(local)
            val bob = sin((time * 4f + i * 0.15f) * 2f * PI.toFloat()) * 6f
            Box(
                Modifier.graphicsLayer {
                    scaleX = s
                    scaleY = s
                    alpha = if (local > 0f) 1f else 0f
                    rotationZ = (1f - local) * -25f
                    translationY = (1f - local) * -120f + if (local >= 1f) bob else 0f
                }
            ) {
                // Белая обводка под цветной буквой — «наклейка»
                Text(
                    text = ch.toString(),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    style = TextStyle(drawStyle = Stroke(width = 18f, join = StrokeJoin.Round))
                )
                Text(
                    text = ch.toString(),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Black,
                    color = LetterColors[(i + colorShift) % LetterColors.size],
                    style = TextStyle(shadow = Shadow(Color(0x40000000), Offset(0f, 6f), 6f))
                )
            }
        }
    }
}

private fun frac(x: Float) = x - floor(x)

private fun DrawScope.drawSplashBackground(progress: Float, time: Float) {
    val w = size.width
    val h = size.height

    // Пузырьки и конфетти плывут вверх
    for (i in 0 until 22) {
        val speed = 0.6f + frac(i * 0.37f) * 0.8f
        val x = frac(i * 0.618f + 0.1f) * w + sin((time * 2f + i) * 2f * PI.toFloat()) * 12f
        val y = h + 60f - frac(time * speed + i * 0.137f) * (h + 120f)
        val r = (8f + frac(i * 0.29f) * 18f) * density / 2f
        drawCircle(LetterColors[i % LetterColors.size].copy(alpha = 0.35f), r, Offset(x, y))
        drawCircle(Color.White.copy(alpha = 0.6f), r * 0.3f, Offset(x - r * 0.35f, y - r * 0.35f))
    }

    // Радуга «рисуется» слева направо
    val sweep = 180f * BackOut.transform((progress / 0.5f).coerceIn(0f, 1f)).coerceAtMost(1f)
    val cx = w / 2f
    val cy = h * 0.95f
    val outer = min(w * 0.46f, h * 0.88f)
    val band = outer * 0.075f
    if (sweep > 0.5f) {
        Rainbow.forEachIndexed { i, color ->
            val r = outer - band * i - band / 2f
            drawArc(
                color = color.copy(alpha = 0.8f),
                startAngle = 180f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(cx - r, cy - r),
                size = Size(r * 2f, r * 2f),
                style = Stroke(width = band, cap = StrokeCap.Butt)
            )
        }
    }
    // Облачка на концах радуги
    val mid = outer - band * 3f
    val cloudAlpha = ((progress - 0.1f) / 0.3f).coerceIn(0f, 1f)
    for (side in listOf(-1f, 1f)) {
        val x = cx + side * mid
        val c = Color.White.copy(alpha = cloudAlpha)
        drawCircle(c, band * 2.2f, Offset(x, cy - band * 0.5f))
        drawCircle(c, band * 1.7f, Offset(x - band * 2.3f, cy + band * 0.6f))
        drawCircle(c, band * 1.8f, Offset(x + band * 2.3f, cy + band * 0.5f))
    }

    // Мерцающие звёздочки
    for (i in 0 until 9) {
        val x = frac(i * 0.43f + 0.05f) * w
        val y = frac(i * 0.71f + 0.12f) * h * 0.6f
        val tw = (sin((time * 3f + i * 0.33f) * 2f * PI.toFloat()) + 1f) / 2f
        val r = (5f + tw * 6f) * density / 2f
        drawCircle(Color.White.copy(alpha = 0.4f + tw * 0.6f), r, Offset(x, y))
    }
}
