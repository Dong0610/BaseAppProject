package com.b096.dramarush5.ads.wrapper.splash

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.AzAdCallback
import com.ads.control.ads.AzAds
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.billing.AppPurchase
import com.ads.control.funtion.AdCallback
import com.ads.control.helper.adnative.preload.NativeAdPreload
import com.ads.control.helper.adnative.preload.NativePreloadState
import com.b096.dramarush5.R
import com.b096.dramarush5.ads.model.splash.SplashConfig
import com.b096.dramarush5.ads.model.config.SplashType
import com.b096.dramarush5.ads.wrapper.interstitial.InterstitialAdManager
import com.b096.dramarush5.app.isInternetAvailable
import com.b096.dramarush5.app.remoteConfig
import com.b096.dramarush5.firebase.Analytics
import com.b096.dramarush5.ui.splash.NativeSplashActivity
import com.google.android.gms.ads.AdError
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

sealed class AdState {
    data object Idle : AdState()
    data object NavigateNext : AdState()
    data object NativeFullScr : AdState()
}

class AdSplashManager private constructor() : DefaultLifecycleObserver {
    companion object {
        private const val TIME_DELAY = 3_000L
        const val TAG = "AdSplashManager"
        val instance: AdSplashManager by lazy { AdSplashManager() }
    }

    private var activityRef: WeakReference<AppCompatActivity>? = null
    private var listener: AdSplashCompleteListener? = null

    private var _adState: MutableStateFlow<AdState> = MutableStateFlow(AdState.Idle)
    val adState: StateFlow<AdState> get() = _adState

    private var splashType: SplashType? = null
    private var splashAdList: MutableList<SplashConfig> = mutableListOf()
    private var isLoadedNativeFullScreen = false

    private var timeoutJob: Job? = null
    private var isTimeOut: Boolean = false

    private var timeoutNativeJob: Job? = null
    private var isTimeoutNative = false

    private var timeDelayNativeJob: Job? = null
    private var isTimeDelayNative = false

    private var isAppOpenBackground = false
    private var isPause = false

    fun bind(activity: AppCompatActivity, listener: AdSplashCompleteListener? = null) {
        activityRef = WeakReference(activity)
        this.listener = listener
        activity.lifecycle.addObserver(this)
        updateState(AdState.Idle)
    }

    private fun getActivity(): AppCompatActivity? {
        return activityRef?.get()
    }

    /**
     * Callback for interstitial ads
     */
    private val interAdCallback by lazy {
        object : AzAdCallback() {
            override fun onNextAction() {
                super.onNextAction()
                updateState(AdState.NavigateNext)
            }

            override fun onAdClosed() {
                super.onAdClosed()
                InterstitialAdManager.isCloseInterSplash.postValue(true)
                InterstitialAdManager.updateTimeShowInterAll()
            }

            override fun onAdFailedToShow(adError: ApAdError?) {
                super.onAdFailedToShow(adError)
                InterstitialAdManager.isCloseInterSplash.postValue(true)
            }
        }
    }

    /**
     * Callback for open app ads
     */
    private val openAdCallback by lazy {
        object : AdCallback() {
            override fun onNextAction() {
                super.onNextAction()
                if (isAppOpenBackground) return
                InterstitialAdManager.isCloseInterSplash.postValue(true)
                updateState(AdState.NavigateNext)
            }

            override fun onAdFailedToShow(adError: AdError?) {
                super.onAdFailedToShow(adError)
                if (adError?.message?.contains("in foreground") == true) {
                    isAppOpenBackground = true
                }
            }
        }
    }

    fun showCurrentAd() {
        when (splashType) {
            SplashType.Inter -> showInterSplash()
            SplashType.AppOpen -> showOpenSplash()
            SplashType.Native -> showNativeFullScrSplash()
            else -> {
                InterstitialAdManager.isCloseInterSplash.postValue(true)
                updateState(AdState.NavigateNext)
            }
        }
    }

