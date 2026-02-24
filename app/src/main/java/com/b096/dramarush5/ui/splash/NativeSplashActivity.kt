package com.b096.dramarush5.ui.splash

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.AzAdCallback
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.helper.adnative.NativeAdConfig
import com.ads.control.helper.adnative.NativeAdHelper
import com.ads.control.helper.adnative.params.AdNativeMediation
import com.ads.control.helper.adnative.params.NativeAdParam
import com.ads.control.helper.adnative.params.NativeLayoutMediation
import com.b096.dramarush5.R
import com.b096.dramarush5.ads.model.config.NativeFullConfig
import com.b096.dramarush5.ads.model.splash.SplashConfig
import com.b096.dramarush5.app.remoteConfig
import com.b096.dramarush5.databinding.ActivityNativeSplashBinding
import com.dong.baselib.base.BaseActivity
import com.dong.baselib.extensions.gone
import com.dong.baselib.extensions.visible
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.apply
import kotlin.let
import kotlin.ranges.coerceAtLeast
import kotlin.runCatching

@SuppressLint("CustomSplashScreen")
class NativeSplashActivity :
    BaseActivity<ActivityNativeSplashBinding>(ActivityNativeSplashBinding::inflate, true) {
    companion object {
        var nativeAdSplash: SplashConfig? = null
    }

    private var job: Job? = null
    private var jobLoading: Job? = null
    private var isFinishLoading = false

    private val nativeAdHelper by lazy {
        nativeAdSplash?.let {
            nativeAdProvider(
                adUnit = it.adUnit,
                isShowAd = remoteConfig.adEnable,
                layout = R.layout.layout_native_full_screen,
                layoutMeta = if (remoteConfig.metaCtrLow) R.layout.layout_native_full_screen_meta_low else R.layout.layout_native_full_screen_meta_high,
            )
        }
    }
    private val configAdScreen =  NativeFullConfig(
        isShowClose = true,
        delayShowClose = 0,
        timeDelaySkip = 3000,
    )

    private fun loadAds() {
        nativeAdHelper
            ?.setNativeContentView(binding.flNativeAd)
            ?.setShimmerLayoutView(binding.shimmerNativeAd.shimmerContainerNative)
        nativeAdHelper?.requestAds(NativeAdParam.Request.create())
        nativeAdHelper?.registerAdListener(object : AzAdCallback() {
            override fun onAdImpression() {
                super.onAdImpression()
                initListener()
            }

            override fun onAdFailedToLoad(adError: ApAdError?) {
                super.onAdFailedToLoad(adError)
                setResult(RESULT_OK)
                finish()
            }
        })
        initListener()
    }

    private fun initListener() {
        runCatching {
            binding.flNativeAd.findViewById<View>(R.id.btnNext)?.let {
                it.setOnClickListener {
                    setResult(RESULT_OK)
                    finish()
                }
            }

            binding.tvSkip.postDelayed({
                binding.tvSkip.isVisible = configAdScreen.isShowClose
            }, configAdScreen.delayShowClose)

            binding.tvSkip.setOnClickListener {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppOpenManager.getInstance().disableAppResume()
        countDownSkip()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (remoteConfig.appOpenAdConfig.enable) {
            AppOpenManager.getInstance().enableAppResume()
        }
    }

    private fun countDownSkip() {
        job?.cancel()
        val remoteDelay = configAdScreen.timeDelaySkip
        val delayTime = if (isFinishLoading) {
            (remoteDelay - 1_000).coerceAtLeast(0L)
        } else {
            remoteDelay
        }
        job = lifecycleScope.launch {
            delay(delayTime)
            if (isActive) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun countDownLoading() {
        isFinishLoading = false
        jobLoading?.cancel()
        jobLoading = lifecycleScope.launch {
            delay(1_000)
            if (isActive) {
                isFinishLoading = true
                binding.rlLoading.gone()
                binding.flNativeAd.visible()
                loadAds()
            }
        }
    }

    override fun backPressed() = Unit

    override fun initialize() {
        countDownLoading()
    }

    override fun ActivityNativeSplashBinding.setData() = Unit

    override fun ActivityNativeSplashBinding.onClick() {

    }

    override fun onPause() {
        super.onPause()
        job?.cancel()
    }
}

fun AppCompatActivity.nativeAdProvider(
    adUnit: String,
    isShowAd: Boolean,
    @LayoutRes layout: Int,
    lifecycleOwner: LifecycleOwner? = null,
    @LayoutRes layoutMeta: Int? = null,
): NativeAdHelper {
    val config = NativeAdConfig(
        adUnit,
        isShowAd,
        true,
        layout,
    )
    layoutMeta?.let {
        config.setLayoutMediation(
            NativeLayoutMediation(
                AdNativeMediation.FACEBOOK, it
            )
        )
    }
    return NativeAdHelper(
        this,
        lifecycleOwner ?: this,
        config
    ).apply {
        setEnablePreload(true)
    }
}