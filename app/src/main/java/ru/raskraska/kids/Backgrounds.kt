package ru.raskraska.kids

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/** Радужный фон главного экрана. */
val RainbowBackground = listOf(
    Color(0xFF7C4DFF), Color(0xFFE040FB), Color(0xFFFF4F9A), Color(0xFFFF9E40),
)

/** Яркий фон экрана темы — переливы цвета самой темы. */
fun themeBackground(theme: ColoringTheme): List<Color> = listOf(
    lerp(theme.color, Color(0xFF311B92), 0.25f),
    theme.color,
    lerp(theme.color, Color(0xFFFF4081), 0.35f),
    lerp(theme.color, Color(0xFFFFD740), 0.35f),
)

/**
 * Яркий градиент, по которому медленно плывут вверх пузырьки, звёздочки и сердечки.
 */
@Composable
fun BrightBackground(colors: List<Color>, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "bg")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(24000, easing = LinearEasing)),
        label = "time"
    )
    Canvas(
        modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors))
    ) {
        drawFloaters(time)
    }
}

private fun frac(x: Float) = x - floor(x)

private fun DrawScope.drawFloaters(time: Float) {
    val w = size.width
    val h = size.height
    val unit = density
    for (i in 0 until 26) {
        val speed = 1f + (i % 3)
        val x = frac(i * 0.618f + 0.07f) * w + sin((time * 3f + i * 0.3f) * 2f * PI.toFloat()) * 18f * unit
        val y = h + 40f * unit - frac(time * speed + i * 0.137f) * (h + 80f * unit)
        val r = (6f + frac(i * 0.29f) * 22f) * unit
        when (i % 4) {
            0, 1 -> {
                drawCircle(Color.White.copy(alpha = 0.18f), r, Offset(x, y))
                drawCircle(Color.White.copy(alpha = 0.45f), r * 0.28f, Offset(x - r * 0.35f, y - r * 0.35f))
            }
            2 -> drawStar(Offset(x, y), r * 0.9f, Color.White.copy(alpha = 0.35f), time * 360f + i * 40f)
            else -> drawHeart(Offset(x, y), r * 0.8f, Color.White.copy(alpha = 0.28f))
        }
    }
}

private fun DrawScope.drawStar(c: Offset, r: Float, color: Color, rotation: Float) {
    val p = Path()
    for (k in 0 until 10) {
        val a = (rotation - 90f + k * 36f) * PI.toFloat() / 180f
        val rr = if (k % 2 == 0) r else r * 0.45f
        val x = c.x + rr * cos(a)
        val y = c.y + rr * sin(a)
        if (k == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close()
    drawPath(p, color)
}

private fun DrawScope.drawHeart(c: Offset, s: Float, color: Color) {
    val p = Path().apply {
        moveTo(c.x, c.y + s * 0.9f)
        cubicTo(c.x - s * 1.5f, c.y - s * 0.1f, c.x - s * 0.8f, c.y - s * 1.2f, c.x, c.y - s * 0.45f)
        cubicTo(c.x + s * 0.8f, c.y - s * 1.2f, c.x + s * 1.5f, c.y - s * 0.1f, c.x, c.y + s * 0.9f)
        close()
    }
    drawPath(p, color)
}
