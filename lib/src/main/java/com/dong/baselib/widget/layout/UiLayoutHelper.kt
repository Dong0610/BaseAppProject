package com.dong.baselib.widget.layout

import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.graphics.*
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import com.dong.baselib.utils.isValidHexColor
import kotlin.math.min

/**
 * Helper class for UI layout drawing operations.
 * Centralizes common drawing logic for all Ui*Layout classes.
 */
class UiLayoutHelper(private val view: View) {
    companion object {
        // Stroke options
        const val STROKE_NONE = 0
        const val STROKE_TOP = 1
        const val STROKE_LEFT = 2
        const val STROKE_BOTTOM = 4
        const val STROKE_RIGHT = 8
        const val STROKE_HORIZONTAL = 5      // top + bottom
        const val STROKE_VERTICAL = 10       // left + right
        const val STROKE_TOP_LEFT = 3        // top + left
        const val STROKE_TOP_RIGHT = 9       // top + right
        const val STROKE_BOTTOM_LEFT = 6     // bottom + left
        const val STROKE_BOTTOM_RIGHT = 12   // bottom + right
        const val STROKE_ALL = 15
    }

    // Corner radius
    var cornerRadius = 0f
    var cornerTopLeft = 0f
    var cornerTopRight = 0f
    var cornerBottomLeft = 0f
    var cornerBottomRight = 0f

    // Background
    var isGradient = false
    var bgGradientOrientation = GradientOrientation.TOP_TO_BOTTOM
    var bgGradientType = GradientType.LINEAR
    var bgGradientCenterX = 0.5f  // 0.0 to 1.0, relative to view width
    var bgGradientCenterY = 0.5f  // 0.0 to 1.0, relative to view height
    var bgGradientRadius = 0f    // 0 means auto-calculate based on view size
    var bgColorLight = Color.TRANSPARENT
    var bgColorDark = Color.TRANSPARENT
    var bgColors: IntArray? = null
    var bgGradientPositions: FloatArray? = null

    // Stroke
    var stWidth = 0f
    var stColorLight = Color.TRANSPARENT
    var stColorDark = Color.TRANSPARENT
    var stColors: IntArray? = null
    var stGradientPositions: FloatArray? = null
    var strokeGradientOrientation = GradientOrientation.LEFT_TO_RIGHT
    var isDashed = false
    var dashSpace = 10f
    var strokeOption = STROKE_ALL
    var strokeCap = Paint.Cap.ROUND

    // Shadow
    var compatElevationDp = 0f
    var compatShadowColor = Color.argb(90, 0, 0, 0)
    var shadowRadiusPx = dp(12f)
    var shadowDxPx = 0f
    var shadowDyPx = dp(4f)

    // Dimension ratio and percentage sizing
    var dimenRatio: String? = null  // Format: "W,16:9" or "H,16:9" or "16:9" (default H)
    var dimenRatioSide = DimenRatioSide.HEIGHT  // Which side is calculated from ratio
    var dimenRatioValue = 0f  // The actual ratio value (width/height)
    var widthPercent = -1f  // 0-100, -1 means not set
    var heightPercent = -1f  // 0-100, -1 means not set
    var maxWidthPercent = -1f
    var maxHeightPercent = -1f
    var minWidthPercent = -1f
    var minHeightPercent = -1f

    // Shape type
    var shapeType = ShapeType.RECTANGLE
    var isCircle = false

    // Ripple effect
    var rippleEnabled = false
    var rippleColor = Color.argb(50, 0, 0, 0)
    var rippleBorderless = false

    // Pressed state
    var pressedBgColor = Color.TRANSPARENT
    var pressedScale = 1f

    // Overlay
    var overlayColor = Color.TRANSPARENT

    // Inner shadow
    var innerShadowEnabled = false
    var innerShadowColor = Color.argb(80, 0, 0, 0)
    var innerShadowRadius = 0f
    var innerShadowDx = 0f
    var innerShadowDy = 0f

    // Border style
    var borderStyle = BorderStyle.SOLID

    // Corner style
    var cornerStyle = CornerStyle.ROUND

    // Padding helpers
    var paddingAll = -1
    var paddingHorizontal = -1
    var paddingVertical = -1

    // Child gap (for LinearLayout types)
    var childGap = 0f

    // Divider
    var dividerColor = Color.TRANSPARENT
    var dividerSize = 0f
    var showDividers = 0

    enum class DimenRatioSide {
        WIDTH,   // Width is calculated from height * ratio
        HEIGHT   // Height is calculated from width / ratio
    }

    enum class ShapeType {
        RECTANGLE, OVAL, CIRCLE, ROUNDED
    }

    enum class BorderStyle {
        SOLID, DASHED, DOTTED, DOUBLES
    }

    enum class CornerStyle {
        ROUND,  // Regular rounded corners
        CUT,    // 45 degree cut corners (chamfer)
        BEVEL   // Beveled corners
    }

    enum class AspectRatio(val value: Float) {
        NONE(0f),
        SQUARE(1f),
        VIDEO_16_9(16f / 9f),
        VIDEO_4_3(4f / 3f),
        PHOTO_3_2(3f / 2f),
        PORTRAIT_9_16(9f / 16f),
        PORTRAIT_3_4(3f / 4f),
        GOLDEN(1.618f),
        WIDE_21_9(21f / 9f)
    }

