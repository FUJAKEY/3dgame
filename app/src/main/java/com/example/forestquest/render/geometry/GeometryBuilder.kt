package com.example.forestquest.render.geometry

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object GeometryBuilder {

    fun createGround(size: Float, colorCenter: FloatArray, colorEdge: FloatArray): Mesh {
        val half = size / 2f
        val vertices = floatArrayOf(
            -half, 0f, -half,
            -half, 0f, half,
            half, 0f, half,

            -half, 0f, -half,
            half, 0f, half,
            half, 0f, -half
        )

        val normals = FloatArray(18) { index -> if (index % 3 == 1) 1f else 0f }

        val colors = floatArrayOf(
            *colorEdge, *colorCenter, *colorEdge,
            *colorEdge, *colorEdge, *colorCenter
        )

        return Mesh(vertices, normals, colors)
    }

    fun createCylinder(radius: Float, height: Float, segments: Int, color: FloatArray): Mesh {
        val vertices = ArrayList<Float>()
        val normals = ArrayList<Float>()
        val colors = ArrayList<Float>()
        val step = (2f * PI.toFloat()) / segments
        val halfHeight = height / 2f
        for (i in 0 until segments) {
            val angle = step * i
            val nextAngle = step * ((i + 1) % segments)
            val x1 = cos(angle) * radius
            val z1 = sin(angle) * radius
            val x2 = cos(nextAngle) * radius
            val z2 = sin(nextAngle) * radius

            val normal1 = normalize(x1, 0f, z1)
            val normal2 = normalize(x2, 0f, z2)

            // First triangle (bottom1, top1, top2)
            vertices.addAll(listOf(x1, -halfHeight, z1, x1, halfHeight, z1, x2, halfHeight, z2))
            normals.addAll(listOf(normal1.first, normal1.second, normal1.third, normal1.first, normal1.second, normal1.third, normal2.first, normal2.second, normal2.third))
            repeat(3) { colors.addAll(color.toList()) }

            // Second triangle (bottom1, top2, bottom2)
            vertices.addAll(listOf(x1, -halfHeight, z1, x2, halfHeight, z2, x2, -halfHeight, z2))
            normals.addAll(listOf(normal1.first, normal1.second, normal1.third, normal2.first, normal2.second, normal2.third, normal2.first, normal2.second, normal2.third))
            repeat(3) { colors.addAll(color.toList()) }
        }

        // Top cap
        val topNormal = Triple(0f, 1f, 0f)
        for (i in 0 until segments) {
            val angle = step * i
            val nextAngle = step * ((i + 1) % segments)
            val x1 = cos(angle) * radius
            val z1 = sin(angle) * radius
            val x2 = cos(nextAngle) * radius
            val z2 = sin(nextAngle) * radius

            vertices.addAll(listOf(0f, halfHeight, 0f, x1, halfHeight, z1, x2, halfHeight, z2))
            repeat(3) { normals.addAll(listOf(topNormal.first, topNormal.second, topNormal.third)) }
            repeat(3) { colors.addAll(color.toList()) }
        }

        // Bottom cap
        val bottomNormal = Triple(0f, -1f, 0f)
        for (i in 0 until segments) {
            val angle = step * i
            val nextAngle = step * ((i + 1) % segments)
            val x1 = cos(angle) * radius
            val z1 = sin(angle) * radius
            val x2 = cos(nextAngle) * radius
            val z2 = sin(nextAngle) * radius

            vertices.addAll(listOf(0f, -halfHeight, 0f, x2, -halfHeight, z2, x1, -halfHeight, z1))
            repeat(3) { normals.addAll(listOf(bottomNormal.first, bottomNormal.second, bottomNormal.third)) }
            repeat(3) { colors.addAll(color.toList()) }
        }

        return Mesh(vertices.toFloatArray(), normals.toFloatArray(), colors.toFloatArray())
    }

    fun createCone(radius: Float, height: Float, segments: Int, color: FloatArray): Mesh {
        val vertices = ArrayList<Float>()
        val normals = ArrayList<Float>()
        val colors = ArrayList<Float>()
        val step = (2f * PI.toFloat()) / segments
        val tip = floatArrayOf(0f, height / 2f, 0f)
        val baseY = -height / 2f

        for (i in 0 until segments) {
            val angle = step * i
            val nextAngle = step * ((i + 1) % segments)
            val x1 = cos(angle) * radius
            val z1 = sin(angle) * radius
            val x2 = cos(nextAngle) * radius
            val z2 = sin(nextAngle) * radius

            vertices.addAll(listOf(tip[0], tip[1], tip[2], x1, baseY, z1, x2, baseY, z2))
            val normal = calculateConeNormal(x1, baseY, z1, x2, baseY, z2, tip[0], tip[1], tip[2])
            repeat(3) { normals.addAll(normal) }
            repeat(3) { colors.addAll(color.toList()) }
        }

        val baseNormal = Triple(0f, -1f, 0f)
        for (i in 0 until segments) {
            val angle = step * i
            val nextAngle = step * ((i + 1) % segments)
            val x1 = cos(angle) * radius
            val z1 = sin(angle) * radius
            val x2 = cos(nextAngle) * radius
            val z2 = sin(nextAngle) * radius

            vertices.addAll(listOf(0f, baseY, 0f, x2, baseY, z2, x1, baseY, z1))
            repeat(3) { normals.addAll(listOf(baseNormal.first, baseNormal.second, baseNormal.third)) }
            repeat(3) { colors.addAll(color.toList()) }
        }

        return Mesh(vertices.toFloatArray(), normals.toFloatArray(), colors.toFloatArray())
    }

    private fun calculateConeNormal(
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        x3: Float, y3: Float, z3: Float
    ): List<Float> {
        val u1 = x2 - x1
        val u2 = y2 - y1
        val u3 = z2 - z1

        val v1 = x3 - x1
        val v2 = y3 - y1
        val v3 = z3 - z1

        val nx = u2 * v3 - u3 * v2
        val ny = u3 * v1 - u1 * v3
        val nz = u1 * v2 - u2 * v1
        val length = kotlin.math.sqrt(nx * nx + ny * ny + nz * nz)
        if (length == 0f) return listOf(0f, 1f, 0f)
        val inv = 1f / length
        return listOf(nx * inv, ny * inv, nz * inv)
    }

    private fun normalize(x: Float, y: Float, z: Float): Triple<Float, Float, Float> {
        val length = kotlin.math.sqrt(x * x + y * y + z * z)
        if (length == 0f) return Triple(0f, 1f, 0f)
        val inv = 1f / length
        return Triple(x * inv, y * inv, z * inv)
    }
}
