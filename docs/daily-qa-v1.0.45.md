# Sudoku v1.0.45 — Daily Challenge (screen 5/8)

## Exact source
- User-owned reference: `Slika ChatGPT 24. lis 2026. 20_47_18-7(9).png` (941×1672).
- Original real Android emulator screenshot: `05-daily.png` from v1.0.44 workflow [40987260085](https://github.com/bren-wp/Sudoku/actions/runs/40987260085) (941×1672).
- RGB mean absolute difference (0–255 per channel), before this PR:

| Measured area, px bounds | v1.0.44 baseline RGB MAE |
| --- | ---: |
| Full screen | **27.514** |
| Header 0..273 | 22.533 |
| Daily summary x30..900, y270..475 | 34.857 |
| Hero x30..900, y480..900 | 36.755 |
| Scenic hero-right x480..890, y480..747 | 35.741 |
| Three metrics x57..891, y745..872 | 39.260 |
| Week timeline x30..895, y900..1185 | 24.667 |
| Launch CTA x110..820, y1185..1315 | 24.217 |
| Local leaderboard x180..755, y1310..1425 | 36.123 |
| Footer landscape y1440..1672 | 22.608 |

These are baseline values, **not** claimed final measurements.

## Pixel work done
1. A Daily-only backdrop keeps Sudoku-decoration in the top corners instead of running a giant graph behind interactive cards.
2. The header title and subtitle are resized only on Daily, leaving other already-refined screens intact.
3. Daily summary uses a three-part wide layout with current date, **real** streak count, full-size flaming icon and a warm ring around the gift. Narrow devices keep a stacked accessible layout.
4. Hero now uses an owner's original cropped art **only for decorative moon, mountains and pavilion**. This new local 337×261 WebP contains NO text, buttons, metrics or dates; those remain true Compose elements and follow dynamic state. No downloaded asset or runtime networking.
5. The hero's three real metrics use dark glass, readable type and increased vector icon sizes; compact 2+1 layout remains.
6. Weekly streak uses real Monday–Sunday dates and actual completion status; glow/filled markers never spoof check marks.
7. Real gradient launch button uses a native filled-play icon and preserves controller callbacks for new/resumed daily games. Secondary action opens **offline personal** leaderboard; it is not falsely presented as global results.
8. The reference's example 28 April 2025 and seven-day streak are illustrative. App must always display the actual device date, solved history and current streak, even if this raises raw pixel MAE.

## Required QA
- Android Gradle Plugin 9.5.0 / Gradle 9.6.0 / JDK17, targetSdk+compileSdk 36, minSdk 26.
- Unchanged applicationId `com.brendigo.sudoku`, no new permissions/trackers/dependencies, no resets of stored games.
- Kotlin unit tests, Python pixel QA tests, Android Lint, debug/release APK, unsigned AAB.
- Android instrumented tests at 941×1672 and 720×1280 for the flow, landscape, metrics order, weekly dates, local leaderboard and interaction.
- Compare the new, real `05-daily.png` with the user reference and publish global + regional RGB differences; report regressions.
- If signing secrets aren't configured, release test APK and unsigned AAB only. Do not describe either as production-signed.
- 1:1 can only be asserted after visual and geometry verification of the real app, not from a green build alone.

Remaining limitations: full-frame pixel MAE is affected by the current date, saved puzzle state and streak, while the global bottom-scenery asset is still less detailed than the source reference. These require separate evaluation and artwork QA.
