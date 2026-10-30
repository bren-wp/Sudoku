# Sudoku v1.0.46 — Statistics pixel QA, screen 6/8

## Reference and real baseline
- Owner's original: `Slika ChatGPT 24. lis 2026. 20_47_19-8(9).png` (941 × 1672).
- Last real emulator screenshot: `06-statistics.png`, v1.0.45, Android UI smoke run 40991382480 (941 × 1672).
- RGB mean absolute error / MAE (0–255; lower means closer), **before changes**:

| Region (pixel coordinates) | v1.0.45 baseline |
|---|---:|
| Entire screenshot | **29.193** |
| Header y0–185 | 27.224 |
| Four metric cards x30–909/y185–550 | 35.384 |
| Chart row x30–909/y550–1030 | 31.718 |
| Recent games x30–909/y1040–1320 | 24.138 |
| Achievement preview x30–909/y1320–1550 | 32.953 |
| Footer y1550–1672 | 25.716 |

The reference is a **conceptual populated state** (e.g. 124 played, 92% finished).
A real user with eight played and zero completed games must see their actual data.
Never hardcode 124, 92%, 45/38/28/13 or fictional game history to match a JPEG.

## Changes
- Statistics-only decorative background: sparse number/grid watermark in the top corners instead of lines crossing the actual report cards.
- Four metric cards use distinct teal/violet/blue/amber gradients, a relevant vector icon, full text labels and an accessible click surface with existing metric details.
- Chart and completion ring are a matched, shorter two-column pair at reference size. Narrow viewport/font-scale screens retain a stacked layout.
- Native grid lines and true four aggregated difficulty groups: `0+1`, `2`, `3`, `4+5`; preserve counts and zero bars. Do not invent sample values.
- Completion ring represents `finished / played` and two actual counts. Uses brush arc only for nonzero progress.
- Recent-game rows have separate dark borders and category/time/date values. If history is empty, show a truthful empty state rather than invented sample rows.
- Four award previews preserve earned/locked state and true progress. Full achievements remain a click-through.
- Layout instrumentation verifies a horizontally aligned chart pair (reference) or stacked pair (compact) with no overlap, while preserving previous interactions and accessibility.
- AGP 9.5.0, Gradle 9.6.0, JDK 17, Android compile/target API 36, minSdk 26, applicationId `com.brendigo.sudoku` unchanged.
- Version 1.0.46, versionCode 47; no database migration, added permissions or new networking dependencies.

## Acceptance gates
1. JUnit, pixel-QA scripts, Android Lint, debug and release APK plus AAB.
2. Real screenshot `06-statistics.png` at 941 × 1672 with global/ROI differences against exact owner original; report regressions honestly.
3. Emulator compact 720 × 1280: labels, scroll, ring, chart, history and awards without offscreen content overlap.
4. Verify statistic detail dialogs, local history and achievements navigation, as well as zero-data states.
5. Do not describe an unsigned AAB as ready for Play. Publisher key needed to sign if unavailable.

Passing CI is not proof of 1:1 pixel correspondence. Remaining dynamic data and high-detail background differences must be reported.
