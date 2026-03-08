package com.b096.dramarush5.ads.wrapper.native

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ads.control.ads.AzAdCallback
import com.ads.control.ads.AzAds
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApNativeAd
import com.ads.control.helper.AdOptionVisibility
import com.ads.control.helper.adnative.NativeAdConfig
import com.ads.control.helper.adnative.NativeAdHelper
import com.ads.control.helper.adnative.params.AdNativeMediation
import com.ads.control.helper.adnative.params.AdNativeState
import com.ads.control.helper.adnative.params.NativeAdParam
import com.ads.control.helper.adnative.params.NativeLayoutMediation
import com.ads.control.helper.adnative.preload.NativeAdPreload
import com.ads.control.helper.adnative.preload.NativePreloadState
import com.b096.dramarush5.R
import com.b096.dramarush5.ads.model.AdNativeConfig
import com.b096.dramarush5.ads.model.config.LayoutNativeType
import com.b096.dramarush5.app.remoteConfig
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * A wrapper for handling native ads, providing an interface for loading,
 * displaying, and managing ad states with proper lifecycle management and memory optimization.
 */

fun AppCompatActivity.createNativeConfig(
    placement: NativePlacement,
    adContainer: FrameLayout,
    shimmerFrameLayout: (AdNativeConfig) -> ShimmerFrameLayout
) = NativeAdsWrapper(
    activity = this,
    config = placement,
    lifecycleOwner = this,
    adContainer = { adContainer },
    shimmerView = { shimmerFrameLayout(it) })

fun Fragment.createNativeConfig(
    placement: NativePlacement,
    adContainer: FrameLayout,
    shimmerFrameLayout: (AdNativeConfig) -> ShimmerFrameLayout
): NativeAdsWrapper? = (activity as? AppCompatActivity)?.let { ac ->
    NativeAdsWrapper(
        activity = ac,
        config = placement,
        lifecycleOwner = this,
        adContainer = { adContainer },
        shimmerView = { shimmerFrameLayout(it) })
}

