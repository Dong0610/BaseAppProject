package com.b096.dramarush5.ads.model

import com.google.gson.annotations.SerializedName

data class AdRewardConfig(
    @SerializedName("enable")
    var enable: Boolean,
    @SerializedName("list_ads")
    val listAds: List<AdConfig>,
) {
    companion object {
        val rewardedDownload = AdRewardConfig(
            enable = true,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5019989394447925/6698835283"
                ),
            )
        )
    }
}