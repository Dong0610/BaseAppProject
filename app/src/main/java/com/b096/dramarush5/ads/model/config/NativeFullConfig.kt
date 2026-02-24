package com.b096.dramarush5.ads.model.config

import com.google.gson.annotations.SerializedName

data class NativeFullConfig(
    @SerializedName("is_show_close")
    val isShowClose: Boolean,
    @SerializedName("delay_show_close_ms")
    val delayShowClose: Long,
    @SerializedName("time_delay_skip_ms")
    val timeDelaySkip: Long,
) {
    companion object {
        val configNativeFullSplash = NativeFullConfig(
            isShowClose = true,
            delayShowClose = 0,
            timeDelaySkip = 6000,
        )


        val configNativeFull1 = NativeFullConfig(
            isShowClose = true,
            delayShowClose = 1000,
            timeDelaySkip = 60000,
        )

        val configNativeFull2 = NativeFullConfig(
            isShowClose = true,
            delayShowClose = 0,
            timeDelaySkip = 3000,
        )
        val configNativeFullHome = NativeFullConfig(
            isShowClose = true,
            delayShowClose = 2000,
            timeDelaySkip = 3000,
        )
    }
}