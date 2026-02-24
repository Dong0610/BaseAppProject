package com.dong.baselib.canvas

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.createBitmap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

// ============================================================================
// region NativeCanvasView - Custom View for Canvas Drawing
// ============================================================================

class NativeCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var drawAction: (Canvas.() -> Unit)? = null

    fun draw(action: Canvas.() -> Unit) {
        drawAction = action
        invalidate()
    }

    fun redraw() = invalidate()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawAction?.invoke(canvas)
    }
}

// endregion

// ============================================================================
// region ViewGroup - Canvas Extensions
// ============================================================================

fun ViewGroup.nativeCanvas(
    elevation: Float = 0f,
    action: Canvas.() -> Unit
): NativeCanvasView {
    val canvasView = NativeCanvasView(context).apply {
        layoutParams = createMatchParentLayoutParams()
        this.elevation = elevation
        draw(action)
    }

    post {
        if (canvasView.parent == null) {
            addView(canvasView)
        }
    }

    return canvasView
}

fun ViewGroup.createMatchParentLayoutParams(): ViewGroup.LayoutParams = when (this) {
    is LinearLayout -> LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT
    )
    is FrameLayout -> FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
    )
    is RelativeLayout -> RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.MATCH_PARENT,
        RelativeLayout.LayoutParams.MATCH_PARENT
    )
    is ConstraintLayout -> ConstraintLayout.LayoutParams(
        ConstraintLayout.LayoutParams.MATCH_PARENT,
        ConstraintLayout.LayoutParams.MATCH_PARENT
    )
    else -> ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
}

// endregion

// ============================================================================
// region Canvas - Basic Shape Drawing
// ============================================================================

fun Canvas.drawTriangle(paint: Paint, p1: PointF, p2: PointF, p3: PointF) {
    val path = Path().apply {
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        close()
    }
    drawPath(path, paint)
}

fun Canvas.drawTriangle(paint: Paint, p1: Point, p2: Point, p3: Point) {
    drawTriangle(paint, p1.toPointF(), p2.toPointF(), p3.toPointF())
}

fun Canvas.drawPolygon(paint: Paint, points: List<PointF>) {
    if (points.size < 3) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
        close()
    }
    drawPath(path, paint)
}

fun Canvas.drawPolygon(paint: Paint, vararg points: PointF) {
    drawPolygon(paint, points.toList())
}

fun Canvas.drawRegularPolygon(
    paint: Paint,
    centerX: Float,
    centerY: Float,
    radius: Float,
    sides: Int,
    rotationDegrees: Float = 0f
) {
    if (sides < 3) return

    val angleStep = (2 * PI / sides).toFloat()
    val startAngle = Math.toRadians(rotationDegrees.toDouble()).toFloat() - (PI / 2).toFloat()

    val points = (0 until sides).map { i ->
        val angle = startAngle + i * angleStep
        PointF(
            centerX + radius * cos(angle),
            centerY + radius * sin(angle)
        )
    }
    drawPolygon(paint, points)
}

fun Canvas.drawStar(
    paint: Paint,
    centerX: Float,
    centerY: Float,
    outerRadius: Float,
    innerRadius: Float,
    points: Int = 5,
    rotationDegrees: Float = 0f
) {
    if (points < 3) return

    val angleStep = (PI / points).toFloat()
    val startAngle = Math.toRadians(rotationDegrees.toDouble()).toFloat() - (PI / 2).toFloat()

    val starPoints = mutableListOf<PointF>()
    for (i in 0 until points * 2) {
        val angle = startAngle + i * angleStep
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        starPoints.add(PointF(
            centerX + radius * cos(angle),
            centerY + radius * sin(angle)
        ))
    }
    drawPolygon(paint, starPoints)
}

// endregion

// ============================================================================
// region Canvas - Rounded Shape Drawing
// ============================================================================

