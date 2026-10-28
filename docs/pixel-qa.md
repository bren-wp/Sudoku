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
capture and use `--include-splash`. Since v1.0.34 the instrumented UI test
`captureRealSplashScreenForReferenceQA` records the actual Compose splash
after deterministically navigating back to that screen. This is not a painted
reference screenshot. Do not substitute the original reference as its own
actual screenshot.

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
results. ## v1.0.33 emulator capture and result (28 October 2026)

The candidate code at commit `19890afdf6a5d869718ee1d6e72c978c7ce385cc`
passed Android CI run `40774194155` (unit tests, pixel metric regression tests,
Android lint and APK/AAB build) and visual-smoke run `40774194240`.
Both reference-size 941×1672 and compact 720×1280 emulator jobs succeeded.

The original reference PNGs were compared with the **actual** 941×1672
visual-smoke screenshots from artifact `12853763946` and the previous
v1.0.32 screenshots from artifact `12624264725`.

| Screen | v1.0.32 raw RGB MAE | v1.0.33 raw RGB MAE | Change |
|---|---:|---:|---:|
| Home | 29.312 | 29.312 | 0.000 |
| New game | 29.061 | 29.061 | 0.000 |
| Game | 23.121 | 23.068 | -0.053 |
| Settings | 27.727 | 27.727 | 0.000 |
| Daily challenge | 29.682 | 29.686 | +0.004 |
| Statistics | 30.986 | 30.986 | 0.000 |
| Achievements | 32.184 | 32.184 | 0.000 |
| **Average** | **28.868** | **28.861** | **-0.007** |

The delta is too small to claim substantive pixel-parity improvement,
particularly because date, puzzle state and live statistics differ between
captures. This iteration primarily improves splash layout, session recovery
and verifiable QA metrics. A splash capture, icon/wordmark direct comparison,
and scenery-versus-interactive-UI normalized analysis are **still pending**.
The next visual corrections are tracked in issue #39; do not declare 1:1
equality, no known crashes, or a completed global security/dead-code audit.


## v1.0.34 — full-resolution screenshot provenance

GitHub Actions run [40778309153](https://github.com/bren-wp/Sudoku/actions/runs/40778309153)
completed successfully and pushed eight **real** WebP screenshot captures to
`docs/screenshots/` on the v1.0.34 PR branch. The automated exporter checks
that all eight Android PNG captures exist and have valid PNG signatures. The
marketing conversion checks 941×1672 resolution before encoding to WebP
at original dimensions.

The eighth file `08-splash.webp` comes from the real Compose splash, not
the original concept PNG. README links to these eight artifacts. This validates
provenance, resolution and capture success; **not** exact visual equality.

The reference 941×1672 pixel-difference measurement should now be repeated
against the v1.0.34 Android visual-smoke artifact after both emulator jobs
succeed. Screenshot content remains dynamic and not normalized. Global
RGB MAE must not be presented as pixel-perfect if differences remain.
