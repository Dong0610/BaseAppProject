package com.dong.baselib.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withSave
import com.dong.baselib.R
import com.dong.baselib.widget.layout.IUiLayout
import com.dong.baselib.widget.layout.UiLayoutHelper

@SuppressLint("CustomViewStyleable")
class UiImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), IUiLayout {

    override val helper = UiLayoutHelper(this)

    // Image-specific properties
    private var gradientIconColors: IntArray? = null
    private var gradientIconOrientation = UiLayoutHelper.GradientOrientation.LEFT_TO_RIGHT

    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        isDither = true
    }
    private val gradientIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }
    private val rectF = RectF()
    private val backgroundPath = Path()

    init {
        setWillNotDraw(false)
        context.obtainStyledAttributes(attrs, R.styleable.UiImageView).apply {
            try {
                // Common layout attrs via helper
                helper.readCornerAttrs(
                    this,
                    R.styleable.UiImageView_cornerRadius,
                    R.styleable.UiImageView_cornerTopLeft,
                    R.styleable.UiImageView_cornerTopRight,
                    R.styleable.UiImageView_cornerBottomLeft,
                    R.styleable.UiImageView_cornerBottomRight
                )
                helper.readBackgroundAttrs(
                    this,
                    R.styleable.UiImageView_bgGradient,
                    R.styleable.UiImageView_bgGradientStart,
                    R.styleable.UiImageView_bgGradientCenter,
                    R.styleable.UiImageView_bgGradientEnd,
                    R.styleable.UiImageView_bgColor,
                    R.styleable.UiImageView_bgGradientOrientation,
                    R.styleable.UiImageView_bgGradientType,
                    R.styleable.UiImageView_bgGradientCenterX,
                    R.styleable.UiImageView_bgGradientCenterY,
                    R.styleable.UiImageView_bgGradientRadius,
                    R.styleable.UiImageView_bgGradientColors,
                    R.styleable.UiImageView_bgColors,
                    R.styleable.UiImageView_bgGradientPositions
                )
                helper.readStrokeAttrs(
                    this,
                    R.styleable.UiImageView_strokeWidth,
                    R.styleable.UiImageView_strokeColor,
                    R.styleable.UiImageView_strokeDashed,
                    R.styleable.UiImageView_dashGap,
                    R.styleable.UiImageView_strokeGradientColors,
                    R.styleable.UiImageView_strokeGradientOrientation,
                    R.styleable.UiImageView_strokeOption,
                    -1,
                    R.styleable.UiImageView_strokeColors,
                    R.styleable.UiImageView_strokeGradientPositions
                )
                helper.readShadowAttrs(
                    this,
                    R.styleable.UiImageView_shadowColor,
                    R.styleable.UiImageView_shadowRadius,
                    R.styleable.UiImageView_shadowDx,
                    R.styleable.UiImageView_shadowDy,
                    R.styleable.UiImageView_shadowElevation
                )

                // Image-specific attrs
                val gradientIconsStr = getString(R.styleable.UiImageView_gradientIcons)
                gradientIconColors = gradientIconsStr?.parseHexColors()
                gradientIconOrientation = getInt(R.styleable.UiImageView_imageGradientOrientation, 6)
                    .toGradientOrientation()

                helper.readDimensionAttrs(
                    this,
                    R.styleable.UiImageView_uiDimenRatio,
                    R.styleable.UiImageView_uiWidthParentPercent,
                    R.styleable.UiImageView_uiHeightParentPercent,
                    R.styleable.UiImageView_uiMaxWidthParentPercent,
                    R.styleable.UiImageView_uiMaxHeightParentPercent,
                    R.styleable.UiImageView_uiMinWidthParentPercent,
                    R.styleable.UiImageView_uiMinHeightParentPercent,
                    R.styleable.UiImageView_uiWidthScreenPercent,
                    R.styleable.UiImageView_uiHeightScreenPercent,
                    R.styleable.UiImageView_uiMaxWidthScreenPercent,
                    R.styleable.UiImageView_uiMaxHeightScreenPercent,
                    R.styleable.UiImageView_uiMinWidthScreenPercent,
                    R.styleable.UiImageView_uiMinHeightScreenPercent
                )
                helper.readRippleAttrs(
                    this,
                    R.styleable.UiImageView_rippleEnabled,
                    R.styleable.UiImageView_rippleColor
                )
                helper.readShapeAttrs(this,
                    R.styleable.UiImageView_shapeType,
                    R.styleable.UiImageView_isCircle
                )
            } finally {
                recycle()
            }
        }

        helper.setupShadow()
        helper.setupRipple()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (helper.shouldApplyCustomMeasure()) {
            val dm = context.resources.displayMetrics
            val specW = MeasureSpec.getSize(widthMeasureSpec)
            val specH = MeasureSpec.getSize(heightMeasureSpec)
            val parentWidth = if (specW > 0) specW else
                (parent as? android.view.View)?.width?.takeIf { it > 0 } ?: dm.widthPixels
            val parentHeight = if (specH > 0) specH else
                (parent as? android.view.View)?.height?.takeIf { it > 0 } ?: dm.heightPixels
            val result = helper.measureWithConstraints(
                widthMeasureSpec, heightMeasureSpec, parentWidth, parentHeight
            )
            val wSpec = if (result.widthCustomized)
                MeasureSpec.makeMeasureSpec(result.width, MeasureSpec.EXACTLY)
                else widthMeasureSpec
            val hSpec = if (result.heightCustomized)
                MeasureSpec.makeMeasureSpec(result.height, MeasureSpec.EXACTLY)
                else heightMeasureSpec
            super.onMeasure(wSpec, hSpec)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        helper.onSizeChanged(w, h)
        helper.setupRipple()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val radii = helper.getCornerRadii(w, h)

        // 1) Pre-L shadow
        

        // 2) Setup clip path
        backgroundPath.reset()
        rectF.set(0f, 0f, w, h)
        backgroundPath.addRoundRect(rectF, radii, Path.Direction.CW)

        canvas.withSave {
            runCatching { canvas.clipPath(backgroundPath) }

            // 3) Draw background
            helper.drawBackground(canvas, w, h)

            // 4) Draw image with optional gradient overlay
            if (gradientIconColors != null && gradientIconColors!!.size > 1) {
                val layerBounds = RectF(0f, 0f, w, h)
                val saveCount = canvas.saveLayer(layerBounds, null)
                super.onDraw(canvas)
                drawGradientOverlay(canvas, w, h)
                canvas.restoreToCount(saveCount)
            } else {
                super.onDraw(canvas)
            }

            // 5) Draw stroke
            helper.drawStroke(canvas, w, h)
        }
    }

    private fun drawGradientOverlay(canvas: Canvas, width: Float, height: Float) {
        gradientIconColors?.takeIf { it.size > 1 }?.let { colors ->
            gradientIconPaint.shader = createIconGradientShader(width, height, colors)
            canvas.drawRect(0f, 0f, width, height, gradientIconPaint)
        }
    }

    private fun createIconGradientShader(width: Float, height: Float, colors: IntArray): Shader {
        val (x0, y0, x1, y1) = gradientIconOrientation.toCoordinates(width, height)
        return LinearGradient(x0, y0, x1, y1, colors, null, Shader.TileMode.CLAMP)
    }

    private fun String.parseHexColors(): IntArray? {
        return split(" ")
            .mapNotNull { if (it.isValidHexColor()) it.toColorInt() else null }
            .takeIf { it.isNotEmpty() }
            ?.toIntArray()
    }

    private fun String.isValidHexColor(): Boolean =
        matches(Regex("^#?[0-9a-fA-F]{6,8}$"))

    private fun Int.toGradientOrientation() =
        UiLayoutHelper.GradientOrientation.entries.getOrElse(this) {
            UiLayoutHelper.GradientOrientation.LEFT_TO_RIGHT
        }

    // IUiLayout implementation
    override fun invalidateView() = invalidate()
    override fun invalidateViewOutline() = invalidateOutline()
    override fun updateViewClipPath() = helper.updateClipPath(width, height)

    fun setCompatElevation(
        elevationDp: Float = helper.compatElevationDp,
        radiusDp: Float = helper.cornerRadius / resources.displayMetrics.density,
        color: Int = helper.compatShadowColor,
        shadowRadius: Float = helper.shadowRadiusPx,
        shadowDx: Float = helper.shadowDxPx,
        shadowDy: Float = helper.shadowDyPx
    ) = apply {
        helper.compatElevationDp = elevationDp
        helper.cornerRadius = helper.dp(radiusDp)
        helper.compatShadowColor = color
        helper.shadowRadiusPx = shadowRadius
        helper.shadowDxPx = shadowDx
        helper.shadowDyPx = shadowDy
        helper.setupShadow()
        invalidateOutline()
    }

    // Image-specific fluent API
    fun setGradientIconColors(colors: IntArray?) = apply {
        gradientIconColors = colors
        invalidate()
    }

    fun setGradientIconColors(startColor: Int, endColor: Int) = apply {
        gradientIconColors = intArrayOf(startColor, endColor)
        invalidate()
    }

    fun setGradientIconColors(startColor: Int, centerColor: Int, endColor: Int) = apply {
        gradientIconColors = intArrayOf(startColor, centerColor, endColor)
        invalidate()
    }

    fun gradientIconOrientation(orientation: UiLayoutHelper.GradientOrientation) = apply {
        gradientIconOrientation = orientation
        invalidate()
    }

    fun clearGradientIcon() = apply {
        gradientIconColors = null
        invalidate()
    }
}
