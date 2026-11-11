#!/usr/bin/env bash
# Offline contract tests for verify-release-assets.sh. No GitHub credentials needed.
set -euo pipefail
cd "$(dirname "$0")"
repo_root="$(pwd)"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
mkdir -p "$tmp/bin" "$tmp/sdk/build-tools/36.0.0" "$tmp/source"

# This test checks the release contract; the real release workflow runs
# the Android SDK's apksigner against downloaded production/test APKs.
printf '#!/bin/sh\nexit 0\n' > "$tmp/sdk/build-tools/36.0.0/apksigner"
chmod +x "$tmp/sdk/build-tools/36.0.0/apksigner"
printf 'fixture\n' > "$tmp/source/AndroidManifest.xml"
(
  cd "$tmp/source"
  zip -q Sudoku-v1.0.62-test.apk AndroidManifest.xml
  zip -q Sudoku-v1.0.62-unsigned.aab AndroidManifest.xml
  sha256sum Sudoku-*.apk Sudoku-*.aab > SHA256SUMS
)
cat > "$tmp/release.json" <<'JSON'
{"target_commitish":"04d9f7fffa8939f62213297eef609ebd5e098d8f","prerelease":true,"assets":[{"name":"Sudoku-v1.0.62-test.apk"},{"name":"Sudoku-v1.0.62-unsigned.aab"},{"name":"SHA256SUMS"}]}
JSON
cat > "$tmp/bin/gh" <<'MOCK'
#!/usr/bin/env bash
set -euo pipefail
if [[ "$1" == api ]]; then
  cat "$MOCK_ROOT/release.json"
  exit 0
fi
if [[ "$1" == release && "$2" == download ]]; then
  for ((i=1; i<=$#; i++)); do
    if [[ "${!i}" == --dir ]]; then next=$((i+1)); dest="${!next}"; fi
  done
  cp "$MOCK_ROOT/source"/Sudoku-*.apk "$MOCK_ROOT/source"/Sudoku-*.aab \
    "$MOCK_ROOT/source/SHA256SUMS" "$dest/"
  exit 0
fi
exit 1
MOCK
chmod +x "$tmp/bin/gh"
export PATH="$tmp/bin:$PATH" MOCK_ROOT="$tmp" ANDROID_HOME="$tmp/sdk"

verify() {
  bash "$repo_root/verify-release-assets.sh" v1.0.62 bren-wp/Sudoku false \
    04d9f7fffa8939f62213297eef609ebd5e098d8f
}
reject() {
  if verify >"$tmp/verify.log" 2>&1; then
    echo "FAIL: verification accepted $1" >&2
    exit 1
  fi
  echo "PASS: rejected $1"
}

verify >"$tmp/verify.log"
echo 'PASS: valid unsigned prerelease with matching SHA256SUMS'
printf 'tamper\n' >> "$tmp/source/Sudoku-v1.0.62-test.apk"
reject 'tampered APK'
# Restore fixture before classification checks.
(cd "$tmp/source" && zip -q Sudoku-v1.0.62-test.apk AndroidManifest.xml)
sed -i 's/"prerelease":true/"prerelease":false/' "$tmp/release.json"
reject 'unsigned stable release'
sed -i 's/"prerelease":false/"prerelease":true/' "$tmp/release.json"
sed -i 's/04d9f7fffa8939f62213297eef609ebd5e098d8f/1111111111111111111111111111111111111111/' "$tmp/release.json"
reject 'wrong source commit'
echo 'Release contract regression checks passed.'
