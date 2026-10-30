#!/usr/bin/env python3
"""QA of the owner's *transparent* 1254px Sudoku 7-3-9 launcher artwork.

Compare with the foreground actually packaged in the APK and exported from
the Android emulator. The Android launcher also composites an opaque
background/OS-specific mask, which must be tested separately. The original
PNG is reference-only and MUST NOT be pasted into the application or APK.

python scripts/launcher_icon_pixel_qa.py \
  --reference references/10-launcher-icon.png \
  --actual screenshots/10-launcher-icon.png \
  --monochrome screenshots/11-launcher-monochrome.png \
  --output-dir qa-pixel-diff/launcher --allow-imperfect
"""
from __future__ import annotations

import argparse
import csv
from pathlib import Path
from PIL import Image, ImageChops, ImageStat

from pixel_compare import BRANDING_SIZE, compare

# Hard-coded source-coordinate regions, not dynamically selected to improve
# the current implementation's error. Right/bottom coordinates exclusive.
ICON_REGIONS = {
    "whole": (0, 0, 1254, 1254),
    "outer_bevel": (92, 82, 1158, 1140),
    "nine_cells": (205, 194, 1050, 1034),
    "digit_7": (268, 250, 435, 428),
    "digit_3": (546, 527, 720, 717),
    "digit_9": (818, 794, 998, 980),
    "cyan_center_tile": (488, 481, 768, 754),
}


def alpha_bounds(image: Image.Image, threshold: int = 128) -> tuple[int, int, int, int] | None:
    """Require the owner's native 1254px canvas and measure visible alpha."""
    if image.size != BRANDING_SIZE:
        raise ValueError(f"Launcher PNG must be 1254x1254, got {image.size}")
    return image.convert("RGBA").getchannel("A").point(
        lambda value: 255 if value >= threshold else 0
    ).getbbox()


def verify_monochrome(path: Path) -> dict:
    """Themed icon is an independent *alpha mask*, not tinted owner artwork."""
    with Image.open(path) as image:
        if image.size != BRANDING_SIZE:
            raise ValueError(f"Monochrome PNG must be 1254x1254, got {image.size}")
        rgba = image.convert("RGBA")
    a = rgba.getchannel("A")
    bbox = alpha_bounds(rgba)
    if bbox is None:
        raise ValueError("Monochrome icon is completely transparent")
    if a.getpixel((0, 0)) or a.getpixel((1253, 1253)):
        raise ValueError("Themed icon must have truly transparent canvas corners")
    if a.getpixel((627, 627)) == 0:
        raise ValueError("Themed icon must preserve the central Sudoku digit")
    visible = a.point(lambda v: 255 if v >= 128 else 0).histogram()[255]
    fraction = visible / (1254 * 1254)
    if not .001 < fraction < .65:
        raise ValueError(f"Themed icon alpha coverage is suspicious: {fraction:.4f}")
    # Android tints using alpha, so the monochrome vector should contain
    # only white RGB where visible; transparent areas may have any RGB.
    r, g, b = rgba.split()[:3]
    for channel in (r, g, b):
        for color, coverage in enumerate(channel.histogram()):
            if color < 240 and coverage > 0:
                mask = a.point(lambda v: 255 if v >= 32 else 0)
                low = channel.point(lambda v: 255 if v < 240 else 0)
                if ImageChops.multiply(mask, low).getbbox() is not None:
                    raise ValueError("Themed icon contains nonwhite visible RGB")
    return {"alpha_bbox": bbox, "visible_alpha_128_pct": round(fraction * 100, 2)}


def launcher_report(reference: Path, actual: Path, output_dir: Path) -> dict:
    summary = compare(reference, actual, output_dir, expected_size=BRANDING_SIZE)
    with Image.open(reference) as original, Image.open(actual) as captured:
        source = original.convert("RGBA")
        current = captured.convert("RGBA")
    ref_bbox = alpha_bounds(source)
    real_bbox = alpha_bounds(current)
    if ref_bbox is None or real_bbox is None:
        raise ValueError("Source or actual vector icon has no visible pixels")

    output_dir.mkdir(parents=True, exist_ok=True)
    navy = Image.new("RGBA", BRANDING_SIZE, (2, 21, 56, 255))
    flattened_source = Image.alpha_composite(navy, source).convert("RGB")
    flattened_current = Image.alpha_composite(navy, current).convert("RGB")
    rgb_diff = ImageChops.difference(flattened_source, flattened_current)
    alpha_diff = ImageChops.difference(source.getchannel("A"), current.getchannel("A"))
    alpha_diff.save(output_dir / "alpha-difference.png")
    rows = []
    for name, roi in ICON_REGIONS.items():
        rgb = rgb_diff.crop(roi)
        alpha = alpha_diff.crop(roi)
        rows.append({
            "region": name, "x0": roi[0], "y0": roi[1],
            "x1": roi[2], "y1": roi[3],
            "navy_rgb_mae": round(sum(ImageStat.Stat(rgb).mean) / 3, 3),
            "alpha_mae": round(ImageStat.Stat(alpha).mean[0], 3),
        })
    with (output_dir / "launcher-regions.csv").open(
        "w", newline="", encoding="utf-8"
    ) as handle:
        writer = csv.DictWriter(handle, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)

    return {
        "original_bbox": ref_bbox,
        "actual_bbox": real_bbox,
        "bounds_delta_xyxy": tuple(b - a for a, b in zip(ref_bbox, real_bbox)),
        "summary": summary,
        "regions": rows,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="Real Android icon RGBA pixel QA")
    parser.add_argument("--reference", required=True, type=Path)
    parser.add_argument("--actual", required=True, type=Path)
    parser.add_argument("--monochrome", type=Path, default=None)
    parser.add_argument("--output-dir", type=Path, default=Path("qa-pixel-diff/launcher"))
    parser.add_argument("--allow-imperfect", action="store_true")
    args = parser.parse_args()
    result = launcher_report(args.reference, args.actual, args.output_dir)
    print("Owner visible bbox alpha>=128:", result["original_bbox"])
    print("Native foreground visible bbox alpha>=128:", result["actual_bbox"])
    print("Bounds delta x0,y0,x1,y1:", result["bounds_delta_xyxy"])
    print("Full alpha MAE:", result["summary"]["alpha_mae_0_to_255"])
    print("Full RGBA MAE:", result["summary"]["rgba_mae_0_to_255"])
    print("Navy-composited RGB MAE:", result["summary"]["navy_composited_rgb_mae"])
    for region in result["regions"]:
        print(region)
    if args.monochrome is not None:
        print("Independent Android themed mask:", verify_monochrome(args.monochrome))
    else:
        print("Themed launcher mask NOT CHECKED: pass --monochrome for full QA")
    exact = (
        result["summary"]["alpha_mae_0_to_255"] == 0
        and result["summary"]["mean_error_0_to_255"] == 0
    )
    print("Actually 1:1 pixel-perfect:", exact)
    return 0 if exact or args.allow_imperfect else 2


if __name__ == "__main__":
    raise SystemExit(main())
