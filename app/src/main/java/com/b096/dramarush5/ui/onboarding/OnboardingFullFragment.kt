package com.b096.dramarush5.ui.onboarding

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.ads.control.admob.AppOpenManager
import com.b096.dramarush5.R
import com.b096.dramarush5.ads.wrapper.native.NativeAdsWrapper
import com.b096.dramarush5.ads.wrapper.native.NativePlacement
import com.b096.dramarush5.app.remoteConfig
import com.b096.dramarush5.databinding.FragmentOnboardingFullBinding
import com.dong.baselib.base.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.let
import kotlin.run
import kotlin.runCatching
import kotlin.to

class OnboardingFullFragment :
    BaseFragment<FragmentOnboardingFullBinding>(FragmentOnboardingFullBinding::inflate, true) {
    companion object {
        private const val ARG_POSITION = "ARG_POSITION"

        fun newInstance(position: Int): OnboardingFullFragment {
            val fragment = OnboardingFullFragment()
            fragment.arguments = bundleOf(ARG_POSITION to position)
            return fragment
        }
    }

    private var job: Job? = null
    private var isAdImpression: Boolean = false
    private var isAdFailed: Boolean = false
    private val viewModel: OnboardingViewModel by activityViewModel()

    private val position by lazy {
        runCatching { arguments?.getInt(ARG_POSITION) }.getOrNull() ?: 0
    }
    private val nativeFullConfig by lazy {
        if (position == 0) remoteConfig.configNativeFull1
        else remoteConfig.configNativeFull2
    }

    private var nativeAdsWrapper: NativeAdsWrapper? = null

    private fun getNativeAdsWrapper(): NativeAdsWrapper? {
        return (activity as? AppCompatActivity)?.let {
            NativeAdsWrapper(
                activity = it,
                config = if (position == 0) NativePlacement.ONBOARDING_FULL_1 else NativePlacement.ONBOARDING_FULL_2,
                lifecycleOwner = this,
                adContainer = { binding.flNativeAd },
                shimmerView = { binding.shimmerNativeAd.shimmerContainerNative }
            )
        }
    }

    private fun nextPage() {
        if (OnboardingActivity.userAction == UserAction.NEXT) {
            (activity as? OnboardingActivity)?.nextPage()
        } else {
            (activity as? OnboardingActivity)?.previousPage()
        }
    }

    override fun onResume() {
        super.onResume()
        AppOpenManager.getInstance().disableAppResume()
        if (!viewModel.isFirstAutoSkip) {
            viewModel.isFirstAutoSkip = true
            countDownSkip()
        }
        if (isAdFailed && !isAdImpression) {
            lifecycleScope.launch(Dispatchers.Main) {
                delay(100)
                nextPage()
            }
        }
    }

    private fun countDownSkip() {
        job?.cancel()
        val timeDelay = nativeFullConfig.timeDelaySkip
        job = lifecycleScope.launch(Dispatchers.Main) {
            delay(timeDelay)
            if (isActive) {
                nextPage()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        job?.cancel()
    }

    override fun FragmentOnboardingFullBinding.initView() {
        viewModel.trackScreenView(
            isFullScreen = true,
            position = position
        )
        if (nativeAdsWrapper == null) nativeAdsWrapper = getNativeAdsWrapper()
        nativeAdsWrapper?.run {
            setupNativeAd("native_full_$position")
            registerAdCallbacks(
                onImpression = {
                    isAdImpression = true
                    initListener()
                },
                onFailed = {
                    if (!isAdImpression) {
                        isAdFailed = true
                        lifecycleScope.launch(Dispatchers.Main) {
                            delay(100)
                            nextPage()
                        }
                    }
                }
            )
            requestAds()
        }
    }

    fun initListener() {
        runCatching {
            binding.tvSkip.postDelayed({
                bindingOrNull?.tvSkip?.isVisible = nativeFullConfig.isShowClose
            }, nativeFullConfig.delayShowClose)
            binding.tvSkip.setOnClickListener { nextPage() }
            binding.flNativeAd.findViewById<View>(R.id.btnNext)?.let {
                it.setOnClickListener { nextPage() }
            }
        }
    }

    override fun FragmentOnboardingFullBinding.onClick() = Unit

    override fun onDestroyView() {
        nativeAdsWrapper = null
        super.onDestroyView()
    }
}
