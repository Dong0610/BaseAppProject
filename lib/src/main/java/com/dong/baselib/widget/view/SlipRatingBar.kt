package com.dong.baselib.widget.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.dong.baselib.R
import kotlin.math.floor
import kotlin.math.max

/**
 * A customizable RatingBar that supports smooth sliding/dragging.
 *
 * Features:
 * - Smooth sliding to change rating
 * - Half-star support
 * - Custom star colors and sizes
 * - Custom drawable icons support
 * - Animation on rating change
 * - Touch feedback
 */
class SlipRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Rating configuration
    var maxStars: Int = 5
        set(value) {
            field = max(1, value)
            requestLayout()
            invalidate()
        }

    var rating: Float = 0f
        set(value) {
            val newRating = value.coerceIn(0f, maxStars.toFloat())
            if (field != newRating) {
                field = newRating
                onRatingChangeListener?.invoke(newRating)
                invalidate()
            }
        }

    var stepSize: Float = 0.5f  // 0.5 for half stars, 1.0 for full stars only
        set(value) {
            field = value.coerceIn(0.1f, 1f)
        }

    var isIndicator: Boolean = false  // If true, user cannot change rating

    // Star appearance
    var starSize: Float = dp(32f)
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    var starSpacing: Float = dp(4f)
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    var starCornerRadius: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    // Colors
    var filledColor: Int = Color.parseColor("#FFC107")  // Amber
        set(value) {
            field = value
            filledPaint.color = value
            invalidate()
        }

    var emptyColor: Int = Color.parseColor("#E0E0E0")  // Light gray
        set(value) {
            field = value
            emptyPaint.color = value
            invalidate()
        }

    var strokeColor: Int = Color.parseColor("#FFA000")  // Dark amber
        set(value) {
            field = value
            strokePaint.color = value
            invalidate()
        }

    var strokeWidth: Float = dp(1f)
        set(value) {
            field = value
            strokePaint.strokeWidth = value
            invalidate()
        }

    var showStroke: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    // Gradient support
    var useGradient: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var gradientStartColor: Int = Color.parseColor("#FFD54F")
    var gradientEndColor: Int = Color.parseColor("#FF6F00")

    // Custom drawable support
    var useDrawable: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var filledDrawable: Drawable? = null
        set(value) {
            field = value?.mutate()
            invalidate()
        }

    var emptyDrawable: Drawable? = null
        set(value) {
            field = value?.mutate()
            invalidate()
        }

    var halfFilledDrawable: Drawable? = null
        set(value) {
            field = value?.mutate()
            invalidate()
        }

    var drawableTintFilled: Int? = null
        set(value) {
            field = value
            invalidate()
        }

    var drawableTintEmpty: Int? = null
        set(value) {
            field = value
            invalidate()
        }

    // Animation
    var animateRatingChange: Boolean = true
    var animationDuration: Long = 200L

    // Touch feedback
    var touchScaleEffect: Boolean = true
    private var currentScale: Float = 1f
    private var pressedStarIndex: Int = -1

    // Listeners
    var onRatingChangeListener: ((Float) -> Unit)? = null
    var onRatingStartListener: (() -> Unit)? = null
    var onRatingEndListener: ((Float) -> Unit)? = null

    // Paints
    private val filledPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = filledColor
    }

    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = emptyColor
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = strokeColor
        strokeWidth = this@SlipRatingBar.strokeWidth
    }

    // Star path
    private val starPath = Path()
    private val clipPath = Path()

    // Touch tracking
    private var isDragging = false
    private var lastTouchX = 0f

    init {
        context.obtainStyledAttributes(attrs, R.styleable.SlipRatingBar).apply {
            try {
                maxStars = getInt(R.styleable.SlipRatingBar_srb_maxStars, 5)
                rating = getFloat(R.styleable.SlipRatingBar_srb_rating, 0f)
                stepSize = getFloat(R.styleable.SlipRatingBar_srb_stepSize, 0.5f)
                isIndicator = getBoolean(R.styleable.SlipRatingBar_srb_isIndicator, false)
                starSize = getDimension(R.styleable.SlipRatingBar_srb_starSize, dp(32f))
                starSpacing = getDimension(R.styleable.SlipRatingBar_srb_starSpacing, dp(4f))
                filledColor = getColor(R.styleable.SlipRatingBar_srb_filledColor, filledColor)
                emptyColor = getColor(R.styleable.SlipRatingBar_srb_emptyColor, emptyColor)
                strokeColor = getColor(R.styleable.SlipRatingBar_srb_strokeColor, strokeColor)
                strokeWidth = getDimension(R.styleable.SlipRatingBar_srb_strokeWidth, dp(1f))
                showStroke = getBoolean(R.styleable.SlipRatingBar_srb_showStroke, true)
                useGradient = getBoolean(R.styleable.SlipRatingBar_srb_useGradient, false)
                gradientStartColor = getColor(R.styleable.SlipRatingBar_srb_gradientStartColor, gradientStartColor)
                gradientEndColor = getColor(R.styleable.SlipRatingBar_srb_gradientEndColor, gradientEndColor)
                animateRatingChange = getBoolean(R.styleable.SlipRatingBar_srb_animateChange, true)
                touchScaleEffect = getBoolean(R.styleable.SlipRatingBar_srb_touchScaleEffect, true)

                // Custom drawable support
                useDrawable = getBoolean(R.styleable.SlipRatingBar_srb_useDrawable, false)
                filledDrawable = getDrawable(R.styleable.SlipRatingBar_srb_filledDrawable)?.mutate()
                emptyDrawable = getDrawable(R.styleable.SlipRatingBar_srb_emptyDrawable)?.mutate()
                halfFilledDrawable = getDrawable(R.styleable.SlipRatingBar_srb_halfFilledDrawable)?.mutate()

                if (hasValue(R.styleable.SlipRatingBar_srb_drawableTintFilled)) {
                    drawableTintFilled = getColor(R.styleable.SlipRatingBar_srb_drawableTintFilled, filledColor)
                }
                if (hasValue(R.styleable.SlipRatingBar_srb_drawableTintEmpty)) {
                    drawableTintEmpty = getColor(R.styleable.SlipRatingBar_srb_drawableTintEmpty, emptyColor)
                }

                // Auto-enable drawable mode if drawables are set
                if (filledDrawable != null || emptyDrawable != null) {
                    useDrawable = true
                }
            } finally {
                recycle()
            }
        }

        filledPaint.color = filledColor
        emptyPaint.color = emptyColor
        strokePaint.color = strokeColor
        strokePaint.strokeWidth = strokeWidth
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = (starSize * maxStars + starSpacing * (maxStars - 1) + paddingLeft + paddingRight).toInt()
        val desiredHeight = (starSize + paddingTop + paddingBottom).toInt()

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val startX = paddingLeft.toFloat()
        val centerY = height / 2f

        if (useDrawable && (filledDrawable != null || emptyDrawable != null)) {
            drawWithDrawables(canvas, startX, centerY)
        } else {
            drawWithPaint(canvas, startX, centerY)
        }
    }

    private fun drawWithDrawables(canvas: Canvas, startX: Float, centerY: Float) {
        for (i in 0 until maxStars) {
            val starX = startX + i * (starSize + starSpacing)
            val starCenterX = starX + starSize / 2
            val starCenterY = centerY

            // Calculate scale for touch effect
            val scale = if (touchScaleEffect && pressedStarIndex == i) currentScale else 1f

            canvas.save()
            canvas.scale(scale, scale, starCenterX, starCenterY)

            // Determine fill level for this star
            val fillLevel = when {
                rating >= i + 1 -> 1f  // Fully filled
                rating > i -> rating - i  // Partially filled
                else -> 0f  // Empty
            }

            val left = starX.toInt()
            val top = (centerY - starSize / 2).toInt()
            val right = (starX + starSize).toInt()
            val bottom = (centerY + starSize / 2).toInt()

            when {
                fillLevel >= 1f -> {
                    // Draw filled drawable
                    drawDrawable(canvas, filledDrawable ?: emptyDrawable, left, top, right, bottom, drawableTintFilled)
                }
                fillLevel > 0f && fillLevel < 1f -> {
                    // Draw half-filled or use clipping
                    if (halfFilledDrawable != null && fillLevel >= 0.5f) {
                        drawDrawable(canvas, halfFilledDrawable, left, top, right, bottom, drawableTintFilled)
                    } else {
                        // Draw empty background
                        drawDrawable(canvas, emptyDrawable ?: filledDrawable, left, top, right, bottom, drawableTintEmpty)

                        // Clip and draw filled portion
                        canvas.save()
                        canvas.clipRect(left, top, left + (starSize * fillLevel).toInt(), bottom)
                        drawDrawable(canvas, filledDrawable ?: emptyDrawable, left, top, right, bottom, drawableTintFilled)
                        canvas.restore()
                    }
                }
                else -> {
                    // Draw empty drawable
                    drawDrawable(canvas, emptyDrawable ?: filledDrawable, left, top, right, bottom, drawableTintEmpty)
                }
            }

            canvas.restore()
        }
    }

    private fun drawDrawable(canvas: Canvas, drawable: Drawable?, left: Int, top: Int, right: Int, bottom: Int, tint: Int?) {
        drawable ?: return
        drawable.setBounds(left, top, right, bottom)
        if (tint != null) {
            DrawableCompat.setTint(drawable, tint)
        }
        drawable.draw(canvas)
    }

    private fun drawWithPaint(canvas: Canvas, startX: Float, centerY: Float) {
        // Apply gradient if enabled
        if (useGradient) {
            val totalWidth = starSize * maxStars + starSpacing * (maxStars - 1)
            filledPaint.shader = LinearGradient(
                startX, centerY,
                startX + totalWidth, centerY,
                gradientStartColor, gradientEndColor,
                Shader.TileMode.CLAMP
            )
        } else {
            filledPaint.shader = null
        }

        for (i in 0 until maxStars) {
            val starX = startX + i * (starSize + starSpacing)
            val starCenterX = starX + starSize / 2
            val starCenterY = centerY

            // Calculate scale for touch effect
            val scale = if (touchScaleEffect && pressedStarIndex == i) currentScale else 1f

            canvas.save()
            canvas.scale(scale, scale, starCenterX, starCenterY)

            // Create star path
            createStarPath(starCenterX, starCenterY, starSize / 2 * 0.9f)

            // Determine fill level for this star
            val fillLevel = when {
                rating >= i + 1 -> 1f  // Fully filled
                rating > i -> rating - i  // Partially filled
                else -> 0f  // Empty
            }

            // Draw empty star background
            canvas.drawPath(starPath, emptyPaint)

            // Draw filled portion
            if (fillLevel > 0f) {
                canvas.save()
                clipPath.reset()
                clipPath.addRect(
                    starX, centerY - starSize / 2,
                    starX + starSize * fillLevel, centerY + starSize / 2,
                    Path.Direction.CW
                )
                canvas.clipPath(clipPath)
                canvas.drawPath(starPath, filledPaint)
                canvas.restore()
            }

            // Draw stroke
            if (showStroke) {
                canvas.drawPath(starPath, strokePaint)
            }

            canvas.restore()
        }
    }

    private fun createStarPath(cx: Float, cy: Float, radius: Float) {
        starPath.reset()

        val innerRadius = radius * 0.4f
        val angleStep = Math.PI / 5

        for (i in 0 until 10) {
            val r = if (i % 2 == 0) radius else innerRadius
            val angle = angleStep * i - Math.PI / 2
            val x = cx + (r * kotlin.math.cos(angle)).toFloat()
            val y = cy + (r * kotlin.math.sin(angle)).toFloat()

            if (i == 0) {
                starPath.moveTo(x, y)
            } else {
                starPath.lineTo(x, y)
            }
        }
        starPath.close()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isIndicator) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = true
                lastTouchX = event.x
                onRatingStartListener?.invoke()
                updateRatingFromTouch(event.x)
                updatePressedStar(event.x)
                if (touchScaleEffect) animateScale(0.9f)
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    updateRatingFromTouch(event.x)
                    updatePressedStar(event.x)
                    lastTouchX = event.x
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    isDragging = false
                    onRatingEndListener?.invoke(rating)
                    pressedStarIndex = -1
                    if (touchScaleEffect) animateScale(1f)
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateRatingFromTouch(touchX: Float) {
        val startX = paddingLeft.toFloat()
        val totalWidth = starSize * maxStars + starSpacing * (maxStars - 1)

        // Calculate raw rating from touch position
        val relativeX = (touchX - startX).coerceIn(0f, totalWidth)

        // Account for spacing when calculating which star
        var accumulatedX = 0f
        var rawRating = 0f

        for (i in 0 until maxStars) {
            val starStart = accumulatedX
            val starEnd = starStart + starSize

            when {
                relativeX <= starStart -> {
                    rawRating = i.toFloat()
                    break
                }
                relativeX <= starEnd -> {
                    val progress = (relativeX - starStart) / starSize
                    rawRating = i + progress
                    break
                }
                i == maxStars - 1 -> {
                    rawRating = maxStars.toFloat()
                }
            }

            accumulatedX = starEnd + starSpacing
        }

        // Apply step size
        val steppedRating = when {
            stepSize >= 1f -> kotlin.math.round(rawRating)
            stepSize == 0.5f -> (kotlin.math.round(rawRating * 2) / 2f)
            else -> (kotlin.math.round(rawRating / stepSize) * stepSize)
        }.coerceIn(0f, maxStars.toFloat())

        if (animateRatingChange && !isDragging) {
            animateRating(steppedRating)
        } else {
            rating = steppedRating
        }
    }

    private fun updatePressedStar(touchX: Float) {
        val startX = paddingLeft.toFloat()
        var accumulatedX = 0f

        for (i in 0 until maxStars) {
            val starStart = startX + accumulatedX
            val starEnd = starStart + starSize

            if (touchX >= starStart && touchX <= starEnd) {
                pressedStarIndex = i
                invalidate()
                return
            }

            accumulatedX += starSize + starSpacing
        }

        pressedStarIndex = -1
        invalidate()
    }

    private fun animateRating(targetRating: Float) {
        ValueAnimator.ofFloat(rating, targetRating).apply {
            duration = animationDuration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                rating = animator.animatedValue as Float
            }
            start()
        }
    }

    private fun animateScale(targetScale: Float) {
        ValueAnimator.ofFloat(currentScale, targetScale).apply {
            duration = 100
            addUpdateListener { animator ->
                currentScale = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    // ==================== Public Methods ====================

    /**
     * Set rating with optional animation
     */
    fun setRating(value: Float, animate: Boolean = true) {
        if (animate && animateRatingChange) {
            animateRating(value.coerceIn(0f, maxStars.toFloat()))
        } else {
            rating = value
        }
    }

    /**
     * Get the number of full stars
     */
    fun getFullStars(): Int = floor(rating).toInt()

    /**
     * Check if rating has half star
     */
    fun hasHalfStar(): Boolean = rating - floor(rating) >= 0.5f

    /**
     * Reset rating to 0
     */
    fun reset(animate: Boolean = true) {
        setRating(0f, animate)
    }

    /**
     * Set to maximum rating
     */
    fun setMax(animate: Boolean = true) {
        setRating(maxStars.toFloat(), animate)
    }

    /**
     * Set custom drawables programmatically
     */
    fun setDrawables(filled: Drawable?, empty: Drawable?, halfFilled: Drawable? = null) {
        filledDrawable = filled?.mutate()
        emptyDrawable = empty?.mutate()
        halfFilledDrawable = halfFilled?.mutate()
        useDrawable = true
        invalidate()
    }

    /**
     * Set custom drawables from resource IDs
     */
    fun setDrawables(filledResId: Int, emptyResId: Int, halfFilledResId: Int? = null) {
        filledDrawable = ContextCompat.getDrawable(context, filledResId)?.mutate()
        emptyDrawable = ContextCompat.getDrawable(context, emptyResId)?.mutate()
        halfFilledResId?.let {
            halfFilledDrawable = ContextCompat.getDrawable(context, it)?.mutate()
        }
        useDrawable = true
        invalidate()
    }

    /**
     * Set tint colors for drawables
     */
    fun setDrawableTint(filledTint: Int?, emptyTint: Int?) {
        drawableTintFilled = filledTint
        drawableTintEmpty = emptyTint
        invalidate()
    }

    /**
     * Clear custom drawables and use default star shape
     */
    fun clearDrawables() {
        filledDrawable = null
        emptyDrawable = null
        halfFilledDrawable = null
        useDrawable = false
        invalidate()
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
