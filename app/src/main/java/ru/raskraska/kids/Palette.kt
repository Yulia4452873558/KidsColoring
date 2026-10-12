package ru.raskraska.kids

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/** 20 оттенков × 4 тона (светлый, средний, яркий, тёмный) = 80 цветов. */
private val Shades: List<Long> = listOf(
    0xFFFFCDD2, 0xFFE57373, 0xFFF44336, 0xFFC62828, // красный
    0xFFF8BBD0, 0xFFF06292, 0xFFE91E63, 0xFFAD1457, // розовый
    0xFFE1BEE7, 0xFFBA68C8, 0xFF9C27B0, 0xFF6A1B9A, // фиолетовый
    0xFFD1C4E9, 0xFF9575CD, 0xFF673AB7, 0xFF4527A0, // сиреневый
    0xFFC5CAE9, 0xFF7986CB, 0xFF3F51B5, 0xFF283593, // индиго
    0xFFBBDEFB, 0xFF64B5F6, 0xFF2196F3, 0xFF1565C0, // синий
    0xFFB3E5FC, 0xFF4FC3F7, 0xFF03A9F4, 0xFF0277BD, // голубой
    0xFFB2EBF2, 0xFF4DD0E1, 0xFF00BCD4, 0xFF00838F, // бирюзовый
    0xFFB2DFDB, 0xFF4DB6AC, 0xFF009688, 0xFF00695C, // морская волна
    0xFFC8E6C9, 0xFF81C784, 0xFF4CAF50, 0xFF2E7D32, // зелёный
    0xFFDCEDC8, 0xFFAED581, 0xFF8BC34A, 0xFF558B2F, // салатовый
    0xFFF0F4C3, 0xFFDCE775, 0xFFCDDC39, 0xFF9E9D24, // лаймовый
    0xFFFFF9C4, 0xFFFFF176, 0xFFFFEB3B, 0xFFF9A825, // жёлтый
    0xFFFFECB3, 0xFFFFD54F, 0xFFFFC107, 0xFFFF8F00, // янтарный
    0xFFFFE0B2, 0xFFFFB74D, 0xFFFF9800, 0xFFEF6C00, // оранжевый
    0xFFFFCCBC, 0xFFFF8A65, 0xFFFF5722, 0xFFD84315, // морковный
    0xFFFFE5D0, 0xFFF5C6A5, 0xFFE0A47A, 0xFFA8693F, // цвет кожи
    0xFFD7CCC8, 0xFFA1887F, 0xFF795548, 0xFF4E342E, // коричневый
    0xFFCFD8DC, 0xFF90A4AE, 0xFF607D8B, 0xFF37474F, // серо-голубой
    0xFFFFFFFF, 0xFFBDBDBD, 0xFF757575, 0xFF212121, // белый, серый, чёрный
)

val PaletteColors: List<Color> = Shades.map { Color(it) }

/** Цвета, которые ребёнок сам выбрал в «радуге» (последние три). */
object CustomColors {
    val list = mutableStateListOf<Color>()

    fun add(c: Color) {
        list.remove(c)
        list.add(0, c)
        while (list.size > 3) list.removeAt(list.lastIndex)
    }
}

private val RainbowColors = listOf(
    Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00), Color(0xFF00FFFF),
    Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000),
)

/**
 * Палитра: первая «строчка» — кнопка своего цвета и три последних своих цвета,
 * дальше 80 готовых цветов. По 4 тона одного цвета в ряд (или в столбик в вертикальной ориентации).
 */
@Composable
fun PaletteStrip(
    selected: Color,
    horizontal: Boolean,
    onPick: (Color) -> Unit,
    onCustom: () -> Unit,
    modifier: Modifier,
) {
    val count = 4 + PaletteColors.size
    val cell: @Composable (Int) -> Unit = { i ->
        val m = Modifier
            .padding(3.dp)
            .aspectRatio(1f, matchHeightConstraintsFirst = horizontal)
        when {
            i == 0 -> PickerDot(m, onCustom)
            i < 4 -> {
                val c = CustomColors.list.getOrNull(i - 1)
                if (c != null) ColorDot(c, c == selected, m) { onPick(c) } else Box(m)
            }
            else -> {
                val c = PaletteColors[i - 4]
                ColorDot(c, c == selected, m) { onPick(c) }
            }
        }
    }
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(VioletLight)
            .padding(4.dp)
    ) {
        if (horizontal) {
            LazyHorizontalGrid(rows = GridCells.Fixed(4), modifier = Modifier.fillMaxSize()) {
                items(count) { i -> cell(i) }
            }
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.fillMaxSize()) {
                items(count) { i -> cell(i) }
            }
        }
    }
}

