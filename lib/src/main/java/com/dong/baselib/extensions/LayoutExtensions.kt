package com.dong.baselib.extensions

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver

// ============================================================================
// region Padding Extensions
// ============================================================================

/**
 * Set equal padding on all sides.
 *
 * @param all Padding value in pixels
 */
fun View.padding(all: Int) = setPadding(all, all, all, all)

/**
 * Set horizontal and vertical padding.
 *
 * @param horizontal Left and right padding in pixels
 * @param vertical Top and bottom padding in pixels
 */
fun View.padding(horizontal: Int = paddingLeft, vertical: Int = paddingTop) =
    setPadding(horizontal, vertical, horizontal, vertical)

/**
 * Set individual padding for each side.
 */
fun View.padding(
    left: Int = paddingLeft,
    top: Int = paddingTop,
    right: Int = paddingRight,
    bottom: Int = paddingBottom
) = setPadding(left, top, right, bottom)

fun View.paddingTop(value: Int) = setPadding(paddingLeft, value, paddingRight, paddingBottom)
fun View.paddingBottom(value: Int) = setPadding(paddingLeft, paddingTop, paddingRight, value)
fun View.paddingLeft(value: Int) = setPadding(value, paddingTop, paddingRight, paddingBottom)
fun View.paddingRight(value: Int) = setPadding(paddingLeft, paddingTop, value, paddingBottom)
fun View.paddingHorizontal(value: Int) = setPadding(value, paddingTop, value, paddingBottom)
fun View.paddingVertical(value: Int) = setPadding(paddingLeft, value, paddingRight, value)

// endregion

// ============================================================================
// region Margin Extensions
// ============================================================================

/**
 * Set equal margin on all sides.
 *
 * @param all Margin value in pixels
 */
fun View.margin(all: Int) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(all, all, all, all)
}

/**
 * Set horizontal and vertical margin.
 *
 * @param horizontal Left and right margin in pixels
 * @param vertical Top and bottom margin in pixels
 */
fun View.margin(horizontal: Int = 0, vertical: Int = 0) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(horizontal, vertical, horizontal, vertical)
}

/**
 * Set individual margin for each side.
 */
fun View.margin(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(left, top, right, bottom)
}

fun View.marginTop(value: Int) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin = value
}

fun View.marginBottom(value: Int) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin = value
}

fun View.marginLeft(value: Int) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.leftMargin = value
}

fun View.marginRight(value: Int) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.rightMargin = value
}

// endregion

// ============================================================================
// region Size Extensions
// ============================================================================

/**
 * Set view size.
 *
 * @param width Width in pixels
 * @param height Height in pixels
 */
fun View.size(width: Int, height: Int) {
    layoutParams = layoutParams?.apply {
        this.width = width
        this.height = height
    }
}

// endregion

// ============================================================================
// region Layout Callbacks
// ============================================================================

/**
 * Execute action after view is laid out.
 * If already laid out, executes immediately.
 *
 * @param action Action to execute with this view
 */
inline fun View.doOnLayout(crossinline action: (View) -> Unit) {
    if (isLaidOut && !isLayoutRequested) {
        action(this)
    } else {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                action(this@doOnLayout)
            }
        })
    }
}

/**
 * Swap positions of two child views in a ViewGroup.
 *
 * @param index1 Index of first child
 * @param index2 Index of second child
 */
fun ViewGroup.swapChildren(index1: Int, index2: Int) {
    if (index1 < 0 || index1 >= childCount || index2 < 0 || index2 >= childCount) {
        return
    }
    val view1 = getChildAt(index1)
    val view2 = getChildAt(index2)
    removeViewAt(index1)
    removeViewAt(index2 - 1)
    addView(view1, index2)
    addView(view2, index1)
    requestLayout()
    invalidate()
}

// endregion
