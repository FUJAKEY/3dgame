#!/usr/bin/env sh

##############################################################################
## Gradle start up script for UN*X based systems.
##############################################################################

set -e

APP_BASE_NAME=`basename "$0"`
APP_HOME=`dirname "$0"`

# Resolve symlinks
while [ -h "$APP_HOME/$APP_BASE_NAME" ]; do
  ls=`ls -ld "$APP_HOME/$APP_BASE_NAME"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    APP_HOME=`dirname "$link"`
  else
    APP_HOME="$APP_HOME/`dirname "$link"`"
  fi
  APP_BASE_NAME=`basename "$link"`
done

APP_HOME=`cd "$APP_HOME" && pwd`

WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading Gradle wrapper jar..."
  mkdir -p "$APP_HOME/gradle/wrapper"
  curl -fsSL https://raw.githubusercontent.com/gradle/gradle/v8.2.1/gradle/wrapper/gradle-wrapper.jar -o "$WRAPPER_JAR"
fi

CLASSPATH=$WRAPPER_JAR

JAVA_EXEC="java"

exec "$JAVA_EXEC" -Xmx64m -Xms64m -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
