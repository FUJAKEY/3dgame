package com.example.forestquest.render.math

import android.opengl.Matrix

object MatrixUtils {

    fun calculateNormalMatrix(modelMatrix: FloatArray, out: FloatArray) {
        require(out.size >= 9) { "Normal matrix output must have at least 9 elements" }
        val inverted = FloatArray(16)
        if (!Matrix.invertM(inverted, 0, modelMatrix, 0)) {
            setIdentity3x3(out)
            return
        }
        val transposed = FloatArray(16)
        Matrix.transposeM(transposed, 0, inverted, 0)
        // Extract upper-left 3x3
        out[0] = transposed[0]
        out[1] = transposed[1]
        out[2] = transposed[2]

        out[3] = transposed[4]
        out[4] = transposed[5]
        out[5] = transposed[6]

        out[6] = transposed[8]
        out[7] = transposed[9]
        out[8] = transposed[10]
    }

    private fun setIdentity3x3(out: FloatArray) {
        out[0] = 1f
        out[1] = 0f
        out[2] = 0f
        out[3] = 0f
        out[4] = 1f
        out[5] = 0f
        out[6] = 0f
        out[7] = 0f
        out[8] = 1f
    }
}
