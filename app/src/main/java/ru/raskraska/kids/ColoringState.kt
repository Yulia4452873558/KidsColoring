package ru.raskraska.kids

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.hypot

/** Сторона рабочего холста в пикселях. Все координаты рисования — в этой системе. */
const val SIZE = 1024

enum class Tool { FILL, PENCIL, MARKER, RAINBOW, ERASER }

/**
 * Рисунок состоит из двух слоёв: [colors] — то, что накрасил ребёнок, и [outline] — чёрный
 * контур, который всегда рисуется сверху, поэтому линии нельзя закрасить или стереть.
 */
class ColoringState(context: android.content.Context, val page: ColoringPage, saved: Bitmap?) {

    val outline: Bitmap = renderOutline(context, page, SIZE)

    val colors: Bitmap =
        if (saved != null && saved.isMutable && saved.width == SIZE && saved.height == SIZE) {
            saved
        } else {
            Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888).also { b ->
                b.eraseColor(Color.WHITE)
                if (saved != null) {
                    Canvas(b).drawBitmap(saved, null, Rect(0, 0, SIZE, SIZE), Paint(Paint.FILTER_BITMAP_FLAG))
                }
            }
        }

    /** true там, где проходит контур: заливка через такие пиксели не растекается. */
    private val barrier = BooleanArray(SIZE * SIZE)
    private val pixels = IntArray(SIZE * SIZE)
    private val visited = BooleanArray(SIZE * SIZE)
    private val stack = IntArray(SIZE * SIZE)

    private val canvas = Canvas(colors)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val history = ArrayDeque<Bitmap>()
    private var hue = 0f
    private var dirty = false

    /** Меняется при каждом изменении рисунка — экран по нему перерисовывается. */
    var version by mutableIntStateOf(0)
        private set
    var canUndo by mutableStateOf(false)
        private set
    var busy by mutableStateOf(false)
        private set

    init {
        outline.getPixels(pixels, 0, SIZE, 0, 0, SIZE, SIZE)
        // Линии чуть «утолщаем» на 1 пиксель: так заливка не просочится
        // через крошечные разрывы в линиях сгенерированных картинок.
        for (y in 0 until SIZE) {
            for (x in 0 until SIZE) {
                if ((pixels[y * SIZE + x] ushr 24) < 100) continue
                for (dy in -1..1) {
                    val ny = y + dy
                    if (ny < 0 || ny >= SIZE) continue
                    for (dx in -1..1) {
                        val nx = x + dx
                        if (nx in 0 until SIZE) barrier[ny * SIZE + nx] = true
                    }
                }
            }
        }
    }

    private fun pushHistory() {
        val copy = colors.copy(Bitmap.Config.ARGB_8888, false) ?: return
        history.addLast(copy)
        if (history.size > MAX_UNDO) history.removeFirst().recycle()
        canUndo = true
    }

    private fun changed() {
        dirty = true
        version++
    }

    fun undo() {
        if (busy || history.isEmpty()) return
        val last = history.removeLast()
        canvas.drawBitmap(last, 0f, 0f, null)
        last.recycle()
        canUndo = history.isNotEmpty()
        changed()
    }

    fun clear() {
        if (busy) return
        pushHistory()
        colors.eraseColor(Color.WHITE)
        changed()
    }

    /** Начало мазка: запоминаем шаг для отмены и ставим первую точку. */
    fun beginStroke(tool: Tool, color: Int, x: Float, y: Float): Boolean {
        if (busy) return false
        pushHistory()
        setupPaint(tool, color, 0f)
        canvas.drawPoint(x, y, paint)
        changed()
        return true
    }

    fun strokeTo(tool: Tool, color: Int, x0: Float, y0: Float, x1: Float, y1: Float) {
        if (busy) return
        setupPaint(tool, color, hypot(x1 - x0, y1 - y0))
        canvas.drawLine(x0, y0, x1, y1, paint)
        changed()
    }

    private fun setupPaint(tool: Tool, color: Int, travelled: Float) {
        when (tool) {
            Tool.PENCIL -> { paint.color = color; paint.strokeWidth = 12f }
            Tool.MARKER -> { paint.color = color; paint.strokeWidth = 36f }
            Tool.ERASER -> { paint.color = Color.WHITE; paint.strokeWidth = 56f }
            Tool.RAINBOW -> {
                hue = (hue + travelled * 0.5f) % 360f
                paint.color = Color.HSVToColor(floatArrayOf(hue, 0.85f, 1f))
                paint.strokeWidth = 28f
            }
            Tool.FILL -> Unit
        }
    }

    /** Заливка области, ограниченной контуром. Вызывать с главного потока. */
    suspend fun fill(x: Int, y: Int, color: Int) {
        if (busy) return
        val start = findOpen(x.coerceIn(0, SIZE - 1), y.coerceIn(0, SIZE - 1))
        if (start < 0) return
        busy = true
        try {
            pushHistory()
            colors.getPixels(pixels, 0, SIZE, 0, 0, SIZE, SIZE)
            withContext(Dispatchers.Default) { floodFill(start, color or 0xFF000000.toInt()) }
            colors.setPixels(pixels, 0, SIZE, 0, 0, SIZE, SIZE)
            changed()
        } finally {
            busy = false
        }
    }

    /** Если палец попал прямо на линию, ищем ближайшую свободную точку рядом. */
    private fun findOpen(x: Int, y: Int): Int {
        if (!barrier[y * SIZE + x]) return y * SIZE + x
        for (r in 1..16) {
            for (dy in -r..r) {
                for (dx in -r..r) {
                    if (kotlin.math.abs(dx) != r && kotlin.math.abs(dy) != r) continue
                    val nx = x + dx
                    val ny = y + dy
                    if (nx < 0 || ny < 0 || nx >= SIZE || ny >= SIZE) continue
                    if (!barrier[ny * SIZE + nx]) return ny * SIZE + nx
                }
            }
        }
        return -1
    }

    private fun floodFill(start: Int, color: Int) {
        visited.fill(false)
        var sp = 0
        stack[sp++] = start
        visited[start] = true
        while (sp > 0) {
            val p = stack[--sp]
            pixels[p] = color
            // Пиксель контура красим (чтобы под линией не было белой каёмки), но дальше не идём
            if (barrier[p]) continue
            val x = p % SIZE
            if (x > 0 && !visited[p - 1]) { visited[p - 1] = true; stack[sp++] = p - 1 }
            if (x < SIZE - 1 && !visited[p + 1]) { visited[p + 1] = true; stack[sp++] = p + 1 }
            if (p >= SIZE && !visited[p - SIZE]) { visited[p - SIZE] = true; stack[sp++] = p - SIZE }
            if (p < SIZE * (SIZE - 1) && !visited[p + SIZE]) { visited[p + SIZE] = true; stack[sp++] = p + SIZE }
        }
    }

    /** Готовая картинка: цвета + контур. */
    fun merged(): Bitmap {
        val result = colors.copy(Bitmap.Config.ARGB_8888, true)
        Canvas(result).drawBitmap(outline, 0f, 0f, null)
        return result
    }

    /** Запоминает незаконченный рисунок, чтобы к нему можно было вернуться. */
    fun persist(context: android.content.Context) {
        if (!dirty) return
        dirty = false
        Storage.saveProgress(context, page.id, colors)
    }

    private companion object {
        const val MAX_UNDO = 8
    }
}
