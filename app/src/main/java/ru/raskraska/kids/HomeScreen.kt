package ru.raskraska.kids

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/** Высота места под рекламный баннер внизу главного экрана (стандартный баннер — 50–60 dp). */
val AdBannerHeight = 50.dp

private val TitlePurple = Color(0xFF7B3FE4)
private val BubbleGreen = Color(0xFF3DB24B)

// ---------------- Главный экран: выбор темы ----------------

@Composable
fun ThemesScreen(onOpen: (ColoringTheme) -> Unit) {
    val context = LocalContext.current.applicationContext
    val themes = remember {
        Themes.map { it to Catalog.pages(context, it).size }.filter { it.second > 0 }
    }
    val conf = LocalConfiguration.current
    val wide = conf.screenWidthDp >= 700
    val k = (conf.screenWidthDp / 900f).coerceIn(0.42f, 1.15f)

    // боковые отступы — у заголовка и карточек, а баннер внизу идёт во всю ширину
    Column(Modifier.fillMaxSize().padding(top = 4.dp)) {
        // --- Заголовок ---
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height((58 * k).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🎨", fontSize = (34 * k).sp)
            Spacer(Modifier.width(8.dp))
            // заголовок занимает всё свободное место и не обрезается
            Box(Modifier.weight(1f)) {
                OutlinedTitle("Что будем раскрашивать?", (28 * k).sp)
            }
            MusicButton(size = (46 * k).coerceAtLeast(40f).dp)
            Spacer(Modifier.width(6.dp))
            if (wide) {
                SunMascot(Modifier.size((60 * k).dp))
                Spacer(Modifier.width(4.dp))
                SpeechBubble(
                    "Выбирай категорию\nи включай фантазию! ♡",
                    Modifier.rotate(-4f)
                )
            } else {
                SunMascot(Modifier.size((50 * k).dp))
            }
        }
        Spacer(Modifier.height(4.dp))

        // --- Карточки тем на белой подложке ---
        val panel = RoundedCornerShape(28.dp)
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .shadow(10.dp, panel)
                .clip(panel)
                .background(Color.White.copy(alpha = 0.6f))
                .border(3.dp, Color.White, panel)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(if (wide) 170.dp else 150.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(themes, key = { it.first.id }) { (theme, count) ->
                    ThemeCard(theme, count) { onOpen(theme) }
                }
            }
        }

        // --- Пустое место под рекламный баннер ---
        Spacer(Modifier.height(4.dp))
        AdBannerSlot(Modifier.fillMaxWidth().height(AdBannerHeight))
    }
}

/** Место под рекламный баннер Яндекса внизу главного экрана. */
@Composable
fun AdBannerSlot(modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        ru.raskraska.kids.ads.AdBanner()
    }
}

@Composable
private fun ThemeCard(theme: ColoringTheme, count: Int, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, label = "press")
    val shape = RoundedCornerShape(24.dp)
    Box(
        Modifier
            .aspectRatio(1.5f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(8.dp, shape)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(lerp(theme.color, Color.White, 0.28f), theme.color, lerp(theme.color, Color.Black, 0.08f))
                )
            )
            .border(4.dp, Color.White, shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        // мягкие блики и звёздочки на карточке
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(alpha = 0.18f), size.height * 0.55f, Offset(size.width * 0.95f, size.height * 0.05f))
            drawCircle(Color.White.copy(alpha = 0.12f), size.height * 0.35f, Offset(size.width * 0.02f, size.height * 0.98f))
            sparkle(Offset(size.width * 0.12f, size.height * 0.16f), size.height * 0.06f)
            sparkle(Offset(size.width * 0.9f, size.height * 0.5f), size.height * 0.045f)
        }
        Text(
            theme.emoji,
            fontSize = 54.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 6.dp),
            style = TextStyle(shadow = Shadow(Color(0x44000000), Offset(0f, 6f), 10f))
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 10.dp, end = 50.dp)
        ) {
            Text(
                theme.title,
                color = Color.White,
                fontSize = 20.sp,
                fontFamily = Nunito,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(shadow = Shadow(Color(0x55000000), Offset(0f, 3f), 6f))
            )
            Text(
                picturesLabel(count),
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 13.sp,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold
            )
        }
        // белая кнопка-стрелка
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 12.dp)
                .size(34.dp)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(14.dp)) {
                val p = Path().apply {
                    moveTo(size.width * 0.3f, size.height * 0.1f)
                    lineTo(size.width * 0.75f, size.height * 0.5f)
                    lineTo(size.width * 0.3f, size.height * 0.9f)
                }
                drawPath(p, theme.color, style = Stroke(size.width * 0.22f, join = StrokeJoin.Round, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }
        }
    }
}

