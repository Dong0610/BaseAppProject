package com.b096.dramarush5.ads.model.splash

import com.google.gson.annotations.SerializedName

data class SplashConfig(
    @SerializedName("enable_ad")
    var enableAd: Boolean,
    @SerializedName("type")
    val type: String,
    @SerializedName("timeout_ms")
    val timeout: Long,
    @SerializedName("adunit")
    val adUnit: String,
)