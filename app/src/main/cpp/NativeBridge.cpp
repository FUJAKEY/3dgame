#include <jni.h>

#include "GameRenderer.h"

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_forestgame_GameNativeBridge_nativeInit(JNIEnv *, jclass, jint width, jint height,
                                                        jint qualityLevel) {
    renderer().initialize(width, height, qualityLevel);
}

JNIEXPORT void JNICALL
Java_com_example_forestgame_GameNativeBridge_nativeResize(JNIEnv *, jclass, jint width,
                                                          jint height) {
    renderer().resize(width, height);
}

JNIEXPORT void JNICALL
Java_com_example_forestgame_GameNativeBridge_nativeRender(JNIEnv *, jclass) {
    renderer().renderFrame();
}

JNIEXPORT void JNICALL
Java_com_example_forestgame_GameNativeBridge_nativeSetMovement(JNIEnv *, jclass,
                                                               jfloat horizontal, jfloat vertical) {
    renderer().setMovement(horizontal, vertical);
}

JNIEXPORT void JNICALL
Java_com_example_forestgame_GameNativeBridge_nativeLookDelta(JNIEnv *, jclass, jfloat deltaX,
                                                             jfloat deltaY) {
    renderer().applyLookDelta(deltaX, deltaY);
}

JNIEXPORT void JNICALL
Java_com_example_forestgame_GameNativeBridge_nativeSetQuality(JNIEnv *, jclass,
                                                              jint qualityLevel) {
    renderer().setQuality(qualityLevel);
}

JNIEXPORT jint JNICALL
Java_com_example_forestgame_GameNativeBridge_nativeGetCollectedCoins(JNIEnv *, jclass) {
    return static_cast<jint>(renderer().collectedCoins());
}

JNIEXPORT jint JNICALL
Java_com_example_forestgame_GameNativeBridge_nativeGetTotalCoins(JNIEnv *, jclass) {
    return static_cast<jint>(renderer().totalCoins());
}

JNIEXPORT jfloat JNICALL
Java_com_example_forestgame_GameNativeBridge_nativeGetCurrentFps(JNIEnv *, jclass) {
    return renderer().currentFps();
}

}
