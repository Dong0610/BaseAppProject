package com.dong.baselib.widget.layout

import android.graphics.Color
import android.view.View

/**
 * Interface for UI layout views with common styling capabilities.
 */
interface IUiLayout {
    val helper: UiLayoutHelper

    fun invalidateView()
    fun invalidateViewOutline()
    fun updateViewClipPath()
}

// Extension functions for fluent API - works with any IUiLayout implementation
@Suppress("UNCHECKED_CAST")
fun <T> T.cornerRadius(radius: Float): T where T : IUiLayout, T : View = apply {
    helper.cornerRadius = radius
    helper.cornerTopLeft = 0f
    helper.cornerTopRight = 0f
    helper.cornerBottomLeft = 0f
    helper.cornerBottomRight = 0f
    updateViewClipPath()
    invalidateViewOutline()
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.cornerRadii(
      topLeft: Float = 0f,
      topRight: Float = 0f,
      bottomRight: Float = 0f,
      bottomLeft: Float = 0f
): T where T : IUiLayout, T : View = apply {
    helper.cornerTopLeft = topLeft
    helper.cornerTopRight = topRight
    helper.cornerBottomRight = bottomRight
    helper.cornerBottomLeft = bottomLeft
    updateViewClipPath()
    invalidateViewOutline()
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.cornerTopLeft(radius: Float): T where T : IUiLayout, T : View = apply {
    helper.cornerTopLeft = radius
    updateViewClipPath()
    invalidateViewOutline()
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.cornerTopRight(radius: Float): T where T : IUiLayout, T : View = apply {
    helper.cornerTopRight = radius
    updateViewClipPath()
    invalidateViewOutline()
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.cornerBottomLeft(radius: Float): T where T : IUiLayout, T : View = apply {
    helper.cornerBottomLeft = radius
    updateViewClipPath()
    invalidateViewOutline()
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.cornerBottomRight(radius: Float): T where T : IUiLayout, T : View = apply {
    helper.cornerBottomRight = radius
    updateViewClipPath()
    invalidateViewOutline()
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.rounded(radius: Float): T where T : IUiLayout, T : View = cornerRadius(radius)

// Background
@Suppress("UNCHECKED_CAST")
fun <T> T.backgroundLight(color: Int): T where T : IUiLayout, T : View = apply {
    helper.bgColors = null
    helper.bgColorLight = color
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.backgroundDark(color: Int): T where T : IUiLayout, T : View = apply {
    helper.bgColors = null
    helper.bgColorDark = color
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.backgroundAll(color: Int): T where T : IUiLayout, T : View = apply {
    helper.bgColors = null
    helper.bgColorLight = color
    helper.bgColorDark = color
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.solidBackground(light: Int, dark: Int = light): T where T : IUiLayout, T : View = apply {
    helper.bgColors = null
    helper.bgColorLight = light
    helper.bgColorDark = dark
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.isBackgroundGradient(isBgGradient: Boolean = true): T where T : IUiLayout, T : View =
    apply {
        helper.isGradient = isBgGradient
        invalidateView()
    }

@Suppress("UNCHECKED_CAST")
fun <T> T.backgroundGradient(
      start: Int,
      end: Int,
      center: Int = Color.TRANSPARENT
): T where T : IUiLayout, T : View = apply {
    helper.bgColors = if (center != Color.TRANSPARENT) {
        intArrayOf(start, center, end)
    } else {
        intArrayOf(start, end)
    }
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.backgroundGradient(vararg colors: Int): T where T : IUiLayout, T : View = apply {
    helper.bgColors = if (colors.isNotEmpty()) colors else null
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.backgroundOrientation(orientation: UiLayoutHelper.GradientOrientation): T where T : IUiLayout, T : View =
    apply {
        helper.isGradient = true
        helper.bgGradientOrientation = orientation
        invalidateView()
    }

// Background Gradient Type
@Suppress("UNCHECKED_CAST")
fun <T> T.backgroundGradientType(type: UiLayoutHelper.GradientType): T where T : IUiLayout, T : View =
    apply {
        helper.isGradient = true
        helper.bgGradientType = type
        invalidateView()
    }

@Suppress("UNCHECKED_CAST")
fun <T> T.linearGradient(): T where T : IUiLayout, T : View =
    backgroundGradientType(UiLayoutHelper.GradientType.LINEAR)

@Suppress("UNCHECKED_CAST")
fun <T> T.radialGradient(): T where T : IUiLayout, T : View =
    backgroundGradientType(UiLayoutHelper.GradientType.RADIAL)

@Suppress("UNCHECKED_CAST")
fun <T> T.sweepGradient(): T where T : IUiLayout, T : View =
    backgroundGradientType(UiLayoutHelper.GradientType.SWEEP)

@Suppress("UNCHECKED_CAST")
fun <T> T.gradientCenter(centerX: Float, centerY: Float): T where T : IUiLayout, T : View = apply {
    helper.bgGradientCenterX = centerX
    helper.bgGradientCenterY = centerY
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.gradientCenterX(centerX: Float): T where T : IUiLayout, T : View = apply {
    helper.bgGradientCenterX = centerX
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.gradientCenterY(centerY: Float): T where T : IUiLayout, T : View = apply {
    helper.bgGradientCenterY = centerY
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.gradientRadius(radius: Float): T where T : IUiLayout, T : View = apply {
    helper.bgGradientRadius = radius
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.gradientPositions(vararg positions: Float): T where T : IUiLayout, T : View = apply {
    helper.bgGradientPositions = if (positions.isNotEmpty()) positions else null
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeGradientPositions(vararg positions: Float): T where T : IUiLayout, T : View = apply {
    helper.stGradientPositions = if (positions.isNotEmpty()) positions else null
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.radialGradient(
      centerX: Float,
      centerY: Float,
      radius: Float = 0f
): T where T : IUiLayout, T : View = apply {
    helper.isGradient = true
    helper.bgGradientType = UiLayoutHelper.GradientType.RADIAL
    helper.bgGradientCenterX = centerX
    helper.bgGradientCenterY = centerY
    helper.bgGradientRadius = radius
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.sweepGradient(centerX: Float, centerY: Float): T where T : IUiLayout, T : View = apply {
    helper.isGradient = true
    helper.bgGradientType = UiLayoutHelper.GradientType.SWEEP
    helper.bgGradientCenterX = centerX
    helper.bgGradientCenterY = centerY
    invalidateView()
}

// Stroke
@Suppress("UNCHECKED_CAST")
fun <T> T.strokeWidth(width: Float): T where T : IUiLayout, T : View = apply {
    helper.stWidth = width
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeLight(color: Int): T where T : IUiLayout, T : View = apply {
    helper.stColors = null
    helper.stColorLight = color
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeDark(color: Int): T where T : IUiLayout, T : View = apply {
    helper.stColors = null
    helper.stColorDark = color
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeColor(color: Int): T where T : IUiLayout, T : View = apply {
    helper.stColors = null
    helper.stColorLight = color
    helper.stColorDark = color
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeDashed(dashed: Boolean, spacing: Float = 10f): T where T : IUiLayout, T : View =
    apply {
        helper.isDashed = dashed
        helper.dashSpace = spacing
        invalidateView()
    }

@Suppress("UNCHECKED_CAST")
fun <T> T.dashed(spacing: Float = 10f): T where T : IUiLayout, T : View =
    strokeDashed(true, spacing)

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeGradientColors(colors: IntArray): T where T : IUiLayout, T : View = apply {
    helper.stColors = colors
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeGradient(vararg colors: Int): T where T : IUiLayout, T : View = apply {
    helper.stColors = if (colors.isNotEmpty()) colors else null
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeOrientation(orientation: UiLayoutHelper.GradientOrientation): T where T : IUiLayout, T : View =
    apply {
        helper.strokeGradientOrientation = orientation
        invalidateView()
    }

// Stroke options
@Suppress("UNCHECKED_CAST")
fun <T> T.strokeOption(option: Int): T where T : IUiLayout, T : View = apply {
    helper.strokeOption = option
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeTop(): T where T : IUiLayout, T : View = apply {
    helper.strokeOption = UiLayoutHelper.STROKE_TOP
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeLeft(): T where T : IUiLayout, T : View = apply {
    helper.strokeOption = UiLayoutHelper.STROKE_LEFT
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeBottom(): T where T : IUiLayout, T : View = apply {
    helper.strokeOption = UiLayoutHelper.STROKE_BOTTOM
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeRight(): T where T : IUiLayout, T : View = apply {
    helper.strokeOption = UiLayoutHelper.STROKE_RIGHT
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeHorizontal(): T where T : IUiLayout, T : View = apply {
    helper.strokeOption = UiLayoutHelper.STROKE_HORIZONTAL
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeVertical(): T where T : IUiLayout, T : View = apply {
    helper.strokeOption = UiLayoutHelper.STROKE_VERTICAL
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeTopLeft(): T where T : IUiLayout, T : View = apply {
    helper.strokeOption = UiLayoutHelper.STROKE_TOP_LEFT
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeTopRight(): T where T : IUiLayout, T : View = apply {
    helper.strokeOption = UiLayoutHelper.STROKE_TOP_RIGHT
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeBottomLeft(): T where T : IUiLayout, T : View = apply {
    helper.strokeOption = UiLayoutHelper.STROKE_BOTTOM_LEFT
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeBottomRight(): T where T : IUiLayout, T : View = apply {
    helper.strokeOption = UiLayoutHelper.STROKE_BOTTOM_RIGHT
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeAll(): T where T : IUiLayout, T : View = apply {
    helper.strokeOption = UiLayoutHelper.STROKE_ALL
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeNone(): T where T : IUiLayout, T : View = apply {
    helper.strokeOption = UiLayoutHelper.STROKE_NONE
    invalidateView()
}

@Suppress("UNCHECKED_CAST")
fun <T> T.strokeSides(
      top: Boolean = false,
      left: Boolean = false,
      bottom: Boolean = false,
      right: Boolean = false
): T where T : IUiLayout, T : View = apply {
    helper.strokeOption = (if (top) UiLayoutHelper.STROKE_TOP else 0) or
          (if (left) UiLayoutHelper.STROKE_LEFT else 0) or
          (if (bottom) UiLayoutHelper.STROKE_BOTTOM else 0) or
          (if (right) UiLayoutHelper.STROKE_RIGHT else 0)
    invalidateView()
}
// ==================== Gradient Colors Array (short names) ====================
/** Set background gradient with multiple colors (any number) */
@Suppress("UNCHECKED_CAST")
fun <T> T.bgColors(vararg colors: Int): T where T : IUiLayout, T : View = apply {
    helper.bgColors = if (colors.isNotEmpty()) colors else null
    invalidateView()
}

/** Set background gradient with colors and orientation */
@Suppress("UNCHECKED_CAST")
fun <T> T.bgColors(
      orientation: UiLayoutHelper.GradientOrientation,
      vararg colors: Int
): T where T : IUiLayout, T : View = apply {
    helper.bgColors = if (colors.isNotEmpty()) colors else null
    helper.bgGradientOrientation = orientation
    invalidateView()
}

/** Set stroke gradient with multiple colors (any number) */
@Suppress("UNCHECKED_CAST")
fun <T> T.stColors(vararg colors: Int): T where T : IUiLayout, T : View = apply {
    helper.stColors = if (colors.isNotEmpty()) colors else null
    invalidateView()
}

/** Set stroke gradient with colors and orientation */
@Suppress("UNCHECKED_CAST")
fun <T> T.stColors(
      orientation: UiLayoutHelper.GradientOrientation,
      vararg colors: Int
): T where T : IUiLayout, T : View = apply {
    helper.stColors = if (colors.isNotEmpty()) colors else null
    helper.strokeGradientOrientation = orientation
    invalidateView()
}

/** Set solid background color */
@Suppress("UNCHECKED_CAST")
fun <T> T.bgColor(color: Int): T where T : IUiLayout, T : View = apply {
    helper.bgColors = null
    helper.bgColorLight = color
    helper.bgColorDark = color
    invalidateView()
}

/** Set solid stroke color */
@Suppress("UNCHECKED_CAST")
fun <T> T.stColor(color: Int): T where T : IUiLayout, T : View = apply {
    helper.stColors = null
    helper.stColorLight = color
    helper.stColorDark = color
    invalidateView()
}

/** Set stroke width (short name) */
@Suppress("UNCHECKED_CAST")
fun <T> T.stWidth(width: Float): T where T : IUiLayout, T : View = apply {
    helper.stWidth = width
    invalidateView()
}

/** Set corner radius (short name) */
@Suppress("UNCHECKED_CAST")
fun <T> T.corner(radius: Float): T where T : IUiLayout, T : View = cornerRadius(radius)

/** Set individual corners (tl, tr, br, bl) */
@Suppress("UNCHECKED_CAST")
fun <T> T.corners(
      tl: Float = 0f,
      tr: Float = 0f,
      br: Float = 0f,
      bl: Float = 0f
): T where T : IUiLayout, T : View = cornerRadii(tl, tr, br, bl)

// ==================== Dimension Ratio and Percentage Sizing ====================

/** Set dimension ratio (e.g., "16:9", "W,16:9", "H,4:3") */
@Suppress("UNCHECKED_CAST")
fun <T> T.dimenRatio(ratio: String): T where T : IUiLayout, T : View = apply {
    helper.setDimenRatio(ratio)
}

/** Set dimension ratio with width:height values */
@Suppress("UNCHECKED_CAST")
fun <T> T.dimenRatio(width: Float, height: Float): T where T : IUiLayout, T : View = apply {
    if (height > 0f) {
        helper.setDimenRatio(width / height, UiLayoutHelper.DimenRatioSide.HEIGHT)
    }
}

/** Set dimension ratio with explicit side control */
@Suppress("UNCHECKED_CAST")
fun <T> T.dimenRatio(
      ratio: Float,
      side: UiLayoutHelper.DimenRatioSide = UiLayoutHelper.DimenRatioSide.HEIGHT
): T where T : IUiLayout, T : View = apply {
    helper.setDimenRatio(ratio, side)
}

/** Clear dimension ratio */
@Suppress("UNCHECKED_CAST")
fun <T> T.clearDimenRatio(): T where T : IUiLayout, T : View = apply {
    helper.setDimenRatio(null)
}

/** Set width as percentage of parent (0-100) */
@Suppress("UNCHECKED_CAST")
fun <T> T.widthPercent(percent: Float): T where T : IUiLayout, T : View = apply {
    helper.setWidthPercent(percent)
}

/** Set height as percentage of parent (0-100) */
@Suppress("UNCHECKED_CAST")
fun <T> T.heightPercent(percent: Float): T where T : IUiLayout, T : View = apply {
    helper.setHeightPercent(percent)
}

/** Set both width and height as percentage of parent */
@Suppress("UNCHECKED_CAST")
fun <T> T.sizePercent(widthPercent: Float, heightPercent: Float): T where T : IUiLayout, T : View = apply {
    helper.setSizePercent(widthPercent, heightPercent)
}

/** Set max width as percentage of parent (0-100) */
@Suppress("UNCHECKED_CAST")
fun <T> T.maxWidthPercent(percent: Float): T where T : IUiLayout, T : View = apply {
    helper.setMaxWidthPercent(percent)
}

/** Set max height as percentage of parent (0-100) */
@Suppress("UNCHECKED_CAST")
fun <T> T.maxHeightPercent(percent: Float): T where T : IUiLayout, T : View = apply {
    helper.setMaxHeightPercent(percent)
}

/** Set min width as percentage of parent (0-100) */
@Suppress("UNCHECKED_CAST")
fun <T> T.minWidthPercent(percent: Float): T where T : IUiLayout, T : View = apply {
    helper.setMinWidthPercent(percent)
}

/** Set min height as percentage of parent (0-100) */
@Suppress("UNCHECKED_CAST")
fun <T> T.minHeightPercent(percent: Float): T where T : IUiLayout, T : View = apply {
    helper.setMinHeightPercent(percent)
}

// ==================== Shape ====================
/** Set as circle shape */
@Suppress("UNCHECKED_CAST")
fun <T> T.circle(): T where T : IUiLayout, T : View = apply {
    helper.isCircle = true
    helper.shapeType = UiLayoutHelper.ShapeType.CIRCLE
    updateViewClipPath()
    invalidateViewOutline()
    invalidateView()
}

/** Set as oval shape */
@Suppress("UNCHECKED_CAST")
fun <T> T.oval(): T where T : IUiLayout, T : View = apply {
    helper.shapeType = UiLayoutHelper.ShapeType.OVAL
    updateViewClipPath()
    invalidateViewOutline()
    invalidateView()
}

/** Set shape type */
@Suppress("UNCHECKED_CAST")
fun <T> T.shapeType(type: UiLayoutHelper.ShapeType): T where T : IUiLayout, T : View = apply {
    helper.shapeType = type
    if (type == UiLayoutHelper.ShapeType.CIRCLE) helper.isCircle = true
    updateViewClipPath()
    invalidateViewOutline()
    invalidateView()
}

// ==================== Aspect Ratio Shortcuts ====================
/** Set square aspect ratio (1:1) */
@Suppress("UNCHECKED_CAST")
fun <T> T.square(): T where T : IUiLayout, T : View = apply {
    helper.dimenRatioValue = 1f
    helper.dimenRatioSide = UiLayoutHelper.DimenRatioSide.HEIGHT
}

/** Set video aspect ratio (16:9) */
@Suppress("UNCHECKED_CAST")
fun <T> T.video16x9(): T where T : IUiLayout, T : View = apply {
    helper.dimenRatioValue = 16f / 9f
    helper.dimenRatioSide = UiLayoutHelper.DimenRatioSide.HEIGHT
}

/** Set video aspect ratio (4:3) */
@Suppress("UNCHECKED_CAST")
fun <T> T.video4x3(): T where T : IUiLayout, T : View = apply {
    helper.dimenRatioValue = 4f / 3f
    helper.dimenRatioSide = UiLayoutHelper.DimenRatioSide.HEIGHT
}

/** Set portrait aspect ratio (9:16) */
@Suppress("UNCHECKED_CAST")
fun <T> T.portrait9x16(): T where T : IUiLayout, T : View = apply {
    helper.dimenRatioValue = 9f / 16f
    helper.dimenRatioSide = UiLayoutHelper.DimenRatioSide.HEIGHT
}

/** Set golden ratio (1.618:1) */
@Suppress("UNCHECKED_CAST")
fun <T> T.goldenRatio(): T where T : IUiLayout, T : View = apply {
    helper.dimenRatioValue = 1.618f
    helper.dimenRatioSide = UiLayoutHelper.DimenRatioSide.HEIGHT
}

// ==================== Overlay ====================
/** Set overlay color */
@Suppress("UNCHECKED_CAST")
fun <T> T.overlayColor(color: Int): T where T : IUiLayout, T : View = apply {
    helper.overlayColor = color
    invalidateView()
}

/** Set overlay with alpha (0-255) */
@Suppress("UNCHECKED_CAST")
fun <T> T.overlayAlpha(alpha: Int): T where T : IUiLayout, T : View = apply {
    helper.overlayColor = android.graphics.Color.argb(alpha, 0, 0, 0)
    invalidateView()
}

// ==================== Inner Shadow ====================
/** Enable inner shadow */
@Suppress("UNCHECKED_CAST")
fun <T> T.innerShadow(radius: Float, color: Int = android.graphics.Color.argb(80, 0, 0, 0)): T where T : IUiLayout, T : View = apply {
    helper.innerShadowEnabled = true
    helper.innerShadowRadius = radius
    helper.innerShadowColor = color
    invalidateView()
}

/** Set inner shadow with offset */
@Suppress("UNCHECKED_CAST")
fun <T> T.innerShadow(radius: Float, dx: Float, dy: Float, color: Int = android.graphics.Color.argb(80, 0, 0, 0)): T where T : IUiLayout, T : View = apply {
    helper.innerShadowEnabled = true
    helper.innerShadowRadius = radius
    helper.innerShadowDx = dx
    helper.innerShadowDy = dy
    helper.innerShadowColor = color
    invalidateView()
}

// ==================== Pressed State ====================
/** Set pressed background color */
@Suppress("UNCHECKED_CAST")
fun <T> T.pressedBg(color: Int): T where T : IUiLayout, T : View = apply {
    helper.pressedBgColor = color
}

/** Set pressed scale */
@Suppress("UNCHECKED_CAST")
fun <T> T.pressedScale(scale: Float): T where T : IUiLayout, T : View = apply {
    helper.pressedScale = scale
}

// ==================== Ripple ====================
/** Enable ripple effect */
@Suppress("UNCHECKED_CAST")
fun <T> T.ripple(color: Int = android.graphics.Color.argb(50, 0, 0, 0)): T where T : IUiLayout, T : View = apply {
    helper.rippleEnabled = true
    helper.rippleColor = color
}

/** Enable borderless ripple effect */
@Suppress("UNCHECKED_CAST")
fun <T> T.rippleBorderless(color: Int = android.graphics.Color.argb(50, 0, 0, 0)): T where T : IUiLayout, T : View = apply {
    helper.rippleEnabled = true
    helper.rippleColor = color
    helper.rippleBorderless = true
}

// ==================== Border Style ====================
/** Set dashed border */
@Suppress("UNCHECKED_CAST")
fun <T> T.dashedBorder(dashWidth: Float = 10f, dashGap: Float = 10f): T where T : IUiLayout, T : View = apply {
    helper.borderStyle = UiLayoutHelper.BorderStyle.DASHED
    helper.isDashed = true
    helper.dashSpace = dashGap
    invalidateView()
}

/** Set dotted border */
@Suppress("UNCHECKED_CAST")
fun <T> T.dottedBorder(): T where T : IUiLayout, T : View = apply {
    helper.borderStyle = UiLayoutHelper.BorderStyle.DOTTED
    helper.isDashed = true
    helper.dashSpace = helper.dp(2f)
    invalidateView()
}

// ==================== Child Gap ====================
/** Set gap between children (for LinearLayout types) */
@Suppress("UNCHECKED_CAST")
fun <T> T.childGap(gap: Float): T where T : IUiLayout, T : View = apply {
    helper.childGap = gap
}
