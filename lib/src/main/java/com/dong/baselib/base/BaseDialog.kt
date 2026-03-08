package com.dong.baselib.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.viewbinding.ViewBinding
import com.dong.baselib.R
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope

abstract class BaseDialog<V : ViewBinding>(
      private val context: Context,
      val bindingFactory: (LayoutInflater) -> V,
      var cancelAble: Boolean = true,
      val isFull: Boolean = false
) :
    Dialog(context, if (!isFull) R.style.BaseDialog else R.style.BaseDialogFull), LifecycleOwner {
    private val TAG: String = BaseDialog::class.java.name
    val binding: V by lazy { bindingFactory(layoutInflater) }
    private val registry = LifecycleRegistry(this)
    private var isDestroyed = false
    override val lifecycle: Lifecycle get() = registry

    val dialogScope: CoroutineScope get() = lifecycleScope

    fun stringRes(@StringRes res: Int): String = context.getString(res)

    protected abstract fun V.initView()

    init {
        require(context is Activity) { "BaseDialog requires an Activity context" }
        initialize()
    }

    private fun initialize() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setGravity(Gravity.CENTER)
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    open fun hideKeyboard() {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window?.decorView?.rootView?.windowToken, 0)
    }

    open fun showKeyboard() {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.root, InputMethodManager.SHOW_IMPLICIT)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        registry.currentState = Lifecycle.State.CREATED
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.initView()
        setCancelable(cancelAble)
        this.setCanceledOnTouchOutside(cancelAble)
    }

    override fun show() {
        // Check if activity is valid before showing
        val activity = context as? Activity
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            return
        }

        try {
            if (isShowing) {
                dismiss()
            }
            // Reset destroyed flag for reuse
            isDestroyed = false
            super.show()
            registry.currentState = Lifecycle.State.STARTED
            registry.currentState = Lifecycle.State.RESUMED
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun dismiss() {
        if (isDestroyed) return
        try {
            isDestroyed = true
            if (registry.currentState != Lifecycle.State.DESTROYED) {
                registry.currentState = Lifecycle.State.DESTROYED
            }
            super.dismiss()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isDestroyed && registry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            registry.currentState = Lifecycle.State.CREATED
        }
    }
}