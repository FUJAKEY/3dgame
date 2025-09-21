package com.example.forestquest.render

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class GameSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
    }

    fun initialize(renderer: GameRenderer) {
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}
