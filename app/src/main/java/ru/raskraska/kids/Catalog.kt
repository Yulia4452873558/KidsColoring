package ru.raskraska.kids

import android.content.Context
import androidx.compose.ui.graphics.Color

/**
 * Тема раскрасок. Картинки темы берутся из двух мест:
 *  1) папка app/src/main/assets/coloring/<id>/ — сюда кладите сгенерированные картинки
 *     (png, jpg или webp; порядок — по имени файла, поэтому удобно называть 001.png, 002.png…);
 *  2) встроенные картинки, нарисованные кодом ([builtIn] — их id из VectorPages).
 * Тема без единой картинки на главном экране не показывается.
 */
class ColoringTheme(
    val id: String,
    val title: String,
    val emoji: String,
    val color: Color,
    val builtIn: List<String> = emptyList(),
)

val Themes: List<ColoringTheme> = listOf(
    ColoringTheme("animals", "Животные", "🐱", Color(0xFFFFA726), listOf("cat")),
    ColoringTheme("farm", "Ферма", "🐮", Color(0xFF8D6E63), listOf("chick")),
    ColoringTheme("sea", "Морской мир", "🐠", Color(0xFF29B6F6), listOf("fish")),
    ColoringTheme("dinosaurs", "Динозавры", "🦕", Color(0xFF66BB6A), listOf("dino")),
    ColoringTheme("transport", "Транспорт", "🚗", Color(0xFFEF5350), listOf("car")),
    ColoringTheme("space", "Космос", "🚀", Color(0xFF5C6BC0), listOf("rocket")),
    ColoringTheme("fairy", "Сказки", "👑", Color(0xFFEC407A), listOf("house")),
    ColoringTheme("unicorns", "Единороги", "🦄", Color(0xFFBA68C8)),
    ColoringTheme("food", "Вкусняшки", "🍦", Color(0xFFFF7043), listOf("icecream", "cupcake")),
    ColoringTheme("nature", "Цветы и природа", "🌸", Color(0xFF9CCC65), listOf("flower", "butterfly", "mushroom")),
    ColoringTheme("holidays", "Праздники", "🎈", Color(0xFFFFCA28), listOf("balloons")),
    ColoringTheme("robots", "Роботы", "🤖", Color(0xFF78909C), listOf("robot")),
    ColoringTheme("insects", "Букашки", "🐞", Color(0xFF26A69A)),
    ColoringTheme("letters", "Буквы и цифры", "🔤", Color(0xFF7E57C2)),
)

object Catalog {
    private val cache = HashMap<String, List<ColoringPage>>()
    private val imageExtensions = setOf("png", "jpg", "jpeg", "webp")

    fun pages(context: Context, theme: ColoringTheme): List<ColoringPage> = synchronized(cache) {
        cache.getOrPut(theme.id) { load(context, theme) }
    }

    private fun load(context: Context, theme: ColoringTheme): List<ColoringPage> {
        val folder = "coloring/${theme.id}"
        val files = try {
            context.assets.list(folder)?.toList()
        } catch (e: Exception) {
            null
        } ?: emptyList()
        val fromAssets = files
            .filter { it.substringAfterLast('.', "").lowercase() in imageExtensions }
            .sorted()
            .map { name ->
                ColoringPage(id = "${theme.id}/${name.substringBeforeLast('.')}", assetPath = "$folder/$name")
            }
        val builtIn = theme.builtIn.mapNotNull { id -> VectorPages.firstOrNull { it.id == id } }
        return builtIn + fromAssets
    }
}

/** «1 картинка», «3 картинки», «12 картинок». */
fun picturesLabel(n: Int): String {
    val m10 = n % 10
    val m100 = n % 100
    val word = when {
        m10 == 1 && m100 != 11 -> "картинка"
        m10 in 2..4 && m100 !in 12..14 -> "картинки"
        else -> "картинок"
    }
    return "$n $word"
}
