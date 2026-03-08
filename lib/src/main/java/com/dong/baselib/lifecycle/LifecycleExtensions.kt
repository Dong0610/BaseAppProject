package com.dong.baselib.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Common extension to handle lifecycle-aware collection of StateFlow.
 * This ensures that collection only happens when the lifecycle is in the specified state
 * and is automatically cancelled when the lifecycle is destroyed.
 */
fun <T> StateFlow<T>.launchCollect(
    owner: LifecycleOwner,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    collector: (T) -> Unit
): Job = owner.lifecycleScope.launch(dispatcher) {
    owner.repeatOnLifecycle(state) {
        collect { collector(it) }
    }
}
