package com.b096.dramarush5.ads.model.config

import com.google.gson.annotations.SerializedName

data class LanguageLoading(
    @SerializedName("enable_screen")
    var enableScreen: Boolean,
    @SerializedName("show_time_ms")
    val showTimeMs: Long,
) {
    companion object {
        val languageLoading = LanguageLoading(
            enableScreen = true,
            showTimeMs = 2000,
        )
    }
}