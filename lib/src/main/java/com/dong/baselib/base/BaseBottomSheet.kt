package com.dong.baselib.base

import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseBottomSheet<V : ViewBinding>(
    private val activity: FragmentActivity,
    private val bindingFactory: (LayoutInflater) -> V,
) : BottomSheetDialog(activity) {

    companion object { const val TAG = "BaseBottomSheet" }
    private val showingGuard = AtomicBoolean(false)
    private var _binding: V? = null

    protected val binding: V
        get() = _binding ?: throw IllegalStateException("Binding accessed before show()/after dismiss()")

    var onDismissListener: () -> Unit = {}

    abstract fun V.onBind()

    init {
        setOnShowListener { trySetupBottomSheet() }

        setOnDismissListener {
            showingGuard.set(false)
            _binding = null
            onDismissListener()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }
    private fun ensureContentView() {
        if (_binding != null) return
        val vb = bindingFactory(LayoutInflater.from(context))
        _binding = vb
        vb.onBind()
        vb.root.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        vb.root.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom != oldBottom) {
                val sheet = findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                ) ?: return@addOnLayoutChangeListener
                BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        setContentView(vb.root)
    }
    @Suppress("DEPRECATION")
    private fun trySetupBottomSheet() {
        window?.let { win ->
            win.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            win.setBackgroundDrawableResource(android.R.color.transparent)
            win.navigationBarColor = Color.TRANSPARENT

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                win.insetsController?.hide(
                    android.view.WindowInsets.Type.navigationBars()
                )
                win.insetsController?.systemBarsBehavior =
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                win.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        )
            }
            win.decorView.setPadding(0, 0, 0, 0)
        }

        val sheet = findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return

        sheet.setBackgroundColor(Color.TRANSPARENT)
        sheet.fitsSystemWindows = false
        sheet.setPadding(0, 0, 0, 0)
        sheet.layoutParams = sheet.layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }

        var parent = sheet.parent
        while (parent is View) {
            (parent as View).apply {
                fitsSystemWindows = false
                setPadding(0, 0, 0, 0)
            }
            ViewCompat.setOnApplyWindowInsetsListener(parent as View) { _, _ ->
                WindowInsetsCompat.CONSUMED
            }
            parent = (parent as View).parent
        }

        ViewCompat.setOnApplyWindowInsetsListener(sheet) { _, _ ->
            WindowInsetsCompat.CONSUMED
        }

        BottomSheetBehavior.from(sheet).apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
            (sheet.parent as? ViewGroup)?.layoutTransition = null
        }
    }

    override fun show() {
        if (activity.isFinishing || activity.isDestroyed) return

        if (Looper.myLooper() != Looper.getMainLooper()) {
            activity.runOnUiThread { show() }
            return
        }

        if (!showingGuard.compareAndSet(false, true)) return
        if (isShowing) return

        try {
            ensureContentView()
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
