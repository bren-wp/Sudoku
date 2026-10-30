#!/usr/bin/env python3
"""Compare the *real* native Sudoku Statistics screen with 941x1672 original.

Actual played games, completion fraction and achievements MUST come from the
user's history. The owner's populated conceptual reference is never copied
into the app as dummy statistics or used to fake clickable Android UI.

python scripts/statistics_pixel_qa.py \
  --reference references/06-statistics.png \
  --actual screenshots/06-statistics.png \
  --output-dir qa-pixel-diff/statistics --allow-imperfect
"""
from __future__ import annotations

import argparse
from pathlib import Path

from home_pixel_qa import measure_regions

# Inclusive left/top and exclusive right/bottom; coordinates are from the
# original 941x1672 reference, not fitted to the current emulator capture.
REGIONS = {
    "whole": (0, 0, 941, 1672),
    "header": (0, 0, 941, 185),
    "metric_cards": (30, 185, 909, 550),
    "chart_row": (30, 550, 909, 1030),
    "history": (30, 1040, 909, 1320),
    "achievement_previews": (30, 1320, 909, 1550),
    "bottom_scenery": (0, 1550, 941, 1672),
}


def main() -> int:
    parser = argparse.ArgumentParser(description="Sudoku Statistics native pixel QA")
    parser.add_argument("--reference", type=Path, required=True)
    parser.add_argument("--actual", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path,
                        default=Path("qa-pixel-diff/statistics"))
    parser.add_argument("--allow-imperfect", action="store_true")
    args = parser.parse_args()
    rows = measure_regions(args.reference, args.actual, args.output_dir, REGIONS)
    for row in rows:
        print(f'{row["region"]}: {row["rgb_mae_0_to_255"]:.3f}/255; '
              f'within ±12 RGB = {row["rgb_pixels_within_12_pct"]:.2f}%')
    exact = all(row["rgb_mae_0_to_255"] == 0 for row in rows)
    print("Statistics screen matches reference pixel-for-pixel:", exact)
    return 0 if exact or args.allow_imperfect else 2


if __name__ == "__main__":
    raise SystemExit(main())
