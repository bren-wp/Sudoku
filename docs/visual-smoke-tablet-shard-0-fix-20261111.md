# Sudoku — tablet-shard-0 post-merge visual smoke repair (2026-11-11)

## Failure reproduced from the actual GitHub Actions artifact

- Failed workflow: [Android visual smoke / 42929478593](https://github.com/bren-wp/Sudoku/actions/runs/42929478593).
- Failed job: Android UI (tablet-shard-0), job `130224297609`.
- The failure artifact `sudoku-test-failure-tablet-shard-0` contains 13 test cases, 1 failure, 0 errors.
- Failing test: `SudokuVisualSmokeTest.ownerThreeDimensionalBadgesFitAchievementsStatisticsAndDailyPanels`, introduced at commit `48713f52c308b82c59854e7073c1c5ded80aef1b`.
- Exact exception: `AssertionError: Action performScrollTo() failed` while looking up `TestTag = 'stats-award-preview-2'` in the **merged** semantics tree; the tag exists in the **unmerged** tree.
- The underlying Kotlin compilation, unit tests, pixel QA script tests, and main Android CI passed.

## Corrective change

Use `rule.onNodeWithTag("stats-achievements").performScrollTo().assertIsDisplayed()` to scroll the publicly accessible clickable parent panel. The subsequent `assertArtInside("stats-award-streak-3d-badge", "stats-award-preview-2")` still queries the **unmerged** tree and independently checks that the rendered owner-art badge stays within the real card bounds.

This fixes the mismatch of merged and unmerged Compose semantics. It does **not** skip or weaken the artwork check, disable CI, delete tests, or change player data, gameplay, visuals, offline behavior or dependencies.

## Release gate

Keep PR unmerged until new Android CI and all Android visual-smoke matrix jobs are green on the *new PR SHA*. The prior screenshots and historical MAE are not proof that these new outputs are pixel-perfect. There is no justification to issue a new APK/AAB solely from a failing post-merge visual test.

The suite's separate reference-image workflow remains necessary for normalized per-region MAE/SSIM comparisons to original owner PNGs. No 1:1 result is claimed here.
