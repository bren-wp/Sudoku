#!/usr/bin/env bash
# Gate publication on BOTH trusted main-branch workflows at the SAME SHA.
# Usage: require-release-ci-gates.sh <owner/repo> <40-char-main-commit-sha>
set -euo pipefail

repo="${1:?Repository is required}"
sha="${2:?Main commit SHA is required}"
[[ "$repo" =~ ^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$ ]] || {
  echo "::error::Invalid repository" >&2; exit 1;
}
[[ "$sha" =~ ^[a-f0-9]{40}$ ]] || {
  echo "::error::Invalid commit SHA" >&2; exit 1;
}

runs="$(gh api "repos/$repo/actions/runs?head_sha=$sha&branch=main&per_page=100")"
latest_run() {
  jq -r --arg workflow "$1" --arg sha "$sha" '
    [.workflow_runs[]
     | select(.name == $workflow and .head_sha == $sha and .head_branch == "main")
     | select(.event == "push" or .event == "workflow_dispatch")]
    | sort_by(.created_at, .run_attempt) | last | if . == null then empty else @json end
  ' <<< "$runs"
}

android="$(latest_run "Android CI")"
visual="$(latest_run "Android visual smoke")"
for pair in "Android CI|$android" "Android visual smoke|$visual"; do
  name="${pair%%|*}"
  json="${pair#*|}"
  if [[ -z "$json" ]] || [[ "$(jq -r '.status' <<< "$json")" != completed ]] ||
     [[ "$(jq -r '.conclusion' <<< "$json")" != success ]]; then
    echo "::notice::$name is not completed successfully for $sha; publication deferred" >&2
    exit 1
  fi
done

visual_id="$(jq -r '.id' <<< "$visual")"
[[ "$visual_id" =~ ^[0-9]+$ ]] || { echo "::error::Invalid visual run ID" >&2; exit 1; }
jobs="$(gh api "repos/$repo/actions/runs/$visual_id/jobs?per_page=100")"
mapfile -t successful < <(
  jq -r '.jobs[] | select(.name | startswith("Android UI (")) |
    select(.status == "completed" and .conclusion == "success") | .name' <<< "$jobs" | sort
)
expected=(
  reference compact narrow landscape
  tablet-shard-0 tablet-shard-1 tablet-shard-2 tablet-shard-3
  tablet-capture tiny medium
)
mapfile -t required < <(printf 'Android UI (%s)\n' "${expected[@]}" | sort)
if [[ "${#successful[@]}" -ne 11 ]] ||
   [[ "$(printf '%s\n' "${successful[@]}")" != "$(printf '%s\n' "${required[@]}")" ]]; then
  echo "::error::The 11 named emulator jobs did not all succeed at $sha" >&2
  printf 'Successful: %s\n' "${successful[*]}" >&2
  exit 1
fi

printf 'Verified Android CI and all 11 emulator jobs at %s (runs %s and %s).\n' \
  "$sha" "$(jq -r '.id' <<< "$android")" "$visual_id"
