package com.example.forestgame;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GameSurfaceView extends GLSurfaceView {
    private final GameRenderer renderer;

    public GameSurfaceView(Context context, int qualityLevel) {
        this(context, null, qualityLevel);
    }

    public GameSurfaceView(Context context, AttributeSet attrs, int qualityLevel) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        renderer = new GameRenderer(qualityLevel);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public GameRenderer getRenderer() {
        return renderer;
    }
}
