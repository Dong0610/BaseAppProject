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
                    R.styleable.UiView_bgIsGradient,
                    R.styleable.UiView_bgGradientStart,
                    R.styleable.UiView_bgGradientCenter,
                    R.styleable.UiView_bgGradientEnd,
                    R.styleable.UiView_bgColorLight,
                    R.styleable.UiView_bgColorDark,
                    R.styleable.UiView_bgColorAll,
                    R.styleable.UiView_bgGdOrientation,
                    R.styleable.UiView_bgGradientType,
                    R.styleable.UiView_bgGradientCenterX,
                    R.styleable.UiView_bgGradientCenterY,
                    R.styleable.UiView_bgGradientRadius,
                    R.styleable.UiView_bgGradientColors,
                    R.styleable.UiView_bgColors,
                    R.styleable.UiView_bgGdPositions
                )
                helper.readStrokeAttrs(this,
                    R.styleable.UiView_strokeWidth,
                    R.styleable.UiView_stColorLight,
                    R.styleable.UiView_stColorDark,
                    R.styleable.UiView_stColorAll,
                    R.styleable.UiView_strokeDistance,
                    R.styleable.UiView_distanceSpace,
                    R.styleable.UiView_strokeGradient,
                    R.styleable.UiView_strokeGdOrientation,
                    R.styleable.UiView_strokeOption,
                    R.styleable.UiView_strokeCap,
                    R.styleable.UiView_stColors,
                    R.styleable.UiView_stGdPositions
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
                    R.styleable.UiView_uiWidthPercent,
                    R.styleable.UiView_uiHeightPercent,
                    R.styleable.UiView_uiMaxWidthPercent,
                    R.styleable.UiView_uiMaxHeightPercent,
                    R.styleable.UiView_uiMinWidthPercent,
                    R.styleable.UiView_uiMinHeightPercent
                )
                helper.readRippleAttrs(this,
                    R.styleable.UiView_rippleEnabled,
                    R.styleable.UiView_rippleColor,
                    R.styleable.UiView_rippleBorderless
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
            val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
            val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
            val (newWidth, newHeight) = helper.measureWithConstraints(
                widthMeasureSpec, heightMeasureSpec, parentWidth, parentHeight
            )
            setMeasuredDimension(newWidth, newHeight)
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
