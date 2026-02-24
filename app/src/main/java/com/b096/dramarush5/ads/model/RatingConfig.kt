package com.b096.dramarush5.ads.model

import com.google.gson.annotations.SerializedName

data class RatingConfig(
    @SerializedName("add_playlist")
    val addPlaylist: List<Int> = listOf(),
    @SerializedName("home")
    val home: List<Int> = listOf(),
    @SerializedName("play_channel")
    val playChannel: List<Int> = listOf(),
) {
    companion object {
        val defaultRateConfig = RatingConfig(
            addPlaylist = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
            home = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
            playChannel = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        )
    }
}