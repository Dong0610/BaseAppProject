package com.b096.dramarush5.ui.language

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.b096.dramarush5.R
import com.b096.dramarush5.databinding.ItemLanguageOpenBinding
import com.b096.dramarush5.utils.grdEnd
import com.b096.dramarush5.utils.grdStart
import com.b096.dramarush5.utils.mainColor
import com.dong.baselib.utils.transparent
import com.dong.baselib.widget.layout.strokeColor
import com.dong.baselib.widget.layout.strokeGradient
import com.dong.baselib.widget.layout.strokeGradientColors

class LfoAdapter : ListAdapter<LanguageItem, LfoAdapter.LanguageViewHolder>(LanguageItemDiffCallback()) {

    private var onItemSelected: ((LanguageItem) -> Unit)? = null
    private var tutorialMode: Boolean = false

    fun setOnItemSelected(listener: (LanguageItem) -> Unit) {
        onItemSelected = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageOpenBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LanguageViewHolder(binding).apply {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemSelected?.invoke(getItem(position))
                }
            }
        }
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: LanguageViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty() && payloads[0] == PAYLOAD_SELECTION_CHANGE) {
            holder.updateSelectionState(getItem(position).isChoose)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    fun selectItem(item: LanguageItem) {
        val current = currentList.toMutableList().map {
            it.copy(isChoose = it.code == item.code, isDefault = false)
        }
        submitList(current) {}
    }

    fun getSelectedLanguage(): LanguageItem? = currentList.firstOrNull { it.isChoose }

    fun enableTutorialMode() {
        tutorialMode = true
        notifyItemChanged(currentList.indexOfFirst { it.isDefault })
    }
    private var isEnable: Boolean = true

    inner class LanguageViewHolder(
        private val binding: ItemLanguageOpenBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LanguageItem) {
            with(binding) {
                if (!isEnable) {
                    binding.imgItemFlag.locked()
                } else {
                    binding.imgItemFlag.unlock()
                }
                imgItemFlag.setImageResource(item.flagId)
                tvItemName.text = item.name
                updateSelectionState(item.isChoose)
                lavClick.isVisible = tutorialMode && item.isDefault
            }
        }
        val listColorSelect = intArrayOf(grdEnd,grdStart)
        val listColorDis = intArrayOf(transparent, transparent)

        fun updateSelectionState(isSelected: Boolean) {
            with(binding) {

                imgItemSelect.apply {
                    setImageResource(
                        if (isSelected) R.drawable.ic_language_selected
                        else R.drawable.ic_language_unselected
                    )
                }
                llFocusItem.strokeGradientColors(
                   if (isSelected) listColorSelect else listColorDis
                )
            }
        }
    }

    private class LanguageItemDiffCallback : DiffUtil.ItemCallback<LanguageItem>() {
        override fun areItemsTheSame(oldItem: LanguageItem, newItem: LanguageItem): Boolean {
            return oldItem.code == newItem.code
        }

        override fun areContentsTheSame(oldItem: LanguageItem, newItem: LanguageItem): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: LanguageItem, newItem: LanguageItem): Any? {
            return if (oldItem.isChoose != newItem.isChoose) {
                PAYLOAD_SELECTION_CHANGE
            } else {
                null
            }
        }
    }

    fun ImageView.locked() {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(matrix)
        this.colorFilter = filter
        this.imageAlpha = 128
    }

    fun ImageView.unlock() {
        this.colorFilter = null
        this.imageAlpha = 255
    }

    fun setEnable(enable: Boolean) {
        this.isEnable = enable
    }
    companion object {
        private const val PAYLOAD_SELECTION_CHANGE = "payload_selection_change"
    }
}