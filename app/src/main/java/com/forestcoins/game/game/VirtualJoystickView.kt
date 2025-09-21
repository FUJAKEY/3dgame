package com.forestcoins.game.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.forestcoins.game.R
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

class VirtualJoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.joystick_base)
        style = Paint.Style.FILL
    }
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.joystick_handle)
        style = Paint.Style.FILL
    }

    private var baseRadius = 0f
    private var handleRadius = 0f
    private var centerX = 0f
    private var centerY = 0f

    private var handleX = 0f
    private var handleY = 0f
    private var activePointerId = MotionEvent.INVALID_POINTER_ID

    private var listener: ((Float, Float) -> Unit)? = null

    init {
        isClickable = true
    }

    fun setJoystickListener(listener: (Float, Float) -> Unit) {
        this.listener = listener
    }

    fun isPointerInBounds(rawX: Float, rawY: Float): Boolean {
        val location = IntArray(2)
        getLocationOnScreen(location)
        val left = location[0]
        val top = location[1]
        return rawX >= left && rawX <= left + width && rawY >= top && rawY <= top + height
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val diameter = min(w, h).toFloat()
        baseRadius = diameter / 2f
        handleRadius = baseRadius / 2.8f
        centerX = w / 2f
        centerY = h / 2f
        resetHandle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint)
        canvas.drawCircle(centerX + handleX, centerY + handleY, handleRadius, handlePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                if (activePointerId == MotionEvent.INVALID_POINTER_ID) {
                    val x = event.getX(index)
                    val y = event.getY(index)
                    if (isWithinBase(x, y)) {
                        activePointerId = event.getPointerId(index)
                        updateHandle(x, y)
                        return true
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(activePointerId)
                if (pointerIndex != -1) {
                    updateHandle(event.getX(pointerIndex), event.getY(pointerIndex))
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                val index = event.actionIndex
                if (event.getPointerId(index) == activePointerId || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                    resetHandle()
                    activePointerId = MotionEvent.INVALID_POINTER_ID
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isWithinBase(x: Float, y: Float): Boolean {
        val distance = hypot(x - centerX, y - centerY)
        return distance <= baseRadius
    }

    private fun updateHandle(x: Float, y: Float) {
        val dx = x - centerX
        val dy = y - centerY
        val distance = hypot(dx, dy)
        val maxDistance = baseRadius - handleRadius
        val clampedDistance = min(distance, maxDistance)
        val angle = atan2(dy, dx)
        handleX = (clampedDistance * kotlin.math.cos(angle)).toFloat()
        handleY = (clampedDistance * kotlin.math.sin(angle)).toFloat()
        invalidate()
        val normalizedX = (handleX / maxDistance).coerceIn(-1f, 1f)
        val normalizedY = (handleY / maxDistance).coerceIn(-1f, 1f)
        listener?.invoke(normalizedX, normalizedY)
    }

    private fun resetHandle() {
        handleX = 0f
        handleY = 0f
        invalidate()
        listener?.invoke(0f, 0f)
    }
}
