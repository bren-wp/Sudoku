# Sudoku pixel-parity verification

## Reference and method

Owner-supplied screen references: 941 × 1672 PNG, RGB. Baseline actuals:
Android emulator screenshots from Android visual smoke workflow, version v1.0.6.
For each screen, the reported mean absolute error (MAE) is the arithmetic mean
of all RGB absolute differences over the full 941×1672 screenshot, on a 0–255 scale.

| Screen | Baseline v1.0.6 MAE |
| --- | ---: |
| Home | 37.89 |
| New game | 45.32 |
| Game | 26.58 |
| Settings | 33.51 |
| Daily challenge | 46.40 |
| Statistics | 34.77 |
| Achievements | 39.27 |

A true pixel-perfect match means **MAE = 0 on every screen**. None of these
baseline screens met that standard. These errors include legitimate dynamic
differences: randomized grids, elapsed game timer, user's actual achievements,
current date, system settings and font rasterization. They must be made
deterministic in a debug-only screenshot fixture before a strict pixel-level
pass/fail claim can be made. Never replace gameplay with flattened screenshots.

## v1.0.7 focused corrections

1. Eight featured achievements appear first in four two-column rows;
   remaining 16 stay navigable via an explicit action.
2. Statistics uses compact side-by-side summary cards, bar/donut charts,
   recent activity and a real achievement preview link.
3. Daily challenge uses the smaller card proportions and three reward details
   from the visual reference without hiding interactive controls.
4. Emulator adds regression coverage for last visible achievement card and
   showing additional achievements.

## Limits

The visual QA tooling in scripts/pixel_compare.py produces difference overlays
and a CSV report. Actual zero-difference verification against normalized v1.0.7
images has not been completed; do not label it "1:1" on the store, in the
README, or in any release notes until all individual normalized captures pass.
