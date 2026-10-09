#!/usr/bin/env bash
# Downloads the pinned Sauce Labs Mobile Sample App binaries used by the demo scenarios.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APPS="$ROOT/apps"
VERSION="2.7.1"
BASE="https://github.com/saucelabs/sample-app-mobile/releases/download/${VERSION}"

mkdir -p "$APPS"

ANDROID_APK="Android.SauceLabs.Mobile.Sample.app.${VERSION}.apk"
IOS_ZIP="iOS.Simulator.SauceLabs.Mobile.Sample.app.${VERSION}.zip"

if [[ ! -f "$APPS/$ANDROID_APK" ]]; then
  echo "Downloading Android demo app..."
  curl -fL --retry 3 --retry-delay 2 -o "$APPS/$ANDROID_APK" "$BASE/$ANDROID_APK"
else
  echo "Android demo app already present: apps/$ANDROID_APK"
fi

IOS_DIR="$APPS/iOS.Simulator.SauceLabs.Mobile.Sample.app.${VERSION}"
if [[ ! -d "$IOS_DIR/Payload" ]]; then
  echo "Downloading iOS simulator demo app..."
  curl -fL --retry 3 --retry-delay 2 -o "$APPS/$IOS_ZIP" "$BASE/$IOS_ZIP"
  rm -rf "$IOS_DIR"
  mkdir -p "$IOS_DIR"
  unzip -q "$APPS/$IOS_ZIP" -d "$IOS_DIR"
  rm -f "$APPS/$IOS_ZIP"
else
  echo "iOS demo app already present under apps/$(basename "$IOS_DIR")"
fi

echo "Demo apps ready under apps/"
ls -la "$APPS"
