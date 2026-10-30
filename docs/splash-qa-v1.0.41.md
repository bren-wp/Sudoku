# Sudoku 1.0.41 — Splash screen pixel QA (screen 1 of 8)

## Source of truth
- User-provided original: `Slika ChatGPT 24. lis 2026. 20_47_14-3(9).png`
- Original dimensions: **941 × 1672 px**
- Baseline: actual `08-splash.png` from Android emulator workflow 40802093754 (API 35, 320 dpi)
- Baseline full-frame RGB mean absolute error (MAE), 0–255: **13.692** (measured 13.69156)
- Baseline regional RGB MAE: upper grid 10.588; logo 30.922; wordmark/caption 35.805; scenery 6.774; loading region 7.282.
- Dynamic compositor and anti-aliasing details should be controlled in tests before interpreting these figures as UI-only differences.

## Changes to the actual Compose UI
1. The 7–3–9 symbol stays anchored at roughly 25.5% of splash viewport height.
2. On full-size phones, the wordmark grows from 64sp to 79sp and the logo-to-wordmark spacing contracts from 30dp to 15dp.
3. The caption remains native text. The gap to the slogan contracts and the slogan grows from 11sp to 15sp.
4. Only the splash logo gains a cyan elevation glow. Home and all other logos retain previous sizing and effects.
5. The loading arc is now a deterministic native Compose Canvas drawing, including a subtle track. Android accessibility settings that disable animations no longer collapse the visual to a dot. It exposes a Croatian description.

## Exact verification gate
Run the Android visual-smoke workflow for both 941×1672 and 720×1280 viewports. Download real `sudoku-emulator-screen-references` screenshots and compare actual `08-splash.png` to the original, using `scripts/pixel_compare.py` with identical size and density.

Do not declare pixel-perfect 1:1 unless the **real** updated screenshot has been evaluated for:
- wordmark and icon bounding boxes and vertical alignment,
- actual accessible loading indicator visibility,
- typography/letter spacing, rendering, background and contrast,
- the scenery artwork at the lower edge (current compressed source asset is lower resolution than the original),
- safe layout on smaller screens and font-scale variations.

A global MAE can also include legitimate OS rendering and anti-aliasing differences; report raw scores without conflating them with exact layout parity.

## Build/toolchain migration
- Official AGP 9.5.0 requires Gradle 9.6.0 and JDK 17.
- AGP 9.x has built-in Kotlin. The old `org.jetbrains.kotlin.android` plugin must be removed; Compose compiler plugin remains.
- All 4 Gradle-using GitHub Actions workflows are pinned to 9.6.0. The repository does not include a Gradle wrapper, so local builds also require Gradle 9.6.0 unless a wrapper is explicitly added.
- `compileSdk=36`, `targetSdk=36`, `minSdk=26`; Google Play app/update API 36 requirement satisfied at the manifest level. Build completion and Play release signing must still be separately verified.

Official references:
- https://developer.android.com/build/releases/agp-9-5-0-release-notes
- https://developer.android.com/build/migrate-to-built-in-kotlin
- https://support.google.com/googleplay/android-developer/answer/11926878
- https://docs.gradle.org/9.6.0/release-notes.html

## Scope / known limitations
This PR intentionally targets **Splash first**, not a bulk replacement of all eight screens, and does not claim an all-screen 1:1 match or full production readiness. The next UI iteration should address Home using a separate measured baseline, while protecting gameplay and saved data.
