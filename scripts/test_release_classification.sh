#!/usr/bin/env bash
# Offline regression tests for GitHub release classification.
set -euo pipefail
cd "$(dirname "$0")"
script_dir="$PWD"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
mkdir -p "$tmp/bin"
export MOCK_ROOT="$tmp" PATH="$tmp/bin:$PATH"

cat > "$tmp/bin/gh" <<'MOCK'
#!/usr/bin/env bash
set -euo pipefail
if [[ "$1" == api && "${2:-}" == --paginate ]]; then
  if [[ "${MOCK_REVERT_AFTER_PATCH:-0}" == 1 && -f "$MOCK_ROOT/patched" ]]; then
    jq 'map(if .id == 1 then .prerelease = false else . end)' "$MOCK_ROOT/releases.json"
  else
    cat "$MOCK_ROOT/releases.json"
  fi
  exit 0
fi
if [[ "$1" == api && "${2:-}" == --method && "${3:-}" == PATCH ]]; then
  [[ "${MOCK_DENY_PATCH:-0}" != 1 ]] || exit 1
  id="${4##*/}"
  [[ "$id" =~ ^[0-9]+$ ]] || exit 1
  jq --argjson id "$id" 'map(if .id == $id then .prerelease = true else . end)' \
    "$MOCK_ROOT/releases.json" > "$MOCK_ROOT/new.json"
  mv "$MOCK_ROOT/new.json" "$MOCK_ROOT/releases.json"
  touch "$MOCK_ROOT/patched"
  exit 0
fi
exit 1
MOCK
chmod +x "$tmp/bin/gh"
reset_fixture() {
  rm -f "$tmp/patched"
  cat > "$tmp/releases.json" <<'JSON'
[
  {"id":1,"tag_name":"v1.0.64","draft":false,"prerelease":false,
   "assets":[{"name":"Sudoku-v1.0.64-unsigned.aab"}]},
  {"id":2,"tag_name":"v1.0.63","draft":false,"prerelease":false,
   "assets":[{"name":"Sudoku-v1.0.63.aab"}]},
  {"id":3,"tag_name":"v1.0.62","draft":false,"prerelease":true,
   "assets":[{"name":"Sudoku-v1.0.62-unsigned.aab"}]}
]
JSON
}
reset_fixture
bash "$script_dir/reconcile-release-classification.sh" bren-wp/Sudoku
jq -e '.[0].prerelease == true and .[1].prerelease == false and .[2].prerelease == true' \
  "$tmp/releases.json" >/dev/null
echo 'PASS: reclassified unsigned release without changing signed stable release'

reset_fixture
if MOCK_DENY_PATCH=1 bash "$script_dir/reconcile-release-classification.sh" bren-wp/Sudoku \
  > "$tmp/denied.log" 2>&1; then
  echo 'FAIL: an unsuccessful PATCH was accepted' >&2
  exit 1
fi
echo 'PASS: rejected failed PATCH'

reset_fixture
if MOCK_REVERT_AFTER_PATCH=1 bash "$script_dir/reconcile-release-classification.sh" bren-wp/Sudoku \
  > "$tmp/reverted.log" 2>&1; then
  echo 'FAIL: a later classification regression was accepted' >&2
  exit 1
fi
echo 'PASS: rejected metadata reverted before final verification'
