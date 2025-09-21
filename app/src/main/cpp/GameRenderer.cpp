#include "GameRenderer.h"

#include <algorithm>
#include <cstddef>

namespace {
constexpr const char *TAG = "ForestGame";
constexpr float PI = 3.1415926535f;

struct Vertex {
    float px;
    float py;
    float pz;
    float nx;
    float ny;
    float nz;
};

Mesh createMesh(const std::vector<Vertex> &vertices, const std::vector<uint16_t> &indices) {
    Mesh mesh;
    if (vertices.empty() || indices.empty()) {
        return mesh;
    }

    glGenBuffers(1, &mesh.vbo);
    glBindBuffer(GL_ARRAY_BUFFER, mesh.vbo);
    glBufferData(GL_ARRAY_BUFFER, static_cast<GLsizeiptr>(vertices.size() * sizeof(Vertex)), vertices.data(), GL_STATIC_DRAW);

    glGenBuffers(1, &mesh.ibo);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mesh.ibo);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, static_cast<GLsizeiptr>(indices.size() * sizeof(uint16_t)), indices.data(), GL_STATIC_DRAW);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

    mesh.indexCount = static_cast<GLsizei>(indices.size());
    return mesh;
}

void destroyMesh(Mesh &mesh) {
    if (mesh.ibo != 0) {
        glDeleteBuffers(1, &mesh.ibo);
        mesh.ibo = 0;
    }
    if (mesh.vbo != 0) {
        glDeleteBuffers(1, &mesh.vbo);
        mesh.vbo = 0;
    }
    mesh.indexCount = 0;
}

Mesh createPlane() {
    std::vector<Vertex> vertices = {
            {-1.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f},
            {1.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f},
            {1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f},
            {-1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f},
    };
    std::vector<uint16_t> indices = {
            0, 1, 2,
            0, 2, 3
    };
    return createMesh(vertices, indices);
}

Mesh createCylinder(int segments, float radius, float height) {
    std::vector<Vertex> vertices;
    std::vector<uint16_t> indices;
    vertices.reserve(static_cast<size_t>(segments + 1) * 4);

    for (int i = 0; i <= segments; ++i) {
        float angle = (static_cast<float>(i) / static_cast<float>(segments)) * 2.0f * PI;
        float x = std::cos(angle) * radius;
        float z = std::sin(angle) * radius;
        Vec3 normal = normalize(Vec3{x, 0.0f, z});
        vertices.push_back({x, 0.0f, z, normal.x, normal.y, normal.z});
        vertices.push_back({x, height, z, normal.x, normal.y, normal.z});
    }

    for (int i = 0; i < segments; ++i) {
        uint16_t bottom0 = static_cast<uint16_t>(i * 2);
        uint16_t top0 = static_cast<uint16_t>(i * 2 + 1);
        uint16_t bottom1 = static_cast<uint16_t>(((i + 1) % segments) * 2);
        uint16_t top1 = static_cast<uint16_t>(((i + 1) % segments) * 2 + 1);

        indices.push_back(bottom0);
        indices.push_back(top0);
        indices.push_back(top1);

        indices.push_back(bottom0);
        indices.push_back(top1);
        indices.push_back(bottom1);
    }

    uint16_t topCenterIndex = static_cast<uint16_t>(vertices.size());
    vertices.push_back({0.0f, height, 0.0f, 0.0f, 1.0f, 0.0f});
    uint16_t bottomCenterIndex = static_cast<uint16_t>(vertices.size());
    vertices.push_back({0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f});

    for (int i = 0; i < segments; ++i) {
        uint16_t top0 = static_cast<uint16_t>(i * 2 + 1);
        uint16_t top1 = static_cast<uint16_t>(((i + 1) % segments) * 2 + 1);
        indices.push_back(topCenterIndex);
        indices.push_back(top0);
        indices.push_back(top1);

        uint16_t bottom0 = static_cast<uint16_t>(i * 2);
        uint16_t bottom1 = static_cast<uint16_t>(((i + 1) % segments) * 2);
        indices.push_back(bottomCenterIndex);
        indices.push_back(bottom1);
        indices.push_back(bottom0);
    }

    return createMesh(vertices, indices);
}

