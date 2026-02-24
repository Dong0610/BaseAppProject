package com.b096.dramarush5.ads.model

import com.google.gson.annotations.SerializedName

data class AppOpenAdConfig(
    @SerializedName("enable")
    var enable: Boolean,
    @SerializedName("list_ads")
    val listAds: List<AdConfig>,
) {
    companion object {
        val appOpenAdConfig = AppOpenAdConfig(
            enable = true,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/7011718480"
                ),
            )
        )
    }
}