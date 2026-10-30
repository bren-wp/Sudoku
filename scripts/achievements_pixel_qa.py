#!/usr/bin/env python3
"""Measure the REAL 941x1672 Sudoku Achievements screen against the owner PNG.

The reference depicts illustrative award progress; live points, unlocks,
dates and saved player data must never be rewritten for visual comparison.
Regions are fixed in owner's original 941x1672 coordinate system.
Do not resize compact 720x1280 screenshots to artificially improve MAE.

python scripts/achievements_pixel_qa.py \
    --reference references/07-achievements.png \
    --actual screenshots/07-achievements.png \
    --output-dir qa-pixel-diff/achievements --allow-imperfect
"""
from __future__ import annotations

import argparse
from pathlib import Path
from home_pixel_qa import measure_regions

REGIONS = {
    "whole": (0, 0, 941, 1672),
    "header": (0, 0, 941, 160),
    "summary": (38, 160, 894, 319),
    "first_award_row": (36, 333, 895, 642),
    "second_award_row": (36, 650, 895, 956),
    "third_award_row": (36, 974, 895, 1274),
    "fourth_award_row": (36, 1285, 895, 1485),
    "bottom_scenery": (0, 1485, 941, 1672),
}


def main() -> int:
    parser = argparse.ArgumentParser(description="Native Sudoku achievements ROI QA")
    parser.add_argument("--reference", type=Path, required=True)
    parser.add_argument("--actual", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path,
                        default=Path("qa-pixel-diff/achievements"))
    parser.add_argument("--allow-imperfect", action="store_true")
    args = parser.parse_args()
    rows = measure_regions(args.reference, args.actual, args.output_dir, REGIONS)
    for row in rows:
        print(f'{row["region"]}: {row["rgb_mae_0_to_255"]:.3f}/255, '
              f'within ±12 RGB = {row["rgb_pixels_within_12_pct"]:.2f}%')
    exact = all(row["rgb_mae_0_to_255"] == 0 for row in rows)
    print("Real achievements screenshot exactly matches reference:", exact)
    return 0 if exact or args.allow_imperfect else 2


if __name__ == "__main__":
    raise SystemExit(main())
