#!/usr/bin/env bash
set -euo pipefail

# On the isolated reference emulator, never use publisher signing credentials.
# Sign the actual R8/minified release APK with a one-day disposable QA key.
# The resulting artifact MUST NOT be uploaded or labelled Play-ready.
gradle --no-daemon :app:assembleRelease
apk="$(find app/build/outputs/apk/release -maxdepth 1 -type f -name '*.apk' | head -n 1)"
test -n "$apk" && test -s "$apk"
count="$(find app/build/outputs/apk/release -maxdepth 1 -type f -name '*.apk' | wc -l)"
if [[ "$count" -ne 1 ]]; then
  echo "::error::Expected exactly one optimized release APK"
  exit 1
fi

signer="$(find "$ANDROID_HOME/build-tools" -maxdepth 2 -type f -name apksigner | sort -V | tail -n 1)"
if [[ -z "$signer" || ! -x "$signer" ]]; then
  echo "::error::Android SDK apksigner not installed"
  exit 1
fi
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
export SUDOKU_QA_PASSWORD
SUDOKU_QA_PASSWORD="$(openssl rand -hex 24)"
keytool -genkeypair -noprompt -keystore "$work/qa.jks" \
  -storepass "$SUDOKU_QA_PASSWORD" -keypass "$SUDOKU_QA_PASSWORD" \
  -alias sudoku-qa-only -keyalg RSA -keysize 3072 -validity 1 \
  -dname "CN=Sudoku temporary emulator test certificate"
"$signer" sign \
  --ks "$work/qa.jks" --ks-key-alias sudoku-qa-only \
  --ks-pass env:SUDOKU_QA_PASSWORD --key-pass env:SUDOKU_QA_PASSWORD \
  --out "$work/Sudoku-R8-emulator-only.apk" "$apk"
"$signer" verify --verbose "$work/Sudoku-R8-emulator-only.apk"
sha256sum "$apk" "$work/Sudoku-R8-emulator-only.apk"
python3 scripts/release_runtime_smoke.py \
  --apk "$work/Sudoku-R8-emulator-only.apk" --screenshots screenshots
