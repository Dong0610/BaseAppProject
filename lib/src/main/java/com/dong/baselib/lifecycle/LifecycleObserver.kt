package com.dong.baselib.lifecycle

import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// ============================================================================
// region MutableLiveData - Basic Extensions
// ============================================================================

fun <T> mutableLiveData(value: T): MutableLiveData<T> = MutableLiveData(value)

fun <T> mutableLiveData(): MutableLiveData<T> = MutableLiveData()

fun <T> MutableLiveData<T>.get(): T = requireNotNull(value)

fun <T> MutableLiveData<T>.getOrNull(): T? = value

fun <T> MutableLiveData<T>.getOrDefault(default: T): T = value ?: default

fun <T> MutableLiveData<T>.set(value: T) {
    this.value = value
}

fun <T> MutableLiveData<T>.post(value: T) {
    postValue(value)
}

fun <T> MutableLiveData<T>.update(transform: (T) -> T) {
    value?.let { this.value = transform(it) }
}

fun <T> MutableLiveData<T>.postUpdate(transform: (T) -> T) {
    value?.let { postValue(transform(it)) }
}

// endregion

// ============================================================================
// region MutableLiveData - List Extensions
// ============================================================================

fun <T> mutableListLiveData(initialList: List<T> = emptyList()): MutableLiveData<List<T>> =
    MutableLiveData(initialList)

fun <T> MutableLiveData<List<T>>.addItem(item: T) {
    value = (value ?: emptyList()) + item
}

fun <T> MutableLiveData<List<T>>.addItems(items: List<T>) {
    value = (value ?: emptyList()) + items
}

fun <T> MutableLiveData<List<T>>.removeItem(item: T) {
    value = value?.toMutableList()?.apply { remove(item) }
}

fun <T> MutableLiveData<List<T>>.removeAt(index: Int) {
    value = value?.toMutableList()?.apply {
        if (index in indices) removeAt(index)
    }
}

fun <T> MutableLiveData<List<T>>.updateAt(index: Int, newItem: T) {
    value = value?.toMutableList()?.apply {
        if (index in indices) this[index] = newItem
    }
}

fun <T> MutableLiveData<List<T>>.updateWhere(predicate: (T) -> Boolean, transform: (T) -> T) {
    value = value?.map { if (predicate(it)) transform(it) else it }
}

fun <T> MutableLiveData<List<T>>.clear() {
    value = emptyList()
}

val <T> MutableLiveData<List<T>>.size: Int get() = value?.size ?: 0

val <T> MutableLiveData<List<T>>.isEmpty: Boolean get() = value.isNullOrEmpty()

// endregion

// ============================================================================
// region LiveData - Combine/Merge Extensions
// ============================================================================

fun <T1, T2, R> LifecycleOwner.combineLiveData(
    source1: LiveData<T1>,
    source2: LiveData<T2>,
    transform: (T1?, T2?) -> R
): LiveData<R> = MediatorLiveData<R>().also { result ->
    var last1: T1? = null
    var last2: T2? = null

    fun update() { result.value = transform(last1, last2) }

    result.addSource(source1) { last1 = it; update() }
    result.addSource(source2) { last2 = it; update() }

    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                result.removeSource(source1)
                result.removeSource(source2)
                source.lifecycle.removeObserver(this)
            }
        }
    })
}

fun <T1, T2, T3, R> LifecycleOwner.combineLiveData(
    source1: LiveData<T1>,
    source2: LiveData<T2>,
    source3: LiveData<T3>,
    transform: (T1?, T2?, T3?) -> R
): LiveData<R> = MediatorLiveData<R>().also { result ->
    var last1: T1? = null
    var last2: T2? = null
    var last3: T3? = null

    fun update() { result.value = transform(last1, last2, last3) }

    result.addSource(source1) { last1 = it; update() }
    result.addSource(source2) { last2 = it; update() }
    result.addSource(source3) { last3 = it; update() }

    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                result.removeSource(source1)
                result.removeSource(source2)
                result.removeSource(source3)
                source.lifecycle.removeObserver(this)
            }
        }
    })
}

