package com.rehelp.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View

// A simple drawn map (no Google Maps needed). Positions are fractions (0..1) of the view size.
class DummyMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    data class Pin(val x: Float, val y: Float, val label: String, val color: Int, val letter: String)

    private var pins: List<Pin> = emptyList()
    private var route: List<PointF> = emptyList()

    private fun dp(v: Float) = v * resources.displayMetrics.density

    private val bgPaint = Paint().apply { color = Color.parseColor("#E6EFE3") }
    private val parkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#CFE5C8") }
    private val roadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = dp(10f)
    }
    private val routePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1565C0")
        style = Paint.Style.STROKE
        strokeWidth = dp(4f)
        strokeCap = Paint.Cap.ROUND
        pathEffect = DashPathEffect(floatArrayOf(dp(12f), dp(8f)), 0f)
    }
    private val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pinRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = dp(3f)
    }
    private val letterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = dp(16f)
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#202124")
        textSize = dp(12f)
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    private val labelBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(230, 255, 255, 255) }

    fun setData(pins: List<Pin>, route: List<PointF>) {
        this.pins = pins
        this.route = route
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        // Background, parks and a simple road grid
        canvas.drawRect(0f, 0f, w, h, bgPaint)
        canvas.drawRoundRect(RectF(w * 0.04f, h * 0.06f, w * 0.30f, h * 0.26f), dp(12f), dp(12f), parkPaint)
        canvas.drawRoundRect(RectF(w * 0.62f, h * 0.70f, w * 0.94f, h * 0.94f), dp(12f), dp(12f), parkPaint)
        for (x in listOf(0.22f, 0.50f, 0.78f)) canvas.drawLine(w * x, 0f, w * x, h, roadPaint)
        for (y in listOf(0.20f, 0.50f, 0.80f)) canvas.drawLine(0f, h * y, w, h * y, roadPaint)
        canvas.drawLine(0f, h * 0.92f, w * 0.65f, h * 0.08f, roadPaint)

        // Dashed route
        if (route.size >= 2) {
            val path = Path().apply {
                moveTo(route[0].x * w, route[0].y * h)
                for (i in 1 until route.size) lineTo(route[i].x * w, route[i].y * h)
            }
            canvas.drawPath(path, routePaint)
        }

        // Pins with labels
        for (p in pins) {
            val cx = p.x * w
            val cy = p.y * h
            pinPaint.color = p.color
            canvas.drawCircle(cx, cy, dp(18f), pinPaint)
            canvas.drawCircle(cx, cy, dp(18f), pinRingPaint)
            canvas.drawText(p.letter, cx, cy + dp(5f), letterPaint)

            val textWidth = labelPaint.measureText(p.label)
            val top = cy + dp(24f)
            canvas.drawRoundRect(
                RectF(cx - textWidth / 2 - dp(6f), top, cx + textWidth / 2 + dp(6f), top + dp(22f)),
                dp(6f), dp(6f), labelBgPaint
            )
            canvas.drawText(p.label, cx, top + dp(15f), labelPaint)
        }
    }
}
