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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin

/** Цвет рамок и кнопок на экране рисования — свой у каждой темы. */
val LocalAccent = compositionLocalOf { Violet }

/** Что рисуется на фоне. */
enum class Motif { MEADOW, SEA, SPACE, MAGIC, SKY, SWEETS, FESTIVE, TECH, JUNGLE, LETTERS }

class SceneStyle(
    val top: Color,
    val bottom: Color,
    val accent: Color,
    val motif: Motif,
)

/** Фон экрана рисования для каждой темы. */
fun sceneFor(themeId: String?): SceneStyle = when (themeId) {
    "animals" -> SceneStyle(Color(0xFFE6FFD0), Color(0xFF8EDB5E), Color(0xFF43A047), Motif.MEADOW)
    "farm" -> SceneStyle(Color(0xFFB8E7FF), Color(0xFF9BDC6A), Color(0xFF7CB342), Motif.SKY)
    "sea" -> SceneStyle(Color(0xFF7FDBFF), Color(0xFF1E88E5), Color(0xFF0288D1), Motif.SEA)
    "dinosaurs" -> SceneStyle(Color(0xFFFFF3C4), Color(0xFF7CC444), Color(0xFF558B2F), Motif.JUNGLE)
    "transport" -> SceneStyle(Color(0xFF8FD8FF), Color(0xFFE3F6FF), Color(0xFFE53935), Motif.SKY)
    "space" -> SceneStyle(Color(0xFF1A1F71), Color(0xFF7B2FBE), Color(0xFF7C4DFF), Motif.SPACE)
    "fairy" -> SceneStyle(Color(0xFFFFD6EC), Color(0xFFD7A6F5), Color(0xFFEC407A), Motif.MAGIC)
    "unicorns" -> SceneStyle(Color(0xFFC9B6FF), Color(0xFFA7D8FF), Color(0xFF9C5BF0), Motif.MAGIC)
    "food" -> SceneStyle(Color(0xFFFFF0D9), Color(0xFFFF9E80), Color(0xFFFF7043), Motif.SWEETS)
    "nature" -> SceneStyle(Color(0xFFEFFFE0), Color(0xFF9EE07A), Color(0xFF43A047), Motif.MEADOW)
    "holidays" -> SceneStyle(Color(0xFFFFE3E8), Color(0xFFFF8A9A), Color(0xFFE53935), Motif.FESTIVE)
    "robots" -> SceneStyle(Color(0xFFD7F7FF), Color(0xFF6FC9E8), Color(0xFF00ACC1), Motif.TECH)
    "insects" -> SceneStyle(Color(0xFFF4FFD9), Color(0xFFB5E36B), Color(0xFF7CB342), Motif.MEADOW)
    "letters" -> SceneStyle(Color(0xFFFFFBD0), Color(0xFFFFC46B), Color(0xFFFF9800), Motif.LETTERS)
    else -> SceneStyle(Color(0xFFDFF3FF), Color(0xFFFFE1F0), Violet, Motif.MAGIC)
}

private fun frac(x: Float) = x - floor(x)

/** Яркий тематический фон с покачивающимися украшениями. */
@Composable
fun ThemeSceneBackground(style: SceneStyle, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "scene")
    val time by t.animateFloat(
        0f, 1f, infiniteRepeatable(tween(8000, easing = LinearEasing)), label = "time"
    )
    Canvas(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(style.top, style.bottom)))
    ) {
        // мягкое свечение в центре — фон кажется ярче и «объёмнее»
        drawCircle(
            Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
                center = Offset(size.width / 2, size.height * 0.45f),
                radius = size.maxDimension * 0.5f
            ),
            radius = size.maxDimension * 0.5f,
            center = Offset(size.width / 2, size.height * 0.45f)
        )
        drawMotif(style.motif, time)
    }
}

