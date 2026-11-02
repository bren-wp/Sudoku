#!/usr/bin/env bash
set -euo pipefail
# Focused size-boundary profiles exercise only the screens currently being
# polished. The existing five profiles continue to run every instrumentation
# test, including gameplay, statistics, settings and persistence.
gradle_args=()
export_args=()
skip_export=false
if [[ "${1:-}" == "--splash-home-only" ]]; then
  gradle_args+=("-Pandroid.testInstrumentationRunnerArguments.class=com.brendigo.sudoku.SudokuVisualSmokeTest#captureRealSplashScreenForReferenceQA,com.brendigo.sudoku.SudokuVisualSmokeTest#splashBrandDoesNotOverlapLoadingIndicatorAtLargeFontScale,com.brendigo.sudoku.SudokuVisualSmokeTest#homeMenuCardsAndWeekStripFitEveryEmulatorProfile,com.brendigo.sudoku.SudokuVisualSmokeTest#newGameActionsRemainLegibleOnEveryResponsiveProfile,com.brendigo.sudoku.SudokuVisualSmokeTest#newGameDoesNotClipAtLargeAccessibilityFontScale")
  export_args+=("--splash-home-only")
elif [[ "${1:-}" == "--tablet-shard" ]]; then
  # AndroidJUnitRunner partitions every instrumentation test exactly once
  # among the specified indices. Each GitHub job starts a fresh emulator.
  index="${2:?Missing Android test shard index}"
  total="${3:?Missing Android test shard count}"
  [[ "$index" =~ ^[0-9]+$ && "$total" =~ ^[1-9][0-9]*$ ]] ||
    { echo "::error::Invalid test shard index/count"; exit 1; }
  (( index < total )) || { echo "::error::Shard index out of range"; exit 1; }
  gradle_args+=("-Pandroid.testInstrumentationRunnerArguments.numShards=$total")
  gradle_args+=("-Pandroid.testInstrumentationRunnerArguments.shardIndex=$index")
  skip_export=true
elif [[ "${1:-}" == "--tablet-capture" ]]; then
  # Short isolated capture run produces all eleven screenshot names with
  # real Android content, without recycling graphics buffers for 41 tests.
  gradle_args+=("-Pandroid.testInstrumentationRunnerArguments.class=com.brendigo.sudoku.SudokuVisualSmokeTest#captureRealSplashScreenForReferenceQA,com.brendigo.sudoku.SudokuVisualSmokeTest#screenNavigationAndGameplayControls,com.brendigo.sudoku.SudokuVisualSmokeTest#captureActualComposeWordmarkForTenReferenceQa,com.brendigo.sudoku.SudokuVisualSmokeTest#captureNativeLauncherForTenReferenceQa")
fi
# Only retry known transient Maven Central rate limits. A failing assertion,
# APK installation error, or genuine compilation failure remains a hard failure.
for attempt in 1 2 3; do
  echo "Android visual test attempt: $attempt"
  if gradle --no-daemon :app:connectedDebugAndroidTest "${gradle_args[@]}" 2>&1 | tee /tmp/sudoku-visual-gradle.log; then
    if [[ "$skip_export" == false ]]; then
      bash .github/scripts/export-emulator-screenshots.sh "${export_args[@]}"
    fi
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
