#!/usr/bin/env bash
# Verify that a GitHub release actually contains the expected Android binaries.
# Usage: verify-release-assets.sh <vX.Y.Z> <owner/repo> <auto|true|false> [source-commit-sha]
set -euo pipefail

tag="${1:?Missing release tag}"
repo="${2:?Missing repository}"
expected_signed="${3:-auto}"
expected_sha="${4:-}"

[[ "$tag" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]] || { echo 'Invalid release tag' >&2; exit 1; }
[[ "$repo" =~ ^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$ ]] || { echo 'Invalid repository' >&2; exit 1; }
[[ "$expected_signed" == auto || "$expected_signed" == true || "$expected_signed" == false ]] || {
  echo 'Invalid signing expectation' >&2; exit 1;
}
[[ -z "$expected_sha" || "$expected_sha" =~ ^[a-f0-9]{40}$ ]] || {
  echo 'Invalid source commit SHA' >&2; exit 1;
}

release="$(gh api "repos/$repo/releases/tags/$tag")"
actual_sha="$(jq -r '.target_commitish' <<< "$release")"
prerelease="$(jq -r '.prerelease' <<< "$release")"
mapfile -t assets < <(jq -r '.assets[].name' <<< "$release")
contains_asset() { printf '%s\n' "${assets[@]}" | grep -Fxq -- "$1"; }

signed_apk="Sudoku-$tag.apk"
signed_aab="Sudoku-$tag.aab"
test_apk="Sudoku-$tag-test.apk"
unsigned_aab="Sudoku-$tag-unsigned.aab"
if contains_asset "$test_apk" && contains_asset "$unsigned_aab" &&
   ! contains_asset "$signed_apk" && ! contains_asset "$signed_aab"; then
  actual_signed=false
  apk="$test_apk"
  aab="$unsigned_aab"
elif contains_asset "$signed_apk" && contains_asset "$signed_aab" &&
     ! contains_asset "$test_apk" && ! contains_asset "$unsigned_aab"; then
  actual_signed=true
  apk="$signed_apk"
  aab="$signed_aab"
else
  echo "::error::Incomplete or mixed APK/AAB assets in $tag" >&2
  exit 1
fi

contains_asset SHA256SUMS || { echo '::error::Missing SHA256SUMS asset' >&2; exit 1; }
if [[ "$expected_signed" != auto && "$expected_signed" != "$actual_signed" ]]; then
  echo '::error::Release signing classification contradicts the build configuration' >&2
  exit 1
fi
if [[ -n "$expected_sha" && "$actual_sha" != "$expected_sha" ]]; then
  echo "::error::Release targets $actual_sha instead of $expected_sha" >&2
  exit 1
fi
if [[ "$actual_signed" == false && "$prerelease" != true ]]; then
  echo '::error::Unsigned AAB must be published as prerelease' >&2
  exit 1
fi

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
for attempt in 1 2 3 4; do
  if gh release download "$tag" --repo "$repo" --dir "$tmp" --clobber \
       --pattern "$apk" --pattern "$aab" --pattern SHA256SUMS; then
    break
  fi
  if [[ "$attempt" == 4 ]]; then
    echo '::error::Could not download published release assets' >&2
    exit 1
  fi
  sleep 5
done

for file in "$apk" "$aab" SHA256SUMS; do
  test -s "$tmp/$file" || { echo "::error::Missing or empty downloaded asset: $file" >&2; exit 1; }
done
# Prevent a manifest that validates only a subset of the published binaries.
diff -u <(printf '%s\n' "$apk" "$aab" | sort) \
  <(awk '{print $2}' "$tmp/SHA256SUMS" | sort)
(cd "$tmp" && sha256sum -c SHA256SUMS)
unzip -tq "$tmp/$aab" >/dev/null

apksigner="$ANDROID_HOME/build-tools/36.0.0/apksigner"
test -x "$apksigner" || { echo '::error::Android SDK 36 apksigner is missing' >&2; exit 1; }
"$apksigner" verify --verbose --print-certs "$tmp/$apk"

jarsigner -verify "$tmp/$aab" > "$tmp/aab-signature.txt" 2>&1
if [[ "$actual_signed" == true ]]; then
  grep -Fq 'jar verified.' "$tmp/aab-signature.txt" || {
    echo '::error::Publisher-signed AAB failed signature verification' >&2; exit 1;
  }
else
  grep -Fq 'jar is unsigned.' "$tmp/aab-signature.txt" || {
    echo '::error::Test release AAB unexpectedly appears to be signed' >&2; exit 1;
  }
fi
printf 'Verified release %s: %s APK, %s AAB, SHA256SUMS and metadata (target %s).\n' \
  "$tag" "$([[ "$actual_signed" == true ]] && echo publisher-signed || echo test-signed)" \
  "$([[ "$actual_signed" == true ]] && echo signed || echo unsigned)" "$actual_sha"
