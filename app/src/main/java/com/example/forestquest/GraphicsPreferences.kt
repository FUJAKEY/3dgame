package com.example.forestquest

import android.content.Context

enum class GraphicsQuality(val treeCount: Int, val coinCount: Int, val treeSegments: Int, val coinSegments: Int) {
    HIGH(treeCount = 140, coinCount = 120, treeSegments = 32, coinSegments = 64),
    MEDIUM(treeCount = 100, coinCount = 90, treeSegments = 24, coinSegments = 48),
    LOW(treeCount = 70, coinCount = 60, treeSegments = 16, coinSegments = 32);

    companion object {
        fun fromLabel(label: String): GraphicsQuality = when (label) {
            "Высокое" -> HIGH
            "Среднее" -> MEDIUM
            "Низкое" -> LOW
            else -> HIGH
        }

        fun labels(): List<String> = listOf("Высокое", "Среднее", "Низкое")
    }
}

object GraphicsPreferences {
    private const val PREF_NAME = "forest_quest_prefs"
    private const val KEY_GRAPHICS = "graphics_quality"

    fun saveQuality(context: Context, quality: GraphicsQuality) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_GRAPHICS, quality.name)
            .apply()
    }

    fun getQuality(context: Context): GraphicsQuality {
        val saved = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_GRAPHICS, null)
        return saved?.let { runCatching { GraphicsQuality.valueOf(it) }.getOrNull() } ?: GraphicsQuality.HIGH
    }
}
