# Sudoku by Brendigo — pixel-parity pass 2026-11-03 (Splash → Home)

## Owner-supplied reference material

The ten original source files were attached in the conversation (not committed as flattened UI screens). Dimensions: eight **941 × 1672 RGBA PNG** screen references and two **1254 × 1254 RGBA PNG** branding assets. The Android visual workflow uses **941 × 1672, 320 dpi**, so nominal dp coordinates are half the pixel coordinates.

Screen order: 08 Splash → 01 Home → 02 New Game → 03 Gameplay → 05 Daily → 06 Statistics → 07 Achievements → 04 Settings. Wordmark and launcher art are inspected independently. All screens must remain genuine interactive Kotlin/Compose UI.

## First code pass in this branch

- **Splash, native 7–3–9 logo:** add thin, clipped specular top/bottom edges to the illuminated central tile, behind the existing true numeral and hit-free accessibility semantics.
- **Splash, native loader:** shift the arc down **6 dp** on reference-size portrait displays and introduce a soft cyan outer bloom. Keep the existing fixed, high-contrast stroke readable when Android animations are disabled.
- **Home and shared primary buttons:** add bounded cyan/blue shadow elevation to actual Compose buttons; preserve gradient, labels, hit targets, icon semantics and callbacks.
- **Home primary tile icons:** add a modest neon ambient elevation to the existing circular 3D icon wells; keep the cards fully clickable.

## Geometry targets visible in the original Splash screenshot

These regions are **approximate inspection guides**, not machine-measured equality claims:

| Native region (941 × 1672 px) | Original artwork, approximate |
| --- | --- |
| 7–3–9 symbol | x 340–600; y 425–690 |
| Large Sudoku wordmark | x 195–755; y 725–870 |
| “by Brendigo” byline | centered; y 867–913 |
| Tagline | centered; y 945–995 |
| Loading arc | near x 440–510; y 1450–1535 |
| Decorative night landscape | lower ~42% of the screen |

Reference elements are **not** the game's live content: daily dates, streaks, elapsed times, earned points, notes, and Sudoku values must always come from controller state, not copied from the picture.

## Validation / acceptance

1. Run unit tests, Kotlin/Android lint, debug and release APK/AAB builds.
2. Run the visual smoke profiles: reference, compact, narrow, medium, tiny, landscape and tablet shards.
3. Export **real** PNG captures and use `scripts/final_ten_reference_qa.py` with the owner's exact PNGs. Numeric RGB MAE, per-region MAE, and alpha mismatches must be reported, not hidden.
4. On the actual home resume screen, generate a **real saved game** first, then capture; do not fake resume text or board state to match a screenshot.
5. Check all buttons and animations in motion and with increased font scale, TalkBack and minimal display sizes.
6. Preserve offline operation, game saves, backup format and signing configuration.

**Status:** native code changes are committed for Splash and Home. Pixel-exact 1:1 is **not certified** until the new emulated captures have been compared to the user originals and all CI checks pass. This is an incremental screen-by-screen iteration, not a final visual sign-off.