Mesh createCone(int segments, float radius, float height) {
    std::vector<Vertex> vertices;
    std::vector<uint16_t> indices;
    vertices.reserve(static_cast<size_t>(segments) + 2);

    for (int i = 0; i < segments; ++i) {
        float angle = (static_cast<float>(i) / static_cast<float>(segments)) * 2.0f * PI;
        float x = std::cos(angle) * radius;
        float z = std::sin(angle) * radius;
        Vec3 edge = normalize(Vec3{x, height, z});
        vertices.push_back({x, 0.0f, z, edge.x, edge.y, edge.z});
    }

    uint16_t tipIndex = static_cast<uint16_t>(vertices.size());
    vertices.push_back({0.0f, height, 0.0f, 0.0f, 1.0f, 0.0f});
    uint16_t baseCenterIndex = static_cast<uint16_t>(vertices.size());
    vertices.push_back({0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f});

    for (int i = 0; i < segments; ++i) {
        uint16_t base0 = static_cast<uint16_t>(i);
        uint16_t base1 = static_cast<uint16_t>((i + 1) % segments);

        indices.push_back(tipIndex);
        indices.push_back(base0);
        indices.push_back(base1);

        indices.push_back(baseCenterIndex);
        indices.push_back(base1);
        indices.push_back(base0);
    }

    return createMesh(vertices, indices);
}

Mesh createCoinMesh(int segments, float radius, float thickness) {
    std::vector<Vertex> vertices;
    std::vector<uint16_t> indices;

    float half = thickness * 0.5f;
    for (int i = 0; i <= segments; ++i) {
        float angle = (static_cast<float>(i) / static_cast<float>(segments)) * 2.0f * PI;
        float x = std::cos(angle) * radius;
        float z = std::sin(angle) * radius;
        Vec3 normal = normalize(Vec3{x, 0.0f, z});
        vertices.push_back({x, -half, z, normal.x, normal.y, normal.z});
        vertices.push_back({x, half, z, normal.x, normal.y, normal.z});
    }

    for (int i = 0; i < segments; ++i) {
        uint16_t bottom0 = static_cast<uint16_t>(i * 2);
        uint16_t top0 = static_cast<uint16_t>(i * 2 + 1);
        uint16_t bottom1 = static_cast<uint16_t>(((i + 1) % segments) * 2);
        uint16_t top1 = static_cast<uint16_t>(((i + 1) % segments) * 2 + 1);

        indices.push_back(bottom0);
        indices.push_back(top0);
        indices.push_back(top1);

        indices.push_back(bottom0);
        indices.push_back(top1);
        indices.push_back(bottom1);
    }

    uint16_t topCenter = static_cast<uint16_t>(vertices.size());
    vertices.push_back({0.0f, half, 0.0f, 0.0f, 1.0f, 0.0f});
    uint16_t bottomCenter = static_cast<uint16_t>(vertices.size());
    vertices.push_back({0.0f, -half, 0.0f, 0.0f, -1.0f, 0.0f});

    for (int i = 0; i < segments; ++i) {
        uint16_t top0 = static_cast<uint16_t>(i * 2 + 1);
        uint16_t top1 = static_cast<uint16_t>(((i + 1) % segments) * 2 + 1);
        indices.push_back(topCenter);
        indices.push_back(top0);
        indices.push_back(top1);

        uint16_t bottom0 = static_cast<uint16_t>(i * 2);
        uint16_t bottom1 = static_cast<uint16_t>(((i + 1) % segments) * 2);
        indices.push_back(bottomCenter);
        indices.push_back(bottom1);
        indices.push_back(bottom0);
    }

    return createMesh(vertices, indices);
}
} // namespace

GameRenderer &renderer() {
    static GameRenderer instance;
    return instance;
}

