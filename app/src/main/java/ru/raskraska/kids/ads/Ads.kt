package ru.raskraska.kids.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader
import ru.raskraska.kids.BuildConfig

/**
 * Рекламные блоки: в отладочной сборке — демо-блоки Яндекса (за них не будет бана),
 * в release — настоящие блоки выбранной сборки (rustore или huawei, см. app/build.gradle.kts).
 */
object AdUnits {
    val banner: String = if (BuildConfig.DEBUG) "demo-banner-yandex" else BuildConfig.AD_UNIT_BANNER
    val interstitial: String = if (BuildConfig.DEBUG) "demo-interstitial-yandex" else BuildConfig.AD_UNIT_INTERSTITIAL
    val rewarded: String = if (BuildConfig.DEBUG) "demo-rewarded-yandex" else BuildConfig.AD_UNIT_REWARDED
}

private const val TAG = "KidsAds"
private const val RETRY_MS = 30_000L


/**
 * Держит заранее загруженную межстраничную рекламу и рекламу с вознаграждением.
 * Любой показ всегда заканчивается вызовом колбэка, даже если рекламы нет,
 * поэтому приложение никогда не «зависает» в ожидании рекламы.
 */
class AdsManager(context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private val interstitialLoader = InterstitialAdLoader(context)
    private val rewardedLoader = RewardedAdLoader(context)

    private var interstitial: InterstitialAd? = null
    private var interstitialLoading = false
    private var rewarded: RewardedAd? = null
    private var rewardedLoading = false

    /** До какого момента «шаг назад» работает без рекламы (после досмотренной рекламы). */
    internal var freeUndoUntil = 0L

    /** true, пока реклама на весь экран открыта. */
    var isShowing = false
        private set

    /** Можно ли сейчас показать рекламу с вознаграждением (для кнопки «посмотреть рекламу»). */
    var rewardedReady by mutableStateOf(false)
        private set

    fun preload() {
        loadInterstitial()
        loadRewarded()
    }

    // --- межстраничная ---------------------------------------------------------

    private fun loadInterstitial() {
        if (interstitial != null || interstitialLoading) return
        interstitialLoading = true
        interstitialLoader.loadAd(
            AdRequest.Builder(AdUnits.interstitial).build(),
            object : InterstitialAdLoadListener {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialLoading = false
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    interstitialLoading = false
                    Log.w(TAG, "interstitial failed: ${error.description}")
                    handler.postDelayed(::loadInterstitial, RETRY_MS)
                }
            },
        )
    }

    /** Показывает межстраничную рекламу, если она загружена. [onClosed] вызывается всегда. */
    fun showInterstitial(activity: Activity?, onClosed: () -> Unit = {}) {
        val ad = interstitial
        if (ad == null || activity == null || activity.isFinishing || isShowing) {
            loadInterstitial()
            onClosed()
            return
        }
        interstitial = null
        isShowing = true
        var finished = false
        fun finish() {
            if (finished) return
            finished = true
            isShowing = false
            ad.setAdEventListener(null)
            loadInterstitial()
            onClosed()
        }
        ad.setAdEventListener(object : InterstitialAdEventListener {
            override fun onAdShown() = Unit
            override fun onAdFailedToShow(adError: AdError) {
                Log.w(TAG, "interstitial show failed: ${adError.description}")
                finish()
            }
            override fun onAdDismissed() = finish()
            override fun onAdClicked() = Unit
            override fun onAdImpression(impressionData: ImpressionData?) = Unit
        })
        ad.show(activity)
    }

    // --- с вознаграждением ---------------------------------------------------------

    private fun loadRewarded() {
        if (rewarded != null || rewardedLoading) return
        rewardedLoading = true
        rewardedLoader.loadAd(
            AdRequest.Builder(AdUnits.rewarded).build(),
            object : RewardedAdLoadListener {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedLoading = false
                    rewarded = ad
                    rewardedReady = true
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    rewardedLoading = false
                    Log.w(TAG, "rewarded failed: ${error.description}")
                    handler.postDelayed(::loadRewarded, RETRY_MS)
                }
            },
        )
    }

    /**
     * Показывает рекламу с вознаграждением. [onReward] — если награда заработана,
     * [onClosed] — всегда, когда реклама закрыта. Возвращает false, если рекламы нет.
     */
    fun showRewarded(activity: Activity?, onReward: () -> Unit, onClosed: () -> Unit = {}): Boolean {
        val ad = rewarded
        if (ad == null || activity == null || activity.isFinishing || isShowing) {
            loadRewarded()
            return false
        }
        rewarded = null
        rewardedReady = false
        isShowing = true
        var finished = false
        fun finish() {
            if (finished) return
            finished = true
            isShowing = false
            ad.setAdEventListener(null)
            loadRewarded()
            onClosed()
        }
        ad.setAdEventListener(object : RewardedAdEventListener {
            override fun onAdShown() = Unit
            override fun onAdFailedToShow(adError: AdError) {
                Log.w(TAG, "rewarded show failed: ${adError.description}")
                finish()
            }
            override fun onAdDismissed() = finish()
            override fun onAdClicked() = Unit
            override fun onAdImpression(impressionData: ImpressionData?) = Unit
            override fun onRewarded(reward: Reward) = onReward()
        })
        ad.show(activity)
        return true
    }
}

/**
 * Выполняет [action] после рекламы с вознаграждением: если ребёнок досмотрел рекламу —
 * действие выполняется; если закрыл раньше — нет. Если рекламы сейчас нет (не загрузилась,
 * нет интернета), действие выполняется сразу, чтобы приложение не «ломалось».
 */
fun AdsManager?.afterRewarded(activity: Activity?, action: () -> Unit) {
    if (this == null) {
        action()
        return
    }
    var earned = false
    var closed = false
    var done = false
    fun runOnce() {
        if (!done) {
            done = true
            action()
        }
    }
    val shown = showRewarded(
        activity,
        // награда может прийти и до, и после закрытия рекламы — учитываем оба случая
        onReward = { earned = true; if (closed) runOnce() },
        onClosed = { closed = true; if (earned) runOnce() },
    )
    if (!shown) runOnce()
}

/** Сколько «шаг назад» бесплатен после одной досмотренной рекламы — 5 минут. */
private const val FREE_UNDO_MS = 5 * 60_000L

/**
 * «Шаг назад»: после досмотренной рекламы с вознаграждением отмена 5 минут работает
 * без рекламы, потом снова просит посмотреть рекламу.
 */
fun AdsManager?.undoWithReward(activity: Activity?, action: () -> Unit) {
    if (this == null || SystemClock.elapsedRealtime() < freeUndoUntil) {
        action()
        return
    }
    afterRewarded(activity) {
        freeUndoUntil = SystemClock.elapsedRealtime() + FREE_UNDO_MS
        action()
    }
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
