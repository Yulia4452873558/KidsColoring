package ru.raskraska.kids

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import ru.raskraska.kids.ads.afterRewarded
import ru.raskraska.kids.ads.findActivity
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ColoringScreen(page: ColoringPage, onBack: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val activity = LocalContext.current.findActivity()
    val ads = (context as? KidsApp)?.ads

    // Холст создаётся в фоне: рисуем контур и подгружаем незаконченный рисунок
    val loaded by produceState<ColoringState?>(null, page.id) {
        value = withContext(Dispatchers.Default) {
            ColoringState(context, page, Storage.load(context, page.id))
        }
    }
    val state = loaded
    if (state == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = LocalAccent.current)
        }
        return
    }

    var tool by rememberSaveable { mutableStateOf(Tool.FILL) }
    var colorArgb by rememberSaveable { mutableIntStateOf(0xFFF44336.toInt()) }
    var showPicker by remember { mutableStateOf(false) }
    var askSave by remember { mutableStateOf(false) }
    val color = Color(colorArgb)
    val scope = rememberCoroutineScope()

    // Рисунок запоминается сам: при выходе к галерее и при сворачивании приложения
    DisposableEffect(state) { onDispose { state.persist(context) } }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { state.persist(context) }

    fun export() {
        val picture = state.merged()
        scope.launch {
            val ok = withContext(Dispatchers.IO) { Storage.exportToGallery(context, picture) }
            picture.recycle()
            Toast.makeText(context, if (ok) R.string.saved else R.string.save_failed, Toast.LENGTH_SHORT).show()
        }
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) export()
    }

    fun save() {
        val allowed = Build.VERSION.SDK_INT >= 29 || ContextCompat.checkSelfPermission(
            context, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (allowed) export() else permission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    fun pickColor(c: Color) {
        colorArgb = c.toArgb()
        if (tool == Tool.ERASER) tool = Tool.FILL
    }

    val canvas: @Composable (Modifier) -> Unit = { modifier ->
        Box(modifier, contentAlignment = Alignment.Center) {
            val shape = RoundedCornerShape(22.dp)
            Box(
                Modifier
                    .aspectRatio(1f)
                    .shadow(10.dp, shape)
                    .clip(shape)
                    .background(Color.White)
                    .border(6.dp, LocalAccent.current, shape)
            ) {
                DrawingCanvas(state, tool, colorArgb)
            }
        }
    }
    val undoAlpha = if (state.canUndo) 1f else 0.4f

    Box(Modifier.fillMaxSize()) {
        run {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    Modifier
                        .width(80.dp)
                        .fillMaxHeight()
                        .then(panel())
                        .padding(5.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ActionButton("🏠", Modifier.weight(1f).fillMaxWidth(), onClick = onBack)
                    for (t in Tool.entries) {
                        ToolButton(t, color, t == tool, Modifier.weight(1f).fillMaxWidth()) { tool = t }
                    }
                }
                canvas(Modifier.weight(1f).fillMaxHeight())
                Column(
                    Modifier
                        .width(212.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ActionButton("↩", Modifier.weight(1f).fillMaxHeight().alpha(undoAlpha), onClick = { state.undo() })
                        ActionButton("🗑", Modifier.weight(1f).fillMaxHeight(), onClick = { state.clear() })
                        ActionButton("💾", Modifier.weight(1f).fillMaxHeight(), onClick = { askSave = true })
                    }
                    PaletteStrip(
                        selected = color,
                        horizontal = false,
                        onPick = ::pickColor,
                        onCustom = { showPicker = true },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }

    if (askSave) {
        WatchAdDialog(
            title = "Посмотри рекламу,\nчтобы сохранить рисунок",
            subtitle = "Рисунок появится в галерее телефона",
            onWatch = {
                askSave = false
                ads.afterRewarded(activity) { save() }
            },
            onDismiss = { askSave = false }
        )
    }

    if (showPicker) {
        ColorPickerDialog(
            initial = color,
            onDismiss = { showPicker = false },
            onPicked = {
                CustomColors.add(it)
                pickColor(it)
                showPicker = false
            }
        )
    }
}

@Composable
private fun DrawingCanvas(state: ColoringState, tool: Tool, color: Int) {
    val currentTool by rememberUpdatedState(tool)
    val currentColor by rememberUpdatedState(color)
    val scope = rememberCoroutineScope()
    val colorsImage = remember(state) { state.colors.asImageBitmap() }
    val outlineImage = remember(state) { state.outline.asImageBitmap() }

    Canvas(
        Modifier
            .fillMaxSize()
            .pointerInput(state) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    // Переводим координаты пальца в координаты холста 1000×1000
                    val k = SIZE / size.width.toFloat()
                    val t = currentTool
                    val c = currentColor
                    var last = down.position * k
                    if (t == Tool.FILL) {
                        scope.launch { state.fill(last.x.toInt(), last.y.toInt(), c) }
                        return@awaitEachGesture
                    }
                    if (!state.beginStroke(t, c, last.x, last.y)) return@awaitEachGesture
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        val p = change.position * k
                        state.strokeTo(t, c, last.x, last.y, p.x, p.y)
                        last = p
                        change.consume()
                    }
                }
            }
    ) {
        state.version // чтение состояния: холст перерисуется после каждого изменения
        val dst = IntSize(size.width.toInt(), size.height.toInt())
        drawImage(colorsImage, dstSize = dst, filterQuality = FilterQuality.Medium)
        drawImage(outlineImage, dstSize = dst, filterQuality = FilterQuality.Medium)
    }
}

@Composable
private fun ActionButton(symbol: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, fontSize = 24.sp, color = LocalAccent.current)
    }
}