/** Крупный заголовок: фиолетовые буквы в толстой белой обводке. */
@Composable
fun OutlinedTitle(text: String, fontSize: androidx.compose.ui.unit.TextUnit, modifier: Modifier = Modifier) {
    val base = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = fontSize)
    val outline = with(androidx.compose.ui.platform.LocalDensity.current) { fontSize.toPx() * 0.2f }
    Box(modifier) {
        Text(
            text, maxLines = 1, softWrap = false,
            style = base.copy(
                color = Color.White,
                drawStyle = Stroke(width = outline, join = StrokeJoin.Round),
                shadow = Shadow(Color(0x40301060), Offset(0f, 5f), 8f)
            )
        )
        Text(
            text, maxLines = 1, softWrap = false,
            style = base.copy(
                brush = Brush.verticalGradient(listOf(Color(0xFF9B6BFF), TitlePurple, Color(0xFF6A2FD0)))
            )
        )
    }
}

@Composable
private fun SunMascot(modifier: Modifier) {
    val t = rememberInfiniteTransition(label = "sun")
    val time by t.animateFloat(0f, 1f, infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "t")
    Canvas(modifier.graphicsLayer { rotationZ = sin(time * 2f * PI.toFloat()) * 6f }) {
        glossySun(Offset(size.width / 2, size.height / 2), min(size.width, size.height) * 0.3f)
    }
}

@Composable
private fun SpeechBubble(text: String, modifier: Modifier) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier
            .shadow(6.dp, shape)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF5BCB5E), BubbleGreen)))
            .border(3.dp, Color.White, shape)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text,
            color = Color.White,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontFamily = Nunito,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

// ---------------- Фон главного экрана ----------------

/** Светлый «весенний» фон: небо, облачка, листочки по углам, искорки. */
@Composable
fun HomeBackground() {
    val t = rememberInfiniteTransition(label = "home")
    val time by t.animateFloat(0f, 1f, infiniteRepeatable(tween(7000, easing = LinearEasing)), label = "t")
    Canvas(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFB9E8FF), Color(0xFFD9F6EE), Color(0xFFBDEFC4))
                )
            )
    ) {
        val w = size.width
        val h = size.height
        val u = min(w, h)
        val tau = 2f * PI.toFloat()
        fun sway(i: Int) = sin((time + i * 0.17f) * tau) * 6f

        softCloud(Offset(w * 0.45f, h * 0.06f), u * 0.07f, 0.8f)
        softCloud(Offset(w * 0.12f, h * 0.9f), u * 0.06f, 0.6f)
        softCloud(Offset(w * 0.7f, h * 0.95f), u * 0.07f, 0.6f)

        // листья в углах
        val leaves = listOf(
            Triple(Offset(w * 0.02f, h * 0.78f), -60f, Color(0xFF43A047)),
            Triple(Offset(w * 0.06f, h * 0.9f), -30f, Color(0xFF66BB6A)),
            Triple(Offset(w * 0.01f, h * 0.97f), -10f, Color(0xFF2E7D32)),
            Triple(Offset(w * 0.98f, h * 0.18f), 230f, Color(0xFF66BB6A)),
            Triple(Offset(w * 0.99f, h * 0.32f), 200f, Color(0xFF43A047)),
            Triple(Offset(w * 0.97f, h * 0.85f), 210f, Color(0xFF43A047)),
            Triple(Offset(w * 0.93f, h * 0.97f), 240f, Color(0xFF66BB6A)),
        )
        leaves.forEachIndexed { i, (p, rot, c) -> glossyLeaf(p, u * 0.09f, rot + sway(i), c) }

        glossyStar(Offset(w * 0.08f, h * 0.96f), u * 0.035f, Color(0xFFFFD54F), 10f)
        daisy(Offset(w * 0.9f, h * 0.9f), u * 0.025f)
        for (i in 0 until 10) {
            val tw = 0.5f + 0.5f * sin((time * 2f + i * 0.31f) * tau)
            sparkle(Offset(w * ((i * 0.618f + 0.1f) % 1f), h * ((i * 0.37f + 0.05f) % 1f)), u * 0.012f * tw)
        }
        for (i in 0 until 8) {
            val rise = ((time + i * 0.13f) % 1f) * h
            drawCircle(Color.White.copy(alpha = 0.35f), u * (0.006f + 0.004f * (i % 3)), Offset(w * ((i * 0.43f + 0.2f) % 1f), h - rise))
        }
    }
}

/** Круглая белая кнопка «назад» с толстой фиолетовой стрелкой. */
@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, label = "press")
    Box(
        modifier
            .size(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF1EBFF))))
            .border(3.dp, Color.White, CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(26.dp)) {
            val w = size.width
            val h = size.height
            val brush = Brush.verticalGradient(listOf(Color(0xFF9B6BFF), TitlePurple, Color(0xFF5E25C2)))
            val stroke = Stroke(w * 0.2f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = StrokeJoin.Round)
            // древко
            drawLine(brush, Offset(w * 0.18f, h * 0.5f), Offset(w * 0.88f, h * 0.5f), w * 0.2f, androidx.compose.ui.graphics.StrokeCap.Round)
            // наконечник
            val head = Path().apply {
                moveTo(w * 0.5f, h * 0.14f)
                lineTo(w * 0.14f, h * 0.5f)
                lineTo(w * 0.5f, h * 0.86f)
            }
            drawPath(head, brush, style = stroke)
        }
    }
}
