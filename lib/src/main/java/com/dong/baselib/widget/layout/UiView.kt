package com.dong.baselib.widget.layout

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.dong.baselib.R

class UiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), IUiLayout {

    override val helper = UiLayoutHelper(this)

    init {
        setWillNotDraw(false)
        context.obtainStyledAttributes(attrs, R.styleable.UiView).apply {
            try {
                helper.readCornerAttrs(this,
                    R.styleable.UiView_cornerRadius,
                    R.styleable.UiView_cornerTopLeft,
                    R.styleable.UiView_cornerTopRight,
                    R.styleable.UiView_cornerBottomLeft,
                    R.styleable.UiView_cornerBottomRight
                )
                helper.readBackgroundAttrs(this,
                    R.styleable.UiView_bgGradient,
                    R.styleable.UiView_bgGradientStart,
                    R.styleable.UiView_bgGradientCenter,
                    R.styleable.UiView_bgGradientEnd,
                    R.styleable.UiView_bgColor,
                    R.styleable.UiView_bgGradientOrientation,
                    R.styleable.UiView_bgGradientType,
                    R.styleable.UiView_bgGradientCenterX,
                    R.styleable.UiView_bgGradientCenterY,
                    R.styleable.UiView_bgGradientRadius,
                    R.styleable.UiView_bgGradientColors,
                    R.styleable.UiView_bgColors,
                    R.styleable.UiView_bgGradientPositions
                )
                helper.readStrokeAttrs(this,
                    R.styleable.UiView_strokeWidth,
                    R.styleable.UiView_strokeColor,
                    R.styleable.UiView_strokeDashed,
                    R.styleable.UiView_dashGap,
                    R.styleable.UiView_strokeGradientColors,
                    R.styleable.UiView_strokeGradientOrientation,
                    R.styleable.UiView_strokeOption,
                    R.styleable.UiView_strokeCap,
                    R.styleable.UiView_strokeColors,
                    R.styleable.UiView_strokeGradientPositions
                )
                helper.readShadowAttrs(this,
                    R.styleable.UiView_shadowColor,
                    R.styleable.UiView_shadowRadius,
                    R.styleable.UiView_shadowDx,
                    R.styleable.UiView_shadowDy,
                    R.styleable.UiView_shadowElevation
                )
                helper.readDimensionAttrs(this,
                    R.styleable.UiView_uiDimenRatio,
                    R.styleable.UiView_uiWidthParentPercent,
                    R.styleable.UiView_uiHeightParentPercent,
                    R.styleable.UiView_uiMaxWidthParentPercent,
                    R.styleable.UiView_uiMaxHeightParentPercent,
                    R.styleable.UiView_uiMinWidthParentPercent,
                    R.styleable.UiView_uiMinHeightParentPercent,
                    R.styleable.UiView_uiWidthScreenPercent,
                    R.styleable.UiView_uiHeightScreenPercent,
                    R.styleable.UiView_uiMaxWidthScreenPercent,
                    R.styleable.UiView_uiMaxHeightScreenPercent,
                    R.styleable.UiView_uiMinWidthScreenPercent,
                    R.styleable.UiView_uiMinHeightScreenPercent
                )
                helper.readRippleAttrs(this,
                    R.styleable.UiView_rippleEnabled,
                    R.styleable.UiView_rippleColor,
                    R.styleable.UiView_rippleBorderless
                )
                helper.readShapeAttrs(this,
                    R.styleable.UiView_shapeType,
                    R.styleable.UiView_isCircle
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

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        
        helper.drawBackground(canvas, w, h)
        val save = canvas.save()
        canvas.clipPath(helper.getClipPath())
        super.onDraw(canvas)
        canvas.restoreToCount(save)
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
