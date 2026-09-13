#!/bin/bash
APK_PATH="/app/applet/app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo "❌ APK not found. Run compile_applet first."
    exit 1
fi
echo "🔍 Checking APK for legacy icons..."
unzip -l "$APK_PATH" | grep -E "res/mipmap|res/drawable.*launcher" > /tmp/apk_icons.txt

if grep -q "\.webp\|\.png" /tmp/apk_icons.txt; then
    echo "❌ ERROR: Legacy raster icons (.webp or .png) found in the APK!"
    cat /tmp/apk_icons.txt | grep -E "\.webp|\.png"
    exit 1
else
    echo "✅ SUCCESS: No legacy icons persist. Only vector resources are bundled."
    cat /tmp/apk_icons.txt
    exit 0
fi
