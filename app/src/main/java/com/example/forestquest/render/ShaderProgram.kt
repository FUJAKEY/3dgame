package com.example.forestquest.render

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.forestquest.render.math.MatrixUtils
import java.nio.FloatBuffer

class ShaderProgram(vertexShaderCode: String, fragmentShaderCode: String) {

    private val programId: Int = createProgram(vertexShaderCode, fragmentShaderCode)

    private val positionHandle: Int = GLES20.glGetAttribLocation(programId, "aPosition")
    private val normalHandle: Int = GLES20.glGetAttribLocation(programId, "aNormal")
    private val colorHandle: Int = GLES20.glGetAttribLocation(programId, "aColor")

    private val mvpMatrixHandle: Int = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
    private val modelMatrixHandle: Int = GLES20.glGetUniformLocation(programId, "uModelMatrix")
    private val normalMatrixHandle: Int = GLES20.glGetUniformLocation(programId, "uNormalMatrix")
    private val lightDirectionHandle: Int = GLES20.glGetUniformLocation(programId, "uLightDirection")

    private val viewModelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(9)

    fun use() {
        GLES20.glUseProgram(programId)
    }

    fun setLightDirection(x: Float, y: Float, z: Float) {
        GLES20.glUniform3f(lightDirectionHandle, x, y, z)
    }

    fun setMatrices(modelMatrix: FloatArray, viewMatrix: FloatArray, projectionMatrix: FloatArray) {
        Matrix.multiplyMM(viewModelMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewModelMatrix, 0)
        MatrixUtils.calculateNormalMatrix(modelMatrix, normalMatrix)
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix3fv(normalMatrixHandle, 1, false, normalMatrix, 0)
    }

    fun bindAttributes(vertexBuffer: FloatBuffer, normalBuffer: FloatBuffer, colorBuffer: FloatBuffer) {
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)

        normalBuffer.position(0)
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, normalBuffer)
        GLES20.glEnableVertexAttribArray(normalHandle)

        colorBuffer.position(0)
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)
        GLES20.glEnableVertexAttribArray(colorHandle)
    }

    fun unbindAttributes() {
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }

    private fun createProgram(vertexCode: String, fragmentCode: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val error = GLES20.glGetProgramInfoLog(program)
            GLES20.glDeleteProgram(program)
            throw RuntimeException("Не удалось линковать шейдерную программу: $error")
        }
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)
        return program
    }

    private fun loadShader(type: Int, code: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            val error = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Ошибка компиляции шейдера: $error")
        }
        return shader
    }
}
