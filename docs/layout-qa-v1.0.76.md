# Sudoku v1.0.76 — P0 responsive layout follow-up

**Status:** development candidate on a pull request. Not released; no claim of 1:1 parity or passing emulator CI before results are available.

## Starting point
- `main` at the start of this iteration: `e411ebe3335d234bcb8081250c4ff12a148d40f9`.
- Last published version: [v1.0.75](https://github.com/bren-wp/Sudoku/releases/tag/v1.0.75), **test prerelease**, APK debug-signed and AAB unsigned.
- Owner's 10 original 941×1672/1254×1254 references remain private. Initial measured strict matches: **0/10**. Baseline per-screen RGB MAE: Home 23.9038; New Game 22.6224; Gameplay 19.5190; Settings 19.4223; Daily 24.6827; Statistics 26.3001; Achievements 29.5142; Splash 10.3709; Wordmark 18.8339; Launcher 16.1747. **These are baseline measurements, not measurements of this candidate.**

## Concrete changes
- `AchievementsPage.kt`: eliminate fixed 2-line description truncation, weight-constrained description sizing and hard card height; cards use a minimum height and intrinsic text sizing. Keep all 24 real achievement data/progress values; show earned counts without clamping the printed number to the target, while the progress indicator still caps its visual fill at 100%.
- `StatsPage.kt`: move 4 difficulty-chart labels into a separately measured row beneath the bars. Provide more space to the native chart and completion panel for enlarged accessibility fonts.
- `SudokuLayoutRegressionTest.kt`: instrumented bounds checks for the description/progress relationship of **all 24** cards, all four chart labels, and the fixed footer. Automatically exercised in the existing full-suite tablet shards.
- `compare_visual_iteration.py`: replace Pillow's deprecated `Image.getdata()` with exact, channel-aligned pixel counting from raw RGB and RGBA bytes; add warning-as-error regression test.
- Development candidate `versionName=1.0.76`, `versionCode=77`.

## Required gates, NOT YET VERIFIED
- Android CI: Kotlin/JUnit, lint, release build, R8 execution, CycloneDX and live OSV vulnerability scan.
- Android visual smoke: all 11 `reference`, `compact`, `narrow`, `medium`, `tiny`, `landscape`, `tablet-capture`, `tablet-shard-0/1/2/3`.
- Actual 941×1672 native screenshot captures from this candidate, separate fixture captures and exact original SHA-protected before/after RGB/RGBA region-by-region comparisons. Dynamic user data cannot be faked or substituted with PNG backgrounds.
- Functional checks for session transition, persistence, backup, restore, rotation and enlarged fonts. Do not treat code review as a substitute for on-device tests.

## Release policy
No `main` merge or release until the required tests and visual QA have been evaluated. A GitHub prerelease may use a clearly labelled debug-signed APK and unsigned AAB if publisher credentials are absent; neither may be represented as a Play production update. Do not leak the original reference PNGs or signing material.
