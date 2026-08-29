#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
chmod +x ./gradlew
./gradlew --no-daemon assembleDebug
APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
if [[ ! -f "$APK" ]]; then
  echo "Build finished but APK not found: $APK" >&2
  exit 1
fi
echo "[Androidbaze] APK: $APK"
