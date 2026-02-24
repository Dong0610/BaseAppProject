package com.b096.dramarush5.ads.admob.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.ads.control.admob.Admob
import com.ads.control.ads.AzAdCallback
import com.ads.control.ads.AzAds
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApNativeAd
import com.ads.control.ads.wrapper.StatusAd
import com.ads.control.billing.AppPurchase
import com.b096.dramarush5.R
import com.dong.baselib.extensions.gone
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.nativead.NativeAdView

class AdPlacer(
    private val activity: Activity,
    private val adapterOriginal: RecyclerView.Adapter<*>,
    private val settings: AdPlacerSettings,
) {
    companion object {
        var TAG = AdPlacer::class.java.simpleName
    }

    private val listAd = HashMap<Int, ApNativeAd?>()
    private val listAdImpression = HashMap<Int, Boolean>()
    private val listPositionAd: MutableList<Int> = ArrayList()
    private var nativeDefault: ApNativeAd? = null

    init {
        configData()
    }

    fun configData() {
        if (AppPurchase.getInstance().isPurchased(activity)) {
            listAd.clear()
            listPositionAd.clear()
            listAdImpression.clear()
            return
        }
        if (settings.isRepeatingAd()) {
            var posAddAd = 0
            var countNewAdapter = adapterOriginal.itemCount
            if (settings.getStartRepeatingAd() >= 0 && countNewAdapter - settings.getStartRepeatingAd() > 0) {
                posAddAd += settings.getStartRepeatingAd()
                if (listAd[posAddAd] == null) {
                    listAd[posAddAd] = ApNativeAd(StatusAd.AD_INIT)
                    listAdImpression[posAddAd] = true
                    listPositionAd.add(posAddAd)
                }
                posAddAd++
                countNewAdapter++
            }

            while (posAddAd <= countNewAdapter - settings.getPositionFixAd()) {
                posAddAd += settings.getPositionFixAd()
                if (listAd[posAddAd] == null) {
                    listAd[posAddAd] = ApNativeAd(StatusAd.AD_INIT)
                    listAdImpression[posAddAd] = true
                    listPositionAd.add(posAddAd)
                }
                posAddAd++
                countNewAdapter++
            }
        } else {
            listPositionAd.add(settings.getPositionFixAd())
            listAd[settings.getPositionFixAd()] = ApNativeAd(StatusAd.AD_INIT)
            listAdImpression[settings.getPositionFixAd()] = true
        }
    }

    fun renderAd(pos: Int, holder: RecyclerView.ViewHolder) {
        val adNative = listAd[pos]
        if (adNative?.admobNativeAd == null) {
            if (listAd[pos]?.status != StatusAd.AD_LOADING) {
                holder.itemView.post {
                    val containerShimmer =
                        holder.itemView.findViewById<ShimmerFrameLayout>(R.id.shimmerContainerNative)
                    if (listAd[pos]?.status == StatusAd.AD_INIT && nativeDefault != null) {
                        listAd[pos] = nativeDefault
                        containerShimmer.gone()
                        populateAdToViewHolder(holder, nativeDefault!!)
                    }

                    var nativeAd = ApNativeAd(StatusAd.AD_LOADING)
                    listAd[pos] = nativeAd
                    val adCallback: AzAdCallback = object : AzAdCallback() {
                        override fun onNativeAdLoaded(apNativeAd: ApNativeAd) {
                            super.onNativeAdLoaded(apNativeAd)
                            nativeAd = apNativeAd
                            nativeAd.status = StatusAd.AD_LOADED
                            listAd[pos] = nativeAd
                            populateAdToViewHolder(holder, apNativeAd)
                        }

                        override fun onAdFailedToLoad(adError: ApAdError?) {
                            super.onAdFailedToLoad(adError)
                            containerShimmer.isGone = true
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                            listAdImpression[pos] = true
                        }
                    }
                    listAdImpression[pos] = false
                    AzAds.getInstance().loadNativeList(
                        activity,
                        settings.getListAdUnitId(),
                        settings.getLayoutCustomAd(),
                        adCallback
                    )
                }
            }
        } else {
            if (adNative.status == StatusAd.AD_LOADED) {
                holder.itemView.post {
                    if (listAdImpression[pos] == false) {
                        populateAdToViewHolder(holder, adNative)
                    } else {
                        var nativeAd = ApNativeAd(StatusAd.AD_LOADING)
                        listAd[pos] = nativeAd
                        val adCallback: AzAdCallback = object : AzAdCallback() {
                            override fun onNativeAdLoaded(apNativeAd: ApNativeAd) {
                                super.onNativeAdLoaded(apNativeAd)
                                nativeAd = apNativeAd
                                nativeAd.status = StatusAd.AD_LOADED
                                listAd[pos] = nativeAd
                                populateAdToViewHolder(holder, apNativeAd)
                            }

                            override fun onAdFailedToLoad(adError: ApAdError?) {
                                super.onAdFailedToLoad(adError)
                                val containerShimmer =
                                    holder.itemView.findViewById<ShimmerFrameLayout>(R.id.shimmerContainerNative)
                                containerShimmer.isGone = true
                            }

                            override fun onAdImpression() {
                                super.onAdImpression()
                                listAdImpression[pos] = true
                            }
                        }
                        listAdImpression[pos] = false
                        AzAds.getInstance().loadNativeList(
                            activity,
                            settings.getListAdUnitId(),
                            settings.getLayoutCustomAd(),
                            adCallback,
                        )
                    }
                }
            }
        }
    }

    private fun populateAdToViewHolder(
        holder: RecyclerView.ViewHolder,
        unifiedNativeAd: ApNativeAd,
    ) {
        if (nativeDefault == null) {
            nativeDefault = unifiedNativeAd
        }
        val adPlaceHolder = holder.itemView.findViewById<FrameLayout>(R.id.flNativeAd)
        val containerShimmer =
            holder.itemView.findViewById<ShimmerFrameLayout>(R.id.shimmerContainerNative)
        val nativeAdView = LayoutInflater.from(activity)
            .inflate(settings.getLayoutCustomAd(), adPlaceHolder, false) as NativeAdView

        containerShimmer.stopShimmer()
        containerShimmer.visibility = View.GONE
        adPlaceHolder.visibility = View.VISIBLE

        if (unifiedNativeAd.admobNativeAd != null) {
            Admob.getInstance()
                .populateUnifiedNativeAdView(unifiedNativeAd.admobNativeAd, nativeAdView)
            adPlaceHolder.removeAllViews()
            adPlaceHolder.addView(nativeAdView)
        } else {
            containerShimmer.visibility = View.GONE
            adPlaceHolder.visibility = View.GONE
        }
    }

    fun isAdPosition(pos: Int): Boolean {
        val nativeAd = listAd[pos]
        return nativeAd != null
    }

    fun getOriginalPosition(posAdAdapter: Int): Int {
        var countAd = 0
        for (i in 0 until posAdAdapter) {
            if (listAd[i] != null) countAd++
        }
        val posOriginal = posAdAdapter - countAd
        Log.d(TAG, "getOriginalPosition: $posOriginal")
        return posOriginal
    }

    fun getAdjustedCount(): Int {
        val countMinAd: Int = if (settings.isRepeatingAd()) {
            adapterOriginal.itemCount / settings.getPositionFixAd()
        } else if (adapterOriginal.itemCount >= settings.getPositionFixAd()) {
            1
        } else {
            0
        }
        return adapterOriginal.itemCount + countMinAd.coerceAtMost(listAd.size)
    }
}
