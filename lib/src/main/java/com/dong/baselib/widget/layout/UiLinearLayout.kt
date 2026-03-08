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
                    R.styleable.UiLinearLayout_bgGradient,
                    R.styleable.UiLinearLayout_bgGradientStart,
                    R.styleable.UiLinearLayout_bgGradientCenter,
                    R.styleable.UiLinearLayout_bgGradientEnd,
                    R.styleable.UiLinearLayout_bgColor,
                    R.styleable.UiLinearLayout_bgGradientOrientation,
                    R.styleable.UiLinearLayout_bgGradientType,
                    R.styleable.UiLinearLayout_bgGradientCenterX,
                    R.styleable.UiLinearLayout_bgGradientCenterY,
                    R.styleable.UiLinearLayout_bgGradientRadius,
                    R.styleable.UiLinearLayout_bgGradientColors,
                    R.styleable.UiLinearLayout_bgColors,
                    R.styleable.UiLinearLayout_bgGradientPositions
                )
                helper.readStrokeAttrs(this,
                    R.styleable.UiLinearLayout_strokeWidth,
                    R.styleable.UiLinearLayout_strokeColor,
                    R.styleable.UiLinearLayout_strokeDashed,
                    R.styleable.UiLinearLayout_dashGap,
                    R.styleable.UiLinearLayout_strokeGradientColors,
                    R.styleable.UiLinearLayout_strokeGradientOrientation,
                    R.styleable.UiLinearLayout_strokeOption,
                    R.styleable.UiLinearLayout_strokeCap,
                    R.styleable.UiLinearLayout_strokeColors,
                    R.styleable.UiLinearLayout_strokeGradientPositions
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
                    R.styleable.UiLinearLayout_uiWidthParentPercent,
                    R.styleable.UiLinearLayout_uiHeightParentPercent,
                    R.styleable.UiLinearLayout_uiMaxWidthParentPercent,
                    R.styleable.UiLinearLayout_uiMaxHeightParentPercent,
                    R.styleable.UiLinearLayout_uiMinWidthParentPercent,
                    R.styleable.UiLinearLayout_uiMinHeightParentPercent,
                    R.styleable.UiLinearLayout_uiWidthScreenPercent,
                    R.styleable.UiLinearLayout_uiHeightScreenPercent,
                    R.styleable.UiLinearLayout_uiMaxWidthScreenPercent,
                    R.styleable.UiLinearLayout_uiMaxHeightScreenPercent,
                    R.styleable.UiLinearLayout_uiMinWidthScreenPercent,
                    R.styleable.UiLinearLayout_uiMinHeightScreenPercent
                )
                // New attrs
                helper.readShapeAttrs(this,
                    R.styleable.UiLinearLayout_shapeType,
                    R.styleable.UiLinearLayout_isCircle
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
