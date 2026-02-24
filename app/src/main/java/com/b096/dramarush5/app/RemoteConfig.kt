package com.b096.dramarush5.app

import android.util.Base64
import android.util.Log
import com.b096.dramarush5.BuildConfig
import com.b096.dramarush5.ads.model.AdBannerConfig
import com.b096.dramarush5.ads.model.AdInterConfig
import com.b096.dramarush5.ads.model.AdNativeConfig
import com.b096.dramarush5.ads.model.AdRewardConfig
import com.b096.dramarush5.ads.model.AppOpenAdConfig
import com.b096.dramarush5.ads.model.RatingConfig
import com.b096.dramarush5.ads.model.config.LanguageLoading
import com.b096.dramarush5.ads.model.config.NativeFullConfig
import com.b096.dramarush5.ads.model.config.OnboardingScreen
import com.b096.dramarush5.ads.model.config.FeatureScreenConfig
import com.b096.dramarush5.ads.model.splash.AdSplashConfig
import com.b096.dramarush5.ads.model.splash.SplashTimeout
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

val remoteConfig get() = RemoteConfig.Instance
data class TimeSlot(val hour: Int, val minute: Int)

class RemoteConfig() : KoinComponent {
    companion object {
        val Instance: RemoteConfig by lazy {
            RemoteConfig()
        }
    }

    private var defaultTime = listOf(
        Pair(8, 0),
        Pair(12, 0),
        Pair(15, 0),
        Pair(19, 0),
        Pair(20, 0),
        Pair(21, 0)
    )

    private val sharedPreference: SharedPreference by inject()
    var inAppUpdate by sharedPreference.string("inAppUpdate", "off_pop_up_update")
    var timesShowUpdate by sharedPreference.int("timesShowUpdate", 3)
    fun syncRemote(remoteConfig: FirebaseRemoteConfig) {
        remoteConfig.remoteObject<String>("inAppUpdate", "off_pop_up_update")
        remoteConfig.remoteObject<Int>("timesShowUpdate", 3)
        remoteConfig.remoteObject<Boolean>("enable_ads", true)
        remoteConfig.remoteObject<Boolean>("meta_ctr_low", false)
        remoteConfig.remoteObject("A101_config_1", AppOpenAdConfig.appOpenAdConfig)
        remoteConfig.remoteObject("N101_config_1", AdNativeConfig.nativeSplashConfig)
        remoteConfig.remoteObject("splash_timeout", SplashTimeout.splashTimeout)
        remoteConfig.remoteObject("N102_config_1", AdNativeConfig.nativeLangLoading)
        remoteConfig.remoteObject("language_loading_config", LanguageLoading.languageLoading)
        remoteConfig.remoteObject("N103_config_1", AdNativeConfig.nativeLanguage1)
        remoteConfig.remoteObject("N104_config_1", AdNativeConfig.nativeLanguage2)
        remoteConfig.remoteObject("N105_config_1", AdNativeConfig.nativeOnboarding1)
        remoteConfig.remoteObject("N105_config_2", AdNativeConfig.nativeOnboarding2)
        remoteConfig.remoteObject("N105_config_3", AdNativeConfig.nativeOnboarding3)
        remoteConfig.remoteObject("N105_config_4", AdNativeConfig.nativeOnboarding4)
        remoteConfig.remoteObject("N107_config_1", AdNativeConfig.nativeOBFull1)
        remoteConfig.remoteObject("N107_config_2", NativeFullConfig.configNativeFull1)
        remoteConfig.remoteObject("N108_config_1", AdNativeConfig.nativeOBFull2)
        remoteConfig.remoteObject("N108_config_2", NativeFullConfig.configNativeFull2)
        remoteConfig.remoteObject("N109_config_1", AdNativeConfig.nativeFeature)
        remoteConfig.remoteObject("N110_config_1", AdNativeConfig.nativeFeatureDup)
        remoteConfig.remoteObject("N111_config_1", AdNativeConfig.nativeHome)
        remoteConfig.remoteObject("N112_config_1", AdNativeConfig.nativePopup)
        remoteConfig.remoteObject("I101_config_1", AdSplashConfig.adSplashConfig)
        remoteConfig.remoteObject("I102_config_1", AdInterConfig.adInterConfig)
        remoteConfig.remoteObject("I103_config_1", AdInterConfig.adInterPaywall)
        remoteConfig.remoteObject("B101_config_1", AdBannerConfig.adBannerConfig)
        remoteConfig.remoteObject<Long>("time_reload_banner_ms", 30000L)
        remoteConfig.remoteObject("R101_config_1", AdRewardConfig.rewardedDownload)
        remoteConfig.remoteObject("onboarding_screen_config", OnboardingScreen.defaultOnboardingScreen)
        remoteConfig.remoteObject("select_screen_config", FeatureScreenConfig.defaultSelectScreen)
        timeNotiLock =
            notiTimesRemoteConfig(remoteConfig.getString("time_noti_reminder"), defaultTime)
        remoteConfig.remoteObject<RatingConfig>("RATING_CONFIG", RatingConfig.defaultRateConfig)
    }

