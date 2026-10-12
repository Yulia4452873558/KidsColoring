package ru.raskraska.kids

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Одна раскраска. Бывает двух видов:
 *  - картинка из папки assets/coloring/<тема>/ (сгенерированная или нарисованная) — [assetPath];
 *  - картинка, описанная кодом в координатах 1000×1000 — [draw].
 */
class ColoringPage(
    val id: String,
    val assetPath: String? = null,
    val draw: (Pen.() -> Unit)? = null,
)

/** Контур раскраски: чёрные линии на прозрачном фоне, size×size пикселей. */
fun renderOutline(context: Context, page: ColoringPage, size: Int): Bitmap {
    val draw = page.draw
    if (draw != null) {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(size / 1000f, size / 1000f)
        draw(Pen(canvas))
        return bitmap
    }
    return renderAssetOutline(context, page.assetPath ?: "", size)
}

/**
 * Превращает обычную чёрно-белую картинку в слой контура: тёмные пиксели становятся
 * чёрной линией, светлые — прозрачными. Картинка вписывается в квадрат по центру.
 */
private fun renderAssetOutline(context: Context, path: String, size: Int): Bitmap {
    val result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val source = decodeAsset(context, path, size) ?: return result

    val square = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    square.eraseColor(Color.WHITE)
    val scale = min(size / source.width.toFloat(), size / source.height.toFloat())
    val w = source.width * scale
    val h = source.height * scale
    val left = (size - w) / 2f
    val top = (size - h) / 2f
    Canvas(square).drawBitmap(source, null, RectF(left, top, left + w, top + h), Paint(Paint.FILTER_BITMAP_FLAG))
    source.recycle()

    val px = IntArray(size * size)
    square.getPixels(px, 0, size, 0, 0, size, size)
    square.recycle()
    for (i in px.indices) {
        val p = px[i]
        val lum = (Color.red(p) * 299 + Color.green(p) * 587 + Color.blue(p) * 114) / 1000
        val alpha = ((255 - lum - 40) * 255 / 140).coerceIn(0, 255)
        px[i] = alpha shl 24 // чёрный цвет с нужной прозрачностью
    }
    result.setPixels(px, 0, size, 0, 0, size, size)
    return result
}

/** Читает картинку из assets, сразу уменьшая большие файлы (экономит память). */
private fun decodeAsset(context: Context, path: String, target: Int): Bitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= target && bounds.outHeight / (sample * 2) >= target) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    context.assets.open(path).use { BitmapFactory.decodeStream(it, null, options) }
} catch (e: Exception) {
    null
}

/**
 * Маленький «карандаш» для рисования контуров. Каждая замкнутая фигура сначала стирает
 * всё под собой, а потом обводится — так фигуры, нарисованные позже, перекрывают прежние,
 * и все области получаются замкнутыми (это важно для заливки).
 */
class Pen(private val c: Canvas) {
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        color = Color.BLACK
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val erase = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    /** Замкнутая фигура. [inner] рисуется внутри неё и обрезается по её границе. */
    fun shape(p: Path, inner: (Pen.() -> Unit)? = null) {
        c.drawPath(p, erase)
        if (inner != null) {
            c.save()
            c.clipPath(p)
            inner()
            c.restore()
        }
        c.drawPath(p, stroke)
    }

    fun path(inner: (Pen.() -> Unit)? = null, build: Path.() -> Unit) {
        val p = Path()
        p.build()
        p.close()
        shape(p, inner)
    }

    fun oval(cx: Int, cy: Int, rx: Int, ry: Int, rot: Int = 0, inner: (Pen.() -> Unit)? = null) {
        val p = Path()
        p.addOval(
            (cx - rx).toFloat(), (cy - ry).toFloat(), (cx + rx).toFloat(), (cy + ry).toFloat(),
            Path.Direction.CW
        )
        if (rot != 0) {
            val m = Matrix()
            m.setRotate(rot.toFloat(), cx.toFloat(), cy.toFloat())
            p.transform(m)
        }
        shape(p, inner)
    }

    fun circle(cx: Int, cy: Int, r: Int, inner: (Pen.() -> Unit)? = null) = oval(cx, cy, r, r, 0, inner)

    fun rect(l: Int, t: Int, r: Int, b: Int, radius: Int = 0, inner: (Pen.() -> Unit)? = null) {
        val p = Path()
        p.addRoundRect(
            l.toFloat(), t.toFloat(), r.toFloat(), b.toFloat(),
            radius.toFloat(), radius.toFloat(), Path.Direction.CW
        )
        shape(p, inner)
    }

    /** Многоугольник: x1, y1, x2, y2, ... */
    fun poly(vararg pts: Int) = shape(polyline(pts).apply { close() })

    /** Незамкнутая ломаная: x1, y1, x2, y2, ... */
    fun line(vararg pts: Int) = c.drawPath(polyline(pts), stroke)

