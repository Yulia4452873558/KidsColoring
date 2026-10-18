package ru.raskraska.kids

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import ru.raskraska.kids.ads.findActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Системная заставка (иконка на голубом фоне) — до первой отрисовки,
        // дальше её сменяет наша анимированная SplashScreen.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Полный экран: ребёнок не заденет случайно системные кнопки
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        Music.init(this)
        setContent { MaterialTheme { App() } }
    }

    override fun onResume() {
        super.onResume()
        Music.onResume(this)
    }

    override fun onPause() {
        Music.onPause()
        super.onPause()
    }
}

val AppBackground = Color(0xFFDFF3FF)
val Violet = Color(0xFF7E57C2)
val VioletLight = Color(0xFFD1C4E9)

/** Какой экран сейчас открыт. */
private data class Route(
    val splash: Boolean,
    val theme: ColoringTheme? = null,
    val page: ColoringPage? = null,
)

@Composable
fun App() {
    val context = LocalContext.current.applicationContext
    val activity = LocalContext.current.findActivity()
    var splashDone by rememberSaveable { mutableStateOf(false) }
    var themeId by rememberSaveable { mutableStateOf<String?>(null) }
    var pageId by rememberSaveable { mutableStateOf<String?>(null) }
    // выход из категории в главное меню (кнопкой «назад» и системной «назад»):
    // каждый раз показываем межстраничную рекламу
    val leaveCategory: () -> Unit = {
        themeId = null
        (context as? KidsApp)?.ads?.showInterstitial(activity)
    }

    val theme = Themes.firstOrNull { it.id == themeId }
    val page = if (theme != null && pageId != null) {
        Catalog.pages(context, theme).firstOrNull { it.id == pageId }
    } else {
        null
    }
    val route = Route(splash = !splashDone, theme = theme, page = page)

    BackHandler(enabled = splashDone && theme != null) {
        if (pageId != null) pageId = null else leaveCategory()
    }

    Crossfade(targetState = route, animationSpec = tween(450), label = "screens") { r ->
        if (r.splash) {
            SplashScreen(onFinished = { splashDone = true })
        } else {
            // Яркий фон на экранах выбора, спокойный — на экране рисования
            when {
                r.theme == null -> HomeBackground()
                r.page == null -> BrightBackground(themeBackground(r.theme))
                else -> ThemeSceneBackground(sceneFor(r.theme.id))
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
            ) {
                when {
                    r.theme == null -> ThemesScreen(onOpen = { themeId = it.id })
                    r.page == null -> PagesScreen(
                        theme = r.theme,
                        onBack = leaveCategory,
                        onOpen = { pageId = it.id }
                    )
                    else -> CompositionLocalProvider(LocalAccent provides sceneFor(r.theme.id).accent) {
                        ColoringScreen(page = r.page, onBack = { pageId = null })
                    }
                }
            }
        }
    }
}
