plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "ru.raskraska.kids"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "ru.raskraska.kids"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "1.1"
    }

    signingConfigs {
        create("release") {
            storeFile = file("key_store")
            storePassword = "ItQVaPfTX8fKMi4x3U0jYqrgob2qQncvNg1"
            keyAlias = "key_paint"
            keyPassword = "ItQVaPfTX8fKMi4x3U0jYqrgob2qQncvNg1"
        }
    }

    // Две сборки под разные магазины: у каждой свои рекламные блоки Яндекса.
    // Нужная выбирается в Android Studio: View → Tool Windows → Build Variants.
    flavorDimensions += "store"
    productFlavors {
        create("rustore") {
            dimension = "store"
            buildConfigField("String", "AD_UNIT_BANNER", "\"R-M-20239659-1\"")
            buildConfigField("String", "AD_UNIT_INTERSTITIAL", "\"R-M-20239659-3\"")
            buildConfigField("String", "AD_UNIT_REWARDED", "\"R-M-20239659-2\"")
        }
        create("huawei") {
            dimension = "store"
            buildConfigField("String", "AD_UNIT_BANNER", "\"R-M-20239673-1\"")
            buildConfigField("String", "AD_UNIT_INTERSTITIAL", "\"R-M-20239673-2\"")
            buildConfigField("String", "AD_UNIT_REWARDED", "\"R-M-20239673-3\"")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.02.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("com.yandex.android:mobileads:8.6.0")
}
