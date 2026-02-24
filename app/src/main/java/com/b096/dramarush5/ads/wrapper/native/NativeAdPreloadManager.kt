package com.b096.dramarush5.ads.wrapper.native

import android.app.Activity
import com.ads.control.ads.wrapper.ApNativeAd
import com.ads.control.helper.adnative.preload.NativeAdPreload
import com.ads.control.helper.adnative.preload.NativePreloadState
import com.b096.dramarush5.BuildConfig
import com.b096.dramarush5.ads.wrapper.native.NativeAdPreloadManager.isNativeQueueEmptyAndNoPreloadActionInProgress
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.WeakReference

/**
 * Manager class for handling native ad preloading operations with optimized memory management
 */
internal object NativeAdPreloadManager {
    private val preloadInstance: NativeAdPreload by lazy {
        NativeAdPreload.getInstance()
    }

    // Cache for activity references to prevent memory leaks
    private var activityRef: WeakReference<Activity>? = null

    /**
     * Get the number of available ads for a specific placement
     * @return The number of available ads in the buffer
     */
    fun getAvailableAdCount(placement: NativePlacement): Int {
        return if (placement.canShowAds()) {
            preloadInstance.getNativeAdBuffer(
                listId = placement.listId(),
                layoutId = placement.preloadLayoutId()
            ).size
        } else {
            0
        }
    }

    /**
     * Get the ad preload state flow for monitoring loading status
     * @return StateFlow emitting the current preload state
     */
    fun getAdPreloadState(placement: NativePlacement): StateFlow<NativePreloadState> {
        return preloadInstance.getAdPreloadState(
            listId = placement.listId(),
            layoutId = placement.preloadLayoutId()
        )
    }

    /**
     * Poll an ad from the preload buffer
     * @return ApNativeAd if available, null otherwise
     */
    fun pollAdNative(placement: NativePlacement): ApNativeAd? {
        if (!placement.canShowAds()) return null
        return preloadInstance.pollAdNative(
            listId = placement.listId(),
            layoutId = placement.preloadLayoutId()
        )
    }

    /**
     * Forces preloading of ads for the specified placement.
     *
     * This method always triggers a preload request even if there are already ads available
     * in the buffer.
     *
     * Preloading is skipped immediately if [placement.canShowAds] returns false, meaning
     * the remote config for this ad placement has been disable.
     *
     * @param activity the host [Activity]
     * @param placement the [NativePlacement] containing ad configuration
     * @param buffer the number of ads to preload in advance
     */
    fun preloadAd(
        activity: Activity,
        placement: NativePlacement,
        buffer: Int = 1,
    ) {
        if (!placement.canShowAds()) return
        activityRef = WeakReference(activity)
        handleInternalPreload(
            placement = placement,
            buffer = buffer
        )
    }

    /**
     * Attempts to safely preload ads for the specified placement.
     *
     * This method only triggers preloading under conditions determined by [preloadIfSafe]:
     *
     * Preloading is skipped immediately if [placement.canShowAds] returns false, meaning
     * the remote config for this ad placement has been disable.
     *
     * @param activity the host [Activity]
     * @param placement the [NativePlacement] containing ad configuration
     * @param buffer the number of ads to preload in advance
     */
    fun safePreloadAd(
        activity: Activity,
        placement: NativePlacement,
        buffer: Int = 1
    ) {
        if (!placement.canShowAds()) return
        activityRef = WeakReference(activity)

        preloadIfSafe(
            placement = placement,
            buffer = buffer
        )
    }

    /**
     * Attempts to preload native ads for the given placement, but only under safe conditions.
     *
     * Preloading will be triggered if EITHER of the following is true:
     * - The app is running in debug mode (`BuildConfig.build_debug == true`), in which case
     *   preloading always runs regardless of the current queue state.
     * - OR the app is in release mode AND [isNativeQueueEmptyAndNoPreloadActionInProgress] returns true,
     *   meaning no ads are buffered and no preload action is currently running.
     *
     * In other words:
     * - Debug build → always preload.
     * - Release build → only preload if the buffer is empty and no preload is active.
     *
     * @param placement the [NativePlacement] configuration for which ads should be preloaded.
     * @param buffer the number of ads to buffer in advance.
     */
    private fun preloadIfSafe(
        placement: NativePlacement,
        buffer: Int
    ) {
        val isDebug = BuildConfig.build_debug
        val shouldPreload = this.isNativeQueueEmptyAndNoPreloadActionInProgress(placement = placement)

        if (!isDebug && !shouldPreload) return

        handleInternalPreload(
            placement = placement,
            buffer = buffer
        )
    }

    /**
     * Checks the preload queue state for a specific native placement.
     *
     * The SDK's `isPreloadAvailable` method returns `true` if:
     * - There is at least one ad available in the buffer for the given [listId] - [layoutId], **or**
     * - If no ad is available, the placement for [listId] - [layoutId] is currently in the process of preloading.
     *
     * In this case, we need the opposite result, meaning we only care when:
     * - There are no ads in the buffer, **and**
     * - There is no ongoing preload action for that placement.
     *
     * @param placement the [NativePlacement] containing the necessary configuration for native ads
     * @return `true` if the preload queue is empty **and** no preload action is in progress;
     *         `false` if an ad is available or preloading is ongoing
     */
    private fun isNativeQueueEmptyAndNoPreloadActionInProgress(
        placement: NativePlacement
    ) : Boolean {
        return preloadInstance.isPreloadAvailable(
            listId = placement.listId(),
            layoutId = placement.preloadLayoutId()
        ).not()
    }

    private fun handleInternalPreload(
        placement: NativePlacement,
        buffer: Int
    ) {
        activityRef?.get()?.let { activity ->
            preloadInstance.preload(
                activity = activity,
                listId = placement.listId(),
                layoutId = placement.preloadLayoutId(),
                buffer = buffer
            )
        }
    }

    /**
     * Clear any cached references and resources
     */
    fun cleanup() {
        activityRef?.clear()
        activityRef = null
    }
}
