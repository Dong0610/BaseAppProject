package com.dong.baselib.widget.view

import android.annotation.SuppressLint
import android.content.Context
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
import com.dong.baselib.widget.layout.IUiLayout
import com.dong.baselib.widget.layout.UiLayoutHelper
import kotlin.math.max

class UiEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr), IUiLayout {

    override val helper = UiLayoutHelper(this)

    enum class DrawableAlign { START, END, TOP, BOTTOM }

    // Focus-specific background (override helper values when focused)
    private var focusBgColorAll = Color.TRANSPARENT
    private var isFocusGradient = false
    private var focusBgColors: IntArray? = null
    private var focusBgGradientType = UiLayoutHelper.GradientType.LINEAR
    private var focusBgGradientOrientation = UiLayoutHelper.GradientOrientation.TOP_TO_BOTTOM
    private var focusBgGradientCenterX = 0.5f
    private var focusBgGradientCenterY = 0.5f
    private var focusBgGradientRadius = 0f

    // Focus-specific stroke (override helper values when focused)
    private var focusStWidth = 0f
    private var focusStColorAll = Color.TRANSPARENT
    private var focusStColors: IntArray? = null
    private var focusStGradientOrientation = UiLayoutHelper.GradientOrientation.LEFT_TO_RIGHT
    private var isFocusDashed = false
    private var focusDashSpace = 10f

    // Drawable
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

    // EditText-specific text
    private var lineOption: LineOption = LineOption.NONE
    private var tColorAll = currentTextColor
    private var tColorHint = currentHintTextColor
    private var textGradient = false
    private var textGradientStart = Color.TRANSPARENT
    private var textGradientEnd = Color.TRANSPARENT
    private var textGradientOrientation = UiLayoutHelper.GradientOrientation.LEFT_TO_RIGHT

