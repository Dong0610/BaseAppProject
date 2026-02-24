package com.dong.baselib.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.withSave
import com.dong.baselib.R
import com.dong.baselib.widget.layout.IUiLayout
import com.dong.baselib.widget.layout.UiLayoutHelper

enum class GradientOrientation {
    TOP_TO_BOTTOM, TR_BL, RIGHT_TO_LEFT, BR_TL,
    BOTTOM_TO_TOP, BL_TR, LEFT_TO_RIGHT, TL_BR
}
typealias TextGradientOrientation = GradientOrientation

enum class LineOption(val value: Int) {
    NONE(0), TOP(1), BOTTOM(2), CENTER(3);

    companion object {
        fun fromValue(value: Int): LineOption = entries.find { it.value == value } ?: NONE
    }
}

private data class Quad(val x0: Float, val y0: Float, val x1: Float, val y1: Float)

private fun GradientOrientation.toCoordinates(w: Float, h: Float) = when (this) {
    GradientOrientation.TOP_TO_BOTTOM -> Quad(0f, 0f, 0f, h)
    GradientOrientation.BOTTOM_TO_TOP -> Quad(0f, h, 0f, 0f)
    GradientOrientation.LEFT_TO_RIGHT -> Quad(0f, 0f, w, 0f)
    GradientOrientation.RIGHT_TO_LEFT -> Quad(w, 0f, 0f, 0f)
    GradientOrientation.TL_BR -> Quad(0f, 0f, w, h)
    GradientOrientation.TR_BL -> Quad(w, 0f, 0f, h)
    GradientOrientation.BL_TR -> Quad(0f, h, w, 0f)
    GradientOrientation.BR_TL -> Quad(w, h, 0f, 0f)
}

fun TextView.textGradient(
    startColor: Int,
    endColor: Int,
    orientation: GradientOrientation = GradientOrientation.LEFT_TO_RIGHT
) {
    val textStr = text?.toString().orEmpty()
    if (textStr.isEmpty()) {
        paint.shader = null
        return
    }
    val width = paint.measureText(textStr)
    val height = textSize * 1.3f
    val (x0, y0, x1, y1) = orientation.toCoordinates(width, height)

    paint.shader = LinearGradient(
        x0, y0, x1, y1,
        intArrayOf(startColor, endColor),
        null,
        Shader.TileMode.CLAMP
    )
    invalidate()
}
fun TextView.textGradient(
    listColor: IntArray,
    orientation: GradientOrientation = GradientOrientation.RIGHT_TO_LEFT
) {
    val textStr = text?.toString().orEmpty()
    if (textStr.isEmpty()) {
        paint.shader = null
        return
    }
    val width = paint.measureText(textStr)
    val height = textSize * 1.3f
    val (x0, y0, x1, y1) = orientation.toCoordinates(width, height)

    paint.shader = LinearGradient(
        x0, y0, x1, y1,
        listColor,
        null,
        Shader.TileMode.CLAMP
    )
    invalidate()
}

class UiTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), IUiLayout {

    override val helper = UiLayoutHelper(this)

    // Text-specific properties
    private var lineOption: LineOption = LineOption.NONE
    private var tColorLight = currentTextColor
    private var tColorDark = currentTextColor
    private var tColorHint = currentHintTextColor

    // Text gradient
    private var textGradient = false
    private var textGradientStart = Color.TRANSPARENT
    private var textGradientCenter = Color.TRANSPARENT
    private var textGradientEnd = Color.TRANSPARENT
    private var textGradientOrientation: GradientOrientation = GradientOrientation.LEFT_TO_RIGHT
    private var needReapplyTextGradient = false

