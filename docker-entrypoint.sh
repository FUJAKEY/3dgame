#!/usr/bin/env bash
set -euo pipefail

WORKDIR="/workspace"
APK_PATH="$WORKDIR/app/build/outputs/apk/release/app-release.apk"
DIST_DIR="$WORKDIR/dist"
APK_TARGET="$DIST_DIR/forest-quest.apk"

mkdir -p "$DIST_DIR"
cd "$WORKDIR"

./gradlew assembleRelease

cp "$APK_PATH" "$APK_TARGET"

cd "$DIST_DIR"
exec python3 /workspace/serve_apk.py