class NativeAdsWrapper(
    activity: AppCompatActivity,
    private val config: NativePlacement,
    private val lifecycleOwner: LifecycleOwner,
    private val adContainer: () -> FrameLayout,
    private val shimmerView: (AdNativeConfig) -> ShimmerFrameLayout
) : DefaultLifecycleObserver {
    private var activityRef: WeakReference<AppCompatActivity> = WeakReference(activity)

    private val nativeAdHelper: NativeAdHelper by lazy {
        NativeAdHelperFactory.create(
            activity = activity,
            placement = config,
            lifecycleOwner = lifecycleOwner
        )
    }
    private var azAdCallback = object : AzAdCallback() {

        override fun onAdFailedToLoad(adError: ApAdError?) {
            super.onAdFailedToLoad(adError)
            Log.e(
                "NativeAdsWrapper",
                "onAdFailedToLoad: ${adError?.message} ${config.toString()}"
            )
            lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                onFailedCallback(adError)
            }

        }

        override fun onAdImpression() {
            super.onAdImpression()
            lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                impressionCallback()
            }
        }
    }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        cleanup()
        super.onDestroy(owner)
    }

    private fun cleanup() {
        nativeAdHelper.cancel()
        nativeAdHelper.unregisterAdListener(azAdCallback)
        activityRef.clear()
    }

    /**
     * Get the number of available ads for a specific placement
     * @return The number of available ads
     */
    fun getAvailableAdCount(): Int = NativeAdPreloadManager.getAvailableAdCount(config)

    /**
     * Get the ad preload state flow for monitoring loading status
     * @return StateFlow emitting the current preload state
     */
    fun getAdPreloadState(): StateFlow<NativePreloadState> =
        NativeAdPreloadManager.getAdPreloadState(config)

    /**
     * Setup the native ad by configuring the container and shimmer views
     * @param tag Optional debug tag for the ad
     */
    fun setupNativeAd(tag: String? = null) = apply {
        nativeAdHelper.apply {
            setNativeContentView(adContainer.invoke())
            setShimmerLayoutView(shimmerView.invoke(config.nativeConfig()))
            tag?.let { setTagForDebug(it) }
            activityRef.get()?.let { activity ->
                if (config.displayLayoutId != null) {
                    setCustomContentView { nativeAd ->
                        nativeAd.layoutCustomNative =
                            config.displayLayoutId ?: config.preloadLayoutId()
                        AzAds.getInstance().populateNativeAdView(
                            activity,
                            nativeAd,
                            adContainer(),
                            shimmerView(config.nativeConfig())
                        )
                    }
                    registerAdListener(azAdCallback)
                }
            }
        }
    }

    fun setupNativeAd(tag: String? = null, block: NativeAdView.() -> Unit) = apply {
        nativeAdHelper.apply {
            setNativeContentView(adContainer.invoke())
            setShimmerLayoutView(shimmerView.invoke(config.nativeConfig()))
            tag?.let { setTagForDebug(it) }
            activityRef.get()?.let { activity ->
                setCustomContentView { nativeAd ->
                    val layout = config.displayLayoutId ?: config.preloadLayoutId()
                    nativeAd.layoutCustomNative = layout
                    AzAds.getInstance().populateNativeAdView(
                        activity,
                        nativeAd,
                        adContainer(),
                        shimmerView(config.nativeConfig())
                    )
                    val adView = adContainer().getChildAt(0) as? NativeAdView
                    adView?.block()
                    registerAdListener(azAdCallback)
                }
            }

        }
    }

    fun setFlagReload(isReload: Boolean) = apply {
        nativeAdHelper.flagUserEnableReload = isReload
    }

    private var impressionCallback: () -> Unit = {}
    fun onAdImpression(action: () -> Unit = {}) = apply {
        this.impressionCallback = action
    }

    private var onFailedCallback: (adError: ApAdError?) -> Unit = {}
    fun onAdFailed(action: (adError: ApAdError?) -> Unit = {}) = apply {
        this.onFailedCallback = action
    }

    /**
     * Register callbacks for ad events
     *
     * @param onLoaded Called when an ad is loaded successfully
     * @param onFailed Called when ad loading fails
     * @param onClicked Called when the ad is clicked
     * @param onImpression Called when the ad makes an impression
     */
    fun registerAdCallbacks(
        onLoaded: ((ApNativeAd) -> Unit)? = null,
        onFailed: ((ApAdError?) -> Unit)? = null,
        onClicked: (() -> Unit)? = null,
        onImpression: (() -> Unit)? = null
    ) = apply {
        nativeAdHelper.registerAdListener(object : AzAdCallback() {
            override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                super.onNativeAdLoaded(nativeAd)
                onLoaded?.invoke(nativeAd)
            }

            override fun onAdFailedToLoad(adError: ApAdError?) {
                super.onAdFailedToLoad(adError)
                onFailed?.invoke(adError)
                lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    onFailedCallback(adError)
                }

            }

            override fun onAdClicked() {
                super.onAdClicked()
                onClicked?.invoke()
            }

            override fun onAdImpression() {
                super.onAdImpression()
                onImpression?.invoke()
                lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    impressionCallback()
                }
            }
        })
    }

    /**
     * Get the ad state flow to monitor loading status
     * @return Flow emitting the current ad state
     */
    fun getAdState(): Flow<AdNativeState> = nativeAdHelper.getAdNativeState()

    /**
     * Cancel the current ad request
     */
    fun cancelRequest() {
        nativeAdHelper.cancel()
    }

    /**
     * Request ads using the configured parameters
     */
    fun requestAds() = lifecycleOwner.lifecycleScope.launch {
        shimmerView(config.nativeConfig()).run {
            visibility = View.VISIBLE
            startShimmer()
        }
        nativeAdHelper.requestAds(NativeAdParam.Request.create())
    }

    fun requestAdsFragment() {
        lifecycleOwner.lifecycleScope.launch {
            shimmerView(config.nativeConfig()).run {
                visibility = View.VISIBLE
                startShimmer()
            }
            nativeAdHelper.nativeAd?.let {
                nativeAdHelper.requestAds(NativeAdParam.Ready(it))
            } ?: run {
                nativeAdHelper.requestAds(NativeAdParam.Request.create())
            }
        }
    }

    fun requestAdsPreview() {
        Log.d("NativeRequest", "Data: ${config.toString()}")
        lifecycleOwner.lifecycleScope.launch {
            shimmerView(config.nativeConfig()).run {
                visibility = View.VISIBLE
                startShimmer()
            }
            nativeAdHelper.nativeAd?.let {
                nativeAdHelper.requestAds(NativeAdParam.Ready(it))
            }
            nativeAdHelper.requestAds(NativeAdParam.Request.create())
        }
    }

    fun pollAdNative(): ApNativeAd? {
        if (!config.canShowAds()) return null
        return NativeAdPreload.getInstance().pollAdNative(
            listId = config.listId(),
            layoutId = config.preloadLayoutId()
        )
    }

    /**
     * Attempts to safely preload ads for the specified placement.
     * @param buffer Number of ads to preload (default: 1)
     */
    fun safePreloadAd(buffer: Int = 1) {
        activityRef.get()?.let { activity ->
            NativeAdPreloadManager.safePreloadAd(activity, config, buffer)
        }
    }

    /**
     * Forces preloading of ads for the specified placement.
     * @param buffer Number of ads to preload (default: 1)
     */
    fun preloadAd(buffer: Int = 1) {
        activityRef.get()?.let { activity ->
            NativeAdPreloadManager.preloadAd(activity, config, buffer)
        }
    }
}

