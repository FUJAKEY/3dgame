#pragma once

#include <cmath>

struct Vec2 {
    float x;
    float y;
};

struct Vec3 {
    float x;
    float y;
    float z;
};

struct Mat4 {
    float m[16];
};

inline Vec3 operator+(const Vec3 &a, const Vec3 &b) {
    return {a.x + b.x, a.y + b.y, a.z + b.z};
}

inline Vec3 operator-(const Vec3 &a, const Vec3 &b) {
    return {a.x - b.x, a.y - b.y, a.z - b.z};
}

inline Vec3 operator*(const Vec3 &v, float scalar) {
    return {v.x * scalar, v.y * scalar, v.z * scalar};
}

inline Vec3 operator*(float scalar, const Vec3 &v) {
    return {v.x * scalar, v.y * scalar, v.z * scalar};
}

inline Vec3 operator/(const Vec3 &v, float scalar) {
    return {v.x / scalar, v.y / scalar, v.z / scalar};
}

inline float dot(const Vec3 &a, const Vec3 &b) {
    return a.x * b.x + a.y * b.y + a.z * b.z;
}

inline Vec3 cross(const Vec3 &a, const Vec3 &b) {
    return {a.y * b.z - a.z * b.y,
            a.z * b.x - a.x * b.z,
            a.x * b.y - a.y * b.x};
}

inline float length(const Vec3 &v) {
    return std::sqrt(dot(v, v));
}

inline Vec3 normalize(const Vec3 &v) {
    float len = length(v);
    if (len < 1e-6f) {
        return {0.0f, 0.0f, 0.0f};
    }
    return v / len;
}

inline Mat4 mat4Identity() {
    Mat4 out = {};
    out.m[0] = 1.0f;
    out.m[5] = 1.0f;
    out.m[10] = 1.0f;
    out.m[15] = 1.0f;
    return out;
}

inline Mat4 mat4Multiply(const Mat4 &a, const Mat4 &b) {
    Mat4 result = {};
    for (int col = 0; col < 4; ++col) {
        for (int row = 0; row < 4; ++row) {
            result.m[col * 4 + row] =
                    a.m[0 * 4 + row] * b.m[col * 4 + 0] +
                    a.m[1 * 4 + row] * b.m[col * 4 + 1] +
                    a.m[2 * 4 + row] * b.m[col * 4 + 2] +
                    a.m[3 * 4 + row] * b.m[col * 4 + 3];
        }
    }
    return result;
}

inline Mat4 mat4Translation(const Vec3 &t) {
    Mat4 out = mat4Identity();
    out.m[12] = t.x;
    out.m[13] = t.y;
    out.m[14] = t.z;
    return out;
}

inline Mat4 mat4Scale(const Vec3 &s) {
    Mat4 out = {};
    out.m[0] = s.x;
    out.m[5] = s.y;
    out.m[10] = s.z;
    out.m[15] = 1.0f;
    return out;
}

inline Mat4 mat4RotationX(float angleRad) {
    float c = std::cos(angleRad);
    float s = std::sin(angleRad);
    Mat4 out = mat4Identity();
    out.m[5] = c;
    out.m[6] = s;
    out.m[9] = -s;
    out.m[10] = c;
    return out;
}

inline Mat4 mat4RotationY(float angleRad) {
    float c = std::cos(angleRad);
    float s = std::sin(angleRad);
    Mat4 out = mat4Identity();
    out.m[0] = c;
    out.m[2] = -s;
    out.m[8] = s;
    out.m[10] = c;
    return out;
}

inline Mat4 mat4RotationZ(float angleRad) {
    float c = std::cos(angleRad);
    float s = std::sin(angleRad);
    Mat4 out = mat4Identity();
    out.m[0] = c;
    out.m[1] = s;
    out.m[4] = -s;
    out.m[5] = c;
    return out;
}

inline Mat4 mat4Perspective(float fovYRad, float aspect, float zNear, float zFar) {
    float f = 1.0f / std::tan(fovYRad * 0.5f);
    Mat4 out = {};
    out.m[0] = f / aspect;
    out.m[5] = f;
    out.m[10] = (zFar + zNear) / (zNear - zFar);
    out.m[11] = -1.0f;
    out.m[14] = (2.0f * zFar * zNear) / (zNear - zFar);
    return out;
}

inline Mat4 mat4LookAt(const Vec3 &eye, const Vec3 &target, const Vec3 &up) {
    Vec3 f = normalize(target - eye);
    Vec3 s = normalize(cross(f, up));
    Vec3 u = cross(s, f);

    Mat4 out = mat4Identity();
    out.m[0] = s.x;
    out.m[4] = s.y;
    out.m[8] = s.z;

    out.m[1] = u.x;
    out.m[5] = u.y;
    out.m[9] = u.z;

    out.m[2] = -f.x;
    out.m[6] = -f.y;
    out.m[10] = -f.z;

    out.m[12] = -dot(s, eye);
    out.m[13] = -dot(u, eye);
    out.m[14] = dot(f, eye);
    return out;
}

inline Vec3 rotateY(const Vec3 &v, float angleRad) {
    float c = std::cos(angleRad);
    float s = std::sin(angleRad);
    return {v.x * c - v.z * s, v.y, v.x * s + v.z * c};
}

inline float clampf(float value, float minValue, float maxValue) {
    if (value < minValue) {
        return minValue;
    }
    if (value > maxValue) {
        return maxValue;
    }
    return value;
}
