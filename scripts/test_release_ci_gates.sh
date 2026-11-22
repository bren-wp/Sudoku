#!/usr/bin/env bash
# Offline regression tests for exact-SHA release quality gates.
set -euo pipefail
cd "$(dirname "$0")"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
sha="$(printf 'a%.0s' {1..40})"
mkdir -p "$tmp/bin"
cat > "$tmp/bin/gh" <<'MOCK'
#!/usr/bin/env bash
set -euo pipefail
[[ "$1" == api ]] || exit 2
case "$2" in
  *'/actions/runs?'*)
    android_status=success
    visual_status=success
    visual_name="Android visual smoke"
    visual_sha="$MOCK_SHA"
    if [[ "$MOCK_CASE" == android-failed ]]; then android_status=failure; fi
    if [[ "$MOCK_CASE" == visual-pending ]]; then visual_status=null; fi
    if [[ "$MOCK_CASE" == missing-visual ]]; then visual_name=other; fi
    if [[ "$MOCK_CASE" == wrong-sha ]]; then visual_sha="$(printf 'b%.0s' {1..40})"; fi
    cat <<JSON
{"workflow_runs":[
 {"id":101,"name":"Android CI","event":"push","head_sha":"$MOCK_SHA","head_branch":"main","created_at":"2026-11-22T12:00:00Z","run_attempt":1,"status":"completed","conclusion":"$android_status"},
 {"id":202,"name":"$visual_name","event":"push","head_sha":"$visual_sha","head_branch":"main","created_at":"2026-11-22T12:01:00Z","run_attempt":1,"status":"completed","conclusion":$(if [[ "$visual_status" == null ]]; then echo null; else printf '"%s"' "$visual_status"; fi)}
 $(if [[ "$MOCK_CASE" == latest-failed ]]; then printf ',{"id":303,"name":"Android CI","event":"push","head_sha":"%s","head_branch":"main","created_at":"2026-11-22T13:00:00Z","run_attempt":1,"status":"completed","conclusion":"failure"}' "$MOCK_SHA"; fi)
]}
JSON
    ;;
  *'/jobs?'*)
    layouts=(reference compact narrow landscape tablet-shard-0 tablet-shard-1 tablet-shard-2 tablet-shard-3 tablet-capture tiny medium)
    for layout in "${layouts[@]}"; do
      [[ "$MOCK_CASE" == missing-job && "$layout" == medium ]] && continue
      printf 'Android UI (%s)\n' "$layout"
    done | jq -R --arg scenario "$MOCK_CASE" '
      {name:.,status:"completed",
       conclusion: (if $scenario == "failed-job" and . == "Android UI (medium)"
                    then "failure" else "success" end)}' | jq -s '{jobs:.}'
    ;;
  *) echo "Unknown mock gh endpoint: $2" >&2; exit 2 ;;
esac
MOCK
chmod +x "$tmp/bin/gh"
export PATH="$tmp/bin:$PATH" MOCK_SHA="$sha"

check() { bash require-release-ci-gates.sh bren-wp/Sudoku "$sha"; }
MOCK_CASE=green; export MOCK_CASE
check >/dev/null
echo 'PASS: both checks + exact 11 successful jobs'

for scenario in android-failed visual-pending missing-visual wrong-sha latest-failed missing-job failed-job; do
  MOCK_CASE="$scenario"; export MOCK_CASE
  if check >"$tmp/log" 2>&1; then
    echo "FAIL: accepted $scenario" >&2
    exit 1
  fi
  echo "PASS: rejected $scenario"
done
echo 'Release gate regression tests passed.'
