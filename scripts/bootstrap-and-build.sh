#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
chmod +x scripts/setup-android.sh scripts/build-debug.sh gradlew
./scripts/setup-android.sh
./scripts/build-debug.sh
