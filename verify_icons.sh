#!/bin/bash
APK_PATH="/app/applet/app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo "APK not found. Run compile_applet first."
    exit 1
fi
echo "Checking APK for legacy icons..."
unzip -l "$APK_PATH" | grep -E "res/mipmap|res/drawable.*launcher" > /tmp/apk_icons.txt
echo "APK contains the following icon resources:"
cat /tmp/apk_icons.txt
