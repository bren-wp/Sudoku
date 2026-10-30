#!/usr/bin/env python3
"""Measure the REAL Settings Compose screenshot against 941x1672 owner reference.

The source PNG must stay unchanged; do not paste reference imagery into the
functional UI, resize the emulator output, or hide difficult regions with masks.
Dynamic settings states and the true BuildConfig version are not fabricated.

python scripts/settings_pixel_qa.py --reference references/04-settings.png \
    --actual screenshots/04-settings.png --output-dir qa-pixel-diff/settings \
    --allow-imperfect
"""
from __future__ import annotations

import argparse
from pathlib import Path

from home_pixel_qa import measure_regions

# Fixed 941x1672 source-image coordinates (exclusive right/bottom edges).
REGIONS = {
    "whole": (0, 0, 941, 1672),
    "header": (0, 0, 941, 206),
    "appearance": (40, 205, 893, 535),
    "game": (40, 545, 893, 988),
    "sound": (40, 995, 893, 1120),
    "language": (40, 1130, 893, 1258),
    "about": (40, 1265, 893, 1520),
    "bottom_scenery": (0, 1518, 941, 1672),
}


def main() -> int:
    parser = argparse.ArgumentParser(description="Real Sudoku Settings pixel QA")
    parser.add_argument("--reference", type=Path, required=True)
    parser.add_argument("--actual", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path,
                        default=Path("qa-pixel-diff/settings"))
    parser.add_argument("--allow-imperfect", action="store_true")
    args = parser.parse_args()
    rows = measure_regions(args.reference, args.actual, args.output_dir, REGIONS)
    for row in rows:
        print(f'{row["region"]}: {row["rgb_mae_0_to_255"]:.3f}/255; '
              f'within ±12 RGB = {row["rgb_pixels_within_12_pct"]:.2f}%')
    exact = all(row["rgb_mae_0_to_255"] == 0 for row in rows)
    print("Real Settings pixel-for-pixel match:", exact)
    return 0 if exact or args.allow_imperfect else 2


if __name__ == "__main__":
    raise SystemExit(main())
