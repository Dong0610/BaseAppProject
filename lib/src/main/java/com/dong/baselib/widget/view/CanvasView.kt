package com.dong.baselib.widget.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class CanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val builder = CanvasBuilder()
    private var viewWidth = 0f
    private var viewHeight = 0f

    init {
        setWillNotDraw(false)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w.toFloat()
        viewHeight = h.toFloat()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (viewWidth == 0f || viewHeight == 0f) return
        builder.width = viewWidth
        builder.height = viewHeight
        builder.canvasDrawCommands.forEach { it(canvas) }
    }

    fun drawCanvas(builderConfig: CanvasBuilder.() -> Unit) {
        post {
            builder.canvasDrawCommands.clear()
            builder.apply(builderConfig)
            invalidate()
        }
    }

    fun clearCanvas() {
        builder.canvasDrawCommands.clear()
        invalidate()
    }

    inner class CanvasBuilder {
        val canvasDrawCommands = mutableListOf<(Canvas) -> Unit>()
        var width = 0f
            internal set
        var height = 0f
            internal set

        // ==================== Basic Shapes ====================

        fun drawCircle(cx: Float, cy: Float, radius: Float, paint: Paint) {
            canvasDrawCommands.add { canvas -> canvas.drawCircle(cx, cy, radius, paint) }
        }

        fun drawRect(left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
            canvasDrawCommands.add { canvas -> canvas.drawRect(left, top, right, bottom, paint) }
        }

        fun drawRect(rect: RectF, paint: Paint) {
            canvasDrawCommands.add { canvas -> canvas.drawRect(rect, paint) }
        }

        fun drawRoundRect(
            left: Float, top: Float, right: Float, bottom: Float,
            rx: Float, ry: Float, paint: Paint
        ) {
            canvasDrawCommands.add { canvas ->
                canvas.drawRoundRect(left, top, right, bottom, rx, ry, paint)
            }
        }

        fun drawRoundRect(rect: RectF, rx: Float, ry: Float, paint: Paint) {
            canvasDrawCommands.add { canvas -> canvas.drawRoundRect(rect, rx, ry, paint) }
        }

        fun drawOval(left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
            canvasDrawCommands.add { canvas -> canvas.drawOval(left, top, right, bottom, paint) }
        }

        fun drawOval(rect: RectF, paint: Paint) {
            canvasDrawCommands.add { canvas -> canvas.drawOval(rect, paint) }
        }

        fun drawArc(
            left: Float, top: Float, right: Float, bottom: Float,
            startAngle: Float, sweepAngle: Float, useCenter: Boolean, paint: Paint
        ) {
            canvasDrawCommands.add { canvas ->
                canvas.drawArc(left, top, right, bottom, startAngle, sweepAngle, useCenter, paint)
            }
        }

        fun drawArc(rect: RectF, startAngle: Float, sweepAngle: Float, useCenter: Boolean, paint: Paint) {
            canvasDrawCommands.add { canvas ->
                canvas.drawArc(rect, startAngle, sweepAngle, useCenter, paint)
            }
        }

        // ==================== Lines ====================

        fun drawLine(startX: Float, startY: Float, endX: Float, endY: Float, paint: Paint) {
            canvasDrawCommands.add { canvas -> canvas.drawLine(startX, startY, endX, endY, paint) }
        }

        fun drawLine(start: PointF, end: PointF, paint: Paint) {
            drawLine(start.x, start.y, end.x, end.y, paint)
        }

        fun drawLines(points: FloatArray, paint: Paint) {
            canvasDrawCommands.add { canvas -> canvas.drawLines(points, paint) }
        }

        fun drawDashedLine(
            startX: Float, startY: Float, endX: Float, endY: Float,
            paint: Paint, dashWidth: Float = 10f, dashGap: Float = 10f
        ) {
            val dashPaint = Paint(paint).apply {
                pathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
            }
            canvasDrawCommands.add { canvas ->
                canvas.drawLine(startX, startY, endX, endY, dashPaint)
            }
        }

        fun drawDottedLine(
            startX: Float, startY: Float, endX: Float, endY: Float,
            paint: Paint, dotRadius: Float = 3f, gap: Float = 6f
        ) {
            val dottedPaint = Paint(paint).apply {
                pathEffect = DashPathEffect(floatArrayOf(dotRadius, gap), 0f)
                strokeCap = Paint.Cap.ROUND
            }
            canvasDrawCommands.add { canvas ->
                canvas.drawLine(startX, startY, endX, endY, dottedPaint)
            }
        }

        // ==================== Text ====================

        fun drawText(text: String, x: Float, y: Float, paint: Paint) {
            canvasDrawCommands.add { canvas -> canvas.drawText(text, x, y, paint) }
        }

        fun drawTextCentered(text: String, cx: Float, cy: Float, paint: Paint) {
            canvasDrawCommands.add { canvas ->
                val textBounds = Rect()
                paint.getTextBounds(text, 0, text.length, textBounds)
                val x = cx - textBounds.exactCenterX()
                val y = cy - textBounds.exactCenterY()
                canvas.drawText(text, x, y, paint)
            }
        }

        fun drawTextOnPath(text: String, path: Path, hOffset: Float, vOffset: Float, paint: Paint) {
            canvasDrawCommands.add { canvas ->
                canvas.drawTextOnPath(text, path, hOffset, vOffset, paint)
            }
        }

        fun drawTextOnCircle(text: String, cx: Float, cy: Float, radius: Float, startAngle: Float, paint: Paint) {
            val path = Path().apply {
                addCircle(cx, cy, radius, Path.Direction.CW)
            }
            val pathMeasure = PathMeasure(path, false)
            val startOffset = pathMeasure.length * startAngle / 360f
            canvasDrawCommands.add { canvas ->
                canvas.drawTextOnPath(text, path, startOffset, 0f, paint)
            }
        }

        // ==================== Path ====================

        fun drawPath(path: Path, paint: Paint) {
            canvasDrawCommands.add { canvas -> canvas.drawPath(path, paint) }
        }

        fun drawBezierCurve(start: PointF, control1: PointF, control2: PointF, end: PointF, paint: Paint) {
            val path = Path().apply {
                moveTo(start.x, start.y)
                cubicTo(control1.x, control1.y, control2.x, control2.y, end.x, end.y)
            }
            drawPath(path, paint)
        }

        fun drawQuadBezier(start: PointF, control: PointF, end: PointF, paint: Paint) {
            val path = Path().apply {
                moveTo(start.x, start.y)
                quadTo(control.x, control.y, end.x, end.y)
            }
            drawPath(path, paint)
        }

        // ==================== Polygons ====================

        fun drawTriangle(p1: PointF, p2: PointF, p3: PointF, paint: Paint) {
            val path = Path().apply {
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                close()
            }
            drawPath(path, paint)
        }

        fun drawPolygon(cx: Float, cy: Float, radius: Float, sides: Int, rotation: Float, paint: Paint) {
            if (sides < 3) return
            val path = Path()
            val angleStep = 2 * PI / sides
            val startAngle = Math.toRadians(rotation.toDouble()) - PI / 2

            for (i in 0 until sides) {
                val angle = startAngle + i * angleStep
                val x = cx + radius * cos(angle).toFloat()
                val y = cy + radius * sin(angle).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, paint)
        }

        fun drawPentagon(cx: Float, cy: Float, radius: Float, rotation: Float, paint: Paint) {
            drawPolygon(cx, cy, radius, 5, rotation, paint)
        }

        fun drawHexagon(cx: Float, cy: Float, radius: Float, rotation: Float, paint: Paint) {
            drawPolygon(cx, cy, radius, 6, rotation, paint)
        }

        fun drawOctagon(cx: Float, cy: Float, radius: Float, rotation: Float, paint: Paint) {
            drawPolygon(cx, cy, radius, 8, rotation, paint)
        }

        fun drawShapeWithPoints(points: List<PointF>, paint: Paint) {
            if (points.size < 2) return
            val path = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
                close()
            }
            drawPath(path, paint)
        }

        fun drawRoundedPolygon(points: List<PointF>, radius: Float, paint: Paint) {
            if (points.size < 3) return
            drawRoundedShape(points, radius, paint)
        }

        fun drawRoundedShape(points: List<PointF>, radius: Float, paint: Paint) {
            if (points.size < 2) return
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

                val scaledIn = vectorIn.scale(radius / lengthIn)
                val scaledOut = vectorOut.scale(radius / lengthOut)

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

        // ==================== Special Shapes ====================

        fun drawStar(cx: Float, cy: Float, outerRadius: Float, innerRadius: Float, points: Int, rotation: Float, paint: Paint) {
            if (points < 3) return
            val path = Path()
            val angleStep = PI / points
            val startAngle = Math.toRadians(rotation.toDouble()) - PI / 2

            for (i in 0 until points * 2) {
                val radius = if (i % 2 == 0) outerRadius else innerRadius
                val angle = startAngle + i * angleStep
                val x = cx + radius * cos(angle).toFloat()
                val y = cy + radius * sin(angle).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, paint)
        }

        fun drawHeart(cx: Float, cy: Float, size: Float, paint: Paint) {
            val path = Path()
            val halfSize = size / 2

            path.moveTo(cx, cy + halfSize * 0.7f)
            path.cubicTo(
                cx - halfSize * 1.5f, cy - halfSize * 0.5f,
                cx - halfSize * 0.5f, cy - halfSize * 1.2f,
                cx, cy - halfSize * 0.4f
            )
            path.cubicTo(
                cx + halfSize * 0.5f, cy - halfSize * 1.2f,
                cx + halfSize * 1.5f, cy - halfSize * 0.5f,
                cx, cy + halfSize * 0.7f
            )
            path.close()
            drawPath(path, paint)
        }

        fun drawDiamond(cx: Float, cy: Float, width: Float, height: Float, paint: Paint) {
            val path = Path().apply {
                moveTo(cx, cy - height / 2)
                lineTo(cx + width / 2, cy)
                lineTo(cx, cy + height / 2)
                lineTo(cx - width / 2, cy)
                close()
            }
            drawPath(path, paint)
        }

        fun drawArrow(
            startX: Float, startY: Float, endX: Float, endY: Float,
            headLength: Float, headAngle: Float, paint: Paint
        ) {
            drawLine(startX, startY, endX, endY, paint)

            val angle = atan2((endY - startY).toDouble(), (endX - startX).toDouble())
            val headRad = Math.toRadians(headAngle.toDouble())

            val x1 = endX - headLength * cos(angle - headRad).toFloat()
            val y1 = endY - headLength * sin(angle - headRad).toFloat()
            val x2 = endX - headLength * cos(angle + headRad).toFloat()
            val y2 = endY - headLength * sin(angle + headRad).toFloat()

            drawLine(endX, endY, x1, y1, paint)
            drawLine(endX, endY, x2, y2, paint)
        }

        fun drawArrowFilled(
            startX: Float, startY: Float, endX: Float, endY: Float,
            headLength: Float, headWidth: Float, paint: Paint
        ) {
            val angle = atan2((endY - startY).toDouble(), (endX - startX).toDouble())
            val sin = sin(angle).toFloat()
            val cos = cos(angle).toFloat()

            // Arrow body (line)
            val bodyEndX = endX - headLength * cos
            val bodyEndY = endY - headLength * sin
            drawLine(startX, startY, bodyEndX, bodyEndY, paint)

            // Arrow head (triangle)
            val halfWidth = headWidth / 2
            val path = Path().apply {
                moveTo(endX, endY)
                lineTo(bodyEndX - halfWidth * sin, bodyEndY + halfWidth * cos)
                lineTo(bodyEndX + halfWidth * sin, bodyEndY - halfWidth * cos)
                close()
            }
            val fillPaint = Paint(paint).apply { style = Paint.Style.FILL }
            drawPath(path, fillPaint)
        }

        fun drawCross(cx: Float, cy: Float, size: Float, thickness: Float, paint: Paint) {
            val half = size / 2
            val halfT = thickness / 2
            drawRect(cx - half, cy - halfT, cx + half, cy + halfT, paint)
            drawRect(cx - halfT, cy - half, cx + halfT, cy + half, paint)
        }

        fun drawPlus(cx: Float, cy: Float, size: Float, thickness: Float, paint: Paint) {
            drawCross(cx, cy, size, thickness, paint)
        }

        fun drawX(cx: Float, cy: Float, size: Float, paint: Paint) {
            val half = size / 2
            drawLine(cx - half, cy - half, cx + half, cy + half, paint)
            drawLine(cx - half, cy + half, cx + half, cy - half, paint)
        }

        fun drawCheckmark(startX: Float, startY: Float, size: Float, paint: Paint) {
            val path = Path().apply {
                moveTo(startX, startY + size * 0.5f)
                lineTo(startX + size * 0.35f, startY + size * 0.85f)
                lineTo(startX + size, startY + size * 0.15f)
            }
            val strokePaint = Paint(paint).apply { style = Paint.Style.STROKE }
            drawPath(path, strokePaint)
        }

        // ==================== Ring & Donut ====================

        fun drawRing(cx: Float, cy: Float, outerRadius: Float, innerRadius: Float, paint: Paint) {
            val path = Path().apply {
                addCircle(cx, cy, outerRadius, Path.Direction.CW)
                addCircle(cx, cy, innerRadius, Path.Direction.CCW)
            }
            drawPath(path, paint)
        }

        fun drawArcRing(
            cx: Float, cy: Float, outerRadius: Float, innerRadius: Float,
            startAngle: Float, sweepAngle: Float, paint: Paint
        ) {
            val path = Path()
            val outerRect = RectF(cx - outerRadius, cy - outerRadius, cx + outerRadius, cy + outerRadius)
            val innerRect = RectF(cx - innerRadius, cy - innerRadius, cx + innerRadius, cy + innerRadius)

            path.arcTo(outerRect, startAngle, sweepAngle, true)
            path.arcTo(innerRect, startAngle + sweepAngle, -sweepAngle)
            path.close()

            drawPath(path, paint)
        }

        // ==================== Progress Indicators ====================

        fun drawProgressArc(
            cx: Float, cy: Float, radius: Float, strokeWidth: Float,
            progress: Float, bgColor: Int, progressColor: Int
        ) {
            val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

            // Background arc
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = bgColor
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                strokeCap = Paint.Cap.ROUND
            }
            canvasDrawCommands.add { canvas -> canvas.drawArc(rect, 0f, 360f, false, bgPaint) }

            // Progress arc
            val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = progressColor
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                strokeCap = Paint.Cap.ROUND
            }
            val sweepAngle = 360f * progress.coerceIn(0f, 1f)
            canvasDrawCommands.add { canvas ->
                canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)
            }
        }

        fun drawProgressBar(
            left: Float, top: Float, right: Float, bottom: Float,
            progress: Float, bgColor: Int, progressColor: Int, cornerRadius: Float = 0f
        ) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = bgColor
                style = Paint.Style.FILL
            }
            val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = progressColor
                style = Paint.Style.FILL
            }

            val progressWidth = (right - left) * progress.coerceIn(0f, 1f)

            canvasDrawCommands.add { canvas ->
                canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, bgPaint)
                canvas.drawRoundRect(left, top, left + progressWidth, bottom, cornerRadius, cornerRadius, progressPaint)
            }
        }

        // ==================== Patterns ====================

        fun drawGrid(
            left: Float, top: Float, right: Float, bottom: Float,
            cellWidth: Float, cellHeight: Float, paint: Paint
        ) {
            canvasDrawCommands.add { canvas ->
                var x = left
                while (x <= right) {
                    canvas.drawLine(x, top, x, bottom, paint)
                    x += cellWidth
                }
                var y = top
                while (y <= bottom) {
                    canvas.drawLine(left, y, right, y, paint)
                    y += cellHeight
                }
            }
        }

        fun drawDots(
            left: Float, top: Float, right: Float, bottom: Float,
            spacingX: Float, spacingY: Float, radius: Float, paint: Paint
        ) {
            canvasDrawCommands.add { canvas ->
                var x = left
                while (x <= right) {
                    var y = top
                    while (y <= bottom) {
                        canvas.drawCircle(x, y, radius, paint)
                        y += spacingY
                    }
                    x += spacingX
                }
            }
        }

        fun drawWave(
            startX: Float, startY: Float, endX: Float,
            amplitude: Float, wavelength: Float, paint: Paint
        ) {
            val path = Path()
            path.moveTo(startX, startY)

            var x = startX
            while (x <= endX) {
                val y = startY + amplitude * sin((x - startX) * 2 * PI / wavelength).toFloat()
                path.lineTo(x, y)
                x += 2f
            }

            val strokePaint = Paint(paint).apply { style = Paint.Style.STROKE }
            drawPath(path, strokePaint)
        }

        fun drawSpiral(cx: Float, cy: Float, maxRadius: Float, turns: Float, paint: Paint) {
            val path = Path()
            val totalAngle = turns * 2 * PI
            val steps = (totalAngle * 20).toInt()

            for (i in 0..steps) {
                val angle = i * totalAngle / steps
                val radius = maxRadius * i / steps
                val x = cx + radius * cos(angle).toFloat()
                val y = cy + radius * sin(angle).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            val strokePaint = Paint(paint).apply { style = Paint.Style.STROKE }
            drawPath(path, strokePaint)
        }

        // ==================== Charts ====================

        fun drawPieSlice(
            cx: Float, cy: Float, radius: Float,
            startAngle: Float, sweepAngle: Float, paint: Paint
        ) {
            val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
            canvasDrawCommands.add { canvas ->
                canvas.drawArc(rect, startAngle, sweepAngle, true, paint)
            }
        }

        fun drawPieChart(cx: Float, cy: Float, radius: Float, values: List<Float>, colors: List<Int>) {
            val total = values.sum()
            var startAngle = -90f

            for (i in values.indices) {
                val sweepAngle = 360f * values[i] / total
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = colors[i % colors.size]
                    style = Paint.Style.FILL
                }
                drawPieSlice(cx, cy, radius, startAngle, sweepAngle, paint)
                startAngle += sweepAngle
            }
        }

        fun drawDonutChart(
            cx: Float, cy: Float, outerRadius: Float, innerRadius: Float,
            values: List<Float>, colors: List<Int>
        ) {
            val total = values.sum()
            var startAngle = -90f

            for (i in values.indices) {
                val sweepAngle = 360f * values[i] / total
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = colors[i % colors.size]
                    style = Paint.Style.FILL
                }
                drawArcRing(cx, cy, outerRadius, innerRadius, startAngle, sweepAngle, paint)
                startAngle += sweepAngle
            }
        }

        // ==================== Bitmap ====================

        fun drawBitmap(bitmap: Bitmap, left: Float, top: Float, paint: Paint? = null) {
            canvasDrawCommands.add { canvas -> canvas.drawBitmap(bitmap, left, top, paint) }
        }

        fun drawBitmap(bitmap: Bitmap, src: Rect?, dst: RectF, paint: Paint? = null) {
            canvasDrawCommands.add { canvas -> canvas.drawBitmap(bitmap, src, dst, paint) }
        }

        fun drawBitmapCentered(bitmap: Bitmap, cx: Float, cy: Float, paint: Paint? = null) {
            val left = cx - bitmap.width / 2f
            val top = cy - bitmap.height / 2f
            drawBitmap(bitmap, left, top, paint)
        }

        // ==================== Canvas Transformations ====================

        fun save() {
            canvasDrawCommands.add { canvas -> canvas.save() }
        }

        fun restore() {
            canvasDrawCommands.add { canvas -> canvas.restore() }
        }

        fun translate(dx: Float, dy: Float) {
            canvasDrawCommands.add { canvas -> canvas.translate(dx, dy) }
        }

        fun rotate(degrees: Float, px: Float = 0f, py: Float = 0f) {
            canvasDrawCommands.add { canvas -> canvas.rotate(degrees, px, py) }
        }

        fun scale(sx: Float, sy: Float, px: Float = 0f, py: Float = 0f) {
            canvasDrawCommands.add { canvas -> canvas.scale(sx, sy, px, py) }
        }

        fun skew(sx: Float, sy: Float) {
            canvasDrawCommands.add { canvas -> canvas.skew(sx, sy) }
        }

        fun clipRect(left: Float, top: Float, right: Float, bottom: Float) {
            canvasDrawCommands.add { canvas -> canvas.clipRect(left, top, right, bottom) }
        }

        fun clipPath(path: Path) {
            canvasDrawCommands.add { canvas -> canvas.clipPath(path) }
        }

        // ==================== Paint Extensions ====================

        fun Paint.applyShadow(
            radius: Float = 10f, dx: Float = 5f, dy: Float = 5f, shadowColor: Int = Color.BLACK
        ) {
            this.setShadowLayer(radius, dx, dy, shadowColor)
        }

        fun Paint.applyGradient(
            colors: IntArray, positions: FloatArray? = null, angle: Float = 0f, bounds: RectF
        ) {
            val radian = Math.toRadians(angle.toDouble())
            val x0 = bounds.left
            val y0 = bounds.top
            val x1 = bounds.right * cos(radian).toFloat()
            val y1 = bounds.bottom * sin(radian).toFloat()
            this.shader = LinearGradient(x0, y0, x1, y1, colors, positions, Shader.TileMode.CLAMP)
        }

        fun Paint.applyLinearGradient(
            x0: Float, y0: Float, x1: Float, y1: Float,
            colors: IntArray, positions: FloatArray? = null
        ) {
            this.shader = LinearGradient(x0, y0, x1, y1, colors, positions, Shader.TileMode.CLAMP)
        }

        fun Paint.applyRadialGradient(
            cx: Float, cy: Float, radius: Float,
            colors: IntArray, positions: FloatArray? = null
        ) {
            this.shader = RadialGradient(cx, cy, radius, colors, positions, Shader.TileMode.CLAMP)
        }

        fun Paint.applySweepGradient(cx: Float, cy: Float, colors: IntArray, positions: FloatArray? = null) {
            this.shader = SweepGradient(cx, cy, colors, positions)
        }

        // ==================== Paint Factory ====================

        fun fillPaint(color: Int, alpha: Int = 255): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.alpha = alpha
            style = Paint.Style.FILL
        }

        fun strokePaint(color: Int, width: Float, cap: Paint.Cap = Paint.Cap.ROUND): Paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                strokeWidth = width
                strokeCap = cap
                style = Paint.Style.STROKE
            }

        fun textPaint(color: Int, textSize: Float, typeface: Typeface? = null): Paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                this.textSize = textSize
                this.typeface = typeface
            }

        // ==================== Helper Extensions ====================

        private fun PointF.length(): Float = hypot(this.x.toDouble(), this.y.toDouble()).toFloat()

        private fun PointF.scale(scalar: Float): PointF = PointF(this.x * scalar, this.y * scalar)

        // ==================== Convenience Methods ====================

        fun center(): PointF = PointF(width / 2f, height / 2f)

        fun centerX(): Float = width / 2f

        fun centerY(): Float = height / 2f
    }
}
