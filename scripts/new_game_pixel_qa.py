#!/usr/bin/env python3
"""Compare the REAL New Game Compose screenshot with the owner's 941x1672 art.

Nothing in this script modifies the owner's PNG, fakes UI, rescales actual
screenshots, or labels a nonzero error 'pixel-perfect'.

python scripts/new_game_pixel_qa.py \
  --reference references/02-new-game.png \
  --actual screenshots/02-new-game.png \
  --output-dir qa-pixel-diff/new-game --allow-imperfect
"""
from __future__ import annotations

import argparse
from pathlib import Path

from home_pixel_qa import measure_regions

# These rectangles were fixed from the owner's 941x1672 reference; do NOT
# adjust them after inspecting a bad result merely to lower the score.
REGIONS = {
    "whole": (0, 0, 941, 1672),
    "size_selector": (465, 28, 900, 150),
    "title_and_subtitle": (35, 162, 630, 360),
    "header": (0, 0, 941, 360),
    "six_cards": (38, 366, 902, 1332),
    "upper_three_cards": (38, 366, 902, 855),
    "lower_three_cards": (38, 855, 902, 1332),
    "cta": (100, 1330, 845, 1505),
    "bottom_scenery": (0, 1490, 941, 1672),
}


def main() -> int:
    parser = argparse.ArgumentParser(description="New Game native screenshot region QA")
    parser.add_argument("--reference", type=Path, required=True)
    parser.add_argument("--actual", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path,
                        default=Path("qa-pixel-diff/new-game"))
    parser.add_argument("--allow-imperfect", action="store_true")
    args = parser.parse_args()
    rows = measure_regions(args.reference, args.actual, args.output_dir, REGIONS)
    for row in rows:
        print(f'{row["region"]}: {row["rgb_mae_0_to_255"]:.3f}/255; '
              f'within 12 = {row["rgb_pixels_within_12_pct"]:.2f}%')
    exact = all(row["rgb_mae_0_to_255"] == 0 for row in rows)
    print("Exact 1:1 match of actual New Game render:", exact)
    return 0 if exact or args.allow_imperfect else 2


if __name__ == "__main__":
    raise SystemExit(main())
