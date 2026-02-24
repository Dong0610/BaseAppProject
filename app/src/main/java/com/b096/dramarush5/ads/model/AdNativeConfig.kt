package com.b096.dramarush5.ads.model

import com.b096.dramarush5.ads.model.config.LayoutNativeType
import com.google.gson.annotations.SerializedName

data class AdNativeConfig(
    @SerializedName("enable")
    var enable: Boolean,
    @SerializedName("type_layout")
    val layout: String,
    @SerializedName("list_ads")
    val listAds: List<AdConfig>,
) {
    companion object {
        val nativeSplashConfig = AdNativeConfig(
            enable = true,
            layout = LayoutNativeType.NativeMediumMediaLeftCtaBottom.type,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/2003369150"
                ),
            )
        )
        val nativeLangLoading = AdNativeConfig(
            enable = true,
            layout = LayoutNativeType.NativeMediumMediaLeftCtaBottom.type,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/1348763037"
                ),
            )
        )
        val nativeLanguage1 = AdNativeConfig(
            enable = true,
            layout = LayoutNativeType.NativeMediumMediaLeftCtaBottom.type,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/5998019713"
                ),
            )
        )
        val nativeLanguage2 = AdNativeConfig(
            enable = true,
            layout = LayoutNativeType.NativeMediumMediaLeftCtaBottom.type,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/8875464496"
                ),
            )
        )
        val nativeOnboarding1 = AdNativeConfig(
            enable = true,
            layout = LayoutNativeType.NativeMediumMediaLeftCtaBottom.type,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/6090061623"
                ),
            )
        )
        val nativeOnboarding2 = AdNativeConfig(
            enable = true,
            layout = LayoutNativeType.NativeMediumMediaLeftCtaBottom.type,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/4876944504"
                ),
            )
        )
        val nativeOnboarding3 = AdNativeConfig(
            enable = true,
            layout = LayoutNativeType.NativeMediumMediaLeftCtaBottom.type,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/2146947189"
                ),
            )
        )
        val nativeOnboarding4 = AdNativeConfig(
            enable = true,
            layout = LayoutNativeType.NativeMediumMediaLeftCtaBottom.type,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/7203290178"
                ),
            )
        )
        val nativeOBFull1 = AdNativeConfig(
            enable = true,
            layout = LayoutNativeType.Other.type,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/8409103203"
                ),
            )
        )
        val nativeOBFull2 = AdNativeConfig(
            enable = true,
            layout = LayoutNativeType.Other.type,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/7096021534"
                ),
            )
        )
        val nativeFeature = AdNativeConfig(
            enable = true,
            layout = LayoutNativeType.NativeMediumMediaLeftCtaBottom.type,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/4469858194"
                ),
            )
        )
        val nativeFeatureDup = AdNativeConfig(
            enable = true,
            layout = LayoutNativeType.NativeMediumMediaLeftCtaBottom.type,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/9418389971"
                ),
            )
        )
        val nativeHome = AdNativeConfig(
            enable = true,
            layout = LayoutNativeType.NativeSmallCtaBottom.type,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/9498715799"
                ),
            )
        )
        val nativePopup = AdNativeConfig(
            enable = true,
            layout = LayoutNativeType.NativeSmallCtaRight.type,
            listAds = listOf(
                AdConfig(
                    enableAd = true,
                    adUnit = "ca-app-pub-5417263955398589/3591782999"
                )
            )
        )
    }

    fun isNativeSmall() = this.layout.contains("native_small")
    fun isNativeLeftMedia() = this.layout.contains("media_left")
    override fun toString(): String {
        val adsSummary = listAds
            .mapIndexed { index, ad ->
                "[$index] ${ad}"
            }
            .joinToString(separator = ", ")

        return "AdNativeConfig(" +
                "enable=$enable, " +
                "layout='$layout', " +
                "nativeSmall=${isNativeSmall()}, " +
                "leftMedia=${isNativeLeftMedia()}, " +
                "listAdsSize=${listAds.size}, " +
                "listAds=[$adsSummary]" +
                ")"
    }
}