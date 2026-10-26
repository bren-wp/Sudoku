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

## v1.0.26 — Achievements responsive layout

The existing 941×1672 reference-width layout retains its two-column featured
award grid and established 151dp/137dp card hierarchy at the default font scale.
At smaller widths award cards can grow in height, and narrow layouts or enlarged
system font scales use a single column. Tests check text containment and access
to all 24 real awards on emulator viewports. This improves adaptive behavior,
not measured pixel equality. Original owner PNG references are still required
to calculate new MAE/SSIM and verify the illustrated background and every icon.
Issue #11 remains open.

## v1.0.27 — Home responsive navigation

At the reference 941×1672 viewport, Home retains the original 2+3 navigation
hierarchy and the single-row weekly streak presentation. At compact widths,
navigation flows to 2+1 compact cards (or single-column primary tiles on very
narrow devices), and the seven streak day markers occupy a dedicated row.
Instrumented smoke tests assert the original reference-width ordering, compact
card separation and streak marker containment. The graphics remain native
Compose components, not flattened screenshots. The owner's original PNG
references and normalized QA fixture remain unavailable in this repository;
MAE/SSIM improvement and exact 1:1 parity are **not yet measured** (#11).

## v1.0.28 — New Game responsive configuration

The normal/reference-width screen retains its original 192dp 9×9/6×6/4×4
selector, six full-width difficulty panels, vector icons and 75%-width start
button. At very narrow widths, the selector contracts to stay alongside the
48dp Back button, and large-font/compact difficulties use slightly taller
panels with more text room. Selections and the New Game destination survive
Activity recreation while the configuration is open. UI smoke tests cover the
selector boundaries, all six difficulty options and an actual generated 6×6
board after rotation. Full RGB MAE/SSIM comparison against owner PNG source
files remains unavailable; issue #11 stays open.

## v1.0.29 — Gameplay board and controls

The default/reference-size game keeps existing board, vector-number keypad,
undo/notes/hint/erase panel dimensions and night background. At very narrow
widths or large Android font scaling, the timer/completion/mistake HUD stacks,
headings scale and numeric key text shrinks to fit available key width. Pausing
now conceals puzzle digits and notes; resuming returns the same state. UI smoke
tests cover state, accessible controls and the concealed board on both emulator
sizes. Original reference PNGs remain unavailable; exact visual matching and
MAE/SSIM zero are unverified, tracked in issue #11.

## v1.0.30 — Daily challenge adaptive layout

On the 941×1672 reference viewport the daily summary and reward metrics keep
the established three-column hierarchy. On compact screens the date becomes a
full-width summary row and the three metrics use a 2+1 arrangement. The local
calendar retains seven real weekday markers and merges legacy `lastDaily` with
completed-date history for display. The action now distinguishes between
starting and resuming the same unfinished daily puzzle. Instrumentation covers
both sizes, marker reachability and the stateful challenge resume path across
Android activity recreation. The owner's original design PNG references are
still missing, so an exact pixel-level match / new MAE/SSIM measurements are
not claimed; #11 remains open.

## v1.0.31 — Settings screen stability and accessibility

The reference layout retains existing section panels, icons, custom brand
switches, language information, privacy copy and footer. Small layouts and
larger Android fonts can wrap setting titles and captions without colliding
with the 55dp switch. The Sound, Language and Privacy dialogs use Compose
saveable state, and the Settings origin survives Activity recreation so Back
returns to the running Sudoku board. UI instrumentation verifies all seven
persisted settings as well as activity recreation from in-game Settings on
941×1672 and 720×1280 emulators. Original owner PNGs are still absent from
this repository; strict MAE/SSIM and 1:1 pixel parity remain unverified (#11).
