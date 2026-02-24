package com.b096.dramarush5.ads.model

import com.google.gson.annotations.SerializedName

data class AdConfig(
    @SerializedName("enable_ad")
    var enableAd: Boolean,
    @SerializedName("adunit")
    val adUnit: String,
)