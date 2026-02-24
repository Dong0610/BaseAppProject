package com.b096.dramarush5.dialog

import android.content.Context
import androidx.core.graphics.toColorInt
import com.b096.dramarush5.R
import com.b096.dramarush5.databinding.DialogNotiFullBinding
import com.dong.baselib.base.BaseDialog
import com.dong.baselib.extensions.click
import com.dong.baselib.utils.textStyle

class DialogNotiFullScreen(context: Context, val onAllow: () -> Unit = {}) :
    BaseDialog<DialogNotiFullBinding>(
        context,
        DialogNotiFullBinding::inflate,
        cancelAble = false
    ) {
    override fun DialogNotiFullBinding.initView() {
        allowContent.text = textStyle {
            lineHeightPx(18)
            normal(text = context.getString(R.string.allow), textColor = "#80828D".toColorInt())
            bold(
                text = " ${context.getString(R.string.app_name)} ",
                textColor = "#FFFFFF".toColorInt()
            )
            normal(
                text = context.getString(R.string.to_send_you_notifications),
                textColor = "#80828D".toColorInt()
            )
        }
        btnAllow.click {
            dismiss()
            onAllow()
        }
        icClose.click {
            dismiss()
        }
    }
}