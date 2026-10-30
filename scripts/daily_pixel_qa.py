#!/usr/bin/env python3
"""Measure real Sudoku Daily Challenge screenshots at the owner's 941x1672 size.

The source artwork is reference-only. Never resize a 720x1280 emulator image,
paste static reference graphics over live controls, or fake user-specific
dates, streaks or scores to reduce MAE.

python scripts/daily_pixel_qa.py --reference references/05-daily.png \
    --actual screenshots/05-daily.png --output-dir qa-pixel-diff/daily \
    --allow-imperfect
"""
from __future__ import annotations

import argparse
from pathlib import Path
from home_pixel_qa import measure_regions

# Source image bounds, right/bottom exclusive. Fixed before checking current
# changes; do not move them to make a new release appear more visually exact.
REGIONS = {
    "whole": (0, 0, 941, 1672),
    "header": (0, 0, 941, 273),
    "daily_summary": (30, 270, 900, 475),
    "hero": (30, 480, 900, 900),
    "hero_scenery": (480, 480, 890, 747),
    "metrics": (57, 745, 891, 872),
    "week_timeline": (30, 900, 895, 1185),
    "start_action": (110, 1185, 820, 1315),
    "leaderboard": (180, 1310, 755, 1425),
    "footer_scenery": (0, 1440, 941, 1672),
}


def main() -> int:
    parser = argparse.ArgumentParser(description="Sudoku real daily screen pixel audit")
    parser.add_argument("--reference", type=Path, required=True)
    parser.add_argument("--actual", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, default=Path("qa-pixel-diff/daily"))
    parser.add_argument("--allow-imperfect", action="store_true")
    args = parser.parse_args()
    rows = measure_regions(args.reference, args.actual, args.output_dir, REGIONS)
    for row in rows:
        print(f'{row["region"]}: {row["rgb_mae_0_to_255"]:.3f}/255; '
              f'within ±12 RGB = {row["rgb_pixels_within_12_pct"]:.2f}%')
    exact = all(row["rgb_mae_0_to_255"] == 0 for row in rows)
    print("Actual daily challenge screenshot pixel-for-pixel exact:", exact)
    return 0 if exact or args.allow_imperfect else 2


if __name__ == "__main__":
    raise SystemExit(main())