@Composable
private fun ToolButton(tool: Tool, color: Color, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier
            .clip(shape)
            .background(if (selected) Color.White else LocalAccent.current.copy(alpha = 0.12f))
            .border(if (selected) 4.dp else 0.dp, if (selected) LocalAccent.current else Color.Transparent, shape)
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) { drawToolIcon(tool, color) }
    }
}


/** Значки инструментов нарисованы кодом и окрашены в выбранный цвет. */
private fun DrawScope.drawToolIcon(tool: Tool, color: Color) {
    val w = size.minDimension
    val wood = Color(0xFFFFE0B2)
    val edge = Color(0x55000000)
    translate((size.width - w) / 2f, (size.height - w) / 2f) {
        if (tool == Tool.FILL) {
            // ведёрко с краской и капля
            val bucket = Path().apply {
                moveTo(0.22f * w, 0.36f * w); lineTo(0.72f * w, 0.36f * w)
                lineTo(0.64f * w, 0.84f * w); lineTo(0.30f * w, 0.84f * w); close()
            }
            drawPath(bucket, Color(0xFFB0BEC5))
            drawPath(bucket, edge, style = Stroke(w * 0.03f))
            drawOval(color, Offset(0.20f * w, 0.26f * w), Size(0.54f * w, 0.20f * w))
            drawOval(edge, Offset(0.20f * w, 0.26f * w), Size(0.54f * w, 0.20f * w), style = Stroke(w * 0.03f))
            drawCircle(color, 0.09f * w, Offset(0.84f * w, 0.66f * w))
            drawCircle(edge, 0.09f * w, Offset(0.84f * w, 0.66f * w), style = Stroke(w * 0.03f))
            return@translate
        }
        rotate(-40f, Offset(w / 2f, w / 2f)) {
            when (tool) {
                Tool.PENCIL, Tool.RAINBOW -> {
                    val top = 0.38f * w
                    val h = 0.24f * w
                    val bodySize = Size(0.56f * w, h)
                    val radius = CornerRadius(w * 0.04f)
                    if (tool == Tool.RAINBOW) {
                        val rainbow = Brush.horizontalGradient(
                            listOf(
                                Color(0xFFF44336), Color(0xFFFFEB3B), Color(0xFF4CAF50),
                                Color(0xFF2196F3), Color(0xFF9C27B0)
                            ),
                            startX = 0.06f * w, endX = 0.62f * w
                        )
                        drawRoundRect(rainbow, Offset(0.06f * w, top), bodySize, radius)
                    } else {
                        drawRoundRect(color, Offset(0.06f * w, top), bodySize, radius)
                    }
                    drawRoundRect(edge, Offset(0.06f * w, top), bodySize, radius, style = Stroke(w * 0.025f))
                    val tip = Path().apply {
                        moveTo(0.62f * w, top); lineTo(0.94f * w, 0.5f * w); lineTo(0.62f * w, top + h); close()
                    }
                    drawPath(tip, wood)
                    drawPath(tip, edge, style = Stroke(w * 0.025f))
                    val lead = Path().apply {
                        moveTo(0.82f * w, 0.455f * w); lineTo(0.94f * w, 0.5f * w); lineTo(0.82f * w, 0.545f * w); close()
                    }
                    drawPath(lead, if (tool == Tool.RAINBOW) Color(0xFFE91E63) else color)
                }
                Tool.MARKER -> {
                    val radius = CornerRadius(w * 0.06f)
                    drawRoundRect(color, Offset(0.06f * w, 0.32f * w), Size(0.56f * w, 0.36f * w), radius)
                    drawRoundRect(
                        edge, Offset(0.06f * w, 0.32f * w), Size(0.56f * w, 0.36f * w), radius,
                        style = Stroke(w * 0.025f)
                    )
                    drawRect(Color(0xFF546E7A), Offset(0.62f * w, 0.40f * w), Size(0.14f * w, 0.20f * w))
                    drawRoundRect(
                        color, Offset(0.76f * w, 0.43f * w), Size(0.16f * w, 0.14f * w), CornerRadius(w * 0.05f)
                    )
                    drawRoundRect(
                        edge, Offset(0.76f * w, 0.43f * w), Size(0.16f * w, 0.14f * w), CornerRadius(w * 0.05f),
                        style = Stroke(w * 0.02f)
                    )
                }
                Tool.ERASER -> {
                    val radius = CornerRadius(w * 0.08f)
                    drawRoundRect(Color(0xFFF06292), Offset(0.14f * w, 0.32f * w), Size(0.72f * w, 0.36f * w), radius)
                    drawRoundRect(Color(0xFF42A5F5), Offset(0.14f * w, 0.32f * w), Size(0.34f * w, 0.36f * w), radius)
                    drawRoundRect(
                        edge, Offset(0.14f * w, 0.32f * w), Size(0.72f * w, 0.36f * w), radius,
                        style = Stroke(w * 0.025f)
                    )
                }
                Tool.FILL -> Unit
            }
        }
    }
}

