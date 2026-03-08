package com.dong.baselib.widget.layout

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.graphics.withClip
import com.dong.baselib.R

@Suppress("DEPRECATION")
open class UiFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IUiLayout {

    override val helper = UiLayoutHelper(this)

    init {
        setWillNotDraw(false)
        context.obtainStyledAttributes(attrs, R.styleable.UiFrameLayout).apply {
            try {
                helper.readCornerAttrs(this,
                    R.styleable.UiFrameLayout_cornerRadius,
                    R.styleable.UiFrameLayout_cornerTopLeft,
                    R.styleable.UiFrameLayout_cornerTopRight,
                    R.styleable.UiFrameLayout_cornerBottomLeft,
                    R.styleable.UiFrameLayout_cornerBottomRight
                )
                helper.readBackgroundAttrs(this,
                    R.styleable.UiFrameLayout_bgGradient,
                    R.styleable.UiFrameLayout_bgGradientStart,
                    R.styleable.UiFrameLayout_bgGradientCenter,
                    R.styleable.UiFrameLayout_bgGradientEnd,
                    R.styleable.UiFrameLayout_bgColor,
                    R.styleable.UiFrameLayout_bgGradientOrientation,
                    R.styleable.UiFrameLayout_bgGradientType,
                    R.styleable.UiFrameLayout_bgGradientCenterX,
                    R.styleable.UiFrameLayout_bgGradientCenterY,
                    R.styleable.UiFrameLayout_bgGradientRadius,
                    R.styleable.UiFrameLayout_bgGradientColors,
                    R.styleable.UiFrameLayout_bgColors,
                    R.styleable.UiFrameLayout_bgGradientPositions
                )
                helper.readStrokeAttrs(this,
                    R.styleable.UiFrameLayout_strokeWidth,
                    R.styleable.UiFrameLayout_strokeColor,
                    R.styleable.UiFrameLayout_strokeDashed,
                    R.styleable.UiFrameLayout_dashGap,
                    R.styleable.UiFrameLayout_strokeGradientColors,
                    R.styleable.UiFrameLayout_strokeGradientOrientation,
                    R.styleable.UiFrameLayout_strokeOption,
                    R.styleable.UiFrameLayout_strokeCap,
                    R.styleable.UiFrameLayout_strokeColors,
                    R.styleable.UiFrameLayout_strokeGradientPositions
                )
                helper.readShadowAttrs(this,
                    R.styleable.UiFrameLayout_shadowColor,
                    R.styleable.UiFrameLayout_shadowRadius,
                    R.styleable.UiFrameLayout_shadowDx,
                    R.styleable.UiFrameLayout_shadowDy,
                    R.styleable.UiFrameLayout_shadowElevation
                )
                helper.readDimensionAttrs(this,
                    R.styleable.UiFrameLayout_uiDimenRatio,
                    R.styleable.UiFrameLayout_uiWidthParentPercent,
                    R.styleable.UiFrameLayout_uiHeightParentPercent,
                    R.styleable.UiFrameLayout_uiMaxWidthParentPercent,
                    R.styleable.UiFrameLayout_uiMaxHeightParentPercent,
                    R.styleable.UiFrameLayout_uiMinWidthParentPercent,
                    R.styleable.UiFrameLayout_uiMinHeightParentPercent,
                    R.styleable.UiFrameLayout_uiWidthScreenPercent,
                    R.styleable.UiFrameLayout_uiHeightScreenPercent,
                    R.styleable.UiFrameLayout_uiMaxWidthScreenPercent,
                    R.styleable.UiFrameLayout_uiMaxHeightScreenPercent,
                    R.styleable.UiFrameLayout_uiMinWidthScreenPercent,
                    R.styleable.UiFrameLayout_uiMinHeightScreenPercent
                )
                helper.readRippleAttrs(this,
                    R.styleable.UiFrameLayout_rippleEnabled,
                    R.styleable.UiFrameLayout_rippleColor,
                    R.styleable.UiFrameLayout_rippleBorderless
                )
                helper.readShapeAttrs(this,
                    R.styleable.UiFrameLayout_shapeType,
                    R.styleable.UiFrameLayout_isCircle
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

    override fun dispatchDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        helper.drawBackground(canvas, w, h)
        canvas.withClip(helper.getClipPath()) {
            super.dispatchDraw(canvas)
        }
        helper.drawStroke(canvas, w, h)
    }

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
    ) {
        helper.compatElevationDp = elevationDp
        helper.cornerRadius = helper.dp(radiusDp)
        helper.compatShadowColor = color
        helper.shadowRadiusPx = shadowRadius
        helper.shadowDxPx = shadowDx
        helper.shadowDyPx = shadowDy
        helper.setupShadow()
        invalidateOutline()
    }
}
