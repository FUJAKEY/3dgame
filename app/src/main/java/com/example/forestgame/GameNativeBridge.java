package com.example.forestgame;

public final class GameNativeBridge {
    static {
        System.loadLibrary("forestgame");
    }

    private GameNativeBridge() {
    }

    public static native void nativeInit(int width, int height, int qualityLevel);

    public static native void nativeResize(int width, int height);

    public static native void nativeRender();

    public static native void nativeSetMovement(float horizontal, float vertical);

    public static native void nativeLookDelta(float deltaX, float deltaY);

    public static native void nativeSetQuality(int qualityLevel);

    public static native int nativeGetCollectedCoins();

    public static native int nativeGetTotalCoins();

    public static native float nativeGetCurrentFps();
}