void GameRenderer::initialize(int width, int height, int qualityLevel) {
    surfaceWidth = std::max(width, 1);
    surfaceHeight = std::max(height, 1);
    quality = qualityLevel;

    ensureShaders();
    createMeshes();
    setQuality(qualityLevel);
    updateProjection();

    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);

    lastFrameTime = std::chrono::steady_clock::now();
    hasLastFrame = false;
    smoothedFps.store(60.0f, std::memory_order_relaxed);
    initialized = true;
}

void GameRenderer::resize(int width, int height) {
    surfaceWidth = std::max(width, 1);
    surfaceHeight = std::max(height, 1);
    updateProjection();
}

void GameRenderer::renderFrame() {
    if (!initialized) {
        return;
    }

    auto now = std::chrono::steady_clock::now();
    float deltaSeconds = 0.016f;
    if (hasLastFrame) {
        deltaSeconds = std::chrono::duration<float>(now - lastFrameTime).count();
    }
    lastFrameTime = now;
    hasLastFrame = true;

    deltaSeconds = std::min(deltaSeconds, 0.1f);

    update(deltaSeconds);
    drawScene();

    if (deltaSeconds > 1e-4f) {
        float instantaneousFps = 1.0f / deltaSeconds;
        float previousFps = smoothedFps.load(std::memory_order_relaxed);
        float blended = previousFps * 0.85f + instantaneousFps * 0.15f;
        smoothedFps.store(blended, std::memory_order_relaxed);
    }
}

void GameRenderer::update(float deltaSeconds) {
    Vec3 forward = {std::sin(cameraYaw), 0.0f, -std::cos(cameraYaw)};
    Vec3 right = {std::cos(cameraYaw), 0.0f, std::sin(cameraYaw)};

    Vec3 movement = {0.0f, 0.0f, 0.0f};
    movement = movement + right * joystickHorizontal;
    movement = movement + forward * joystickVertical;

    float speed = 8.0f + static_cast<float>(quality) * 1.5f;
    float magnitude = length(movement);
    if (magnitude > 0.001f) {
        movement = movement / magnitude;
    }

    playerPosition = playerPosition + movement * (speed * deltaSeconds);
    playerPosition.x = clampf(playerPosition.x, -mapHalfSize + 4.0f, mapHalfSize - 4.0f);
    playerPosition.z = clampf(playerPosition.z, -mapHalfSize + 4.0f, mapHalfSize - 4.0f);

    coinRotation += deltaSeconds * 2.2f;

    for (auto &coin : coins) {
        if (coin.collected) {
            continue;
        }
        float dx = playerPosition.x - coin.position.x;
        float dz = playerPosition.z - coin.position.z;
        float dist = std::sqrt(dx * dx + dz * dz);
        if (dist < 1.2f) {
            coin.collected = true;
            collectedCoinCount.fetch_add(1);
        }
    }
}

