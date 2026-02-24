package com.b096.dramarush5.ui.language

import android.os.Bundle
import com.b096.dramarush5.ads.wrapper.native.NativeAdPreloadManager
import com.b096.dramarush5.ads.wrapper.native.NativeAdsWrapper
import com.b096.dramarush5.ads.wrapper.native.NativePlacement
import com.b096.dramarush5.app.PreferenceData.isUfo
import com.b096.dramarush5.databinding.ActivityLanguageOpenBinding
import com.b096.dramarush5.firebase.Analytics
import kotlin.getValue

class Language1Activity : LanguageOpenActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isUfo()) {
            Analytics.track("ufo_language")
        }
    }


    private val nativeAdsWrapper by lazy {
        NativeAdsWrapper(
            activity = this,
            config = NativePlacement.LANGUAGE_1,
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
        preloadNativeOb()
        Analytics.track("LFOScr2_Show")
    }

    fun preloadNativeOb() {
        launchMain {
            NativeAdPreloadManager.preloadAd(
                this@Language1Activity,
                placement = NativePlacement.ONBOARDING_1,
                buffer = 1,
            )
            NativeAdPreloadManager.preloadAd(
                this@Language1Activity,
                placement = NativePlacement.ONBOARDING_2,
                buffer = 1,
            )
        }
    }

    override fun onResume() {
        super.onResume()
    }

}