    private fun notiTimesRemoteConfig(
        rawValue: String,
        default: List<Pair<Int, Int>> = defaultTime
    ): List<Pair<Int, Int>> {
        if (rawValue.isBlank()) return default
        val result = rawValue.split(",")
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { timeStr ->
                val parts = timeStr.split(":")
                if (parts.size != 2) return@mapNotNull null
                val hour = parts[0].toIntOrNull()
                val minute = parts[1].toIntOrNull()
                if (hour == null || minute == null) return@mapNotNull null
                if (hour !in 0..23 || minute !in 0..59) return@mapNotNull null
                hour to minute
            }
            .distinct()
            .sortedWith(compareBy<Pair<Int, Int>> { it.first }.thenBy { it.second })
            .toList()

        return result.ifEmpty { default }
    }

    var timeNotiLock by dataObject("time_noti_reminder", defaultTime)
        private set
    val ratingConfig by dataObject("RATING_CONFIG", RatingConfig.defaultRateConfig)
    val adEnable by dataObject("enable_ads", true)
    val metaCtrLow by dataObject("meta_ctr_low", false)
    val appOpenAdConfig by dataObject("A101_config_1", AppOpenAdConfig.appOpenAdConfig){
        enable = adEnable && enable
    }
    val splashTimeout by dataObject("splash_timeout", SplashTimeout.splashTimeout)
    val languageLoading by dataObject("language_loading_config", LanguageLoading.languageLoading)
    val nativeSplashConfig by dataObject("N101_config_1", AdNativeConfig.nativeSplashConfig) {
        enable = adEnable && enable
    }
    val nativeLangWaitingConfig by dataObject("N102_config_1", AdNativeConfig.nativeLangLoading) {
        enable = adEnable && enable
    }
    val nativeLang1Config by dataObject("N103_config_1", AdNativeConfig.nativeLanguage1) {
        enable = adEnable && enable
    }
    val nativeLang2Config by dataObject("N104_config_1", AdNativeConfig.nativeLanguage2) {
        enable = adEnable && enable
    }
    val nativeOnboarding1Config by dataObject("N105_config_1", AdNativeConfig.nativeOnboarding1) {
        enable = adEnable && enable
    }
    val nativeOnboarding2Config by dataObject("N105_config_2", AdNativeConfig.nativeOnboarding2) {
        enable = adEnable && enable
    }
    val nativeOnboarding3Config by dataObject("N105_config_3", AdNativeConfig.nativeOnboarding3) {
        enable = adEnable && enable
    }
    val nativeOnboarding4Config by dataObject("N105_config_4", AdNativeConfig.nativeOnboarding4) {
        enable = adEnable && enable
    }
    val nativeObFull1Config by dataObject("N107_config_1", AdNativeConfig.nativeOBFull1) {
        enable = adEnable && enable
    }
    val nativeObFull2Config by dataObject("N108_config_1", AdNativeConfig.nativeOBFull2) {
        enable = adEnable && enable
    }
    val nativeSelectConfig by dataObject("N109_config_1", AdNativeConfig.nativeFeature) {
        enable = adEnable && enable
    }
    val nativeSelectDupConfig by dataObject("N110_config_1", AdNativeConfig.nativeFeatureDup) {
        enable = adEnable && enable
    }
    val nativeHomeConfig by dataObject("N111_config_1", AdNativeConfig.nativeHome) {
        enable = adEnable && enable
    }
    val nativePopupConfig by dataObject("N112_config_1", AdNativeConfig.nativePopup) {
        enable = adEnable && enable
    }
    val bannerAllConfig by dataObject("B101_config_1", AdBannerConfig.adBannerConfig) {
        enable = adEnable && enable
    }
    val timeReloadBannerMs by dataObject("time_reload_banner_ms", 30000L)
    val splashAdConfig by dataObject("I101_config_1", AdSplashConfig.adSplashConfig) {
        enable = adEnable && enable
    }
    val interAllConfig by dataObject("I102_config_1", AdInterConfig.adInterConfig) {
        enable = adEnable && enable
    }
    val interPaywallConfig by dataObject("I103_config_1", AdInterConfig.adInterPaywall) {
        enable = adEnable && enable
    }
    val configNativeFull1 by dataObject(
        "N107_config_2",
        NativeFullConfig.configNativeFull1
    )
    val configNativeFull2 by dataObject(
        "N108_config_2",
        NativeFullConfig.configNativeFull2
    )
    val rewardedAllConfig by dataObject("R101_config_1", AdRewardConfig.rewardedDownload) {
        enable = adEnable && enable
    }
    val onboardingScreenConfig by dataObject(
        "onboarding_screen_config",
        OnboardingScreen.defaultOnboardingScreen
    )
    val featureScreenConfig by dataObject(
        "select_screen_config",
        FeatureScreenConfig.defaultSelectScreen
    )


