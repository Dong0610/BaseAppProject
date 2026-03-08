package com.dong.baselib.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import androidx.core.net.toUri

// ============================================================================
// region Basic Image Loading
// ============================================================================

/**
 * Load image from URL with default caching.
 */
fun ImageView.load(url: String?) {
    if (url.isNullOrEmpty()) return
    Glide.with(context)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image from assets folder. Path should be relative to assets/, e.g. "style/img_style_modern.webp".
 */
fun ImageView.loadAsset(assetPath: String?) {
    if (assetPath.isNullOrEmpty()) return
    val uri = Uri.parse("file:///android_asset/$assetPath")
    Glide.with(context)
        .load(uri)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image from drawable resource.
 */
fun ImageView.load(@DrawableRes resId: Int) {
    Glide.with(context)
        .load(resId)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image from URI.
 */
fun ImageView.load(uri: Uri?) {
    if (uri == null) return
    Glide.with(context)
        .load(uri)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image from File.
 */
fun ImageView.load(file: File?) {
    if (file == null || !file.exists()) return
    Glide.with(context)
        .load(file)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image from Bitmap.
 */
fun ImageView.load(bitmap: Bitmap?) {
    if (bitmap == null) return
    Glide.with(context)
        .load(bitmap)
        .into(this)
}

// endregion

// ============================================================================
// region Resize Image Loading
// ============================================================================

/**
 * Load image from drawable resource with resize.
 *
 * @param resId Drawable resource ID
 * @param width Target width in pixels
 * @param height Target height in pixels
 */
fun ImageView.loadResize(@DrawableRes resId: Int, width: Int, height: Int) {
    Glide.with(context)
        .load(resId)
        .override(width, height)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image from drawable resource with optional resize.
 * If only width or height is provided, the other dimension will be calculated
 * to maintain aspect ratio.
 *
 * @param resId Drawable resource ID
 * @param width Target width in pixels (null to calculate from height)
 * @param height Target height in pixels (null to calculate from width)
 */
fun ImageView.loadResize(@DrawableRes resId: Int, width: Int? = null, height: Int? = null) {
    when {
        width != null && height != null -> {
            loadResize(resId, width, height)
        }
        width != null || height != null -> {
            Glide.with(context)
                .asBitmap()
                .load(resId)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val originalWidth = resource.width
                        val originalHeight = resource.height
                        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()

                        val targetWidth = width ?: (height!! * aspectRatio).toInt()
                        val targetHeight = height ?: (width!! / aspectRatio).toInt()

                        Glide.with(context)
                            .load(resId)
                            .override(targetWidth, targetHeight)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(this@loadResize)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
        else -> load(resId)
    }
}

/**
 * Load image from URL with resize.
 *
 * @param url Image URL
 * @param width Target width in pixels
 * @param height Target height in pixels
 */
fun ImageView.loadResize(url: String?, width: Int, height: Int) {
    if (url.isNullOrEmpty()) return
    Glide.with(context)
        .load(url)
        .override(width, height)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image from URL with optional resize.
 * If only width or height is provided, the other dimension will be calculated
 * to maintain aspect ratio.
 *
 * @param url Image URL
 * @param width Target width in pixels (null to calculate from height)
 * @param height Target height in pixels (null to calculate from width)
 */
fun ImageView.loadResize(url: String?, width: Int?, height: Int?) {
    if (url.isNullOrEmpty()) return
    when {
        width != null && height != null -> {
            loadResize(url, width, height)
        }
        width != null || height != null -> {
            Glide.with(context)
                .asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val originalWidth = resource.width
                        val originalHeight = resource.height
                        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()

                        val targetWidth = width ?: (height!! * aspectRatio).toInt()
                        val targetHeight = height ?: (width!! / aspectRatio).toInt()

                        Glide.with(context)
                            .load(url)
                            .override(targetWidth, targetHeight)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(this@loadResize)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
        else -> load(url)
    }
}

/**
 * Load image from URI with resize.
 */
fun ImageView.loadResize(uri: Uri?, width: Int, height: Int) {
    if (uri == null) return
    Glide.with(context)
        .load(uri)
        .override(width, height)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image from File with resize.
 */
fun ImageView.loadResize(file: File?, width: Int, height: Int) {
    if (file == null || !file.exists()) return
    Glide.with(context)
        .load(file)
        .override(width, height)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image with resize and center crop.
 */
fun ImageView.loadResizeCenterCrop(@DrawableRes resId: Int, width: Int, height: Int) {
    Glide.with(context)
        .load(resId)
        .override(width, height)
        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image from URL with resize and center crop.
 */
fun ImageView.loadResizeCenterCrop(url: String?, width: Int, height: Int) {
    if (url.isNullOrEmpty()) return
    Glide.with(context)
        .load(url)
        .override(width, height)
        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image with resize and fit center.
 */
fun ImageView.loadResizeFitCenter(@DrawableRes resId: Int, width: Int, height: Int) {
    Glide.with(context)
        .load(resId)
        .override(width, height)
        .fitCenter()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image from URL with resize and fit center.
 */
fun ImageView.loadResizeFitCenter(url: String?, width: Int, height: Int) {
    if (url.isNullOrEmpty()) return
    Glide.with(context)
        .load(url)
        .override(width, height)
        .fitCenter()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image with resize and rounded corners.
 */
fun ImageView.loadResizeRounded(@DrawableRes resId: Int, width: Int, height: Int, radiusPx: Int) {
    Glide.with(context)
        .load(resId)
        .override(width, height)
        .transform(CenterCrop(), RoundedCorners(radiusPx))
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image from URL with resize and rounded corners.
 */
fun ImageView.loadResizeRounded(url: String?, width: Int, height: Int, radiusPx: Int) {
    if (url.isNullOrEmpty()) return
    Glide.with(context)
        .load(url)
        .override(width, height)
        .transform(CenterCrop(), RoundedCorners(radiusPx))
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

// endregion

// ============================================================================
// region Advanced Image Loading with Options
// ============================================================================

/**
 * Load image with placeholder and error drawable.
 */
fun ImageView.load(
    url: String?,
    @DrawableRes placeholder: Int = 0,
    @DrawableRes error: Int = 0
) {
    if (url.isNullOrEmpty()) {
        if (error != 0) setImageResource(error)
        return
    }
    Glide.with(context)
        .load(url)
        .apply {
            if (placeholder != 0) placeholder(placeholder)
            if (error != 0) error(error)
        }
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image with custom RequestOptions.
 */
fun ImageView.load(url: String?, options: RequestOptions) {
    if (url.isNullOrEmpty()) return
    Glide.with(context)
        .load(url)
        .apply(options)
        .into(this)
}

/**
 * Load image with builder pattern for full customization.
 */
inline fun ImageView.load(
    url: String?,
    builder: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable>
) {
    if (url.isNullOrEmpty()) return
    Glide.with(context)
        .load(url)
        .builder()
        .into(this)
}

// endregion

// ============================================================================
// region Circle Image Loading
// ============================================================================

/**
 * Load image as circle.
 */
fun ImageView.loadCircle(url: String?) {
    if (url.isNullOrEmpty()) return
    Glide.with(context)
        .load(url)
        .circleCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image as circle with placeholder.
 */
fun ImageView.loadCircle(
    url: String?,
    @DrawableRes placeholder: Int = 0,
    @DrawableRes error: Int = 0
) {
    if (url.isNullOrEmpty()) {
        if (error != 0) setImageResource(error)
        return
    }
    Glide.with(context)
        .load(url)
        .circleCrop()
        .apply {
            if (placeholder != 0) placeholder(placeholder)
            if (error != 0) error(error)
        }
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load drawable resource as circle.
 */
fun ImageView.loadCircle(@DrawableRes resId: Int) {
    Glide.with(context)
        .load(resId)
        .circleCrop()
        .into(this)
}

// endregion

// ============================================================================
// region Rounded Corner Image Loading
// ============================================================================

/**
 * Load image with rounded corners.
 *
 * @param url Image URL
 * @param radiusPx Corner radius in pixels
 */
fun ImageView.loadRounded(url: String?, radiusPx: Int) {
    if (url.isNullOrEmpty()) return
    Glide.with(context)
        .load(url)
        .transform(CenterCrop(), RoundedCorners(radiusPx))
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image with rounded corners and placeholder.
 */
fun ImageView.loadRounded(
    url: String?,
    radiusPx: Int,
    @DrawableRes placeholder: Int = 0,
    @DrawableRes error: Int = 0
) {
    if (url.isNullOrEmpty()) {
        if (error != 0) setImageResource(error)
        return
    }
    Glide.with(context)
        .load(url)
        .transform(CenterCrop(), RoundedCorners(radiusPx))
        .apply {
            if (placeholder != 0) placeholder(placeholder)
            if (error != 0) error(error)
        }
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load drawable resource with rounded corners.
 */
fun ImageView.loadRounded(@DrawableRes resId: Int, radiusPx: Int) {
    Glide.with(context)
        .load(resId)
        .transform(CenterCrop(), RoundedCorners(radiusPx))
        .into(this)
}

// endregion

// ============================================================================
// region Thumbnail Loading
// ============================================================================

/**
 * Load thumbnail with optimized settings.
 *
 * @param url Image URL or path
 * @param sizePx Thumbnail size in pixels
 */
fun ImageView.loadThumbnail(url: String?, sizePx: Int = 200) {
    if (url.isNullOrEmpty()) return
    val options = RequestOptions()
        .override(sizePx, sizePx)
        .format(DecodeFormat.PREFER_RGB_565)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .centerCrop()

    Glide.with(this)
        .load(url)
        .apply(options)
        .into(this)
}

/**
 * Load thumbnail with placeholder.
 */
fun ImageView.loadThumbnail(
    url: String?,
    sizePx: Int = 200,
    @DrawableRes placeholder: Int = 0
) {
    if (url.isNullOrEmpty()) return
    val options = RequestOptions()
        .override(sizePx, sizePx)
        .format(DecodeFormat.PREFER_RGB_565)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .centerCrop()
        .apply { if (placeholder != 0) placeholder(placeholder) }

    Glide.with(this)
        .load(url)
        .apply(options)
        .into(this)
}

// endregion

// ============================================================================
// region Animated Loading
// ============================================================================

/**
 * Load image with crossfade animation.
 */
fun ImageView.loadWithCrossFade(url: String?, durationMs: Int = 300) {
    if (url.isNullOrEmpty()) return
    Glide.with(context)
        .load(url)
        .transition(DrawableTransitionOptions.withCrossFade(durationMs))
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

// endregion

// ============================================================================
// region Bitmap Loading (for processing)
// ============================================================================

/**
 * Load image as Bitmap with callback.
 */
fun Context.loadBitmap(
    url: String?,
    onSuccess: (Bitmap) -> Unit,
    onError: ((Exception?) -> Unit)? = null
) {
    if (url.isNullOrEmpty()) {
        onError?.invoke(IllegalArgumentException("URL is null or empty"))
        return
    }

    Glide.with(this)
        .asBitmap()
        .load(url)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                onSuccess(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {}

            override fun onLoadFailed(errorDrawable: Drawable?) {
                onError?.invoke(null)
            }
        })
}

/**
 * Load image as Bitmap with size constraints.
 */
fun Context.loadBitmap(
    url: String?,
    width: Int,
    height: Int,
    onSuccess: (Bitmap) -> Unit,
    onError: ((Exception?) -> Unit)? = null
) {
    if (url.isNullOrEmpty()) {
        onError?.invoke(IllegalArgumentException("URL is null or empty"))
        return
    }

    Glide.with(this)
        .asBitmap()
        .load(url)
        .override(width, height)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                onSuccess(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {}

            override fun onLoadFailed(errorDrawable: Drawable?) {
                onError?.invoke(null)
            }
        })
}

// endregion

// ============================================================================
// region Cache Control
// ============================================================================

/**
 * Load image skipping memory cache.
 */
fun ImageView.loadSkipMemoryCache(url: String?) {
    if (url.isNullOrEmpty()) return
    Glide.with(context)
        .load(url)
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Load image skipping all caches (force refresh).
 */
fun ImageView.loadNoCache(url: String?) {
    if (url.isNullOrEmpty()) return
    Glide.with(context)
        .load(url)
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .into(this)
}

/**
 * Clear image and cancel any pending load.
 */
fun ImageView.clearGlide() {
    Glide.with(context).clear(this)
}

// endregion

// ============================================================================
// region Any-Type Loading
// ============================================================================

/**
 * Load image from any supported source with optional resize and transform.
 *
 * @param source  String (URL / relative asset path / content:// / file://), Uri, File, Bitmap, Drawable, Int (@DrawableRes), ByteArray
 * @param width   Override width in px (0 = no override)
 * @param height  Override height in px (0 = no override)
 * @param scaleType  How to scale the image: CENTER_CROP, FIT_CENTER, FIT_XY, CENTER_INSIDE, CIRCLE_CROP, or NONE
 * @param roundedCornersPx  Radius in px for rounded corners (ignored when scaleType is CIRCLE_CROP)
 * @param placeholder  Drawable res shown while loading (0 = none)
 * @param error  Drawable res shown on failure (0 = none)
 *
 * String detection:
 *   "http(s)://"                  → remote URL
 *   "content://" / "file://"      → parsed as Uri
 *   anything else                 → relative asset path (file:///android_asset/…)
 */
fun ImageView.loadAny(
    source: Any?,
    width: Int = 0,
    height: Int = 0,
    scaleType: GlideScaleType = GlideScaleType.NONE,
    roundedCornersPx: Int = 0,
    @DrawableRes placeholder: Int = 0,
    @DrawableRes error: Int = 0,
) {
    if (source == null) return

    val model: Any = when (source) {
        is String -> when {
            source.isEmpty() -> return
            source.startsWith("http://") || source.startsWith("https://") -> source
            source.startsWith("content://") || source.startsWith("file://") -> source.toUri()
            else -> "file:///android_asset/$source".toUri()
        }
        is File -> if (source.exists()) source else return
        else -> source
    }

    var request = Glide.with(context)
        .load(model)
        .diskCacheStrategy(if (model is Bitmap) DiskCacheStrategy.NONE else DiskCacheStrategy.ALL)

    if (width > 0 && height > 0) request = request.override(width, height)
    if (placeholder != 0) request = request.placeholder(placeholder)
    if (error != 0) request = request.error(error)
    request = when {
        scaleType == GlideScaleType.CIRCLE_CROP -> request.circleCrop()
        roundedCornersPx > 0 -> request.transform(CenterCrop(), RoundedCorners(roundedCornersPx))
        scaleType == GlideScaleType.CENTER_CROP -> request.centerCrop()
        scaleType == GlideScaleType.FIT_CENTER -> request.fitCenter()
        scaleType == GlideScaleType.CENTER_INSIDE -> request.centerInside()
        scaleType == GlideScaleType.FIT_XY -> request.fitCenter()
        else -> request
    }
    request.into(this)
}

enum class GlideScaleType { NONE, CENTER_CROP, FIT_CENTER, FIT_XY, CENTER_INSIDE, CIRCLE_CROP }

// endregion

// ============================================================================
// region Glide Manager Extensions
// ============================================================================

/**
 * Get Glide RequestManager from Context.
 */
val Context.glide: RequestManager
    get() = Glide.with(this)

/**
 * Get Glide RequestManager from ImageView.
 */
val ImageView.glide: RequestManager
    get() = Glide.with(this)

/**
 * Clear Glide disk cache (call from background thread).
 */
fun Context.clearGlideDiskCache() {
    Glide.get(this).clearDiskCache()
}

/**
 * Clear Glide memory cache (call from main thread).
 */
fun Context.clearGlideMemoryCache() {
    Glide.get(this).clearMemory()
}

// endregion

// ============================================================================
// region RequestOptions Builders
// ============================================================================

/**
 * Create RequestOptions for thumbnail loading.
 */
fun thumbnailOptions(sizePx: Int = 200): RequestOptions = RequestOptions()
    .override(sizePx, sizePx)
    .format(DecodeFormat.PREFER_RGB_565)
    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
    .centerCrop()

/**
 * Create RequestOptions for circle crop.
 */
fun circleOptions(): RequestOptions = RequestOptions()
    .circleCrop()
    .diskCacheStrategy(DiskCacheStrategy.ALL)

/**
 * Create RequestOptions for rounded corners.
 */
fun roundedOptions(radiusPx: Int): RequestOptions = RequestOptions()
    .transform(CenterCrop(), RoundedCorners(radiusPx))
    .diskCacheStrategy(DiskCacheStrategy.ALL)

/**
 * Create RequestOptions for center crop.
 */
fun centerCropOptions(): RequestOptions = RequestOptions()
    .centerCrop()
    .diskCacheStrategy(DiskCacheStrategy.ALL)

/**
 * Create RequestOptions for fit center.
 */
fun fitCenterOptions(): RequestOptions = RequestOptions()
    .fitCenter()
    .diskCacheStrategy(DiskCacheStrategy.ALL)

// endregion
