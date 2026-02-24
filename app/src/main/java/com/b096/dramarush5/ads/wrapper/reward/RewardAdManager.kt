package com.b096.dramarush5.ads.wrapper.reward

import android.app.Activity

object RewardAdManager {
    private var rewardAll = RewardAdsWrapper(RewardPlacement.REWARD_ALL)

    fun loadRewardAll(activity: Activity) {
        rewardAll.preload(activity)
    }

    fun showRewardAll(
        activity: Activity?,
        onNextAction: () -> Unit,
        onAdNotReady: () -> Unit,
    ) {
        activity?.let {
            rewardAll.showAd(activity, onNextAction, onAdNotReady)
        } ?: run {
            onNextAction()
        }

    }
}