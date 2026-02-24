package com.b096.dramarush5.ads.wrapper.interstitial

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.dong.baselib.lifecycle.post

object InterstitialAdManager {
    var isCloseInterSplash = MutableLiveData(false)
    private var interAll = InterstitialAdsWrapper(InterstitialPlacement.INTER_ALL)
     var isInterPaywallClose = MutableLiveData(false)
    fun resetInterAllConfig() {
        interAll.resetConfig()
    }

    fun updateTimeShowInterAll() {
        interAll.updateTimeShowInter()
    }

    fun loadInterAll(context: Context) {
        interAll.preloadAd(context)
    }

    fun showInterAll(activity: Activity?, onNextAction: () -> Unit) {
        activity?.let {
            interAll.showAd(activity, true, onNextAction)
        } ?: run { onNextAction() }
    }


    private var interPaywall = InterstitialAdsWrapper(InterstitialPlacement.INTER_PAYWALL)

    fun resetInterPaywallConfig() {
        interPaywall.resetConfig()
    }

    fun updateTimeShowInterPaywall() {
        interPaywall.updateTimeShowInter()
    }

    fun loadInterPaywall(context: Context) {
        interPaywall.preloadAd(context)
    }

    fun showInterPaywall(activity: Activity?, onNextAction: () -> Unit) {
        activity?.let {
            interPaywall.showAd(activity, false, onNextAction) {
                isInterPaywallClose.postValue(it)
            }
        } ?: run { onNextAction() }
    }
}
