# syntax=docker/dockerfile:1

FROM ubuntu:22.04 AS builder
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jdk \
    wget \
    unzip \
    curl \
    python3 \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV ANDROID_SDK_ROOT=/opt/android-sdk
RUN mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
WORKDIR /tmp
RUN wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O cmdline-tools.zip \
    && unzip -q cmdline-tools.zip -d "$ANDROID_SDK_ROOT/cmdline-tools" \
    && rm cmdline-tools.zip \
    && mv "$ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools" "$ANDROID_SDK_ROOT/cmdline-tools/latest"

ENV PATH="$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/build-tools/34.0.0"
RUN yes | sdkmanager --sdk_root="$ANDROID_SDK_ROOT" --licenses
RUN yes | sdkmanager --sdk_root="$ANDROID_SDK_ROOT" "platform-tools" "platforms;android-34" "build-tools;34.0.0"

WORKDIR /workspace
COPY . /workspace
RUN printf 'sdk.dir=%s\n' "$ANDROID_SDK_ROOT" > local.properties
RUN ./gradlew android:assembleDebug
RUN mv android/build/outputs/apk/debug/android-debug.apk /workspace/ForestCollect-debug.apk

FROM ubuntu:22.04
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y --no-install-recommends python3 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /workspace/ForestCollect-debug.apk ./ForestCollect-debug.apk
COPY --from=builder /workspace/tools/serve_apk.py ./serve_apk.py
RUN chmod +x ./serve_apk.py

EXPOSE 8000
ENV APK_PATH=/app/ForestCollect-debug.apk
ENV APK_FILENAME=ForestCollect-debug.apk
CMD ["python3", "serve_apk.py"]
