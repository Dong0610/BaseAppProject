package com.b096.dramarush5.ui.feature

import android.os.Bundle
import com.b096.dramarush5.ads.wrapper.native.NativePlacement
import com.b096.dramarush5.ads.wrapper.native.createNativeConfig
import com.b096.dramarush5.app.PreferenceData.isUfo
import com.b096.dramarush5.app.remoteConfig
import com.b096.dramarush5.firebase.Analytics

class Feature3Activity : FeatureActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedScrollState = null
        if (isUfo()) {
            Analytics.track("ufo_feature_3")
        }
        if (remoteConfig.featureScreenConfig.isEnableScreen2) {
            createNativeConfig(
                placement = NativePlacement.SELECT_DUP,
                adContainer = binding.flNativeAd,
                shimmerFrameLayout = {
                    if (remoteConfig.nativeSelectConfig.isNativeSmall()) {
                        binding.shimmerNoMedia.shimmerContainerNative
                    } else if (remoteConfig.nativeSelectConfig.isNativeLeftMedia()) {
                        binding.shimmerMediaLeft.shimmerContainerNative
                    } else {
                        binding.shimmerMediaBottom.shimmerContainerNative
                    }
                }
            ).setupNativeAd("native_feature_3").requestAds()
        }
    }
}
