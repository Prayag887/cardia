#!/bin/sh
# Fetch the official, checksum-pinned wrapper. No sudo or system modification.
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
expected=2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046
target=gradle/wrapper/gradle-wrapper.jar
hash() {
  if command -v sha256sum >/dev/null 2>&1; then sha256sum "$1" | cut -d ' ' -f 1
  else shasum -a 256 "$1" | cut -d ' ' -f 1; fi
}
if [ -f "$target" ] && [ "$(hash "$target")" = "$expected" ]; then
  echo "Official Gradle wrapper verified. Open this folder in Android Studio."
  exit 0
fi
temp_wrapper=$(mktemp "gradle/wrapper/download.XXXXXX")
trap 'rm -f "$temp_wrapper"' EXIT HUP INT TERM
echo "Downloading official Gradle 8.11.1 wrapper..."
curl --fail --location --connect-timeout 15 --max-time 120 \
  https://raw.githubusercontent.com/gradle/gradle/v8.11.1/gradle/wrapper/gradle-wrapper.jar \
  --output "$temp_wrapper"
if [ "$(hash "$temp_wrapper")" != "$expected" ]; then
  echo "Wrapper checksum mismatch; refusing to execute." >&2; exit 1
fi
mv "$temp_wrapper" "$target"
echo "Ready. Open this folder in Android Studio, or run ./gradlew :androidApp:assembleDebug"