private fun DrawScope.drawMotif(motif: Motif, time: Float) {
    val w = size.width
    val h = size.height
    val u = min(w, h)
    val tau = 2f * PI.toFloat()
    fun bob(i: Int) = sin((time * 2f + i * 0.21f) * tau) * u * 0.008f

    // Псевдослучайные, но всегда одинаковые места для украшений
    val spots = List(26) { i ->
        Offset(frac(i * 0.618f + 0.05f) * w, frac(i * 0.381f + 0.11f) * h)
    }

    when (motif) {
        Motif.MEADOW, Motif.JUNGLE -> {
            if (motif == Motif.MEADOW) glossySun(Offset(w * 0.88f, h * 0.1f), u * 0.09f)
            else glossySun(Offset(w * 0.9f, h * 0.1f), u * 0.08f, Color(0xFFFFB300))
            spots.forEachIndexed { i, p ->
                val q = p + Offset(0f, bob(i))
                when (i % 4) {
                    0, 1 -> glossyLeaf(q, u * (0.045f + 0.02f * (i % 3)), i * 47f + time * 20f,
                        if (motif == Motif.JUNGLE) Color(0xFF2E7D32) else Color(0xFF66BB6A))
                    2 -> daisy(q, u * 0.03f)
                    else -> glossyBall(q, u * 0.012f, Color(0xFFFFEB3B))
                }
            }
            hills(Color(0xFF7CCB4C), Color(0xFF5DB637))
        }
        Motif.SKY -> {
            glossySun(Offset(w * 0.88f, h * 0.12f), u * 0.09f)
            listOf(0.12f to 0.15f, 0.45f to 0.08f, 0.7f to 0.25f, 0.2f to 0.55f, 0.85f to 0.6f).forEachIndexed { i, (x, y) ->
                softCloud(Offset(w * x + bob(i) * 3f, h * y), u * 0.06f, 0.95f)
            }
            spots.take(10).forEachIndexed { i, p -> daisy(p.copy(y = h * (0.82f + 0.15f * frac(i * 0.37f))), u * 0.025f) }
            hills(Color(0xFF9BDC6A), Color(0xFF7CC94A))
        }
        Motif.SEA -> {
            // лучи света сверху
            for (i in 0 until 5) {
                val x = w * (0.1f + i * 0.2f)
                drawPath(Path().apply {
                    moveTo(x - u * 0.03f, 0f); lineTo(x + u * 0.03f, 0f)
                    lineTo(x + u * 0.15f, h); lineTo(x - u * 0.05f, h); close()
                }, Color.White.copy(alpha = 0.10f))
            }
            spots.forEachIndexed { i, p ->
                val rise = frac(time + i * 0.137f) * h
                when (i % 3) {
                    0, 1 -> bubble(Offset(p.x + bob(i) * 3f, h - rise), u * (0.012f + 0.012f * (i % 4)))
                    else -> fish(p + Offset(bob(i) * 4f, 0f), u * 0.05f,
                        listOf(Color(0xFFFF8A3D), Color(0xFFFFD54F), Color(0xFFFF5C8A))[i % 3], i % 2 == 0)
                }
            }
            seaweedRow()
        }
        Motif.SPACE -> {
            spots.forEachIndexed { i, p ->
                val tw = 0.5f + 0.5f * sin((time * 3f + i * 0.3f) * tau)
                if (i % 5 == 0) glossyStar(p, u * 0.03f, Color(0xFFFFD54F), i * 20f)
                else sparkle(p, u * (0.008f + 0.012f * tw))
            }
            planet(Offset(w * 0.1f, h * 0.82f + bob(1)), u * 0.08f, Color(0xFFFF8A65))
            planet(Offset(w * 0.9f, h * 0.15f + bob(2)), u * 0.06f, Color(0xFF4FC3F7))
            glossyBall(Offset(w * 0.75f, h * 0.88f + bob(3)), u * 0.025f, Color(0xFFE040FB))
            moon(Offset(w * 0.08f, h * 0.12f), u * 0.06f)
        }
        Motif.MAGIC -> {
            rainbowCorner(Offset(w * 0.08f, h * 0.9f), u * 0.18f)
            softCloud(Offset(w * 0.08f, h * 0.92f), u * 0.05f, 1f)
            softCloud(Offset(w * 0.92f, h * 0.95f), u * 0.06f, 1f)
            spots.forEachIndexed { i, p ->
                val q = p + Offset(0f, bob(i))
                when (i % 4) {
                    0 -> glossyStar(q, u * 0.03f, Color(0xFFFFD54F), i * 15f)
                    1 -> glossyHeart(q, u * 0.025f, Color(0xFFFF4F9A))
                    2 -> sparkle(q, u * 0.022f)
                    else -> glossyBall(q, u * 0.012f, Color(0xFFB388FF))
                }
            }
        }
        Motif.SWEETS -> {
            spots.forEachIndexed { i, p ->
                val q = p + Offset(0f, bob(i))
                when (i % 4) {
                    0 -> candy(q, u * 0.035f, listOf(Color(0xFFFF5C8A), Color(0xFF42A5F5), Color(0xFF66BB6A))[i % 3], i * 30f)
                    1 -> glossyHeart(q, u * 0.022f, Color(0xFFFF4F9A))
                    2 -> sprinkle(q, u * 0.03f, listOf(Color(0xFFFFEB3B), Color(0xFF7E57C2), Color(0xFF26C6DA))[i % 3], i * 40f)
                    else -> glossyBall(q, u * 0.016f, Color(0xFFFFB74D))
                }
            }
        }
        Motif.FESTIVE -> {
            garland()
            spots.forEachIndexed { i, p ->
                val fall = frac(time * 0.5f + i * 0.13f) * h
                val q = Offset(p.x + bob(i) * 3f, (p.y + fall) % h)
                when (i % 3) {
                    0 -> snowflake(q, u * 0.022f)
                    1 -> sprinkle(q, u * 0.025f, listOf(Color(0xFFFFD54F), Color(0xFF66BB6A), Color(0xFF42A5F5))[i % 3], i * 50f + time * 360f)
                    else -> glossyStar(q, u * 0.02f, Color(0xFFFFD54F), i * 25f)
                }
            }
        }
        Motif.TECH -> {
            spots.forEachIndexed { i, p ->
                when (i % 3) {
                    0 -> gear(p, u * 0.045f, Color(0xFF90A4AE), time * 360f * (if (i % 2 == 0) 1 else -1))
                    1 -> glossyBall(p + Offset(0f, bob(i)), u * 0.014f, Color(0xFF26C6DA))
                    else -> sparkle(p, u * 0.02f)
                }
            }
        }
        Motif.LETTERS -> {
            glossySun(Offset(w * 0.9f, h * 0.1f), u * 0.08f)
            val cols = listOf(Color(0xFFF44336), Color(0xFF42A5F5), Color(0xFF66BB6A), Color(0xFFAB47BC), Color(0xFFFF7043))
            spots.forEachIndexed { i, p ->
                val q = p + Offset(0f, bob(i))
                when (i % 3) {
                    0 -> glossyBall(q, u * 0.018f, cols[i % cols.size])
                    1 -> sprinkle(q, u * 0.03f, cols[(i + 2) % cols.size], i * 33f)
                    else -> glossyStar(q, u * 0.022f, Color(0xFFFFD54F), i * 12f)
                }
            }
        }
    }
}

