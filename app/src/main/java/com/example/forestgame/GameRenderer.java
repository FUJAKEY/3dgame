package com.example.forestgame;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameRenderer implements GLSurfaceView.Renderer {
    private final int initialQuality;
    private boolean initialized = false;

    public GameRenderer(int qualityLevel) {
        this.initialQuality = qualityLevel;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Native renderer handles all GL initialization.
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (!initialized) {
            GameNativeBridge.nativeInit(width, height, initialQuality);
            initialized = true;
        } else {
            GameNativeBridge.nativeResize(width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GameNativeBridge.nativeRender();
    }

    public void updateQuality(int qualityLevel) {
        GameNativeBridge.nativeSetQuality(qualityLevel);
    }
}
