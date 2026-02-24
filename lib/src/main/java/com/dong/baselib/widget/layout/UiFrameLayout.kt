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
                    R.styleable.UiFrameLayout_bgIsGradient,
                    R.styleable.UiFrameLayout_bgGradientStart,
                    R.styleable.UiFrameLayout_bgGradientCenter,
                    R.styleable.UiFrameLayout_bgGradientEnd,
                    R.styleable.UiFrameLayout_bgColorLight,
                    R.styleable.UiFrameLayout_bgColorDark,
                    R.styleable.UiFrameLayout_bgColorAll,
                    R.styleable.UiFrameLayout_bgGdOrientation,
                    R.styleable.UiFrameLayout_bgGradientType,
                    R.styleable.UiFrameLayout_bgGradientCenterX,
                    R.styleable.UiFrameLayout_bgGradientCenterY,
                    R.styleable.UiFrameLayout_bgGradientRadius,
                    R.styleable.UiFrameLayout_bgGradientColors,
                    R.styleable.UiFrameLayout_bgColors,
                    R.styleable.UiFrameLayout_bgGdPositions
                )
                helper.readStrokeAttrs(this,
                    R.styleable.UiFrameLayout_strokeWidth,
                    R.styleable.UiFrameLayout_stColorLight,
                    R.styleable.UiFrameLayout_stColorDark,
                    R.styleable.UiFrameLayout_stColorAll,
                    R.styleable.UiFrameLayout_strokeDistance,
                    R.styleable.UiFrameLayout_distanceSpace,
                    R.styleable.UiFrameLayout_strokeGradient,
                    R.styleable.UiFrameLayout_strokeGdOrientation,
                    R.styleable.UiFrameLayout_strokeOption,
                    R.styleable.UiFrameLayout_strokeCap,
                    R.styleable.UiFrameLayout_stColors,
                    R.styleable.UiFrameLayout_stGdPositions
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
                    R.styleable.UiFrameLayout_uiWidthPercent,
                    R.styleable.UiFrameLayout_uiHeightPercent,
                    R.styleable.UiFrameLayout_uiMaxWidthPercent,
                    R.styleable.UiFrameLayout_uiMaxHeightPercent,
                    R.styleable.UiFrameLayout_uiMinWidthPercent,
                    R.styleable.UiFrameLayout_uiMinHeightPercent
                )
                helper.readRippleAttrs(this,
                    R.styleable.UiFrameLayout_rippleEnabled,
                    R.styleable.UiFrameLayout_rippleColor,
                    R.styleable.UiFrameLayout_rippleBorderless
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
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY)
            )
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
