package com.b096.dramarush5.ads.wrapper.banner

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.b096.dramarush5.databinding.LayoutBannerAdBinding
import com.dong.baselib.api.UnitFun0
import com.dong.baselib.api.UnitFun1
import com.dong.baselib.api.emptyLambda
import com.dong.baselib.api.emptyLambda1
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError

class BannerAdView @JvmOverloads constructor(
      context: Context,
      attrs: AttributeSet? = null,
      defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val binding = LayoutBannerAdBinding.inflate(LayoutInflater.from(context), this, true)
    private var activity: AppCompatActivity? = null
    private var onAdImpression: UnitFun0 = emptyLambda

    fun onOnAdImpression(value: UnitFun0) = apply {
        this.onAdImpression = value
    }

    private var onFailed: UnitFun1<LoadAdError?> = emptyLambda1()

    fun onOnAdImpression(value: UnitFun1<LoadAdError?>) = apply {
        this.onFailed = value
    }

    private var bannerAdWrapper: BannerAdWrapper? = null
    fun attach(
          activity: AppCompatActivity?,
          placement: BannerPlacement,
          tag: String = "banner_ads_view"
    ) = apply {
        this@BannerAdView.activity = activity
        activity?.let { activity ->
            bannerAdWrapper = BannerAdWrapper(
                activity = activity,
                config = placement,
                lifecycleOwner = activity,
                adContainer = { binding.flBanner }
            )
            bannerAdWrapper?.setupBannerAd(tag)
            bannerAdWrapper?.registerAdCallbacks(onFailed = {
                onFailed(it)
            }) {
                onAdImpression()
            }
        }
    }

    fun setAutoReload(isReload: Boolean, timeReload: Long) = apply {
        bannerAdWrapper?.setAutoReload(enabled = isEnabled, intervalSeconds = timeReload)
    }

    fun requestAd() {
        bannerAdWrapper?.requestAds()
    }

}