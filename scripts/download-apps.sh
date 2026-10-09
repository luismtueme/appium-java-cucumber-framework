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
IOS_APP="$APPS/iOS.Simulator.SauceLabs.Mobile.Sample.app.${VERSION}.app"

if [[ ! -f "$APPS/$ANDROID_APK" ]]; then
  echo "Downloading Android demo app..."
  curl -fL --retry 3 --retry-delay 2 -o "$APPS/$ANDROID_APK" "$BASE/$ANDROID_APK"
else
  echo "Android demo app already present: apps/$ANDROID_APK"
fi

if [[ ! -d "$IOS_APP" ]]; then
  echo "Downloading iOS simulator demo app..."
  curl -fL --retry 3 --retry-delay 2 -o "$APPS/$IOS_ZIP" "$BASE/$IOS_ZIP"
  EXTRACT="$APPS/_ios_extract"
  rm -rf "$EXTRACT" "$IOS_APP"
  mkdir -p "$EXTRACT"
  unzip -q "$APPS/$IOS_ZIP" -d "$EXTRACT"
  # Release zip ships the .app at the archive root (not an IPA Payload tree).
  FOUND="$(find "$EXTRACT" -maxdepth 2 -type d -name '*.app' | head -n 1)"
  if [[ -z "$FOUND" ]]; then
    echo "ERROR: no .app bundle inside $IOS_ZIP" >&2
    find "$EXTRACT" -maxdepth 3 >&2 || true
    exit 1
  fi
  mv "$FOUND" "$IOS_APP"
  rm -rf "$EXTRACT" "$APPS/$IOS_ZIP"
else
  echo "iOS demo app already present: apps/$(basename "$IOS_APP")"
fi

echo "Demo apps ready under apps/"
ls -la "$APPS"
