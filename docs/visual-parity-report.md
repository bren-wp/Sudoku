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

## Follow-up v1.0.10
Visual improvements now include native layered night scenery and more faithful
streak/reward icons on the seven screens. The Android 13+ monochrome launcher icon
had a real defect: an opaque black fill was treated as opaque ALPHA by the
themed launcher, causing the grid to disappear. Rebuilt it as transparent
vector paths and added a rendered-bitmap instrumentation regression check.
Achievement cards now expose actual detail dialogs, and the game lives use
filled/outline vector hearts. **A new normalized RGB MAE=0 result has not been
established**; reference backgrounds are visually richer than the Compose
illustration. Keep #11 open.

## v1.0.25 — compact Statistics cards

The metric tiles now switch to smaller decorations and readable two-line labels
when constrained below 180dp in width. The existing 941×1672 and 720×1280
emulator smoke tests check card separation and the Statistics dialog interaction.
The seven original owner PNG reference files are not present in this repository;
therefore this change does **not** constitute measured MAE/SSIM improvement or
proof of 1:1 matching. Capture and compare normalized screenshots before making
pixel-perfect claims, keeping issue #11 open.
