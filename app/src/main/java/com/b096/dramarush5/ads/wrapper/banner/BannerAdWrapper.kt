package com.b096.dramarush5.ads.wrapper.banner

import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ads.control.funtion.AdCallback
import com.ads.control.helper.banner.BannerAdConfig
import com.ads.control.helper.banner.BannerAdHelper
import com.ads.control.helper.banner.params.BannerAdParam
import com.ads.control.util.AppConstant.CollapsibleGravity
import com.b096.dramarush5.app.remoteConfig
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class BannerAdWrapper(
    private val activity: AppCompatActivity,
    private val config: BannerPlacement,
    private val lifecycleOwner: LifecycleOwner,
    private val adContainer: () -> FrameLayout,
) : DefaultLifecycleObserver {

    private var activityRef: WeakReference<AppCompatActivity> = WeakReference(activity)

    private val bannerAdHelper by lazy {
        BannerAdHelperFactory.create(
            activity = activity,
            placement = config,
            lifecycleOwner = lifecycleOwner
        )
    }

    // ===== Auto-reload state =====
    private var autoReloadEnabled: Boolean = false
    private var reloadIntervalMs: Long = 0L
    private var reloadJob: Job? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        stopAutoReload()
        lifecycleOwner.lifecycle.removeObserver(this)
        super.onDestroy(owner)
    }

    override fun onPause(owner: LifecycleOwner) {
        stopAutoReload()
        super.onPause(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        if (autoReloadEnabled && reloadIntervalMs > 0) {
            startAutoReload(owner)
        }
        super.onResume(owner)
    }

    private fun cleanup() {
        bannerAdHelper.cancel()
        activityRef.clear()
    }

    fun setupBannerAd(tag: String? = null) {
        bannerAdHelper.apply {
            setBannerContentView(adContainer.invoke())
            tag?.let { setTagForDebug(it) }
        }
    }

    fun cancelRequest() {
        bannerAdHelper.cancel()
    }

    fun requestAds() {
        bannerAdHelper.requestAds(BannerAdParam.Request.create())
    }

    fun registerAdCallbacks(
        onLoaded: (() -> Unit)? = null,
        onFailed: ((LoadAdError?) -> Unit)? = null,
        onClicked: (() -> Unit)? = null,
        onImpression: (() -> Unit)? = null
    ) {
        bannerAdHelper.registerAdListener(object : AdCallback() {
            override fun onAdClicked() {
                super.onAdClicked()
                onClicked?.invoke()
            }

            override fun onAdImpression() {
                super.onAdImpression()
                onImpression?.invoke()
            }

            override fun onBannerLoaded(adView: AdView?) {
                super.onBannerLoaded(adView)
                onLoaded?.invoke()
            }

            override fun onAdFailedToLoad(i: LoadAdError?) {
                super.onAdFailedToLoad(i)
                onFailed?.invoke(i)
            }
        })
    }

    fun setFlagReload(isReload: Boolean) {
        bannerAdHelper.flagUserEnableReload = isReload
    }

    fun setAutoReload(enabled: Boolean, intervalSeconds: Long) = apply {
        autoReloadEnabled = enabled
        reloadIntervalMs = intervalSeconds
    }

    private fun startAutoReload(owner: LifecycleOwner) {
        stopAutoReload()
        if (!autoReloadEnabled || reloadIntervalMs <= 0L) return
        reloadJob = owner.lifecycleScope.launch {
            while (isActive) {
                delay(reloadIntervalMs)
                requestAds()
            }
        }
    }

    private fun stopAutoReload() {
        reloadJob?.cancel()
        reloadJob = null
    }
}

internal object BannerAdHelperFactory {
    fun create(
        activity: AppCompatActivity,
        placement: BannerPlacement,
        lifecycleOwner: LifecycleOwner? = null,
    ): BannerAdHelper {
        val canShowAds = placement.canShowAds()
        return BannerAdHelper(
            activity = activity,
            lifecycleOwner ?: activity,
            createBannerAdConfig(placement, canShowAds)
        ).apply {
            setEnableListBanner(true)
        }
    }

    private fun createBannerAdConfig(
        placement: BannerPlacement,
        canShowAds: Boolean,
    ): BannerAdConfig {
        return BannerAdConfig(
            idAds = placement.listId().lastOrNull() ?: "",
            canShowAds = canShowAds,
            canReloadAds = true,
        ).apply {
            setListId(placement.listId())
            if (placement.isCollapsible()) {
                this.collapsibleGravity = CollapsibleGravity.BOTTOM
            }
        }
    }
}

enum class BannerPlacement(
    val listId: () -> List<String>,
    val canShowAds: () -> Boolean,
    val isCollapsible: () -> Boolean,
) {
    BANNER_ALL(
        listId = {
            remoteConfig.bannerAllConfig.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.bannerAllConfig.enable },
        isCollapsible = { remoteConfig.bannerAllConfig.isCollapsible },
    )
}