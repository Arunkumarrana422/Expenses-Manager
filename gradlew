#!/bin/sh
# Gradle startup script for POSIX systems
APP_BASE_NAME=`basename "$0"`
APP_HOME=`cd "\`dirname \"$0\"\`" >/dev/null 2>&1 && pwd`
exec java -version >/dev/null 2>&1 || { echo "Java not found"; exit 1; }
exec "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