fun Canvas.drawRoundedPolygon(paint: Paint, points: List<PointF>, cornerRadius: Float) {
    if (points.size < 3) return

    val path = Path()
    val pointCount = points.size

    for (i in points.indices) {
        val current = points[i]
        val next = points[(i + 1) % pointCount]
        val previous = points[(i - 1 + pointCount) % pointCount]

        val vectorIn = PointF(current.x - previous.x, current.y - previous.y)
        val vectorOut = PointF(next.x - current.x, next.y - current.y)

        val lengthIn = vectorIn.length()
        val lengthOut = vectorOut.length()

        val scaledIn = vectorIn.scale(cornerRadius / lengthIn)
        val scaledOut = vectorOut.scale(cornerRadius / lengthOut)

        val cornerStart = PointF(current.x - scaledIn.x, current.y - scaledIn.y)
        val cornerEnd = PointF(current.x + scaledOut.x, current.y + scaledOut.y)

        if (i == 0) {
            path.moveTo(cornerStart.x, cornerStart.y)
        } else {
            path.lineTo(cornerStart.x, cornerStart.y)
        }
        path.quadTo(current.x, current.y, cornerEnd.x, cornerEnd.y)
    }

    path.close()
    drawPath(path, paint)
}

fun Canvas.drawRoundedRect(
    paint: Paint,
    rectF: RectF,
    cornerRadius: Float,
    topLeft: Boolean = true,
    topRight: Boolean = true,
    bottomLeft: Boolean = true,
    bottomRight: Boolean = true
) {
    val path = Path()

    val strokeInset = if (paint.style == Paint.Style.STROKE) paint.strokeWidth / 2 else 0f
    val insetRect = RectF(
        rectF.left + strokeInset,
        rectF.top + strokeInset,
        rectF.right - strokeInset,
        rectF.bottom - strokeInset
    )

    val width = insetRect.width()
    val height = insetRect.height()
    val halfSize = width / 2

    // Draw circle if square with radius >= half size
    if (width == height && cornerRadius >= halfSize) {
        drawCircle(insetRect.centerX(), insetRect.centerY(), halfSize, paint)
        return
    }

    val maxRadius = min(width, height) / 2
    val safeRadius = cornerRadius.coerceAtMost(maxRadius)

    path.moveTo(insetRect.left, insetRect.bottom - if (bottomLeft) safeRadius else 0f)

    // Top-left corner
    if (topLeft) {
        path.lineTo(insetRect.left, insetRect.top + safeRadius)
        path.quadTo(insetRect.left, insetRect.top, insetRect.left + safeRadius, insetRect.top)
    } else {
        path.lineTo(insetRect.left, insetRect.top)
    }

    // Top-right corner
    if (topRight) {
        path.lineTo(insetRect.right - safeRadius, insetRect.top)
        path.quadTo(insetRect.right, insetRect.top, insetRect.right, insetRect.top + safeRadius)
    } else {
        path.lineTo(insetRect.right, insetRect.top)
    }

    // Bottom-right corner
    if (bottomRight) {
        path.lineTo(insetRect.right, insetRect.bottom - safeRadius)
        path.quadTo(insetRect.right, insetRect.bottom, insetRect.right - safeRadius, insetRect.bottom)
    } else {
        path.lineTo(insetRect.right, insetRect.bottom)
    }

    // Bottom-left corner
    if (bottomLeft) {
        path.lineTo(insetRect.left + safeRadius, insetRect.bottom)
        path.quadTo(insetRect.left, insetRect.bottom, insetRect.left, insetRect.bottom - safeRadius)
    } else {
        path.lineTo(insetRect.left, insetRect.bottom)
    }

    path.close()
    drawPath(path, paint)
}

// endregion

// ============================================================================
// region Canvas - Line Drawing
// ============================================================================

fun Canvas.drawDashedLine(
    paint: Paint,
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    dashLength: Float = 10f,
    gapLength: Float = 5f
) {
    val dashedPaint = Paint(paint).apply {
        pathEffect = DashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
    }
    drawLine(startX, startY, endX, endY, dashedPaint)
}

