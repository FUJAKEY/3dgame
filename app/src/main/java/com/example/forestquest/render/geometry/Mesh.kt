package com.example.forestquest.render.geometry

import android.opengl.GLES20
import com.example.forestquest.render.ShaderProgram
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Mesh(
    vertices: FloatArray,
    normals: FloatArray,
    colors: FloatArray
) {

    private val vertexBuffer: FloatBuffer = vertices.toFloatBuffer()
    private val normalBuffer: FloatBuffer = normals.toFloatBuffer()
    private val colorBuffer: FloatBuffer = colors.toFloatBuffer()
    private val vertexCount: Int = vertices.size / 3

    fun render(
        shader: ShaderProgram,
        modelMatrix: FloatArray,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray
    ) {
        shader.setMatrices(modelMatrix, viewMatrix, projectionMatrix)
        shader.bindAttributes(vertexBuffer, normalBuffer, colorBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
    }

    private fun FloatArray.toFloatBuffer(): FloatBuffer {
        val buffer = ByteBuffer.allocateDirect(size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        buffer.put(this)
        buffer.position(0)
        return buffer
    }
}
