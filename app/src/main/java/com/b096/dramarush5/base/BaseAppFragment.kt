package com.b096.dramarush5.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding
import com.b096.dramarush5.dialog.DialogNoInternet
import com.dong.baselib.base.BaseFragment
import com.dong.baselib.extensions.click
import com.dong.baselib.extensions.clickSingle
import com.dong.baselib.network.isNetworkAvailable
import com.dong.baselib.network.observeNetworkConnectivity

abstract class BaseAppFragment<VB : ViewBinding>(
      bindingFactory: (LayoutInflater) -> VB, isFullSc: Boolean = false, backPress: Boolean = true
) : BaseFragment<VB>(bindingFactory, isFullSc, backPress) {
    private var dialogNoInternet: DialogNoInternet? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogNoInternet = DialogNoInternet(appContext()) {}
        observeNetworkConnectivity(onNetworkAvailable = {
            dialogNoInternet?.dismiss()
        }, onNetworkLost = { dialogNoInternet?.show() }) {
            dialogNoInternet?.dismiss()
        }
    }

    fun View.internetClick(action: (View) -> Unit = {}) {
        click {
            if (appContext().isNetworkAvailable()) {
                action.invoke(this@internetClick)
            } else {
                dialogNoInternet?.show()
            }
        }
    }

    fun View.internetSingleClick(action: (View) -> Unit = {}) {
        clickSingle {
            if (appContext().isNetworkAvailable()) {
                action.invoke(this@internetSingleClick)
            } else {
                dialogNoInternet?.show()
            }
        }
    }

    fun runWithInternet(action: () -> Unit = {}) {
        if (appContext().isNetworkAvailable()) {
            action()
        } else {
            dialogNoInternet?.show()
        }
    }
}