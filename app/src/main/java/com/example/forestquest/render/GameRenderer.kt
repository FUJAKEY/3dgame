package com.example.forestquest.render

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.forestquest.GraphicsQuality
import com.example.forestquest.render.geometry.GeometryBuilder
import com.example.forestquest.render.geometry.Mesh
import com.example.forestquest.render.math.Vector3
import com.example.forestquest.render.world.GameWorld
import com.example.forestquest.render.world.TreeInstance
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GameRenderer(
    private val quality: GraphicsQuality,
    private val callback: Callback
) : GLSurfaceView.Renderer {

    interface Callback {
        fun onCoinsChanged(collected: Int)
    }

    private lateinit var shader: ShaderProgram
    private lateinit var world: GameWorld
    private lateinit var groundMesh: Mesh
    private lateinit var trunkMesh: Mesh
    private lateinit var crownMesh: Mesh
    private lateinit var coinMesh: Mesh

    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private val cameraPosition = Vector3(0f, CAMERA_HEIGHT, 0f)
    private val cameraDirection = Vector3(0f, 0f, -1f)

    @Volatile
    private var inputX: Float = 0f

    @Volatile
    private var inputY: Float = 0f

    @Volatile
    private var yaw: Float = 180f

    @Volatile
    private var pitch: Float = 0f

    private var lastFrameTimeNs: Long = System.nanoTime()
    private var collectedCoins: Int = 0
    private var lastReportedCoins: Int = -1

    override fun onSurfaceCreated(gl: javax.microedition.khronos.opengles.GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        GLES20.glClearColor(0.05f, 0.12f, 0.08f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)

        shader = ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        world = GameWorld(quality)

        groundMesh = GeometryBuilder.createGround(
            world.mapSize,
            floatArrayOf(0.27f, 0.48f, 0.27f, 1f),
            floatArrayOf(0.18f, 0.34f, 0.19f, 1f)
        )
        trunkMesh = GeometryBuilder.createCylinder(1f, 1f, quality.treeSegments, floatArrayOf(0.34f, 0.24f, 0.18f, 1f))
        crownMesh = GeometryBuilder.createCone(1f, 1.2f, quality.treeSegments, floatArrayOf(0.12f, 0.45f, 0.18f, 1f))
        coinMesh = GeometryBuilder.createCylinder(1f, 0.2f, quality.coinSegments, floatArrayOf(0.95f, 0.8f, 0.2f, 1f))
    }

    override fun onSurfaceChanged(gl: javax.microedition.khronos.opengles.GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val safeHeight = if (height == 0) 1 else height
        val aspect = width.toFloat() / safeHeight.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 60f, aspect, 0.5f, 500f)
    }

    override fun onDrawFrame(gl: javax.microedition.khronos.opengles.GL10?) {
        val now = System.nanoTime()
        val deltaTime = ((now - lastFrameTimeNs).coerceAtLeast(1_000_000L)) / 1_000_000_000f
        lastFrameTimeNs = now

        updateCamera(deltaTime)
        collectCoins()

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        shader.use()
        shader.setLightDirection(0.3f, 0.8f, -0.6f)

        renderGround()
        renderTrees()
        renderCoins()
        shader.unbindAttributes()
    }

    private fun renderGround() {
        Matrix.setIdentityM(modelMatrix, 0)
        groundMesh.render(shader, modelMatrix, viewMatrix, projectionMatrix)
    }

    private fun renderTrees() {
        for (tree in world.trees) {
            renderTree(tree)
        }
    }

    private fun renderTree(tree: TreeInstance) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, tree.position.x, tree.trunkHeight / 2f, tree.position.z)
        Matrix.scaleM(modelMatrix, 0, tree.trunkRadius, tree.trunkHeight, tree.trunkRadius)
        trunkMesh.render(shader, modelMatrix, viewMatrix, projectionMatrix)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, tree.position.x, tree.trunkHeight + tree.crownHeight / 2f, tree.position.z)
        Matrix.scaleM(modelMatrix, 0, tree.crownRadius, tree.crownHeight, tree.crownRadius)
        crownMesh.render(shader, modelMatrix, viewMatrix, projectionMatrix)
    }

    private fun renderCoins() {
        val rotation = ((System.nanoTime() / 1_000_000L) % 3600L) / 10f
        for (coin in world.coins) {
            if (coin.collected) continue
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, coin.position.x, coin.position.y, coin.position.z)
            Matrix.rotateM(modelMatrix, 0, rotation, 0f, 1f, 0f)
            Matrix.scaleM(modelMatrix, 0, COIN_RADIUS, COIN_THICKNESS, COIN_RADIUS)
            coinMesh.render(shader, modelMatrix, viewMatrix, projectionMatrix)
        }
    }

    private fun updateCamera(deltaTime: Float) {
        val yawLocal = yaw
        val pitchLocal = pitch.coerceIn(-70f, 70f)
        val yawRad = Math.toRadians(yawLocal.toDouble()).toFloat()
        val pitchRad = Math.toRadians(pitchLocal.toDouble()).toFloat()

        cameraDirection.x = (cos(pitchRad) * sin(yawRad))
        cameraDirection.y = sin(pitchRad)
        cameraDirection.z = (cos(pitchRad) * cos(yawRad))

        val forward = Vector3(cameraDirection.x, 0f, cameraDirection.z)
        if (forward.length() < 0.0001f) {
            forward.z = -1f
        }
        forward.normalize()
        val right = Vector3(forward.z, 0f, -forward.x).normalize()

        val moveVector = Vector3(0f, 0f, 0f)
        moveVector.add(forward.copy().multiply(-inputY))
        moveVector.add(right.copy().multiply(inputX))
        if (moveVector.length() > 0f) {
            moveVector.normalize().multiply(MOVE_SPEED * deltaTime)
            cameraPosition.add(moveVector)
        }

        val halfSize = world.mapSize / 2f - 6f
        cameraPosition.x = cameraPosition.x.coerceIn(-halfSize, halfSize)
        cameraPosition.z = cameraPosition.z.coerceIn(-halfSize, halfSize)
        cameraPosition.y = CAMERA_HEIGHT

        val targetX = cameraPosition.x + cameraDirection.x
        val targetY = cameraPosition.y + cameraDirection.y
        val targetZ = cameraPosition.z + cameraDirection.z
        Matrix.setLookAtM(
            viewMatrix,
            0,
            cameraPosition.x,
            cameraPosition.y,
            cameraPosition.z,
            targetX,
            targetY,
            targetZ,
            0f,
            1f,
            0f
        )
    }

    private fun collectCoins() {
        var updated = false
        for (coin in world.coins) {
            if (coin.collected) continue
            if (distanceHorizontal(coin.position, cameraPosition) < COIN_PICK_RADIUS) {
                coin.collected = true
                collectedCoins += 1
                updated = true
            }
        }
        if (lastReportedCoins == -1) {
            lastReportedCoins = 0
            callback.onCoinsChanged(0)
        }
        if (updated && collectedCoins != lastReportedCoins) {
            lastReportedCoins = collectedCoins
            callback.onCoinsChanged(collectedCoins)
        }
    }

    private fun distanceHorizontal(a: Vector3, b: Vector3): Float {
        val dx = a.x - b.x
        val dz = a.z - b.z
        return sqrt(dx * dx + dz * dz)
    }

    fun setMovementInput(x: Float, y: Float) {
        inputX = x
        inputY = y
    }

    fun rotateCamera(deltaX: Float, deltaY: Float) {
        yaw -= deltaX * LOOK_SENSITIVITY
        val targetPitch = pitch - deltaY * LOOK_SENSITIVITY
        pitch = targetPitch.coerceIn(-70f, 70f)
    }

    companion object {
        private const val MOVE_SPEED = 12f
        private const val LOOK_SENSITIVITY = 0.18f
        private const val CAMERA_HEIGHT = 5.5f
        private const val COIN_RADIUS = 0.45f
        private const val COIN_THICKNESS = 0.18f
        private const val COIN_PICK_RADIUS = 1.3f

        private val VERTEX_SHADER = """
            uniform mat4 uMVPMatrix;
            uniform mat4 uModelMatrix;
            uniform mat3 uNormalMatrix;
            uniform vec3 uLightDirection;
            attribute vec3 aPosition;
            attribute vec3 aNormal;
            attribute vec4 aColor;
            varying vec4 vColor;
            void main() {
                vec3 normal = normalize(uNormalMatrix * aNormal);
                float diffuse = max(dot(normal, normalize(-uLightDirection)), 0.0);
                float ambient = 0.35;
                float intensity = ambient + diffuse * 0.65;
                vColor = vec4(aColor.rgb * intensity, aColor.a);
                gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
            }
        """.trimIndent()

        private val FRAGMENT_SHADER = """
            precision mediump float;
            varying vec4 vColor;
            void main() {
                gl_FragColor = vColor;
            }
        """.trimIndent()
    }
}