void GameRenderer::drawScene() {
    glViewport(0, 0, surfaceWidth, surfaceHeight);
    glClearColor(0.55f, 0.8f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    if (!shaderProgram) {
        return;
    }

    Vec3 eye = {playerPosition.x, playerHeight, playerPosition.z};
    Vec3 dir = {std::sin(cameraYaw) * std::cos(cameraPitch), std::sin(cameraPitch), -std::cos(cameraYaw) * std::cos(cameraPitch)};
    Vec3 target = eye + dir;

    viewMatrix = mat4LookAt(eye, target, {0.0f, 1.0f, 0.0f});
    viewProjectionMatrix = mat4Multiply(projectionMatrix, viewMatrix);

    glUseProgram(shaderProgram);

    Vec3 lightDir = normalize(lightDirection);
    if (uniformLightDir >= 0) {
        glUniform3f(uniformLightDir, lightDir.x, lightDir.y, lightDir.z);
    }

    Mat4 groundModel = mat4Multiply(mat4Translation({0.0f, 0.0f, 0.0f}), mat4Scale({mapHalfSize, 1.0f, mapHalfSize}));
    drawMesh(groundMesh, groundModel, {0.36f, 0.68f, 0.32f});

    for (const auto &tree : trees) {
        Mat4 rotation = mat4RotationY(tree.rotation);
        Mat4 trunkTransform = mat4Multiply(mat4Translation(tree.position), mat4Multiply(rotation, mat4Scale({tree.scale, tree.scale, tree.scale})));
        drawMesh(treeTrunkMesh, trunkTransform, {0.45f, 0.29f, 0.16f});

        Vec3 canopyPosition = tree.position + Vec3{0.0f, tree.scale * 0.9f, 0.0f};
        Mat4 canopyTransform = mat4Multiply(mat4Translation(canopyPosition), mat4Multiply(rotation, mat4Scale({tree.canopyScale, tree.canopyScale, tree.canopyScale})));
        drawMesh(treeLeavesMesh, canopyTransform, {0.12f, 0.55f, 0.22f});
    }

    for (const auto &coin : coins) {
        if (coin.collected) {
            continue;
        }
        float bounce = std::sin(coinRotation * 1.3f + coin.position.x * 0.2f) * 0.15f + 0.3f;
        Vec3 coinPosition = {coin.position.x, bounce, coin.position.z};
        Mat4 model = mat4Multiply(mat4Translation(coinPosition), mat4Multiply(mat4RotationY(coinRotation), mat4Scale({0.6f, 0.2f, 0.6f})));
        drawMesh(coinMesh, model, {0.95f, 0.85f, 0.35f});
    }
}

void GameRenderer::setMovement(float horizontal, float vertical) {
    joystickHorizontal = clampf(horizontal, -1.0f, 1.0f);
    joystickVertical = clampf(vertical, -1.0f, 1.0f);
}

void GameRenderer::applyLookDelta(float deltaX, float deltaY) {
    float sensitivityYaw = 0.0035f;
    float sensitivityPitch = 0.0025f;
    cameraYaw -= deltaX * sensitivityYaw;
    cameraPitch -= deltaY * sensitivityPitch;
    cameraPitch = clampf(cameraPitch, -1.2f, 1.2f);
}

void GameRenderer::setQuality(int qualityLevel) {
    quality = std::clamp(qualityLevel, 0, 2);
    createWorld();
}

int GameRenderer::collectedCoins() const {
    return collectedCoinCount.load();
}

int GameRenderer::totalCoins() const {
    return totalCoinCount.load(std::memory_order_relaxed);
}

float GameRenderer::currentFps() const {
    return smoothedFps.load(std::memory_order_relaxed);
}

void GameRenderer::ensureShaders() {
    if (shaderProgram != 0) {
        return;
    }

    const char *vertexShaderSource = R"(precision mediump float;
attribute vec3 aPosition;
attribute vec3 aNormal;

uniform mat4 uMvp;
uniform mat4 uModel;
uniform vec3 uLightDir;

varying vec3 vNormal;
varying vec3 vLightDir;

void main() {
    vec3 worldNormal = mat3(uModel) * aNormal;
    vNormal = worldNormal;
    vLightDir = -uLightDir;
    gl_Position = uMvp * vec4(aPosition, 1.0);
}
)";

    const char *fragmentShaderSource = R"(precision mediump float;

varying vec3 vNormal;
varying vec3 vLightDir;

uniform vec3 uColor;

void main() {
    vec3 normal = normalize(vNormal);
    vec3 lightDir = normalize(vLightDir);
    float diffuse = max(dot(normal, lightDir), 0.2);
    float ambient = 0.25;
    float intensity = clamp(diffuse + ambient, 0.0, 1.0);
    gl_FragColor = vec4(uColor * intensity, 1.0);
}
)";

    GLuint vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderSource);
    GLuint fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentShaderSource);
    if (!vertexShader || !fragmentShader) {
        return;
    }

    shaderProgram = glCreateProgram();
    glAttachShader(shaderProgram, vertexShader);
    glAttachShader(shaderProgram, fragmentShader);
    glLinkProgram(shaderProgram);

    GLint linked = 0;
    glGetProgramiv(shaderProgram, GL_LINK_STATUS, &linked);
    if (!linked) {
        GLint length = 0;
        glGetProgramiv(shaderProgram, GL_INFO_LOG_LENGTH, &length);
        std::vector<char> buffer(static_cast<size_t>(length));
        glGetProgramInfoLog(shaderProgram, length, nullptr, buffer.data());
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Program link failed: %s", buffer.data());
        glDeleteProgram(shaderProgram);
        shaderProgram = 0;
    }

    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);

    if (shaderProgram != 0) {
        uniformMvp = -1;
        uniformModel = -1;
        uniformColor = -1;
        uniformLightDir = -1;
        attributePosition = -1;
        attributeNormal = -1;
        uniformMvp = glGetUniformLocation(shaderProgram, "uMvp");
        uniformModel = glGetUniformLocation(shaderProgram, "uModel");
        uniformColor = glGetUniformLocation(shaderProgram, "uColor");
        uniformLightDir = glGetUniformLocation(shaderProgram, "uLightDir");
        attributePosition = glGetAttribLocation(shaderProgram, "aPosition");
        attributeNormal = glGetAttribLocation(shaderProgram, "aNormal");
        if (attributePosition < 0 || attributeNormal < 0) {
            __android_log_print(ANDROID_LOG_ERROR, TAG, "Missing vertex attributes: position=%d normal=%d", attributePosition, attributeNormal);
        }
    }
}