fun Canvas.drawArrow(
    paint: Paint,
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    arrowHeadLength: Float = 20f,
    arrowHeadAngle: Float = 30f
) {
    // Draw line
    drawLine(startX, startY, endX, endY, paint)

    // Calculate arrow head
    val angle = kotlin.math.atan2((endY - startY).toDouble(), (endX - startX).toDouble())
    val angleRad = Math.toRadians(arrowHeadAngle.toDouble())

    val x1 = endX - arrowHeadLength * cos(angle - angleRad).toFloat()
    val y1 = endY - arrowHeadLength * sin(angle - angleRad).toFloat()
    val x2 = endX - arrowHeadLength * cos(angle + angleRad).toFloat()
    val y2 = endY - arrowHeadLength * sin(angle + angleRad).toFloat()

    val path = Path().apply {
        moveTo(endX, endY)
        lineTo(x1, y1)
        lineTo(x2, y2)
        close()
    }
    drawPath(path, Paint(paint).apply { style = Paint.Style.FILL })
}

// endregion

// ============================================================================
// region Canvas - Arc & Pie Drawing
// ============================================================================

fun Canvas.drawArcStroke(
    paint: Paint,
    rectF: RectF,
    startAngle: Float,
    sweepAngle: Float,
    strokeWidth: Float = paint.strokeWidth
) {
    val arcPaint = Paint(paint).apply {
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidth
    }
    drawArc(rectF, startAngle, sweepAngle, false, arcPaint)
}

fun Canvas.drawPieSlice(
    paint: Paint,
    rectF: RectF,
    startAngle: Float,
    sweepAngle: Float
) {
    drawArc(rectF, startAngle, sweepAngle, true, paint)
}

fun Canvas.drawDonut(
    paint: Paint,
    centerX: Float,
    centerY: Float,
    outerRadius: Float,
    innerRadius: Float
) {
    val path = Path().apply {
        addCircle(centerX, centerY, outerRadius, Path.Direction.CW)
        addCircle(centerX, centerY, innerRadius, Path.Direction.CCW)
    }
    drawPath(path, paint)
}

// endregion

// ============================================================================
// region Canvas - Gradient Drawing
// ============================================================================

fun Canvas.drawGradientRect(
    rectF: RectF,
    @ColorInt startColor: Int,
    @ColorInt endColor: Int,
    isHorizontal: Boolean = true
) {
    val shader = if (isHorizontal) {
        LinearGradient(
            rectF.left, rectF.centerY(),
            rectF.right, rectF.centerY(),
            startColor, endColor,
            Shader.TileMode.CLAMP
        )
    } else {
        LinearGradient(
            rectF.centerX(), rectF.top,
            rectF.centerX(), rectF.bottom,
            startColor, endColor,
            Shader.TileMode.CLAMP
        )
    }

    val paint = Paint().apply {
        this.shader = shader
    }
    drawRect(rectF, paint)
}

fun Canvas.drawGradientCircle(
    centerX: Float,
    centerY: Float,
    radius: Float,
    @ColorInt centerColor: Int,
    @ColorInt edgeColor: Int
) {
    val shader = RadialGradient(
        centerX, centerY, radius,
        centerColor, edgeColor,
        Shader.TileMode.CLAMP
    )
    val paint = Paint().apply {
        this.shader = shader
    }
    drawCircle(centerX, centerY, radius, paint)
}

// endregion

// ============================================================================
// region Canvas - Grid & Guide Drawing
// ============================================================================

fun Canvas.drawGrid(
    paint: Paint,
    rectF: RectF,
    cellWidth: Float,
    cellHeight: Float = cellWidth
) {
    var x = rectF.left
    while (x <= rectF.right) {
        drawLine(x, rectF.top, x, rectF.bottom, paint)
        x += cellWidth
    }

    var y = rectF.top
    while (y <= rectF.bottom) {
        drawLine(rectF.left, y, rectF.right, y, paint)
        y += cellHeight
    }
}