/**
 * Factory for creating NativeAdHelper instances with optimized configuration
 */
internal object NativeAdHelperFactory {
    /**
     * Creates a NativeAdHelper instance configured for the specified placement
     */
    fun create(
        activity: AppCompatActivity,
        placement: NativePlacement,
        lifecycleOwner: LifecycleOwner? = null,
    ): NativeAdHelper {
        val canShowAds = placement.canShowAds()

        return NativeAdHelper(
            activity,
            lifecycleOwner ?: activity,
            createNativeAdConfig(placement, canShowAds, placement.preloadLayoutId())
        ).apply {
            setEnableListNative(true)
            setEnablePreload(true)
            adVisibility = placement.adVisibility
        }
    }

    /**
     * Creates the ad configuration for the placement
     */
    private fun createNativeAdConfig(
        placement: NativePlacement,
        canShowAds: Boolean,
        finalLayoutId: Int,
    ): NativeAdConfig {
        return NativeAdConfig(
            idAds = placement.listId().lastOrNull() ?: "",
            canShowAds = canShowAds,
            canReloadAds = true,
            layoutId = finalLayoutId,
        ).apply {
            setListId(placement.listId())
            if (placement.layoutMeta != null) {
                setLayoutMediation(
                    NativeLayoutMediation(AdNativeMediation.FACEBOOK, placement.layoutMeta.invoke())
                )
            }
        }
    }
}

/**
 * Enum defining different ad placements with their configurations
 */
