package ru.raskraska.kids

import android.app.Application
import com.yandex.mobile.ads.common.YandexAds
import ru.raskraska.kids.ads.AdsManager

class KidsApp : Application() {
    lateinit var ads: AdsManager
        private set

    override fun onCreate() {
        super.onCreate()
        ads = AdsManager(this)
        // Приложение детское: просим рекламу без персонализации (требование COPPA и магазинов)
        YandexAds.setAgeRestricted(true)
        YandexAds.initialize(this) {
            // SDK готов — заранее загружаем межстраничную рекламу, чтобы она показывалась сразу
            ads.preload()
        }
    }
}