@Composable
private fun ColorDot(color: Color, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .drawBehind {
                val r = size.minDimension / 2f * (if (selected) 1f else 0.86f)
                drawCircle(color, r)
                drawCircle(Color(0x33000000), r, style = Stroke(width = 1.dp.toPx()))
                if (selected) {
                    drawCircle(Color.White, r - 3.dp.toPx(), style = Stroke(width = 3.dp.toPx()))
                    drawCircle(Violet, r, style = Stroke(width = 2.dp.toPx()))
                }
            }
    )
}

@Composable
private fun PickerDot(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(CircleShape)
            .background(Brush.sweepGradient(RainbowColors))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("+", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
    }
}

/** Следит за пальцем: сообщает координату при касании и при движении. */
private suspend fun PointerInputScope.trackTouches(onPosition: (Offset) -> Unit) {
    awaitEachGesture {
        val down = awaitFirstDown()
        down.consume()
        onPosition(down.position)
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) break
            onPosition(change.position)
            change.consume()
        }
    }
}

private fun hsvColor(h: Float, s: Float, v: Float) =
    Color(android.graphics.Color.HSVToColor(floatArrayOf(h, s, v)))

/** Окошко «свой цвет»: квадрат яркости/насыщенности и радужная полоска оттенка. */
@Composable
fun ColorPickerDialog(initial: Color, onDismiss: () -> Unit, onPicked: (Color) -> Unit) {
    val start = remember {
        FloatArray(3).also { android.graphics.Color.colorToHSV(initial.toArgb(), it) }
    }
    var hue by remember { mutableFloatStateOf(start[0]) }
    var sat by remember { mutableFloatStateOf(start[1]) }
    var value by remember { mutableFloatStateOf(start[2]) }
    val current = hsvColor(hue, sat, value)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        trackTouches { p ->
                            sat = (p.x / size.width).coerceIn(0f, 1f)
                            value = 1f - (p.y / size.height).coerceIn(0f, 1f)
                        }
                    }
                    .drawBehind {
                        drawRect(Brush.horizontalGradient(listOf(Color.White, hsvColor(hue, 1f, 1f))))
                        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                        val c = Offset(sat * size.width, (1f - value) * size.height)
                        drawCircle(Color.White, 13.dp.toPx(), c, style = Stroke(4.dp.toPx()))
                        drawCircle(Color(0x66000000), 15.dp.toPx(), c, style = Stroke(1.dp.toPx()))
                    }
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .pointerInput(Unit) {
                        trackTouches { p -> hue = (p.x / size.width).coerceIn(0f, 1f) * 359.9f }
                    }
                    .drawBehind {
                        drawRect(Brush.horizontalGradient(RainbowColors))
                        val c = Offset(hue / 360f * size.width, size.height / 2f)
                        drawCircle(hsvColor(hue, 1f, 1f), size.height / 2f - 4.dp.toPx(), c)
                        drawCircle(Color.White, size.height / 2f - 4.dp.toPx(), c, style = Stroke(4.dp.toPx()))
                    }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(current)
                        .drawBehind { drawCircle(Color(0x33000000), size.minDimension / 2f, style = Stroke(2.dp.toPx())) }
                )
                Spacer(Modifier.weight(1f))
                DialogButton("✖", Color(0xFFFFCDD2), onDismiss)
                Spacer(Modifier.width(12.dp))
                DialogButton("✔", Color(0xFFC8E6C9)) { onPicked(current) }
            }
        }
    }
}

@Composable
private fun DialogButton(symbol: String, background: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .size(width = 72.dp, height = 52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, fontSize = 24.sp, color = Violet, fontWeight = FontWeight.Bold)
    }
}
