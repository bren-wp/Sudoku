# Pixel parity quality assurance

The target illustrations supplied by the project owner are `941×1672` pixels.
These are **visual reference screenshots**, not live Android UI or a source of
fake clickable backgrounds. The application must render real Compose components.

## Reference mapping

Copy the seven portrait screenshots into a local `references/` directory using:

| Filename | Supplied screenshot |
|---|---|
| 01-home.png | Main Sudoku menu |
| 02-new-game.png | Size selector and difficulty options |
| 03-game.png | Active 9×9 game |
| 04-settings.png | Settings |
| 05-daily.png | Daily challenge |
| 06-statistics.png | Statistics |
| 07-achievements.png | Achievements |

The splash, full logo and adaptive icon are evaluated separately. Do not commit
personally-identifying system screenshot overlays or test-account information.

### Original design images received 26 October 2026

The actual source PNG images are now available in the user-provided chat
attachment set. The originals have been packaged **without changing any pixels**
under these normalized filenames:

- `references/01-home.png` — 941×1672
- `references/02-new-game.png` — 941×1672
- `references/03-game.png` — 941×1672
- `references/04-settings.png` — 941×1672
- `references/05-daily.png` — 941×1672
- `references/06-statistics.png` — 941×1672
- `references/07-achievements.png` — 941×1672
- `references/08-splash.png` — 941×1672
- `references/09-wordmark.png` — 1254×1254
- `references/10-launcher-icon.png` — 1254×1254

The binary PNG assets **are not committed in the repository**; keep the
original reference ZIP and its SHA-256 manifest as the source of truth for the
next audit. When comparing with emulator screenshots use only the first seven
reference names. Do not screenshot-fake the app with whole-screen bitmaps;
all buttons and state remain real Compose code.

## Capture and comparison

1. Run the `Android visual smoke` GitHub workflow; it sets the emulator to
   **941×1672** and uploads seven `sudoku-emulator-screen-references` PNG files.
2. Download that artifact and unzip the PNGs into `screenshots/`.
3. Install Pillow using `python -m pip install Pillow`.
4. Run `python scripts/pixel_compare.py --reference-dir references --actual-dir screenshots --output-dir qa-pixel-diff`.
5. Inspect the overlay and difference PNGs for all seven screens and the
   `report.csv`. The command exits nonzero if any color-channel pixel differs.

Caveats: system status/navigation bars, font rasterization, locale, elapsed game
time, random puzzles, real user statistics and Android density can create
differences even when component geometry matches. These must be normalized
before any claim of 1:1 pixel parity. This repository does **not** yet certify a
zero-error result; green functional CI tests are not proof of pixel equality.

Avoid storing sensitive production information or signing keys alongside QA images.

## Compact-screen regression gate

The `Android visual smoke` workflow also runs the same functional Compose
navigation and control tests at `720×1280` pixels, density 320dpi.
The compact screenshots are uploaded as `sudoku-emulator-compact-screenshots`.
This is a **layout and accessibility regression**, not an input to the
941×1672 pixel comparison. Use the `sudoku-emulator-screen-references`
artifact for the original-size comparison only.

Real 1:1 validation requires normalized screen content, date, puzzle seed,
statistics and font environment. Screenshot fixtures must never alter
production gameplay or masquerade as working interface components.

## v1.0.33 candidate: separate UI and scenery diagnostics

The script now treats an RGB pixel as within tolerance **only if all three
channels** are within 12 or 24 levels of the reference; a grayscale-luminance
difference no longer hides large single-channel changes.

For a component-versus-background diagnosis, provide an optional directory
of 941×1672 grayscale binary PNG masks matching the seven screen names:
**white (255) selects live UI, black (0) selects background/scenery**.
The script reports `ui_region_mae` and `scenery_region_mae` alongside the
unmasked global MAE, while maintaining the global error as the definitive
metric. The masks are analysis annotations only, never rendered in the app.

```bash
python scripts/pixel_compare.py \
  --reference-dir references \
  --actual-dir screenshots \
  --ui-mask-dir qa-ui-masks \
  --output-dir qa-pixel-diff
```

To include the splash screen, supply a **real** `08-splash.png` Android
capture and use `--include-splash`. The seven-screen emulator artifact does
not currently contain a launch-splash capture. Do not substitute the original
reference as its own actual screenshot.

### v1.0.32 verified baseline from the successful emulator artifact

On 28 October 2026 the original user-provided references were compared
against the actual 941×1672 screenshots from successful visual-smoke
run `40261033976`, artifact `12624264725`:

| Screen | Actual v1.0.32 RGB MAE (0–255) |
|---|---:|
| Home | 29.312 |
| New game | 29.061 |
| Game | 23.121 |
| Settings | 27.727 |
| Daily | 29.682 |
| Statistics | 30.986 |
| Achievements | 32.184 |

These are raw full-screen metrics, including differing live data, layout,
typography and procedural scenery; they are not normalized component-parity
results. The splash layout and optional-note recovery changes in the
v1.0.33 candidate are **not yet verified** by a new emulator screenshot
comparison. Do not report the values above as post-change improvements.
