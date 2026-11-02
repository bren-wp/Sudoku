#!/usr/bin/env bash
set -euo pipefail
# Focused size-boundary profiles exercise only the screens currently being
# polished. The existing five profiles continue to run every instrumentation
# test, including gameplay, statistics, settings and persistence.
gradle_args=()
export_args=()
skip_export=false
if [[ "${1:-}" == "--splash-home-only" ]]; then
  # Limit each constrained device to three deterministic real-screen checks.
  # The complete 41-test suite still runs across the four tablet shards.
  # A long comma-separated filter previously fell back to all 41 tests.
  gradle_args+=("-Pandroid.testInstrumentationRunnerArguments.class=com.brendigo.sudoku.SudokuVisualSmokeTest#captureRealSplashScreenForReferenceQA,com.brendigo.sudoku.SudokuVisualSmokeTest#homeMenuCardsAndWeekStripFitEveryEmulatorProfile,com.brendigo.sudoku.SudokuVisualSmokeTest#newGameActionsRemainLegibleOnEveryResponsiveProfile")
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
elif [[ "${1:-}" == "--reference-capture" ]]; then
  # The owner's Home reference has an unfinished game. Export that real
  # state separately instead of distorting the screenshot from a fresh app.
  # First collect the clean Home and other reference screens.
  # A later separate instrumentation invocation creates the real resume
  # state. The completed first capture remains immutable in MediaStore.
  gradle_args+=("-Pandroid.testInstrumentationRunnerArguments.class=com.brendigo.sudoku.SudokuVisualSmokeTest#captureRealSplashScreenForReferenceQA,com.brendigo.sudoku.SudokuVisualSmokeTest#screenNavigationAndGameplayControls,com.brendigo.sudoku.SudokuVisualSmokeTest#captureActualComposeWordmarkForTenReferenceQa,com.brendigo.sudoku.SudokuVisualSmokeTest#captureNativeLauncherForTenReferenceQa")
  export_args+=("--reference-capture")
elif [[ "${1:-}" == "--tablet-capture" ]]; then
  # Bounded real-Android capture prevents exhausting emulator graphics buffers.
  # The full suite is still covered by the independent tablet shards.
  gradle_args+=("-Pandroid.testInstrumentationRunnerArguments.class=com.brendigo.sudoku.SudokuVisualSmokeTest#captureRealSplashScreenForReferenceQA,com.brendigo.sudoku.SudokuVisualSmokeTest#screenNavigationAndGameplayControls,com.brendigo.sudoku.SudokuVisualSmokeTest#captureActualComposeWordmarkForTenReferenceQa,com.brendigo.sudoku.SudokuVisualSmokeTest#captureNativeLauncherForTenReferenceQa")
fi
# Only retry known transient Maven Central rate limits. A failing assertion,
# APK installation error, or genuine compilation failure remains a hard failure.
for attempt in 1 2 3; do
  echo "Android visual test attempt: $attempt"
  if gradle --no-daemon :app:connectedDebugAndroidTest "${gradle_args[@]}" 2>&1 | tee /tmp/sudoku-visual-gradle.log; then
    if [[ "${1:-}" == "--reference-capture" ]]; then
      # Keep the clean Home capture intact: it is already complete and
      # stored in MediaStore before the separate resume-state invocation.
      # Do not run 'adb shell pm clear' here: on Android 15 it may return
      # "Failed" even after successful instrumentation and abort this lane.
      # Isolation is by test ORDER and a separate instrumentation invocation:
      # only the later test creates/captures 01-home-resume.png, never 01-home.
      echo "Capturing the real resume-state Home in separate instrumentation"
      # A different instrumentation filter is a distinct Gradle task input.
      # Do not retry assertions, mask exit codes, or fabricate game state.
      gradle --no-daemon :app:connectedDebugAndroidTest \
        "-Pandroid.testInstrumentationRunnerArguments.class=com.brendigo.sudoku.SudokuVisualSmokeTest#captureRealHomeWithResumeCardForReferenceQA" \
        2>&1 | tee /tmp/sudoku-resume-gradle.log
    fi
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