// ---------------- Элементы ----------------

private fun DrawScope.hills(front: Color, back: Color) {
    val w = size.width
    val h = size.height
    drawPath(Path().apply {
        moveTo(0f, h * 0.9f)
        quadraticTo(w * 0.25f, h * 0.8f, w * 0.5f, h * 0.88f)
        quadraticTo(w * 0.8f, h * 0.96f, w, h * 0.84f)
        lineTo(w, h); lineTo(0f, h); close()
    }, back.copy(alpha = 0.6f))
    drawPath(Path().apply {
        moveTo(0f, h * 0.95f)
        quadraticTo(w * 0.35f, h * 0.88f, w * 0.65f, h * 0.95f)
        quadraticTo(w * 0.85f, h * 0.99f, w, h * 0.93f)
        lineTo(w, h); lineTo(0f, h); close()
    }, front)
}

internal fun DrawScope.glossySun(c: Offset, r: Float, color: Color = Color(0xFFFFE04A)) {
    for (i in 0 until 12) {
        val a = i * 30f * PI.toFloat() / 180f
        drawLine(
            color.copy(alpha = 0.8f),
            Offset(c.x + cos(a) * r * 1.2f, c.y + sin(a) * r * 1.2f),
            Offset(c.x + cos(a) * r * 1.55f, c.y + sin(a) * r * 1.55f),
            strokeWidth = r * 0.16f, cap = StrokeCap.Round
        )
    }
    glossyBall(c, r, color)
    // мордочка
    drawCircle(Color(0xFF5D4037), r * 0.1f, Offset(c.x - r * 0.32f, c.y - r * 0.05f))
    drawCircle(Color(0xFF5D4037), r * 0.1f, Offset(c.x + r * 0.32f, c.y - r * 0.05f))
    drawArc(
        Color(0xFF5D4037), 20f, 140f, false,
        Offset(c.x - r * 0.3f, c.y - r * 0.05f), Size(r * 0.6f, r * 0.45f),
        style = Stroke(r * 0.08f, cap = StrokeCap.Round)
    )
    drawCircle(Color(0xFFFF8A80).copy(alpha = 0.7f), r * 0.13f, Offset(c.x - r * 0.55f, c.y + r * 0.2f))
    drawCircle(Color(0xFFFF8A80).copy(alpha = 0.7f), r * 0.13f, Offset(c.x + r * 0.55f, c.y + r * 0.2f))
}

