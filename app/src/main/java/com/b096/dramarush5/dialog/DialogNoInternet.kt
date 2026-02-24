package com.b096.dramarush5.dialog

import android.content.Context
import com.b096.dramarush5.databinding.DialogNoInternetBinding
import com.dong.baselib.base.BaseDialog
import com.dong.baselib.extensions.click

class DialogNoInternet(context: Context, val onTryAgain: () -> Unit = {}) :
    BaseDialog<DialogNoInternetBinding>(context, DialogNoInternetBinding::inflate) {
    override fun DialogNoInternetBinding.initView() {
        btnExit.click {
            dismiss()
            onTryAgain()
        }
    }
}