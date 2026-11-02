#!/usr/bin/env bash
set -euo pipefail
# Focused size-boundary profiles exercise only the screens currently being
# polished. The existing five profiles continue to run every instrumentation
# test, including gameplay, statistics, settings and persistence.
gradle_args=()
export_args=()
if [[ "${1:-}" == "--splash-home-only" ]]; then
  gradle_args+=("-Pandroid.testInstrumentationRunnerArguments.class=com.brendigo.sudoku.SudokuVisualSmokeTest#captureRealSplashScreenForReferenceQA,com.brendigo.sudoku.SudokuVisualSmokeTest#splashBrandDoesNotOverlapLoadingIndicatorAtLargeFontScale,com.brendigo.sudoku.SudokuVisualSmokeTest#homeMenuCardsAndWeekStripFitEveryEmulatorProfile")
  export_args+=("--splash-home-only")
fi
# Only retry known transient Maven Central rate limits. A failing assertion,
# APK installation error, or genuine compilation failure remains a hard failure.
for attempt in 1 2 3; do
  echo "Android visual test attempt: $attempt"
  if gradle --no-daemon :app:connectedDebugAndroidTest "${gradle_args[@]}" 2>&1 | tee /tmp/sudoku-visual-gradle.log; then
    bash .github/scripts/export-emulator-screenshots.sh "${export_args[@]}"
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
