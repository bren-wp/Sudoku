#!/usr/bin/env bash
set -euo pipefail
# Only retry known transient Maven Central rate limits. A failing assertion,
# APK installation error, or genuine compilation failure remains a hard failure.
for attempt in 1 2 3; do
  echo "Android visual test attempt: $attempt"
  if gradle --no-daemon :app:connectedDebugAndroidTest 2>&1 | tee /tmp/sudoku-visual-gradle.log; then
    bash .github/scripts/export-emulator-screenshots.sh
    exit 0
  fi
  if ! grep -Eiq '429|Too Many Requests|maven central.*temporarily|Read timed out|Connection reset'       /tmp/sudoku-visual-gradle.log; then
    echo "::error::A real build or test failure occurred; not retrying."
    exit 1
  fi
  if [ "$attempt" -eq 3 ]; then
    echo "::error::External dependency repository is still unavailable after retries."
    exit 1
  fi
  sleep $((attempt * 20))
done
