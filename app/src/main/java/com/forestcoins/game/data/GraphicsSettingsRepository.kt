package com.forestcoins.game.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GraphicsSettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    suspend fun readQuality(): GraphicsQuality = withContext(Dispatchers.IO) {
        val value = preferences.getString(KEY_QUALITY, GraphicsQuality.HIGH.name) ?: GraphicsQuality.HIGH.name
        GraphicsQuality.fromKey(value)
    }

    suspend fun saveQuality(quality: GraphicsQuality) = withContext(Dispatchers.IO) {
        preferences.edit().putString(KEY_QUALITY, quality.name).apply()
    }

    companion object {
        private const val PREF_NAME = "graphics_settings"
        private const val KEY_QUALITY = "quality"
    }
}
