package com.example.forestquest.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class LookControllerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    interface LookListener {
        fun onLook(deltaX: Float, deltaY: Float)
    }

    private var listener: LookListener? = null
    private var activePointerId: Int = MotionEvent.INVALID_POINTER_ID
    private var lastX: Float = 0f
    private var lastY: Float = 0f

    fun setOnLookListener(listener: LookListener) {
        this.listener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = event.getPointerId(0)
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                activePointerId = event.getPointerId(index)
                lastX = event.getX(index)
                lastY = event.getY(index)
            }
            MotionEvent.ACTION_MOVE -> {
                val index = event.findPointerIndex(activePointerId)
                if (index != -1) {
                    val x = event.getX(index)
                    val y = event.getY(index)
                    val dx = x - lastX
                    val dy = y - lastY
                    lastX = x
                    lastY = y
                    listener?.onLook(dx, dy)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                if (event.getPointerId(event.actionIndex) == activePointerId) {
                    activePointerId = MotionEvent.INVALID_POINTER_ID
                }
            }
        }
        return true
    }
}
