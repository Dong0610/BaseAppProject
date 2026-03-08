package com.dong.baselib.widget.layout

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.withClip
import com.dong.baselib.R

@Suppress("DEPRECATION")
open class UiConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), IUiLayout {

    override val helper = UiLayoutHelper(this)

    init {
        setWillNotDraw(false)
        context.obtainStyledAttributes(attrs, R.styleable.UiConstraintLayout).apply {
            try {
                helper.readCornerAttrs(this,
                    R.styleable.UiConstraintLayout_cornerRadius,
                    R.styleable.UiConstraintLayout_cornerTopLeft,
                    R.styleable.UiConstraintLayout_cornerTopRight,
                    R.styleable.UiConstraintLayout_cornerBottomLeft,
                    R.styleable.UiConstraintLayout_cornerBottomRight
                )
                helper.readBackgroundAttrs(this,
                    R.styleable.UiConstraintLayout_bgGradient,
                    R.styleable.UiConstraintLayout_bgGradientStart,
                    R.styleable.UiConstraintLayout_bgGradientCenter,
                    R.styleable.UiConstraintLayout_bgGradientEnd,
                    R.styleable.UiConstraintLayout_bgColor,
                    R.styleable.UiConstraintLayout_bgGradientOrientation,
                    R.styleable.UiConstraintLayout_bgGradientType,
                    R.styleable.UiConstraintLayout_bgGradientCenterX,
                    R.styleable.UiConstraintLayout_bgGradientCenterY,
                    R.styleable.UiConstraintLayout_bgGradientRadius,
                    R.styleable.UiConstraintLayout_bgGradientColors,
                    R.styleable.UiConstraintLayout_bgColors,
                    R.styleable.UiConstraintLayout_bgGradientPositions
                )
                helper.readStrokeAttrs(this,
                    R.styleable.UiConstraintLayout_strokeWidth,
                    R.styleable.UiConstraintLayout_strokeColor,
                    R.styleable.UiConstraintLayout_strokeDashed,
                    R.styleable.UiConstraintLayout_dashGap,
                    R.styleable.UiConstraintLayout_strokeGradientColors,
                    R.styleable.UiConstraintLayout_strokeGradientOrientation,
                    R.styleable.UiConstraintLayout_strokeOption,
                    R.styleable.UiConstraintLayout_strokeCap,
                    R.styleable.UiConstraintLayout_strokeColors,
                    R.styleable.UiConstraintLayout_strokeGradientPositions
                )
                helper.readShadowAttrs(this,
                    R.styleable.UiConstraintLayout_shadowColor,
                    R.styleable.UiConstraintLayout_shadowRadius,
                    R.styleable.UiConstraintLayout_shadowDx,
                    R.styleable.UiConstraintLayout_shadowDy,
                    R.styleable.UiConstraintLayout_shadowElevation
                )
                helper.readDimensionAttrs(this,
                    R.styleable.UiConstraintLayout_uiDimenRatio,
                    R.styleable.UiConstraintLayout_uiWidthParentPercent,
                    R.styleable.UiConstraintLayout_uiHeightParentPercent,
                    R.styleable.UiConstraintLayout_uiMaxWidthParentPercent,
                    R.styleable.UiConstraintLayout_uiMaxHeightParentPercent,
                    R.styleable.UiConstraintLayout_uiMinWidthParentPercent,
                    R.styleable.UiConstraintLayout_uiMinHeightParentPercent,
                    R.styleable.UiConstraintLayout_uiWidthScreenPercent,
                    R.styleable.UiConstraintLayout_uiHeightScreenPercent,
                    R.styleable.UiConstraintLayout_uiMaxWidthScreenPercent,
                    R.styleable.UiConstraintLayout_uiMaxHeightScreenPercent,
                    R.styleable.UiConstraintLayout_uiMinWidthScreenPercent,
                    R.styleable.UiConstraintLayout_uiMinHeightScreenPercent
                )
                helper.readRippleAttrs(this,
                    R.styleable.UiConstraintLayout_rippleEnabled,
                    R.styleable.UiConstraintLayout_rippleColor,
                    R.styleable.UiConstraintLayout_rippleBorderless
                )
                helper.readShapeAttrs(this,
                    R.styleable.UiConstraintLayout_shapeType,
                    R.styleable.UiConstraintLayout_isCircle
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