/** Полупрозрачная белая подложка под кнопки и палитру — фон темы виден, но кнопки читаются. */
@Composable
private fun panel(): Modifier {
    val shape = RoundedCornerShape(22.dp)
    return Modifier
        .shadow(8.dp, shape)
        .clip(shape)
        .background(Color.White.copy(alpha = 0.85f))
}

/** Небольшое окно «Посмотри рекламу, чтобы…» перед рекламой с вознаграждением. */
@Composable
private fun WatchAdDialog(title: String, subtitle: String, onWatch: () -> Unit, onDismiss: () -> Unit) {
    val accent = LocalAccent.current
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        val shape = RoundedCornerShape(28.dp)
        Column(
            Modifier
                .width(300.dp)
                .shadow(16.dp, shape)
                .clip(shape)
                .background(Color.White)
                .border(4.dp, accent, shape)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🎬", fontSize = 44.sp)
            Text(
                title,
                fontFamily = Nunito,
                fontWeight = FontWeight.Black,
                fontSize = 19.sp,
                lineHeight = 23.sp,
                color = Color(0xFF4A3A6B),
                textAlign = TextAlign.Center
            )
            Text(
                subtitle,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = Color(0xFF8A7FA3),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(2.dp))
            val pill = RoundedCornerShape(50)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(6.dp, pill)
                    .clip(pill)
                    .background(Brush.verticalGradient(listOf(lerp(accent, Color.White, 0.25f), accent)))
                    .clickable(onClick = onWatch),
                contentAlignment = Alignment.Center
            ) {
                Text("▶  Смотреть", color = Color.White, fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(pill)
                    .background(Color(0xFFF1EEF7))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text("Не сейчас", color = Color(0xFF8A7FA3), fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }
    }
}