    fun loadAds(isUsingTimeout: Boolean = true) {
        if (remoteConfig.splashAdConfig.enable
            && remoteConfig.adEnable
            && getActivity()?.isInternetAvailable() == true
            && !AppPurchase.getInstance().isPurchased
        ) {
            updateState(AdState.Idle)
            isTimeOut = false
            if (isUsingTimeout) {
                startLoadAdsTimeout()
            }
            splashAdList = remoteConfig.splashAdConfig.listAds.toMutableList()
            loadNextAdSplash(true)
        } else {
            splashType = null
            onAdComplete()
        }
    }

    private fun loadNextAdSplash(isDelay: Boolean = false) {
        if (isTimeOut) return
        val adConfig = splashAdList.removeFirstOrNull() ?: return run {
            splashType = null
            onAdComplete()
        }
        loadAdSplashConfig(adConfig, isDelay)
    }

    private fun loadAdSplashConfig(splashConfig: SplashConfig, isDelay: Boolean) {
        when (splashConfig.type) {
            SplashType.Inter.type -> loadInterSplash(splashConfig, isDelay)
            SplashType.AppOpen.type -> loadOpenSplash(splashConfig, isDelay)
            SplashType.Native.type -> loadNativeSplash(splashConfig, isDelay)
            else -> {
                loadNextAdSplash()
                Analytics.track("Ad Splash Type Not Valid: ${splashConfig.type}")
            }
        }
    }

