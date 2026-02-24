package com.b096.dramarush5.ads.wrapper.interstitial

import android.app.Activity
import android.content.Context
import com.ads.control.ads.AzAdCallback
import com.ads.control.ads.AzAds
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApInterstitialAd
import com.ads.control.billing.AppPurchase
import com.b096.dramarush5.app.isInternetAvailable
import com.b096.dramarush5.app.remoteConfig
import com.dong.baselib.api.UnitFun1
import com.dong.baselib.api.emptyLambda1

class InterstitialAdsWrapper(
    private val config: InterstitialPlacement,
) {
    private var lastTimeInterstitialShow = 0L
    private var isRequestAds = false
    private var interAd: ApInterstitialAd? = null
    private var currentStepInter = 1
    private val showType by lazy {
        when {
            config.timeInterval() > 0 -> InterShowType.INTERVAL
            else -> InterShowType.NONE
        }
    }

    private fun isAdsReady(): Boolean {
        return interAd?.isReady == true
    }

    fun resetConfig() {
        lastTimeInterstitialShow = 0
        currentStepInter = 1
    }

    fun updateTimeShowInter() {
        lastTimeInterstitialShow = System.currentTimeMillis()
    }

    fun preloadAd(context: Context) {
        if (!isRequestAds
            && !isAdsReady()
            && context.isInternetAvailable()
            && config.canShowAds()
            && !AppPurchase.getInstance().isPurchased
        ) {
            isRequestAds = true
            AzAds.getInstance().getInterstitialAdsList(
                context,
                config.listId(),
                object : AzAdCallback() {
                    override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        interAd = interstitialAd
                        isRequestAds = false
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        isRequestAds = false
                    }
                }
            )
        }
    }

    fun showAd(
        activity: Activity,
        shouldReloadAds: Boolean = false,
        onNextAction: () -> Unit = {},
        isCloseInter: UnitFun1<Boolean> = emptyLambda1()
    ) {
        if (AppPurchase.getInstance().isPurchased) {
            onNextAction()
            return
        }
        when (showType) {
            InterShowType.INTERVAL -> {
                showInterInterval(activity, shouldReloadAds, onNextAction, isCloseInter)
            }

            InterShowType.NONE -> {
                showInterNone(activity, shouldReloadAds, onNextAction, isCloseInter)
            }
        }
    }

    private fun showInterInterval(
        activity: Activity,
        shouldReloadAds: Boolean = false,
        onNextAction: () -> Unit = {},
        isCloseInter: UnitFun1<Boolean>,
    ) {
        isCloseInter(false)
        if (canShowAdsInterval() || canShowAdsStepInterval()) {
            AzAds.getInstance().forceShowInterstitial(
                activity,
                interAd,
                object : AzAdCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        lastTimeInterstitialShow = System.currentTimeMillis()
                        interAd = null
                        if (shouldReloadAds) {
                            preloadAd(activity)
                        }
                        isCloseInter(true)
                    }

                    override fun onAdFailedToShow(adError: ApAdError?) {
                        super.onAdFailedToShow(adError)
                        isCloseInter(true)
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        isCloseInter(true)
                    }
                },
                false,
            )
        } else {
            onNextAction()
            isCloseInter(true)
        }
        currentStepInter++
    }


    private fun showInterNone(
        activity: Activity,
        shouldReloadAds: Boolean = false,
        onNextAction: () -> Unit = {},
        isCloseInter: UnitFun1<Boolean>,
    ) {
        isCloseInter(false)
        if (canShowAdsStep()) {
            AzAds.getInstance().forceShowInterstitial(
                activity,
                interAd,
                object : AzAdCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()

                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        lastTimeInterstitialShow = System.currentTimeMillis()
                        interAd = null
                        if (shouldReloadAds) {
                            preloadAd(activity)
                        }
                        isCloseInter(true)
                    }

                    override fun onAdFailedToShow(adError: ApAdError?) {
                        super.onAdFailedToShow(adError)
                        isCloseInter(true)
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        isCloseInter(true)
                    }
                },
                false,
            )
        } else {
            isCloseInter(true)
            onNextAction()
        }
        currentStepInter++
    }

    private fun canShowAdsInterval(): Boolean {
        val timeInterval = System.currentTimeMillis() - lastTimeInterstitialShow
        return timeInterval > config.timeInterval()
                && isAdsReady()
    }

    private fun canShowAdsStepInterval(): Boolean {
        return if (config.timeSteps().isEmpty()) {
            false
        } else {
            config.timeSteps().contains(currentStepInter) && isAdsReady()
        }
    }

    private fun canShowAdsStep(): Boolean {
        return if (config.timeSteps().isEmpty() && isAdsReady()) {
            true
        } else {
            config.timeSteps().contains(currentStepInter) && isAdsReady()
        }
    }
}

enum class InterShowType {
    INTERVAL,
    NONE
}

enum class InterstitialPlacement(
    val listId: () -> List<String>,
    val canShowAds: () -> Boolean,
    val timeInterval: () -> Long,
    val timeSteps: () -> List<Int>,
) {
    INTER_ALL(
        listId = {
            remoteConfig.interAllConfig.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.interAllConfig.enable },
        timeInterval = { remoteConfig.interAllConfig.timeInterval },
        timeSteps = { remoteConfig.interAllConfig.timeSteps },
    ),
    INTER_PAYWALL(
        listId = {
            remoteConfig.interPaywallConfig.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.interPaywallConfig.enable },
        timeInterval = { remoteConfig.interPaywallConfig.timeInterval },
        timeSteps = { remoteConfig.interPaywallConfig.timeSteps },
    ),
    ;
}