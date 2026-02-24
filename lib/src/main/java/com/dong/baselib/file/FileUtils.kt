@file:Suppress("DEPRECATION")

package com.dong.baselib.file

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import com.dong.baselib.api.UnitFun1
import java.io.File

// ============================================================================
// This file contains core utility functions.
// Main functionality has been organized into:
// - ImageSaving.kt: Save/move images to gallery, downloads, cache, storage
// - FileSharing.kt: Share files and bitmaps (includes ShareThrottler)
// - ClipboardUtils.kt: Clipboard operations
// - AssetLoading.kt: Load from assets folder (includes Activity extensions)
// - UriUtils.kt: URI path resolution and file info
// - MimeUtils.kt: MIME type utilities
// - Bitmap.kt: Bitmap encoding, decoding, and transformations
// ============================================================================

// ============================================================================
// region Bitmap Resize Extensions
// ============================================================================

/**
 * Resize bitmap to specified width maintaining aspect ratio.
 */
fun Bitmap.resizeWidth(newWidth: Int): Bitmap {
    val aspectRatio = this.height.toFloat() / this.width
    val targetHeight = (newWidth * aspectRatio).toInt()
    return this.scale(newWidth, targetHeight)
}

/**
 * Resize bitmap to specified height maintaining aspect ratio.
 */
fun Bitmap.resizeHeight(newHeight: Int): Bitmap {
    val aspectRatio = this.width.toFloat() / this.height
    val targetWidth = (newHeight * aspectRatio).toInt()
    return this.scale(targetWidth, newHeight)
}

/**
 * Resize bitmap to specified dimensions.
 */
fun Bitmap.resize(newWidth: Int, newHeight: Int): Bitmap {
    return this.scale(newWidth, newHeight)
}

// endregion

// ============================================================================
// region Icon Utilities
// ============================================================================

/**
 * Get a Drawable from various data types.
 *
 * @param icon Can be: Int (drawable resource), Bitmap, or String (file path or asset path)
 * @return Drawable or null if cannot be resolved
 */
@SuppressLint("UseCompatLoadingForDrawables")
fun Context.getIconFromData(icon: Any): Drawable? {
    return when (icon) {
        is Int -> getDrawable(icon)
        is Bitmap -> icon.toDrawable(resources)
        is String -> {
            val file = File(icon)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(icon)
                bitmap?.toDrawable(resources)
            } else {
                loadDrawableFromAssets(icon)
            }
        }
        else -> null
    }
}

// endregion

// ============================================================================
// region File Listener Helper
// ============================================================================

/**
 * Save file with bitmap and report result via listener.
 *
 * @param bitmap The bitmap to save
 * @param fileName File name
 * @param folderName Folder name in gallery
 * @param onSaveSuccess Callback on success with URI string
 * @param onSaveError Callback on error with message
 */
suspend fun Context.saveFileByBitmap(
    bitmap: Bitmap?,
    fileName: String,
    folderName: String,
    onSaveSuccess: UnitFun1<String> = {},
    onSaveError: UnitFun1<String> = {}
) {
    bitmap?.let {
        val savedUri = saveImageToGallery(it, fileName, folderName)
        if (savedUri != null) {
            onSaveSuccess("$savedUri")
        } else {
            onSaveError("Failed to save image")
        }
    } ?: onSaveError("Bitmap is null")
}

// endregion
