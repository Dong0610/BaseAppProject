package com.b096.dramarush5.dialog

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.b096.dramarush5.R
import com.b096.dramarush5.ads.wrapper.native.NativePlacement
import com.b096.dramarush5.ads.wrapper.native.createNativeConfig
import com.b096.dramarush5.databinding.DialogRenameBinding
import com.dong.baselib.api.UnitFun1
import com.dong.baselib.api.emptyLambda
import com.dong.baselib.base.BaseDialog
import com.dong.baselib.extensions.click
import com.dong.baselib.extensions.gone

class DialogRename(
    private val activity: Activity,
    private val lastName: String = "",
    private val onSaveItem: UnitFun1<String> = {}
) :
    BaseDialog<DialogRenameBinding>(activity, DialogRenameBinding::inflate) {
    override fun DialogRenameBinding.initView() {
        edtName.setText(lastName)
        icClose.click {
            dismiss()
        }
        edtName.onDrawableNotEmptyClick {
            edtName.clearText()
        }
        btnSave.click {
            val text = edtName.text.toString()
            if (text.isBlank()) {
                Toast.makeText(context, stringRes(R.string.please_enter_name), Toast.LENGTH_SHORT)
                    .show()
            } else {
                onSaveItem(text)
                dismiss()
            }
        }
        (activity as? AppCompatActivity)?.createNativeConfig(
            NativePlacement.POPUP,
            flNativeAd,
            { binding.shimmerMediaLeft.shimmerContainerNative })
            ?.setupNativeAd("native_dialog_rename")
            ?.requestAds()
            ?: run {
                binding.flNativeAd.gone()
            }
    }
}