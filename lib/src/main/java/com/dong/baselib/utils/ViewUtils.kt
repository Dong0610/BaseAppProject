package com.dong.baselib.utils

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.View

// ============================================================================
// region Location & Visibility Utilities
// ============================================================================

/**
 * Get view location on screen.
 *
 * @return IntArray with [x, y] coordinates
 */
fun View.locationOnScreen(): IntArray {
    val location = IntArray(2)
    getLocationOnScreen(location)
    return location
}

/**
 * Check if view is visible on screen.
 *
 * @return true if view is shown and has visible area
 */
fun View.isVisibleOnScreen(): Boolean {
    if (!isShown) return false
    val rect = Rect()
    return getGlobalVisibleRect(rect)
}

// endregion

// ============================================================================
// region Safe Post Delayed
// ============================================================================

/**
 * Post delayed with auto-cancel on detach.
 * Prevents memory leaks by removing callback when view is detached.
 *
 * @param delayMillis Delay in milliseconds
 * @param action Action to run
 */
fun View.postDelayedSafe(delayMillis: Long, action: () -> Unit) {
    val runnable = Runnable { action() }
    postDelayed(runnable, delayMillis)
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {}
        override fun onViewDetachedFromWindow(v: View) {
            removeCallbacks(runnable)
            removeOnAttachStateChangeListener(this)
        }
    })
}

// endregion

// ============================================================================
// region Time Formatting Utilities
// ============================================================================

/**
 * Convert milliseconds to HH:MM:SS or MM:SS format.
 *
 * @param millis Time in milliseconds
 * @return Formatted time string
 */
@SuppressLint("DefaultLocale")
fun convertMillieToHhMmSs(millis: Long): String {
    val seconds = millis / 1000
    val second = seconds % 60
    val minute = seconds / 60 % 60
    val hour = seconds / (60 * 60) % 24
    return if (hour > 0) {
        String.format("%02d:%02d:%02d", hour, minute, second)
    } else {
        String.format("%02d:%02d", minute, second)
    }
}

// endregion

// ============================================================================
// region List Utilities
// ============================================================================

/**
 * Move an item matching predicate to specified position.
 *
 * @param position Target position (1-indexed)
 * @param predicate Condition to find the item
 * @return New list with item moved
 */
inline fun <T> List<T>.moveItemToPosition(position: Int, predicate: (T) -> Boolean): List<T> {
    for (element in this.withIndex()) {
        if (predicate(element.value)) {
            return this.toMutableList().apply {
                removeAt(element.index)
                add(position - 1, element.value)
            }.toList()
        }
    }
    return this
}

// endregion
