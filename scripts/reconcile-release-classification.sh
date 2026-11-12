#!/usr/bin/env bash
# Reconcile all published GitHub releases: an unsigned AAB must never be stable.
set -euo pipefail
repo="${1:?Usage: reconcile-release-classification.sh owner/repo}"
[[ "$repo" =~ ^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$ ]] || {
  echo 'Invalid repository name' >&2
  exit 1
}

list_releases() {
  gh api --paginate "repos/$repo/releases?per_page=100" | jq -sc 'add // []'
}

# GitHub release events can occur before assets are uploaded; inspect the
# authoritative asset list, not the tag label or an earlier event payload.
releases="$(list_releases)"
incorrect="$(jq -r '
  .[] | select(.draft == false and .prerelease == false and
    (.assets | any(.[]?; ((.name // "") | endswith("-unsigned.aab"))))) |
  [.id, .tag_name] | @tsv
' <<< "$releases")"

if [[ -n "$incorrect" ]]; then
  while IFS=$'\t' read -r release_id tag; do
    [[ "$release_id" =~ ^[0-9]+$ ]] || { echo 'Invalid release id' >&2; exit 1; }
    echo "Reclassifying $tag as prerelease (unsigned AAB)."
    gh api --method PATCH "repos/$repo/releases/$release_id" \
      -F prerelease=true --silent
  done <<< "$incorrect"
fi

# Read again after mutation. A successful HTTP PATCH by itself does not prove
# that the published metadata is now correct.
remaining="$(list_releases | jq -r '[.[] | select(.draft == false and .prerelease == false and
  (.assets | any(.[]?; ((.name // "") | endswith("-unsigned.aab")))))] | length')"
if (( remaining != 0 )); then
  echo "::error::$remaining unsigned releases still labelled stable" >&2
  exit 1
fi
echo 'All published unsigned-AAB releases are classified as prerelease.'