    private fun loadInterSplash(splashConfig: SplashConfig, isDelay: Boolean) {
        if (!splashConfig.enableAd) {
            loadNextAdSplash()
            return
        }

        withActivity {
            AzAds.getInstance().loadSplashInterstitialAds(
                it,
                splashConfig.adUnit,
                splashConfig.timeout,
                if (isDelay) TIME_DELAY else 0,
                object : AzAdCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        loadNextAdSplash()
                    }

                    override fun onAdSplashReady() {
                        super.onAdSplashReady()
                        if (isTimeOut) return
                        splashType = SplashType.Inter
                        timeoutJob?.cancel()
                        onAdComplete()
                    }
                }
            )
        } ?: loadNextAdSplash()
    }

    private fun loadOpenSplash(splashConfig: SplashConfig, isDelay: Boolean) {
        if (!splashConfig.enableAd) {
            loadNextAdSplash()
            return
        }

        withActivity {
            AppOpenManager.getInstance().setSplashAdId(splashConfig.adUnit)
            AppOpenManager.getInstance().loadOpenAppAdSplash(
                it,
                if (isDelay) TIME_DELAY else 0,
                splashConfig.timeout,
                object : AdCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        loadNextAdSplash()
                    }

                    override fun onAdSplashReady() {
                        super.onAdSplashReady()
                        if (isTimeOut) return
                        splashType = SplashType.AppOpen
                        timeoutJob?.cancel()
                        onAdComplete()
                    }
                }
            )
        } ?: loadNextAdSplash()
    }

    private fun loadNativeSplash(splashConfig: SplashConfig, isDelay: Boolean) {
        if (!splashConfig.enableAd) {
            loadNextAdSplash()
            return
        }

        withActivity { act ->
            isTimeoutNative = false
            countTimeoutNative(splashConfig.timeout)
            if (isDelay) {
                countTimeDelayNative()
            } else {
                isTimeDelayNative = true
            }
            NativeSplashActivity.nativeAdSplash = splashConfig
            isLoadedNativeFullScreen = false
            NativeAdPreload.getInstance().preload(
                act,
                splashConfig.adUnit,
                R.layout.layout_native_full_screen,
            )
            NativeAdPreload.getInstance().getAdPreloadState(
                splashConfig.adUnit,
                R.layout.layout_native_full_screen
            ).onEach {
                when (it) {
                    is NativePreloadState.Consume -> {
                        isLoadedNativeFullScreen = true
                    }

                    is NativePreloadState.Complete -> {
                        if (isTimeOut || isTimeoutNative) return@onEach
                        timeoutNativeJob?.cancel()
                        if (isTimeDelayNative) onAdNativeComplete()
                    }

                    else -> Unit
                }
            }.launchIn(act.lifecycleScope)
        } ?: loadNextAdSplash()
    }

    private fun onAdNativeComplete() {
        if (!isLoadedNativeFullScreen) {
            loadNextAdSplash()
        } else {
            splashType = SplashType.Native
            timeoutJob?.cancel()
            onAdComplete()
        }
    }

    private fun countTimeDelayNative() {
        timeDelayNativeJob?.cancel()
        withActivity {
            timeDelayNativeJob = it.lifecycleScope.launch {
                delay(TIME_DELAY)
                if (isActive) handleTimeDelayNative()
            }
        }
    }

    private fun handleTimeDelayNative() {
        isTimeDelayNative = true
        onAdNativeComplete()
    }

    private fun countTimeoutNative(timeout: Long) {
        timeoutNativeJob?.cancel()
        withActivity {
            timeoutNativeJob = it.lifecycleScope.launch {
                delay(timeout)
                if (isActive) handleTimeOutNative()
            }
        }
    }

    private fun handleTimeOutNative() {
        if (isTimeoutNative) return
        if (isLoadedNativeFullScreen) {
            splashType = SplashType.Native
            timeoutJob?.cancel()
            onAdComplete()
        } else {
            loadNextAdSplash()
        }
        isTimeoutNative = true
    }

    private fun showInterSplash() {
        withActivity {
            AzAds.getInstance().onShowSplash(it, interAdCallback)
        }
    }

    private fun showOpenSplash() {
        withActivity {
            AppOpenManager.getInstance().showAppOpenSplash(it, openAdCallback)
        }
    }

    private fun showNativeFullScrSplash() {
        if (isPause) return
        InterstitialAdManager.isCloseInterSplash.postValue(true)
        updateState(AdState.NativeFullScr)
    }

    private fun startLoadAdsTimeout() {
        timeoutJob?.cancel()
        withActivity {
            timeoutJob = it.lifecycleScope.launch {
                delay(remoteConfig.splashAdConfig.totalTimeout)
                if (isActive) handleTimeOut()
            }
        }
    }

    private fun handleTimeOut() {
        if (isTimeOut) return
        onAdComplete()
        isTimeOut = true
    }

    /**
     * Called when the activity is resumed
     */
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        isAppOpenBackground = false
        isPause = false
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        isPause = true
    }

    override fun onDestroy(owner: LifecycleOwner) {
        getActivity()?.lifecycle?.removeObserver(this)
        super.onDestroy(owner)
    }

    private fun updateState(state: AdState) {
        _adState.update { state }
    }

    fun onCheckShowAdsWhenFail() {
        when (splashType) {
            SplashType.Inter -> {
                withActivity {
                    AzAds.getInstance().onCheckShowSplashWhenFail(it, interAdCallback, 1000)
                } ?: updateStateNext()
            }

            SplashType.AppOpen -> {
                withActivity {
                    AppOpenManager.getInstance()
                        .onCheckShowAppOpenSplashWhenFail(it, openAdCallback, 1000)
                } ?: updateStateNext()
            }

            SplashType.Native -> {
                withActivity {
                    it.lifecycleScope.launch {
                        delay(1000)
                        InterstitialAdManager.isCloseInterSplash.postValue(true)
                        if (isLoadedNativeFullScreen) {
                            updateState(AdState.NativeFullScr)
                        } else {
                            updateState(AdState.NavigateNext)
                        }
                    }
                } ?: updateStateNext()
            }

            else -> {
                updateStateNext()
            }
        }
    }

    private fun updateStateNext() {
        withActivity {
            it.lifecycleScope.launch {
                delay(1000)
                InterstitialAdManager.isCloseInterSplash.postValue(true)
                updateState(AdState.NavigateNext)
            }
        } ?: run {
            InterstitialAdManager.isCloseInterSplash.postValue(true)
            updateState(AdState.NavigateNext)
        }
    }

    private fun onAdComplete() {
        listener?.onAdComplete(TAG)
    }

    private inline fun <T> withActivity(block: (AppCompatActivity) -> T): T? {
        val act = activityRef?.get()
        return if (act != null && !act.isFinishing && !act.isDestroyed) {
            block(act)
        } else null
    }
}