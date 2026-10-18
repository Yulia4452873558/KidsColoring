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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Круглый детский шрифт Nunito (лицензия OFL — можно использовать в приложении). */
val Nunito = FontFamily(
    Font(R.font.nunito_black, FontWeight.Black),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
)

private val SkyTop = Color(0xFF4FC3F7)
private val SkyBottom = Color(0xFFB3E5FC)
private val RainbowBands = listOf(
    Color(0xFFF4363A), Color(0xFFFF8A1F), Color(0xFFFFD32A),
    Color(0xFF3BC54A), Color(0xFF2E8BF0), Color(0xFF8E3BE0),
)
private val Line1 = listOf(
    Color(0xFFF4363A), Color(0xFFFF7A1A), Color(0xFFFFB300), Color(0xFF2EB84A),
    Color(0xFF2E8BF0), Color(0xFF8E3BE0), Color(0xFFF0388C),
)
private val Line2 = listOf(
    Color(0xFF2EB84A), Color(0xFF2E8BF0), Color(0xFF8E3BE0), Color(0xFFF0388C), Color(0xFFFF7A1A),
    Color(0xFFFFB300), Color(0xFF2EB84A), Color(0xFF2E8BF0), Color(0xFF8E3BE0),
)
private val SubtitleColor = Color(0xFF6A1FB0)

/** Пружинка: буква слегка «перелетает» и возвращается. */
private val BackOut = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

