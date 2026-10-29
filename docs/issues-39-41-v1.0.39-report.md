# Sudoku 1.0.39 — ten-reference visual QA: verified results

This report compares the **owner's original PNGs** against **real Android
emulator screenshots** from workflow
[40797325078](https://github.com/bren-wp/Sudoku/actions/runs/40797325078),
artifact `sudoku-emulator-screen-references` (`12865140208`).

The emulator ran API 35 at 941×1672, 320dpi. The two original branding
references are square 1254×1254; their actual images were captured by
new instrumented Android tests (real Compose header and installed APK's
adaptive launcher resource). The Compose wordmark is **cropped and centered**
into a 1254px black comparison canvas, and does not claim to be a standalone
transparent wordmark export. Differences in this geometric transformation
contribute to its reported error. A native launcher render is subject to
Android adaptive-mask semantics; it cannot be identified with a flat
source icon bitmap.

## Full-image RGB MAE, 0–255 (lower is better)

| Original reference | 1.0.38 | Actual 1.0.39 | Change |
|---|---:|---:|---:|
| 01 Home | 26.381 | 26.381 | +0.000 |
| 02 New game | 26.552 | 26.552 | +0.000 |
| 03 Gameplay | 21.608 | 21.695 | +0.087 |
| 04 Settings | 24.171 | 24.174 | +0.003 |
| 05 Daily | 26.936 | 26.936 | +0.000 |
| 06 Statistics | 28.813 | 28.813 | +0.000 |
| 07 Achievements | 30.528 | 30.528 | +0.000 |
| 08 Splash | 14.223 | 13.692 | −0.531 |
| 09 Wordmark | Not captured | 17.571 | Baseline |
| 10 Adaptive launcher icon | Not captured | 68.415 | Baseline |

The two very small regressions on screens 03/04 are linked to
dynamic game values and rendering and must **not** be called an
improvement; screen 08 measured lower error. These ten numbers are
raw, **not normalized** and **not proof of pixel parity**. In particular
the launcher remains notably different from the owner's icon (flat
vector illustration versus the original detailed glossy icon). The
artwork/typography/layout should be brought closer **without** ever
replacing functional Compose screens with screenshots.

## Issue #39 acceptance status

- [x] All ten original PNG references can now be measured using
  separately captured Android/Compose/native branding outputs.
- [x] Real branded Android resource checks and real UI screenshot tests
  at both resolutions.
- [x] Optional dynamic-region masks, separate scenery/UI metrics,
  and raw-MAE regression checking have automated tests.
- [ ] Deterministic, reversible test-only stats/game fixtures across
  every reference (never change user saves).
- [ ] Icon source fidelity / wordmark typography, 1:1 card geometry,
  visual effects and responsive layout all matched with normalised
  reference screenshots.
- [ ] Full ten-reference **zero** pixel MAE (or explicit approved tolerance)
  independently verified. **Keep issue #39 OPEN.**

## Issue #41 verification

Android CI `40797325001` completed successfully. Visual-smoke
`40797325078` completed with both the 941×1672 and 720×1280
emulators green, including the new
`SudokuBackupSafetyRegressionTest` and the existing
`SudokuBackupTest`. The app is offline and requires explicit
confirmation before replacing local data; review and encryption remain
implemented in native Android. This does **not** guarantee recovery of
a forgotten password, missing backup file, or failed storage hardware.
