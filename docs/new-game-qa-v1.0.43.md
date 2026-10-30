# Sudoku 1.0.43 — New Game pixel QA (screen 3 of 8)

## Source of truth
Original user reference: `Slika ChatGPT 24. lis 2026. 20_47_17-5(9).png`, resolution 941×1672.
Actual v1.0.42 baseline: `02-new-game.png`, Android API 35, 320 dpi, 941×1672, exported in visual-smoke workflow 40981237796 (not concept art).

Baseline mean absolute RGB error (0–255; lower is better):
- Full screenshot: **26.552**
- Header: **22.735**
- Six cards (x38–902, y366–1332): **28.437**
- CTA region (x100–845, y1330–1505): **39.425**
- Bottom scenery (y1490–1672): **25.590**

All are baseline measurements, **not** measurements for this PR. Repeat after emulator captures become available; do not call a nonzero error `pixel-perfect`.

## Implemented changes
1. Native **segmented** 9×9/6×6/4×4 radio selector, keeping existing saved-state and test tags.
2. New Game-specific upper-right decorative Sudoku grid in the background, with cyan focus and numbered cells; other screens retain their backdrops.
3. Six native Compose clickable difficulty cards with distinct colors, icon rings and decorative angled Sudoku grids. Card text, icon, selection and navigation are independent of decorative Canvas pixels.
4. Native blue vertical-gradient primary action with filled play symbol.
5. Revised Croatian typography and spacing, with accessibility fallback when the display or font scale is narrow/large.
6. New Game-specific footer removal to reflect reference's uninterrupted landscape.

## Required verification gates
- `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug`, `:app:assembleRelease`, `:app:bundleRelease` with AGP 9.5 and Gradle 9.6.
- Real device/emulator: 941×1672 and 720×1280, Android API 35; test size choices, difficulty choices, rotation state recovery, and working 4×4, 6×6, 9×9 games.
- A new **real** `02-new-game.png` screenshot measured against the provided reference at the identical resolution, including ROI diagnostics.
- Re-check body rendering, font clipping, back target, accessibility semantics, contrast, and all six cards. Pixel-perfect 1:1 is only appropriate if measured and visually verified.
- Keep targetSdk/compileSdk 36, minSdk 26, Android package identity and signing configuration unchanged. Google Play upload requires real publishing key; test APK and unsigned AAB must be labeled accordingly.

No image generation, advertising SDK or network dependency introduced.
