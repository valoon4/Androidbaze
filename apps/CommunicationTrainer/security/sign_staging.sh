#!/usr/bin/env bash
# Sign a pre-verified, NON-DEBUGGABLE staging APK with a persistent private key.
# Environment: CT_STAGING_KEYSTORE_B64, CT_STAGING_STORE_PASSWORD,
# CT_STAGING_KEY_PASSWORD, CT_STAGING_KEY_ALIAS, CT_STAGING_SIGNER_SHA256.
# Never put these values in a repository, job log, issue, or download artifact.
set -euo pipefail
set +x
umask 077

if [[ "$#" -ne 2 ]]; then
  echo "Usage: sign_staging.sh <unsigned-staging.apk> <signed-output.apk>" >&2
  exit 2
fi
: "${CT_STAGING_KEYSTORE_B64:?Configure protected staging keystore secret}"
: "${CT_STAGING_STORE_PASSWORD:?Configure protected staging store-password secret}"
: "${CT_STAGING_KEY_PASSWORD:?Configure protected staging key-password secret}"
: "${CT_STAGING_KEY_ALIAS:?Configure protected staging key alias}"
: "${CT_STAGING_SIGNER_SHA256:?Configure the public, expected SHA-256 certificate fingerprint}"
: "${ANDROID_HOME:?Android SDK path is required}"

INPUT="$(realpath "$1")"
OUTPUT="$2"
if [[ ! -f "$INPUT" ]]; then
  echo "Unsigned staging APK missing" >&2
  exit 1
fi
case "$OUTPUT" in
  *.apk) ;;
  *) echo "Output must end in .apk" >&2; exit 2 ;;
esac

TOOLS="$ANDROID_HOME/build-tools/36.0.0"
TEMPDIR="$(mktemp -d)"
trap 'rm -rf "$TEMPDIR"' EXIT
printf '%s' "$CT_STAGING_KEYSTORE_B64" | base64 -d > "$TEMPDIR/staging.jks"
test -s "$TEMPDIR/staging.jks"

# An APK signed in this step must first pass manifest checks; fail closed.
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
python3 "$SCRIPT_DIR/verify_staging_apk.py" "$INPUT"

"$TOOLS/zipalign" -f -p 4 "$INPUT" "$TEMPDIR/aligned.apk"
mkdir -p "$(dirname "$OUTPUT")"
"$TOOLS/apksigner" sign \
  --ks "$TEMPDIR/staging.jks" \
  --ks-key-alias "$CT_STAGING_KEY_ALIAS" \
  --ks-pass env:CT_STAGING_STORE_PASSWORD \
  --key-pass env:CT_STAGING_KEY_PASSWORD \
  --out "$OUTPUT" \
  "$TEMPDIR/aligned.apk"

"$TOOLS/apksigner" verify --verbose --print-certs "$OUTPUT" > "$TEMPDIR/verify.txt"
FINGERPRINT="$(sed -n 's/^Signer #1 certificate SHA-256 digest: //p' "$TEMPDIR/verify.txt" | head -n 1 | tr 'A-F' 'a-f')"
EXPECTED="$(printf '%s' "$CT_STAGING_SIGNER_SHA256" | tr -d ':' | tr 'A-F' 'a-f')"
if [[ -z "$FINGERPRINT" || ! "$FINGERPRINT" =~ ^[0-9a-f]{64}$ || "$FINGERPRINT" != "$EXPECTED" ]]; then
  rm -f "$OUTPUT"
  echo "Signing certificate fingerprint mismatch: refusing staging distribution" >&2
  exit 1
fi
python3 "$SCRIPT_DIR/verify_staging_apk.py" "$OUTPUT"
echo "SIGNED_STAGING_VERIFIED: stable key fingerprint matched; debuggable false; PreviewActivity absent"
sha256sum "$OUTPUT"
