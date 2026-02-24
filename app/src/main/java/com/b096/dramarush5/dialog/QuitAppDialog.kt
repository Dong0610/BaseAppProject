package com.b096.dramarush5.dialog

import android.content.Context
import com.dong.baselib.base.BaseDialog
import com.b096.dramarush5.databinding.DialogQuitAppBinding
import com.dong.baselib.extensions.click

class QuitAppDialog(context: Context, var action: () -> Unit = {}) :
    BaseDialog<DialogQuitAppBinding>(context, DialogQuitAppBinding::inflate, true) {
    override fun DialogQuitAppBinding.initView() {
        btnExit.click {
            action.invoke()
            dismiss()
        }
        btnCancel.click {
            dismiss()
        }
    }
}