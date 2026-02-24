package com.dong.baselib.api

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// ============================================================================
// region Function Type Aliases - No Return
// ============================================================================
/**
 * Function type aliases for callbacks that return Unit (void).
 *
 * Example usage:
 * ```
 * // UnitFun0 - No parameters
 * var onComplete: UnitFun0? = null
 * onComplete = { showToast("Done!") }
 * onComplete?.invoke()
 *
 * // UnitFun1<T> - 1 parameter
 * var onItemClick: UnitFun1<Int>? = null
 * onItemClick = { position -> selectItem(position) }
 * onItemClick?.invoke(5)
 *
 * // UnitFun2<T, T1> - 2 parameters
 * var onRangeChanged: UnitFun2<Int, Int>? = null
 * onRangeChanged = { start, end -> updateRange(start, end) }
 * onRangeChanged?.invoke(0, 100)
 *
 * // In Adapter
 * class MyAdapter {
 *     var onItemClick: UnitFun1<Item> = emptyLambda1()
 *     var onItemLongClick: UnitFun2<Item, Int> = emptyLambda2()
 * }
 * adapter.onItemClick = { item -> navigateToDetail(item) }
 * ```
 */

/** No parameters */
typealias UnitFun0 = () -> Unit

/** 1 parameter */
typealias UnitFun1<T> = (T) -> Unit

/** 2 parameters */
typealias UnitFun2<T, T1> = (T, T1) -> Unit

/** 3 parameters */
typealias UnitFun3<T, T1, T2> = (T, T1, T2) -> Unit

/** 4 parameters */
typealias UnitFun4<T, T1, T2, T3> = (T, T1, T2, T3) -> Unit

/** 5 parameters */
typealias UnitFun5<T, T1, T2, T3, T4> = (T, T1, T2, T3, T4) -> Unit

// endregion

// ============================================================================
// region Function Type Aliases - With Return
// ============================================================================
/**
 * Function type aliases for callbacks that return a value.
 *
 * Example usage:
 * ```
 * // Fun0<R> - No params, returns R
 * val getTimestamp: Fun0<Long> = { System.currentTimeMillis() }
 * val time = getTimestamp()
 *
 * // Fun1<T, R> - 1 param, returns R
 * val formatPrice: Fun1<Int, String> = { price -> "$${price}.00" }
 * val priceText = formatPrice(99) // "$99.00"
 *
 * // Fun2<T, T1, R> - 2 params, returns R
 * val calculateTotal: Fun2<Int, Float, Float> = { qty, price -> qty * price }
 * val total = calculateTotal(5, 19.99f) // 99.95f
 *
 * // Fun3<T, T1, T2, R> - 3 params, returns R
 * val buildFullName: Fun3<String, String, String, String> = { first, middle, last ->
 *     "$first $middle $last"
 * }
 * ```
 */

/** No parameters, returns R */
typealias Fun0<R> = () -> R

/** 1 parameter, returns R */
typealias Fun1<T, R> = (T) -> R

/** 2 parameters, returns R */
typealias Fun2<T, T1, R> = (T, T1) -> R

/** 3 parameters, returns R */
typealias Fun3<T, T1, T2, R> = (T, T1, T2) -> R

// endregion

// ============================================================================
// region Common Callback Aliases
// ============================================================================
/**
 * Common callback type aliases for frequently used parameter types.
 *
 * Example usage:
 * ```
 * // BooleanCallback
 * fun checkPermission(callback: BooleanCallback) {
 *     val granted = hasPermission()
 *     callback(granted)
 * }
 * checkPermission { granted -> if (granted) proceed() }
 *
 * // StringCallback
 * fun fetchName(callback: StringCallback) {
 *     callback("John Doe")
 * }
 *
 * // IntCallback
 * fun getCount(callback: IntCallback) {
 *     callback(42)
 * }
 *
 * // ResultCallback<T>
 * fun loadData(callback: ResultCallback<User>) {
 *     try {
 *         val user = api.getUser()
 *         callback(Result.success(user))
 *     } catch (e: Exception) {
 *         callback(Result.failure(e))
 *     }
 * }
 *
 * // NullableCallback<T>
 * fun findItem(id: Int, callback: NullableCallback<Item>) {
 *     val item = items.find { it.id == id }
 *     callback(item) // Can be null
 * }
 * ```
 */

/** Boolean callback */
typealias BooleanCallback = (Boolean) -> Unit

/** String callback */
typealias StringCallback = (String) -> Unit

/** Int callback */
typealias IntCallback = (Int) -> Unit

/** Success/Error callback */
typealias ResultCallback<T> = (Result<T>) -> Unit

/** Nullable callback */
typealias NullableCallback<T> = (T?) -> Unit

// endregion

// ============================================================================
// region Empty Lambda Constants
// ============================================================================
/**
 * Empty lambda constants for safe default callbacks (no null checks needed).
 *
 * Example usage:
 * ```
 * // emptyLambda - Default no-op callback
 * class Button {
 *     var onClick: UnitFun0 = emptyLambda  // Safe default
 * }
 * button.onClick() // Does nothing, but won't crash
 *
 * // emptyLambda1<T>() - Default for 1 param
 * class RecyclerAdapter {
 *     var onItemClick: UnitFun1<Int> = emptyLambda1()
 * }
 * adapter.onItemClick(0) // Safe, does nothing
 *
 * // emptyLambda2<T, T1>() - Default for 2 params
 * class SeekBar {
 *     var onProgressChanged: UnitFun2<Int, Boolean> = emptyLambda2()
 * }
 * seekBar.onProgressChanged(50, true) // Safe, does nothing
 *
 * // Real-world Adapter example
 * class MyAdapter : DiffAdapter<Item, ItemBinding>() {
 *     var onItemClick: UnitFun1<Item> = emptyLambda1()
 *     var onDeleteClick: UnitFun1<Int> = emptyLambda1()
 *
 *     override fun onBind(holder: ViewHolder, position: Int) {
 *         holder.binding.root.click { onItemClick(getItem(position)) }
 *         holder.binding.btnDelete.click { onDeleteClick(position) }
 *     }
 * }
 * // Usage in Fragment
 * adapter.onItemClick = { item -> navigateToDetail(item) }
 * adapter.onDeleteClick = { position -> deleteItem(position) }
 * ```
 */

/** Empty lambda - no parameters */
val emptyLambda: UnitFun0 = {}

/** Empty lambda - 1 parameter */
fun <T> emptyLambda1(): UnitFun1<T> = {}

/** Empty lambda - 2 parameters */
fun <T, T1> emptyLambda2(): UnitFun2<T, T1> = { _, _ -> }

// endregion