    private val clipPath = Path()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiTextView).apply {
            try {
                // Common layout attrs via helper
                helper.readCornerAttrs(
                    this,
                    R.styleable.UiTextView_cornerRadius,
                    R.styleable.UiTextView_cornerTopLeft,
                    R.styleable.UiTextView_cornerTopRight,
                    R.styleable.UiTextView_cornerBottomLeft,
                    R.styleable.UiTextView_cornerBottomRight
                )
                helper.readBackgroundAttrs(
                    this,
                    R.styleable.UiTextView_bgIsGradient,
                    R.styleable.UiTextView_bgGradientStart,
                    R.styleable.UiTextView_bgGradientCenter,
                    R.styleable.UiTextView_bgGradientEnd,
                    R.styleable.UiTextView_bgColorLight,
                    R.styleable.UiTextView_bgColorDark,
                    R.styleable.UiTextView_bgColorAll,
                    R.styleable.UiTextView_bgGdOrientation,
                    R.styleable.UiTextView_bgGradientType,
                    R.styleable.UiTextView_bgGradientCenterX,
                    R.styleable.UiTextView_bgGradientCenterY,
                    R.styleable.UiTextView_bgGradientRadius,
                    R.styleable.UiTextView_bgGradientColors,
                    R.styleable.UiTextView_bgColors
                )
                helper.readStrokeAttrs(
                    this,
                    R.styleable.UiTextView_strokeWidth,
                    R.styleable.UiTextView_stColorLight,
                    R.styleable.UiTextView_stColorDark,
                    R.styleable.UiTextView_stColorAll,
                    R.styleable.UiTextView_strokeDashed,
                    R.styleable.UiTextView_distanceSpace,
                    R.styleable.UiTextView_strokeGradient,
                    R.styleable.UiTextView_strokeGdOrientation,
                    R.styleable.UiTextView_strokeOption,
                    -1,
                    R.styleable.UiTextView_stColors
                )
                helper.readShadowAttrs(
                    this,
                    R.styleable.UiTextView_shadowColor,
                    R.styleable.UiTextView_shadowRadius,
                    R.styleable.UiTextView_shadowDx,
                    R.styleable.UiTextView_shadowDy,
                    R.styleable.UiTextView_shadowElevation
                )

                // Text-specific attrs
                tColorLight = getColor(R.styleable.UiTextView_tvColorLight, currentTextColor)
                tColorDark = getColor(R.styleable.UiTextView_tvColorDark, currentTextColor)
                tColorHint = getColor(R.styleable.UiTextView_tvColorHint, currentHintTextColor)
                val tvColorAll = getColor(R.styleable.UiTextView_tvColor, Color.TRANSPARENT)
                if (tvColorAll != Color.TRANSPARENT) {
                    tColorLight = tvColorAll
                    tColorDark = tvColorAll
                }

                lineOption = LineOption.fromValue(getInt(R.styleable.UiTextView_lineOption, 0))

                // Text gradient
                textGradient = getBoolean(R.styleable.UiTextView_textGradient, false)
                textGradientStart = getColor(R.styleable.UiTextView_textGradientStart, Color.TRANSPARENT)
                textGradientCenter = getColor(R.styleable.UiTextView_textGradientCenter, Color.TRANSPARENT)
                textGradientEnd = getColor(R.styleable.UiTextView_textGradientEnd, Color.TRANSPARENT)
                textGradientOrientation = getInt(R.styleable.UiTextView_textGdOrientation, 6).toGradientOrientation()

                val wantSingleLine = getBoolean(R.styleable.UiTextView_tvSingleLine, false)
                if (wantSingleLine) {
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                }
            } finally {
                recycle()
            }
        }

        helper.setupShadow()
        applyTextStyles()
    }

    private fun Int.toGradientOrientation(): GradientOrientation =
        GradientOrientation.entries.toTypedArray().getOrElse(this) {
            GradientOrientation.LEFT_TO_RIGHT
        }

    private fun applyTextStyles() {
        // Clear line flags first
        paintFlags = paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv() and Paint.STRIKE_THRU_TEXT_FLAG.inv()

        // Apply line option
        when (lineOption) {
            LineOption.BOTTOM -> paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            LineOption.CENTER -> paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            LineOption.TOP, LineOption.NONE -> { /* TOP is drawn manually in onDraw, NONE does nothing */ }
        }

        setHintTextColor(tColorHint)

        if (textGradient) {
            applyTextGradient()
        } else {
            paint.shader = null
            setTextColor(if (helper.isDarkMode()) tColorDark else tColorLight)
        }
    }

    private fun applyTextGradient() {
        val textStr = text?.toString().orEmpty()
        if (textStr.isEmpty()) {
            needReapplyTextGradient = true
            paint.shader = null
            return
        }
        val width = paint.measureText(textStr)
        val height = textSize * 1.3f
        val (x0, y0, x1, y1) = textGradientOrientation.toCoordinates(width, height)
        paint.shader = LinearGradient(
            x0, y0, x1, y1,
            if (textGradientCenter != Color.TRANSPARENT) intArrayOf(
                textGradientStart,
                textGradientCenter,
                textGradientEnd
            ) else intArrayOf(textGradientStart, textGradientEnd),
            null,
            Shader.TileMode.CLAMP
        )
        invalidate()
        needReapplyTextGradient = false
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (textGradient) applyTextGradient()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        helper.onSizeChanged(w, h)
        if (textGradient || needReapplyTextGradient) applyTextGradient()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) {
            super.onDraw(canvas)
            return
        }
        helper.drawBackground(canvas, w, h)
        clipPath.reset()
        clipPath.addRoundRect(RectF(0f, 0f, w, h), helper.getCornerRadii(w, h), Path.Direction.CW)
        canvas.withSave {
            runCatching { canvas.clipPath(clipPath) }
            super.onDraw(canvas)
        }

        // Draw top line manually (no built-in Paint flag for this)
        if (lineOption == LineOption.TOP) {
            drawTopLine(canvas)
        }
    }

    private fun drawTopLine(canvas: Canvas) {
        val textStr = text?.toString().orEmpty()
        if (textStr.isEmpty()) return

        val textWidth = paint.measureText(textStr)
        val startX = when (gravity and android.view.Gravity.HORIZONTAL_GRAVITY_MASK) {
            android.view.Gravity.CENTER_HORIZONTAL -> (width - textWidth) / 2f
            android.view.Gravity.RIGHT, android.view.Gravity.END -> width - paddingEnd - textWidth
            else -> paddingStart.toFloat()
        }
        val endX = startX + textWidth

        // Position line at the top of the text (ascent line)
        val fontMetrics = paint.fontMetrics
        val baseline = when (gravity and android.view.Gravity.VERTICAL_GRAVITY_MASK) {
            android.view.Gravity.CENTER_VERTICAL -> (height - fontMetrics.bottom - fontMetrics.top) / 2f
            android.view.Gravity.BOTTOM -> height - paddingBottom - fontMetrics.bottom
            else -> paddingTop - fontMetrics.top
        }
        val lineY = baseline + fontMetrics.top

        val linePaint = Paint(paint).apply {
            style = Paint.Style.STROKE
            strokeWidth = textSize / 12f // Similar thickness to underline
        }
        canvas.drawLine(startX, lineY, endX, lineY, linePaint)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        helper.drawStroke(canvas, w, h)
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        invalidate()
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
    ) = apply {
        helper.compatElevationDp = elevationDp
        helper.cornerRadius = helper.dp(radiusDp)
        helper.compatShadowColor = color
        helper.shadowRadiusPx = shadowRadius
        helper.shadowDxPx = shadowDx
        helper.shadowDyPx = shadowDy
        helper.setupShadow()
        invalidateOutline()
    }

    // Text-specific fluent API
    fun setLineOption(option: LineOption) = apply {
        this.lineOption = option
        applyTextStyles()
        invalidate()
    }

    @Deprecated("Use setLineOption(LineOption.BOTTOM) instead", ReplaceWith("setLineOption(if (underline) LineOption.BOTTOM else LineOption.NONE)"))
    fun textUnderline(underline: Boolean) = apply {
        setLineOption(if (underline) LineOption.BOTTOM else LineOption.NONE)
    }

    fun setTextGradient(
        startColor: Int,
        centerColor: Int,
        endColor: Int,
        orientation: GradientOrientation = GradientOrientation.TOP_TO_BOTTOM
    ) = apply {
        textGradient = true
        textGradientStart = startColor
        textGradientCenter = centerColor
        textGradientEnd = endColor
        textGradientOrientation = orientation
        applyTextGradient()
    }

    fun clearTextGradient() = apply {
        textGradient = false
        paint.shader = null
        setTextColor(if (helper.isDarkMode()) tColorDark else tColorLight)
        invalidate()
    }

    fun textColorLight(color: Int) = apply {
        tColorLight = color
        if (!helper.isDarkMode() && !textGradient) setTextColor(color)
    }

    fun textColorDark(color: Int) = apply {
        tColorDark = color
        if (helper.isDarkMode() && !textGradient) setTextColor(color)
    }

    fun textColorAll(color: Int) = apply {
        tColorLight = color
        tColorDark = color
        if (!textGradient) setTextColor(color)
    }

    fun hintColor(color: Int) = apply {
        tColorHint = color
        setHintTextColor(color)
    }

    fun <T : UiTextView> applyStyle(block: T.() -> Unit): T {
        @Suppress("UNCHECKED_CAST")
        (this as T).block()
        return this
    }
}
