package com.b096.dramarush5.base

import android.app.AppOpsManager
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding
import com.b096.dramarush5.dialog.DialogNoInternet
import com.dong.baselib.base.BaseActivity
import com.dong.baselib.extensions.click
import com.dong.baselib.extensions.clickSingle
import com.dong.baselib.network.isNetworkAvailable
import com.dong.baselib.network.observeNetworkConnectivity

abstract class BaseAppActivity<VB : ViewBinding>(
      bindingFactory: (LayoutInflater) -> VB,
      fullStatus: Boolean = false,
) : BaseActivity<VB>(bindingFactory, fullStatus) {

    private val dialogNoInternet by lazy {
        DialogNoInternet(this@BaseAppActivity) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeNetworkConnectivity(
            onNetworkAvailable = {
                dialogNoInternet.dismiss()
            },
            onNetworkLost = { dialogNoInternet.show() }) {
            dialogNoInternet.dismiss()
        }
    }
    fun View.internetClick(action: (View) -> Unit = {}) {
        click {
            if (isNetworkAvailable()) {
                action(this@internetClick)
            } else {
                dialogNoInternet.show()
            }
        }
    }

    fun View.internetSingleClick(action: (View) -> Unit = {}) {
        clickSingle {
            if (isNetworkAvailable()) {
                action(this@internetSingleClick)
            } else {
                dialogNoInternet.show()
            }
        }
    }

    fun runWithInternet(action: () -> Unit = {}) {
        if(isNetworkAvailable()){
            action()
        } else {
            dialogNoInternet.show()
        }
    }
}