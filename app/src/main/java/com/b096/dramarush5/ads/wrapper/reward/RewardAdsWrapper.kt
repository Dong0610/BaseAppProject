package com.b096.dramarush5.ads.wrapper.reward

import android.app.Activity
import android.util.Log
import com.ads.control.ads.AzAdCallback
import com.ads.control.ads.AzAds
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApRewardAd
import com.ads.control.billing.AppPurchase
import com.b096.dramarush5.app.isInternetAvailable
import com.b096.dramarush5.app.remoteConfig

class RewardAdsWrapper(
      private val config: RewardPlacement,
) {
    private var isRequestAds = false
    private var rewardAd: ApRewardAd? = null

    private fun isAdsReady(): Boolean {
        return rewardAd?.isReady == true
    }

    fun preload(activity: Activity) {
        if (!isRequestAds
            && !isAdsReady()
            && activity.isInternetAvailable()
            && config.canShowAds()
            && !AppPurchase.getInstance().isPurchased
        ) {
            isRequestAds = true
            AzAds.getInstance().loadRewardAdList(
                activity,
                config.listId(),
                object : AzAdCallback() {
                    override fun onRewardAdLoaded(apRewardAd: ApRewardAd?) {
                        super.onRewardAdLoaded(apRewardAd)
                        rewardAd = apRewardAd
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
          onNextAction: () -> Unit = {},
          onAdNotReady: () -> Unit = {},
    ) {
        if (AppPurchase.getInstance().isPurchased) {
            onNextAction()
            return
        }
        if (isAdsReady()) {
            AzAds.getInstance().forceShowRewardAd(
                activity,
                rewardAd,
                object : AzAdCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        rewardAd = null
                        onNextAction()
                    }

                    override fun onAdFailedToShow(adError: ApAdError?) {
                        super.onAdFailedToShow(adError)
                        onNextAction()
                    }
                }
            )
        } else {
            if (config.canShowAds()) {
                onAdNotReady()
            } else {
                onNextAction()
            }
        }
    }
}

enum class RewardPlacement(
      val listId: () -> List<String>,
      val canShowAds: () -> Boolean,
) {
    REWARD_ALL(
        listId = {
            remoteConfig.rewardedAllConfig.listAds.filter { it.enableAd }.map { it.adUnit }
        },
        canShowAds = { remoteConfig.rewardedAllConfig.enable },
    )
}