fun <R> LifecycleOwner.combineLiveData(
    vararg sources: LiveData<*>,
    transform: (Array<Any?>) -> R
): LiveData<R> = MediatorLiveData<R>().also { result ->
    val latest = Array<Any?>(sources.size) { null }

    sources.forEachIndexed { index, src ->
        @Suppress("UNCHECKED_CAST")
        result.addSource(src as LiveData<Any?>) { value ->
            latest[index] = value
            result.value = transform(latest)
        }
    }

    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                sources.forEach { result.removeSource(it) }
                source.lifecycle.removeObserver(this)
            }
        }
    })
}

// endregion

// ============================================================================
// region Coroutine - Lifecycle Launch Extensions
// ============================================================================

fun AppCompatActivity.launchOnLifecycle(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job = lifecycleScope.launch(context, start, block)

fun Fragment.launchOnLifecycle(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job = viewLifecycleOwner.lifecycleScope.launch(context, start, block)

fun AppCompatActivity.launchWhenStarted(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): Job = lifecycleScope.launch(context) {
    repeatOnLifecycle(Lifecycle.State.STARTED) { block() }
}

fun Fragment.launchWhenStarted(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): Job = viewLifecycleOwner.lifecycleScope.launch(context) {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) { block() }
}

fun AppCompatActivity.launchWhenResumed(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): Job = lifecycleScope.launch(context) {
    repeatOnLifecycle(Lifecycle.State.RESUMED) { block() }
}

fun Fragment.launchWhenResumed(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): Job = viewLifecycleOwner.lifecycleScope.launch(context) {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) { block() }
}

// endregion

// ============================================================================
// region Flow - Collection Extensions
// ============================================================================

inline fun <T> AppCompatActivity.collectFlow(
    flow: Flow<T>,
    crossinline onEach: suspend (T) -> Unit
): Job = lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        flow.collect { onEach(it) }
    }
}

inline fun <T> Fragment.collectFlow(
    flow: Flow<T>,
    crossinline onEach: suspend (T) -> Unit
): Job = viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        flow.collect { onEach(it) }
    }
}

inline fun <T> AppCompatActivity.collectFlowLatest(
    flow: Flow<T>,
    crossinline onEach: suspend (T) -> Unit
): Job = lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        flow.collectLatest { onEach(it) }
    }
}

inline fun <T> Fragment.collectFlowLatest(
    flow: Flow<T>,
    crossinline onEach: suspend (T) -> Unit
): Job = viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        flow.collectLatest { onEach(it) }
    }
}

inline fun <T> AppCompatActivity.collectFlowWhenResumed(
    flow: Flow<T>,
    crossinline onEach: suspend (T) -> Unit
): Job = lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.RESUMED) {
        flow.collect { onEach(it) }
    }
}

inline fun <T> Fragment.collectFlowWhenResumed(
    flow: Flow<T>,
    crossinline onEach: suspend (T) -> Unit
): Job = viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
        flow.collect { onEach(it) }
    }
}

// endregion

// ============================================================================
// region Coroutine - Parallel Execution Utilities
// ============================================================================

suspend fun <T> runParallel(vararg blocks: suspend () -> T): List<T> = coroutineScope {
    blocks.map { async { it() } }.awaitAll()
}

suspend fun <T> runParallelSafe(vararg blocks: suspend () -> T): List<Result<T>> =
    supervisorScope {
        blocks.map { block -> async { runCatching { block() } } }.awaitAll()
    }

suspend fun <I, R> Iterable<I>.mapParallel(
    maxConcurrency: Int = Int.MAX_VALUE,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    transform: suspend (I) -> R
): List<R> {
    require(maxConcurrency > 0) { "maxConcurrency must be > 0" }
    val semaphore = Semaphore(maxConcurrency)
    return coroutineScope {
        map { item ->
            async(dispatcher) {
                semaphore.withPermit { transform(item) }
            }
        }.awaitAll()
    }
}