void GameRenderer::destroyMeshes() {
    destroyMesh(groundMesh);
    destroyMesh(treeTrunkMesh);
    destroyMesh(treeLeavesMesh);
    destroyMesh(coinMesh);
}

void GameRenderer::createMeshes() {
    destroyMeshes();
    groundMesh = createPlane();
    treeTrunkMesh = createCylinder(20, 0.18f, 1.0f);
    treeLeavesMesh = createCone(20, 0.9f, 1.2f);
    coinMesh = createCoinMesh(40, 0.5f, 0.2f);
}

void GameRenderer::createWorld() {
    std::mt19937 rng(static_cast<uint32_t>(quality * 997u + 42u));
    std::uniform_real_distribution<float> posDist;

    switch (quality) {
        case 0:
            mapHalfSize = 80.0f;
            posDist = std::uniform_real_distribution<float>(-mapHalfSize + 6.0f, mapHalfSize - 6.0f);
            break;
        case 1:
            mapHalfSize = 130.0f;
            posDist = std::uniform_real_distribution<float>(-mapHalfSize + 6.0f, mapHalfSize - 6.0f);
            break;
        default:
            mapHalfSize = 180.0f;
            posDist = std::uniform_real_distribution<float>(-mapHalfSize + 8.0f, mapHalfSize - 8.0f);
            break;
    }

    int treeCount = 220 + quality * 130;
    int desiredCoins = 60 + quality * 30;

    std::uniform_real_distribution<float> scaleDist(3.5f + quality, 6.0f + quality * 1.1f);
    std::uniform_real_distribution<float> canopyDist(1.6f, 2.1f + quality * 0.4f);
    std::uniform_real_distribution<float> rotationDist(0.0f, 2.0f * PI);

    trees.clear();
    trees.reserve(static_cast<size_t>(treeCount));

    Vec3 spawn = {0.0f, 0.0f, 12.0f};
    for (int i = 0; i < treeCount; ++i) {
        Vec3 pos = {posDist(rng), 0.0f, posDist(rng)};
        float dx = pos.x - spawn.x;
        float dz = pos.z - spawn.z;
        if (std::sqrt(dx * dx + dz * dz) < 10.0f) {
            pos.x += (dx >= 0.0f ? 1.0f : -1.0f) * 12.0f;
            pos.z += (dz >= 0.0f ? 1.0f : -1.0f) * 12.0f;
        }
        TreeInstance tree{
                pos,
                scaleDist(rng),
                canopyDist(rng),
                rotationDist(rng)
        };
        trees.push_back(tree);
    }

    coins.clear();
    int newTotalCoins = std::min(desiredCoins, static_cast<int>(trees.size()));
    totalCoinCount.store(newTotalCoins, std::memory_order_relaxed);
    std::uniform_real_distribution<float> angleDist(0.0f, 2.0f * PI);
    std::uniform_real_distribution<float> radiusDist(1.4f, 2.8f);

    coins.reserve(static_cast<size_t>(newTotalCoins));
    for (int i = 0; i < newTotalCoins; ++i) {
        const auto &tree = trees[i];
        float angle = angleDist(rng);
        float radius = radiusDist(rng);
        Vec3 coinPos = tree.position + Vec3{std::cos(angle) * radius, 0.0f, std::sin(angle) * radius};
        coinPos.x = clampf(coinPos.x, -mapHalfSize + 4.0f, mapHalfSize - 4.0f);
        coinPos.z = clampf(coinPos.z, -mapHalfSize + 4.0f, mapHalfSize - 4.0f);
        coins.push_back({coinPos, false});
    }

    collectedCoinCount.store(0);
    smoothedFps.store(60.0f, std::memory_order_relaxed);
    playerPosition = {0.0f, playerHeight, mapHalfSize * 0.6f};
    cameraYaw = PI;
    cameraPitch = -0.15f;
    updateProjection();
}

