#!/usr/bin/env bash
set -euo pipefail

GRADLE_VERSION="8.3"
DISTRIBUTION_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
WRAPPER_DIR="${HOME}/.gradle-wrappers"
INSTALL_DIR="${WRAPPER_DIR}/gradle-${GRADLE_VERSION}"
ZIP_PATH="${WRAPPER_DIR}/gradle-${GRADLE_VERSION}-bin.zip"

mkdir -p "${WRAPPER_DIR}"
if [ ! -d "${INSTALL_DIR}" ]; then
  echo "Gradle ${GRADLE_VERSION} not found. Downloading..." >&2
  if [ ! -f "${ZIP_PATH}" ]; then
    if command -v curl >/dev/null 2>&1; then
      curl -L "${DISTRIBUTION_URL}" -o "${ZIP_PATH}"
    elif command -v wget >/dev/null 2>&1; then
      wget "${DISTRIBUTION_URL}" -O "${ZIP_PATH}"
    else
      echo "Neither curl nor wget available to download Gradle." >&2
      exit 1
    fi
  fi
  echo "Extracting Gradle..." >&2
  unzip -q "${ZIP_PATH}" -d "${WRAPPER_DIR}"
fi

GRADLE_CMD="${INSTALL_DIR}/bin/gradle"
if [ ! -x "${GRADLE_CMD}" ]; then
  echo "Gradle binary not found after extraction." >&2
  exit 1
fi

"${GRADLE_CMD}" "$@"