internal fun DrawScope.glossyLeaf(c: Offset, s: Float, rot: Float, color: Color) {
    rotate(rot, c) {
        val p = Path().apply {
            moveTo(c.x - s, c.y)
            quadraticTo(c.x, c.y - s * 0.75f, c.x + s, c.y)
            quadraticTo(c.x, c.y + s * 0.75f, c.x - s, c.y)
            close()
        }
        drawPath(p, Brush.verticalGradient(
            listOf(lerp(color, Color.White, 0.35f), color, lerp(color, Color.Black, 0.2f)),
            startY = c.y - s * 0.5f, endY = c.y + s * 0.5f
        ))
        drawLine(lerp(color, Color.Black, 0.25f), Offset(c.x - s * 0.8f, c.y), Offset(c.x + s * 0.8f, c.y), s * 0.06f)
        drawOval(Color.White.copy(alpha = 0.5f), Offset(c.x - s * 0.5f, c.y - s * 0.3f), Size(s * 0.5f, s * 0.12f))
    }
}

internal fun DrawScope.daisy(c: Offset, r: Float) {
    for (i in 0 until 6) {
        val a = i * 60f * PI.toFloat() / 180f
        drawCircle(Color.White, r * 0.5f, Offset(c.x + cos(a) * r * 0.62f, c.y + sin(a) * r * 0.62f))
    }
    drawCircle(Color(0xFFFFC107), r * 0.38f, c)
}

private fun DrawScope.bubble(c: Offset, r: Float) {
    drawCircle(Color.White.copy(alpha = 0.18f), r, c)
    drawCircle(Color.White.copy(alpha = 0.7f), r, c, style = Stroke(r * 0.12f))
    drawCircle(Color.White.copy(alpha = 0.85f), r * 0.22f, Offset(c.x - r * 0.4f, c.y - r * 0.4f))
}

private fun DrawScope.fish(c: Offset, s: Float, color: Color, left: Boolean) {
    val d = if (left) -1f else 1f
    val tail = Path().apply {
        moveTo(c.x - d * s * 0.8f, c.y)
        lineTo(c.x - d * s * 1.35f, c.y - s * 0.45f)
        lineTo(c.x - d * s * 1.35f, c.y + s * 0.45f)
        close()
    }
    drawPath(tail, lerp(color, Color.Black, 0.1f))
    drawOval(
        Brush.verticalGradient(listOf(lerp(color, Color.White, 0.4f), color), startY = c.y - s * 0.5f, endY = c.y + s * 0.5f),
        Offset(c.x - s, c.y - s * 0.55f), Size(s * 2f, s * 1.1f)
    )
    drawCircle(Color.White, s * 0.18f, Offset(c.x + d * s * 0.5f, c.y - s * 0.1f))
    drawCircle(Color(0xFF263238), s * 0.1f, Offset(c.x + d * s * 0.55f, c.y - s * 0.1f))
}

