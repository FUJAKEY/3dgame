package com.example.forestquest.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class JoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    interface OnMoveListener {
        fun onMove(x: Float, y: Float)
        fun onStop()
    }

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#44303030")
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#66FFFFFF")
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#AA66BB6A")
        style = Paint.Style.FILL
    }

    private var listener: OnMoveListener? = null
    private var baseRadius: Float = 0f
    private var handleRadius: Float = 0f
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var handleX: Float = 0f
    private var handleY: Float = 0f

    fun setOnMoveListener(listener: OnMoveListener) {
        this.listener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val diameter = min(w, h).toFloat()
        baseRadius = diameter / 2f * 0.9f
        handleRadius = baseRadius * 0.35f
        centerX = w / 2f
        centerY = h / 2f
        resetHandle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint)
        canvas.drawCircle(centerX, centerY, baseRadius, borderPaint)
        canvas.drawCircle(handleX, handleY, handleRadius, handlePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y
                val dx = x - centerX
                val dy = y - centerY
                val distance = sqrt(dx * dx + dy * dy)
                val maxDistance = baseRadius
                if (distance > maxDistance) {
                    val angle = atan2(dy, dx)
                    handleX = centerX + cos(angle) * maxDistance
                    handleY = centerY + sin(angle) * maxDistance
                } else {
                    handleX = x
                    handleY = y
                }
                invalidate()
                val normalizedX = (handleX - centerX) / maxDistance
                val normalizedY = (handleY - centerY) / maxDistance
                listener?.onMove(normalizedX.coerceIn(-1f, 1f), normalizedY.coerceIn(-1f, 1f))
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                resetHandle()
                listener?.onStop()
                invalidate()
            }
        }
        return true
    }

    private fun resetHandle() {
        handleX = centerX
        handleY = centerY
    }
}
