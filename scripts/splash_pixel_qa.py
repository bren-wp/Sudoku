#!/usr/bin/env python3
"""Compare the real Compose splash screenshot with the owner's 941x1672 PNG.

Use untouched images from the real Android emulator and owner. No screenshot
overlays in the application, synthetic screenshots or rescaling to 941px.

python scripts/splash_pixel_qa.py \
  --reference references/08-splash.png \
  --actual screenshots/08-splash.png \
  --output-dir qa-pixel-diff/splash --allow-imperfect
"""
from __future__ import annotations

import argparse
from pathlib import Path

from home_pixel_qa import measure_regions

# Fixed, exclusive-right/bottom coordinates from the 941x1672 source.
# The grid region deliberately overlaps the top of the logo's halo; report
# this openly rather than tuning masks to make the measured MAE look better.
REGIONS = {
    "whole": (0, 0, 941, 1672),
    "upper_decorative_grid": (0, 0, 941, 430),
    "logo_and_halo": (280, 400, 665, 810),
    "title_byline_tagline": (160, 675, 780, 1000),
    "lower_scenery": (0, 1100, 941, 1672),
    "loading_indicator": (365, 1450, 580, 1610),
}


def main() -> int:
    parser = argparse.ArgumentParser(description="Real Sudoku Splash pixel QA")
    parser.add_argument("--reference", type=Path, required=True)
    parser.add_argument("--actual", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path,
                        default=Path("qa-pixel-diff/splash"))
    parser.add_argument("--allow-imperfect", action="store_true")
    args = parser.parse_args()
    results = measure_regions(args.reference, args.actual, args.output_dir, REGIONS)
    for row in results:
        print(f'{row["region"]}: {row["rgb_mae_0_to_255"]:.3f}/255; '
              f'within ±12 RGB = {row["rgb_pixels_within_12_pct"]:.2f}%')
    exact = all(row["rgb_mae_0_to_255"] == 0 for row in results)
    print("Exact real Splash pixel-for-pixel match:", exact)
    return 0 if exact or args.allow_imperfect else 2


if __name__ == "__main__":
    raise SystemExit(main())