void GameRenderer::drawMesh(const Mesh &mesh, const Mat4 &modelMatrix, const Vec3 &color) const {
    if (mesh.vbo == 0 || mesh.ibo == 0 || mesh.indexCount == 0) {
        return;
    }
    if (attributePosition < 0 || attributeNormal < 0) {
        return;
    }

    Mat4 mvp = mat4Multiply(viewProjectionMatrix, modelMatrix);
    if (uniformMvp >= 0) {
        glUniformMatrix4fv(uniformMvp, 1, GL_FALSE, mvp.m);
    }
    if (uniformModel >= 0) {
        glUniformMatrix4fv(uniformModel, 1, GL_FALSE, modelMatrix.m);
    }
    if (uniformColor >= 0) {
        glUniform3f(uniformColor, color.x, color.y, color.z);
    }

    glBindBuffer(GL_ARRAY_BUFFER, mesh.vbo);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mesh.ibo);

    glEnableVertexAttribArray(static_cast<GLuint>(attributePosition));
    glVertexAttribPointer(static_cast<GLuint>(attributePosition), 3, GL_FLOAT, GL_FALSE, sizeof(Vertex), reinterpret_cast<void *>(0));

    glEnableVertexAttribArray(static_cast<GLuint>(attributeNormal));
    glVertexAttribPointer(static_cast<GLuint>(attributeNormal), 3, GL_FLOAT, GL_FALSE, sizeof(Vertex), reinterpret_cast<void *>(offsetof(Vertex, nx)));

    glDrawElements(GL_TRIANGLES, mesh.indexCount, GL_UNSIGNED_SHORT, nullptr);

    glDisableVertexAttribArray(static_cast<GLuint>(attributePosition));
    glDisableVertexAttribArray(static_cast<GLuint>(attributeNormal));

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
}

void GameRenderer::updateProjection() {
    float aspect = static_cast<float>(surfaceWidth) / static_cast<float>(surfaceHeight);
    projectionMatrix = mat4Perspective(60.0f * PI / 180.0f, aspect, 0.1f, mapHalfSize * 4.0f + 100.0f);
}

GLuint GameRenderer::compileShader(GLenum type, const char *source) {
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &source, nullptr);
    glCompileShader(shader);

    GLint compiled = 0;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    if (!compiled) {
        GLint length = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &length);
        std::vector<char> buffer(static_cast<size_t>(length));
        glGetShaderInfoLog(shader, length, nullptr, buffer.data());
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Shader compile failed: %s", buffer.data());
        glDeleteShader(shader);
        return 0;
    }
    return shader;
}
