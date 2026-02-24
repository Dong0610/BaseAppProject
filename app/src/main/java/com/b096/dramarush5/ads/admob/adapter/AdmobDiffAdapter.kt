package com.b096.dramarush5.ads.admob.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
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

sealed class AdmobListItem<out T> {
    data class Content<T>(val data: T) : AdmobListItem<T>()
    data class Ad(val position: Int) : AdmobListItem<Nothing>()
}

/**
 * Configuration for ad placement in the adapter
 */
data class AdmobAdConfig(
    val adInterval: Int = 2,           // Show ad after every N items
    val firstAdPosition: Int = 0,      // Position of first ad (0 = after first item)
    val gridSpanCount: Int = 2,        // Grid span count
    val adSpanSize: Int = 2,           // Span size for ad items (full width = gridSpanCount)
    @LayoutRes val layoutAdPlaceHolder: Int = 0,
    val layoutCustomAd: Int = R.layout.layout_native_small_cta_bot,
    val listAdUnitId: List<String> = emptyList(),
    val isLoop: Boolean = true         // If false, only add ads in first iteration/sequence
)

abstract class AdmobDiffAdapter<T : Any, VB : ViewBinding>(
    private val activity: Activity,
    private val adConfig: AdmobAdConfig = AdmobAdConfig(),
    private val areItemsTheSame: (old: T, new: T) -> Boolean,
    private val areContentsTheSame: (old: T, new: T) -> Boolean = { o, n -> o == n },
) : ListAdapter<AdmobListItem<T>, RecyclerView.ViewHolder>(
    AdmobDiffCallback(areItemsTheSame, areContentsTheSame)
) {

    companion object {
        private const val VIEW_TYPE_AD = Int.MIN_VALUE
    }

    private val listAd = HashMap<Int, ApNativeAd?>()
    private val listAdImpression = HashMap<Int, Boolean>()
    private var nativeDefault: ApNativeAd? = null

    abstract fun createBinding(inflater: LayoutInflater, parent: ViewGroup): VB
    abstract fun VB.bind(item: T, position: Int)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AdmobListItem.Ad -> VIEW_TYPE_AD
            is AdmobListItem.Content -> 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_AD) {
            val view = inflater.inflate(adConfig.layoutAdPlaceHolder, parent, false)
            AdViewHolder(view)
        } else {
            val binding = createBinding(inflater, parent)
            ContentViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is AdmobListItem.Ad -> renderAd(item.position, holder)
            is AdmobListItem.Content -> {
                @Suppress("UNCHECKED_CAST")
                val contentHolder = holder as AdmobDiffAdapter<T, VB>.ContentViewHolder
                contentHolder.binding.bind(item.data, position)
            }
        }
    }

    /**
     * Submit content list - ads will be automatically inserted based on adConfig
     * If isLoop is false, ads will only be added in the first iteration/sequence
     */
    fun submitContentList(items: List<T>) {
        if (AppPurchase.getInstance().isPurchased(activity)) {
            submitList(items.map { AdmobListItem.Content(it) })
            return
        }

        val mixedList = mutableListOf<AdmobListItem<T>>()
        var adCount = 0
        var contentIndex = 0
        var adIterationCount = 0  // Track which iteration we're in

        items.forEachIndexed { index, item ->
            val adjustedPos = index + adCount

            // Check if we should insert an ad at this position
            if (shouldInsertAdAt(contentIndex, adIterationCount)) {
                val adPosition = adjustedPos
                if (listAd[adPosition] == null) {
                    listAd[adPosition] = ApNativeAd(StatusAd.AD_INIT)
                    listAdImpression[adPosition] = true
                }
                mixedList.add(AdmobListItem.Ad(adPosition))
                adCount++
                adIterationCount++
            }

            mixedList.add(AdmobListItem.Content(item))
            contentIndex++
        }

        submitList(mixedList)
    }

    /**
     * Determines if an ad should be inserted at the given content index
     * Ad is inserted when contentIndex % adInterval == firstAdPosition
     * If isLoop is false, only ads in the first iteration are added
     */
    private fun shouldInsertAdAt(contentIndex: Int, adIterationCount: Int = 0): Boolean {
        if (adConfig.adInterval <= 0) return false
        if (contentIndex < adConfig.firstAdPosition) return false

        // If isLoop is false, only allow one ad per sequence (adIterationCount should be 0)
        if (!adConfig.isLoop && adIterationCount > 0) return false

        val adjustedIndex = contentIndex - adConfig.firstAdPosition
        return adjustedIndex % adConfig.adInterval == 0
    }

    /**
     * Creates a SpanSizeLookup for GridLayoutManager that makes ads span full width
     */
    fun createSpanSizeLookup(layoutManager: GridLayoutManager): GridLayoutManager.SpanSizeLookup {
        return object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position < itemCount && getItem(position) is AdmobListItem.Ad) {
                    adConfig.adSpanSize
                } else {
                    1
                }
            }
        }
    }

    /**
     * Attaches this adapter to a RecyclerView with GridLayoutManager configured
     */
    fun attachToRecyclerView(recyclerView: RecyclerView) {
        val layoutManager = GridLayoutManager(recyclerView.context, adConfig.gridSpanCount)
        layoutManager.spanSizeLookup = createSpanSizeLookup(layoutManager)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = this
    }

    private fun renderAd(adPosition: Int, holder: RecyclerView.ViewHolder) {
        val adNative = listAd[adPosition]
        val shimmerContainer = holder.itemView.findViewById<ShimmerFrameLayout>(R.id.shimmerContainerNative)
            ?: holder.itemView.findViewById<ShimmerFrameLayout>(R.id.shimmerNativeAd)?.findViewById(R.id.shimmerContainerNative)

        if (adNative?.admobNativeAd == null) {
            if (listAd[adPosition]?.status != StatusAd.AD_LOADING) {
                holder.itemView.post {
                    if (listAd[adPosition]?.status == StatusAd.AD_INIT && nativeDefault != null) {
                        listAd[adPosition] = nativeDefault
                        shimmerContainer?.gone()
                        populateAdToViewHolder(holder, nativeDefault!!)
                        return@post
                    }

                    var nativeAd = ApNativeAd(StatusAd.AD_LOADING)
                    listAd[adPosition] = nativeAd
                    val adCallback: AzAdCallback = object : AzAdCallback() {
                        override fun onNativeAdLoaded(apNativeAd: ApNativeAd) {
                            super.onNativeAdLoaded(apNativeAd)
                            nativeAd = apNativeAd
                            nativeAd.status = StatusAd.AD_LOADED
                            listAd[adPosition] = nativeAd
                            populateAdToViewHolder(holder, apNativeAd)
                        }

                        override fun onAdFailedToLoad(adError: ApAdError?) {
                            super.onAdFailedToLoad(adError)
                            shimmerContainer?.isGone = true
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                            listAdImpression[adPosition] = true
                        }
                    }
                    listAdImpression[adPosition] = false
                    if (adConfig.listAdUnitId.isNotEmpty()) {
                        AzAds.getInstance().loadNativeList(
                            activity,
                            adConfig.listAdUnitId,
                            adConfig.layoutCustomAd,
                            adCallback
                        )
                    }
                }
            }
        } else {
            if (adNative.status == StatusAd.AD_LOADED) {
                holder.itemView.post {
                    if (listAdImpression[adPosition] == false) {
                        populateAdToViewHolder(holder, adNative)
                    } else {
                        var nativeAd = ApNativeAd(StatusAd.AD_LOADING)
                        listAd[adPosition] = nativeAd
                        val adCallback: AzAdCallback = object : AzAdCallback() {
                            override fun onNativeAdLoaded(apNativeAd: ApNativeAd) {
                                super.onNativeAdLoaded(apNativeAd)
                                nativeAd = apNativeAd
                                nativeAd.status = StatusAd.AD_LOADED
                                listAd[adPosition] = nativeAd
                                populateAdToViewHolder(holder, apNativeAd)
                            }

                            override fun onAdFailedToLoad(adError: ApAdError?) {
                                super.onAdFailedToLoad(adError)
                                shimmerContainer?.isGone = true
                            }

                            override fun onAdImpression() {
                                super.onAdImpression()
                                listAdImpression[adPosition] = true
                            }
                        }
                        listAdImpression[adPosition] = false
                        if (adConfig.listAdUnitId.isNotEmpty()) {
                            AzAds.getInstance().loadNativeList(
                                activity,
                                adConfig.listAdUnitId,
                                adConfig.layoutCustomAd,
                                adCallback,
                            )
                        }
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
        val shimmerContainer = holder.itemView.findViewById<ShimmerFrameLayout>(R.id.shimmerContainerNative)
            ?: holder.itemView.findViewById<View>(R.id.shimmerNativeAd)?.findViewById(R.id.shimmerContainerNative)

        if (adPlaceHolder == null) return

        val nativeAdView = LayoutInflater.from(activity)
            .inflate(adConfig.layoutCustomAd, adPlaceHolder, false) as NativeAdView

        (shimmerContainer as? ShimmerFrameLayout)?.stopShimmer()
        shimmerContainer?.visibility = View.GONE
        adPlaceHolder.visibility = View.VISIBLE

        if (unifiedNativeAd.admobNativeAd != null) {
            Admob.getInstance()
                .populateUnifiedNativeAdView(unifiedNativeAd.admobNativeAd, nativeAdView)
            adPlaceHolder.removeAllViews()
            adPlaceHolder.addView(nativeAdView)
        } else {
            shimmerContainer?.visibility = View.GONE
            adPlaceHolder.visibility = View.GONE
        }
    }

    /**
     * Get original position (without ads) from adapter position
     */
    fun getOriginalPosition(adapterPosition: Int): Int {
        var adCount = 0
        for (i in 0 until adapterPosition) {
            if (i < itemCount && getItem(i) is AdmobListItem.Ad) adCount++
        }
        return adapterPosition - adCount
    }

    /**
     * Get content item at adapter position (returns null for ad positions)
     */
    fun getContentItem(adapterPosition: Int): T? {
        return if (adapterPosition < itemCount) {
            when (val item = getItem(adapterPosition)) {
                is AdmobListItem.Content -> item.data
                is AdmobListItem.Ad -> null
            }
        } else null
    }

    /**
     * Check if position is an ad
     */
    fun isAdPosition(position: Int): Boolean {
        return position < itemCount && getItem(position) is AdmobListItem.Ad
    }

    inner class ContentViewHolder(val binding: VB) : RecyclerView.ViewHolder(binding.root)

    inner class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

private class AdmobDiffCallback<T : Any>(
    private val areItemsTheSame: (old: T, new: T) -> Boolean,
    private val areContentsTheSame: (old: T, new: T) -> Boolean,
) : DiffUtil.ItemCallback<AdmobListItem<T>>() {

    override fun areItemsTheSame(
        oldItem: AdmobListItem<T>,
        newItem: AdmobListItem<T>
    ): Boolean {
        return when {
            oldItem is AdmobListItem.Ad && newItem is AdmobListItem.Ad ->
                oldItem.position == newItem.position
            oldItem is AdmobListItem.Content && newItem is AdmobListItem.Content ->
                areItemsTheSame(oldItem.data, newItem.data)
            else -> false
        }
    }

    override fun areContentsTheSame(
        oldItem: AdmobListItem<T>,
        newItem: AdmobListItem<T>
    ): Boolean {
        return when {
            oldItem is AdmobListItem.Ad && newItem is AdmobListItem.Ad ->
                oldItem.position == newItem.position
            oldItem is AdmobListItem.Content && newItem is AdmobListItem.Content ->
                areContentsTheSame(oldItem.data, newItem.data)
            else -> false
        }
    }
}