    /** Незамкнутая кривая. */
    fun curve(build: Path.() -> Unit) {
        val p = Path()
        p.build()
        c.drawPath(p, stroke)
    }

    /** Закрашенная чёрная точка (глаз, нос). */
    fun dot(cx: Int, cy: Int, r: Int) = c.drawCircle(cx.toFloat(), cy.toFloat(), r.toFloat(), ink)

    fun star(cx: Int, cy: Int, r: Int) {
        val p = Path()
        for (i in 0 until 10) {
            val a = Math.toRadians(-90.0 + i * 36.0)
            val rr = if (i % 2 == 0) r.toDouble() else r * 0.45
            val x = (cx + rr * cos(a)).toFloat()
            val y = (cy + rr * sin(a)).toFloat()
            if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        p.close()
        shape(p)
    }

    fun cloud(cx: Int, cy: Int) {
        val p = Path()
        p.addCircle(cx - 75f, cy + 10f, 55f, Path.Direction.CW)
        val b = Path()
        b.addCircle(cx.toFloat(), cy - 25f, 75f, Path.Direction.CW)
        p.op(b, Path.Op.UNION)
        b.reset()
        b.addCircle(cx + 80f, cy + 10f, 60f, Path.Direction.CW)
        p.op(b, Path.Op.UNION)
        shape(p)
    }

    private fun polyline(pts: IntArray): Path {
        val p = Path()
        p.moveTo(pts[0].toFloat(), pts[1].toFloat())
        var i = 2
        while (i + 1 < pts.size) {
            p.lineTo(pts[i].toFloat(), pts[i + 1].toFloat())
            i += 2
        }
        return p
    }
}

// Короткие команды для контуров в целых координатах
private fun Path.m(x: Int, y: Int) = moveTo(x.toFloat(), y.toFloat())
private fun Path.l(x: Int, y: Int) = lineTo(x.toFloat(), y.toFloat())
private fun Path.q(x1: Int, y1: Int, x2: Int, y2: Int) =
    quadTo(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())
private fun Path.cb(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int) =
    cubicTo(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), x3.toFloat(), y3.toFloat())

