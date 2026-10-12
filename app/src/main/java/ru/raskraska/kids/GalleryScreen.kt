package ru.raskraska.kids

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Rect
import android.util.LruCache
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ---------- Главный экран: выбор темы ----------

@Composable
fun ThemesScreen(onOpen: (ColoringTheme) -> Unit) {
    val context = LocalContext.current.applicationContext
    val themes = remember {
        Themes.map { it to Catalog.pages(context, it).size }.filter { it.second > 0 }
    }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🎨", fontSize = 32.sp)
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Что будем раскрашивать?",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                style = TextStyle(shadow = Shadow(Color(0x66000000), Offset(0f, 4f), 8f))
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(themes, key = { it.first.id }) { (theme, count) ->
                ThemeCard(theme, count) { onOpen(theme) }
            }
        }
    }
}

@Composable
private fun ThemeCard(theme: ColoringTheme, count: Int, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, label = "press")
    val shape = RoundedCornerShape(28.dp)
    Box(
        Modifier
            .aspectRatio(1.2f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(8.dp, shape)
            .clip(shape)
            .background(Brush.linearGradient(listOf(lerp(theme.color, Color.White, 0.35f), theme.color)))
            .border(4.dp, Color.White, shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        ComposeCanvas(Modifier.fillMaxSize()) {
            drawCircle(
                Color.White.copy(alpha = 0.2f), size.minDimension * 0.5f,
                Offset(size.width * 0.92f, size.height * 0.08f)
            )
            drawCircle(
                Color.White.copy(alpha = 0.14f), size.minDimension * 0.3f,
                Offset(size.width * 0.04f, size.height * 0.96f)
            )
        }
        Box(
            Modifier
                .align(Alignment.Center)
                .offset(y = (-16).dp)
                .size(84.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Text(theme.emoji, fontSize = 50.sp)
        }
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = theme.title,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(shadow = Shadow(Color(0x66000000), Offset(0f, 3f), 6f))
            )
            Text(
                text = picturesLabel(count),
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ---------- Экран темы: картинки ----------

@Composable
fun PagesScreen(theme: ColoringTheme, onBack: () -> Unit, onOpen: (ColoringPage) -> Unit) {
    val context = LocalContext.current.applicationContext
    val pages = remember(theme.id) { Catalog.pages(context, theme) }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.padding(start = 16.dp, top = 10.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text("⬅", fontSize = 24.sp, color = Violet)
            }
            Spacer(Modifier.width(14.dp))
            Text(theme.emoji, fontSize = 30.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                theme.title,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                style = TextStyle(shadow = Shadow(Color(0x66000000), Offset(0f, 4f), 8f))
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(140.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(pages, key = { it.id }) { page ->
                Thumbnail(page, theme.color) { onOpen(page) }
            }
        }
    }
}

@Composable
private fun Thumbnail(page: ColoringPage, accent: Color, onClick: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val revision = Storage.revision.intValue
    val key = "${page.id}@$revision"
    val image by produceState<ImageBitmap?>(Thumbnails.cache.get(key), key) {
        if (value == null) {
            value = withContext(Dispatchers.Default) {
                makeThumbnail(context, page).also { Thumbnails.cache.put(key, it) }
            }
        }
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, label = "press")
    val shape = RoundedCornerShape(22.dp)
    Box(
        Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(8.dp, shape)
            .clip(shape)
            .background(Color.White)
            .border(5.dp, Color.White, shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = image
        if (bitmap != null) {
            Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.fillMaxSize().padding(6.dp))
        } else {
            CircularProgressIndicator(color = accent, modifier = Modifier.size(28.dp))
        }
    }
}

private object Thumbnails {
    val cache = LruCache<String, ImageBitmap>(120)
}

private const val THUMB = 360

private fun makeThumbnail(context: Context, page: ColoringPage): ImageBitmap {
    val result = Bitmap.createBitmap(THUMB, THUMB, Bitmap.Config.ARGB_8888)
    result.eraseColor(AndroidColor.WHITE)
    val canvas = Canvas(result)
    val saved = Storage.load(context, page.id)
    if (saved != null) {
        canvas.drawBitmap(saved, null, Rect(0, 0, THUMB, THUMB), Paint(Paint.FILTER_BITMAP_FLAG))
        saved.recycle()
    }
    val outline = renderOutline(context, page, THUMB)
    canvas.drawBitmap(outline, 0f, 0f, null)
    outline.recycle()
    return result.asImageBitmap()
}
