package com.b096.dramarush5.ui.language

import android.content.res.Resources
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.b096.dramarush5.ads.wrapper.native.NativeAdPreloadManager
import com.b096.dramarush5.ads.wrapper.native.NativeAdsWrapper
import com.b096.dramarush5.ads.wrapper.native.NativePlacement
import com.b096.dramarush5.ads.wrapper.native.NativePlacement.Companion.preloadAdConfig
import com.b096.dramarush5.app.PreferenceData.isUfo
import com.b096.dramarush5.databinding.ActivityLanguageWaitingBinding
import com.b096.dramarush5.firebase.Analytics
import com.dong.baselib.base.BaseActivity
import com.dong.baselib.utils.moveItemToPosition
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LanguageWaitingActivity :
    BaseActivity<ActivityLanguageWaitingBinding>(ActivityLanguageWaitingBinding::inflate) {
    private var job: Job? = null
    private val languageAdapter by lazy { LfoAdapter() }

    companion object {
        var isShowAdInWaiting = false
    }

    private val nativeAdsWrapper by lazy {
        NativeAdsWrapper(
            activity = this,
            config = NativePlacement.LANGUAGE_LOADING,
            lifecycleOwner = this@LanguageWaitingActivity,
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

    override fun backPressed() = Unit
    override fun initialize() = Unit
    override fun ActivityLanguageWaitingBinding.setData() {
        countDownSkip()
        setupListLanguage()
        with(nativeAdsWrapper) {
            setupNativeAd("native_lang")
            requestAds()
        }
        countDownSkip()
        setupListLanguage()
        launchMain {
            NativePlacement.LANGUAGE_2.preloadAdConfig(this@LanguageWaitingActivity,1)
        }
        Analytics.track("LFOScr1_Show")
    }

    fun nextAction() {
        LanguageOpenActivity.start(this@LanguageWaitingActivity, LanguageScreenType.Language1)
        overridePendingTransition(0, 0)
        finish()
    }

    override fun ActivityLanguageWaitingBinding.onClick() = Unit

    private fun countDownSkip() {
        job?.cancel()
        job = lifecycleScope.launch {
            val totalDuration = 2000L
            val maxProgress = 100
            for (progress in 0..maxProgress) {
                if (!isActive) return@launch
                delay(totalDuration / maxProgress)
            }
            nextAction()
        }
    }

    override fun isFinishFirstFlow(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isUfo()) {
            Analytics.track("ufo_language_loading")
        }
    }

    private fun setupListLanguage() {
        languageAdapter.setEnable(false)
        languageAdapter.submitList(getListLanguageLfo())
        binding.rcvLanguage.adapter = languageAdapter
    }

    private fun getListLanguageLfo(): List<LanguageItem> {
        val deviceLanguage = Resources.getSystem().configuration.locales[0].language
        val indexLanguageDevice = listLanguage.indexOfFirst { it.code == deviceLanguage }
        val listLfo = if (indexLanguageDevice != -1) {
            listLanguage[indexLanguageDevice].isDefault = true
            listLanguage.moveItemToPosition(3) { it.code == deviceLanguage }
        } else {
            listLanguage[0].isDefault = true
            listLanguage
        }
        return listLfo
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}