    private val clipPath = Path()
    private var hasFocusState = false

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiEditText).apply {
            try {
                helper.readCornerAttrs(
                    this,
                    R.styleable.UiEditText_cornerRadius,
                    R.styleable.UiEditText_cornerTopLeft,
                    R.styleable.UiEditText_cornerTopRight,
                    R.styleable.UiEditText_cornerBottomLeft,
                    R.styleable.UiEditText_cornerBottomRight
                )
                helper.readBackgroundAttrs(
                    this,
                    R.styleable.UiEditText_bgGradient,
                    R.styleable.UiEditText_bgGradientStart,
                    R.styleable.UiEditText_bgGradientCenter,
                    R.styleable.UiEditText_bgGradientEnd,
                    R.styleable.UiEditText_bgColor,
                    R.styleable.UiEditText_bgGradientOrientation,
                    R.styleable.UiEditText_bgGradientType,
                    R.styleable.UiEditText_bgGradientCenterX,
                    R.styleable.UiEditText_bgGradientCenterY,
                    R.styleable.UiEditText_bgGradientRadius,
                    R.styleable.UiEditText_bgGradientColors,
                    R.styleable.UiEditText_bgColors,
                    R.styleable.UiEditText_bgGradientPositions
                )
                helper.readStrokeAttrs(
                    this,
                    R.styleable.UiEditText_strokeWidth,
                    R.styleable.UiEditText_strokeColor,
                    R.styleable.UiEditText_strokeDashed,
                    R.styleable.UiEditText_dashGap,
                    R.styleable.UiEditText_strokeGradientColors,
                    R.styleable.UiEditText_strokeGradientOrientation,
                    -1,  // no strokeOption attr — defaults to STROKE_ALL
                    -1,  // no strokeCap attr
                    R.styleable.UiEditText_strokeColors,
                    R.styleable.UiEditText_strokeGradientPositions
                )
                helper.readShadowAttrs(
                    this,
                    R.styleable.UiEditText_shadowColor,
                    R.styleable.UiEditText_shadowRadius,
                    R.styleable.UiEditText_shadowDx,
                    R.styleable.UiEditText_shadowDy,
                    R.styleable.UiEditText_shadowElevation
                )
                helper.readDimensionAttrs(
                    this,
                    R.styleable.UiEditText_uiDimenRatio,
                    R.styleable.UiEditText_uiWidthParentPercent,
                    R.styleable.UiEditText_uiHeightParentPercent,
                    R.styleable.UiEditText_uiMaxWidthParentPercent,
                    R.styleable.UiEditText_uiMaxHeightParentPercent,
                    R.styleable.UiEditText_uiMinWidthParentPercent,
                    R.styleable.UiEditText_uiMinHeightParentPercent,
                    R.styleable.UiEditText_uiWidthScreenPercent,
                    R.styleable.UiEditText_uiHeightScreenPercent,
                    R.styleable.UiEditText_uiMaxWidthScreenPercent,
                    R.styleable.UiEditText_uiMaxHeightScreenPercent,
                    R.styleable.UiEditText_uiMinWidthScreenPercent,
                    R.styleable.UiEditText_uiMinHeightScreenPercent
                )

                // Focus background
                focusBgColorAll = getColor(R.styleable.UiEditText_focusBgColor, helper.bgColor)
                isFocusGradient = getBoolean(R.styleable.UiEditText_focusBgGradient, false)
                val focusStart = getColor(R.styleable.UiEditText_focusBgGradientStart, Color.TRANSPARENT)
                val focusCenter = getColor(R.styleable.UiEditText_focusBgGradientCenter, Color.TRANSPARENT)
                val focusEnd = getColor(R.styleable.UiEditText_focusBgGradientEnd, Color.TRANSPARENT)
                if (focusStart != Color.TRANSPARENT || focusEnd != Color.TRANSPARENT) {
                    focusBgColors = if (focusCenter != Color.TRANSPARENT)
                        intArrayOf(focusStart, focusCenter, focusEnd)
                    else intArrayOf(focusStart, focusEnd)
                }
                focusBgGradientOrientation = helper.bgGradientOrientation
                focusBgGradientType = UiLayoutHelper.GradientType.entries.getOrElse(
                    getInt(R.styleable.UiEditText_focusBgGradientType, helper.bgGradientType.ordinal)
                ) { UiLayoutHelper.GradientType.LINEAR }
                focusBgGradientCenterX = getFloat(R.styleable.UiEditText_focusBgGradientCenterX, helper.bgGradientCenterX)
                focusBgGradientCenterY = getFloat(R.styleable.UiEditText_focusBgGradientCenterY, helper.bgGradientCenterY)
                focusBgGradientRadius = getDimension(R.styleable.UiEditText_focusBgGradientRadius, helper.bgGradientRadius)

                // Focus stroke
                focusStWidth = getDimension(R.styleable.UiEditText_focusStrokeWidth, helper.stWidth)
                focusStColorAll = getColor(R.styleable.UiEditText_focusStrokeColor, Color.TRANSPARENT)
                focusStColors = getString(R.styleable.UiEditText_focusStrokeGradientColors)?.split(" ")
                    ?.mapNotNull { if (it.isValidHexColor()) it.toColorInt() else null }
                    ?.toIntArray()?.takeIf { it.size >= 2 }
                focusStGradientOrientation = helper.strokeGradientOrientation
                isFocusDashed = getBoolean(R.styleable.UiEditText_focusStrokeDashed, helper.isDashed)
                focusDashSpace = getDimension(R.styleable.UiEditText_focusDashGap, helper.dashGap)

                // EditText-specific
                tColorAll = getColor(R.styleable.UiEditText_edtTextColor, currentTextColor)
                tColorHint = getColor(R.styleable.UiEditText_edtHintColor, currentHintTextColor)
                lineOption = LineOption.fromValue(getInt(R.styleable.UiEditText_lineOption, 0))
                textGradient = getBoolean(R.styleable.UiEditText_textGradient, false)
                textGradientStart = getColor(R.styleable.UiEditText_textGradientStart, Color.TRANSPARENT)
                textGradientEnd = getColor(R.styleable.UiEditText_textGradientEnd, Color.TRANSPARENT)
                textGradientOrientation = UiLayoutHelper.GradientOrientation.entries.getOrElse(
                    getInt(R.styleable.UiEditText_textGradientOrientation, 6)
                ) { UiLayoutHelper.GradientOrientation.LEFT_TO_RIGHT }

                // Drawable
                drawableEmpty = getDrawable(R.styleable.UiEditText_drawableEmpty)
                drawableNotEmpty = getDrawable(R.styleable.UiEditText_drawableNotEmpty)
                drawableAlign = DrawableAlign.entries.getOrElse(
                    getInt(R.styleable.UiEditText_drawableAlign, 1)
                ) { DrawableAlign.END }
                drawableSize = getDimensionPixelSize(R.styleable.UiEditText_drawableSize, 0)
                drawablePadding = getDimensionPixelSize(R.styleable.UiEditText_drawablePadding, 0)
                drawableTint = getColor(R.styleable.UiEditText_drawableTint, Color.TRANSPARENT)
                drawableEmptyTint = getColor(R.styleable.UiEditText_drawableEmptyTint, drawableTint)
                drawableNotEmptyTint = getColor(R.styleable.UiEditText_drawableNotEmptyTint, drawableTint)
                drawableEmptyVisible = getBoolean(R.styleable.UiEditText_drawableEmptyVisible, true)
                drawableNotEmptyVisible = getBoolean(R.styleable.UiEditText_drawableNotEmptyVisible, true)
            } finally {
                recycle()
            }
        }

        helper.setupShadow()
        applyTextStyles()
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        isLongClickable = true
        setupDrawableTextWatcher()
        updateDrawableState()
    }

    // ==================== Measure & Layout ====================

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (helper.shouldApplyCustomMeasure()) {
            val dm = context.resources.displayMetrics
            val specW = MeasureSpec.getSize(widthMeasureSpec)
            val specH = MeasureSpec.getSize(heightMeasureSpec)
            val parentWidth = if (specW > 0) specW else
                (parent as? android.view.View)?.width?.takeIf { it > 0 } ?: dm.widthPixels
            val parentHeight = if (specH > 0) specH else
                (parent as? android.view.View)?.height?.takeIf { it > 0 } ?: dm.heightPixels
            val result = helper.measureWithConstraints(widthMeasureSpec, heightMeasureSpec, parentWidth, parentHeight)
            val wSpec = if (result.widthCustomized) MeasureSpec.makeMeasureSpec(result.width, MeasureSpec.EXACTLY) else widthMeasureSpec
            val hSpec = if (result.heightCustomized) MeasureSpec.makeMeasureSpec(result.height, MeasureSpec.EXACTLY) else heightMeasureSpec
            super.onMeasure(wSpec, hSpec)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        helper.onSizeChanged(w, h)
    }

    // ==================== Focus ====================

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        hasFocusState = gainFocus
        invalidate()
    }

    private fun hasFocusBackground(): Boolean =
        isFocusGradient || focusBgColorAll != helper.bgColor

    private fun hasFocusStroke(): Boolean =
        focusStWidth != helper.stWidth || focusStColorAll != Color.TRANSPARENT || focusStColors != null

    // ==================== Drawing ====================

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) { super.onDraw(canvas); return }

        // Translate for EditText's horizontal scroll so bg/clip stay visually fixed
        canvas.withSave {
            translate(scrollX.toFloat(), scrollY.toFloat())
            if (hasFocusState && hasFocusBackground()) {
                drawFocusedBackground(canvas, w, h)
            } else {
                helper.drawBackground(canvas, w, h)
            }
            clipPath.reset()
            clipPath.addRoundRect(RectF(0f, 0f, w, h), helper.getCornerRadii(w, h), Path.Direction.CW)
            runCatching { clipPath(this@UiEditText.clipPath) }
        }
        super.onDraw(canvas)
        if (lineOption == LineOption.TOP) drawTopLine(canvas)
    }

    private fun drawFocusedBackground(canvas: Canvas, w: Float, h: Float) {
        val colors = focusBgColors
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            if (isFocusGradient && colors != null && colors.size >= 2) {
                shader = when (focusBgGradientType) {
                    UiLayoutHelper.GradientType.LINEAR -> {
                        val (x0, y0, x1, y1) = focusBgGradientOrientation.toCoordinates(w, h)
                        LinearGradient(x0, y0, x1, y1, colors, null, Shader.TileMode.CLAMP)
                    }
                    UiLayoutHelper.GradientType.RADIAL -> {
                        val cx = w * focusBgGradientCenterX
                        val cy = h * focusBgGradientCenterY
                        val r = if (focusBgGradientRadius > 0f) focusBgGradientRadius else max(w, h) / 2f
                        RadialGradient(cx, cy, r, colors, null, Shader.TileMode.CLAMP)
                    }
                    UiLayoutHelper.GradientType.SWEEP ->
                        SweepGradient(w * focusBgGradientCenterX, h * focusBgGradientCenterY, colors, null)
                }
            } else {
                color = focusBgColorAll
            }
        }
        val path = Path().apply {
            addRoundRect(RectF(0f, 0f, w, h), helper.getCornerRadii(w, h), Path.Direction.CW)
        }
        canvas.drawPath(path, bgPaint)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) { super.dispatchDraw(canvas); return }
        canvas.withSave {
            translate(scrollX.toFloat(), scrollY.toFloat())
            if (hasFocusState && hasFocusStroke()) {
                drawFocusedStroke(canvas, w, h)
            } else {
                helper.drawStroke(canvas, w, h)
            }
        }
    }

    private fun drawFocusedStroke(canvas: Canvas, w: Float, h: Float) {
        if (focusStWidth <= 0f) return
        val radii = helper.getCornerRadii(w, h)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = focusStWidth
            strokeJoin = Paint.Join.ROUND
            if (isFocusDashed) {
                pathEffect = DashPathEffect(floatArrayOf(focusDashSpace, focusDashSpace), 0f)
            }
            val gradColors = focusStColors
            if (gradColors != null && gradColors.size >= 2) {
                val (x0, y0, x1, y1) = focusStGradientOrientation.toCoordinates(w, h)
                shader = LinearGradient(x0, y0, x1, y1, gradColors, null, Shader.TileMode.CLAMP)
            } else {
                color = focusStColorAll
            }
        }
        val inset = focusStWidth / 2f
        val insetRadii = radii.map { max(0f, it - inset) }.toFloatArray()
        val path = Path().apply {
            addRoundRect(RectF(inset, inset, w - inset, h - inset), insetRadii, Path.Direction.CW)
        }
        canvas.drawPath(path, strokePaint)
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
        val fontMetrics = paint.fontMetrics
        val baseline = when (gravity and android.view.Gravity.VERTICAL_GRAVITY_MASK) {
            android.view.Gravity.CENTER_VERTICAL -> (height - fontMetrics.bottom - fontMetrics.top) / 2f
            android.view.Gravity.BOTTOM -> height - paddingBottom - fontMetrics.bottom
            else -> paddingTop - fontMetrics.top
        }
        val linePaint = Paint(paint).apply {
            style = Paint.Style.STROKE
            strokeWidth = textSize / 12f
        }
        canvas.drawLine(startX, baseline + fontMetrics.top, startX + textWidth, baseline + fontMetrics.top, linePaint)
    }

    // ==================== Text ====================

    private fun applyTextStyles() {
        paintFlags = paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv() and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        when (lineOption) {
            LineOption.BOTTOM -> paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            LineOption.CENTER -> paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            else -> Unit
        }
        setHintTextColor(tColorHint)
        if (textGradient) {
            applyTextGradient()
        } else {
            paint.shader = null
            setTextColor(tColorAll)
        }
    }

    private fun applyTextGradient() {
        val textStr = text?.toString().orEmpty()
        if (textStr.isEmpty()) return
        val w = paint.measureText(textStr)
        val h = textSize * 1.3f
        val (x0, y0, x1, y1) = textGradientOrientation.toCoordinates(w, h)
        paint.shader = LinearGradient(x0, y0, x1, y1, textGradientStart, textGradientEnd, Shader.TileMode.CLAMP)
        invalidate()
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (textGradient) applyTextGradient()
    }

    // ==================== Drawable ====================

    private fun setupDrawableTextWatcher() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateDrawableState() }
        })
    }

    private fun updateDrawableState() {
        val isEmpty = text.isNullOrEmpty()
        val visible = if (isEmpty) drawableEmptyVisible else drawableNotEmptyVisible
        currentDrawable = (if (isEmpty) drawableEmpty else drawableNotEmpty)?.mutate()
        currentDrawable?.let { drawable ->
            if (drawableSize > 0) {
                drawable.setBounds(0, 0, drawableSize, drawableSize)
            } else {
                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            }
            drawable.alpha = if (visible) 255 else 0
            if (visible) {
                val tint = if (isEmpty) {
                    if (drawableEmptyTint != Color.TRANSPARENT) drawableEmptyTint else drawableTint
                } else {
                    if (drawableNotEmptyTint != Color.TRANSPARENT) drawableNotEmptyTint else drawableTint
                }
                if (tint != Color.TRANSPARENT) DrawableCompat.setTint(drawable, tint)
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
        if (drawablePadding > 0) compoundDrawablePadding = drawablePadding
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && currentDrawable != null) {
            val drawableBounds = currentDrawable?.bounds ?: return super.onTouchEvent(event)
            val touchX = event.x.toInt()
            val touchY = event.y.toInt()
            val isDrawableClicked = when (drawableAlign) {
                DrawableAlign.END -> touchX >= width - paddingEnd - drawableBounds.width() - compoundDrawablePadding
                DrawableAlign.START -> touchX <= paddingStart + drawableBounds.width() + compoundDrawablePadding
                DrawableAlign.TOP -> touchY <= paddingTop + drawableBounds.height() + compoundDrawablePadding
                DrawableAlign.BOTTOM -> touchY >= height - paddingBottom - drawableBounds.height() - compoundDrawablePadding
            }
            if (isDrawableClicked) {
                val isEmpty = text.isNullOrEmpty()
                val visible = if (isEmpty) drawableEmptyVisible else drawableNotEmptyVisible
                if (visible) {
                    when {
                        isEmpty && onDrawableEmptyClick != null -> { onDrawableEmptyClick?.invoke(drawableAlign); return true }
                        !isEmpty && onDrawableNotEmptyClick != null -> { onDrawableNotEmptyClick?.invoke(drawableAlign); return true }
                        onDrawableClick != null -> { onDrawableClick?.invoke(); return true }
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    // ==================== IUiLayout ====================

    override fun invalidateView() = invalidate()
    override fun invalidateViewOutline() = invalidateOutline()
    override fun updateViewClipPath() = helper.updateClipPath(width, height)

    // ==================== Fluent API — Background ====================

    fun cornerRadius(radius: Float) = apply {
        helper.cornerRadius = radius
        invalidate()
    }

    fun backgroundAll(color: Int) = apply {
        helper.isGradient = false
        helper.bgColor = color
        invalidate()
    }

    fun backgroundGradient(start: Int, end: Int, center: Int = Color.TRANSPARENT) = apply {
        helper.isGradient = true
        helper.bgColors = if (center != Color.TRANSPARENT) intArrayOf(start, center, end) else intArrayOf(start, end)
        invalidate()
    }

    fun backgroundGradient(vararg colors: Int) = apply {
        if (colors.isNotEmpty()) {
            helper.isGradient = true
            helper.bgColors = colors.copyOf()
        }
        invalidate()
    }

    fun backgroundOrientation(orientation: UiLayoutHelper.GradientOrientation) = apply {
        helper.bgGradientOrientation = orientation
        invalidate()
    }

    fun backgroundGradientType(type: UiLayoutHelper.GradientType) = apply {
        helper.isGradient = true
        helper.bgGradientType = type
        invalidate()
    }

    fun linearGradient() = backgroundGradientType(UiLayoutHelper.GradientType.LINEAR)
    fun radialGradient() = backgroundGradientType(UiLayoutHelper.GradientType.RADIAL)
    fun sweepGradient() = backgroundGradientType(UiLayoutHelper.GradientType.SWEEP)

    fun gradientCenter(centerX: Float, centerY: Float) = apply {
        helper.bgGradientCenterX = centerX
        helper.bgGradientCenterY = centerY
        invalidate()
    }

    fun gradientRadius(radius: Float) = apply {
        helper.bgGradientRadius = radius
        invalidate()
    }

    fun radialGradient(centerX: Float, centerY: Float, radius: Float = 0f) = apply {
        helper.isGradient = true
        helper.bgGradientType = UiLayoutHelper.GradientType.RADIAL
        helper.bgGradientCenterX = centerX
        helper.bgGradientCenterY = centerY
        helper.bgGradientRadius = radius
        invalidate()
    }

    fun sweepGradient(centerX: Float, centerY: Float) = apply {
        helper.isGradient = true
        helper.bgGradientType = UiLayoutHelper.GradientType.SWEEP
        helper.bgGradientCenterX = centerX
        helper.bgGradientCenterY = centerY
        invalidate()
    }

    fun solidBackground(color: Int) = apply {
        helper.isGradient = false
        helper.bgColor = color
        invalidate()
    }

    fun rounded(radius: Float) = cornerRadius(radius)

    // ==================== Fluent API — Focus Background ====================

    fun focusBackgroundAll(color: Int) = apply {
        isFocusGradient = false
        focusBgColorAll = color
        invalidate()
    }

    fun focusBackgroundGradient(start: Int, end: Int, center: Int = Color.TRANSPARENT) = apply {
        isFocusGradient = true
        focusBgColors = if (center != Color.TRANSPARENT) intArrayOf(start, center, end) else intArrayOf(start, end)
        invalidate()
    }

    fun focusBackgroundGradient(vararg colors: Int) = apply {
        if (colors.isNotEmpty()) { isFocusGradient = true; focusBgColors = colors.copyOf() }
        invalidate()
    }

    fun focusBackgroundOrientation(orientation: UiLayoutHelper.GradientOrientation) = apply {
        isFocusGradient = true
        focusBgGradientOrientation = orientation
        invalidate()
    }

    fun focusBackgroundGradientType(type: UiLayoutHelper.GradientType) = apply {
        isFocusGradient = true
        focusBgGradientType = type
        invalidate()
    }

    fun focusLinearGradient() = focusBackgroundGradientType(UiLayoutHelper.GradientType.LINEAR)
    fun focusRadialGradient() = focusBackgroundGradientType(UiLayoutHelper.GradientType.RADIAL)
    fun focusSweepGradient() = focusBackgroundGradientType(UiLayoutHelper.GradientType.SWEEP)

    fun focusGradientCenter(centerX: Float, centerY: Float) = apply {
        focusBgGradientCenterX = centerX; focusBgGradientCenterY = centerY; invalidate()
    }

    fun focusGradientRadius(radius: Float) = apply {
        focusBgGradientRadius = radius; invalidate()
    }

    fun focusRadialGradient(centerX: Float, centerY: Float, radius: Float = 0f) = apply {
        isFocusGradient = true
        focusBgGradientType = UiLayoutHelper.GradientType.RADIAL
        focusBgGradientCenterX = centerX; focusBgGradientCenterY = centerY
        focusBgGradientRadius = radius; invalidate()
    }

    fun focusSweepGradient(centerX: Float, centerY: Float) = apply {
        isFocusGradient = true
        focusBgGradientType = UiLayoutHelper.GradientType.SWEEP
        focusBgGradientCenterX = centerX; focusBgGradientCenterY = centerY; invalidate()
    }

    // ==================== Fluent API — Stroke ====================

    fun strokeWidth(width: Float) = apply { helper.stWidth = width; invalidate() }

    fun strokeColor(color: Int) = apply {
        helper.strokeColors = null
        helper.strokeColor = color
        invalidate()
    }

    fun strokeGradient(vararg colors: Int) = apply {
        helper.strokeColors = if (colors.isNotEmpty()) colors.copyOf() else null
        invalidate()
    }

    fun strokeOrientation(orientation: UiLayoutHelper.GradientOrientation) = apply {
        helper.strokeGradientOrientation = orientation; invalidate()
    }

    fun strokeDashed(dashed: Boolean, spacing: Float = 10f) = apply {
        helper.isDashed = dashed; helper.dashGap = spacing; invalidate()
    }

    fun dashed(spacing: Float = 10f) = strokeDashed(true, spacing)

    // ==================== Fluent API — Focus Stroke ====================

    fun focusStrokeWidth(width: Float) = apply { focusStWidth = width; invalidate() }

    fun focusStrokeColor(color: Int) = apply {
        focusStColors = null; focusStColorAll = color; invalidate()
    }

    fun focusStrokeGradient(vararg colors: Int) = apply {
        focusStColors = if (colors.isNotEmpty()) colors.copyOf() else null; invalidate()
    }

    fun focusStrokeOrientation(orientation: UiLayoutHelper.GradientOrientation) = apply {
        focusStGradientOrientation = orientation; invalidate()
    }

    fun focusStrokeDashed(dashed: Boolean, spacing: Float = 10f) = apply {
        isFocusDashed = dashed; focusDashSpace = spacing; invalidate()
    }

    fun focusDashed(spacing: Float = 10f) = focusStrokeDashed(true, spacing)

    // ==================== Fluent API — Text ====================

    fun setLineOption(option: LineOption) = apply {
        lineOption = option; applyTextStyles(); invalidate()
    }

    fun setTextGradient(start: Int, end: Int, orientation: UiLayoutHelper.GradientOrientation = UiLayoutHelper.GradientOrientation.LEFT_TO_RIGHT) = apply {
        textGradient = true
        textGradientStart = start; textGradientEnd = end
        textGradientOrientation = orientation
        applyTextGradient()
    }

    fun clearTextGradient() = apply {
        textGradient = false; paint.shader = null; setTextColor(tColorAll); invalidate()
    }

    fun edtSingleLine(single: Boolean = true) = apply {
        isSingleLine = single
        if (single) { maxLines = 1; setHorizontallyScrolling(true) }
        invalidate()
    }

    fun applyStyle(block: UiEditText.() -> Unit) = apply { block() }

    // ==================== Fluent API — Drawable ====================

    fun drawableEmpty(drawable: Drawable?) = apply { this.drawableEmpty = drawable; updateDrawableState() }
    fun drawableEmpty(drawableRes: Int) = apply { this.drawableEmpty = ContextCompat.getDrawable(context, drawableRes); updateDrawableState() }
    fun drawableNotEmpty(drawable: Drawable?) = apply { this.drawableNotEmpty = drawable; updateDrawableState() }
    fun drawableNotEmpty(drawableRes: Int) = apply { this.drawableNotEmpty = ContextCompat.getDrawable(context, drawableRes); updateDrawableState() }
    fun drawables(empty: Drawable?, notEmpty: Drawable?) = apply { this.drawableEmpty = empty; this.drawableNotEmpty = notEmpty; updateDrawableState() }
    fun drawables(emptyRes: Int, notEmptyRes: Int) = apply {
        this.drawableEmpty = ContextCompat.getDrawable(context, emptyRes)
        this.drawableNotEmpty = ContextCompat.getDrawable(context, notEmptyRes)
        updateDrawableState()
    }
    fun drawableAlign(align: DrawableAlign) = apply { this.drawableAlign = align; updateDrawableState() }
    fun drawableSize(size: Int) = apply { this.drawableSize = size; updateDrawableState() }
    fun drawablePadding(padding: Int) = apply { this.drawablePadding = padding; updateDrawableState() }
    fun drawableTint(color: Int) = apply { this.drawableTint = color; this.drawableEmptyTint = color; this.drawableNotEmptyTint = color; updateDrawableState() }
    fun drawableEmptyTint(color: Int) = apply { this.drawableEmptyTint = color; updateDrawableState() }
    fun drawableNotEmptyTint(color: Int) = apply { this.drawableNotEmptyTint = color; updateDrawableState() }
    fun drawableEmptyVisible(visible: Boolean) = apply { this.drawableEmptyVisible = visible; updateDrawableState() }
    fun drawableNotEmptyVisible(visible: Boolean) = apply { this.drawableNotEmptyVisible = visible; updateDrawableState() }
    fun onDrawableEmptyClick(listener: (DrawableAlign) -> Unit) = apply { this.onDrawableEmptyClick = listener }
    fun onDrawableNotEmptyClick(listener: (DrawableAlign) -> Unit) = apply { this.onDrawableNotEmptyClick = listener }
    fun onDrawableClick(listener: () -> Unit) = apply { this.onDrawableClick = listener }

    fun clearText() { setText("") }
    fun isEmpty(): Boolean = text.isNullOrEmpty()
    fun isNotEmpty(): Boolean = !text.isNullOrEmpty()
}
