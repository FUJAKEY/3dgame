package com.example.forestquest

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.forestquest.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        val labels = GraphicsQuality.labels()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)
        binding.qualitySpinner.setAdapter(adapter)

        val currentQuality = GraphicsPreferences.getQuality(this)
        binding.qualitySpinner.setText(currentQuality.toLabel(), false)

        binding.qualitySpinner.setOnItemClickListener { _, _, position, _ ->
            val selected = GraphicsQuality.fromLabel(labels[position])
            GraphicsPreferences.saveQuality(this, selected)
        }
    }

    private fun GraphicsQuality.toLabel(): String = when (this) {
        GraphicsQuality.HIGH -> "Высокое"
        GraphicsQuality.MEDIUM -> "Среднее"
        GraphicsQuality.LOW -> "Низкое"
    }
}
