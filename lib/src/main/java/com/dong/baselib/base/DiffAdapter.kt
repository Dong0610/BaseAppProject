package com.dong.baselib.base

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class ModelDiffCallback<T : Any>(
    private val areItemsTheSameCallback: (oldItem: T, newItem: T) -> Boolean,
    private val areContentsTheSameCallback: (oldItem: T, newItem: T) -> Boolean = { o, n -> o == n },
    private val payloadProvider: ((oldItem: T, newItem: T) -> Any?)? = null
) : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
        areItemsTheSameCallback(oldItem, newItem)

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
        areContentsTheSameCallback(oldItem, newItem)

    override fun getChangePayload(oldItem: T, newItem: T): Any? =
        payloadProvider?.invoke(oldItem, newItem)
}

abstract class DiffAdapter<T : Any, VB : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, DiffAdapter<T, VB>.ViewHolder>(diffCallback), LifecycleOwner {

    constructor(
        areItemsTheSame: (old: T, new: T) -> Boolean,
        areContentsTheSame: (old: T, new: T) -> Boolean = { o, n -> o == n },
        payloadProvider: ((old: T, new: T) -> Any?)? = null
    ) : this(
        ModelDiffCallback(
            areItemsTheSameCallback = areItemsTheSame,
            areContentsTheSameCallback = areContentsTheSame,
            payloadProvider = payloadProvider
        )
    )

    var currentPosition = MutableLiveData(RecyclerView.NO_POSITION)
        private set

    // Backing list — kept in sync with every submitList so getItem() always returns fresh data.
    // changeItemWithPos reads/writes this directly and calls notifyItemChanged → O(1), no DiffUtil.
    private val mutableItems = mutableListOf<T>()

    override fun submitList(list: List<T>?) {
        mutableItems.clear()
        list?.let { mutableItems.addAll(it) }
        super.submitList(list)
    }

    override fun submitList(list: List<T>?, commitCallback: Runnable?) {
        mutableItems.clear()
        list?.let { mutableItems.addAll(it) }
        super.submitList(list, commitCallback)
    }

    override fun getItem(position: Int): T = mutableItems[position]

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

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        context = null
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    fun stringRes(@StringRes res: Int): String = context?.getString(res) ?: ""

    fun stringRes(@StringRes res: Int, vararg args: Any): String =
        context?.getString(res, *args) ?: ""

    abstract fun createBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): VB
    abstract fun VB.bind(item: T, position: Int)

    open fun VB.bind(item: T, position: Int, payloads: List<Any>) {
        bind(item, position)
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
        val item = getItem(position)
        holder.binding.bind(item, position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        val item = getItem(position)
        if (payloads.isEmpty()) {
            holder.binding.bind(item, position)
        } else {
            holder.binding.bind(item, position, payloads)
        }
    }

    fun submitListCustom(newList: List<T>?, commitCallback: (() -> Unit)? = null) {
        submitList(newList?.toList(), commitCallback)
    }

    fun removeItem(position: Int) {
        val current = currentList.toMutableList()
        if (position in current.indices) {
            current.removeAt(position)
            submitListCustom(current)
        }
    }

    fun addItem(item: T, index: Int = currentList.size) {
        val current = currentList.toMutableList()
        val safeIndex = index.coerceIn(0, current.size)
        current.add(safeIndex, item)
        submitListCustom(current)
    }

    // O(1) — updates one item without running DiffUtil over the whole list.
    fun changeItemWithPos(index: Int, newItem: T) {
        if (index in mutableItems.indices) {
            mutableItems[index] = newItem
            notifyItemChanged(index)
        }
    }

    fun setCurrentPos(position: Int) {
        val prev = currentPosition.value ?: RecyclerView.NO_POSITION
        if (prev == position) return
        
        currentPosition.value = position
        
        if (prev != RecyclerView.NO_POSITION && prev < itemCount) {
            notifyItemChanged(prev)
        }
        if (position != RecyclerView.NO_POSITION && position < itemCount) {
            notifyItemChanged(position)
        }
    }
}