    private var remoteConfig: FirebaseRemoteConfig

    init {
        val configSettings = remoteConfigSettings {
            setFetchTimeoutInSeconds(30L)
            setMinimumFetchIntervalInSeconds(if (BuildConfig.build_debug) 0L else 3600L)
        }
        Firebase.remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
        }
        this.remoteConfig = Firebase.remoteConfig
    }

    suspend fun setupRemoteConfig(isDebug: Boolean) {
        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setFetchTimeoutInSeconds(30L)
            .setMinimumFetchIntervalInSeconds(if (isDebug) 0L else 3600L)
            .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        val isSuccess = runCatching {
            firebaseRemoteConfig.fetchAndActivate().await()
        }.getOrElse {
            false
        }
        if (isSuccess) {
            syncRemote(firebaseRemoteConfig)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> FirebaseRemoteConfig.readAsType(
        key: String,
        default: T
    ): T {
        val rcVal = getValue(key)
        val raw = rcVal.asString().trim()
        Log.d("FetchRemote", "Key: $key\nFetch: $rcVal")
        val result: T = when (T::class) {
            Boolean::class -> rcVal.asBoolean() as T
            Int::class -> rcVal.asLong().toInt() as T
            Long::class -> rcVal.asLong() as T
            Float::class -> rcVal.asDouble().toFloat() as T
            Double::class -> rcVal.asDouble() as T
            String::class -> raw as T
            else -> when {
                T::class.java.isEnum -> {
                    if (raw.isBlank()) default
                    else runCatching {
                        @Suppress("UNCHECKED_CAST")
                        java.lang.Enum.valueOf(
                            T::class.java as Class<out Enum<*>>,
                            raw
                        ) as T
                    }.getOrDefault(default)
                }

                (default is ByteArray) || (T::class == ByteArray::class) -> {
                    if (raw.isBlank()) default
                    else runCatching {
                        Base64.decode(raw, Base64.DEFAULT) as T
                    }.getOrDefault(default)
                }

                (default is Set<*>) && (default.all { it is String }) -> {
                    if (raw.isBlank()) default
                    else {
                        val jsonSet = runCatching {
                            val listType =
                                object : TypeToken<List<String>>() {}.type
                            val list = Gson().fromJson<List<String>>(raw, listType)
                            list.toSet()
                        }.getOrNull()

                        @Suppress("UNCHECKED_CAST")
                        (jsonSet ?: raw.split(',')
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .toSet()
                            .ifEmpty { default as Set<String> }) as T
                    }
                }

                raw.isBlank() -> default
                else -> {
                    runCatching {
                        Gson().fromJson<T>(raw, T::class.java)
                    }.getOrDefault(default)
                }
            }
        }

        //Log.d("FetchRemote", "Key: $key\nValue: $result")
        return result
    }

    private inline fun <reified T : Any> dataObject(
        key: String,
        default: T,
        noinline block: (T.() -> Unit)? = null,
    ): ReadWriteProperty<Any?, T> {
        return object : ReadWriteProperty<Any?, T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T {
                val result = sharedPreference.readAny(key, default)
                block?.invoke(result)
                return result
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                sharedPreference.writeAny(key, value)
            }
        }
    }

    private inline fun <reified T> FirebaseRemoteConfig.remoteObject(
        key: String,
        default: T,
        block: T.() -> Unit = {}
    ): T {
        val value = readAsType<T>(key, default).apply(block)
        sharedPreference.writeAny(key, value)
        return value
    }
}