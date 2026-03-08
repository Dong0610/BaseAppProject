package com.dong.baselib.extensions

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

// ============================================================================
// region Image — single / multiple
// ============================================================================

/**
 * Register a launcher to pick a **single image**.
 *
 * ```kotlin
 * val pickImage = registerPickSingleImage { uri -> uri?.let { load(it) } }
 * pickImage.launchImage()
 * ```
 */
fun FragmentActivity.registerPickSingleImage(
    onResult: (Uri?) -> Unit
): ActivityResultLauncher<PickVisualMediaRequest> =
    registerForActivityResult(PickVisualMedia()) { onResult(it) }

fun Fragment.registerPickSingleImage(
    onResult: (Uri?) -> Unit
): ActivityResultLauncher<PickVisualMediaRequest> =
    registerForActivityResult(PickVisualMedia()) { onResult(it) }

/**
 * Register a launcher to pick **multiple images**.
 *
 * @param maxItems Max images selectable (default 9).
 *
 * ```kotlin
 * val pickImages = registerPickMultipleImages { uris -> show(uris) }
 * pickImages.launchImage()
 * ```
 */
fun FragmentActivity.registerPickMultipleImages(
    maxItems: Int = 9,
    onResult: (List<Uri>) -> Unit
): ActivityResultLauncher<PickVisualMediaRequest> =
    registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxItems)) {
        onResult(it)
    }

fun Fragment.registerPickMultipleImages(
    maxItems: Int = 9,
    onResult: (List<Uri>) -> Unit
): ActivityResultLauncher<PickVisualMediaRequest> =
    registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxItems)) {
        onResult(it)
    }

/** Launch an image-only [PickVisualMedia] / [PickMultipleVisualMedia] picker. */
fun ActivityResultLauncher<PickVisualMediaRequest>.launchImage() {
    val now = android.os.SystemClock.elapsedRealtime()
    if (now - launchImage_lastTs < 1000L) return
    launchImage_lastTs = now
    launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
}
private var launchImage_lastTs = 0L

// endregion

// ============================================================================
// region Video — single / multiple
// ============================================================================

/**
 * Register a launcher to pick a **single video**.
 *
 * ```kotlin
 * val pickVideo = registerPickSingleVideo { uri -> uri?.let { play(it) } }
 * pickVideo.launchVideo()
 * ```
 */
fun FragmentActivity.registerPickSingleVideo(
    onResult: (Uri?) -> Unit
): ActivityResultLauncher<PickVisualMediaRequest> =
    registerForActivityResult(PickVisualMedia()) { onResult(it) }

fun Fragment.registerPickSingleVideo(
    onResult: (Uri?) -> Unit
): ActivityResultLauncher<PickVisualMediaRequest> =
    registerForActivityResult(PickVisualMedia()) { onResult(it) }

/**
 * Register a launcher to pick **multiple videos**.
 *
 * @param maxItems Max videos selectable (default 9).
 */
fun FragmentActivity.registerPickMultipleVideos(
    maxItems: Int = 9,
    onResult: (List<Uri>) -> Unit
): ActivityResultLauncher<PickVisualMediaRequest> =
    registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxItems)) {
        onResult(it)
    }

fun Fragment.registerPickMultipleVideos(
    maxItems: Int = 9,
    onResult: (List<Uri>) -> Unit
): ActivityResultLauncher<PickVisualMediaRequest> =
    registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxItems)) {
        onResult(it)
    }

/** Launch a video-only [PickVisualMedia] / [PickMultipleVisualMedia] picker. */
fun ActivityResultLauncher<PickVisualMediaRequest>.launchVideo() {
    launch(PickVisualMediaRequest(PickVisualMedia.VideoOnly))
}

// endregion

// ============================================================================
// region Audio — single / multiple
// ============================================================================

/**
 * Register a launcher to pick a **single audio file**.
 *
 * ```kotlin
 * val pickAudio = registerPickSingleAudio { uri -> uri?.let { play(it) } }
 * pickAudio.launch(arrayOf("audio/*"))
 * ```
 */
fun FragmentActivity.registerPickSingleAudio(
    onResult: (Uri?) -> Unit
): ActivityResultLauncher<Array<String>> =
    registerForActivityResult(ActivityResultContracts.OpenDocument()) { onResult(it) }

fun Fragment.registerPickSingleAudio(
    onResult: (Uri?) -> Unit
): ActivityResultLauncher<Array<String>> =
    registerForActivityResult(ActivityResultContracts.OpenDocument()) { onResult(it) }

/**
 * Register a launcher to pick **multiple audio files**.
 *
 * ```kotlin
 * val pickAudios = registerPickMultipleAudios { uris -> play(uris) }
 * pickAudios.launchAudio()
 * ```
 */
fun FragmentActivity.registerPickMultipleAudios(
    onResult: (List<Uri>) -> Unit
): ActivityResultLauncher<Array<String>> =
    registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { onResult(it) }

fun Fragment.registerPickMultipleAudios(
    onResult: (List<Uri>) -> Unit
): ActivityResultLauncher<Array<String>> =
    registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { onResult(it) }

/** Launch an audio-only document picker. */
fun ActivityResultLauncher<Array<String>>.launchAudio() {
    launch(arrayOf("audio/*"))
}

// endregion

// ============================================================================
// region File — single / multiple (with MIME filter)
// ============================================================================

/**
 * Register a launcher to pick a **single file**.
 *
 * ```kotlin
 * val pickFile = registerPickSingleFile { uri -> uri?.let { open(it) } }
 * pickFile.launchFile()                          // any file
 * pickFile.launchFile("application/pdf")         // PDFs only
 * pickFile.launchFile("image/*", "video/*")      // images or videos
 * ```
 */
fun FragmentActivity.registerPickSingleFile(
    onResult: (Uri?) -> Unit
): ActivityResultLauncher<Array<String>> =
    registerForActivityResult(ActivityResultContracts.OpenDocument()) { onResult(it) }

fun Fragment.registerPickSingleFile(
    onResult: (Uri?) -> Unit
): ActivityResultLauncher<Array<String>> =
    registerForActivityResult(ActivityResultContracts.OpenDocument()) { onResult(it) }

/**
 * Register a launcher to pick **multiple files**.
 *
 * ```kotlin
 * val pickFiles = registerPickMultipleFiles { uris -> attach(uris) }
 * pickFiles.launchFile("application/pdf", "application/msword")
 * ```
 */
fun FragmentActivity.registerPickMultipleFiles(
    onResult: (List<Uri>) -> Unit
): ActivityResultLauncher<Array<String>> =
    registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { onResult(it) }

fun Fragment.registerPickMultipleFiles(
    onResult: (List<Uri>) -> Unit
): ActivityResultLauncher<Array<String>> =
    registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { onResult(it) }

/**
 * Launch a file picker filtered by the given MIME types.
 * Defaults to `*/*` (all files) when no types are provided.
 */
fun ActivityResultLauncher<Array<String>>.launchFile(vararg mimeTypes: String) {
    launch(if (mimeTypes.isEmpty()) arrayOf("*/*") else arrayOf(*mimeTypes))
}

// endregion
 **/