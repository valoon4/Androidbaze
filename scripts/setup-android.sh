#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$ROOT/.android-sdk}}"
TOOLS_REV="11076708"
TOOLS_ZIP="$ROOT/.android-commandlinetools.zip"
export ANDROID_SDK_ROOT="$SDK"
export ANDROID_HOME="$SDK"
mkdir -p "$SDK/cmdline-tools"
if [[ ! -x "$SDK/cmdline-tools/latest/bin/sdkmanager" ]]; then
  URL="https://dl.google.com/android/repository/commandlinetools-linux-${TOOLS_REV}_latest.zip"
  echo "[Androidbaze] Bootstrapping Android command-line tools..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL --retry 3 -o "$TOOLS_ZIP" "$URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$TOOLS_ZIP" "$URL"
  else
    echo "curl or wget is required." >&2
    exit 1
  fi
  TMP="$ROOT/.android-tools-tmp"
  rm -rf "$TMP"
  mkdir -p "$TMP"
  if command -v unzip >/dev/null 2>&1; then
    unzip -q -o "$TOOLS_ZIP" -d "$TMP"
  else
    python3 - "$TOOLS_ZIP" "$TMP" <<'PY'
import sys, zipfile
with zipfile.ZipFile(sys.argv[1]) as z: z.extractall(sys.argv[2])
PY
  fi
  rm -rf "$SDK/cmdline-tools/latest"
  mkdir -p "$SDK/cmdline-tools/latest"
  mv "$TMP/cmdline-tools"/* "$SDK/cmdline-tools/latest/"
  rm -rf "$TMP" "$TOOLS_ZIP"
fi
SDKMANAGER="$SDK/cmdline-tools/latest/bin/sdkmanager"
yes | "$SDKMANAGER" --sdk_root="$SDK" --licenses >/dev/null || true
"$SDKMANAGER" --sdk_root="$SDK" "platform-tools" "platforms;android-35" "build-tools;35.0.0"
printf 'sdk.dir=%s\n' "$SDK" > "$ROOT/local.properties"
echo "[Androidbaze] Android SDK ready at $SDK"
