package com.b096.dramarush5.ui.splash

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.lifecycle.lifecycleScope
import com.ads.control.admob.AdsConsentManager2
import com.ads.control.billing.AppPurchase
import com.ads.control.billing.PurchaseItem
import com.b096.dramarush5.BuildConfig
import com.b096.dramarush5.ads.wrapper.interstitial.InterstitialAdManager
import com.b096.dramarush5.ads.wrapper.native.NativeAdPreloadManager
import com.b096.dramarush5.ads.wrapper.native.NativePlacement
import com.b096.dramarush5.ads.wrapper.splash.AdSplashCompleteListener
import com.b096.dramarush5.ads.wrapper.splash.AdSplashManager
import com.b096.dramarush5.ads.wrapper.splash.AdState
import com.b096.dramarush5.ads.wrapper.splash.NativeSplashManager
import com.b096.dramarush5.app.PreferenceData
import com.b096.dramarush5.app.PreferenceData.countSessionApp
import com.b096.dramarush5.app.PreferenceData.isUfo
import com.b096.dramarush5.dialog.RatePopupManager
import com.b096.dramarush5.app.isInternetAvailable
import com.b096.dramarush5.app.remoteConfig
import com.b096.dramarush5.databinding.ActivitySplashBinding
import com.b096.dramarush5.firebase.Analytics
import com.b096.dramarush5.ui.language.Language1Activity
import com.b096.dramarush5.ui.language.LanguageWaitingActivity
import com.b096.dramarush5.ui.main.MainActivity
import com.dong.baselib.base.BaseActivity
import kotlinx.coroutines.delay
import com.az.inappupdate.AppUpdateManager
import com.dong.baselib.extensions.gone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.collections.plusAssign

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate),
    AdSplashCompleteListener {
    override fun backPressed() = Unit
    override fun initialize() = Unit
    private var isNextAction = false
    private var isAcceptUmp = true
    private var isFirstResume = true
    private var nativeSplashManager: NativeSplashManager? = null
    private var isFinishAdSplash = false
    private var isFinishAdNative = false
    private var jobAdSplash: Job? = null
    private var jobNativeSplash: Job? = null
    override fun isFinishFirstFlow(): Boolean = PreferenceData.isFinishFirstFlow

    override fun ActivitySplashBinding.onClick() = Unit
    private fun navigateToNextScreen() {
        if (isNextAction) return
        isNextAction = true
        if (PreferenceData.isFinishFirstFlow) {
            launchActivity<MainActivity>()
            finish()
        } else {
            if (remoteConfig.languageLoading.enableScreen) {
                launchActivity<LanguageWaitingActivity>()
            } else {
                launchActivity<Language1Activity>()
            }
        }
        finish()
    }

    override fun ActivitySplashBinding.setData() {
        lifecycleScope.launch {
            awaitAll(
                async { requestUmp() },
                async { withTimeoutOrNull(30000) { remoteConfig.setupRemoteConfig(BuildConfig.build_debug) } }
            )
            setupInAppUpdate()
            initAds()
        }

        Analytics.track("SplashScr_Show")
    }

    private suspend fun requestUmp() {
        val consentManager = AdsConsentManager2(this@SplashActivity)
        consentManager.requestUMP()
        isAcceptUmp = consentManager.getCanRequestAd()
        if (!isAcceptUmp) finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        countSessionApp += 1
        RatePopupManager.reset()
        if (isUfo()) {
            Analytics.track("ufo_splash")
        }
    }

    private fun setupInAppUpdate() {
        AppUpdateManager.getInstance(this@SplashActivity).setupUpdate(
            remoteConfig.inAppUpdate,
            remoteConfig.timesShowUpdate,
        )
    }

    private suspend fun initAds() {
        InterstitialAdManager.resetInterAllConfig()
        InterstitialAdManager.isCloseInterSplash.postValue(false)
        if (isEnableAds()) {
            setupAdManager()
            nativeSplashManager?.loadNative()
            AdSplashManager.instance.loadAds()
            observeAds()
            preloadAdNextScreen()
        } else {
            binding.flNativeAd.gone()
            InterstitialAdManager.isCloseInterSplash.postValue(true)
            delay(3000)
            navigateToNextScreen()
        }
    }

    private fun preloadAdNextScreen() {
        lifecycleScope.launch(Dispatchers.Main) {
            if (!PreferenceData.isFinishFirstFlow) {
                if (remoteConfig.languageLoading.enableScreen) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        NativeAdPreloadManager.preloadAd(
                            this@SplashActivity,
                            NativePlacement.LANGUAGE_LOADING
                        )
                    }
                }
                lifecycleScope.launch(Dispatchers.Main) {
                    NativeAdPreloadManager.preloadAd(
                        this@SplashActivity,
                        NativePlacement.LANGUAGE_1
                    )
                }
                if (!remoteConfig.languageLoading.enableScreen) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        NativeAdPreloadManager.preloadAd(
                            this@SplashActivity,
                            NativePlacement.LANGUAGE_2
                        )
                    }
                }
            }
        }
    }

    private fun observeAds() {
        lifecycleScope.launch {
            AdSplashManager.instance.adState.collectLatest {
                when (it) {
                    AdState.Idle -> Unit
                    AdState.NavigateNext -> navigateToNextScreen()
                    AdState.NativeFullScr -> navigateToNativeFullScreen()
                }
            }
        }
    }

    private fun setupAdManager() {
        nativeSplashManager = NativeSplashManager(
            this,
            binding.flNativeAd,
            if (remoteConfig.nativeSplashConfig.isNativeSmall()) {
                binding.shimmerNoMedia.shimmerContainerNative
            } else if (remoteConfig.nativeSplashConfig.isNativeLeftMedia()) {
                binding.shimmerMediaLeft.shimmerContainerNative
            } else {
                binding.shimmerMediaBottom.shimmerContainerNative
            },
            this@SplashActivity
        )
        AdSplashManager.instance.bind(this@SplashActivity, this@SplashActivity)
    }

    private fun navigateToNativeFullScreen() {
        launcherForResult<NativeSplashActivity>()
    }

    override fun listenerResult(result: ActivityResult) {
        super.listenerResult(result)
        if (result.resultCode == RESULT_OK) {
            navigateToNextScreen()
        }
    }

    override fun onDestroy() {
        nativeSplashManager?.cleanup()
        super.onDestroy()
    }

    override fun onAdComplete(adName: String) {
        when (adName) {
            AdSplashManager.TAG -> {
                isFinishAdSplash = true
                if (isFinishAdNative) {
                    AdSplashManager.instance.showCurrentAd()
                } else {
                    handleWaitingAdSplash()
                }
            }

            NativeSplashManager.TAG -> {
                handleDelayAdNative()
            }
        }
    }

    private fun handleWaitingAdSplash() {
        jobAdSplash?.cancel()
        jobAdSplash = lifecycleScope.launch {
            delay(remoteConfig.splashTimeout.waitingMs)
            AdSplashManager.instance.showCurrentAd()
            jobNativeSplash?.cancel()
        }
    }

    private fun handleDelayAdNative() {
        jobNativeSplash?.cancel()
        jobNativeSplash = lifecycleScope.launch {
            delay(remoteConfig.splashTimeout.delayMs)
            isFinishAdNative = true
            if (isFinishAdSplash) {
                AdSplashManager.instance.showCurrentAd()
                jobAdSplash?.cancel()
            }
        }
    }

    private fun isEnableAds(): Boolean {
        return remoteConfig.adEnable && isInternetAvailable() && isAcceptUmp
    }

    override fun onResume() {
        super.onResume()
        if (isFirstResume) {
            isFirstResume = false
            return
        }
        if (remoteConfig.splashAdConfig.enable && isEnableAds()) {
            if (isFinishAdSplash && isFinishAdNative) {
                AdSplashManager.instance.onCheckShowAdsWhenFail()
            } else {
                lifecycleScope.launch {
                    delay(remoteConfig.splashAdConfig.totalTimeout)
                    if (!isNextAction && !isFinishing) {
                        navigateToNextScreen()
                    }
                }
            }
        } else {
            navigateToNextScreen()
        }
    }
}