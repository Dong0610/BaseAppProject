package com.b096.dramarush5.ads.model.splash

import com.b096.dramarush5.ads.model.config.SplashType
import com.google.gson.annotations.SerializedName

data class AdSplashConfig(
    @SerializedName("enable")
    var enable: Boolean,
    @SerializedName("total_timeout_ms")
    val totalTimeout: Long,
    @SerializedName("list_ads")
    val listAds: List<SplashConfig>,
) {
    companion object {
        val adSplashConfig = AdSplashConfig(
            enable = true,
            totalTimeout = 30000,
            listAds = listOf(
                SplashConfig(
                    enableAd = true,
                    type = SplashType.Inter.type,
                    timeout = 25000,
                    adUnit = "ca-app-pub-5417263955398589/3264045165"
                ),
            )
        )
    }
}