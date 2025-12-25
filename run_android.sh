#!/bin/bash
echo "--- Ghost Command Deployment ---"

# 1. Check ADB
if ! adb devices | grep -q "device$"; then
    echo "ERROR: No ADB device connected."
    exit 1
fi

# 2. Build & Install
cd android_app
if [ -f "./gradlew" ]; then
    echo "Building with Gradle Wrapper..."
    ./gradlew installDebug
else
    echo "Gradle Wrapper not found. Trying global 'gradle'..."
    if ! command -v gradle &> /dev/null; then
        echo "ERROR: Gradle not found. Please install Gradle or open project in Android Studio to generate wrapper."
        exit 1
    fi
    gradle installDebug
fi

# 3. Launch
echo "Launching Ghost Command..."
adb shell am start -n com.ghostcommand.app/.MainActivity
