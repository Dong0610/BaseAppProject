package com.b096.dramarush5.ads.wrapper.splash

import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import com.b096.dramarush5.ads.wrapper.native.NativeAdsWrapper
import com.b096.dramarush5.ads.wrapper.native.NativePlacement
import com.b096.dramarush5.app.remoteConfig
import com.dong.baselib.extensions.gone
import com.dong.baselib.extensions.visible
import com.facebook.shimmer.ShimmerFrameLayout

interface AdSplashCompleteListener {
    fun onAdComplete(adName: String)
}

class NativeSplashManager(
    private val activity: AppCompatActivity,
    private val adContainer: FrameLayout,
    private val shimmerView: ShimmerFrameLayout,
    private val listener: AdSplashCompleteListener,
) : DefaultLifecycleObserver {
    companion object {
        const val TAG = "NativeSplashManager"
    }

    private var isCleanedUp = false
    private var isCallAdComplete = false

    init {
        activity.lifecycle.addObserver(this)
    }

    private val nativeAdsWrapper by lazy {
        NativeAdsWrapper(
            activity = activity,
            config = NativePlacement.SPLASH,
            lifecycleOwner = activity,
            adContainer = { adContainer },
            shimmerView = { shimmerView },
        )
    }

    fun loadNative() {
        if (remoteConfig.splashAdConfig.enable) {
            adContainer.visible()
            nativeAdsWrapper.apply {
                setupNativeAd(TAG)
                registerAdCallbacks(
                    onImpression = {
                        if (isCleanedUp) return@registerAdCallbacks
                        onAdComplete()
                    },
                    onFailed = {
                        if (isCleanedUp) return@registerAdCallbacks
                        onAdComplete()
                    },
                )
                requestAds()
            }
        } else {
            adContainer.gone()
            onAdComplete()
        }
    }

    fun cleanup() {
        isCleanedUp = true
        activity.lifecycle.removeObserver(this)
    }

    private fun onAdComplete() {
        if (isCallAdComplete) return
        isCallAdComplete = true
        listener.onAdComplete(TAG)
    }
}