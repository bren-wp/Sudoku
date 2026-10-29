# Sudoku v1.0.40 — session durability review

## Scope
Targeted reliability pass on existing Kotlin/Compose code. The package name,
local preference keys, puzzle format and signing configuration are unchanged.
No user data migration, analytics, advertising SDK, network permissions or
new production dependency has been introduced.

## Observed defects
1. Starting another puzzle performed `saveGame(synchronous = true)` but
   ignored a false disk commit. The old in-memory board was then replaced,
   risking loss of its latest moves when device storage is unavailable.
2. Switching from an active classic game to a saved daily game, or from an
   active daily game back to classic, also ignored checkpoint failure.
3. Recovery always preferred the shared primary snapshot, even when it
   described a completed/failed puzzle and a named unfinished puzzle was
   available for continuation.

## Fixes
- `checkpointBeforeSwitch()` now blocks replacement on a failed commit or
  during a backup restore. An actionable Croatian message is displayed.
- The recovery selector preserves a live primary game and matching Undo.
  Otherwise it prefers an unfinished regular game, then today's unfinished
  daily game, each with its own Undo key. If none is resumable, a valid last
  primary result remains visible for backwards compatibility.
- Added JUnit regression cases for completed/expired primary and an Android
  instrumentation fault-injection test that rejects actual commit requests.
  The test uses a dedicated app-private preferences file, not user progress.

## Verification and release gates
- Run `:app:testDebugUnitTest`, `:app:lintDebug`,
  `:app:assembleDebug`, `:app:assembleRelease`, `:app:bundleRelease`.
- Run connected Android tests on both 941×1672 and 720×1280 emulator viewports,
  including `SudokuSessionSwitchSafetyTest`.
- Re-run ten-reference screenshot comparison from real app captures. The
  previously measured v1.0.39 per-screen RGB MAE values remain nonzero.
  This patch does **not** claim pixel-perfect parity or improve the art.
- Verify APK and AAB assets exist. A Play-ready release additionally requires
  the owner's publisher upload key configured in GitHub Actions. Never label
  debug-signed APK and unsigned AAB as Play-ready.

## Remaining risks
- `SharedPreferences.apply()` still writes normal moves asynchronously.
  Activity background `flush()` commits synchronously, but power loss and
  failed physical storage cannot be made impossible by app logic.
- No device testing has been performed locally for this report: GitHub Actions
  results on this branch must be inspected before merge.
- Full security, dead-code and visual 1:1 audit are separate outstanding
  acceptance criteria. Existing privacy policy and screenshots were left
  unchanged because this patch does not alter data collection or UI.