/**
 * Заставка: голубое небо, большая радуга, облака, блестящие звёзды, сердечки и шарики.
 * Буквы названия впрыгивают по очереди. Длится ~3 с, по нажатию пропускается.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val finish by rememberUpdatedState(onFinished)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 3000, easing = LinearEasing))
        finish()
    }
    val infinite = rememberInfiniteTransition(label = "splash")
    val time by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "time"
    )
    val p = progress.value

    // Размер надписи подстраивается под экран (и горизонтальный, и вертикальный)
    val conf = LocalConfiguration.current
    val k = min(conf.screenWidthDp / 820f, conf.screenHeightDp / 400f).coerceIn(0.42f, 1.4f)

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyTop, SkyBottom)))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { finish() }
    ) {
        Canvas(Modifier.fillMaxSize()) { drawScene(p, time) }
        Column(
            Modifier
                .align(Alignment.Center)
                .offset(y = (-14 * k).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BouncyWord("Весёлые", Line1, p, start = 0.15f, fontSize = 86.sp * k, time = time)
            BouncyWord("раскраски", Line2, p, start = 0.33f, fontSize = 96.sp * k, time = time)
            Spacer(Modifier.height((10 * k).dp))
            val sub = ((p - 0.62f) / 0.18f).coerceIn(0f, 1f)
            val pill = RoundedCornerShape(50)
            Box(
                Modifier
                    .graphicsLayer {
                        alpha = sub
                        val s = 0.6f + 0.4f * BackOut.transform(sub)
                        scaleX = s
                        scaleY = s
                    }
                    .shadow((6 * k).dp, pill)
                    .background(Color.White, pill)
                    .padding(horizontal = (28 * k).dp, vertical = (9 * k).dp)
            ) {
                Text(
                    text = "Раскрашивай и фантазируй!",
                    color = SubtitleColor,
                    fontSize = 26.sp * k,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun BouncyWord(
    word: String,
    colors: List<Color>,
    progress: Float,
    start: Float,
    fontSize: TextUnit,
    time: Float,
) {
    val outline = with(androidx.compose.ui.platform.LocalDensity.current) { (fontSize.toPx() * 0.17f) }
    Row {
        word.forEachIndexed { i, ch ->
            val local = ((progress - start - i * 0.028f) / 0.2f).coerceIn(0f, 1f)
            val s = BackOut.transform(local)
            val bob = sin((time + i * 0.11f) * 2f * PI.toFloat()) * 4f
            val tilt = if (i % 2 == 0) -4f else 4f
            val color = colors[i % colors.size]
            Box(
                Modifier.graphicsLayer {
                    scaleX = s
                    scaleY = s
                    alpha = if (local > 0f) 1f else 0f
                    rotationZ = tilt + (1f - local) * -30f
                    translationY = (1f - local) * -160f + if (local >= 1f) bob else 0f
                }
            ) {
                val base = TextStyle(
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Black,
                    fontSize = fontSize,
                    lineHeight = fontSize * 1.08f,
                )
                // 1) белая «наклейка» с мягкой тенью
                Text(
                    ch.toString(),
                    style = base.copy(
                        color = Color.White,
                        drawStyle = Stroke(width = outline, join = StrokeJoin.Round),
                        shadow = Shadow(Color(0x55203060), Offset(0f, outline * 0.45f), outline * 0.6f)
                    )
                )
                // 2) тёмная кромка, чтобы буква выглядела объёмной
                Text(
                    ch.toString(),
                    style = base.copy(
                        color = lerp(color, Color.Black, 0.25f),
                        drawStyle = Stroke(width = outline * 0.22f, join = StrokeJoin.Round)
                    )
                )
                // 3) сама буква с глянцевым переливом
                Text(
                    ch.toString(),
                    style = base.copy(
                        brush = Brush.verticalGradient(
                            0f to lerp(color, Color.White, 0.45f),
                            0.45f to color,
                            1f to lerp(color, Color.Black, 0.18f),
                        )
                    )
                )
            }
        }
    }
}

// ---------------- Фон ----------------

private fun DrawScope.drawScene(progress: Float, time: Float) {
    val w = size.width
    val h = size.height
    val tau = 2f * PI.toFloat()

    // мягкие облачка в верхних углах
    softCloud(Offset(w * 0.06f, h * 0.10f), h * 0.12f, 0.55f)
    softCloud(Offset(w * 0.95f, h * 0.06f), h * 0.10f, 0.5f)

    // радуга
    val cx = w / 2f
    val cy = h * 1.0f
    val outer = min(w * 0.47f, h * 0.97f)
    val band = outer * 0.088f
    val sweep = 180f * ((progress / 0.42f).coerceIn(0f, 1f)).let { 1f - (1f - it) * (1f - it) }
    val inner = outer - band * RainbowBands.size
    if (sweep > 0.5f) {
        // нежная розовая середина
        drawArc(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFFE3F3), Color(0xFFF8C8EE), Color(0xFFD9C2FF)),
                center = Offset(cx, cy), radius = inner
            ),
            startAngle = 180f, sweepAngle = sweep, useCenter = true,
            topLeft = Offset(cx - inner, cy - inner), size = Size(inner * 2, inner * 2)
        )
        RainbowBands.forEachIndexed { i, c ->
            val r = outer - band * i - band / 2f
            val tl = Offset(cx - r, cy - r)
            val sz = Size(r * 2, r * 2)
            drawArc(c, 180f, sweep, false, tl, sz, style = Stroke(band + 1f, cap = StrokeCap.Butt))
            // блик по верхней кромке полосы
            val rh = r - band * 0.28f
            drawArc(
                Color.White.copy(alpha = 0.28f), 180f, sweep, false,
                Offset(cx - rh, cy - rh), Size(rh * 2, rh * 2),
                style = Stroke(band * 0.18f, cap = StrokeCap.Round)
            )
        }
    }

    // облака внизу поднимаются
    val rise = (1f - BackOut.transform((progress / 0.35f).coerceIn(0f, 1f))) * h * 0.3f
    cloudBank(w, h, rise)

    // блестящие украшения
    val pop = ((progress - 0.25f) / 0.3f).coerceIn(0f, 1f)
    if (pop > 0f) {
        val sc = BackOut.transform(pop)
        val u = min(w, h)
        fun bob(i: Int) = sin((time + i * 0.17f) * tau) * u * 0.012f
        glossyHeart(Offset(w * 0.07f, h * 0.20f + bob(0)), u * 0.085f * sc, Color(0xFFFF3D8B))
        glossyStar(Offset(w * 0.84f, h * 0.08f + bob(1)), u * 0.075f * sc, Color(0xFFFFC928), -12f)
        glossyStar(Offset(w * 0.33f, h * 0.92f + bob(2)), u * 0.05f * sc, Color(0xFFFFC928), 10f)
        glossyBall(Offset(w * 0.955f, h * 0.17f + bob(3)), u * 0.035f * sc, Color(0xFF7E57C2))
        glossyBall(Offset(w * 0.91f, h * 0.27f + bob(4)), u * 0.022f * sc, Color(0xFFFFC928))
        glossyBall(Offset(w * 0.97f, h * 0.61f + bob(5)), u * 0.032f * sc, Color(0xFFFF4FA3))
        glossyBall(Offset(w * 0.46f, h * 0.95f + bob(6)), u * 0.03f * sc, Color(0xFFE040FB))
        glossyBall(Offset(w * 0.04f, h * 0.55f + bob(7)), u * 0.025f * sc, Color(0xFF29B6F6))
        // мерцающие искорки
        val tw = { i: Int -> 0.55f + 0.45f * sin((time * 2f + i * 0.3f) * tau) }
        sparkle(Offset(w * 0.17f, h * 0.06f), u * 0.035f * sc * tw(0))
        sparkle(Offset(w * 0.05f, h * 0.37f), u * 0.03f * sc * tw(1))
        sparkle(Offset(w * 0.73f, h * 0.03f), u * 0.025f * sc * tw(2))
        sparkle(Offset(w * 0.94f, h * 0.36f), u * 0.04f * sc * tw(3))
        sparkle(Offset(w * 0.62f, h * 0.90f), u * 0.025f * sc * tw(4))
    }
}

internal fun DrawScope.softCloud(c: Offset, r: Float, alpha: Float) {
    val col = Color.White.copy(alpha = alpha)
    drawCircle(col, r, c)
    drawCircle(col, r * 0.8f, Offset(c.x - r * 1.0f, c.y + r * 0.3f))
    drawCircle(col, r * 0.85f, Offset(c.x + r * 1.0f, c.y + r * 0.25f))
}

private fun DrawScope.cloudBank(w: Float, h: Float, rise: Float) {
    val shade = Color(0xFFD3E9FA)
    val puffs = listOf(
        0.00f to 0.17f, 0.10f to 0.13f, 0.19f to 0.11f, 0.30f to 0.08f,
        0.72f to 0.08f, 0.80f to 0.12f, 0.90f to 0.15f, 1.00f to 0.18f,
    )
    val u = min(w, h) * 1.2f
    for ((fx, fr) in puffs) {
        val r = fr * u
        drawCircle(shade, r, Offset(fx * w + r * 0.12f, h + rise - r * 0.25f))
    }
    for ((fx, fr) in puffs) {
        val r = fr * u
        drawCircle(Color.White, r, Offset(fx * w, h + rise - r * 0.35f))
    }
    // «пушистая» нижняя кромка во всю ширину
    drawRect(Color.White, Offset(0f, h + rise - u * 0.05f), Size(w, u * 0.2f))
}

internal fun DrawScope.glossyBall(c: Offset, r: Float, color: Color) {
    if (r <= 0f) return
    drawCircle(
        Brush.radialGradient(
            listOf(lerp(color, Color.White, 0.55f), color, lerp(color, Color.Black, 0.25f)),
            center = Offset(c.x - r * 0.35f, c.y - r * 0.35f), radius = r * 1.6f
        ), r, c
    )
    drawCircle(Color.White.copy(alpha = 0.85f), r * 0.28f, Offset(c.x - r * 0.38f, c.y - r * 0.4f))
}

internal fun DrawScope.glossyHeart(c: Offset, s: Float, color: Color) {
    if (s <= 0f) return
    val p = Path().apply {
        moveTo(c.x, c.y + s * 0.85f)
        cubicTo(c.x - s * 1.45f, c.y - s * 0.05f, c.x - s * 0.8f, c.y - s * 1.15f, c.x, c.y - s * 0.4f)
        cubicTo(c.x + s * 0.8f, c.y - s * 1.15f, c.x + s * 1.45f, c.y - s * 0.05f, c.x, c.y + s * 0.85f)
        close()
    }
    drawPath(p, Color.White, style = Stroke(s * 0.12f, join = StrokeJoin.Round))
    drawPath(
        p, Brush.verticalGradient(
            listOf(lerp(color, Color.White, 0.4f), color, lerp(color, Color.Black, 0.2f)),
            startY = c.y - s, endY = c.y + s
        )
    )
    drawOval(
        Color.White.copy(alpha = 0.8f),
        Offset(c.x - s * 0.72f, c.y - s * 0.62f), Size(s * 0.38f, s * 0.24f)
    )
}

internal fun DrawScope.glossyStar(c: Offset, r: Float, color: Color, rot: Float) {
    if (r <= 0f) return
    val p = Path()
    for (i in 0 until 10) {
        val a = (rot - 90f + i * 36f) * PI.toFloat() / 180f
        val rr = if (i % 2 == 0) r else r * 0.5f
        val x = c.x + rr * cos(a)
        val y = c.y + rr * sin(a)
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close()
    drawPath(p, Color.White, style = Stroke(r * 0.14f, join = StrokeJoin.Round))
    drawPath(
        p, Brush.verticalGradient(
            listOf(lerp(color, Color.White, 0.5f), color, lerp(color, Color(0xFFFF8F00), 0.5f)),
            startY = c.y - r, endY = c.y + r
        )
    )
    rotate(rot, c) {
        drawOval(Color.White.copy(alpha = 0.8f), Offset(c.x - r * 0.32f, c.y - r * 0.42f), Size(r * 0.28f, r * 0.2f))
    }
}

internal fun DrawScope.sparkle(c: Offset, r: Float) {
    if (r <= 0f) return
    val p = Path().apply {
        moveTo(c.x, c.y - r)
        quadraticTo(c.x + r * 0.15f, c.y - r * 0.15f, c.x + r, c.y)
        quadraticTo(c.x + r * 0.15f, c.y + r * 0.15f, c.x, c.y + r)
        quadraticTo(c.x - r * 0.15f, c.y + r * 0.15f, c.x - r, c.y)
        quadraticTo(c.x - r * 0.15f, c.y - r * 0.15f, c.x, c.y - r)
        close()
    }
    drawPath(p, Color.White)
}
