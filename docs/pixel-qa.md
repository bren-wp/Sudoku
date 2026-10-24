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
