package com.example.forestgame;

import android.util.Log;

public final class GameNativeBridge {
    private static final String TAG = "ForestGameNative";

    static {
        try {
            System.loadLibrary("forestgame");
        } catch (UnsatisfiedLinkError error) {
            Log.e(TAG, "Не удалось загрузить библиотеку forestgame", error);
            throw error;
        }
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
