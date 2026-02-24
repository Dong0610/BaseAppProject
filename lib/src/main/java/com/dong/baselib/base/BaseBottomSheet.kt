package com.dong.baselib.base

import android.graphics.Color
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.dong.baselib.R
import com.dong.baselib.api.UnitFun0
import com.dong.baselib.api.emptyLambda
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseBottomSheet<V : ViewBinding>(
    private val activity: FragmentActivity
) : BottomSheetDialog(activity) {

    companion object {
        const val TAG = "BaseBottomSheet"
    }

    private val showingGuard = AtomicBoolean(false)
    private var _binding: V? = null

    protected val binding: V
        get() = _binding ?: throw IllegalStateException("Binding accessed outside of view lifecycle")

    var onDismissListener: UnitFun0 = emptyLambda

    abstract fun initView(inflater: LayoutInflater): V
    abstract fun V.onBind()
    open fun showKeyboard() {
        try {
            val imm = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.root, InputMethodManager.SHOW_IMPLICIT)
        } catch (_: Throwable) {
        }
    }
    open fun hideKeyboard() {
        try {
            val imm = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(activity.window.decorView.rootView.windowToken, 0)
        } catch (_: Throwable) {
        }
    }

    init {
        setOnDismissListener {
            showingGuard.set(false)
            _binding = null
            onDismissListener()
        }
    }

    override fun onStart() {
        super.onStart()

        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        val sheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        sheet?.let { bs ->
            bs.setBackgroundColor(Color.TRANSPARENT)
            bs.layoutParams = bs.layoutParams.apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
            }

            ViewCompat.setOnApplyWindowInsetsListener(bs) { v, insets ->
                v.updatePadding(bottom = 0)
                WindowInsetsCompat.CONSUMED
            }
            ViewCompat.requestApplyInsets(bs)

            BottomSheetBehavior.from(bs).apply {
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun createContentView() {
        _binding = initView(LayoutInflater.from(context))
        binding.onBind()

        binding.root.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        setContentView(binding.root)
    }

    override fun show() {
        if (activity.isFinishing || activity.isDestroyed) return

        if (Looper.myLooper() != Looper.getMainLooper()) {
            activity.runOnUiThread { showOnMainThread() }
        } else {
            showOnMainThread()
        }
    }

    @Synchronized
    private fun showOnMainThread() {
        if (activity.isFinishing || activity.isDestroyed) return
        if (showingGuard.get()) return
        if (isShowing) return

        showingGuard.set(true)
        try {
            createContentView()
            super.show()
        } catch (_: Throwable) {
            showingGuard.set(false)
        }
    }

    fun dismissSafe() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            activity.runOnUiThread { dismissSafe() }
            return
        }
        if (!isShowing) {
            showingGuard.set(false)
            return
        }
        try {
            dismiss()
        } finally {
            showingGuard.set(false)
        }
    }
}