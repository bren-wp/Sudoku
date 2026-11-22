#!/usr/bin/env bash
# Ensure the publisher-signed APK and AAB use the SAME explicitly pinned
# upload certificate. The expected fingerprint comes from trusted release
# environment configuration (never from an untrusted PR or build output).
# Usage: verify-publisher-certificate.sh APK AAB expected-SHA256
set -euo pipefail
apk="${1:?APK path required}"
aab="${2:?AAB path required}"
expected="${3:?Pinned publisher certificate SHA-256 is required}"
expected="$(printf '%s' "$expected" | tr -d ':[:space:]' | tr 'A-F' 'a-f')"
[[ "$expected" =~ ^[0-9a-f]{64}$ ]] || {
  echo "::error::Invalid pinned certificate SHA-256 fingerprint" >&2; exit 1;
}
[[ -s "$apk" && -s "$aab" ]] || {
  echo "::error::Missing APK or AAB" >&2; exit 1;
}
apksigner="$ANDROID_HOME/build-tools/36.0.0/apksigner"
[[ -x "$apksigner" ]] || { echo "::error::Android 36 apksigner unavailable" >&2; exit 1; }

apk_cert="$("$apksigner" verify --print-certs "$apk")"
apk_count="$(grep -Ec '^Signer #[0-9]+ certificate SHA-256 digest:' <<< "$apk_cert" || true)"
[[ "$apk_count" == 1 ]] || { echo "::error::Expected exactly one APK signer" >&2; exit 1; }
apk_sha="$(sed -nE 's/^Signer #[0-9]+ certificate SHA-256 digest:[[:space:]]*([0-9a-fA-F:]+)[[:space:]]*$/\1/p' <<< "$apk_cert" | tr -d ':' | tr 'A-F' 'a-f')"

jar_result="$(jarsigner -verify "$aab" 2>&1)" || {
  echo "::error::AAB signature verification failed" >&2; exit 1;
}
grep -Fq 'jar verified.' <<< "$jar_result" || {
  echo "::error::AAB is unsigned or unverified" >&2; exit 1;
}
aab_cert="$(keytool -printcert -jarfile "$aab")"
aab_count="$(grep -Ec '^Signer #[0-9]+:' <<< "$aab_cert" || true)"
[[ "$aab_count" == 1 ]] || { echo "::error::Expected exactly one AAB signer" >&2; exit 1; }
aab_sha="$(sed -nE 's/^[[:space:]]*SHA256:[[:space:]]*([0-9a-fA-F:]+)[[:space:]]*$/\1/p' <<< "$aab_cert" | head -n 1 | tr -d ':' | tr 'A-F' 'a-f')"

if [[ "$apk_sha" != "$expected" || "$aab_sha" != "$expected" ]]; then
  echo "::error::APK/AAB publisher certificate does not match the pinned upload identity" >&2
  exit 1
fi
echo "APK and AAB match the pinned publisher certificate SHA-256: $expected"
