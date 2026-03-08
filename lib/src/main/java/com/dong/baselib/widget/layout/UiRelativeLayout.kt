package com.dong.baselib.widget.layout

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.core.graphics.withClip
import com.dong.baselib.R

@Suppress("DEPRECATION")
open class UiRelativeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), IUiLayout {

    override val helper = UiLayoutHelper(this)

    init {
        setWillNotDraw(false)
        context.obtainStyledAttributes(attrs, R.styleable.UiRelativeLayout).apply {
            try {
                helper.readCornerAttrs(this,
                    R.styleable.UiRelativeLayout_cornerRadius,
                    R.styleable.UiRelativeLayout_cornerTopLeft,
                    R.styleable.UiRelativeLayout_cornerTopRight,
                    R.styleable.UiRelativeLayout_cornerBottomLeft,
                    R.styleable.UiRelativeLayout_cornerBottomRight
                )
                helper.readBackgroundAttrs(this,
                    R.styleable.UiRelativeLayout_bgGradient,
                    R.styleable.UiRelativeLayout_bgGradientStart,
                    R.styleable.UiRelativeLayout_bgGradientCenter,
                    R.styleable.UiRelativeLayout_bgGradientEnd,

                    R.styleable.UiRelativeLayout_bgColor,
                    R.styleable.UiRelativeLayout_bgGradientOrientation,
                    R.styleable.UiRelativeLayout_bgGradientType,
                    R.styleable.UiRelativeLayout_bgGradientCenterX,
                    R.styleable.UiRelativeLayout_bgGradientCenterY,
                    R.styleable.UiRelativeLayout_bgGradientRadius,
                    R.styleable.UiRelativeLayout_bgGradientColors,
                    R.styleable.UiRelativeLayout_bgColors,
                    R.styleable.UiRelativeLayout_bgGradientPositions
                )
                helper.readStrokeAttrs(this,
                    R.styleable.UiRelativeLayout_strokeWidth,

                    R.styleable.UiRelativeLayout_strokeColor,
                    R.styleable.UiRelativeLayout_strokeDashed,
                    R.styleable.UiRelativeLayout_dashGap,
                    R.styleable.UiRelativeLayout_strokeGradientColors,
                    R.styleable.UiRelativeLayout_strokeGradientOrientation,
                    R.styleable.UiRelativeLayout_strokeOption,
                    R.styleable.UiRelativeLayout_strokeCap,
                    R.styleable.UiRelativeLayout_strokeColors,
                    R.styleable.UiRelativeLayout_strokeGradientPositions
                )
                helper.readShadowAttrs(this,
                    R.styleable.UiRelativeLayout_shadowColor,
                    R.styleable.UiRelativeLayout_shadowRadius,
                    R.styleable.UiRelativeLayout_shadowDx,
                    R.styleable.UiRelativeLayout_shadowDy,
                    R.styleable.UiRelativeLayout_shadowElevation
                )
                helper.readDimensionAttrs(this,
                    R.styleable.UiRelativeLayout_uiDimenRatio,
                    R.styleable.UiRelativeLayout_uiWidthParentPercent,
                    R.styleable.UiRelativeLayout_uiHeightParentPercent,
                    R.styleable.UiRelativeLayout_uiMaxWidthParentPercent,
                    R.styleable.UiRelativeLayout_uiMaxHeightParentPercent,
                    R.styleable.UiRelativeLayout_uiMinWidthParentPercent,
                    R.styleable.UiRelativeLayout_uiMinHeightParentPercent,
                    R.styleable.UiRelativeLayout_uiWidthScreenPercent,
                    R.styleable.UiRelativeLayout_uiHeightScreenPercent,
                    R.styleable.UiRelativeLayout_uiMaxWidthScreenPercent,
                    R.styleable.UiRelativeLayout_uiMaxHeightScreenPercent,
                    R.styleable.UiRelativeLayout_uiMinWidthScreenPercent,
                    R.styleable.UiRelativeLayout_uiMinHeightScreenPercent
                )
                helper.readRippleAttrs(this,
                    R.styleable.UiRelativeLayout_rippleEnabled,
                    R.styleable.UiRelativeLayout_rippleColor,
                    R.styleable.UiRelativeLayout_rippleBorderless
                )
                helper.readShapeAttrs(this,
                    R.styleable.UiRelativeLayout_shapeType,
                    R.styleable.UiRelativeLayout_isCircle
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
