package com.b096.dramarush5.ui.feature

import android.os.Bundle
import com.b096.dramarush5.ads.wrapper.native.NativePlacement
import com.b096.dramarush5.ads.wrapper.native.createNativeConfig
import com.b096.dramarush5.app.PreferenceData.isUfo
import com.b096.dramarush5.app.remoteConfig
import com.b096.dramarush5.firebase.Analytics

class Feature1Activity : FeatureActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedScrollState = null
        featureList = listFeatureContent.toMutableList()
        if (isUfo()) {
            Analytics.track("ufo_feature_1")
        }
        Analytics.track("AskScr1_Show")
        if (remoteConfig.featureScreenConfig.isEnableScreen1) {
            createNativeConfig(
                placement = NativePlacement.SELECT,
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
            ).setupNativeAd("native_feature_1").requestAds()
        }
    }
}
