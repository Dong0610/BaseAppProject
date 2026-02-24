package com.b096.dramarush5.ads.model.config

sealed class SplashType(val type: String) {
    data object Inter : SplashType("inter")
    data object AppOpen : SplashType("appopen")
    data object Native : SplashType("native")
}