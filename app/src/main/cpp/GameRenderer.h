#pragma once

#include <GLES3/gl3.h>
#include <android/log.h>
#include <atomic>
#include <chrono>
#include <random>
#include <vector>

#include "MathUtils.h"

struct Mesh {
    GLuint vao = 0;
    GLuint vbo = 0;
    GLuint ibo = 0;
    GLsizei indexCount = 0;
};

struct TreeInstance {
    Vec3 position;
    float scale;
    float canopyScale;
    float rotation;
};

struct CoinInstance {
    Vec3 position;
    bool collected = false;
};

class GameRenderer {
public:
    void initialize(int width, int height, int qualityLevel);
    void resize(int width, int height);
    void renderFrame();
    void setMovement(float horizontal, float vertical);
    void applyLookDelta(float deltaX, float deltaY);
    void setQuality(int qualityLevel);
    int collectedCoins() const;
    int totalCoins() const;
    float currentFps() const;

private:
    void ensureShaders();
    void destroyMeshes();
    void createMeshes();
    void createWorld();
    void update(float deltaSeconds);
    void drawScene();
    void drawMesh(const Mesh &mesh, const Mat4 &modelMatrix, const Vec3 &color) const;
    void updateProjection();

    GLuint compileShader(GLenum type, const char *source);

    bool initialized = false;
    int surfaceWidth = 1;
    int surfaceHeight = 1;
    int quality = 1;

    Mesh groundMesh;
    Mesh treeTrunkMesh;
    Mesh treeLeavesMesh;
    Mesh coinMesh;

    GLuint shaderProgram = 0;
    GLint uniformMvp = -1;
    GLint uniformModel = -1;
    GLint uniformColor = -1;
    GLint uniformLightDir = -1;

    Vec3 lightDirection = {-0.4f, -1.0f, -0.3f};

    std::vector<TreeInstance> trees;
    std::vector<CoinInstance> coins;

    float mapHalfSize = 120.0f;
    Vec3 playerPosition = {0.0f, 1.6f, 12.0f};
    float playerHeight = 1.6f;
    float cameraYaw = 0.0f;
    float cameraPitch = -0.2f;

    float joystickHorizontal = 0.0f;
    float joystickVertical = 0.0f;

    Mat4 projectionMatrix = mat4Identity();
    Mat4 viewMatrix = mat4Identity();
    Mat4 viewProjectionMatrix = mat4Identity();

    std::chrono::steady_clock::time_point lastFrameTime;
    bool hasLastFrame = false;

    std::atomic<int> collectedCoinCount{0};
    int totalCoinCount = 0;

    float coinRotation = 0.0f;
    float smoothedFps = 60.0f;
};

GameRenderer &renderer();
