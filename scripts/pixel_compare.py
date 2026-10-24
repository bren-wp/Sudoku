#!/usr/bin/env python3
"""Compare *rendered* Android screenshots against the supplied 941x1672 references.

The app remains real Compose UI: reference images are never used as a fake screen.
Requires Pillow: python -m pip install Pillow
Usage:
  python scripts/pixel_compare.py --reference-dir references --actual-dir screenshots --output-dir qa-diff

Reference files (provided by the owner) can retain original filenames; rename them
as in the mapping below. This script does NOT declare visual parity without proof.
"""
from __future__ import annotations

import argparse
import csv
from pathlib import Path

from PIL import Image, ImageChops, ImageEnhance, ImageStat

SCREENS = (
    "01-home",
    "02-new-game",
    "03-game",
    "04-settings",
    "05-daily",
    "06-statistics",
    "07-achievements",
)
REFERENCE_SIZE = (941, 1672)

def compare(reference: Path, actual: Path, destination: Path) -> dict[str, str | float]:
    with Image.open(reference) as r, Image.open(actual) as a:
        expected = r.convert("RGB")
        observed = a.convert("RGB")
    if expected.size != REFERENCE_SIZE:
        raise ValueError(f"{reference.name}: expected {REFERENCE_SIZE}, found {expected.size}")
    if observed.size != REFERENCE_SIZE:
        raise ValueError(f"{actual.name}: emulator resolution {observed.size}, expected {REFERENCE_SIZE}")

    difference = ImageChops.difference(expected, observed)
    mean_r, mean_g, mean_b = ImageStat.Stat(difference).mean
    mae = (mean_r + mean_g + mean_b) / 3
    histogram = difference.convert("L").histogram()
    pixel_count = expected.width * expected.height
    within_12 = sum(histogram[:13]) / pixel_count
    within_24 = sum(histogram[:25]) / pixel_count

    destination.mkdir(parents=True, exist_ok=True)
    Image.blend(expected, observed, .5).save(destination / "overlay.png", optimize=True)
    ImageEnhance.Contrast(difference).enhance(2).save(
        destination / "difference.png", optimize=True
    )
    return {
        "screen": actual.stem,
        "mean_error_0_to_255": round(mae, 3),
        "pixels_within_12_pct": round(within_12 * 100, 2),
        "pixels_within_24_pct": round(within_24 * 100, 2),
    }

def main() -> int:
    parser = argparse.ArgumentParser(description="Pixel comparison of native Sudoku UI")
    parser.add_argument("--reference-dir", required=True, type=Path)
    parser.add_argument("--actual-dir", required=True, type=Path)
    parser.add_argument("--output-dir", default=Path("qa-pixel-diff"), type=Path)
    args = parser.parse_args()
    args.output_dir.mkdir(parents=True, exist_ok=True)
    rows = []
    for name in SCREENS:
        ref = args.reference_dir / f"{name}.png"
        actual = args.actual_dir / f"{name}.png"
        if not ref.is_file() or not actual.is_file():
            raise FileNotFoundError(f"Missing reference or screenshot for {name}: {ref} / {actual}")
        row = compare(ref, actual, args.output_dir / name)
        rows.append(row)
        print(f'{name}: mean channel error {row["mean_error_0_to_255"]} / 255; '
              f'within 12 = {row["pixels_within_12_pct"]}%')
    with (args.output_dir / "report.csv").open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)
    exact = all(float(row["mean_error_0_to_255"]) == 0 for row in rows)
    print("Exact pixel match across all screens:", exact)
    return 0 if exact else 2

if __name__ == "__main__":
    raise SystemExit(main())
