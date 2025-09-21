package com.example.forestquest.render.math

import kotlin.math.sqrt

data class Vector3(var x: Float, var y: Float, var z: Float) {

    fun set(other: Vector3) {
        x = other.x
        y = other.y
        z = other.z
    }

    fun add(other: Vector3): Vector3 {
        x += other.x
        y += other.y
        z += other.z
        return this
    }

    fun subtract(other: Vector3): Vector3 {
        x -= other.x
        y -= other.y
        z -= other.z
        return this
    }

    fun multiply(value: Float): Vector3 {
        x *= value
        y *= value
        z *= value
        return this
    }

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalize(): Vector3 {
        val len = length()
        if (len > 0f) {
            val inv = 1f / len
            x *= inv
            y *= inv
            z *= inv
        }
        return this
    }

    companion object {
        fun from(other: Vector3) = Vector3(other.x, other.y, other.z)
    }
}