private fun DrawScope.seaweedRow() {
    val w = size.width
    val h = size.height
    val u = min(w, h)
    for (i in 0 until 9) {
        val x = w * (i + 0.5f) / 9f
        val hh = u * (0.12f + 0.08f * frac(i * 0.37f))
        drawPath(Path().apply {
            moveTo(x - u * 0.015f, h)
            quadraticTo(x - u * 0.05f, h - hh * 0.5f, x, h - hh)
            quadraticTo(x + u * 0.03f, h - hh * 0.5f, x + u * 0.015f, h)
            close()
        }, if (i % 2 == 0) Color(0xFF26A69A) else Color(0xFF43A047))
    }
    drawPath(Path().apply {
        moveTo(0f, h * 0.97f); quadraticTo(w * 0.5f, h * 0.92f, w, h * 0.97f); lineTo(w, h); lineTo(0f, h); close()
    }, Color(0xFFFFE0A3))
}

private fun DrawScope.planet(c: Offset, r: Float, color: Color) {
    glossyBall(c, r, color)
    rotate(-20f, c) {
        drawOval(
            Color.White.copy(alpha = 0.8f), Offset(c.x - r * 1.6f, c.y - r * 0.3f), Size(r * 3.2f, r * 0.6f),
            style = Stroke(r * 0.12f)
        )
    }
}

private fun DrawScope.moon(c: Offset, r: Float) {
    drawCircle(Color(0xFFFFF59D), r, c)
    drawCircle(Color(0xFF1A1F71), r * 0.85f, Offset(c.x + r * 0.45f, c.y - r * 0.2f))
}

private fun DrawScope.rainbowCorner(c: Offset, r: Float) {
    val cols = listOf(Color(0xFFF44336), Color(0xFFFF9800), Color(0xFFFFEB3B), Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFF9C27B0))
    val band = r * 0.12f
    cols.forEachIndexed { i, col ->
        val rr = r - band * i
        drawArc(col.copy(alpha = 0.85f), 180f, 180f, false, Offset(c.x - rr, c.y - rr), Size(rr * 2, rr * 2), style = Stroke(band))
    }
}

private fun DrawScope.candy(c: Offset, s: Float, color: Color, rot: Float) {
    rotate(rot, c) {
        drawPath(Path().apply {
            moveTo(c.x - s * 0.7f, c.y); lineTo(c.x - s * 1.2f, c.y - s * 0.45f); lineTo(c.x - s * 1.2f, c.y + s * 0.45f); close()
        }, lerp(color, Color.White, 0.3f))
        drawPath(Path().apply {
            moveTo(c.x + s * 0.7f, c.y); lineTo(c.x + s * 1.2f, c.y - s * 0.45f); lineTo(c.x + s * 1.2f, c.y + s * 0.45f); close()
        }, lerp(color, Color.White, 0.3f))
        glossyBall(c, s * 0.7f, color)
    }
}

private fun DrawScope.sprinkle(c: Offset, s: Float, color: Color, rot: Float) {
    rotate(rot, c) {
        drawLine(color, Offset(c.x - s * 0.5f, c.y), Offset(c.x + s * 0.5f, c.y), s * 0.3f, cap = StrokeCap.Round)
    }
}

private fun DrawScope.snowflake(c: Offset, r: Float) {
    for (i in 0 until 3) {
        val a = i * 60f * PI.toFloat() / 180f
        drawLine(
            Color.White, Offset(c.x - cos(a) * r, c.y - sin(a) * r), Offset(c.x + cos(a) * r, c.y + sin(a) * r),
            r * 0.18f, cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.gear(c: Offset, r: Float, color: Color, rot: Float) {
    rotate(rot, c) {
        for (i in 0 until 8) {
            val a = i * 45f * PI.toFloat() / 180f
            drawLine(color, c, Offset(c.x + cos(a) * r, c.y + sin(a) * r), r * 0.35f, cap = StrokeCap.Butt)
        }
        drawCircle(color, r * 0.75f, c)
        drawCircle(lerp(color, Color.White, 0.6f), r * 0.32f, c)
    }
}

private fun DrawScope.garland() {
    val w = size.width
    val cols = listOf(Color(0xFFF44336), Color(0xFFFFC107), Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFE040FB))
    val n = 14
    for (i in 0 until n) {
        val x = w * (i + 0.5f) / n
        val y = size.height * 0.03f + sin(i / (n - 1f) * PI.toFloat()) * size.height * 0.04f
        val r = min(w, size.height) * 0.022f
        drawPath(Path().apply { moveTo(x - r, y); lineTo(x + r, y); lineTo(x, y + r * 2f); close() }, cols[i % cols.size])
    }
}
