#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
GRADLE_VERSION="8.9"
CACHE_DIR="${GRADLE_BOOTSTRAP_DIR:-$ROOT/.gradle-dist}"
DIST_DIR="$CACHE_DIR/gradle-$GRADLE_VERSION"
ZIP="$CACHE_DIR/gradle-$GRADLE_VERSION-bin.zip"
if [[ ! -x "$DIST_DIR/bin/gradle" ]]; then
  mkdir -p "$CACHE_DIR"
  if [[ ! -f "$ZIP" ]]; then
    URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
    echo "[Androidbaze] Downloading Gradle $GRADLE_VERSION..."
    if command -v curl >/dev/null 2>&1; then
      curl -fL --retry 3 -o "$ZIP" "$URL"
    elif command -v wget >/dev/null 2>&1; then
      wget -O "$ZIP" "$URL"
    else
      echo "curl or wget is required to bootstrap Gradle." >&2
      exit 1
    fi
  fi
  echo "[Androidbaze] Extracting Gradle..."
  if command -v unzip >/dev/null 2>&1; then
    unzip -q -o "$ZIP" -d "$CACHE_DIR"
  else
    python3 - "$ZIP" "$CACHE_DIR" <<'PY'
import sys, zipfile
with zipfile.ZipFile(sys.argv[1]) as z: z.extractall(sys.argv[2])
PY
  fi
fi
exec "$DIST_DIR/bin/gradle" "$@"
