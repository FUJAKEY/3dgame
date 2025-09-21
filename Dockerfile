# Stage 1: build APK
FROM ubuntu:22.04 AS build

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    unzip \
    wget \
    curl \
    python3 \
    git \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

RUN mkdir -p "$ANDROID_HOME/cmdline-tools" && \
    curl -fsSL https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -o /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d $ANDROID_HOME/cmdline-tools && \
    rm /tmp/cmdline-tools.zip && \
    mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest

RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

WORKDIR /workspace
COPY . /workspace

RUN ./gradlew --version
RUN ./gradlew assembleRelease

RUN APK_PATH=$(ls app/build/outputs/apk/release/*.apk | head -n 1) && \
    mkdir -p /tmp/output && \
    cp "$APK_PATH" /tmp/output/ForestCoins-arm64-release.apk

# Stage 2: runtime server
FROM python:3.11-slim AS runtime

WORKDIR /srv/app
COPY --from=build /tmp/output/ForestCoins-arm64-release.apk ./ForestCoins-arm64-release.apk
COPY scripts/serve_apk.py ./serve_apk.py

ENV APK_DIR=/srv/app
ENV APK_SERVER_PORT=8000

EXPOSE 8000
ENTRYPOINT ["python3", "serve_apk.py"]