suspend fun <I, R> Iterable<I>.mapParallelSafe(
    maxConcurrency: Int = Int.MAX_VALUE,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    transform: suspend (I) -> R
): List<Result<R>> {
    require(maxConcurrency > 0) { "maxConcurrency must be > 0" }
    val semaphore = Semaphore(maxConcurrency)
    return supervisorScope {
        map { item ->
            async(dispatcher) {
                runCatching { semaphore.withPermit { transform(item) } }
            }
        }.awaitAll()
    }
}

// endregion

// ============================================================================
// region Disposable Effect System
// ============================================================================

interface EffectResult {
    fun onStart() {}
    fun onResume() {}
    fun onDispose() {}
}

class EffectScope {
    private val effects = mutableListOf<EffectResult>()

    fun onDispose(block: () -> Unit): EffectResult = object : EffectResult {
        override fun onDispose() = block()
    }.also { effects += it }

    fun onResume(block: () -> Unit): EffectResult = object : EffectResult {
        override fun onResume() = block()
    }.also { effects += it }

    fun onStart(block: () -> Unit): EffectResult = object : EffectResult {
        override fun onStart() = block()
    }.also { effects += it }

    internal fun build(): EffectResult = object : EffectResult {
        override fun onStart() = effects.forEach { it.onStart() }
        override fun onResume() = effects.forEach { it.onResume() }
        override fun onDispose() = effects.forEach { it.onDispose() }
    }
}

class DisposableEffectManager(
    private val owner: LifecycleOwner,
    private val disposeOn: Lifecycle.Event = Lifecycle.Event.ON_DESTROY
) : DefaultLifecycleObserver {

    private var previousKeys: List<Any?>? = null
    private var currentEffect: EffectResult? = null
    private var isDestroyed = false

    init {
        owner.lifecycle.addObserver(this)
    }

    @Synchronized
    fun update(vararg keys: Any?, effect: EffectScope.() -> Unit) {
        requireMainThread()
        if (isDestroyed) return

        val newKeys = keys.toList()
        if (previousKeys == newKeys && currentEffect != null) return

        // Dispose previous effect
        currentEffect?.runCatching { onDispose() }
        currentEffect = null

        // Create new effect
        currentEffect = EffectScope().apply(effect).build()
        previousKeys = newKeys

        // Trigger callbacks based on current state
        when {
            owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) -> {
                currentEffect?.onStart()
                currentEffect?.onResume()
            }
            owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) -> {
                currentEffect?.onStart()
            }
        }
    }

    @Synchronized
    fun dispose() {
        requireMainThread()
        currentEffect?.runCatching { onDispose() }
        currentEffect = null
        previousKeys = null
    }

    override fun onStart(owner: LifecycleOwner) {
        currentEffect?.onStart()
    }

    override fun onResume(owner: LifecycleOwner) {
        currentEffect?.onResume()
    }

    override fun onStop(owner: LifecycleOwner) {
        if (disposeOn == Lifecycle.Event.ON_STOP) dispose()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (disposeOn == Lifecycle.Event.ON_DESTROY) {
            isDestroyed = true
            dispose()
            owner.lifecycle.removeObserver(this)
        }
    }

    private fun requireMainThread() {
        check(Looper.myLooper() == Looper.getMainLooper()) {
            "DisposableEffectManager must be called on the Main thread"
        }
    }
}

fun AppCompatActivity.disposableEffect(
    disposeOn: Lifecycle.Event = Lifecycle.Event.ON_DESTROY
): DisposableEffectManager = DisposableEffectManager(this, disposeOn)

fun Fragment.disposableEffect(
    disposeOn: Lifecycle.Event = Lifecycle.Event.ON_DESTROY
): DisposableEffectManager = DisposableEffectManager(viewLifecycleOwner, disposeOn)

// endregion
