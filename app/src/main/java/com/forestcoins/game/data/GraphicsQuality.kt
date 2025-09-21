package com.forestcoins.game.data

enum class GraphicsQuality(val treeCount: Int, val coinCount: Int, val renderDistance: Float) {
    LOW(treeCount = 40, coinCount = 30, renderDistance = 80f),
    MEDIUM(treeCount = 70, coinCount = 50, renderDistance = 120f),
    HIGH(treeCount = 100, coinCount = 70, renderDistance = 160f);

    companion object {
        fun fromKey(key: String): GraphicsQuality = entries.firstOrNull { it.name == key } ?: HIGH
    }
}
