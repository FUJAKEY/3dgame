package com.example.forestquest.render.world

import com.example.forestquest.GraphicsQuality
import com.example.forestquest.render.math.Vector3
import kotlin.random.Random

data class TreeInstance(
    val position: Vector3,
    val trunkHeight: Float,
    val trunkRadius: Float,
    val crownHeight: Float,
    val crownRadius: Float
)

data class CoinInstance(val position: Vector3, var collected: Boolean = false)

class GameWorld(quality: GraphicsQuality) {

    val trees: List<TreeInstance>
    val coins: List<CoinInstance>
    val mapSize = 220f

    private val random = Random(quality.ordinal * 9973L + 1337L)

    init {
        trees = generateTrees(quality.treeCount)
        coins = generateCoins(quality.coinCount, trees)
    }

    private fun generateTrees(count: Int): List<TreeInstance> {
        val result = ArrayList<TreeInstance>(count)
        val half = mapSize / 2f - 10f
        repeat(count) {
            val x = randomBetween(-half, half)
            val z = randomBetween(-half, half)
            val trunkHeight = randomBetween(8f, 13f)
            val trunkRadius = randomBetween(0.6f, 1.1f)
            val crownHeight = randomBetween(10f, 18f)
            val crownRadius = randomBetween(4f, 7f)
            result += TreeInstance(Vector3(x, 0f, z), trunkHeight, trunkRadius, crownHeight, crownRadius)
        }
        return result
    }

    private fun generateCoins(count: Int, trees: List<TreeInstance>): List<CoinInstance> {
        val result = ArrayList<CoinInstance>(count)
        if (trees.isEmpty()) return result
        val coinsPerTree = count / trees.size.toFloat()
        var accumulated = 0f
        for (tree in trees) {
            accumulated += coinsPerTree
            while (accumulated >= 1f && result.size < count) {
                accumulated -= 1f
                val offsetRadius = randomBetween(1.5f, 3.5f)
                val angle = randomBetween(0f, 360f)
                val rad = Math.toRadians(angle.toDouble())
                val x = (tree.position.x + (offsetRadius * kotlin.math.cos(rad)).toFloat())
                val z = (tree.position.z + (offsetRadius * kotlin.math.sin(rad)).toFloat())
                result += CoinInstance(Vector3(x, 0.6f, z))
            }
        }
        while (result.size < count) {
            val x = randomBetween(-mapSize / 2f + 5f, mapSize / 2f - 5f)
            val z = randomBetween(-mapSize / 2f + 5f, mapSize / 2f - 5f)
            result += CoinInstance(Vector3(x, 0.6f, z))
        }
        return result
    }

    private fun randomBetween(min: Float, max: Float): Float {
        return random.nextFloat() * (max - min) + min
    }
}
