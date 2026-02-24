package com.b096.dramarush5.ads.model

import com.google.gson.annotations.SerializedName

data class AdInterConfig(
    @SerializedName("enable")
    var enable: Boolean,
    @SerializedName("time_interval_ms")
    val timeInterval: Long,
    @SerializedName("time_steps")
    val timeSteps: List<Int>,
    @SerializedName("list_ads")
    val listAds: List<AdConfig>,
) {
    companion object {
        val adInterConfig = AdInterConfig(
            enable = true,
            timeInterval = 30000,
            timeSteps = listOf(0),
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/1081277522"
                )
            )
        )
        val adInterPaywall = AdInterConfig(
            enable = true,
            timeInterval = 30000,
            timeSteps = listOf(0),
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/8848086907"
                )
            )
        )
    }
}