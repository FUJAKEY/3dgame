package com.forestcoins.game.game

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import com.forestcoins.game.data.GraphicsQuality
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.random.Random

class GameRenderer(
    private val quality: GraphicsQuality
) : GLSurfaceView.Renderer {

    interface GameStateListener {
        fun onCoinCountChanged(collected: Int, total: Int)
    }

    private var gameStateListener: GameStateListener? = null

    private val groundMesh = MeshFactory.createGround(quality.renderDistance * 3f)
    private val trunkMesh = MeshFactory.createCylinder(radius = 0.22f, height = 1.7f, segments = 24)
    private val canopyMesh = MeshFactory.createCone(radius = 1.4f, height = 2.8f, segments = 28)
    private val coinMesh = MeshFactory.createCylinder(radius = 0.45f, height = 0.12f, segments = 32)

    private val trees = mutableListOf<Tree>()
    private val coins = mutableListOf<Coin>()

    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)
    private val inverseModel = FloatArray(16)
    private val normalMatrix = FloatArray(9)

    private var program = 0
    private var positionHandle = 0
    private var normalHandle = 0
    private var colorHandle = 0
    private var mvpHandle = 0
    private var modelHandle = 0
    private var lightDirHandle = 0
    private var normalMatrixHandle = 0

    private var lastFrameTime = 0L

    private var playerX = 0f
    private var playerY = 1.7f
    private var playerZ = 0f
    private var playerYaw = 180f
    private var playerPitch = -5f

    @Volatile private var moveX = 0f
    @Volatile private var moveY = 0f
    private val lookLock = Any()
    private var lookAccumulatedX = 0f
    private var lookAccumulatedY = 0f

    private val lightDirection = floatArrayOf(-0.6f, -1.0f, -0.3f).normalize()

    private val random = Random(quality.ordinal * 977 + 42)
    private val renderDistanceSq = quality.renderDistance * quality.renderDistance
    private val mapRadius = quality.renderDistance * 0.75f

    private var collectedCoins = 0

    init {
        generateWorld()
    }

    fun setGameStateListener(listener: GameStateListener) {
        gameStateListener = listener
        listener.onCoinCountChanged(collectedCoins, coins.size)
    }

    fun updateMovementVector(x: Float, y: Float) {
        moveX = x
        moveY = y
    }

    fun addLookDelta(dx: Float, dy: Float) {
        synchronized(lookLock) {
            lookAccumulatedX += dx
            lookAccumulatedY += dy
        }
    }

    fun resetLook() {
        synchronized(lookLock) {
            lookAccumulatedX = 0f
            lookAccumulatedY = 0f
        }
    }

    override fun onSurfaceCreated(unused: javax.microedition.khronos.opengles.GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        GLES20.glClearColor(0.58f, 0.82f, 1.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)
        program = buildProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        GLES20.glUseProgram(program)
        positionHandle = GLES20.glGetAttribLocation(program, "a_Position")
        normalHandle = GLES20.glGetAttribLocation(program, "a_Normal")
        colorHandle = GLES20.glGetUniformLocation(program, "u_Color")
        mvpHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix")
        modelHandle = GLES20.glGetUniformLocation(program, "u_ModelMatrix")
        lightDirHandle = GLES20.glGetUniformLocation(program, "u_LightDirection")
        normalMatrixHandle = GLES20.glGetUniformLocation(program, "u_NormalMatrix")
    }

    override fun onSurfaceChanged(unused: javax.microedition.khronos.opengles.GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Matrix.perspectiveM(projectionMatrix, 0, 60f, ratio, 0.1f, 400f)
    }

    override fun onDrawFrame(unused: javax.microedition.khronos.opengles.GL10?) {
        val now = SystemClock.uptimeMillis()
        val deltaSeconds = if (lastFrameTime == 0L) 0f else (now - lastFrameTime) / 1000f
        lastFrameTime = now

        updateCamera(deltaSeconds)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(program)
        GLES20.glUniform3fv(lightDirHandle, 1, lightDirection, 0)

        drawGround()
        drawTrees()
        drawCoins(deltaSeconds)
    }

    private fun drawGround() {
        Matrix.setIdentityM(modelMatrix, 0)
        drawMesh(groundMesh, floatArrayOf(0.38f, 0.58f, 0.32f, 1f))
    }

    private fun drawTrees() {
        val trunkColor = floatArrayOf(0.36f, 0.23f, 0.18f, 1f)
        val canopyColor = floatArrayOf(0.23f, 0.55f, 0.28f, 1f)
        for (tree in trees) {
            val dx = tree.x - playerX
            val dz = tree.z - playerZ
            val distanceSq = dx * dx + dz * dz
            if (distanceSq > renderDistanceSq) continue
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, tree.x, 0f, tree.z)
            Matrix.rotateM(modelMatrix, 0, tree.rotation, 0f, 1f, 0f)
            Matrix.scaleM(modelMatrix, 0, tree.scale, tree.scale, tree.scale)
            drawMesh(trunkMesh, trunkColor)

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, tree.x, 1.7f * tree.scale * 0.95f, tree.z)
            Matrix.rotateM(modelMatrix, 0, tree.rotation, 0f, 1f, 0f)
            Matrix.scaleM(modelMatrix, 0, tree.scale, tree.scale, tree.scale)
            drawMesh(canopyMesh, canopyColor)
        }
    }

    private fun drawCoins(deltaSeconds: Float) {
        val coinColor = floatArrayOf(1f, 0.85f, 0.2f, 1f)
        val glowColor = floatArrayOf(1f, 0.95f, 0.6f, 0.45f)
        var updated = false
        coins.forEach { coin ->
            if (coin.collected) return@forEach
            val dx = coin.x - playerX
            val dz = coin.z - playerZ
            val distanceSq = dx * dx + dz * dz
            if (distanceSq > renderDistanceSq) return@forEach
            if (distanceSq < 1.2f * 1.2f) {
                coin.collected = true
                collectedCoins += 1
                updated = true
                return@forEach
            }
            coin.rotation = (coin.rotation + 90f * deltaSeconds) % 360f
            val bounce = kotlin.math.sin(SystemClock.uptimeMillis() / 400.0 + coin.phase) * 0.08f
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, coin.x, 0.3f + bounce.toFloat(), coin.z)
            Matrix.rotateM(modelMatrix, 0, coin.rotation, 0f, 1f, 0f)
            drawMesh(coinMesh, coinColor)

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, coin.x, 0.3f + bounce.toFloat(), coin.z)
            Matrix.scaleM(modelMatrix, 0, 1.5f, 1.5f, 1.5f)
            drawMesh(coinMesh, glowColor)
        }
        if (updated) {
            gameStateListener?.onCoinCountChanged(collectedCoins, coins.size)
        }
    }

    private fun drawMesh(mesh: Mesh, color: FloatArray) {
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, mesh.vertexBuffer)
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, mesh.normalBuffer)

        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, vpMatrix, 0)
        GLES20.glUniformMatrix4fv(modelHandle, 1, false, modelMatrix, 0)

        computeNormalMatrix()
        GLES20.glUniformMatrix3fv(normalMatrixHandle, 1, false, normalMatrix, 0)
        GLES20.glUniform4fv(colorHandle, 1, color, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mesh.vertexCount)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
    }

    private fun computeNormalMatrix() {
        Matrix.invertM(inverseModel, 0, modelMatrix, 0)
        Matrix.transposeM(tempMatrix, 0, inverseModel, 0)
        normalMatrix[0] = tempMatrix[0]
        normalMatrix[1] = tempMatrix[1]
        normalMatrix[2] = tempMatrix[2]
        normalMatrix[3] = tempMatrix[4]
        normalMatrix[4] = tempMatrix[5]
        normalMatrix[5] = tempMatrix[6]
        normalMatrix[6] = tempMatrix[8]
        normalMatrix[7] = tempMatrix[9]
        normalMatrix[8] = tempMatrix[10]
    }

    private fun updateCamera(deltaSeconds: Float) {
        val lookX: Float
        val lookY: Float
        synchronized(lookLock) {
            lookX = lookAccumulatedX
            lookY = lookAccumulatedY
            lookAccumulatedX = 0f
            lookAccumulatedY = 0f
        }
        val sensitivity = 0.12f
        playerYaw = (playerYaw - lookX * sensitivity) % 360f
        playerPitch = (playerPitch - lookY * sensitivity).coerceIn(-60f, 60f)

        val yawRad = Math.toRadians(playerYaw.toDouble())
        val pitchRad = Math.toRadians(playerPitch.toDouble())
        val cosPitch = kotlin.math.cos(pitchRad)
        val sinPitch = kotlin.math.sin(pitchRad)
        val forwardX = (kotlin.math.sin(yawRad) * cosPitch).toFloat()
        val forwardZ = (kotlin.math.cos(yawRad) * cosPitch).toFloat()
        val forwardY = sinPitch.toFloat()
        val rightX = kotlin.math.cos(yawRad).toFloat()
        val rightZ = (-kotlin.math.sin(yawRad)).toFloat()

        val speed = when (quality) {
            GraphicsQuality.LOW -> 5.5f
            GraphicsQuality.MEDIUM -> 6.2f
            GraphicsQuality.HIGH -> 6.8f
        }
        val moveForward = moveY.coerceIn(-1f, 1f)
        val moveSide = moveX.coerceIn(-1f, 1f)

        playerX += (forwardX * moveForward + rightX * moveSide) * speed * deltaSeconds
        playerZ += (forwardZ * moveForward + rightZ * moveSide) * speed * deltaSeconds
        playerX = playerX.coerceIn(-mapRadius, mapRadius)
        playerZ = playerZ.coerceIn(-mapRadius, mapRadius)

        val eyeX = playerX
        val eyeY = playerY
        val eyeZ = playerZ
        Matrix.setLookAtM(
            viewMatrix,
            0,
            eyeX,
            eyeY,
            eyeZ,
            eyeX + forwardX,
            eyeY + forwardY,
            eyeZ + forwardZ,
            0f,
            1f,
            0f
        )
    }

    private fun generateWorld() {
        trees.clear()
        coins.clear()
        val radius = quality.renderDistance * 0.9f
        repeat(quality.treeCount) {
            val angle = random.nextFloat() * (2f * PI.toFloat())
            val distance = radius * sqrt(random.nextFloat())
            val x = (kotlin.math.cos(angle.toDouble()) * distance).toFloat()
            val z = (kotlin.math.sin(angle.toDouble()) * distance).toFloat()
            val scale = 0.9f + random.nextFloat() * 0.8f
            val rotation = random.nextFloat() * 360f
            trees += Tree(x, z, scale, rotation)
        }
        if (trees.isEmpty()) {
            trees += Tree(0f, 5f, 1.2f, 0f)
        }
        repeat(quality.coinCount) { index ->
            val tree = trees[index % trees.size]
            val angle = random.nextFloat() * (2f * PI.toFloat())
            val distance = 1.6f + random.nextFloat() * 2.6f
            val x = tree.x + (kotlin.math.cos(angle.toDouble()) * distance).toFloat()
            val z = tree.z + (kotlin.math.sin(angle.toDouble()) * distance).toFloat()
            val phase = random.nextFloat() * PI.toFloat()
            coins += Coin(x, z, phase)
        }
    }

    private fun FloatArray.normalize(): FloatArray {
        val length = sqrt(this[0] * this[0] + this[1] * this[1] + this[2] * this[2])
        return floatArrayOf(this[0] / length, this[1] / length, this[2] / length)
    }

    private fun buildProgram(vertexCode: String, fragmentCode: String): Int {
        val vertex = loadShader(GLES20.GL_VERTEX_SHADER, vertexCode)
        val fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertex)
        GLES20.glAttachShader(program, fragment)
        GLES20.glLinkProgram(program)
        return program
    }

    private fun loadShader(type: Int, code: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)
        return shader
    }

    private data class Tree(val x: Float, val z: Float, val scale: Float, val rotation: Float)

    private data class Coin(val x: Float, val z: Float, val phase: Float, var rotation: Float = 0f, var collected: Boolean = false)

    companion object {
        private const val VERTEX_SHADER = """
            uniform mat4 u_MVPMatrix;
            uniform mat4 u_ModelMatrix;
            uniform mat3 u_NormalMatrix;
            attribute vec4 a_Position;
            attribute vec3 a_Normal;
            varying vec3 v_Normal;
            void main() {
                v_Normal = u_NormalMatrix * a_Normal;
                gl_Position = u_MVPMatrix * a_Position;
            }
        """

        private const val FRAGMENT_SHADER = """
            precision mediump float;
            uniform vec4 u_Color;
            uniform vec3 u_LightDirection;
            varying vec3 v_Normal;
            void main() {
                vec3 normal = normalize(v_Normal);
                float diffuse = max(dot(normal, -u_LightDirection), 0.25);
                float ambient = 0.18;
                vec3 color = u_Color.rgb * (diffuse + ambient);
                gl_FragColor = vec4(color, u_Color.a);
            }
        """
    }
}
