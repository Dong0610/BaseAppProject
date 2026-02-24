package com.dong.baselib.widget.layout

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.graphics.withClip
import com.dong.baselib.R

@Suppress("DEPRECATION")
open class UiLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), IUiLayout {

    override val helper = UiLayoutHelper(this)

    init {
        setWillNotDraw(false)
        context.obtainStyledAttributes(attrs, R.styleable.UiLinearLayout).apply {
            try {
                helper.readCornerAttrs(this,
                    R.styleable.UiLinearLayout_cornerRadius,
                    R.styleable.UiLinearLayout_cornerTopLeft,
                    R.styleable.UiLinearLayout_cornerTopRight,
                    R.styleable.UiLinearLayout_cornerBottomLeft,
                    R.styleable.UiLinearLayout_cornerBottomRight
                )
                helper.readBackgroundAttrs(this,
                    R.styleable.UiLinearLayout_bgIsGradient,
                    R.styleable.UiLinearLayout_bgGradientStart,
                    R.styleable.UiLinearLayout_bgGradientCenter,
                    R.styleable.UiLinearLayout_bgGradientEnd,
                    R.styleable.UiLinearLayout_bgColorLight,
                    R.styleable.UiLinearLayout_bgColorDark,
                    R.styleable.UiLinearLayout_bgColorAll,
                    R.styleable.UiLinearLayout_bgGdOrientation,
                    R.styleable.UiLinearLayout_bgGradientType,
                    R.styleable.UiLinearLayout_bgGradientCenterX,
                    R.styleable.UiLinearLayout_bgGradientCenterY,
                    R.styleable.UiLinearLayout_bgGradientRadius,
                    R.styleable.UiLinearLayout_bgGradientColors,
                    R.styleable.UiLinearLayout_bgColors,
                    R.styleable.UiLinearLayout_bgGdPositions
                )
                helper.readStrokeAttrs(this,
                    R.styleable.UiLinearLayout_strokeWidth,
                    R.styleable.UiLinearLayout_stColorLight,
                    R.styleable.UiLinearLayout_stColorDark,
                    R.styleable.UiLinearLayout_stColorAll,
                    R.styleable.UiLinearLayout_strokeDistance,
                    R.styleable.UiLinearLayout_distanceSpace,
                    R.styleable.UiLinearLayout_strokeGradient,
                    R.styleable.UiLinearLayout_strokeGdOrientation,
                    R.styleable.UiLinearLayout_strokeOption,
                    R.styleable.UiLinearLayout_strokeCap,
                    R.styleable.UiLinearLayout_stColors,
                    R.styleable.UiLinearLayout_stGdPositions
                )
                helper.readShadowAttrs(this,
                    R.styleable.UiLinearLayout_shadowColor,
                    R.styleable.UiLinearLayout_shadowRadius,
                    R.styleable.UiLinearLayout_shadowDx,
                    R.styleable.UiLinearLayout_shadowDy,
                    R.styleable.UiLinearLayout_shadowElevation
                )
                helper.readDimensionAttrs(this,
                    R.styleable.UiLinearLayout_uiDimenRatio,
                    R.styleable.UiLinearLayout_uiWidthPercent,
                    R.styleable.UiLinearLayout_uiHeightPercent,
                    R.styleable.UiLinearLayout_uiMaxWidthPercent,
                    R.styleable.UiLinearLayout_uiMaxHeightPercent,
                    R.styleable.UiLinearLayout_uiMinWidthPercent,
                    R.styleable.UiLinearLayout_uiMinHeightPercent
                )
                // New attrs
                helper.readShapeAttrs(this,
                    R.styleable.UiLinearLayout_shapeType,
                    R.styleable.UiLinearLayout_isCircle,
                    R.styleable.UiLinearLayout_aspectRatio
                )
                helper.readRippleAttrs(this,
                    R.styleable.UiLinearLayout_rippleEnabled,
                    R.styleable.UiLinearLayout_rippleColor,
                    R.styleable.UiLinearLayout_rippleBorderless
                )
                helper.readPressedAttrs(this,
                    R.styleable.UiLinearLayout_pressedBgColor,
                    R.styleable.UiLinearLayout_pressedScale
                )
                helper.readPaddingAttrs(this,
                    R.styleable.UiLinearLayout_paddingAll,
                    R.styleable.UiLinearLayout_paddingHorizontal,
                    R.styleable.UiLinearLayout_paddingVertical
                )
                helper.readOverlayAttrs(this,
                    R.styleable.UiLinearLayout_overlayColor
                )
                helper.readInnerShadowAttrs(this,
                    R.styleable.UiLinearLayout_innerShadowEnabled,
                    R.styleable.UiLinearLayout_innerShadowColor,
                    R.styleable.UiLinearLayout_innerShadowRadius
                )
                helper.readBorderStyleAttr(this, R.styleable.UiLinearLayout_borderStyle)
                helper.readCornerStyleAttr(this, R.styleable.UiLinearLayout_cornerStyle)
                helper.readChildGapAttr(this, R.styleable.UiLinearLayout_childGap)
            } finally {
                recycle()
            }
        }
        helper.setupShadow()
        helper.applyPadding()
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
        helper.drawOverlay(canvas, w, h)
        helper.drawInnerShadow(canvas, w, h)
        helper.drawStroke(canvas, w, h)
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
