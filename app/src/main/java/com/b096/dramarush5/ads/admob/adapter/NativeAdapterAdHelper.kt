package com.b096.dramarush5.ads.admob.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isGone
import com.ads.control.admob.Admob
import com.ads.control.ads.AzAdCallback
import com.ads.control.ads.AzAds
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApNativeAd
import com.ads.control.ads.wrapper.StatusAd
import com.b096.dramarush5.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Helper load native cho từng item NativeAdChat trong MessageAdapter
 */
class NativeAdapterAdHelper(
      private val activity: Activity,
      private val listAdUnitId: List<String>, // NativePlacement.NATIVE_DETAIL_SMALL_CTA_RIGHT.listId()
      private val layoutCustomAd: Int         // NativePlacement.NATIVE_DETAIL_SMALL_CTA_RIGHT.preloadLayoutId()
) {

   // map theo id của ChatUiItem (id mình tạo cho NativeAdChat)
   private val listAd = HashMap<Long, ApNativeAd?>()
   private val listAdImpression = HashMap<Long, Boolean>()
   private var nativeDefault: ApNativeAd? = null
   private val TAG = "zzzzzzz"

   // Store pending callbacks for items that are still loading

   fun bindNative(
         holder: ViewGroup,
         itemId: Long,
         onAdImpression: () -> Unit = {},
         onAdFailed: () -> Unit = {}
   ) {
      val current = listAd[itemId]

      // chưa có ad hoặc chưa load
      if (current?.admobNativeAd == null) {
         if (current?.status != StatusAd.AD_LOADING) {
            holder.post {
               val containerShimmer =
                  holder.findViewById<ShimmerFrameLayout>(R.id.shimmerContainerNative)
               if (current?.status == StatusAd.AD_INIT && nativeDefault != null) {
                  listAd[itemId] = nativeDefault
                  containerShimmer.isGone = true
                  populateAdToViewHolder(holder, nativeDefault!!)
                  return@post
               }

               var nativeAdApp = ApNativeAd(StatusAd.AD_LOADING)
               listAd[itemId] = nativeAdApp
               val callback = object : AzAdCallback() {
                  override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                     super.onNativeAdLoaded(nativeAd)
                     nativeAdApp = nativeAd
                     nativeAdApp.status = StatusAd.AD_LOADED
                     listAd[itemId] = nativeAdApp
                     populateAdToViewHolder(holder, nativeAdApp)
                  }

                  override fun onAdFailedToLoad(adError: ApAdError?) {
                     super.onAdFailedToLoad(adError)
                     try {
                        activity.runOnUiThread {
                           containerShimmer.isGone = true
                        }
                     }
                     catch (e: Exception) {
                        e.printStackTrace()
                     }
                     onAdFailed()
                  }

                  override fun onAdImpression() {
                     super.onAdImpression()
                     listAdImpression[itemId] = true
                     onAdImpression()
                  }
               }
               listAdImpression[itemId] = false
               AzAds.getInstance().loadNativeList(
                  activity,
                  listAdUnitId,
                  layoutCustomAd,
                  callback,
               )

            }
         }
      } else {
         // đã có ad (AD_LOADED) trong cache
         if (current.status == StatusAd.AD_LOADED) {
            holder.post {
               if (listAdImpression[itemId] == false) {
                  populateAdToViewHolder(holder, current)
               } else {
                  var nativeAd = ApNativeAd(StatusAd.AD_LOADING)
                  listAd[itemId] = nativeAd

                  val callback = object : AzAdCallback() {
                     override fun onNativeAdLoaded(apNativeAd: ApNativeAd) {
                        super.onNativeAdLoaded(apNativeAd)
                        nativeAd = apNativeAd
                        nativeAd.status = StatusAd.AD_LOADED
                        listAd[itemId] = nativeAd
                        populateAdToViewHolder(holder, apNativeAd)
                     }

                     override fun onAdFailedToShow(adError: ApAdError?) {
                        super.onAdFailedToShow(adError)
                        val containerShimmer =
                           holder.findViewById<ShimmerFrameLayout>(R.id.shimmerContainerNative)
                        containerShimmer.isGone = true
                        onAdFailed()
                     }

                     override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        try {
                           activity.runOnUiThread {
                              val containerShimmer =
                                 holder.findViewById<ShimmerFrameLayout>(R.id.shimmerContainerNative)
                              containerShimmer.isGone = true
                           }
                        }
                        catch (e: Exception) {
                           e.printStackTrace()
                        }
                        onAdFailed()
                     }

                     override fun onAdImpression() {
                        super.onAdImpression()
                        listAdImpression[itemId] = true
                        onAdImpression()
                     }
                  }
                  listAdImpression[itemId] = false
                  AzAds.getInstance().loadNativeList(
                     activity,
                     listAdUnitId,
                     layoutCustomAd,
                     callback,
                  )
               }
            }
         }
      }
   }

   private fun populateAdToViewHolder(
         holder: ViewGroup,
         unifiedNativeAd: ApNativeAd
   ) {
      try {
         activity.runOnUiThread {
            if (nativeDefault == null) {
               nativeDefault = unifiedNativeAd
            }

            val adPlaceHolder = holder.findViewById<FrameLayout>(R.id.flNativeAd)
            val containerShimmer =
               holder.findViewById<ShimmerFrameLayout?>(R.id.shimmerContainerNative)

            val nativeAdView = LayoutInflater.from(activity)
               .inflate(layoutCustomAd, adPlaceHolder, false) as NativeAdView

            containerShimmer?.stopShimmer()
            containerShimmer?.visibility = View.GONE
            adPlaceHolder.visibility = View.VISIBLE

            if (unifiedNativeAd.admobNativeAd != null) {
               Admob.getInstance()
                  .populateUnifiedNativeAdView(unifiedNativeAd.admobNativeAd, nativeAdView)
               adPlaceHolder.removeAllViews()
               adPlaceHolder.addView(nativeAdView)
            } else {
               containerShimmer?.visibility = View.GONE
               adPlaceHolder.visibility = View.GONE
            }
         }
      }
      catch (e: Exception) {
         e.printStackTrace()
      }
   }
}
