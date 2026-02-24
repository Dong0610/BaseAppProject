package com.dong.baselib.extensions

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData

// ============================================================================
// region Text Change Listeners
// ============================================================================

/**
 * Listen for text changes with debouncing.
 * Callback is invoked after text stops changing for 150ms.
 *
 * @param callback Callback with the current text
 */
fun TextView.afterTextChanged(callback: (String) -> Unit) = apply {
    val handler = Handler(Looper.getMainLooper())
    var runnable: Runnable? = null

    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            runnable?.let { handler.removeCallbacks(it) }
            runnable = Runnable { callback(s.toString()) }
            handler.postDelayed(runnable!!, 150)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

/**
 * Listen for text changes before they happen.
 *
 * @param callback Callback with the text before change
 */
fun TextView.beforeTextChanged(callback: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            callback(s.toString())
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

/**
 * Listen for text changes in real-time.
 *
 * @param callback Callback with the current text
 */
fun TextView.textChanged(callback: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            callback(s.toString())
        }
    })
}

/**
 * Unified text change listener with configurable behavior.
 *
 * @param mode When to trigger callback: BEFORE, DURING, or AFTER change
 * @param debounceMs Debounce delay for AFTER mode (default: 150ms)
 * @param callback Callback with the text
 */
fun TextView.onTextChange(
    mode: TextChangeMode = TextChangeMode.AFTER,
    debounceMs: Long = 150,
    callback: (String) -> Unit
) {
    when (mode) {
        TextChangeMode.BEFORE -> beforeTextChanged(callback)
        TextChangeMode.DURING -> textChanged(callback)
        TextChangeMode.AFTER -> {
            val handler = Handler(Looper.getMainLooper())
            var runnable: Runnable? = null
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    runnable?.let { handler.removeCallbacks(it) }
                    runnable = Runnable { callback(s.toString()) }
                    handler.postDelayed(runnable!!, debounceMs)
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }
}

enum class TextChangeMode {
    BEFORE, DURING, AFTER
}

// endregion

// ============================================================================
// region Focus Change Listener
// ============================================================================

/**
 * Listen for focus changes with current text.
 *
 * @param callback Callback with (hasFocus, currentText)
 */
fun TextView.onFocusChange(callback: (Boolean, String) -> Unit) = apply {
    onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
        callback(hasFocus, text.toString())
    }
}

// endregion

// ============================================================================
// region Key Listener
// ============================================================================

/**
 * Listen for key down events.
 *
 * @param callback Callback with key code
 */
fun TextView.keyDown(callback: (Int) -> Unit) {
    setOnKeyListener { _, keyCode, event ->
        if (event.action == KeyEvent.ACTION_DOWN) {
            callback(keyCode)
            true
        } else {
            false
        }
    }
}

// endregion

// ============================================================================
// region LiveData Binding
// ============================================================================

/**
 * Bind TextView text to a MutableLiveData.
 * Set this property to automatically update text when LiveData changes.
 */
var TextView.listenText: MutableLiveData<String>?
    get() = throw UnsupportedOperationException("Getter is not supported for listenText")
    set(value) {
        value?.observe(context as LifecycleOwner) { text ->
            this.text = text.toString()
        }
    }

// endregion
