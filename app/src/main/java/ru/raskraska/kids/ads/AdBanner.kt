package ru.raskraska.kids.ads

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData

/** Высота места под баннер. */
const val BANNER_HEIGHT_DP = 50

private const val RETRY_MS = 15_000L

/**
 * Один баннер на всё время работы приложения. Он не пересоздаётся при переходах между
 * экранами, поэтому на главном экране виден сразу, без повторной загрузки.
 * Если загрузить не удалось (нет интернета) — пробуем снова каждые 15 секунд.
 */
private object BannerHolder {
    private val handler = Handler(Looper.getMainLooper())
    private var view: BannerAdView? = null
    private var owner: Activity? = null
    private var ownerWidth = 0

    fun get(activity: Activity, widthDp: Int): BannerAdView {
        val existing = view
        if (existing != null && owner === activity && ownerWidth == widthDp) return existing
        existing?.destroy()
        owner = activity
        ownerWidth = widthDp
        return BannerAdView(activity).apply {
            setAdSize(BannerAdSize.sticky(activity, widthDp))
            setBannerAdEventListener(object : BannerAdEventListener {
                override fun onAdLoaded() = Unit
                override fun onAdFailedToLoad(error: AdRequestError) {
                    Log.w("KidsAds", "banner failed: ${error.description}")
                    handler.postDelayed({ if (view === this@apply) load(this@apply) }, RETRY_MS)
                }
                override fun onAdClicked() = Unit
                override fun onImpression(impressionData: ImpressionData?) = Unit
            })
            view = this
            load(this)
        }
    }

    private fun load(v: BannerAdView) = v.loadAd(AdRequest.Builder(AdUnits.banner).build())
}

/** Баннер Яндекс Рекламы во всю ширину экрана. */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val activity = LocalContext.current.findActivity() ?: return
    val widthDp = LocalConfiguration.current.screenWidthDp
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = {
                BannerHolder.get(activity, widthDp).also { v -> (v.parent as? ViewGroup)?.removeView(v) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(BANNER_HEIGHT_DP.dp),
        )
    }
}
