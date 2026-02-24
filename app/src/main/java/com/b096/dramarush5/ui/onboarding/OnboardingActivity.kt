package com.b096.dramarush5.ui.onboarding

import androidx.activity.result.ActivityResult
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.ads.control.billing.AppPurchase
import com.b096.dramarush5.R
import com.b096.dramarush5.ads.model.AdNativeConfig
import com.b096.dramarush5.ads.wrapper.native.NativePlacement
import com.b096.dramarush5.ads.wrapper.native.createNativeConfig
import com.b096.dramarush5.app.isInternetAvailable
import com.b096.dramarush5.app.remoteConfig
import com.b096.dramarush5.databinding.ActivityOnboardingBinding
import com.b096.dramarush5.firebase.Analytics
import com.b096.dramarush5.ui.feature.FeatureActivity
import com.b096.dramarush5.ui.feature.FeatureScreenType
import com.dong.baselib.base.BaseActivity
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

enum class UserAction {
    BACK, NEXT
}


class OnboardingActivity :
    BaseActivity<ActivityOnboardingBinding>(ActivityOnboardingBinding::inflate, true) {
    companion object {
        var userAction = UserAction.NEXT
        const val SCREEN_ACTION = "onboarding"
    }

    fun shimmerAdView(adCf: AdNativeConfig): ShimmerFrameLayout = if (adCf.isNativeSmall()) {
        binding.shimmerNoMedia.shimmerContainerNative
    } else if (adCf.isNativeLeftMedia()) {
        binding.shimmerMediaLeft.shimmerContainerNative
    } else {
        binding.shimmerMediaBottom.shimmerContainerNative
    }

    val listAdConfig by lazy {
        mapOf(
            1 to createNativeConfig(
                NativePlacement.ONBOARDING_1, binding.flNativeAd
            ) { shimmerAdView(it) }.setupNativeAd("native_ob_1").setFlagReload(false),
            2 to createNativeConfig(
                NativePlacement.ONBOARDING_2, binding.flNativeAd
            ) { shimmerAdView(it) }.setupNativeAd("native_ob_2").setFlagReload(false),
            3 to createNativeConfig(
                NativePlacement.ONBOARDING_3, binding.flNativeAd
            ) { shimmerAdView(it) }.setupNativeAd("native_ob_3").setFlagReload(false),
            4 to createNativeConfig(
                NativePlacement.ONBOARDING_4, binding.flNativeAd
            ) { shimmerAdView(it) }.setupNativeAd("native_ob_4").setFlagReload(false)
        )
    }

    override fun fragmentOnBack() {
        super.fragmentOnBack()
        backPressed()
    }

    private var previousPosition = 0
    private val listFragment by lazy {
        mutableListOf<OnboardingPage>().apply {
            add(
                OnboardingPage(
                    index = 1,
                    fragment = OnboardingFragment.newInstance(0),
                    showTabLayout = true,
                    shouldShowAd = ::isShowNativeOnb1,
                )
            )
            add(
                OnboardingPage(
                    index = 2,
                    fragment = OnboardingFragment.newInstance(1),
                    showTabLayout = true,
                    shouldShowAd = ::isShowNativeOnb2
                )
            )
            if (isShowNativeFullScreen1()) {
                add(
                    OnboardingPage(
                        index = -1,
                        fragment = OnboardingFullFragment.newInstance(0),
                        showTabLayout = false,
                        shouldShowAd = { false })
                )
            }
            add(
                OnboardingPage(
                    index = 3,
                    fragment = OnboardingFragment.newInstance(2),
                    showTabLayout = true,
                    shouldShowAd = ::isShowNativeOnb3
                )
            )
            if (isShowNativeFullScreen2()) {
                add(
                    OnboardingPage(
                        index = -1,
                        fragment = OnboardingFullFragment.newInstance(1),
                        showTabLayout = false,
                        shouldShowAd = { false })
                )
            }
            add(
                OnboardingPage(
                    index = 4,
                    fragment = OnboardingFragment.newInstance(3),
                    showTabLayout = true,
                    shouldShowAd = ::isShowNativeOnb4
                )
            )
        }
    }

    val viewModel: OnboardingViewModel by inject()
    override fun backPressed() {
        previousPage()
    }

    override fun initialize() = Unit

    override fun ActivityOnboardingBinding.setData() {
        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                userAction = if (position > previousPosition) {
                    UserAction.NEXT
                } else {
                    UserAction.BACK
                }
                super.onPageSelected(position)
                updateTextNext(position)
                setActiveIndicator()
                lifecycleScope.launch(Dispatchers.Main) {
                    checkLoadAds()
                }
                previousPosition = position
            }
        })
    }

    override fun ActivityOnboardingBinding.onClick() {
        binding.tvNext.setOnClickListener { nextPage() }
    }

    private val adapter by lazy {
        OnboardingAdapter(this, listFragment.map { it.fragment })
    }

    private fun updateTextNext(position: Int) {
        if (position == listFragment.size - 1) {
            binding.tvNext.text = getString(R.string.continue_)
        } else {
            binding.tvNext.text = getString(R.string.next)
        }
    }

    private fun setActiveIndicator() {
        val currentItem = getCurrentItem()
        val currentFragment = listFragment.map { it.fragment }.getOrNull(currentItem)
        if (currentFragment !is OnboardingFragment) return
        val indicatorIndex = listFragment.map { it.fragment }.take(currentItem + 1)
            .count { it is OnboardingFragment } - 1
        binding.indicatorView.setIndicatorActive(indicatorIndex)
        when (indicatorIndex) {
            0 -> {
                binding.tvTitle.text = getString(R.string.title_onboarding_1)
                binding.tvContent.text = getString(R.string.content_onboarding_1)
            }

            1 -> {
                binding.tvTitle.text = getString(R.string.title_onboarding_2)
                binding.tvContent.text = getString(R.string.content_onboarding_2)
            }

            2 -> {
                binding.tvTitle.text = getString(R.string.title_onboarding_3)
                binding.tvContent.text = getString(R.string.content_onboarding_3)
            }

            3 -> {
                binding.tvTitle.text = getString(R.string.title_onboarding_4)
                binding.tvContent.text = getString(R.string.content_onboarding_4)
            }
        }
    }

    private fun getCurrentItem(): Int {
        return binding.viewPager.currentItem
    }


    fun nextPage() {
        if (getCurrentItem() >= listFragment.map { it.fragment }.size - 1) {
            actionGoNext()
        } else {
            binding.viewPager.setCurrentItem(getCurrentItem() + 1, false)
        }
    }

    private fun actionGoNext() {
        FeatureActivity.start(this@OnboardingActivity, FeatureScreenType.Feature1)
        finish()
    }

    fun previousPage() {
        if (getCurrentItem() == 0) {
            finishAffinity()
        } else {
            binding.viewPager.setCurrentItem(getCurrentItem() - 1, false)
        }
    }

    private fun checkLoadAds() {
        val index = getCurrentItem()
        if (index !in listFragment.indices) return
        val currentFragment = listFragment[getCurrentItem()]
        binding.llTabLayout.isVisible = currentFragment.showTabLayout
        binding.tvTitle.isVisible = currentFragment.showTabLayout
        requestAds(currentFragment.index, currentFragment.shouldShowAd())
    }

    private fun requestAds(index: Int, isRequest: Boolean) {
        listAdConfig.forEach { (key, value) ->
            Analytics.track("OB${index}Scr_Show")
            if (index == key && isRequest) {
                value.setFlagReload(true).requestAdsPreview()
            } else {
                value.setFlagReload(false).cancelRequest()
            }
        }
    }

    private fun cancelAds(index: Int) {
        listAdConfig.forEach { (key, value) ->
            if (index == key) value.cancelRequest()
        }
    }

    private fun isShowNativeFullScreen1(): Boolean {
        return remoteConfig.nativeObFull1Config.enable && isInternetAvailable() && !AppPurchase.getInstance().isPurchased
    }

    private fun isShowNativeFullScreen2(): Boolean {
        return remoteConfig.nativeObFull2Config.enable && isInternetAvailable() && !AppPurchase.getInstance().isPurchased
    }

    private fun isShowNativeOnb1(): Boolean {
        return remoteConfig.nativeOnboarding1Config.enable
    }

    private fun isShowNativeOnb2(): Boolean {
        return remoteConfig.nativeOnboarding2Config.enable
    }

    private fun isShowNativeOnb3(): Boolean {
        return remoteConfig.nativeOnboarding3Config.enable
    }

    private fun isShowNativeOnb4(): Boolean {
        return remoteConfig.nativeOnboarding4Config.enable
    }
}

data class OnboardingPage(
    val index: Int,
    val fragment: Fragment,
    val showTabLayout: Boolean,
    val shouldShowAd: () -> Boolean,
)
