#!/usr/bin/env bash
# Local verified bootstrap. GitHub Actions uses setup-gradle pinned to 8.13 instead.
set -euo pipefail
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GRADLE_VERSION="8.13"
GRADLE_SHA256="20f1b1176237254a6fc204d8434196fa11a4cfb387567519c61556e8710aed78"
CACHE="${GRADLE_BOOTSTRAP_DIR:-$PROJECT_ROOT/.gradle-dist}"
ZIP="$CACHE/gradle-$GRADLE_VERSION-bin.zip"
BIN="$CACHE/gradle-$GRADLE_VERSION/bin/gradle"

if [[ ! -x "$BIN" ]]; then
  mkdir -p "$CACHE"
  if [[ ! -f "$ZIP" ]]; then
    echo "Downloading Gradle $GRADLE_VERSION (verified SHA-256)..."
    curl -fL --retry 3 "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP"
  fi
  if command -v sha256sum >/dev/null 2>&1; then
    ACTUAL_SHA="$(sha256sum "$ZIP" | awk '{print $1}')"
  else
    ACTUAL_SHA="$(shasum -a 256 "$ZIP" | awk '{print $1}')"
  fi
  if [[ "$ACTUAL_SHA" != "$GRADLE_SHA256" ]]; then
    echo "Gradle download SHA-256 mismatch. Refusing to run." >&2
    rm -f "$ZIP"
    exit 1
  fi
  unzip -q "$ZIP" -d "$CACHE"
fi
exec "$BIN" -p "$PROJECT_ROOT" "${@:-:app:assembleDebug}"
