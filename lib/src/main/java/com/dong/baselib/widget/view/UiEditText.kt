package com.dong.baselib.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withSave
import com.dong.baselib.R
import com.dong.baselib.utils.isValidHexColor
import kotlin.math.max

class UiEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    enum class BgGradientType { LINEAR, RADIAL, SWEEP }
    enum class DrawableAlign { START, END, TOP, BOTTOM }

    private var cornerRadius = 0f

    // Drawable state variables
    private var drawableEmpty: Drawable? = null
    private var drawableNotEmpty: Drawable? = null
    private var drawableAlign = DrawableAlign.END
    private var drawableSize = 0
    private var drawablePadding = 0
    private var drawableTint = Color.TRANSPARENT
    private var drawableEmptyTint = Color.TRANSPARENT
    private var drawableNotEmptyTint = Color.TRANSPARENT
    private var drawableEmptyVisible = true
    private var drawableNotEmptyVisible = true
    private var onDrawableEmptyClick: ((DrawableAlign) -> Unit)? = null
    private var onDrawableNotEmptyClick: ((DrawableAlign) -> Unit)? = null
    private var onDrawableClick: (() -> Unit)? = null
    private var currentDrawable: Drawable? = null
    private var isGradient = false
    private var bgGradientStart = Color.TRANSPARENT
    private var bgGradientCenter = Color.TRANSPARENT
    private var bgGradientEnd = Color.TRANSPARENT
    private var bgGradientOrientation = GradientOrientation.TOP_TO_BOTTOM
    private var bgGradientType = BgGradientType.LINEAR
    private var bgGradientCenterX = 0.5f
    private var bgGradientCenterY = 0.5f
    private var bgGradientRadius = 0f
    private var bgColorLight = Color.TRANSPARENT
    private var bgColorDark = Color.TRANSPARENT
    private var focusBgColorLight = Color.TRANSPARENT
    private var focusBgColorDark = Color.TRANSPARENT
    private var isFocusGradient = false
    private var focusBgGradientStart = Color.TRANSPARENT
    private var focusBgGradientCenter = Color.TRANSPARENT
    private var focusBgGradientEnd = Color.TRANSPARENT
    private var focusBgGradientOrientation = GradientOrientation.TOP_TO_BOTTOM
    private var focusBgGradientType = BgGradientType.LINEAR
    private var focusBgGradientCenterX = 0.5f
    private var focusBgGradientCenterY = 0.5f
    private var focusBgGradientRadius = 0f
    private var stWidth = 0f
    private var stColorLight = Color.TRANSPARENT
    private var stColorDark = Color.TRANSPARENT
    private var strokeGradient: IntArray? = null
    private var stColors: IntArray? = null
    private var bgColors: IntArray? = null
    private var strokeGradientOrientation = GradientOrientation.LEFT_TO_RIGHT
    private var isDashed = false
    private var dashSpace = 10f
    private var focusStWidth = 0f
    private var focusStColorLight = Color.TRANSPARENT
    private var focusStColorDark = Color.TRANSPARENT
    private var focusStrokeGradient: IntArray? = null
    private var focusStrokeGradientOrientation = GradientOrientation.LEFT_TO_RIGHT
    private var isFocusDashed = false
    private var focusDashSpace = 10f
    private var lineOption: LineOption = LineOption.NONE
    private var tColorLight = currentTextColor
    private var tColorDark = currentTextColor
    private var tColorHint = currentHintTextColor
    private var textGradient = false
    private var textGradientStart = Color.TRANSPARENT
    private var textGradientEnd = Color.TRANSPARENT
    private var textGradientOrientation = TextGradientOrientation.LEFT_TO_RIGHT
    private val clipPath = Path()
    private val strokeRectF = RectF()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiEditText).apply {
            try {
                cornerRadius = getDimension(R.styleable.UiEditText_cornerRadius, 0f)

                isGradient = getBoolean(R.styleable.UiEditText_bgIsGradient, false)
                bgGradientStart =
                    getColor(R.styleable.UiEditText_bgGradientStart, Color.TRANSPARENT)
                bgGradientCenter =
                    getColor(R.styleable.UiEditText_bgGradientCenter, Color.TRANSPARENT)
                bgGradientEnd = getColor(R.styleable.UiEditText_bgGradientEnd, Color.TRANSPARENT)
                bgColorLight = getColor(R.styleable.UiEditText_bgColorLight, Color.TRANSPARENT)
                bgColorDark = getColor(R.styleable.UiEditText_bgColorDark, Color.TRANSPARENT)
                bgGradientOrientation =
                    getInt(R.styleable.UiEditText_bgGdOrientation, 0).toGradientOrientation()
                bgGradientType = getInt(R.styleable.UiEditText_bgGradientType, 0).toBgGradientType()
                bgGradientCenterX = getFloat(R.styleable.UiEditText_bgGradientCenterX, 0.5f)
                bgGradientCenterY = getFloat(R.styleable.UiEditText_bgGradientCenterY, 0.5f)
                bgGradientRadius = getDimension(R.styleable.UiEditText_bgGradientRadius, 0f)
                val bgColorAll = getColor(R.styleable.UiEditText_bgColorAll, Color.TRANSPARENT)
                if (bgColorAll != Color.TRANSPARENT) {
                    bgColorLight = bgColorAll
                    bgColorDark = bgColorAll
                }
                // Read bgColors from string
                val bgGdColorsStr = getString(R.styleable.UiEditText_bgGradientColors)
                bgColors = bgGdColorsStr?.split(" ")
                    ?.mapNotNull { if (it.isValidHexColor()) it.toColorInt() else null }
                    ?.toIntArray()
                // Read bgColors from integer-array reference (higher priority)
                val bgColorsResId = getResourceId(R.styleable.UiEditText_bgColors, 0)
                if (bgColorsResId != 0) {
                    bgColors = resources.getIntArray(bgColorsResId)
                }

                focusBgColorLight = getColor(R.styleable.UiEditText_focusBgColorLight, bgColorLight)
                focusBgColorDark = getColor(R.styleable.UiEditText_focusBgColorDark, bgColorDark)
                isFocusGradient = getBoolean(R.styleable.UiEditText_focusBgIsGradient, false)
                focusBgGradientStart =
                    getColor(R.styleable.UiEditText_focusBgGradientStart, Color.TRANSPARENT)
                focusBgGradientCenter =
                    getColor(R.styleable.UiEditText_focusBgGradientCenter, Color.TRANSPARENT)
                focusBgGradientEnd =
                    getColor(R.styleable.UiEditText_focusBgGradientEnd, Color.TRANSPARENT)
                val focusBgColorAll =
                    getColor(R.styleable.UiEditText_focusBgColorAll, Color.TRANSPARENT)
                if (focusBgColorAll != Color.TRANSPARENT) {
                    focusBgColorLight = focusBgColorAll
                    focusBgColorDark = focusBgColorAll
                }
                focusBgGradientOrientation = bgGradientOrientation
                focusBgGradientType = getInt(
                    R.styleable.UiEditText_focusBgGradientType,
                    bgGradientType.ordinal
                ).toBgGradientType()
                focusBgGradientCenterX =
                    getFloat(R.styleable.UiEditText_focusBgGradientCenterX, bgGradientCenterX)
                focusBgGradientCenterY =
                    getFloat(R.styleable.UiEditText_focusBgGradientCenterY, bgGradientCenterY)
                focusBgGradientRadius =
                    getDimension(R.styleable.UiEditText_focusBgGradientRadius, bgGradientRadius)

                stWidth = getDimension(R.styleable.UiEditText_strokeWidth, 0f)
                stColorLight = getColor(R.styleable.UiEditText_stColorLight, Color.TRANSPARENT)
                stColorDark = getColor(R.styleable.UiEditText_stColorDark, Color.TRANSPARENT)
                val stColorAll = getColor(R.styleable.UiEditText_stColorAll, Color.TRANSPARENT)
                if (stColorAll != Color.TRANSPARENT) {
                    stColorLight = stColorAll
                    stColorDark = stColorAll
                }
                isDashed = getBoolean(R.styleable.UiEditText_strokeDistance, false)
                dashSpace = getDimension(R.styleable.UiEditText_distanceSpace, 10f)
                val strokeGdColors = getString(R.styleable.UiEditText_strokeGradient)
                strokeGradient = strokeGdColors?.split(" ")
                    ?.mapNotNull { if (it.isValidHexColor()) Color.parseColor(it) else null }
                    ?.toIntArray()
                // Read stColors from integer-array reference (higher priority)
                val stColorsResId = getResourceId(R.styleable.UiEditText_stColors, 0)
                if (stColorsResId != 0) {
                    stColors = resources.getIntArray(stColorsResId)
                }

                strokeGradientOrientation =
                    getInt(R.styleable.UiEditText_strokeGdOrientation, 6).toStrokeOrientation()

                focusStWidth = getDimension(R.styleable.UiEditText_focusStrokeWidth, stWidth)
                focusStColorLight = getColor(R.styleable.UiEditText_focusStColorLight, stColorLight)
                focusStColorDark = getColor(R.styleable.UiEditText_focusStColorDark, stColorDark)
                val focusStColorAll =
                    getColor(R.styleable.UiEditText_focusStColorAll, Color.TRANSPARENT)
                focusStrokeGradientOrientation = strokeGradientOrientation
                if (focusStColorAll != Color.TRANSPARENT) {
                    focusStColorLight = focusStColorAll
                    focusStColorDark = focusStColorAll
                }
                isFocusDashed = getBoolean(R.styleable.UiEditText_focusStrokeDistance, isDashed)
                focusDashSpace = getDimension(R.styleable.UiEditText_focusDistanceSpace, dashSpace)
                val focusStrokeGdColors = getString(R.styleable.UiEditText_focusStrokeGradient)
                focusStrokeGradient = focusStrokeGdColors?.split(" ")
                    ?.mapNotNull { if (it.isValidHexColor()) it.toColorInt() else null }
                    ?.toIntArray()

                tColorLight = getColor(R.styleable.UiEditText_edtColorLight, currentTextColor)
                tColorDark = getColor(R.styleable.UiEditText_edtColorDark, currentTextColor)
                tColorHint = getColor(R.styleable.UiEditText_edtColorHint, currentHintTextColor)
                val tvColor = getColor(R.styleable.UiEditText_edtColor, Color.TRANSPARENT)
                if (tvColor != Color.TRANSPARENT) {
                    tColorLight = tvColor
                    tColorDark = tvColor
                }

                lineOption = LineOption.fromValue(getInt(R.styleable.UiEditText_lineOption, 0))
                textGradient = getBoolean(R.styleable.UiEditText_textGradient, false)
                textGradientStart =
                    getColor(R.styleable.UiEditText_textGradientStart, Color.TRANSPARENT)
                textGradientEnd =
                    getColor(R.styleable.UiEditText_textGradientEnd, Color.TRANSPARENT)
                textGradientOrientation =
                    getInt(R.styleable.UiEditText_textGdOrientation, 6).toTextOrientation()

                // Drawable attributes
                drawableEmpty = getDrawable(R.styleable.UiEditText_drawableEmpty)
                drawableNotEmpty = getDrawable(R.styleable.UiEditText_drawableNotEmpty)
                drawableAlign = getInt(R.styleable.UiEditText_drawableAlign, 1).toDrawableAlign()
                drawableSize = getDimensionPixelSize(R.styleable.UiEditText_drawableSize, 0)
                drawablePadding = getDimensionPixelSize(R.styleable.UiEditText_drawablePadding, 0)
                drawableTint = getColor(R.styleable.UiEditText_drawableTint, Color.TRANSPARENT)
                drawableEmptyTint = getColor(R.styleable.UiEditText_drawableEmptyTint, drawableTint)
                drawableNotEmptyTint = getColor(R.styleable.UiEditText_drawableNotEmptyTint, drawableTint)
                drawableEmptyVisible = getBoolean(R.styleable.UiEditText_drawableEmptyVisible, true)
                drawableNotEmptyVisible = getBoolean(R.styleable.UiEditText_drawableNotEmptyVisible, true)
            }
            finally {
                recycle()
            }
        }

        applyTextStyles()
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        isLongClickable = true

        applyTextStyles()
        setupDrawableTextWatcher()
        updateDrawableState()
    }

    private fun Int.toDrawableAlign() = DrawableAlign.entries.toTypedArray()
        .getOrElse(this) { DrawableAlign.END }

    private fun setupDrawableTextWatcher() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateDrawableState()
            }
        })
    }

    private fun updateDrawableState() {
        val isEmpty = text.isNullOrEmpty()
        val visible = if (isEmpty) drawableEmptyVisible else drawableNotEmptyVisible
        val newDrawable = if (isEmpty) drawableEmpty else drawableNotEmpty

        currentDrawable = newDrawable?.mutate()

        // Apply size if specified
        currentDrawable?.let { drawable ->
            if (drawableSize > 0) {
                drawable.setBounds(0, 0, drawableSize, drawableSize)
            } else {
                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            }

            // When not visible, keep drawable for space but make it invisible
            drawable.alpha = if (visible) 255 else 0

            // Apply tint only when visible
            if (visible) {
                val tint = if (isEmpty) {
                    if (drawableEmptyTint != Color.TRANSPARENT) drawableEmptyTint else drawableTint
                } else {
                    if (drawableNotEmptyTint != Color.TRANSPARENT) drawableNotEmptyTint else drawableTint
                }
                if (tint != Color.TRANSPARENT) {
                    DrawableCompat.setTint(drawable, tint)
                }
            }
        }

        applyDrawable()
    }

    private fun applyDrawable() {
        val (start, top, end, bottom) = when (drawableAlign) {
            DrawableAlign.START -> arrayOf(currentDrawable, null, null, null)
            DrawableAlign.END -> arrayOf(null, null, currentDrawable, null)
            DrawableAlign.TOP -> arrayOf(null, currentDrawable, null, null)
            DrawableAlign.BOTTOM -> arrayOf(null, null, null, currentDrawable)
        }
        setCompoundDrawablesRelative(start, top, end, bottom)
        if (drawablePadding > 0) {
            compoundDrawablePadding = drawablePadding
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && currentDrawable != null) {
            val drawableBounds = currentDrawable?.bounds ?: return super.onTouchEvent(event)
            val touchX = event.x.toInt()
            val touchY = event.y.toInt()

            val isDrawableClicked = when (drawableAlign) {
                DrawableAlign.END -> {
                    val drawableStart = width - paddingEnd - drawableBounds.width() - compoundDrawablePadding
                    touchX >= drawableStart
                }
                DrawableAlign.START -> {
                    val drawableEnd = paddingStart + drawableBounds.width() + compoundDrawablePadding
                    touchX <= drawableEnd
                }
                DrawableAlign.TOP -> {
                    val drawableBottom = paddingTop + drawableBounds.height() + compoundDrawablePadding
                    touchY <= drawableBottom
                }
                DrawableAlign.BOTTOM -> {
                    val drawableTop = height - paddingBottom - drawableBounds.height() - compoundDrawablePadding
                    touchY >= drawableTop
                }
            }

            if (isDrawableClicked) {
                val isEmpty = text.isNullOrEmpty()
                val visible = if (isEmpty) drawableEmptyVisible else drawableNotEmptyVisible
                if (visible) {
                    when {
                        isEmpty && onDrawableEmptyClick != null -> {
                            onDrawableEmptyClick?.invoke(drawableAlign)
                            return true
                        }
                        !isEmpty && onDrawableNotEmptyClick != null -> {
                            onDrawableNotEmptyClick?.invoke(drawableAlign)
                            return true
                        }
                        onDrawableClick != null -> {
                            onDrawableClick?.invoke()
                            return true
                        }
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private var hasFocusState = false
    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        hasFocusState = gainFocus
        invalidate()
    }

    private fun applyTextStyles() {
        if (textGradient) {
            setGradient(textGradientStart, textGradientEnd, textGradientOrientation)
        } else {
            setTextColor(if (isDarkMode()) tColorDark else tColorLight)
        }

        setHintTextColor(tColorHint)

        // Clear line flags first
        paintFlags = paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv() and Paint.STRIKE_THRU_TEXT_FLAG.inv()

        // Apply line option
        when (lineOption) {
            LineOption.BOTTOM -> paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            LineOption.CENTER -> paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            LineOption.TOP, LineOption.NONE -> { /* TOP drawn manually, NONE does nothing */ }
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) {
            super.onDraw(canvas)
            return
        }
        val focused = super.isFocused
        val radius = minOf(w / 2f, h / 2f, cornerRadius)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        if (focused && isFocusGradient) {
            val colors = if (focusBgGradientCenter != Color.TRANSPARENT) {
                intArrayOf(focusBgGradientStart, focusBgGradientCenter, focusBgGradientEnd)
            } else {
                intArrayOf(focusBgGradientStart, focusBgGradientEnd)
            }
            bgPaint.shader = createGradientShader(
                w,
                h,
                colors,
                focusBgGradientType,
                focusBgGradientOrientation,
                focusBgGradientCenterX,
                focusBgGradientCenterY,
                focusBgGradientRadius
            )
        } else if (bgColors != null && bgColors!!.size >= 2) {
            // Use bgColors array (highest priority)
            bgPaint.shader = createGradientShader(
                w,
                h,
                bgColors!!,
                bgGradientType,
                bgGradientOrientation,
                bgGradientCenterX,
                bgGradientCenterY,
                bgGradientRadius
            )
        } else if (isGradient) {
            val colors = if (bgGradientCenter != Color.TRANSPARENT) {
                intArrayOf(bgGradientStart, bgGradientCenter, bgGradientEnd)
            } else {
                intArrayOf(bgGradientStart, bgGradientEnd)
            }
            bgPaint.shader = createGradientShader(
                w,
                h,
                colors,
                bgGradientType,
                bgGradientOrientation,
                bgGradientCenterX,
                bgGradientCenterY,
                bgGradientRadius
            )
        } else {
            bgPaint.color = if (focused) {
                if (isDarkMode()) focusBgColorDark else focusBgColorLight
            } else {
                if (isDarkMode()) bgColorDark else bgColorLight
            }
        }

        // Translate canvas to compensate for scroll offset so background stays fixed
        canvas.withSave {
            translate(scrollX.toFloat(), scrollY.toFloat())
            drawRoundRect(0f, 0f, w, h, radius, radius, bgPaint)
            clipPath.reset()
            clipPath.addRoundRect(RectF(0f, 0f, w, h), radius, radius, Path.Direction.CW)
            runCatching { clipPath(this@UiEditText.clipPath) }
        }
        super.onDraw(canvas)

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
            strokeWidth = textSize / 12f
        }
        canvas.drawLine(startX, lineY, endX, lineY, linePaint)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) {
            super.dispatchDraw(canvas)
            return
        }
        val radius = minOf(w / 2f, h / 2f, cornerRadius)
        val focused = super.isFocused
        val currentStWidth = if (focused) focusStWidth else stWidth
        if (currentStWidth > 0f) {
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = currentStWidth
                strokeJoin = Paint.Join.ROUND
                val dashed = if (focused) isFocusDashed else isDashed
                val space = if (focused) focusDashSpace else dashSpace
                if (dashed) {
                    pathEffect = DashPathEffect(floatArrayOf(space, space), 0f)
                }
                // Priority: stColors > strokeGradient > solid color
                val gradColors = stColors ?: (if (focused) focusStrokeGradient else strokeGradient)
                val ori = if (focused) focusStrokeGradientOrientation else strokeGradientOrientation
                if (gradColors != null && gradColors.size >= 2) {
                    val (x0, y0, x1, y1) = ori.toCoordinates(w, h)
                    shader = LinearGradient(x0, y0, x1, y1, gradColors, null, Shader.TileMode.CLAMP)
                } else {
                    color = if (focused) {
                        if (isDarkMode()) focusStColorDark else focusStColorLight
                    } else {
                        if (isDarkMode()) stColorDark else stColorLight
                    }
                }
            }
            val inset = currentStWidth / 2f
            // Translate canvas to compensate for scroll offset so border stays fixed
            canvas.withSave {
                translate(scrollX.toFloat(), scrollY.toFloat())
                strokeRectF.set(inset, inset, w - inset, h - inset)
                val strokeRadius = max(0f, radius - inset)
                drawRoundRect(strokeRectF, strokeRadius, strokeRadius, strokePaint)
            }
        }
    }

    private fun isDarkMode(): Boolean {
        return (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    private fun createGradientShader(
        w: Float, h: Float, colors: IntArray,
        type: BgGradientType, orientation: GradientOrientation,
        centerX: Float, centerY: Float, radius: Float
    ): Shader {
        return when (type) {
            BgGradientType.LINEAR -> {
                val (x0, y0, x1, y1) = orientation.toCoordinates(w, h)
                LinearGradient(x0, y0, x1, y1, colors, null, Shader.TileMode.CLAMP)
            }
            BgGradientType.RADIAL -> {
                val cx = w * centerX
                val cy = h * centerY
                val r = if (radius > 0f) radius else kotlin.math.max(w, h) / 2f
                RadialGradient(cx, cy, r, colors, null, Shader.TileMode.CLAMP)
            }
            BgGradientType.SWEEP -> {
                val cx = w * centerX
                val cy = h * centerY
                SweepGradient(cx, cy, colors, null)
            }
        }
    }

    fun setGradient(startColor: Int, endColor: Int, orientation: TextGradientOrientation) {
        val width = paint.measureText(text.toString())
        val height = textSize * 1.3f
        val (x0, y0, x1, y1) = orientation.toCoordinates(width, height)

        paint.shader = LinearGradient(x0, y0, x1, y1, startColor, endColor, Shader.TileMode.CLAMP)
        invalidate()
    }

    enum class TextGradientOrientation {
        TOP_TO_BOTTOM, TR_BL, RIGHT_TO_LEFT, BR_TL,
        BOTTOM_TO_TOP, BL_TR, LEFT_TO_RIGHT, TL_BR
    }

    enum class GradientOrientation {
        TOP_TO_BOTTOM, TR_BL, RIGHT_TO_LEFT, BR_TL,
        BOTTOM_TO_TOP, BL_TR, LEFT_TO_RIGHT, TL_BR
    }

    private fun Int.toTextOrientation() = TextGradientOrientation.entries.toTypedArray()
        .getOrElse(this) { TextGradientOrientation.LEFT_TO_RIGHT }

    private fun Int.toStrokeOrientation() = UiEditText.GradientOrientation.entries.toTypedArray()
        .getOrElse(this) { GradientOrientation.LEFT_TO_RIGHT }

    private fun Int.toGradientOrientation() = UiEditText.GradientOrientation.entries.toTypedArray()
        .getOrElse(this) { GradientOrientation.TOP_TO_BOTTOM }

    private fun Int.toBgGradientType() = BgGradientType.entries.toTypedArray()
        .getOrElse(this) { BgGradientType.LINEAR }

    private fun TextGradientOrientation.toCoordinates(w: Float, h: Float) = when (this) {
        TextGradientOrientation.TOP_TO_BOTTOM -> Quad(0f, 0f, 0f, h)
        TextGradientOrientation.BOTTOM_TO_TOP -> Quad(0f, h, 0f, 0f)
        TextGradientOrientation.LEFT_TO_RIGHT -> Quad(0f, 0f, w, 0f)
        TextGradientOrientation.RIGHT_TO_LEFT -> Quad(w, 0f, 0f, 0f)
        TextGradientOrientation.TL_BR -> Quad(0f, 0f, w, h)
        TextGradientOrientation.TR_BL -> Quad(w, 0f, 0f, h)
        TextGradientOrientation.BL_TR -> Quad(0f, h, w, 0f)
        TextGradientOrientation.BR_TL -> Quad(w, h, 0f, 0f)
    }

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

    private data class Quad(val x0: Float, val y0: Float, val x1: Float, val y1: Float)

    private fun String.isValidHexColor(): Boolean {
        return this.matches(Regex("^#?[0-9a-fA-F]{6,8}$"))
    }

    fun cornerRadius(radius: Float) = apply {
        cornerRadius = radius
        invalidate()
    }

    fun backgroundLight(color: Int) = apply {
        isGradient = false
        bgColorLight = color
        invalidate()
    }

    fun backgroundDark(color: Int) = apply {
        isGradient = false
        bgColorDark = color
        invalidate()
    }

    fun backgroundAll(color: Int) = apply {
        isGradient = false
        bgColorLight = color
        bgColorDark = color
        invalidate()
    }

    fun backgroundGradientStart(color: Int) = apply {
        isGradient = true
        bgGradientStart = color
        invalidate()
    }

    fun backgroundGradientCenter(color: Int) = apply {
        isGradient = true
        bgGradientCenter = color
        invalidate()
    }

    fun backgroundGradient(start: Int, end: Int, center: Int) = apply {
        isGradient = true
        bgGradientStart = start
        bgGradientCenter = center
        bgGradientEnd = end
        invalidate()
    }

    fun backgroundGradientEnd(color: Int) = apply {
        isGradient = true
        bgGradientEnd = color
        invalidate()
    }

    fun backgroundOrientation(orientation: GradientOrientation) = apply {
        isGradient = true
        bgGradientOrientation = orientation
        invalidate()
    }

    fun backgroundGradientType(type: BgGradientType) = apply {
        isGradient = true
        bgGradientType = type
        invalidate()
    }

    fun linearGradient() = backgroundGradientType(BgGradientType.LINEAR)

    fun radialGradient() = backgroundGradientType(BgGradientType.RADIAL)

    fun sweepGradient() = backgroundGradientType(BgGradientType.SWEEP)

    fun gradientCenter(centerX: Float, centerY: Float) = apply {
        bgGradientCenterX = centerX
        bgGradientCenterY = centerY
        invalidate()
    }

    fun gradientRadius(radius: Float) = apply {
        bgGradientRadius = radius
        invalidate()
    }

    fun radialGradient(centerX: Float, centerY: Float, radius: Float = 0f) = apply {
        isGradient = true
        bgGradientType = BgGradientType.RADIAL
        bgGradientCenterX = centerX
        bgGradientCenterY = centerY
        bgGradientRadius = radius
        invalidate()
    }

    fun sweepGradient(centerX: Float, centerY: Float) = apply {
        isGradient = true
        bgGradientType = BgGradientType.SWEEP
        bgGradientCenterX = centerX
        bgGradientCenterY = centerY
        invalidate()
    }

    fun focusBackgroundLight(color: Int) = apply {
        isFocusGradient = false
        focusBgColorLight = color
        invalidate()
    }

    fun focusBackgroundDark(color: Int) = apply {
        isFocusGradient = false
        focusBgColorDark = color
        invalidate()
    }

    fun focusBackgroundAll(color: Int) = apply {
        isFocusGradient = false
        focusBgColorLight = color
        focusBgColorDark = color
        invalidate()
    }

    fun focusBackgroundGradientStart(color: Int) = apply {
        isFocusGradient = true
        focusBgGradientStart = color
        invalidate()
    }

    fun focusBackgroundGradientCenter(color: Int) = apply {
        isFocusGradient = true
        focusBgGradientCenter = color
        invalidate()
    }

    fun focusBackgroundGradient(start: Int, end: Int, center: Int) = apply {
        isFocusGradient = true
        focusBgGradientStart = start
        focusBgGradientCenter = center
        focusBgGradientEnd = end
        invalidate()
    }

    fun focusBackgroundGradientEnd(color: Int) = apply {
        isFocusGradient = true
        focusBgGradientEnd = color
        invalidate()
    }

    fun focusBackgroundOrientation(orientation: GradientOrientation) = apply {
        isFocusGradient = true
        focusBgGradientOrientation = orientation
        invalidate()
    }

    fun focusBackgroundGradientType(type: BgGradientType) = apply {
        isFocusGradient = true
        focusBgGradientType = type
        invalidate()
    }

    fun focusLinearGradient() = focusBackgroundGradientType(BgGradientType.LINEAR)

    fun focusRadialGradient() = focusBackgroundGradientType(BgGradientType.RADIAL)

    fun focusSweepGradient() = focusBackgroundGradientType(BgGradientType.SWEEP)

    fun focusGradientCenter(centerX: Float, centerY: Float) = apply {
        focusBgGradientCenterX = centerX
        focusBgGradientCenterY = centerY
        invalidate()
    }

    fun focusGradientRadius(radius: Float) = apply {
        focusBgGradientRadius = radius
        invalidate()
    }

    fun focusRadialGradient(centerX: Float, centerY: Float, radius: Float = 0f) = apply {
        isFocusGradient = true
        focusBgGradientType = BgGradientType.RADIAL
        focusBgGradientCenterX = centerX
        focusBgGradientCenterY = centerY
        focusBgGradientRadius = radius
        invalidate()
    }

    fun focusSweepGradient(centerX: Float, centerY: Float) = apply {
        isFocusGradient = true
        focusBgGradientType = BgGradientType.SWEEP
        focusBgGradientCenterX = centerX
        focusBgGradientCenterY = centerY
        invalidate()
    }

    fun strokeWidth(width: Float) = apply {
        stWidth = width
        invalidate()
    }

    fun strokeLight(color: Int) = apply {
        strokeGradient = null
        stColorLight = color
        invalidate()
    }

    fun strokeDark(color: Int) = apply {
        strokeGradient = null
        stColorDark = color
        invalidate()
    }

    fun strokeColor(color: Int) = apply {
        strokeGradient = null
        stColorDark = color
        stColorLight = color
        invalidate()
    }

    fun strokeDashed(dashed: Boolean, spacing: Float = 10f) = apply {
        isDashed = dashed
        dashSpace = spacing
        invalidate()
    }

    fun strokeGradientColors(colors: IntArray) = apply {
        strokeGradient = colors
        invalidate()
    }

    fun strokeOrientation(orientation: GradientOrientation) = apply {
        strokeGradientOrientation = orientation
        invalidate()
    }

    fun focusStrokeWidth(width: Float) = apply {
        focusStWidth = width
        invalidate()
    }

    fun focusStrokeLight(color: Int) = apply {
        focusStrokeGradient = null
        focusStColorLight = color
        invalidate()
    }

    fun focusStrokeDark(color: Int) = apply {
        focusStrokeGradient = null
        focusStColorDark = color
        invalidate()
    }

    fun focusStrokeColor(color: Int) = apply {
        focusStrokeGradient = null
        focusStColorDark = color
        focusStColorLight = color
        invalidate()
    }

    fun focusStrokeDashed(dashed: Boolean, spacing: Float = 10f) = apply {
        isFocusDashed = dashed
        focusDashSpace = spacing
        invalidate()
    }

    fun focusStrokeGradientColors(colors: IntArray) = apply {
        focusStrokeGradient = colors
        invalidate()
    }

    fun focusStrokeOrientation(orientation: GradientOrientation) = apply {
        focusStrokeGradientOrientation = orientation
        invalidate()
    }

    fun rounded(radius: Float) = apply {
        cornerRadius(radius)
    }

    fun solidBackground(light: Int, dark: Int = light) = apply {
        isGradient = false
        bgColorLight = light
        bgColorDark = dark
        invalidate()
    }

    fun backgroundGradient(vararg colors: Int) = apply {
        isGradient = true
        when (colors.size) {
            0 -> { /* no-op */
            }
            1 -> {
                bgGradientStart = colors[0]; bgGradientCenter = Color.TRANSPARENT; bgGradientEnd =
                    Color.TRANSPARENT
            }
            2 -> {
                bgGradientStart = colors[0]; bgGradientCenter = Color.TRANSPARENT; bgGradientEnd =
                    colors[1]
            }
            else -> {
                bgGradientStart = colors[0]; bgGradientCenter = colors[1]; bgGradientEnd = colors[2]
            }
        }
        invalidate()
    }

    fun focusBackgroundGradient(vararg colors: Int) = apply {
        isFocusGradient = true
        when (colors.size) {
            0 -> { /* no-op */
            }
            1 -> {
                focusBgGradientStart = colors[0]; focusBgGradientCenter =
                    Color.TRANSPARENT; focusBgGradientEnd =
                    Color.TRANSPARENT
            }
            2 -> {
                focusBgGradientStart = colors[0]; focusBgGradientCenter =
                    Color.TRANSPARENT; focusBgGradientEnd =
                    colors[1]
            }
            else -> {
                focusBgGradientStart = colors[0]; focusBgGradientCenter =
                    colors[1]; focusBgGradientEnd = colors[2]
            }
        }
        invalidate()
    }

    fun strokeGradient(vararg colors: Int) = apply {
        strokeGradient = if (colors.isNotEmpty()) colors.copyOf() else null
        invalidate()
    }

    fun focusStrokeGradient(vararg colors: Int) = apply {
        focusStrokeGradient = if (colors.isNotEmpty()) colors.copyOf() else null
        invalidate()
    }

    fun dashed(spacing: Float = 10f) = apply {
        return strokeDashed(true, spacing)
    }

    fun focusDashed(spacing: Float = 10f) = apply {
        return focusStrokeDashed(true, spacing)
    }

    fun applyStyle(block: UiEditText.() -> Unit) = apply {
        block()
    }

    fun edtSingleLine(single: Boolean = true) = apply {
        isSingleLine = single
        if (single) {
            maxLines = 1
            setHorizontallyScrolling(true)
        }
        invalidate()
    }

    fun setLineOption(option: LineOption) = apply {
        this.lineOption = option
        applyTextStyles()
        invalidate()
    }

    // ==================== Drawable Functions ====================

    /**
     * Set drawable shown when text is empty
     */
    fun drawableEmpty(drawable: Drawable?) = apply {
        this.drawableEmpty = drawable
        updateDrawableState()
    }

    /**
     * Set drawable shown when text is empty (by resource id)
     */
    fun drawableEmpty(drawableRes: Int) = apply {
        this.drawableEmpty = ContextCompat.getDrawable(context, drawableRes)
        updateDrawableState()
    }

    /**
     * Set drawable shown when text is not empty
     */
    fun drawableNotEmpty(drawable: Drawable?) = apply {
        this.drawableNotEmpty = drawable
        updateDrawableState()
    }

    /**
     * Set drawable shown when text is not empty (by resource id)
     */
    fun drawableNotEmpty(drawableRes: Int) = apply {
        this.drawableNotEmpty = ContextCompat.getDrawable(context, drawableRes)
        updateDrawableState()
    }

    /**
     * Set both drawables at once
     */
    fun drawables(empty: Drawable?, notEmpty: Drawable?) = apply {
        this.drawableEmpty = empty
        this.drawableNotEmpty = notEmpty
        updateDrawableState()
    }

    /**
     * Set both drawables at once (by resource id)
     */
    fun drawables(emptyRes: Int, notEmptyRes: Int) = apply {
        this.drawableEmpty = ContextCompat.getDrawable(context, emptyRes)
        this.drawableNotEmpty = ContextCompat.getDrawable(context, notEmptyRes)
        updateDrawableState()
    }

    /**
     * Set drawable alignment (START, END, TOP, BOTTOM)
     */
    fun drawableAlign(align: DrawableAlign) = apply {
        this.drawableAlign = align
        updateDrawableState()
    }

    /**
     * Set drawable size in pixels
     */
    fun drawableSize(size: Int) = apply {
        this.drawableSize = size
        updateDrawableState()
    }

    /**
     * Set drawable padding in pixels
     */
    fun drawablePadding(padding: Int) = apply {
        this.drawablePadding = padding
        updateDrawableState()
    }

    /**
     * Set tint for both drawables
     */
    fun drawableTint(color: Int) = apply {
        this.drawableTint = color
        this.drawableEmptyTint = color
        this.drawableNotEmptyTint = color
        updateDrawableState()
    }

    /**
     * Set tint for empty state drawable
     */
    fun drawableEmptyTint(color: Int) = apply {
        this.drawableEmptyTint = color
        updateDrawableState()
    }

    /**
     * Set tint for not empty state drawable
     */
    fun drawableNotEmptyTint(color: Int) = apply {
        this.drawableNotEmptyTint = color
        updateDrawableState()
    }

    /**
     * Set visibility for empty state drawable.
     * When false, drawable is invisible but still reserves space (prevents height change).
     */
    fun drawableEmptyVisible(visible: Boolean) = apply {
        this.drawableEmptyVisible = visible
        updateDrawableState()
    }

    /**
     * Set visibility for not-empty state drawable.
     * When false, drawable is invisible but still reserves space (prevents height change).
     */
    fun drawableNotEmptyVisible(visible: Boolean) = apply {
        this.drawableNotEmptyVisible = visible
        updateDrawableState()
    }

    /**
     * Set click listener for drawable when text is empty
     */
    fun onDrawableEmptyClick(listener: (DrawableAlign) -> Unit) = apply {
        this.onDrawableEmptyClick = listener
    }

    /**
     * Set click listener for drawable when text is not empty
     */
    fun onDrawableNotEmptyClick(listener: (DrawableAlign) -> Unit) = apply {
        this.onDrawableNotEmptyClick = listener
    }

    /**
     * Set click listener for drawable (called for both states if specific listeners are not set)
     */
    fun onDrawableClick(listener: () -> Unit) = apply {
        this.onDrawableClick = listener
    }

    /**
     * Clear text and trigger drawable update
     */
    fun clearText() {
        setText("")
    }

    /**
     * Check if text is currently empty
     */
    fun isEmpty(): Boolean = text.isNullOrEmpty()

    /**
     * Check if text is not empty
     */
    fun isNotEmpty(): Boolean = !text.isNullOrEmpty()
}
