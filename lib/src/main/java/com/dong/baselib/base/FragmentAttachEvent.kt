package com.dong.baselib.base

import android.os.Bundle

/**
 * Interface for Fragment-Activity communication
 *
 * Usage in Fragment:
 * ```kotlin
 * (activity as? FragmentAttachEvent)?.fragmentSendData("result") {
 *     putInt("song_id", 123)
 *     putString("song_name", "Happy Birthday")
 *     putBoolean("play", true)
 * }
 * ```
 *
 * Usage in Activity:
 * ```kotlin
 * override fun fragmentSendData(key: String, bundle: Bundle) {
 *     when (key) {
 *         "result" -> {
 *             val songId = bundle.getInt("song_id")
 *             val songName = bundle.getString("song_name")
 *             val play = bundle.getBoolean("play")
 *         }
 *     }
 * }
 * ```
 */
interface FragmentAttachEvent {
    /** Called when fragment requests back navigation */
    fun fragmentOnBack() {}

    /** Called when fragment sends an action with any data */
    fun fragmentAction(data: Any) {}

    /** Called when fragment sends data with a key identifier */
    fun fragmentSendData(key: String, bundle: Bundle) {}

    /** Convenience method to send data using Bundle DSL */
    fun fragmentSendData(key: String, block: Bundle.() -> Unit) {
        fragmentSendData(key, Bundle().apply(block))
    }
}