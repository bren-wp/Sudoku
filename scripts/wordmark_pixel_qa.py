#!/usr/bin/env python3
"""Compare the owner's transparent 1254x1254 wordmark with *actual* Compose QA.

The source image is private and NEVER copied into the Android APK.
Report RAW RGBA and alpha accuracy as well as appearance on navy. Separate
source-visible symbol, title and byline regions retain fixed coordinates.

python scripts/wordmark_pixel_qa.py \
  --reference references/09-wordmark.png --actual screenshots/09-wordmark.png \
  --output-dir qa-pixel-diff/wordmark --allow-imperfect
"""
from __future__ import annotations

import argparse
import csv
from pathlib import Path

from PIL import Image, ImageChops, ImageStat
from pixel_compare import BRANDING_SIZE, compare

# Fixed coordinates from the original owner's 1254x1254 canvas.
WORDMARK_REGIONS = {
    "full": (0, 0, 1254, 1254),
    "symbol_739": (43, 415, 413, 810),
    "title_sudoku": (392, 476, 1230, 716),
    "byline_brendigo": (650, 700, 1160, 800),
}


def opaque_bounds(image: Image.Image, minimum_alpha: int = 128) -> tuple[int, int, int, int] | None:
    """Actual alpha coverage; never confuse black transparent RGB with ink."""
    if image.size != BRANDING_SIZE:
        raise ValueError(f"Wordmark must be 1254x1254, found {image.size}")
    alpha = image.convert("RGBA").getchannel("A")
    return alpha.point(lambda value: 255 if value >= minimum_alpha else 0).getbbox()


def wordmark_report(reference: Path, actual: Path, output_dir: Path) -> dict:
    summary = compare(reference, actual, output_dir, expected_size=BRANDING_SIZE)
    with Image.open(reference) as source, Image.open(actual) as rendered:
        expected = source.convert("RGBA")
        observed = rendered.convert("RGBA")
    source_bbox = opaque_bounds(expected)
    actual_bbox = opaque_bounds(observed)
    if source_bbox is None or actual_bbox is None:
        raise ValueError("Reference or native Compose wordmark has no visible alpha >= 128")
    output_dir.mkdir(parents=True, exist_ok=True)
    navy = Image.new("RGBA", BRANDING_SIZE, (2, 21, 56, 255))
    expected_visible = Image.alpha_composite(navy, expected).convert("RGB")
    actual_visible = Image.alpha_composite(navy, observed).convert("RGB")
    composite_diff = ImageChops.difference(expected_visible, actual_visible)
    alpha_diff = ImageChops.difference(expected.getchannel("A"), observed.getchannel("A"))
    alpha_diff.save(output_dir / "alpha-difference.png")
    rows = []
    for name, box in WORDMARK_REGIONS.items():
        region = composite_diff.crop(box)
        change = alpha_diff.crop(box)
        rows.append({
            "region": name,
            "x0": box[0], "y0": box[1], "x1": box[2], "y1": box[3],
            "navy_composited_rgb_mae": round(sum(ImageStat.Stat(region).mean) / 3, 3),
            "alpha_mae": round(ImageStat.Stat(change).mean[0], 3),
        })
    with (output_dir / "wordmark-regions.csv").open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)
    return {
        "source_visible_bbox": source_bbox,
        "actual_visible_bbox": actual_bbox,
        "bounds_delta_xyxy": tuple(b - a for a, b in zip(source_bbox, actual_bbox)),
        "summary": summary,
        "regions": rows,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="Transparent real Compose wordmark QA")
    parser.add_argument("--reference", type=Path, required=True)
    parser.add_argument("--actual", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, default=Path("qa-pixel-diff/wordmark"))
    parser.add_argument("--allow-imperfect", action="store_true")
    args = parser.parse_args()
    report = wordmark_report(args.reference, args.actual, args.output_dir)
    print("Original alpha>=128 bounding box:", report["source_visible_bbox"])
    print("Native alpha>=128 bounding box:", report["actual_visible_bbox"])
    print("Bounds delta xyxy:", report["bounds_delta_xyxy"])
    print("Full RGBA MAE:", report["summary"]["rgba_mae_0_to_255"])
    print("Full alpha MAE:", report["summary"]["alpha_mae_0_to_255"])
    for row in report["regions"]:
        print(row)
    exact = (
        report["summary"]["mean_error_0_to_255"] == 0 and
        report["summary"]["alpha_mae_0_to_255"] == 0
    )
    print("Pixel 1:1 identical:", exact)
    return 0 if exact or args.allow_imperfect else 2


if __name__ == "__main__":
    raise SystemExit(main())
