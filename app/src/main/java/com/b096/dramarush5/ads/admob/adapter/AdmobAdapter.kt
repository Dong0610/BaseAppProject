package com.b096.dramarush5.ads.admob.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class AdmobAdapter<T : RecyclerView.ViewHolder>(
    private val activity: Activity,
    private val adapterOriginal: RecyclerView.Adapter<T>,
    private val settings: AdPlacerSettings,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType(val value: Int) {
        TYPE_AD_VIEW(Int.MIN_VALUE),
    }

    private val adPlacer: AdPlacer = AdPlacer(activity, adapterOriginal, settings)
    private val adapterDataObserver: AdapterDataObserver = AdapterDataObserver()

    init {
        adapterOriginal.registerAdapterDataObserver(adapterDataObserver)
        adPlacer.configData()
    }

    override fun getItemCount(): Int {
        return adPlacer.getAdjustedCount()
    }

    override fun getItemViewType(position: Int): Int {
        return if (adPlacer.isAdPosition(position)) {
            ItemType.TYPE_AD_VIEW.value
        } else {
            val originalPos = adPlacer.getOriginalPosition(position)
            adapterOriginal.getItemViewType(originalPos)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == ItemType.TYPE_AD_VIEW.value) {
            val view = LayoutInflater.from(parent.context)
                .inflate(settings.getLayoutAdPlaceHolder(), parent, false)
            ItemViewHolder(view)
        } else {
            adapterOriginal.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (adPlacer.isAdPosition(position)) {
            adPlacer.renderAd(position, holder)
        } else {
            @Suppress("UNCHECKED_CAST")
            adapterOriginal.onBindViewHolder(
                holder as T,
                adPlacer.getOriginalPosition(position)
            )
        }
    }

    fun getOriginalPosition(pos: Int): Int {
        return adPlacer.getOriginalPosition(pos)
    }

    private inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun destroy() {
        try {
            adapterOriginal.unregisterAdapterDataObserver(adapterDataObserver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class AdapterDataObserver : RecyclerView.AdapterDataObserver() {

        @SuppressLint("NotifyDataSetChanged")
        override fun onChanged() {
            adPlacer.configData()
            notifyDataSetChanged()
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            adPlacer.configData()
            notifyDataSetChanged()
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            adPlacer.configData()
            notifyDataSetChanged()
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            adPlacer.configData()
            notifyDataSetChanged()
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            adPlacer.configData()
            notifyDataSetChanged()
        }
    }
}
