package com.b096.dramarush5.ui.language

import android.os.Bundle
import com.b096.dramarush5.ads.wrapper.native.NativeAdPreloadManager
import com.b096.dramarush5.ads.wrapper.native.NativeAdsWrapper
import com.b096.dramarush5.ads.wrapper.native.NativePlacement
import com.b096.dramarush5.app.PreferenceData.isUfo
import com.b096.dramarush5.databinding.ActivityLanguageOpenBinding
import com.b096.dramarush5.firebase.Analytics

class Language2Activity : LanguageOpenActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isUfo()) {
            Analytics.track("ufo_language_dup")
        }
    }

    private val nativeAdsWrapper by lazy {
        NativeAdsWrapper(
            activity = this,
            config = NativePlacement.LANGUAGE_2,
            lifecycleOwner = this,
            adContainer = { binding.flNativeAd },
            shimmerView = {
                if (it.isNativeSmall()) {
                    binding.shimmerNoMedia.shimmerContainerNative
                } else if (it.isNativeLeftMedia()) {
                    binding.shimmerMediaLeft.shimmerContainerNative
                } else {
                    binding.shimmerMediaBottom.shimmerContainerNative
                }
            }
        )
    }

    override fun ActivityLanguageOpenBinding.setData() {
        with(nativeAdsWrapper) {
            setupNativeAd("native_lang")
            requestAds()
        }
        currentLang.value?.let {
            languageAdapter.selectItem(it)
        }
        preloadNativeOb()
        Analytics.track("LFOScr3_Show")
    }

    fun preloadNativeOb() {
        launchMain {
            NativeAdPreloadManager.preloadAd(
                this@Language2Activity,
                placement = NativePlacement.ONBOARDING_4,
                buffer = 1,
            )
            NativeAdPreloadManager.preloadAd(
                this@Language2Activity,
                placement = NativePlacement.ONBOARDING_3,
                buffer = 1,
            )
        }
    }


    override fun onResume() {
        super.onResume()
    }
}
