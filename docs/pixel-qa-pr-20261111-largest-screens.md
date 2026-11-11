# Sudoku — 2026-11-11: current owner-reference pixel QA and targeted UI iteration

## Source provenance

Owner's ten original PNG files were recovered from the user's earlier uploaded project references ("Slika ChatGPT 24. lis 2026. 20_47_13-1" through "20_47_20-10", archived in the user's file library). Baseline screenshots came from actual Android UI reference capture (workflow [42929478593](https://github.com/bren-wp/Sudoku/actions/runs/42929478593), commit `48713f52`). Its only failing matrix job was subsequently fixed in merged PR #67; the UI bytes are unchanged by that test-only fix.

**Measurement:** raw RGB MAE, same native resolutions, no resizing. First eight images 941×1672; logo and launcher 1254×1254. This is a visual difference score 0–255, NOT a percentage of deviation.

| Owner reference | Baseline screenshot | RGB MAE |
|---|---|---:|
| 01-home | 01-home-resume | 23.9463 |
| 02-new-game | 02-new-game | 22.6101 |
| 03-game | 03-game | 20.8222 |
| 04-settings | 04-settings | 19.4215 |
| 05-daily | 05-daily | 24.8353 |
| 06-statistics | 06-statistics | 26.6392 |
| 07-achievements | 07-achievements | 29.3214 |
| 08-splash | 08-splash | 10.3842 |

The fresh-home screenshot (not comparable to the reference *resume* state) was 32.1495. Launcher and wordmark use different RGB+alpha validation and are not mixed into the screen ranking.

## Concrete visual defects visible in side-by-side inspection

- **Achievements (highest MAE)**: the top four live, text-only featured cards show only one truncated description line while the owner's art has two; excessive bottom padding and insufficient minimum description height leave room below the progress label. Preserve original artwork layers and real player stats, adjust inside-card vertical allocation only.
- **Statistics (second highest MAE)**: four previews at the bottom are unnecessarily tall relative to the owner's art, reducing visible scenery. Reduce their minimum height and internal padding and icon geometry, preserving 4 across at the reference width, 2×2 at narrower widths and 24 real achievements.
- **Daily (third)**: major differences depend on genuine date, streak, actual results; no state fabrication just to match the screenshot. Artist-supplied hero visual is still distinct and requires a separate measured revision.

## Verification and merge gate

- The source of truth for gameplay, dates, totals, checkmarks, completed challenges and earned badges stays in `SudokuController`.
- Android CI, all 11 actual Android emulators and the added bounds checks must succeed before merge.
- The full first-eight-screen raw MAE must be recomputed using a **new screenshot artifact from the PR head**, before anyone can claim improvement or 1:1.
- Visual QA should be done on identical seeded user states; the above comparison includes dynamic differences.
- No screenshots or reference artwork are committed into the runnable UI. No image generation or fake Compose screen has been used.

## PR #68 follow-up: Daily hero, Home resume and New Game — 2026-11-11

Reference-based side-by-side inspection identified a vertical seam where the daily hero image begins at 60% of the card width. The reference shows mountain shapes extending farther into the center behind real live text. This iteration adds *native, still mountain ridges* and a multi-stop scrim in `DailyHeroBackdrop`; the bundled moon/pagoda artwork stays at the original focal coordinates. The daily screen keeps its live date, streak, badge/points, weekly progress, CTA and offline leaderboard.

Home: the resume card's caption returns to the reference's muted tone and the real action is proportionally wider. New Game: the six selection badges receive a slightly brighter embossed rim/elevation without changing their clickable dimensions or engine state.

Added a 11-profile Android instrumentation test verifying that the daily native scenery is bounded by the live hero card, the metrics exist and the leaderboard dialog still opens.

**This is a candidate change, not a measured improvement yet.** Original references and previous real screenshots are valid baseline only. After the new PR commit finishes the reference emulator run, remeasure region MAE and revert any worsening. Do not claim 1:1/0% deviation until same-state comparison proves it.
