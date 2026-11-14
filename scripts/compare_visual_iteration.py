#!/usr/bin/env python3
"""Three-way SHA-authenticated visual QA: owner original vs before vs after.

Never substitutes the original, rescales a screen, or claims 1:1 from lower
MAE alone. The same physical reference and viewport are required for each pair.
"""
from __future__ import annotations

import argparse
import csv
import json
from pathlib import Path

from PIL import Image, ImageChops, ImageStat

from final_ten_reference_qa import (
    OWNER_REFERENCE_SHA256, TEN, actual_capture_name, sha256, validate_image
)
from pixel_compare import REFERENCE_SIZE, BRANDING_SIZE


def measure_rgb(reference: Image.Image, actual: Image.Image) -> dict:
    if reference.size != actual.size:
        raise ValueError(f"Different image dimensions: {reference.size} / {actual.size}")
    diff = ImageChops.difference(reference.convert("RGB"), actual.convert("RGB"))
    alpha_diff = ImageChops.difference(
        reference.convert("RGBA"), actual.convert("RGBA")
    )
    mean = sum(ImageStat.Stat(diff).mean) / 3
    exact = sum(1 for pixel in diff.getdata() if pixel == (0, 0, 0))
    exact_rgba = sum(1 for pixel in alpha_diff.getdata()
                     if pixel == (0, 0, 0, 0))
    total = reference.width * reference.height
    return {
        "mae_0_to_255": mean,
        "exact_rgb_pixels": exact,
        "exact_rgba_pixels": exact_rgba,
        "exact_rgb_percent": 100 * exact / total,
        "exact_rgba_percent": 100 * exact_rgba / total,
        "identical": exact_rgba == total,
    }


def compare_one(ref: Path, before: Path, after: Path, bottom_start: float) -> dict:
    with Image.open(ref) as original, Image.open(before) as prior, Image.open(after) as next_image:
        if not (original.size == prior.size == next_image.size):
            raise ValueError("Source, before and after must have identical native dimensions")
        original_rgb = original.convert("RGB")
        before_rgb = prior.convert("RGB")
        after_rgb = next_image.convert("RGB")
        before_full = measure_rgb(original_rgb, before_rgb)
        after_full = measure_rgb(original_rgb, after_rgb)
        y0 = int(original.height * bottom_start)
        roi = (0, y0, original.width, original.height)
        before_bottom = measure_rgb(original_rgb.crop(roi), before_rgb.crop(roi))
        after_bottom = measure_rgb(original_rgb.crop(roi), after_rgb.crop(roi))
    return {
        "before_full_mae": before_full["mae_0_to_255"],
        "after_full_mae": after_full["mae_0_to_255"],
        "full_mae_delta": after_full["mae_0_to_255"] - before_full["mae_0_to_255"],
        "before_bottom_mae": before_bottom["mae_0_to_255"],
        "after_bottom_mae": after_bottom["mae_0_to_255"],
        "bottom_mae_delta": after_bottom["mae_0_to_255"] - before_bottom["mae_0_to_255"],
        "before_exact_rgb_percent": before_full["exact_rgb_percent"],
        "after_exact_rgb_percent": after_full["exact_rgb_percent"],
        "after_exact_1_to_1": after_full["identical"],
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--reference-dir", type=Path, required=True)
    parser.add_argument("--before-dir", type=Path, required=True)
    parser.add_argument("--after-dir", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--bottom-start", type=float, default=.79)
    parser.add_argument("--home-resume", action="store_true")
    args = parser.parse_args()
    if not .5 <= args.bottom_start < 1:
        parser.error("--bottom-start must be within [0.5, 1.0)")

    # Fail closed BEFORE writing any results: the original files must match
    # all known owner hashes. Capture alpha is not silently discarded.
    inputs = []
    for name in TEN:
        reference = args.reference_dir / f"{name}.png"
        capture = actual_capture_name(name, args.home_resume) + ".png"
        before = args.before_dir / capture
        after = args.after_dir / capture
        size = BRANDING_SIZE if name.startswith(("09-", "10-")) else REFERENCE_SIZE
        validate_image(reference, size, require_alpha=name.startswith(("09-", "10-")))
        validate_image(before, size, require_alpha=name.startswith(("09-", "10-")))
        validate_image(after, size, require_alpha=name.startswith(("09-", "10-")))
        if sha256(reference) != OWNER_REFERENCE_SHA256[name]:
            raise ValueError(f"Original reference mismatch: {name}")
        inputs.append((name, reference, before, after))

    rows = []
    for name, reference, before, after in inputs:
        row = compare_one(reference, before, after, args.bottom_start)
        row.update({
            "screen": name,
            "reference_sha256": sha256(reference),
            "before_sha256": sha256(before),
            "after_sha256": sha256(after),
        })
        rows.append(row)
        print(f"{name}: full MAE Δ {row['full_mae_delta']:+.6f}; "
              f"bottom MAE Δ {row['bottom_mae_delta']:+.6f}; "
              f"exact={row['after_exact_1_to_1']}")
    args.output_dir.mkdir(parents=True, exist_ok=True)
    with (args.output_dir / "visual-iteration.csv").open("w", newline="") as fh:
        writer = csv.DictWriter(fh, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)
    (args.output_dir / "visual-iteration.json").write_text(
        json.dumps({"bottom_start": args.bottom_start, "screens": rows,
                    "exact_matches": sum(r["after_exact_1_to_1"] for r in rows)},
                   indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
