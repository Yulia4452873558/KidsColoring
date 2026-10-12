package ru.raskraska.kids

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

/** Хранит незаконченные рисунки внутри приложения и экспортирует готовые в галерею. */
object Storage {
    /** Увеличивается после каждой записи — галерея по нему обновляет миниатюры. */
    val revision = mutableIntStateOf(0)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun file(context: Context, id: String) =
        File(context.filesDir, "page_" + id.replace(Regex("[^A-Za-z0-9_-]"), "_") + ".png")

    fun load(context: Context, id: String): Bitmap? {
        val f = file(context, id)
        if (!f.exists()) return null
        return try {
            BitmapFactory.decodeFile(f.path, BitmapFactory.Options().apply { inMutable = true })
        } catch (e: Exception) {
            null
        }
    }

    /** Вызывать с главного потока: копия снимается сразу, запись идёт в фоне. */
    fun saveProgress(context: Context, id: String, bitmap: Bitmap) {
        val copy = bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: return
        val target = file(context, id)
        scope.launch {
            try {
                val tmp = File(target.path + ".tmp")
                tmp.outputStream().use { copy.compress(Bitmap.CompressFormat.PNG, 100, it) }
                tmp.renameTo(target)
                revision.intValue++
            } catch (e: Exception) {
                // не страшно: рисунок просто не запомнится
            } finally {
                copy.recycle()
            }
        }
    }

    /** Сохраняет картинку в «Картинки/Раскраски». Вызывать из фонового потока. */
    fun exportToGallery(context: Context, bitmap: Bitmap): Boolean {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "raskraska_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= 29) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Raskraski")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val uri = try {
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } catch (e: Exception) {
            null
        }
        if (uri == null) return false
        return try {
            val out = resolver.openOutputStream(uri) ?: return false
            out.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            if (Build.VERSION.SDK_INT >= 29) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            true
        } catch (e: Exception) {
            try {
                resolver.delete(uri, null, null)
            } catch (ignored: Exception) {
            }
            false
        }
    }
}
