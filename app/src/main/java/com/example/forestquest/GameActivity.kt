package com.example.forestquest

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.forestquest.databinding.ActivityGameBinding
import com.example.forestquest.render.GameRenderer
import com.example.forestquest.ui.JoystickView
import com.example.forestquest.ui.LookControllerView

class GameActivity : AppCompatActivity(), GameRenderer.Callback {

    private lateinit var binding: ActivityGameBinding
    private lateinit var renderer: GameRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding.coinsCounter.text = getString(R.string.coins_collected, 0)

        val quality = GraphicsPreferences.getQuality(this)
        renderer = GameRenderer(quality, this)

        binding.gameSurface.initialize(renderer)
        binding.joystick.setOnMoveListener(object : JoystickView.OnMoveListener {
            override fun onMove(x: Float, y: Float) {
                renderer.setMovementInput(x, y)
            }

            override fun onStop() {
                renderer.setMovementInput(0f, 0f)
            }
        })

        binding.lookController.setOnLookListener(object : LookControllerView.LookListener {
            override fun onLook(deltaX: Float, deltaY: Float) {
                renderer.rotateCamera(deltaX, deltaY)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        binding.gameSurface.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.gameSurface.onPause()
    }

    override fun onCoinsChanged(collected: Int) {
        runOnUiThread {
            binding.coinsCounter.text = getString(R.string.coins_collected, collected)
        }
    }
}
