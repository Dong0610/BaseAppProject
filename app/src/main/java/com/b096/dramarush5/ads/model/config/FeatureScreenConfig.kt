package com.b096.dramarush5.ads.model.config

import com.google.gson.annotations.SerializedName

data class FeatureScreenConfig(
    @SerializedName("screen_1")
    val isEnableScreen1: Boolean,
    @SerializedName("screen_2")
    val isEnableScreen2: Boolean,
) {
    companion object {
        val defaultSelectScreen = FeatureScreenConfig(
            isEnableScreen1 = true,
            isEnableScreen2 = true,
        )
    }
}
