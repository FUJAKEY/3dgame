package com.forestcoins.game.game

import android.content.Context
import android.util.AttributeSet
import android.opengl.GLSurfaceView
import com.forestcoins.game.data.GraphicsQuality

class GameSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private var forestRenderer: GameRenderer? = null

    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
    }

    fun configure(quality: GraphicsQuality, listener: GameRenderer.GameStateListener) {
        if (forestRenderer != null) return
        val renderer = GameRenderer(quality)
        renderer.setGameStateListener(listener)
        forestRenderer = renderer
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun updateMovementVector(x: Float, y: Float) {
        forestRenderer?.updateMovementVector(x, y)
    }

    fun addLookDelta(dx: Float, dy: Float) {
        forestRenderer?.addLookDelta(dx, dy)
    }

    fun resetLook() {
        forestRenderer?.resetLook()
    }
}
