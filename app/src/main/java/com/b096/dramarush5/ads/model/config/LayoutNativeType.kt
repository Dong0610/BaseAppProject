package com.b096.dramarush5.ads.model.config

sealed class LayoutNativeType(val type: String) {
    data object NativeSmallCtaBottom : LayoutNativeType("native_small_cta_bottom")
    data object NativeSmallCtaTop : LayoutNativeType("native_small_cta_top")
    data object NativeSmallCtaRight : LayoutNativeType("native_small_cta_right")
    data object NativeMediumCtaBottom : LayoutNativeType("native_medium_cta_bottom")
    data object NativeMediumCtaTop : LayoutNativeType("native_medium_cta_top")
    data object MediumCtaRightBottom : LayoutNativeType("medium_cta_right_bottom")
    data object MediumCtaRightTop : LayoutNativeType("medium_cta_right_top")
    data object NativeMediumMediaLeftCtaBottom : LayoutNativeType("native_medium_media_left_cta_bottom")
    data object NativeMediumMediaLeftCtaRight : LayoutNativeType("native_medium_media_left_cta_right")
    data object Other : LayoutNativeType("other")
}
