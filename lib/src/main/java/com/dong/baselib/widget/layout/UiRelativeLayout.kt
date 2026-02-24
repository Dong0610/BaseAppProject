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
                    R.styleable.UiRelativeLayout_bgIsGradient,
                    R.styleable.UiRelativeLayout_bgGradientStart,
                    R.styleable.UiRelativeLayout_bgGradientCenter,
                    R.styleable.UiRelativeLayout_bgGradientEnd,
                    R.styleable.UiRelativeLayout_bgColorLight,
                    R.styleable.UiRelativeLayout_bgColorDark,
                    R.styleable.UiRelativeLayout_bgColorAll,
                    R.styleable.UiRelativeLayout_bgGdOrientation,
                    R.styleable.UiRelativeLayout_bgGradientType,
                    R.styleable.UiRelativeLayout_bgGradientCenterX,
                    R.styleable.UiRelativeLayout_bgGradientCenterY,
                    R.styleable.UiRelativeLayout_bgGradientRadius,
                    R.styleable.UiRelativeLayout_bgGradientColors,
                    R.styleable.UiRelativeLayout_bgColors,
                    R.styleable.UiRelativeLayout_bgGdPositions
                )
                helper.readStrokeAttrs(this,
                    R.styleable.UiRelativeLayout_strokeWidth,
                    R.styleable.UiRelativeLayout_stColorLight,
                    R.styleable.UiRelativeLayout_stColorDark,
                    R.styleable.UiRelativeLayout_stColorAll,
                    R.styleable.UiRelativeLayout_strokeDistance,
                    R.styleable.UiRelativeLayout_distanceSpace,
                    R.styleable.UiRelativeLayout_strokeGradient,
                    R.styleable.UiRelativeLayout_strokeGdOrientation,
                    R.styleable.UiRelativeLayout_strokeOption,
                    R.styleable.UiRelativeLayout_strokeCap,
                    R.styleable.UiRelativeLayout_stColors,
                    R.styleable.UiRelativeLayout_stGdPositions
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
                    R.styleable.UiRelativeLayout_uiWidthPercent,
                    R.styleable.UiRelativeLayout_uiHeightPercent,
                    R.styleable.UiRelativeLayout_uiMaxWidthPercent,
                    R.styleable.UiRelativeLayout_uiMaxHeightPercent,
                    R.styleable.UiRelativeLayout_uiMinWidthPercent,
                    R.styleable.UiRelativeLayout_uiMinHeightPercent
                )
                helper.readRippleAttrs(this,
                    R.styleable.UiRelativeLayout_rippleEnabled,
                    R.styleable.UiRelativeLayout_rippleColor,
                    R.styleable.UiRelativeLayout_rippleBorderless
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
