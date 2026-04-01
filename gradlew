#!/bin/bash

# Gradle wrapper script for Unix-based systems
# Download and run the Gradle wrapper

DEFAULT_GRADLE_VERSION="8.2"
GRADLE_WRAPPER_PROPERTIES="gradle/wrapper/gradle-wrapper.properties"

if [ ! -f "$GRADLE_WRAPPER_PROPERTIES" ]; then
    echo "Error: $GRADLE_WRAPPER_PROPERTIES not found."
    exit 1
fi

# Extract distribution URL
DISTRIBUTION_URL=$(grep "distributionUrl" "$GRADLE_WRAPPER_PROPERTIES" | cut -d'=' -f2 | tr -d '[:space:]')

if [ -z "$DISTRIBUTION_URL" ]; then
    DISTRIBUTION_URL="https://services.gradle.org/distributions/gradle-${DEFAULT_GRADLE_VERSION}-bin.zip"
fi

# Determine script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Download Gradle if needed
GRADLE_HOME="${HOME}/.gradle/wrapper/dists/$(basename "$DISTRIBUTION_URL" .zip)"
if [ ! -d "$GRADLE_HOME" ]; then
    echo "Downloading Gradle from $DISTRIBUTION_URL..."
    mkdir -p "$GRADLE_HOME"
    curl -sL "$DISTRIBUTION_URL" -o "/tmp/$(basename "$DISTRIBUTION_URL")" || wget -q "$DISTRIBUTION_URL" -O "/tmp/$(basename "$DISTRIBUTION_URL")"
    unzip -q "/tmp/$(basename "$DISTRIBUTION_URL")" -d "$GRADLE_HOME"
    rm "/tmp/$(basename "$DISTRIBUTION_URL")"
fi

# Run Gradle
"$GRADLE_HOME/gradle-*/bin/gradle" "$@"