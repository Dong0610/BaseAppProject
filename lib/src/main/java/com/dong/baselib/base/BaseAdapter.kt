package com.dong.baselib.base

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.dong.baselib.api.UnitFun1
import com.dong.baselib.api.emptyLambda1
import java.util.Collections

/**
 * Base RecyclerView Adapter with ViewBinding and Lifecycle support.
 *
 * Example usage:
 * ```
 * class MyAdapter : BaseAdapter<Item, ItemBinding>() {
 *     override fun createBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int) =
 *         ItemBinding.inflate(inflater, parent, false)
 *
 *     override fun ItemBinding.bind(item: Item, position: Int, lifecycleOwner: LifecycleOwner) {
 *         tvName.text = item.name
 *         // Observe LiveData with ViewHolder lifecycle - auto cleanup when recycled
 *         someLiveData.observe(lifecycleOwner) { value ->
 *             tvValue.text = value
 *         }
 *     }
 * }
 *
 * // Usage
 * adapter.submitList(items)
 * ```
 */
abstract class BaseAdapter<T, VB : ViewBinding> :
    RecyclerView.Adapter<BaseAdapter<T, VB>.ViewHolder>(), LifecycleOwner {

    private val listItem = mutableListOf<T>()
    val currentPosition = MutableLiveData(RecyclerView.NO_POSITION)

    // Lifecycle support
    private val lifecycleRegistry = LifecycleRegistry(this)
    protected var context: Context? = null
        private set

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    fun stringRes(@StringRes res: Int): String = context?.getString(res) ?: ""

    fun stringRes(@StringRes res: Int, vararg args: Any): String = context?.getString(res, *args) ?: ""

    var onPositionChanged: UnitFun1<Int> = emptyLambda1()

    abstract fun createBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): VB
    abstract fun VB.bind(item: T, position: Int)

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
    inner class ViewHolder(val binding: VB) : RecyclerView.ViewHolder(binding.root) {

        val context: Context
            get() = binding.root.context

        fun stringRes(@StringRes res: Int): String = context.getString(res)

        fun stringRes(@StringRes res: Int, vararg args: Any): String = context.getString(res, *args)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val vb = createBinding(inflater, parent, viewType)
        return ViewHolder(vb)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position !in listItem.indices) return
        val item = listItem[position]
        holder.binding.bind(item, position)
    }

    override fun getItemCount(): Int = listItem.size

    /** Check if adapter has no items */
    fun isEmpty(): Boolean = listItem.isEmpty()

    /** Check if adapter has items */
    fun isNotEmpty(): Boolean = listItem.isNotEmpty()

    /** Get item at position safely, returns null if out of bounds */
    fun getItem(position: Int): T? = listItem.getOrNull(position)

    /** Get immutable copy of current list */
    fun getListItem(): List<T> = listItem.toList()

    /** Get mutable list reference (use with caution) */
    fun getMutableList(): MutableList<T> = listItem

    /** Submit new list and refresh all items */
    fun submitList(items: List<T>)  =apply{
        this.listItem.clear()
        this.listItem.addAll(items)
        notifyDataSetChanged()
    }

    /** Add single item at specific index */
    fun addItem(item: T, index: Int = listItem.size) {
        val insertIndex = index.coerceIn(0, listItem.size)
        this.listItem.add(insertIndex, item)
        notifyItemInserted(insertIndex)
    }

    /** Add multiple items at end of list */
    fun addItems(items: List<T>) {
        if (items.isEmpty()) return
        val startPos = listItem.size
        this.listItem.addAll(items)
        notifyItemRangeInserted(startPos, items.size)
    }

    /** Remove item at position */
    fun removeItem(position: Int) {
        if (position in 0 until listItem.size) {
            listItem.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, listItem.size - position)
        }
    }

    /** Remove specific item */
    fun removeItem(item: T) {
        val index = listItem.indexOf(item)
        if (index != -1) {
            listItem.removeAt(index)
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, listItem.size - index)
        }
    }

    /** Update item at position */
    fun updateItem(index: Int, newItem: T) {
        if (index in 0 until listItem.size) {
            listItem[index] = newItem
            notifyItemChanged(index)
        }
    }

    /** Move item from one position to another (for drag & drop) */
    fun onMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(listItem, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(listItem, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    /** Remove item by swipe */
    fun swipe(position: Int) {
        removeItem(position)
    }

    /** Clear all items */
    fun clearAll() {
        val size = listItem.size
        listItem.clear()
        notifyItemRangeRemoved(0, size)
    }

    /** Set current selected position and notify changes */
    fun setCurrentPos(position: Int) {
        val prev = currentPosition.value ?: RecyclerView.NO_POSITION
        if (prev == position) return

        if (prev in 0 until itemCount) notifyItemChanged(prev)
        currentPosition.value = position
        if (position in 0 until itemCount) notifyItemChanged(position)
        onPositionChanged(position)
    }

    /** Get current selected position */
    fun getCurrentPos(): Int = currentPosition.value ?: RecyclerView.NO_POSITION
}
