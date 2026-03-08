package com.dong.baselib.widget.layout

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import com.dong.baselib.R

class UiRowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), IUiLayout {

    override val helper = UiLayoutHelper(this)
    private var justifyContent: Int = 0

    init {
        setWillNotDraw(false)
        context.obtainStyledAttributes(attrs, R.styleable.UiRowLayout).apply {
            try {
                helper.readCornerAttrs(this,
                    R.styleable.UiRowLayout_cornerRadius,
                    R.styleable.UiRowLayout_cornerTopLeft,
                    R.styleable.UiRowLayout_cornerTopRight,
                    R.styleable.UiRowLayout_cornerBottomLeft,
                    R.styleable.UiRowLayout_cornerBottomRight
                )
                helper.readBackgroundAttrs(this,
                    R.styleable.UiRowLayout_bgGradient,
                    R.styleable.UiRowLayout_bgGradientStart,
                    R.styleable.UiRowLayout_bgGradientCenter,
                    R.styleable.UiRowLayout_bgGradientEnd,

                    R.styleable.UiRowLayout_bgColor,
                    R.styleable.UiRowLayout_bgGradientOrientation,
                    R.styleable.UiRowLayout_bgGradientType,
                    R.styleable.UiRowLayout_bgGradientCenterX,
                    R.styleable.UiRowLayout_bgGradientCenterY,
                    R.styleable.UiRowLayout_bgGradientRadius,
                    R.styleable.UiRowLayout_bgGradientColors,
                    R.styleable.UiRowLayout_bgColors,
                    R.styleable.UiRowLayout_bgGradientPositions
                )
                helper.readStrokeAttrs(this,
                    R.styleable.UiRowLayout_strokeWidth,
                    R.styleable.UiRowLayout_strokeColor,
                    R.styleable.UiRowLayout_strokeDashed,
                    R.styleable.UiRowLayout_dashGap,
                    R.styleable.UiRowLayout_strokeGradientColors,
                    R.styleable.UiRowLayout_strokeGradientOrientation,
                    R.styleable.UiRowLayout_strokeOption,
                    R.styleable.UiRowLayout_strokeCap,
                    R.styleable.UiRowLayout_strokeColors,
                    R.styleable.UiRowLayout_strokeGradientPositions
                )
                helper.readShadowAttrs(this,
                    R.styleable.UiRowLayout_shadowColor,
                    R.styleable.UiRowLayout_shadowRadius,
                    R.styleable.UiRowLayout_shadowDx,
                    R.styleable.UiRowLayout_shadowDy,
                    R.styleable.UiRowLayout_shadowElevation
                )
                helper.readDimensionAttrs(this,
                    R.styleable.UiRowLayout_uiDimenRatio,
                    R.styleable.UiRowLayout_uiWidthParentPercent,
                    R.styleable.UiRowLayout_uiHeightParentPercent,
                    R.styleable.UiRowLayout_uiMaxWidthParentPercent,
                    R.styleable.UiRowLayout_uiMaxHeightParentPercent,
                    R.styleable.UiRowLayout_uiMinWidthParentPercent,
                    R.styleable.UiRowLayout_uiMinHeightParentPercent,
                    R.styleable.UiRowLayout_uiWidthScreenPercent,
                    R.styleable.UiRowLayout_uiHeightScreenPercent,
                    R.styleable.UiRowLayout_uiMaxWidthScreenPercent,
                    R.styleable.UiRowLayout_uiMaxHeightScreenPercent,
                    R.styleable.UiRowLayout_uiMinWidthScreenPercent,
                    R.styleable.UiRowLayout_uiMinHeightScreenPercent
                )
                justifyContent = getInt(R.styleable.UiRowLayout_rowJustifyContent, 0)
                helper.readRippleAttrs(this,
                    R.styleable.UiRowLayout_rippleEnabled,
                    R.styleable.UiRowLayout_rippleColor,
                    R.styleable.UiRowLayout_rippleBorderless
                )
                helper.readShapeAttrs(this,
                    R.styleable.UiRowLayout_shapeType,
                    R.styleable.UiRowLayout_isCircle
                )
            } finally {
                recycle()
            }
        }
        helper.setupShadow()
        helper.setupRipple()
        orientation = HORIZONTAL
        updateJustifyContent()
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

    private fun updateJustifyContent() {
        gravity = when (justifyContent) {
            1 -> Gravity.CENTER_HORIZONTAL
            2 -> Gravity.END
            3, 4, 5 -> Gravity.START or Gravity.CENTER_VERTICAL
            else -> Gravity.START
        }
        requestLayout()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (justifyContent in 3..5) {
            val n = childCount
            if (n > 1) {
                val totalWidth = width - paddingStart - paddingEnd
                var totalChildrenWidth = 0
                for (i in 0 until n) totalChildrenWidth += getChildAt(i).measuredWidth

                if (justifyContent == 1) {
                    gravity = Gravity.CENTER
                } else {
                    val spaceBetween = when (justifyContent) {
                        3 -> (totalWidth - totalChildrenWidth) / (n - 1)
                        4 -> (totalWidth - totalChildrenWidth) / n
                        5 -> (totalWidth - totalChildrenWidth) / (n + 1)
                        else -> 0
                    }
                    var currentX = paddingStart
                    if (justifyContent == 5) currentX += spaceBetween
                    for (i in 0 until n) {
                        val child = getChildAt(i)
                        val wChild = child.measuredWidth
                        child.layout(currentX, child.top, currentX + wChild, child.bottom)
                        currentX += wChild + spaceBetween
                    }
                }
            }
        }
    }

    fun setJustifyContent(justify: Int) {
        justifyContent = justify
        updateJustifyContent()
        requestLayout()
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
        val save = canvas.save()
        canvas.clipPath(helper.getClipPath())
        super.dispatchDraw(canvas)
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