    // Internal
    private val strokeRectF = RectF()
    private val clipPath = Path()
    private val tmpRectF = RectF()
    private val shadowRect = RectF()
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    val roundOutlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
        }
    }

    fun dp(value: Float): Float = value * view.resources.displayMetrics.density

    fun isDarkMode(): Boolean {
        return (view.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
              Configuration.UI_MODE_NIGHT_YES
    }

    // Read common attributes from TypedArray
    fun readCornerAttrs(
          ta: TypedArray,
          cornerRadiusAttr: Int,
          topLeftAttr: Int,
          topRightAttr: Int,
          bottomLeftAttr: Int,
          bottomRightAttr: Int
    ) {
        cornerRadius = ta.getDimension(cornerRadiusAttr, 0f)
        cornerTopLeft = ta.getDimension(topLeftAttr, 0f)
        cornerTopRight = ta.getDimension(topRightAttr, 0f)
        cornerBottomLeft = ta.getDimension(bottomLeftAttr, 0f)
        cornerBottomRight = ta.getDimension(bottomRightAttr, 0f)
    }

    fun readBackgroundAttrs(
          ta: TypedArray,
          isGradientAttr: Int,
          gradientStartAttr: Int,
          gradientCenterAttr: Int,
          gradientEndAttr: Int,
          colorLightAttr: Int,
          colorDarkAttr: Int,
          colorAllAttr: Int,
          orientationAttr: Int,
          gradientTypeAttr: Int = -1,
          gradientCenterXAttr: Int = -1,
          gradientCenterYAttr: Int = -1,
          gradientRadiusAttr: Int = -1,
          gradientColorsAttr: Int = -1,
          bgColorsAttr: Int = -1,
          bgPositionsAttr: Int = -1
    ) {
        isGradient = ta.getBoolean(isGradientAttr, false)
        bgColorLight = ta.getColor(colorLightAttr, Color.TRANSPARENT)
        bgColorDark = ta.getColor(colorDarkAttr, Color.TRANSPARENT)
        bgGradientOrientation = ta.getInt(orientationAttr, 0).toGradientOrientation()
        val bgColorAll = ta.getColor(colorAllAttr, Color.TRANSPARENT)
        if (bgColorAll != Color.TRANSPARENT) {
            bgColorLight = bgColorAll
            bgColorDark = bgColorAll
        }

        if (gradientTypeAttr != -1) {
            bgGradientType = ta.getInt(gradientTypeAttr, 0).toGradientType()
        }
        if (gradientCenterXAttr != -1) {
            bgGradientCenterX = ta.getFloat(gradientCenterXAttr, 0.5f)
        }
        if (gradientCenterYAttr != -1) {
            bgGradientCenterY = ta.getFloat(gradientCenterYAttr, 0.5f)
        }
        if (gradientRadiusAttr != -1) {
            bgGradientRadius = ta.getDimension(gradientRadiusAttr, 0f)
        }
        val gradientStart = ta.getColor(gradientStartAttr, Color.TRANSPARENT)
        val gradientCenter = ta.getColor(gradientCenterAttr, Color.TRANSPARENT)
        val gradientEnd = ta.getColor(gradientEndAttr, Color.TRANSPARENT)
        if (gradientStart != Color.TRANSPARENT || gradientEnd != Color.TRANSPARENT) {
            bgColors = if (gradientCenter != Color.TRANSPARENT) {
                intArrayOf(gradientStart, gradientCenter, gradientEnd)
            } else {
                intArrayOf(gradientStart, gradientEnd)
            }
        }
        // Priority 2: Read from bgGradientColors string (e.g., "#FF0000 #00FF00 #0000FF")
        if (gradientColorsAttr != -1) {
            ta.getString(gradientColorsAttr)?.split(" ")
                ?.mapNotNull { if (it.isValidHexColor()) it.toColorInt() else null }
                ?.toIntArray()
                ?.takeIf { it.size >= 2 }
                ?.let { bgColors = it }
        }
        // Priority 3 (highest): Read from bgColors integer-array reference
        if (bgColorsAttr != -1) {
            val resId = ta.getResourceId(bgColorsAttr, 0)
            if (resId != 0) {
                bgColors = view.resources.getIntArray(resId)
            }
        }
        // Read gradient positions (e.g., "0.0 0.7 1.0")
        if (bgPositionsAttr != -1) {
            ta.getString(bgPositionsAttr)?.split(" ")
                ?.mapNotNull { it.toFloatOrNull() }
                ?.toFloatArray()
                ?.takeIf { it.size >= 2 }
                ?.let { bgGradientPositions = it }
        }
    }

    fun readStrokeAttrs(
          ta: TypedArray,
          widthAttr: Int,
          colorLightAttr: Int,
          colorDarkAttr: Int,
          colorAllAttr: Int,
          dashedAttr: Int,
          dashSpaceAttr: Int,
          gradientAttr: Int,
          orientationAttr: Int,
          optionAttr: Int,
          capAttr: Int = -1,
          stColorsAttr: Int = -1,
          stPositionsAttr: Int = -1
    ) {
        stWidth = ta.getDimension(widthAttr, 0f)
        stColorLight = ta.getColor(colorLightAttr, Color.TRANSPARENT)
        stColorDark = ta.getColor(colorDarkAttr, Color.TRANSPARENT)
        val stColorAll = ta.getColor(colorAllAttr, Color.TRANSPARENT)
        if (stColorAll != Color.TRANSPARENT) {
            stColorLight = stColorAll
            stColorDark = stColorAll
        }
        isDashed = ta.getBoolean(dashedAttr, false)
        dashSpace = ta.getDimension(dashSpaceAttr, 10f)
        strokeGradientOrientation = ta.getInt(orientationAttr, 6).toStrokeOrientation()
        strokeOption = ta.getInt(optionAttr, STROKE_ALL)
        if (capAttr != -1) {
            strokeCap = ta.getInt(capAttr, 1).toStrokeCap()
        }

        ta.getString(gradientAttr)?.split(" ")
            ?.mapNotNull { if (it.isValidHexColor()) it.toColorInt() else null }
            ?.toIntArray()
            ?.takeIf { it.size >= 2 }
            ?.let { stColors = it }
        if (stColorsAttr != -1) {
            val resId = ta.getResourceId(stColorsAttr, 0)
            if (resId != 0) {
                stColors = view.resources.getIntArray(resId)
            }
        }
        // Read stroke gradient positions (e.g., "0.0 0.7 1.0")
        if (stPositionsAttr != -1) {
            ta.getString(stPositionsAttr)?.split(" ")
                ?.mapNotNull { it.toFloatOrNull() }
                ?.toFloatArray()
                ?.takeIf { it.size >= 2 }
                ?.let { stGradientPositions = it }
        }
    }

    private fun Int.toStrokeCap(): Paint.Cap = when (this) {
        0 -> Paint.Cap.BUTT
        1 -> Paint.Cap.ROUND
        2 -> Paint.Cap.SQUARE
        else -> Paint.Cap.ROUND
    }

    fun readShadowAttrs(
          ta: TypedArray,
          colorAttr: Int,
          radiusAttr: Int,
          dxAttr: Int,
          dyAttr: Int,
          elevationAttr: Int
    ) {
        compatShadowColor = ta.getColor(colorAttr, compatShadowColor)
        shadowRadiusPx = ta.getDimension(radiusAttr, shadowRadiusPx)
        shadowDxPx = ta.getDimension(dxAttr, shadowDxPx)
        shadowDyPx = ta.getDimension(dyAttr, shadowDyPx)
        val elevPx = ta.getDimension(elevationAttr, dp(compatElevationDp))
        compatElevationDp = elevPx / view.resources.displayMetrics.density
    }

    fun readDimensionAttrs(
          ta: TypedArray,
          dimenRatioAttr: Int = -1,
          widthPercentAttr: Int = -1,
          heightPercentAttr: Int = -1,
          maxWidthPercentAttr: Int = -1,
          maxHeightPercentAttr: Int = -1,
          minWidthPercentAttr: Int = -1,
          minHeightPercentAttr: Int = -1
    ) {
        if (dimenRatioAttr != -1) {
            ta.getString(dimenRatioAttr)?.let { parseDimenRatio(it) }
        }
        if (widthPercentAttr != -1) {
            val fraction = ta.getFraction(widthPercentAttr, 1, 1, -1f)
            if (fraction >= 0f) widthPercent = fraction * 100f
        }
        if (heightPercentAttr != -1) {
            val fraction = ta.getFraction(heightPercentAttr, 1, 1, -1f)
            if (fraction >= 0f) heightPercent = fraction * 100f
        }
        if (maxWidthPercentAttr != -1) {
            val fraction = ta.getFraction(maxWidthPercentAttr, 1, 1, -1f)
            if (fraction >= 0f) maxWidthPercent = fraction * 100f
        }
        if (maxHeightPercentAttr != -1) {
            val fraction = ta.getFraction(maxHeightPercentAttr, 1, 1, -1f)
            if (fraction >= 0f) maxHeightPercent = fraction * 100f
        }
        if (minWidthPercentAttr != -1) {
            val fraction = ta.getFraction(minWidthPercentAttr, 1, 1, -1f)
            if (fraction >= 0f) minWidthPercent = fraction * 100f
        }
        if (minHeightPercentAttr != -1) {
            val fraction = ta.getFraction(minHeightPercentAttr, 1, 1, -1f)
            if (fraction >= 0f) minHeightPercent = fraction * 100f
        }
    }

    fun readShapeAttrs(
          ta: TypedArray,
          shapeTypeAttr: Int = -1,
          isCircleAttr: Int = -1,
          aspectRatioAttr: Int = -1
    ) {
        if (shapeTypeAttr != -1) {
            shapeType = ta.getInt(shapeTypeAttr, 0).toShapeType()
        }
        if (isCircleAttr != -1) {
            isCircle = ta.getBoolean(isCircleAttr, false)
            if (isCircle) shapeType = ShapeType.CIRCLE
        }
        if (aspectRatioAttr != -1) {
            val ratio = ta.getInt(aspectRatioAttr, 0).toAspectRatio()
            if (ratio.value > 0f) {
                dimenRatioValue = ratio.value
                dimenRatioSide = DimenRatioSide.HEIGHT
            }
        }
    }

    fun readRippleAttrs(
          ta: TypedArray,
          rippleEnabledAttr: Int = -1,
          rippleColorAttr: Int = -1,
          rippleBorderlessAttr: Int = -1
    ) {
        if (rippleEnabledAttr != -1) {
            rippleEnabled = ta.getBoolean(rippleEnabledAttr, false)
        }
        if (rippleColorAttr != -1) {
            rippleColor = ta.getColor(rippleColorAttr, rippleColor)
        }
        if (rippleBorderlessAttr != -1) {
            rippleBorderless = ta.getBoolean(rippleBorderlessAttr, false)
        }
    }

    fun setupRipple() {
        if (!rippleEnabled) return
        val colorStateList = android.content.res.ColorStateList.valueOf(rippleColor)
        val mask = if (rippleBorderless) {
            null
        } else {
            val w = view.width.toFloat().coerceAtLeast(1f)
            val h = view.height.toFloat().coerceAtLeast(1f)
            val maskPath = Path()
            when {
                isCircle || shapeType == ShapeType.CIRCLE || shapeType == ShapeType.OVAL -> {
                    maskPath.addOval(RectF(0f, 0f, w, h), Path.Direction.CW)
                }
                cornerStyle == CornerStyle.CUT -> createCutCornerPath(maskPath, w, h)
                cornerStyle == CornerStyle.BEVEL -> createBevelCornerPath(maskPath, w, h)
                else -> {
                    val radii = getCornerRadii(w, h)
                    maskPath.addRoundRect(RectF(0f, 0f, w, h), radii, Path.Direction.CW)
                }
            }
            maskPath.close()
            val pathShape = android.graphics.drawable.shapes.PathShape(maskPath, w, h)
            android.graphics.drawable.ShapeDrawable(pathShape).apply {
                paint.color = Color.WHITE
                intrinsicWidth = w.toInt()
                intrinsicHeight = h.toInt()
            }
        }
        val rippleDrawable = android.graphics.drawable.RippleDrawable(colorStateList, null, mask)
        view.foreground = rippleDrawable
        view.isClickable = true
    }

    fun readPressedAttrs(
          ta: TypedArray,
          pressedBgColorAttr: Int = -1,
          pressedScaleAttr: Int = -1
    ) {
        if (pressedBgColorAttr != -1) {
            pressedBgColor = ta.getColor(pressedBgColorAttr, Color.TRANSPARENT)
        }
        if (pressedScaleAttr != -1) {
            pressedScale = ta.getFloat(pressedScaleAttr, 1f)
        }
    }

    fun readPaddingAttrs(
          ta: TypedArray,
          paddingAllAttr: Int = -1,
          paddingHorizontalAttr: Int = -1,
          paddingVerticalAttr: Int = -1
    ) {
        if (paddingAllAttr != -1) {
            paddingAll = ta.getDimensionPixelSize(paddingAllAttr, -1)
        }
        if (paddingHorizontalAttr != -1) {
            paddingHorizontal = ta.getDimensionPixelSize(paddingHorizontalAttr, -1)
        }
        if (paddingVerticalAttr != -1) {
            paddingVertical = ta.getDimensionPixelSize(paddingVerticalAttr, -1)
        }
    }

    fun readOverlayAttrs(
          ta: TypedArray,
          overlayColorAttr: Int = -1
    ) {
        if (overlayColorAttr != -1) {
            overlayColor = ta.getColor(overlayColorAttr, Color.TRANSPARENT)
        }
    }

    fun readInnerShadowAttrs(
          ta: TypedArray,
          enabledAttr: Int = -1,
          colorAttr: Int = -1,
          radiusAttr: Int = -1
    ) {
        if (enabledAttr != -1) {
            innerShadowEnabled = ta.getBoolean(enabledAttr, false)
        }
        if (colorAttr != -1) {
            innerShadowColor = ta.getColor(colorAttr, innerShadowColor)
        }
        if (radiusAttr != -1) {
            innerShadowRadius = ta.getDimension(radiusAttr, 0f)
        }
    }

    fun readBorderStyleAttr(ta: TypedArray, borderStyleAttr: Int = -1) {
        if (borderStyleAttr != -1) {
            borderStyle = ta.getInt(borderStyleAttr, 0).toBorderStyle()
            when (borderStyle) {
                BorderStyle.DASHED -> isDashed = true
                BorderStyle.DOTTED -> {
                    isDashed = true
                    dashSpace = dp(2f)
                }
                BorderStyle.DOUBLES -> {
                    // Double border will be handled in drawStroke
                }
                else -> {}
            }
        }
    }

    fun readCornerStyleAttr(ta: TypedArray, cornerStyleAttr: Int = -1) {
        if (cornerStyleAttr != -1) {
            cornerStyle = ta.getInt(cornerStyleAttr, 0).toCornerStyle()
        }
    }

    private fun Int.toCornerStyle() = CornerStyle.entries.getOrElse(this) { CornerStyle.ROUND }

    fun readChildGapAttr(ta: TypedArray, childGapAttr: Int = -1) {
        if (childGapAttr != -1) {
            childGap = ta.getDimension(childGapAttr, 0f)
        }
    }

    private fun Int.toShapeType() = ShapeType.entries.getOrElse(this) { ShapeType.RECTANGLE }
    private fun Int.toBorderStyle() = BorderStyle.entries.getOrElse(this) { BorderStyle.SOLID }
    private fun Int.toAspectRatio() = AspectRatio.entries.getOrElse(this) { AspectRatio.NONE }

    /** Apply padding helper values to the view */
    fun applyPadding() {
        val left: Int
        val top: Int
        val right: Int
        val bottom: Int

        if (paddingAll >= 0) {
            left = paddingAll
            top = paddingAll
            right = paddingAll
            bottom = paddingAll
        } else {
            left = if (paddingHorizontal >= 0) paddingHorizontal else view.paddingLeft
            right = if (paddingHorizontal >= 0) paddingHorizontal else view.paddingRight
            top = if (paddingVertical >= 0) paddingVertical else view.paddingTop
            bottom = if (paddingVertical >= 0) paddingVertical else view.paddingBottom
        }

        view.setPadding(left, top, right, bottom)
    }

    fun parseDimenRatio(ratio: String) {
        dimenRatio = ratio
        val trimmed = ratio.trim()
        if (trimmed.isEmpty()) {
            dimenRatioValue = 0f
            return
        }

        var ratioStr = trimmed
        // Check for side prefix: "W,16:9" or "H,16:9"
        when {
            trimmed.startsWith("W,", ignoreCase = true) || trimmed.startsWith("w,") -> {
                dimenRatioSide = DimenRatioSide.WIDTH
                ratioStr = trimmed.substring(2)
            }
            trimmed.startsWith("H,", ignoreCase = true) || trimmed.startsWith("h,") -> {
                dimenRatioSide = DimenRatioSide.HEIGHT
                ratioStr = trimmed.substring(2)
            }
            else -> {
                dimenRatioSide = DimenRatioSide.HEIGHT  // Default
            }
        }

        // Parse ratio value: "16:9" or "1.78"
        dimenRatioValue = if (ratioStr.contains(":")) {
            val parts = ratioStr.split(":")
            if (parts.size == 2) {
                val w = parts[0].trim().toFloatOrNull() ?: 0f
                val h = parts[1].trim().toFloatOrNull() ?: 0f
                if (h > 0f) w / h else 0f
            } else 0f
        } else {
            ratioStr.toFloatOrNull() ?: 0f
        }
    }

    fun measureWithConstraints(
          widthMeasureSpec: Int,
          heightMeasureSpec: Int,
          parentWidth: Int,
          parentHeight: Int
    ): Pair<Int, Int> {
        var measuredWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        var measuredHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        // Apply percentage width
        if (widthPercent >= 0f && parentWidth > 0) {
            measuredWidth = (parentWidth * widthPercent / 100f).toInt()
        }
        // Apply percentage height
        if (heightPercent >= 0f && parentHeight > 0) {
            measuredHeight = (parentHeight * heightPercent / 100f).toInt()
        }

        // Apply min/max percentage constraints
        if (minWidthPercent >= 0f && parentWidth > 0) {
            val minW = (parentWidth * minWidthPercent / 100f).toInt()
            if (measuredWidth < minW) measuredWidth = minW
        }
        if (maxWidthPercent >= 0f && parentWidth > 0) {
            val maxW = (parentWidth * maxWidthPercent / 100f).toInt()
            if (measuredWidth > maxW) measuredWidth = maxW
        }
        if (minHeightPercent >= 0f && parentHeight > 0) {
            val minH = (parentHeight * minHeightPercent / 100f).toInt()
            if (measuredHeight < minH) measuredHeight = minH
        }
        if (maxHeightPercent >= 0f && parentHeight > 0) {
            val maxH = (parentHeight * maxHeightPercent / 100f).toInt()
            if (measuredHeight > maxH) measuredHeight = maxH
        }

        // Apply dimension ratio
        if (dimenRatioValue > 0f) {
            when (dimenRatioSide) {
                DimenRatioSide.HEIGHT -> {
                    // Height is calculated from width
                    if (measuredWidth > 0) {
                        measuredHeight = (measuredWidth / dimenRatioValue).toInt()
                    }
                }
                DimenRatioSide.WIDTH -> {
                    // Width is calculated from height
                    if (measuredHeight > 0) {
                        measuredWidth = (measuredHeight * dimenRatioValue).toInt()
                    }
                }
            }
        }

        return Pair(measuredWidth, measuredHeight)
    }

    fun shouldApplyCustomMeasure(): Boolean {
        return dimenRatioValue > 0f ||
              widthPercent >= 0f ||
              heightPercent >= 0f ||
              maxWidthPercent >= 0f ||
              maxHeightPercent >= 0f ||
              minWidthPercent >= 0f ||
              minHeightPercent >= 0f
    }

    fun setupShadow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.outlineProvider = roundOutlineProvider
            view.clipToOutline = false
            ViewCompat.setElevation(view, dp(compatElevationDp))
            applyPlatformShadowColor()
        } else {
            view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            updateCompatShadow()
        }
    }

    fun updateCompatShadow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) return
        if (shadowRadiusPx <= 0f) shadowRadiusPx = dp(compatElevationDp * 1.5f)
        if (shadowDyPx == 0f) shadowDyPx = dp(compatElevationDp * 0.8f)
        shadowPaint.setShadowLayer(shadowRadiusPx, shadowDxPx, shadowDyPx, compatShadowColor)
        view.invalidate()
    }

    fun applyPlatformShadowColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.outlineSpotShadowColor = compatShadowColor
            view.outlineAmbientShadowColor = compatShadowColor
        }
    }

    fun onSizeChanged(w: Int, h: Int) {
        updateClipPath(w, h)
        view.invalidateOutline()
        if (w > 0 && h > 0) {
            val padding = shadowRadiusPx.coerceAtLeast(dp(compatElevationDp * 1.5f))
            shadowRect.set(padding, padding, w.toFloat() - padding, h.toFloat() - padding)
        }
    }

    fun updateClipPath(w: Int, h: Int) {
        clipPath.reset()
        if (w <= 0 || h <= 0) return

        when (cornerStyle) {
            CornerStyle.ROUND -> {
                tmpRectF.set(0f, 0f, w.toFloat(), h.toFloat())
                val radii = getCornerRadii(w.toFloat(), h.toFloat())
                clipPath.addRoundRect(tmpRectF, radii, Path.Direction.CW)
            }
            CornerStyle.CUT -> {
                createCutCornerPath(clipPath, w.toFloat(), h.toFloat())
            }
            CornerStyle.BEVEL -> {
                createBevelCornerPath(clipPath, w.toFloat(), h.toFloat())
            }
        }
        clipPath.close()
    }

    private fun createCutCornerPath(path: Path, w: Float, h: Float) {
        val maxRadius = min(w / 2f, h / 2f)
        val tl = if (cornerTopLeft > 0f) min(cornerTopLeft, maxRadius) else min(cornerRadius, maxRadius)
        val tr = if (cornerTopRight > 0f) min(cornerTopRight, maxRadius) else min(cornerRadius, maxRadius)
        val br = if (cornerBottomRight > 0f) min(cornerBottomRight, maxRadius) else min(cornerRadius, maxRadius)
        val bl = if (cornerBottomLeft > 0f) min(cornerBottomLeft, maxRadius) else min(cornerRadius, maxRadius)

        // Top-left corner
        if (tl > 0f) {
            path.moveTo(tl, 0f)
        } else {
            path.moveTo(0f, 0f)
        }

        // Top edge to top-right
        if (tr > 0f) {
            path.lineTo(w - tr, 0f)
            path.lineTo(w, tr)  // Cut
        } else {
            path.lineTo(w, 0f)
        }

        // Right edge to bottom-right
        if (br > 0f) {
            path.lineTo(w, h - br)
            path.lineTo(w - br, h)  // Cut
        } else {
            path.lineTo(w, h)
        }

        // Bottom edge to bottom-left
        if (bl > 0f) {
            path.lineTo(bl, h)
            path.lineTo(0f, h - bl)  // Cut
        } else {
            path.lineTo(0f, h)
        }

        // Left edge to top-left
        if (tl > 0f) {
            path.lineTo(0f, tl)
            path.lineTo(tl, 0f)  // Cut
        } else {
            path.lineTo(0f, 0f)
        }
    }

    private fun createBevelCornerPath(path: Path, w: Float, h: Float) {
        val maxRadius = min(w / 2f, h / 2f)
        val tl = if (cornerTopLeft > 0f) min(cornerTopLeft, maxRadius) else min(cornerRadius, maxRadius)
        val tr = if (cornerTopRight > 0f) min(cornerTopRight, maxRadius) else min(cornerRadius, maxRadius)
        val br = if (cornerBottomRight > 0f) min(cornerBottomRight, maxRadius) else min(cornerRadius, maxRadius)
        val bl = if (cornerBottomLeft > 0f) min(cornerBottomLeft, maxRadius) else min(cornerRadius, maxRadius)

        // Bevel is similar to cut but with smaller chamfer
        val bevelFactor = 0.5f

        // Top-left corner
        if (tl > 0f) {
            path.moveTo(tl * bevelFactor, 0f)
        } else {
            path.moveTo(0f, 0f)
        }

        // Top edge to top-right
        if (tr > 0f) {
            path.lineTo(w - tr * bevelFactor, 0f)
            path.lineTo(w, tr * bevelFactor)  // Bevel
        } else {
            path.lineTo(w, 0f)
        }

        // Right edge to bottom-right
        if (br > 0f) {
            path.lineTo(w, h - br * bevelFactor)
            path.lineTo(w - br * bevelFactor, h)  // Bevel
        } else {
            path.lineTo(w, h)
        }

        // Bottom edge to bottom-left
        if (bl > 0f) {
            path.lineTo(bl * bevelFactor, h)
            path.lineTo(0f, h - bl * bevelFactor)  // Bevel
        } else {
            path.lineTo(0f, h)
        }

        // Left edge to top-left
        if (tl > 0f) {
            path.lineTo(0f, tl * bevelFactor)
            path.lineTo(tl * bevelFactor, 0f)  // Bevel
        } else {
            path.lineTo(0f, 0f)
        }
    }

    fun getClipPath(): Path = clipPath

    private fun hasIndividualCorners(): Boolean =
        cornerTopLeft > 0f || cornerTopRight > 0f || cornerBottomLeft > 0f || cornerBottomRight > 0f

    fun getCornerRadii(w: Float, h: Float): FloatArray {
        val maxRadius = min(w / 2f, h / 2f)

        // Circle or oval shapes use maximum radius
        if (isCircle || shapeType == ShapeType.CIRCLE || shapeType == ShapeType.OVAL) {
            return floatArrayOf(maxRadius, maxRadius, maxRadius, maxRadius, maxRadius, maxRadius, maxRadius, maxRadius)
        }

        return if (hasIndividualCorners()) {
            floatArrayOf(
                min(cornerTopLeft, maxRadius), min(cornerTopLeft, maxRadius),
                min(cornerTopRight, maxRadius), min(cornerTopRight, maxRadius),
                min(cornerBottomRight, maxRadius), min(cornerBottomRight, maxRadius),
                min(cornerBottomLeft, maxRadius), min(cornerBottomLeft, maxRadius)
            )
        } else {
            val r = min(cornerRadius, maxRadius)
            floatArrayOf(r, r, r, r, r, r, r, r)
        }
    }

    fun getStrokeCornerRadii(w: Float, h: Float, inset: Float): FloatArray {
        val maxRadius = min(w / 2f, h / 2f)
        return if (hasIndividualCorners()) {
            floatArrayOf(
                (min(cornerTopLeft, maxRadius) - inset).coerceAtLeast(0f),
                (min(cornerTopLeft, maxRadius) - inset).coerceAtLeast(0f),
                (min(cornerTopRight, maxRadius) - inset).coerceAtLeast(0f),
                (min(cornerTopRight, maxRadius) - inset).coerceAtLeast(0f),
                (min(cornerBottomRight, maxRadius) - inset).coerceAtLeast(0f),
                (min(cornerBottomRight, maxRadius) - inset).coerceAtLeast(0f),
                (min(cornerBottomLeft, maxRadius) - inset).coerceAtLeast(0f),
                (min(cornerBottomLeft, maxRadius) - inset).coerceAtLeast(0f)
            )
        } else {
            val r = (min(cornerRadius, maxRadius) - inset).coerceAtLeast(0f)
            floatArrayOf(r, r, r, r, r, r, r, r)
        }
    }

    fun drawBackground(canvas: Canvas, w: Float, h: Float) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            val gradientColors = bgColors
            if (isGradient) {
                if (gradientColors != null && gradientColors.size >= 2) {
                    val positions = bgGradientPositions?.takeIf { it.size == gradientColors.size }
                    shader = when (bgGradientType) {
                        GradientType.LINEAR -> {
                            val (x0, y0, x1, y1) = bgGradientOrientation.toCoordinates(w, h)
                            LinearGradient(
                                x0,
                                y0,
                                x1,
                                y1,
                                gradientColors,
                                positions,
                                Shader.TileMode.CLAMP
                            )
                        }
                        GradientType.RADIAL -> {
                            val centerX = w * bgGradientCenterX
                            val centerY = h * bgGradientCenterY
                            val radius = if (bgGradientRadius > 0f) bgGradientRadius else {
                                kotlin.math.max(w, h) / 2f
                            }
                            RadialGradient(
                                centerX,
                                centerY,
                                radius,
                                gradientColors,
                                positions,
                                Shader.TileMode.CLAMP
                            )
                        }
                        GradientType.SWEEP -> {
                            val centerX = w * bgGradientCenterX
                            val centerY = h * bgGradientCenterY
                            SweepGradient(centerX, centerY, gradientColors, positions)
                        }
                    }
                } else {
                    color = if (isDarkMode()) bgColorDark else bgColorLight
                }
            } else {
                color = if (isDarkMode()) bgColorDark else bgColorLight
            }
        }

        // Use the clip path which already handles corner style
        val bgPath = Path()
        when (cornerStyle) {
            CornerStyle.ROUND -> {
                val radii = getCornerRadii(w, h)
                bgPath.addRoundRect(RectF(0f, 0f, w, h), radii, Path.Direction.CW)
            }
            CornerStyle.CUT -> createCutCornerPath(bgPath, w, h)
            CornerStyle.BEVEL -> createBevelCornerPath(bgPath, w, h)
        }
        canvas.drawPath(bgPath, bgPaint)
    }

    fun drawOverlay(canvas: Canvas, w: Float, h: Float) {
        if (overlayColor == Color.TRANSPARENT) return
        val radii = getCornerRadii(w, h)
        val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = overlayColor
        }
        val overlayPath = Path().apply {
            if (shapeType == ShapeType.OVAL || shapeType == ShapeType.CIRCLE || isCircle) {
                addOval(RectF(0f, 0f, w, h), Path.Direction.CW)
            } else {
                addRoundRect(RectF(0f, 0f, w, h), radii, Path.Direction.CW)
            }
        }
        canvas.drawPath(overlayPath, overlayPaint)
    }

    fun drawInnerShadow(canvas: Canvas, w: Float, h: Float) {
        if (!innerShadowEnabled || innerShadowRadius <= 0f) return
        val radii = getCornerRadii(w, h)
        val innerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = innerShadowRadius * 2
            color = innerShadowColor
            maskFilter = android.graphics.BlurMaskFilter(innerShadowRadius, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        val inset = innerShadowRadius
        val innerPath = Path().apply {
            if (shapeType == ShapeType.OVAL || shapeType == ShapeType.CIRCLE || isCircle) {
                addOval(RectF(inset, inset, w - inset, h - inset), Path.Direction.CW)
            } else {
                addRoundRect(RectF(inset, inset, w - inset, h - inset), radii, Path.Direction.CW)
            }
        }
        canvas.save()
        canvas.clipPath(getClipPath())
        canvas.translate(innerShadowDx, innerShadowDy)
        canvas.drawPath(innerPath, innerShadowPaint)
        canvas.restore()
    }

    fun drawStroke(canvas: Canvas, w: Float, h: Float) {
        if (stWidth <= 0f || strokeOption == STROKE_NONE) return

        // Handle double border style
        if (borderStyle == BorderStyle.DOUBLES) {
            drawDoubleBorder(canvas, w, h)
            return
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stWidth
            strokeJoin = Paint.Join.ROUND
            strokeCap = this@UiLayoutHelper.strokeCap
            if (isDashed) {
                pathEffect = DashPathEffect(floatArrayOf(dashSpace, dashSpace), 0f)
            }
            val gradientColors = stColors
            if (gradientColors != null && gradientColors.size >= 2) {
                val (x0, y0, x1, y1) = strokeGradientOrientation.toCoordinates(w, h)
                val positions = stGradientPositions?.takeIf { it.size == gradientColors.size }
                shader = LinearGradient(x0, y0, x1, y1, gradientColors, positions, Shader.TileMode.CLAMP)
            } else {
                color = if (isDarkMode()) stColorDark else stColorLight
            }
        }
        val inset = stWidth / 2

        if (strokeOption == STROKE_ALL) {
            strokeRectF.set(inset, inset, w - inset, h - inset)
            val strokeRadii = getStrokeCornerRadii(w, h, inset)
            canvas.drawPath(Path().apply {
                addRoundRect(
                    strokeRectF,
                    strokeRadii,
                    Path.Direction.CW
                )
            }, strokePaint)
        } else {
            drawPartialStroke(canvas, w, h, inset, strokePaint)
        }
    }

    private fun drawDoubleBorder(canvas: Canvas, w: Float, h: Float) {
        val singleWidth = stWidth / 3f  // Each line is 1/3 of total, gap is 1/3
        val gap = singleWidth

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = singleWidth
            strokeJoin = Paint.Join.ROUND
            strokeCap = this@UiLayoutHelper.strokeCap
            val gradientColors = stColors
            if (gradientColors != null && gradientColors.size >= 2) {
                val (x0, y0, x1, y1) = strokeGradientOrientation.toCoordinates(w, h)
                val positions = stGradientPositions?.takeIf { it.size == gradientColors.size }
                shader = LinearGradient(x0, y0, x1, y1, gradientColors, positions, Shader.TileMode.CLAMP)
            } else {
                color = if (isDarkMode()) stColorDark else stColorLight
            }
        }

        // Outer stroke
        val outerInset = singleWidth / 2
        strokeRectF.set(outerInset, outerInset, w - outerInset, h - outerInset)
        val outerRadii = getStrokeCornerRadii(w, h, outerInset)
        canvas.drawPath(Path().apply {
            addRoundRect(strokeRectF, outerRadii, Path.Direction.CW)
        }, strokePaint)

        // Inner stroke
        val innerInset = singleWidth + gap + singleWidth / 2
        strokeRectF.set(innerInset, innerInset, w - innerInset, h - innerInset)
        val innerRadii = getStrokeCornerRadii(w, h, innerInset)
        canvas.drawPath(Path().apply {
            addRoundRect(strokeRectF, innerRadii, Path.Direction.CW)
        }, strokePaint)
    }

    private fun drawPartialStroke(canvas: Canvas, w: Float, h: Float, inset: Float, paint: Paint) {
        val strokePath = Path()
        val radii = getStrokeCornerRadii(w, h, inset)
        val topLeftR = radii[0]
        val topRightR = radii[2]
        val bottomRightR = radii[4]
        val bottomLeftR = radii[6]

        if (strokeOption and STROKE_TOP != 0) {
            strokePath.moveTo(inset + topLeftR, inset)
            strokePath.lineTo(w - inset - topRightR, inset)
            if (strokeOption and STROKE_RIGHT != 0 && topRightR > 0) {
                strokePath.arcTo(
                    w - inset - topRightR * 2,
                    inset,
                    w - inset,
                    inset + topRightR * 2,
                    -90f,
                    90f,
                    false
                )
            }
        }
        if (strokeOption and STROKE_RIGHT != 0) {
            if (strokeOption and STROKE_TOP == 0) strokePath.moveTo(w - inset, inset + topRightR)
            strokePath.lineTo(w - inset, h - inset - bottomRightR)
            if (strokeOption and STROKE_BOTTOM != 0 && bottomRightR > 0) {
                strokePath.arcTo(
                    w - inset - bottomRightR * 2,
                    h - inset - bottomRightR * 2,
                    w - inset,
                    h - inset,
                    0f,
                    90f,
                    false
                )
            }
        }
        if (strokeOption and STROKE_BOTTOM != 0) {
            if (strokeOption and STROKE_RIGHT == 0) strokePath.moveTo(
                w - inset - bottomRightR,
                h - inset
            )
            strokePath.lineTo(inset + bottomLeftR, h - inset)
            if (strokeOption and STROKE_LEFT != 0 && bottomLeftR > 0) {
                strokePath.arcTo(
                    inset,
                    h - inset - bottomLeftR * 2,
                    inset + bottomLeftR * 2,
                    h - inset,
                    90f,
                    90f,
                    false
                )
            }
        }
        if (strokeOption and STROKE_LEFT != 0) {
            if (strokeOption and STROKE_BOTTOM == 0) strokePath.moveTo(
                inset,
                h - inset - bottomLeftR
            )
            strokePath.lineTo(inset, inset + topLeftR)
            if (strokeOption and STROKE_TOP != 0 && topLeftR > 0) {
                strokePath.arcTo(
                    inset,
                    inset,
                    inset + topLeftR * 2,
                    inset + topLeftR * 2,
                    180f,
                    90f,
                    false
                )
            }
        }
        canvas.drawPath(strokePath, paint)
    }

    // Gradient type
    enum class GradientType {
        LINEAR, RADIAL, SWEEP
    }

    // Gradient orientation
    enum class GradientOrientation {
        TOP_TO_BOTTOM, TR_BL, RIGHT_TO_LEFT, BR_TL,
        BOTTOM_TO_TOP, BL_TR, LEFT_TO_RIGHT, TL_BR;

        fun toCoordinates(w: Float, h: Float) = when (this) {
            TOP_TO_BOTTOM -> Quad(0f, 0f, 0f, h)
            BOTTOM_TO_TOP -> Quad(0f, h, 0f, 0f)
            LEFT_TO_RIGHT -> Quad(0f, 0f, w, 0f)
            RIGHT_TO_LEFT -> Quad(w, 0f, 0f, 0f)
            TL_BR -> Quad(0f, 0f, w, h)
            TR_BL -> Quad(w, 0f, 0f, h)
            BL_TR -> Quad(0f, h, w, 0f)
            BR_TL -> Quad(w, h, 0f, 0f)
        }
    }

    data class Quad(val x0: Float, val y0: Float, val x1: Float, val y1: Float)

    private fun Int.toGradientOrientation() =
        GradientOrientation.entries.getOrElse(this) { GradientOrientation.TOP_TO_BOTTOM }

    private fun Int.toStrokeOrientation() =
        GradientOrientation.entries.getOrElse(this) { GradientOrientation.LEFT_TO_RIGHT }

    private fun Int.toGradientType() = GradientType.entries.getOrElse(this) { GradientType.LINEAR }
    // ==================== Programmatic Setters ====================
    /** Set background gradient colors */
    fun setBgColors(vararg colors: Int): UiLayoutHelper {
        bgColors = colors
        view.invalidate()
        return this
    }

    /** Set background gradient colors with orientation */
    fun setBgColors(orientation: GradientOrientation, vararg colors: Int): UiLayoutHelper {
        bgColors = colors
        bgGradientOrientation = orientation
        view.invalidate()
        return this
    }

    /** Set stroke gradient colors */
    fun setStColors(vararg colors: Int): UiLayoutHelper {
        stColors = colors
        view.invalidate()
        return this
    }

    /** Set stroke gradient colors with orientation */
    fun setStColors(orientation: GradientOrientation, vararg colors: Int): UiLayoutHelper {
        stColors = colors
        strokeGradientOrientation = orientation
        view.invalidate()
        return this
    }

    /** Set solid background color (light & dark) */
    fun setBgColor(color: Int): UiLayoutHelper {
        bgColorLight = color
        bgColorDark = color
        bgColors = null
        view.invalidate()
        return this
    }

    /** Set solid stroke color (light & dark) */
    fun setStColor(color: Int): UiLayoutHelper {
        stColorLight = color
        stColorDark = color
        stColors = null
        view.invalidate()
        return this
    }

    /** Set corner radius for all corners */
    fun setCorner(radius: Float): UiLayoutHelper {
        cornerRadius = radius
        cornerTopLeft = 0f
        cornerTopRight = 0f
        cornerBottomLeft = 0f
        cornerBottomRight = 0f
        view.invalidateOutline()
        view.invalidate()
        return this
    }

    /** Set individual corner radii (tl, tr, br, bl) */
    fun setCorners(tl: Float, tr: Float, br: Float, bl: Float): UiLayoutHelper {
        cornerRadius = 0f
        cornerTopLeft = tl
        cornerTopRight = tr
        cornerBottomRight = br
        cornerBottomLeft = bl
        view.invalidateOutline()
        view.invalidate()
        return this
    }

    /** Set stroke width */
    fun setStWidth(width: Float): UiLayoutHelper {
        stWidth = width
        view.invalidate()
        return this
    }

    /** Set dimension ratio (e.g., "16:9", "W,16:9", "H,4:3") */
    fun setDimenRatio(ratio: String?): UiLayoutHelper {
        if (ratio == null) {
            dimenRatio = null
            dimenRatioValue = 0f
        } else {
            parseDimenRatio(ratio)
        }
        view.requestLayout()
        return this
    }

    /** Set dimension ratio with explicit side */
    fun setDimenRatio(ratio: Float, side: DimenRatioSide = DimenRatioSide.HEIGHT): UiLayoutHelper {
        dimenRatioValue = ratio
        dimenRatioSide = side
        dimenRatio = if (side == DimenRatioSide.WIDTH) "W,$ratio" else "H,$ratio"
        view.requestLayout()
        return this
    }

    /** Set width as percentage of parent (0-100) */
    fun setWidthPercent(percent: Float): UiLayoutHelper {
        widthPercent = percent
        view.requestLayout()
        return this
    }

    /** Set height as percentage of parent (0-100) */
    fun setHeightPercent(percent: Float): UiLayoutHelper {
        heightPercent = percent
        view.requestLayout()
        return this
    }

    /** Set both width and height as percentage of parent */
    fun setSizePercent(wPercent: Float, hPercent: Float): UiLayoutHelper {
        widthPercent = wPercent
        heightPercent = hPercent
        view.requestLayout()
        return this
    }

    /** Set max width as percentage of parent (0-100) */
    fun setMaxWidthPercent(percent: Float): UiLayoutHelper {
        maxWidthPercent = percent
        view.requestLayout()
        return this
    }

    /** Set max height as percentage of parent (0-100) */
    fun setMaxHeightPercent(percent: Float): UiLayoutHelper {
        maxHeightPercent = percent
        view.requestLayout()
        return this
    }

    /** Set min width as percentage of parent (0-100) */
    fun setMinWidthPercent(percent: Float): UiLayoutHelper {
        minWidthPercent = percent
        view.requestLayout()
        return this
    }

    /** Set min height as percentage of parent (0-100) */
    fun setMinHeightPercent(percent: Float): UiLayoutHelper {
        minHeightPercent = percent
        view.requestLayout()
        return this
    }
}
