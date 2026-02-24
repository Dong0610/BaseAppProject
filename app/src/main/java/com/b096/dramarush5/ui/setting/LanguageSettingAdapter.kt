package com.b096.dramarush5.ui.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.b096.dramarush5.R
import com.b096.dramarush5.databinding.ItemLanguageSettingBinding
import com.b096.dramarush5.ui.language.LanguageItem
import com.b096.dramarush5.utils.grdEnd
import com.b096.dramarush5.utils.grdStart
import com.b096.dramarush5.utils.mainColor
import com.dong.baselib.base.BaseAdapter
import com.dong.baselib.extensions.click
import com.dong.baselib.utils.transparent
import com.dong.baselib.widget.layout.strokeColor
import com.dong.baselib.widget.layout.strokeGradientColors

class LanguageSettingAdapter(var lang: (LanguageItem) -> Unit = { _ -> }) :
    BaseAdapter<LanguageItem, ItemLanguageSettingBinding>() {
    override fun createBinding(
          inflater: LayoutInflater,
          parent: ViewGroup,
          viewType: Int
    ) = ItemLanguageSettingBinding.inflate(inflater, parent, false)

    val listColorSelect = intArrayOf(grdEnd, grdStart)
    val listColorDis = intArrayOf(transparent, transparent)

    override fun ItemLanguageSettingBinding.bind(item: LanguageItem, position: Int) {
        currentPosition.observe(this@LanguageSettingAdapter) { pos ->
            if (pos == position) {
                llFocusItem.strokeGradientColors(listColorSelect)
                imgItemSelect.setImageResource(R.drawable.ic_language_selected)
            } else {
                llFocusItem.strokeGradientColors(listColorDis)
                imgItemSelect.setImageResource(R.drawable.ic_language_unselected)
            }
        }

        imgItemFlag.setImageResource(item.flagId)
        tvItemName.text = item.name

        root.click {
            currentPosition.value = position
            lang.invoke(item)
        }
    }
}