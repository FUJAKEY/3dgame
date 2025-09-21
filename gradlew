#!/usr/bin/env sh

set -e

BASEDIR=$(cd "$(dirname "$0")"; pwd)
GRADLE_DIR="$BASEDIR/.gradle"
WRAPPER_VERSION="8.4"
WRAPPER_DIST_URL="https://services.gradle.org/distributions/gradle-${WRAPPER_VERSION}-bin.zip"
INSTALL_DIR="$GRADLE_DIR/gradle-${WRAPPER_VERSION}"
GRADLE_BIN="$INSTALL_DIR/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
  echo "Downloading Gradle ${WRAPPER_VERSION}..." >&2
  mkdir -p "$INSTALL_DIR"
  TMP_DIR=$(mktemp -d)
  curl -fL "$WRAPPER_DIST_URL" -o "$TMP_DIR/gradle.zip"
  unzip -q "$TMP_DIR/gradle.zip" -d "$TMP_DIR"
  EXTRACTED=$(find "$TMP_DIR" -maxdepth 1 -type d -name "gradle-*" | head -n 1)
  if [ -z "$EXTRACTED" ]; then
    echo "Не удалось распаковать Gradle" >&2
    rm -rf "$TMP_DIR"
    exit 1
  fi
  rm -rf "$INSTALL_DIR"
  mv "$EXTRACTED" "$INSTALL_DIR"
  rm -rf "$TMP_DIR"
fi

exec "$GRADLE_BIN" "$@"