enum class NativePlacement(
    val listId: () -> List<String>,
    val canShowAds: () -> Boolean,
    val adVisibility: AdOptionVisibility = AdOptionVisibility.GONE,
    @LayoutRes val preloadLayoutId: () -> Int,
    @LayoutRes var displayLayoutId: Int? = null,
    val layoutMeta: (() -> Int)? = null,
    val nativeConfig: () -> AdNativeConfig
) {

    SPLASH(
        listId = {
            remoteConfig.nativeSplashConfig.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.nativeSplashConfig.enable },
        preloadLayoutId = {
            LayoutSelector.getLayout(remoteConfig.nativeSplashConfig.layout)
        },
        layoutMeta = { LayoutSelector.getMetaLayout(type = remoteConfig.nativeSplashConfig.layout) },
        nativeConfig = { remoteConfig.nativeSplashConfig }
    ),
    LANGUAGE_LOADING(
        listId = {
            remoteConfig.nativeLangWaitingConfig.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.nativeLangWaitingConfig.enable },
        preloadLayoutId = {
            LayoutSelector.getLayout(remoteConfig.nativeLangWaitingConfig.layout)
        },
        layoutMeta = { LayoutSelector.getMetaLayout(type = remoteConfig.nativeLangWaitingConfig.layout) },
        nativeConfig = { remoteConfig.nativeLangWaitingConfig }
    ),
    LANGUAGE_1(
        listId = {
            remoteConfig.nativeLang1Config.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.nativeLang1Config.enable },
        preloadLayoutId = {
            LayoutSelector.getLayout(remoteConfig.nativeLang1Config.layout)
        },
        layoutMeta = { LayoutSelector.getMetaLayout(type = remoteConfig.nativeLang1Config.layout) },
        nativeConfig = { remoteConfig.nativeLang1Config }
    ),
    LANGUAGE_2(
        listId = {
            remoteConfig.nativeLang2Config.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.nativeLang2Config.enable },
        preloadLayoutId = {
            LayoutSelector.getLayout(remoteConfig.nativeLang2Config.layout)
        },
        layoutMeta = { LayoutSelector.getMetaLayout(type = remoteConfig.nativeLang2Config.layout) },
        nativeConfig = { remoteConfig.nativeLang2Config }
    ),

    ONBOARDING_1(
        listId = {
            remoteConfig.nativeOnboarding1Config.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.nativeOnboarding1Config.enable },
        adVisibility = AdOptionVisibility.INVISIBLE,
        preloadLayoutId = {
            LayoutSelector.getLayout(remoteConfig.nativeOnboarding1Config.layout)
        },
        layoutMeta = { LayoutSelector.getMetaLayout(type = remoteConfig.nativeOnboarding1Config.layout) },
        nativeConfig = { remoteConfig.nativeOnboarding1Config }
    ),

    ONBOARDING_2(
        listId = {
            remoteConfig.nativeOnboarding2Config.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.nativeOnboarding2Config.enable },
        adVisibility = AdOptionVisibility.INVISIBLE,
        preloadLayoutId = {
            LayoutSelector.getLayout(remoteConfig.nativeOnboarding2Config.layout)
        },
        layoutMeta = { LayoutSelector.getMetaLayout(type = remoteConfig.nativeOnboarding2Config.layout) },
        nativeConfig = { remoteConfig.nativeOnboarding2Config }
    ),

    ONBOARDING_3(
        listId = {
            remoteConfig.nativeOnboarding3Config.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.nativeOnboarding3Config.enable },
        adVisibility = AdOptionVisibility.INVISIBLE,
        preloadLayoutId = {
            LayoutSelector.getLayout(remoteConfig.nativeOnboarding3Config.layout)
        },
        layoutMeta = { LayoutSelector.getMetaLayout(type = remoteConfig.nativeOnboarding3Config.layout) },
        nativeConfig = { remoteConfig.nativeOnboarding3Config }
    ),

    ONBOARDING_4(
        listId = {
            remoteConfig.nativeOnboarding4Config.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.nativeOnboarding4Config.enable },
        adVisibility = AdOptionVisibility.INVISIBLE,
        preloadLayoutId = {
            LayoutSelector.getLayout(remoteConfig.nativeOnboarding4Config.layout)
        },
        layoutMeta = { LayoutSelector.getMetaLayout(type = remoteConfig.nativeOnboarding4Config.layout) },
        nativeConfig = { remoteConfig.nativeOnboarding4Config }
    ),

    ONBOARDING_FULL_1(
        listId = {
            remoteConfig.nativeObFull1Config.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.nativeObFull1Config.enable },
        preloadLayoutId = {
            R.layout.layout_native_full_screen
        },
        layoutMeta = { LayoutSelector.getMetaLayout(isFullScreen = true) },
        nativeConfig = { remoteConfig.nativeObFull1Config }
    ),

    ONBOARDING_FULL_2(
        listId = {
            remoteConfig.nativeObFull2Config.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.nativeObFull2Config.enable },
        preloadLayoutId = {
            R.layout.layout_native_full_screen
        },
        layoutMeta = { LayoutSelector.getMetaLayout(isFullScreen = true) },
        nativeConfig = { remoteConfig.nativeObFull2Config }
    ),

    SELECT(
        listId = {
            remoteConfig.nativeSelectConfig.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.nativeSelectConfig.enable },
        preloadLayoutId = {
            LayoutSelector.getLayout(remoteConfig.nativeSelectConfig.layout)
        },
        layoutMeta = { LayoutSelector.getMetaLayout(type = remoteConfig.nativeSelectConfig.layout) },
        nativeConfig = { remoteConfig.nativeSelectConfig }
    ),

    SELECT_DUP(
        listId = {
            remoteConfig.nativeSelectDupConfig.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.nativeSelectDupConfig.enable },
        preloadLayoutId = {
            LayoutSelector.getLayout(remoteConfig.nativeSelectDupConfig.layout)
        },
        layoutMeta = { LayoutSelector.getMetaLayout(type = remoteConfig.nativeSelectDupConfig.layout) },
        nativeConfig = { remoteConfig.nativeSelectDupConfig }
    ),

    HOME(
        listId = {
            remoteConfig.nativeHomeConfig.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.nativeHomeConfig.enable },
        preloadLayoutId = {
            LayoutSelector.getLayout(remoteConfig.nativeHomeConfig.layout)
        },
        layoutMeta = { LayoutSelector.getMetaLayout(type = remoteConfig.nativeHomeConfig.layout) },
        nativeConfig = { remoteConfig.nativeHomeConfig }
    ),

    POPUP(
        listId = {
            remoteConfig.nativePopupConfig.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.nativePopupConfig.enable },
        preloadLayoutId = {
            LayoutSelector.getLayout(remoteConfig.nativePopupConfig.layout)
        },
        layoutMeta = { LayoutSelector.getMetaLayout(type = remoteConfig.nativePopupConfig.layout) },
        nativeConfig = { remoteConfig.nativePopupConfig }
    );

    override fun toString(): String {
        return "NativePlacement(listId=${listId()}, canShowAds=${canShowAds()}, adVisibility=$adVisibility, layoutMeta=$layoutMeta), Config: ${nativeConfig()}"
    }

    companion object {
        fun NativePlacement.preloadAdConfig(
            activity: Activity,
            count: Int = 1,
        ) {
            NativeAdPreloadManager.preloadAd(activity, placement = this, buffer = count)
        }

        fun NativePlacement.safePreloadAd(
            activity: Activity,
            count: Int = 1,
        ) {
            if (this.canShowAds()) {
                NativeAdPreloadManager.safePreloadAd(
                    activity = activity,
                    placement = this,
                    buffer = count
                )
            }

        }
    }
}

