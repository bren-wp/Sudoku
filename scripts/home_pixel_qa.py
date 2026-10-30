#!/usr/bin/env python3
"""Measure the REAL Sudoku Home screenshot against the owner's reference.

No reference bitmap is shipped in the app or committed to the repository.
The historical regions are measured with fixed source-image coordinates, not
shifted until they conveniently match the actual app. Variable statistics
remain in the raw totals and are identified as such in the final report.

python scripts/home_pixel_qa.py \
  --reference references/01-home.png --actual screenshots/01-home.png \
  --output-dir qa-pixel-diff/home --allow-imperfect
"""
from __future__ import annotations

import argparse
import csv
from pathlib import Path

from PIL import Image, ImageChops, ImageEnhance, ImageStat

REFERENCE_SIZE = (941, 1672)
# Inclusive-exclusive rectangles in exact 941x1672 source coordinates.
HOME_REGIONS = {
    "whole": (0, 0, 941, 1672),
    "header": (0, 0, 941, 360),
    "resume": (35, 363, 896, 751),
    "primary_cards": (37, 766, 900, 1063),
    "compact_cards": (38, 1073, 898, 1265),
    "streak": (38, 1275, 897, 1437),
    "bottom_scenery": (0, 1450, 941, 1672),
}


def measure_regions(
    reference: Path, actual: Path, output_dir: Path,
    regions: dict[str, tuple[int, int, int, int]],
) -> list[dict]:
    with Image.open(reference) as source, Image.open(actual) as captured:
        if source.size != REFERENCE_SIZE or captured.size != REFERENCE_SIZE:
            raise ValueError(
                f"Home screenshot/reference must both be {REFERENCE_SIZE}; "
                f"received {source.size} and {captured.size}"
            )
        expected = source.convert("RGB")
        observed = captured.convert("RGB")

    output_dir.mkdir(parents=True, exist_ok=True)
    # Always write full-image evidence, including regions not selected below.
    Image.blend(expected, observed, .5).save(output_dir / "overlay.png")
    raw_diff = ImageChops.difference(expected, observed)
    ImageEnhance.Contrast(raw_diff).enhance(2).save(output_dir / "difference.png")

    report = []
    for name, bounds in regions.items():
        diff = raw_diff.crop(bounds)
        error = sum(ImageStat.Stat(diff).mean) / 3
        pixel_count = diff.width * diff.height
        close = None
        for channel in ("R", "G", "B"):
            channel_close = diff.getchannel(channel).point(
                lambda value: 255 if value <= 12 else 0
            )
            close = channel_close if close is None else ImageChops.multiply(close, channel_close)
        within_12 = close.histogram()[255] * 100 / pixel_count
        report.append({
            "region": name,
            "x0": bounds[0], "y0": bounds[1],
            "x1": bounds[2], "y1": bounds[3],
            "rgb_mae_0_to_255": round(error, 3),
            "rgb_pixels_within_12_pct": round(within_12, 2),
        })
    with (output_dir / "regions.csv").open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(report[0]))
        writer.writeheader()
        writer.writerows(report)
    return report



def measure_home(reference: Path, actual: Path, output_dir: Path) -> list[dict]:
    """Backward-compatible Home-specific view of the shared region comparator."""
    return measure_regions(reference, actual, output_dir, HOME_REGIONS)


def main() -> int:
    parser = argparse.ArgumentParser(description="Measured Home pixel regions")
    parser.add_argument("--reference", type=Path, required=True)
    parser.add_argument("--actual", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, default=Path("qa-pixel-diff/home"))
    parser.add_argument("--allow-imperfect", action="store_true")
    args = parser.parse_args()
    measurements = measure_home(args.reference, args.actual, args.output_dir)
    for item in measurements:
        print(f'{item["region"]}: {item["rgb_mae_0_to_255"]:.3f} / 255; '
              f'within 12 = {item["rgb_pixels_within_12_pct"]:.2f}%')
    exact = measurements[0]["rgb_mae_0_to_255"] == 0
    print("Actual Home screenshot exact 1:1 match:", exact)
    return 0 if exact or args.allow_imperfect else 2


if __name__ == "__main__":
    raise SystemExit(main())
