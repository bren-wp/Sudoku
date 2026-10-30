# Sudoku v1.0.44 — gameplay pixel QA (screen 4/8)

## Reference and actual baseline
Owner's original `Slika ChatGPT 24. lis 2026. 20_47_17-6(9).png`
and the *real* Android screenshot `03-game.png` from v1.0.43 visual smoke
run #40983921076, 941×1672 px.

Baseline mean absolute RGB difference (0–255), **not post-change results**:

| Region | Baseline MAE |
| --- | ---: |
| Whole screen | 22.054 |
| Header 0–150 px | 24.202 |
| HUD 140–250 px | 24.829 |
| Board x40–890 y250–1110 px | 20.241 |
| Toolbar x38–899 y1100–1250 px | 25.788 |
| Keypad x40–900 y1240–1485 px | 26.833 |
| Night scenery y1490–1672 px | 22.398 |

The real puzzle values, elapsed time, notes state, selected cell, remaining
hints, and mistakes differ from the reference and must not be faked.
A raw full-frame pixel score therefore diagnoses visual differences but cannot
prove or refute **layout** parity by itself.

## Implemented
- Gameplay-only quiet upper background: remove large decorative non-game Sudoku
  numbers/grids crossing the header and HUD. The actual playable board remains
  fully functional. Adjust the crop/fade of existing local scenery to the lower
  fifth of the game screen. Splash, Home and other pages retain their styling.
- Reference-like dark button surfaces and 1dp blue outlines on Back / Pause /
  Settings. These retain their content descriptions, click callbacks and
  touch areas.
- Slightly tighten header/board vertical spacing; tune board navy fill and
  cyan outer border, increase 9×9 digit legibility and soften 3×3 region rules.
  Fixed/editable number colors, validation, highlights and notes remain dynamic.
- Gameplay-only action panels with independent neon gradient, larger vector
  icons and legible Croatian labels. Paused/completed and disabled states
  keep existing controller behavior.
- Add a shared, framed two-row numeric keypad with equal-width keys, centered
  final four digits and a non-ornamental dark blue fill. No screenshot overlays.
- Add Android instrumentation layout regression asserting that nine keys
  remain inside their frame and below the grid on actual emulators.

## Build/platform invariants
- `applicationId=com.brendigo.sudoku`; `versionCode=45`, `versionName=1.0.44`.
- AGP 9.5.0, Gradle 9.6.0 in existing workflows, JDK 17, compileSdk /
  targetSdk 36 and minSdk 26 remain.
- No new dependencies, permissions, trackers, network access or user-data
  migration.
- Publisher signing key is not part of the repository. Label release APK as
  test-only and AAB unsigned unless issuer configuration genuinely signs them.

## Acceptance
- Android CI unit tests, Pixel QA scripts, lint, APK and AAB builds.
- Android UI instrumented tests at 941×1672 and 720×1280, including normal
  number entry, notes, hint, undo, erase, pause, save/resume and settings access.
- Capture a real new `03-game.png` screenshot, compare with reference by
  region, and report regressions. The owner requests exact 1:1: never claim
  that until proven by real pixel and geometry comparison.

**Remaining limitation:** static screenshot captures differ in the generated
puzzle and running timer. The lower scenery asset is lower-fidelity than the
owner's original illustration; a true 1:1 graphical reconstruction requires
closer artwork, not merely a passing CI run.
