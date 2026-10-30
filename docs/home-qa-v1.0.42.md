# Sudoku v1.0.42 — Home pixel QA (screen 2 of 8)

## Ground truth
Owner's 941×1672 reference image: `Slika ChatGPT 24. lis 2026. 20_47_15-4(9).png`.
Actual baseline: real Android emulator screenshot `01-home.png` from Sudoku v1.0.41 visual-smoke run 40977519924 (API 35, 320 dpi).

Raw per-channel RGB MAE baseline (0–255, lower is better):

| Region | v1.0.41 baseline |
|---|---:|
| Whole screenshot | 26.381 |
| Header (0–360px) | 21.482 |
| Resume hero (35,363–896,751) | 31.880 |
| Primary cards (37,766–900,1063) | 33.008 |
| Compact cards (38,1073–898,1265) | 29.319 |
| Weekly streak (38,1275–897,1437) | 32.335 |
| Bottom scenery (0,1450–941,1672) | 19.034 |

All numbers refer to the original baseline, **not** new screenshots. A new report must state new results from real emulator captures; never infer success from build-only CI.

## Implemented scope
1. Brand wordmark size/placement and slogan letter spacing on Home.
2. Constrain high-contrast Sudoku grid decoration to left/right upper corners *only on Home*. Splash, Game and other screens preserve their background paths.
3. Resume card padding, real button size, textual hierarchy and angled decorative Sudoku board. Decorative digits never replace the real puzzle state.
4. More faithful cyan and moonlit amber illustrated cards, drawn with scalable native Compose Canvas layers underneath working clickable surfaces. No screenshot of an interactive screen is used as a UI overlay.
5. Compact card icon positions/type sizes. Correct gear, calendar-star, simple plus and filled warm flame shapes.
6. Keep real player statistics, saved game state, elapsed time and streak; the reference's illustrative '7 days' and '12:34' are not copied into the actual app.

## Acceptance criteria
- Android CI: Kotlin unit tests, Android Lint, 9.6.0 Gradle build of APK and AAB.
- Android connected tests at 941×1672 reference and 720×1280 compact viewports, including primary-card equal-height and nonoverlap assertions.
- Capture the exact latest release candidate, compare real `01-home.png` against the provided owner PNG, and publish both global and region-specific RGB MAE values. Dynamic player data makes raw MAE a diagnostic, not a proof of layout parity.
- Inspect the screenshot for clipping, text wraps, touch targets, 2+3 row order, day markers and scenery contrast.
- No change to local puzzle data, package ID, privacy permissions or Google Play signing keys.
- Do not advertise `1:1 pixel-perfect` until image diff and actual devices meet that claim.

This iteration is Home only. Next targeted screen will be New Game, not bulk changes to all screens.
