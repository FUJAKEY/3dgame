package com.example.forestgame;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {
    private GameSurfaceView surfaceView;
    private JoystickView joystickView;
    private Handler uiHandler;
    private final Runnable statsUpdater = new Runnable() {
        @Override
        public void run() {
            if (surfaceView != null) {
                int coins = GameNativeBridge.nativeGetCollectedCoins();
                int total = GameNativeBridge.nativeGetTotalCoins();
                TextView coinCounter = findViewById(R.id.coinCounter);
                coinCounter.setText(getString(R.string.label_coin_count, coins, total));

                float fps = GameNativeBridge.nativeGetCurrentFps();
                TextView fpsCounter = findViewById(R.id.fpsCounter);
                fpsCounter.setText(getString(R.string.label_fps, fps));
            }
            uiHandler.postDelayed(this, 500);
        }
    };

    private int lookPointerId = -1;
    private float lastLookX;
    private float lastLookY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        uiHandler = new Handler(Looper.getMainLooper());

        SharedPreferences preferences = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        int quality = preferences.getInt(SettingsActivity.KEY_QUALITY, 1);

        FrameLayout frameLayout = findViewById(R.id.gameFrame);
        surfaceView = new GameSurfaceView(this, quality);
        surfaceView.setKeepScreenOn(true);
        frameLayout.addView(surfaceView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        joystickView = new JoystickView(this);
        FrameLayout.LayoutParams joystickParams = new FrameLayout.LayoutParams(dpToPx(180), dpToPx(180));
        joystickParams.gravity = Gravity.START | Gravity.BOTTOM;
        joystickParams.setMargins(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24));
        frameLayout.addView(joystickView, joystickParams);

        joystickView.setOnMoveListener((horizontal, vertical) ->
                surfaceView.queueEvent(() -> GameNativeBridge.nativeSetMovement(horizontal, vertical)));

        surfaceView.setOnTouchListener(this::handleLookTouch);
    }

    private boolean handleLookTouch(View view, MotionEvent event) {
        int width = view.getWidth();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                int index = event.getActionIndex();
                float x = event.getX(index);
                if (x > width * 0.4f) {
                    lookPointerId = event.getPointerId(index);
                    lastLookX = x;
                    lastLookY = event.getY(index);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (lookPointerId != -1) {
                    int pointerIndex = event.findPointerIndex(lookPointerId);
                    if (pointerIndex != -1) {
                        float x = event.getX(pointerIndex);
                        float y = event.getY(pointerIndex);
                        final float dx = x - lastLookX;
                        final float dy = y - lastLookY;
                        lastLookX = x;
                        lastLookY = y;
                        surfaceView.queueEvent(() -> GameNativeBridge.nativeLookDelta(dx, dy));
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                int pointerId = event.getPointerId(event.getActionIndex());
                if (pointerId == lookPointerId) {
                    lookPointerId = -1;
                }
                break;
            }
            default:
                break;
        }
        return true;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onResume() {
        super.onResume();
        surfaceView.onResume();
        uiHandler.post(statsUpdater);
    }

    @Override
    protected void onPause() {
        uiHandler.removeCallbacks(statsUpdater);
        surfaceView.onPause();
        super.onPause();
    }
}
