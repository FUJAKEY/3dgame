package com.forestcoins.game

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.forestcoins.game.data.GraphicsSettingsRepository
import com.forestcoins.game.databinding.ActivityGameBinding
import com.forestcoins.game.game.GameRenderer
import kotlinx.coroutines.launch

class GameActivity : ComponentActivity() {

    private lateinit var binding: ActivityGameBinding
    private val settingsRepository by lazy { GraphicsSettingsRepository(applicationContext) }

    private var lookPointerId = MotionEvent.INVALID_POINTER_ID
    private var lastLookX = 0f
    private var lastLookY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            val quality = settingsRepository.readQuality()
            binding.gameSurfaceView.configure(quality, object : GameRenderer.GameStateListener {
                override fun onCoinCountChanged(collected: Int, total: Int) {
                    runOnUiThread {
                        binding.coinCounter.text = getString(R.string.coin_count, collected, total)
                    }
                }
            })
            binding.coinCounter.text = getString(R.string.coin_count, 0, quality.coinCount)
        }

        binding.joystick.setJoystickListener { x, y ->
            binding.gameSurfaceView.updateMovementVector(x, y)
        }

        binding.gameSurfaceView.setOnTouchListener { _, event ->
            handleLookTouch(event)
            true
        }

        binding.exitButton.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        binding.gameSurfaceView.onResume()
    }

    override fun onPause() {
        binding.gameSurfaceView.onPause()
        super.onPause()
    }

    private fun handleLookTouch(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                val pointerId = event.getPointerId(index)
                if (lookPointerId == MotionEvent.INVALID_POINTER_ID &&
                    !binding.joystick.isPointerInBounds(event.getRawX(index), event.getRawY(index))
                ) {
                    lookPointerId = pointerId
                    lastLookX = event.getX(index)
                    lastLookY = event.getY(index)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(lookPointerId)
                if (pointerIndex != -1) {
                    val x = event.getX(pointerIndex)
                    val y = event.getY(pointerIndex)
                    val dx = x - lastLookX
                    val dy = y - lastLookY
                    lastLookX = x
                    lastLookY = y
                    binding.gameSurfaceView.addLookDelta(dx, dy)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val index = event.actionIndex
                if (event.getPointerId(index) == lookPointerId) {
                    lookPointerId = MotionEvent.INVALID_POINTER_ID
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                lookPointerId = MotionEvent.INVALID_POINTER_ID
            }
        }
    }
}
