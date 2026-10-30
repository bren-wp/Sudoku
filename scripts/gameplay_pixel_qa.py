#!/usr/bin/env python3
"""Native Sudoku Gameplay pixel QA at the owner's fixed 941x1672 resolution.

Compare real emulator capture against the untouched owner's original. Puzzle
digits, timer, selection, notes, hearts and hints are dynamic, so raw RGB MAE
is an honest diagnostic, not a claim that different user data should match.
Never composite a static reference screenshot into the Android game.

Usage:
python scripts/gameplay_pixel_qa.py --reference references/03-game.png \
    --actual screenshots/03-game.png --allow-imperfect
"""
from __future__ import annotations

import argparse
from pathlib import Path

from home_pixel_qa import measure_regions

# Documented in docs/gameplay-qa-v1.0.44.md; exclusive right/bottom edges.
REGIONS = {
    "whole": (0, 0, 941, 1672),
    "header": (0, 0, 941, 150),
    "hud": (0, 140, 941, 250),
    "board": (40, 250, 890, 1110),
    "board_top": (40, 250, 890, 680),
    "board_bottom": (40, 680, 890, 1110),
    "tools": (38, 1100, 899, 1250),
    "keypad": (40, 1240, 900, 1485),
    "scenery": (0, 1490, 941, 1672),
}


def main() -> int:
    parser = argparse.ArgumentParser(description="Sudoku Gameplay real-screen region QA")
    parser.add_argument("--reference", required=True, type=Path)
    parser.add_argument("--actual", required=True, type=Path)
    parser.add_argument("--output-dir", default=Path("qa-pixel-diff/gameplay"), type=Path)
    parser.add_argument("--allow-imperfect", action="store_true",
                        help="Return success for a valid measured imperfect audit")
    args = parser.parse_args()
    rows = measure_regions(args.reference, args.actual, args.output_dir, REGIONS)
    for row in rows:
        print(f'{row["region"]}: {row["rgb_mae_0_to_255"]:.3f} / 255, '
              f'within ±12 RGB = {row["rgb_pixels_within_12_pct"]:.2f}%')
    exact = all(row["rgb_mae_0_to_255"] == 0 for row in rows)
    print("Entire real Gameplay screenshot exactly 1:1:", exact)
    return 0 if exact or args.allow_imperfect else 2


if __name__ == "__main__":
    raise SystemExit(main())
