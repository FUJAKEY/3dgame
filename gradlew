#!/usr/bin/env sh
set -e

DIR="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"
GRADLE_VERSION="8.2.1"
DIST_NAME="gradle-${GRADLE_VERSION}"
DIST_ZIP="${DIST_NAME}-bin.zip"
CACHE_DIR="${DIR}/.gradle-cache"
GRADLE_HOME="${CACHE_DIR}/${DIST_NAME}"

mkdir -p "${CACHE_DIR}" "${DIR}/.gradle-user"

if [ ! -d "${GRADLE_HOME}" ]; then
  ARCHIVE_PATH="${CACHE_DIR}/${DIST_ZIP}"
  if [ ! -f "${ARCHIVE_PATH}" ]; then
    URL="https://services.gradle.org/distributions/${DIST_ZIP}"
    if command -v curl >/dev/null 2>&1; then
      curl -fLo "${ARCHIVE_PATH}" "${URL}"
    elif command -v wget >/dev/null 2>&1; then
      wget -O "${ARCHIVE_PATH}" "${URL}"
    else
      echo "Не найден curl или wget для загрузки Gradle" >&2
      exit 1
    fi
  fi
  unzip -qo "${ARCHIVE_PATH}" -d "${CACHE_DIR}"
fi

export GRADLE_USER_HOME="${DIR}/.gradle-user"
exec "${GRADLE_HOME}/bin/gradle" "$@"
