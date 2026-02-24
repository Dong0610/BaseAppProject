package com.b096.dramarush5.ui.feature

import android.view.LayoutInflater
import android.view.ViewGroup
import com.b096.dramarush5.databinding.ItemFeatureViewBinding
import com.dong.baselib.base.DiffAdapter
import com.dong.baselib.utils.opacity
import com.dong.baselib.utils.transparent
import com.dong.baselib.utils.white
import com.dong.baselib.widget.layout.isBackgroundGradient
import com.dong.baselib.widget.layout.strokeGradientColors

class FeatureAdapter(
      private val onSelectedItem: (FeatureModel) -> Unit,
) : DiffAdapter<FeatureModel, ItemFeatureViewBinding>(
    areItemsTheSame = { old, new -> old.index == new.index },
    areContentsTheSame = { old, new -> old.index == new.index && old.isSelected == new.isSelected }
) {
    override fun createBinding(
          inflater: LayoutInflater,
          parent: ViewGroup,
          viewType: Int
    ) = ItemFeatureViewBinding.inflate(inflater, parent, false)

    override fun ItemFeatureViewBinding.bind(item: FeatureModel, position: Int) {
        tvItemName.text = root.context.getString(item.label)
        llFocusItem.apply {
            isBackgroundGradient(item.isSelected)
            strokeGradientColors(
                if (item.isSelected)
                    intArrayOf(transparent, transparent)
                else intArrayOf(
                    white.opacity(20),
                    white.opacity(20)
                )
            )
        }
        root.setOnClickListener { onSelectedItem(item) }
    }
}