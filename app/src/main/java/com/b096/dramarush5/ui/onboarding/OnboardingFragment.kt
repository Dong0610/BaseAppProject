package com.b096.dramarush5.ui.onboarding

import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.b096.dramarush5.R
import com.b096.dramarush5.ads.wrapper.native.NativeAdPreloadManager
import com.b096.dramarush5.ads.wrapper.native.NativePlacement
import com.b096.dramarush5.ads.wrapper.native.NativePlacement.Companion.preloadAdConfig
import com.b096.dramarush5.app.remoteConfig
import com.b096.dramarush5.databinding.FragmentOnboardingBinding
import com.bumptech.glide.Glide
import com.dong.baselib.base.BaseFragment
import com.dong.baselib.extensions.load
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class OnboardingFragment :
    BaseFragment<FragmentOnboardingBinding>(FragmentOnboardingBinding::inflate, true) {

    companion object {
        private const val ARG_POSITION = "ARG_POSITION"

        fun newInstance(position: Int): OnboardingFragment {
            val fragment = OnboardingFragment()
            fragment.arguments = bundleOf(ARG_POSITION to position)
            return fragment
        }
    }

    override fun backPress() {
        super.backPress()
        fragmentAttach?.fragmentOnBack()
    }

    private val listOBImage = listOf(
        R.drawable.img_onboarding_1,
        R.drawable.img_onboarding_2,
        R.drawable.img_onboarding_3,
        R.drawable.img_onboarding_4
    )
    private val position by lazy {
        runCatching { arguments?.getInt(ARG_POSITION) }.getOrNull() ?: 0
    }
    private val viewModel: OnboardingViewModel by activityViewModel()

    override fun FragmentOnboardingBinding.initView() {
        if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            binding.lavSwipe.scaleX = -1f
        } else {
            binding.lavSwipe.scaleX = 1f
        }
        lavSwipe.isVisible = position == 1
        tvSwipe.isVisible = position == 1
        imgOnbBackground?.load(listOBImage[position])
        when (position) {
            0 -> {
                if (!viewModel.isPreloadNativeFull) {
                    viewModel.isPreloadNativeFull = true
                    activity?.let {
                        NativeAdPreloadManager.preloadAd(
                            activity = it,
                            placement = NativePlacement.ONBOARDING_FULL_1
                        )
                        NativeAdPreloadManager.preloadAd(
                            activity = it,
                            placement = NativePlacement.ONBOARDING_FULL_2
                        )
                    }
                }
            }

            3 -> {
                if (!viewModel.isPreloadNativeFeature) {
                    viewModel.isPreloadNativeFeature = true
                    activity?.let {
                        if (remoteConfig.featureScreenConfig.isEnableScreen1) {
                            NativePlacement.SELECT.preloadAdConfig(it, 1)
                        }
                        if (remoteConfig.featureScreenConfig.isEnableScreen2) {
                            NativePlacement.SELECT_DUP.preloadAdConfig(it, 2)
                        }
                    }
                }
            }
        }
    }

    override fun FragmentOnboardingBinding.onClick() = Unit

    override fun onResume() {
        super.onResume()
        viewModel.trackScreenView(false, position)
    }
}
