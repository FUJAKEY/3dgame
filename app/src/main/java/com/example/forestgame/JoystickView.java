package com.example.forestgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {
    public interface OnMoveListener {
        void onMove(float horizontal, float vertical);
    }

    private final Paint basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint knobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float baseRadius;
    private float knobRadius;
    private float centerX;
    private float centerY;
    private float knobX;
    private float knobY;
    private OnMoveListener onMoveListener;

    public JoystickView(Context context) {
        this(context, null);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        basePaint.setColor(Color.argb(120, 50, 70, 90));
        basePaint.setStyle(Paint.Style.FILL);
        knobPaint.setColor(Color.argb(180, 82, 224, 154));
        knobPaint.setStyle(Paint.Style.FILL);
        setAlpha(0.9f);
        setFocusable(true);
    }

    public void setOnMoveListener(OnMoveListener listener) {
        this.onMoveListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        baseRadius = Math.min(w, h) * 0.45f;
        knobRadius = baseRadius * 0.45f;
        centerX = w * 0.5f;
        centerY = h * 0.5f;
        resetKnobPosition();
    }

    private void resetKnobPosition() {
        knobX = centerX;
        knobY = centerY;
        invalidate();
        dispatchMove(0f, 0f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint);
        canvas.drawCircle(knobX, knobY, knobRadius, knobPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                float dx = x - centerX;
                float dy = y - centerY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float limitedDist = Math.min(dist, baseRadius);
                float scale = dist > 0 ? limitedDist / dist : 0f;
                knobX = centerX + dx * scale;
                knobY = centerY + dy * scale;
                invalidate();
                float normalizedX = (knobX - centerX) / baseRadius;
                float normalizedY = (centerY - knobY) / baseRadius;
                dispatchMove(normalizedX, normalizedY);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resetKnobPosition();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void dispatchMove(float horizontal, float vertical) {
        if (onMoveListener != null) {
            onMoveListener.onMove(horizontal, vertical);
        }
    }
}
