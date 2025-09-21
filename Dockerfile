FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    ANDROID_HOME=/opt/android-sdk \
    JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 \
    GRADLE_VERSION=8.5 \
    PATH="/opt/gradle/bin:${PATH}"

RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    wget \
    unzip \
    python3 \
    openjdk-17-jdk \
    && rm -rf /var/lib/apt/lists/*

# Install Gradle
RUN wget -q https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -O /tmp/gradle.zip \ 
    && unzip -q /tmp/gradle.zip -d /opt \ 
    && mv /opt/gradle-${GRADLE_VERSION} /opt/gradle \ 
    && rm /tmp/gradle.zip

# Install Android SDK command line tools
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools \ 
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O /tmp/cmdline-tools.zip \ 
    && unzip -q /tmp/cmdline-tools.zip -d /tmp/cmdline-tools \ 
    && mv /tmp/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest \ 
    && rm -rf /tmp/cmdline-tools /tmp/cmdline-tools.zip

RUN yes | ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --licenses
RUN ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager --sdk_root=${ANDROID_SDK_ROOT} \ 
    "platform-tools" \ 
    "platforms;android-34" \ 
    "build-tools;34.0.0"

WORKDIR /app
COPY . /app
RUN echo "sdk.dir=${ANDROID_SDK_ROOT}" > local.properties
RUN gradle assembleRelease assembleDebug

EXPOSE 8000
CMD ["python3", "scripts/serve_apk.py", "app/build/outputs/apk/debug/app-debug.apk"]
