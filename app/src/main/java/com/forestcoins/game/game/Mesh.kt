package com.forestcoins.game.game

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Mesh(vertices: FloatArray, normals: FloatArray) {
    val vertexCount: Int = vertices.size / 3
    val vertexBuffer: FloatBuffer = vertices.toBuffer()
    val normalBuffer: FloatBuffer = normals.toBuffer()

    private fun FloatArray.toBuffer(): FloatBuffer =
        ByteBuffer.allocateDirect(size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(this@toBuffer)
                position(0)
            }
}