fun Canvas.drawCrosshair(
    paint: Paint,
    centerX: Float,
    centerY: Float,
    size: Float
) {
    val halfSize = size / 2
    drawLine(centerX - halfSize, centerY, centerX + halfSize, centerY, paint)
    drawLine(centerX, centerY - halfSize, centerX, centerY + halfSize, paint)
}

// endregion

// ============================================================================
// region Paint - Factory Functions
// ============================================================================

fun fillPaint(
    @ColorInt color: Int,
    isAntiAlias: Boolean = true
): Paint = Paint().apply {
    this.color = color
    style = Paint.Style.FILL
    this.isAntiAlias = isAntiAlias
}

fun strokePaint(
    @ColorInt color: Int,
    strokeWidth: Float = 2f,
    isAntiAlias: Boolean = true
): Paint = Paint().apply {
    this.color = color
    this.strokeWidth = strokeWidth
    style = Paint.Style.STROKE
    this.isAntiAlias = isAntiAlias
}

fun dashedPaint(
    @ColorInt color: Int,
    strokeWidth: Float = 2f,
    dashLength: Float = 10f,
    gapLength: Float = 5f,
    isAntiAlias: Boolean = true
): Paint = Paint().apply {
    this.color = color
    this.strokeWidth = strokeWidth
    style = Paint.Style.STROKE
    pathEffect = DashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
    this.isAntiAlias = isAntiAlias
}

// endregion

// ============================================================================
// region Bitmap - Canvas Factory
// ============================================================================

inline fun createBitmapWithCanvas(
    width: Int,
    height: Int,
    config: Bitmap.Config = Bitmap.Config.ARGB_8888,
    draw: Canvas.() -> Unit
): Bitmap {
    val bitmap = createBitmap(width, height, config)
    Canvas(bitmap).apply(draw)
    return bitmap
}

// endregion

// ============================================================================
// region PointF - Extensions
// ============================================================================

fun Point.toPointF(): PointF = PointF(x.toFloat(), y.toFloat())

fun PointF.length(): Float = hypot(x.toDouble(), y.toDouble()).toFloat()

fun PointF.scale(scalar: Float): PointF = PointF(x * scalar, y * scalar)

fun PointF.normalize(): PointF {
    val len = length()
    return if (len > 0) PointF(x / len, y / len) else PointF(0f, 0f)
}

fun PointF.distanceTo(other: PointF): Float =
    hypot((x - other.x).toDouble(), (y - other.y).toDouble()).toFloat()

fun PointF.midPointTo(other: PointF): PointF =
    PointF((x + other.x) / 2, (y + other.y) / 2)

operator fun PointF.plus(other: PointF): PointF = PointF(x + other.x, y + other.y)

operator fun PointF.minus(other: PointF): PointF = PointF(x - other.x, y - other.y)

operator fun PointF.times(scalar: Float): PointF = PointF(x * scalar, y * scalar)

operator fun PointF.div(scalar: Float): PointF = PointF(x / scalar, y / scalar)

// endregion

// ============================================================================
// region RectF - Extensions
// ============================================================================

fun RectF.scale(factor: Float): RectF {
    val newWidth = width() * factor
    val newHeight = height() * factor
    val dx = (width() - newWidth) / 2
    val dy = (height() - newHeight) / 2
    return RectF(left + dx, top + dy, right - dx, bottom - dy)
}

fun RectF.inset(inset: Float): RectF = RectF(
    left + inset,
    top + inset,
    right - inset,
    bottom - inset
)

fun RectF.toPoints(): List<PointF> = listOf(
    PointF(left, top),
    PointF(right, top),
    PointF(right, bottom),
    PointF(left, bottom)
)

// endregion