/** Картинки, нарисованные кодом. В какие темы они попадают — см. Catalog.kt */
val VectorPages: List<ColoringPage> = listOf(
    ColoringPage("cat") {
        oval(730, 760, 60, 170, rot = -30)                 // хвост
        oval(500, 700, 210, 230)                           // туловище
        oval(500, 740, 110, 140)                           // животик
        oval(420, 900, 70, 45); oval(580, 900, 70, 45)     // лапки
        poly(300, 330, 310, 120, 450, 230)                 // ушки
        poly(700, 330, 690, 120, 550, 230)
        circle(500, 380, 220)                              // голова
        oval(410, 360, 38, 50); dot(410, 370, 18)          // глаза
        oval(590, 360, 38, 50); dot(590, 370, 18)
        poly(480, 430, 520, 430, 500, 455)                 // нос
        curve { m(500, 455); q(470, 500, 440, 470) }       // рот
        curve { m(500, 455); q(530, 500, 560, 470) }
        line(350, 440, 220, 410); line(350, 460, 220, 480) // усы
        line(650, 440, 780, 410); line(650, 460, 780, 480)
    },
    ColoringPage("fish") {
        curve { m(0, 880); q(250, 820, 500, 880); q(750, 940, 1000, 880) } // дно
        poly(760, 500, 930, 360, 900, 500, 930, 640)       // хвост
        path { m(380, 330); q(500, 180, 620, 330) }        // плавники
        poly(450, 680, 520, 790, 580, 670)
        oval(480, 500, 320, 200, inner = {                 // тело с полосками
            curve { m(330, 280); q(410, 500, 330, 720) }
            curve { m(520, 280); q(590, 500, 520, 720) }
            curve { m(650, 300); q(710, 500, 650, 700) }
        })
        circle(250, 450, 36); dot(255, 455, 16)            // глаз
        curve { m(178, 540); q(205, 572, 240, 550) }       // улыбка
        circle(120, 300, 30); circle(170, 200, 22); circle(110, 120, 16) // пузырьки
    },
    ColoringPage("flower") {
        curve { m(0, 860); q(500, 780, 1000, 860) }        // земля
        circle(860, 140, 80)                               // солнце
        cloud(200, 170)
        rect(470, 480, 530, 850)                           // стебель
        path { m(530, 700); q(700, 560, 760, 640); q(680, 760, 530, 700) } // листья
        path { m(470, 740); q(300, 600, 240, 680); q(320, 800, 470, 740) }
        for (i in 0 until 8) {                             // лепестки
            val a = Math.toRadians(i * 45.0)
            oval((500 + 150 * sin(a)).toInt(), (330 - 150 * cos(a)).toInt(), 70, 95, rot = i * 45)
        }
        circle(500, 330, 90)                               // серединка
        dot(470, 315, 10); dot(530, 315, 10)
        curve { m(465, 350); q(500, 385, 535, 350) }
    },
    ColoringPage("house") {
        line(0, 850, 1000, 850)                            // земля
        circle(130, 140, 75)                               // солнце
        cloud(800, 150)
        rect(640, 230, 710, 400)                           // труба
        rect(250, 450, 750, 850)                           // стены
        poly(190, 460, 500, 200, 810, 460)                 // крыша
        circle(500, 370, 45)                               // чердачное окно
        rect(440, 640, 560, 850); dot(535, 750, 10)        // дверь
        rect(300, 520, 410, 630, inner = { line(355, 500, 355, 650); line(280, 575, 430, 575) })
        rect(590, 520, 700, 630, inner = { line(645, 500, 645, 650); line(570, 575, 720, 575) })
    },
    ColoringPage("butterfly") {
        oval(350, 680, 150, 170, rot = 20)                 // нижние крылья
        oval(650, 680, 150, 170, rot = -20)
        oval(310, 360, 190, 220, rot = -25)                // верхние крылья
        oval(690, 360, 190, 220, rot = 25)
        circle(300, 350, 70); circle(700, 350, 70)         // узоры
        circle(350, 690, 50); circle(650, 690, 50)
        curve { m(485, 250); q(430, 120, 360, 110) }; dot(360, 110, 16) // усики
        curve { m(515, 250); q(570, 120, 640, 110) }; dot(640, 110, 16)
        oval(500, 520, 48, 260, inner = {                  // тельце
            line(400, 450, 600, 450); line(400, 550, 600, 550); line(400, 650, 600, 650)
        })
        circle(500, 240, 62)                               // голова
        dot(480, 232, 9); dot(520, 232, 9)
        curve { m(478, 258); q(500, 280, 522, 258) }
    },
    ColoringPage("rocket") {
        star(160, 180, 55); star(850, 260, 45); star(820, 640, 40); star(150, 560, 40)
        path { m(430, 760); q(500, 990, 570, 760) }        // пламя
        poly(380, 560, 250, 760, 380, 720)                 // крылья
        poly(620, 560, 750, 760, 620, 720)
        path(inner = {                                     // корпус
            line(200, 300, 800, 300); line(200, 680, 800, 680)
        }) {
            m(500, 90); cb(700, 250, 660, 600, 620, 760); l(380, 760); cb(340, 600, 300, 250, 500, 90)
        }
        circle(500, 460, 80); circle(500, 460, 52)         // иллюминатор
    },
    ColoringPage("car") {
        line(0, 800, 1000, 800)                            // дорога
        circle(860, 150, 75)                               // солнце
        cloud(250, 170)
        poly(300, 530, 370, 360, 640, 360, 730, 530)       // кабина
        rect(120, 520, 880, 720, radius = 50)              // кузов
        poly(385, 385, 490, 385, 490, 500, 335, 500)       // окна
        poly(520, 385, 625, 385, 690, 500, 520, 500)
        line(540, 570, 590, 570)                           // ручка
        oval(850, 580, 22, 30)                             // фара
        circle(300, 720, 95); circle(300, 720, 45)         // колёса
        circle(700, 720, 95); circle(700, 720, 45)
    },
    ColoringPage("balloons") {
        cloud(150, 820); cloud(840, 780)
        curve { m(300, 550); q(350, 750, 500, 960) }       // ниточки
        curve { m(700, 550); q(650, 750, 500, 960) }
        curve { m(500, 490); q(520, 700, 500, 960) }
        poly(300, 510, 278, 550, 322, 550); oval(300, 330, 150, 190)
        poly(700, 510, 678, 550, 722, 550); oval(700, 330, 150, 190)
        poly(500, 450, 478, 490, 522, 490); oval(500, 260, 160, 200)
    },
    ColoringPage("mushroom") {
        curve { m(0, 860); q(500, 800, 1000, 860) }        // трава
        cloud(820, 170)
        path { m(400, 520); q(360, 760, 380, 870); l(620, 870); q(640, 760, 600, 520) } // ножка
        path(inner = {                                     // шляпка с пятнышками
            circle(350, 350, 60); circle(560, 290, 70); circle(700, 430, 50); circle(230, 480, 40)
        }) {
            m(120, 540); cb(120, 120, 880, 120, 880, 540); q(500, 620, 120, 540)
        }
    },
    ColoringPage("icecream") {
        star(160, 200, 50); star(840, 260, 40); star(820, 760, 45); star(170, 720, 35)
        path(inner = {                                     // вафельный рожок
            for (d in listOf(-240, -160, -80, 0, 80, 160, 240)) {
                line(500 + d - 300, 400, 500 + d + 300, 1000)
                line(500 + d + 300, 400, 500 + d - 300, 1000)
            }
        }) { m(370, 520); l(630, 520); l(500, 940) }
        oval(500, 490, 180, 100)                           // нижний шарик
        circle(500, 330, 130)                              // верхний шарик
        dot(455, 330, 13); dot(545, 330, 13)               // мордочка
        curve { m(460, 375); q(500, 410, 540, 375) }
        circle(500, 175, 42)                               // вишенка
        curve { m(500, 135); q(515, 80, 565, 65) }
    },
    ColoringPage("cupcake") {
        circle(150, 160, 55); circle(860, 200, 40)         // шарики-конфетти
        path(inner = {                                     // формочка в полоску
            for (x in listOf(380, 440, 500, 560, 620)) line(x, 560, x + (x - 500) / 6, 940)
        }) { m(290, 600); l(710, 600); l(640, 900); l(360, 900) }
        oval(500, 570, 240, 80)                            // крем
        oval(500, 480, 190, 80)
        oval(500, 395, 130, 70)
        circle(500, 305, 45)                               // вишенка
        curve { m(500, 262); q(520, 200, 570, 190) }
        line(420, 470, 440, 500); line(560, 460, 545, 495); line(480, 560, 500, 585) // посыпка
    },
    ColoringPage("robot") {
        line(0, 930, 1000, 930)
        line(500, 170, 500, 105); circle(500, 85, 26)      // антенна
        rect(330, 230, 365, 310, radius = 10)              // ушки
        rect(635, 230, 670, 310, radius = 10)
        rect(360, 170, 640, 370, radius = 40)              // голова
        circle(440, 260, 40); dot(440, 260, 16)
        circle(560, 260, 40); dot(560, 260, 16)
        rect(440, 310, 560, 345, radius = 10, inner = {
            line(470, 300, 470, 360); line(500, 300, 500, 360); line(530, 300, 530, 360)
        })
        rect(470, 365, 530, 405)                           // шея
        rect(230, 420, 320, 620, radius = 40)              // руки
        rect(680, 420, 770, 620, radius = 40)
        rect(380, 690, 470, 860, radius = 20)              // ноги
        rect(530, 690, 620, 860, radius = 20)
        rect(320, 400, 680, 700, radius = 40)              // туловище
        rect(400, 450, 600, 600, radius = 20)              // экранчик
        circle(500, 525, 42)
        circle(440, 650, 18); circle(500, 650, 18); circle(560, 650, 18)
        circle(275, 650, 42); circle(725, 650, 42)         // ладошки
        rect(345, 850, 495, 912, radius = 30)              // ботинки
        rect(505, 850, 655, 912, radius = 30)
    },
    ColoringPage("dino") {
        line(0, 880, 1000, 880)                            // земля
        circle(850, 150, 70)                               // солнце
        cloud(200, 140)
        path { m(640, 620); q(850, 640, 960, 480); q(890, 700, 640, 740) } // хвост
        poly(430, 500, 465, 410, 505, 480)                 // гребень
        poly(510, 480, 550, 395, 590, 480)
        poly(595, 495, 640, 415, 670, 520)
        rect(370, 690, 450, 880, radius = 30)              // ноги
        rect(590, 690, 670, 880, radius = 30)
        path { m(360, 610); q(270, 420, 300, 260); l(400, 260); q(400, 420, 480, 560) } // шея
        oval(530, 640, 230, 165, inner = {                 // туловище с пятнышками
            circle(470, 610, 32); circle(570, 690, 26); circle(620, 590, 22)
        })
        oval(330, 230, 110, 80)                            // голова
        dot(305, 205, 15)
        curve { m(250, 262); q(290, 292, 335, 265) }
        dot(240, 220, 6)
    },
    ColoringPage("chick") {
        curve { m(0, 910); q(500, 850, 1000, 910) }        // травка
        circle(150, 150, 70)                               // солнце
        cloud(820, 160)
        circle(500, 600, 230)                              // тельце
        oval(320, 640, 65, 105, rot = 25)                  // крылышки
        oval(680, 640, 65, 105, rot = -25)
        curve { m(500, 372); q(470, 300, 425, 318) }       // хохолок
        curve { m(500, 372); q(525, 290, 570, 305) }
        dot(430, 520, 20); dot(570, 520, 20)               // глазки
        circle(390, 580, 28); circle(610, 580, 28)         // щёчки
        poly(470, 570, 530, 570, 500, 620)                 // клюв
        path {                                             // скорлупка
            m(280, 700); l(340, 650); l(400, 710); l(460, 650); l(520, 710); l(580, 650); l(640, 710)
            l(720, 660); q(730, 900, 500, 915); q(270, 900, 280, 700)
        }
    },
)
