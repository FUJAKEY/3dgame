FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jdk \
    wget \
    unzip \
    python3 \
    python3-pip \
    curl \
    ca-certificates && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools" && \
    cd /tmp && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip && \
    unzip -q commandlinetools-linux-11076708_latest.zip && \
    rm commandlinetools-linux-11076708_latest.zip && \
    mv cmdline-tools "$ANDROID_SDK_ROOT/cmdline-tools/latest"

ENV PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"

RUN yes | sdkmanager --sdk_root="$ANDROID_SDK_ROOT" --licenses >/dev/null
RUN sdkmanager --sdk_root="$ANDROID_SDK_ROOT" \
    "platform-tools" \
    "platforms;android-33" \
    "build-tools;33.0.2" \
    "cmake;3.22.1" \
    "ndk;26.1.10909125"

WORKDIR /opt/project
COPY . /opt/project

RUN ./gradlew --no-daemon --console=plain assembleRelease

EXPOSE 8000
ENV APK_PATH=/opt/project/app/build/outputs/apk/release/app-release-unsigned.apk \
    APK_NAME=ForestAdventure.apk \
    PORT=8000

CMD ["python3", "scripts/serve_apk.py"]
