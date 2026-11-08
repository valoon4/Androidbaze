#!/usr/bin/env bash
# Pinned-source bootstrap for the isolated D04.1 experimental Android APK.
# No weights are downloaded. Do not check third-party sources into the app repo.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VENDOR="$ROOT/app/src/main/cpp/vendor/llama.cpp"
PIN="a691b2345844ce1b430201fc5fd404ce062e05f2"

if [[ -d "$VENDOR/.git" ]]; then
  ACTUAL="$(git -C "$VENDOR" rev-parse HEAD)"
  if [[ "$ACTUAL" != "$PIN" ]]; then
    echo "Unexpected native library revision: $ACTUAL; expected $PIN" >&2
    exit 1
  fi
  exit 0
fi
if [[ -e "$VENDOR" ]]; then
  echo "Refusing pre-existing, unverified native source directory $VENDOR" >&2
  exit 1
fi
mkdir -p "$(dirname "$VENDOR")"
git init -q "$VENDOR"
git -C "$VENDOR" remote add origin "https://github.com/ggml-org/llama.cpp.git"
git -C "$VENDOR" fetch -q --depth 1 origin "$PIN"
git -C "$VENDOR" checkout -q --detach FETCH_HEAD
ACTUAL="$(git -C "$VENDOR" rev-parse HEAD)"
test "$ACTUAL" = "$PIN"
echo "Pinned llama.cpp ready: $ACTUAL (NO model files)"
