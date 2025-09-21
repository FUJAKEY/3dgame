package com.forestcoins.game.game

import kotlin.math.cos
import kotlin.math.sin

object MeshFactory {
    fun createGround(size: Float): Mesh {
        val half = size / 2f
        val vertices = floatArrayOf(
            -half, 0f, -half,
            half, 0f, -half,
            -half, 0f, half,

            half, 0f, -half,
            half, 0f, half,
            -half, 0f, half
        )
        val normals = FloatArray(vertices.size) { index ->
            if (index % 3 == 1) 1f else 0f
        }
        return Mesh(vertices, normals)
    }

    fun createCylinder(radius: Float, height: Float, segments: Int): Mesh {
        val vertexList = mutableListOf<Float>()
        val normalList = mutableListOf<Float>()
        for (i in 0 until segments) {
            val angle0 = 2f * Math.PI.toFloat() * i / segments
            val angle1 = 2f * Math.PI.toFloat() * (i + 1) / segments
            val x0 = cos(angle0) * radius
            val z0 = sin(angle0) * radius
            val x1 = cos(angle1) * radius
            val z1 = sin(angle1) * radius

            // Side triangle 1
            vertexList.addAll(listOf(x0, 0f, z0, x1, 0f, z1, x1, height, z1))
            normalList.addAll(listOf(cos(angle0), 0f, sin(angle0), cos(angle1), 0f, sin(angle1), cos(angle1), 0f, sin(angle1)))
            // Side triangle 2
            vertexList.addAll(listOf(x0, 0f, z0, x1, height, z1, x0, height, z0))
            normalList.addAll(listOf(cos(angle0), 0f, sin(angle0), cos(angle1), 0f, sin(angle1), cos(angle0), 0f, sin(angle0)))
        }

        // Top
        for (i in 0 until segments) {
            val angle0 = 2f * Math.PI.toFloat() * i / segments
            val angle1 = 2f * Math.PI.toFloat() * (i + 1) / segments
            val x0 = cos(angle0) * radius
            val z0 = sin(angle0) * radius
            val x1 = cos(angle1) * radius
            val z1 = sin(angle1) * radius
            vertexList.addAll(listOf(0f, height, 0f, x1, height, z1, x0, height, z0))
            normalList.addAll(listOf(0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f))
        }

        // Bottom
        for (i in 0 until segments) {
            val angle0 = 2f * Math.PI.toFloat() * i / segments
            val angle1 = 2f * Math.PI.toFloat() * (i + 1) / segments
            val x0 = cos(angle0) * radius
            val z0 = sin(angle0) * radius
            val x1 = cos(angle1) * radius
            val z1 = sin(angle1) * radius
            vertexList.addAll(listOf(0f, 0f, 0f, x0, 0f, z0, x1, 0f, z1))
            normalList.addAll(listOf(0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f))
        }

        return Mesh(vertexList.toFloatArray(), normalList.toFloatArray())
    }

    fun createCone(radius: Float, height: Float, segments: Int): Mesh {
        val vertexList = mutableListOf<Float>()
        val normalList = mutableListOf<Float>()
        val slope = radius / height
        for (i in 0 until segments) {
            val angle0 = 2f * Math.PI.toFloat() * i / segments
            val angle1 = 2f * Math.PI.toFloat() * (i + 1) / segments
            val x0 = cos(angle0) * radius
            val z0 = sin(angle0) * radius
            val x1 = cos(angle1) * radius
            val z1 = sin(angle1) * radius
            vertexList.addAll(listOf(0f, height, 0f, x1, 0f, z1, x0, 0f, z0))

            val nx0 = cos(angle0)
            val nz0 = sin(angle0)
            val nx1 = cos(angle1)
            val nz1 = sin(angle1)
            val ny = slope
            normalList.addAll(listOf(nx0, ny, nz0, nx1, ny, nz1, nx0, ny, nz0))
        }

        // Base
        for (i in 0 until segments) {
            val angle0 = 2f * Math.PI.toFloat() * i / segments
            val angle1 = 2f * Math.PI.toFloat() * (i + 1) / segments
            val x0 = cos(angle0) * radius
            val z0 = sin(angle0) * radius
            val x1 = cos(angle1) * radius
            val z1 = sin(angle1) * radius
            vertexList.addAll(listOf(0f, 0f, 0f, x0, 0f, z0, x1, 0f, z1))
            normalList.addAll(listOf(0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f))
        }

        return Mesh(vertexList.toFloatArray(), normalList.toFloatArray())
    }
}
