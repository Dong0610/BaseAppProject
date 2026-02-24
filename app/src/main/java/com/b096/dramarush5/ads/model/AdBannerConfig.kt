package com.b096.dramarush5.ads.model

import com.google.gson.annotations.SerializedName

data class AdBannerConfig(
    @SerializedName("enable")
    var enable: Boolean,
    @SerializedName("is_collapsible")
    val isCollapsible: Boolean,
    @SerializedName("list_ads")
    val listAds: List<AdConfig>,
) {
    companion object {
        val adBannerConfig = AdBannerConfig(
            enable = true,
            isCollapsible = false,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/3399776536"
                )
            )
        )
    }
}