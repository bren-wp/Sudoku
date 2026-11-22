#!/usr/bin/env bash
# Offline signing-identity contract tests. No publisher keystore is needed.
set -euo pipefail
cd "$(dirname "$0")"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
mkdir -p "$tmp/bin" "$tmp/sdk/build-tools/36.0.0"
printf 'fixture' > "$tmp/test.apk"
printf 'fixture' > "$tmp/test.aab"
cat > "$tmp/sdk/build-tools/36.0.0/apksigner" <<'MOCK'
#!/usr/bin/env bash
echo "Signer #1 certificate SHA-256 digest: $MOCK_APK_SHA"
[[ "${MOCK_DUPLICATE:-false}" != true ]] || echo "Signer #2 certificate SHA-256 digest: $MOCK_APK_SHA"
MOCK
cat > "$tmp/bin/jarsigner" <<'MOCK'
#!/usr/bin/env bash
if [[ "${MOCK_UNSIGNED:-false}" == true ]]; then
  echo 'jar is unsigned.'
else
  echo 'jar verified.'
fi
MOCK
cat > "$tmp/bin/keytool" <<'MOCK'
#!/usr/bin/env bash
printf 'Signer #1:\nCertificate #1:\n SHA256: %s\n' "$MOCK_AAB_SHA"
MOCK
chmod +x "$tmp/sdk/build-tools/36.0.0/apksigner" "$tmp/bin/jarsigner" "$tmp/bin/keytool"
export ANDROID_HOME="$tmp/sdk" PATH="$tmp/bin:$PATH"
expected="$(printf 'ab%.0s' {1..32})"
wrong="$(printf 'cd%.0s' {1..32})"
export MOCK_APK_SHA="$expected" MOCK_AAB_SHA="$expected"
check() { bash verify-publisher-certificate.sh "$tmp/test.apk" "$tmp/test.aab" "$expected"; }
check >/dev/null
echo 'PASS: APK and AAB same pinned certificate'

reject() {
  if check >"$tmp/log" 2>&1; then
    echo "FAIL: accepted $1" >&2
    exit 1
  fi
  echo "PASS: rejected $1"
}
MOCK_APK_SHA="$wrong"; export MOCK_APK_SHA
reject 'wrong APK certificate'
MOCK_APK_SHA="$expected"; MOCK_AAB_SHA="$wrong"; export MOCK_APK_SHA MOCK_AAB_SHA
reject 'wrong AAB certificate'
MOCK_AAB_SHA="$expected"; MOCK_UNSIGNED=true; export MOCK_AAB_SHA MOCK_UNSIGNED
reject 'unsigned AAB'
unset MOCK_UNSIGNED
MOCK_DUPLICATE=true; export MOCK_DUPLICATE
reject 'multiple APK signers'
unset MOCK_DUPLICATE
if bash verify-publisher-certificate.sh "$tmp/test.apk" "$tmp/test.aab" invalid >"$tmp/log" 2>&1; then
  echo 'FAIL: accepted invalid pinned fingerprint' >&2
  exit 1
fi
echo 'PASS: rejected invalid fingerprint'
echo 'Publisher identity regression tests passed.'