object LayoutSelector {
    fun getMetaLayout(isFullScreen: Boolean = false, type: String = ""): Int {
        return if (isFullScreen) {
            if (remoteConfig.metaCtrLow) {
                R.layout.layout_native_full_screen_meta_low
            } else {
                R.layout.layout_native_full_screen_meta_high
            }
        } else {
            if (remoteConfig.metaCtrLow) {
                when (type) {
                    LayoutNativeType.NativeSmallCtaBottom.type -> R.layout.layout_native_small_meta_cta_bot_low
                    LayoutNativeType.NativeSmallCtaTop.type -> R.layout.layout_native_small_meta_cta_top_low
                    LayoutNativeType.NativeSmallCtaRight.type -> R.layout.layout_native_small_cta_right
                    LayoutNativeType.NativeMediumCtaBottom.type -> R.layout.layout_native_medium_meta_cta_bot_low
                    LayoutNativeType.NativeMediumCtaTop.type -> R.layout.layout_native_medium_meta_cta_top_low
                    LayoutNativeType.MediumCtaRightBottom.type -> R.layout.layout_native_medium_cta_bot_right_meta
                    LayoutNativeType.MediumCtaRightTop.type -> R.layout.layout_native_medium_cta_top_right_meta
                    LayoutNativeType.NativeMediumMediaLeftCtaBottom.type -> R.layout.layout_native_medium_meta_low_left_cta_bottom
                    LayoutNativeType.NativeMediumMediaLeftCtaRight.type -> R.layout.layout_native_medium_meta_low_left_cta_right
                    else -> R.layout.layout_native_small_meta_cta_bot_low
                }
            } else {
                when (type) {
                    LayoutNativeType.NativeSmallCtaBottom.type -> R.layout.layout_native_small_meta_cta_bot_high
                    LayoutNativeType.NativeSmallCtaTop.type -> R.layout.layout_native_small_meta_cta_top_high
                    LayoutNativeType.NativeSmallCtaRight.type -> R.layout.layout_native_small_cta_right
                    LayoutNativeType.NativeMediumCtaBottom.type -> R.layout.layout_native_medium_meta_cta_bot_high
                    LayoutNativeType.NativeMediumCtaTop.type -> R.layout.layout_native_medium_meta_cta_top_high
                    LayoutNativeType.MediumCtaRightBottom.type -> R.layout.layout_native_medium_cta_bot_right_meta
                    LayoutNativeType.MediumCtaRightTop.type -> R.layout.layout_native_medium_cta_top_right_meta
                    LayoutNativeType.NativeMediumMediaLeftCtaBottom.type -> R.layout.layout_native_medium_meta_high_left_cta_bottom
                    LayoutNativeType.NativeMediumMediaLeftCtaRight.type -> R.layout.layout_native_medium_meta_high_left_cta_right
                    else -> R.layout.layout_native_small_meta_cta_bot_high
                }
            }
        }
    }

    fun getLayout(type: String): Int {
        return when (type) {
            LayoutNativeType.NativeSmallCtaBottom.type -> R.layout.layout_native_small_cta_bot
            LayoutNativeType.NativeSmallCtaTop.type -> R.layout.layout_native_small_cta_top
            LayoutNativeType.NativeSmallCtaRight.type -> R.layout.layout_native_small_cta_right
            LayoutNativeType.NativeMediumCtaBottom.type -> R.layout.layout_native_medium_cta_bot
            LayoutNativeType.NativeMediumCtaTop.type -> R.layout.layout_native_medium_cta_top
            LayoutNativeType.MediumCtaRightBottom.type -> R.layout.layout_native_medium_cta_bot_right
            LayoutNativeType.MediumCtaRightTop.type -> R.layout.layout_native_medium_cta_top_right
            LayoutNativeType.NativeMediumMediaLeftCtaBottom.type -> R.layout.layout_native_medium_left_cta_bottom
            LayoutNativeType.NativeMediumMediaLeftCtaRight.type -> R.layout.layout_native_medium_left_cta_right
            else -> R.layout.layout_native_small_cta_bot
        }
    }
}