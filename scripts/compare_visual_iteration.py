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
    OWNER_REFERENCE_SHA256, REGIONS, TEN, actual_capture_name, sha256, validate_image
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
    rgba_channel_means = ImageStat.Stat(alpha_diff).mean
    # Pillow Image.getdata() is deprecated. Raw RGB/RGBA bytes retain
    # *exact* pixel boundaries, unlike overlapping byte-pattern counts.
    rgb_bytes = diff.tobytes()
    rgba_bytes = alpha_diff.tobytes()
    exact = sum(
        rgb_bytes[i] == 0 and rgb_bytes[i + 1] == 0 and rgb_bytes[i + 2] == 0
        for i in range(0, len(rgb_bytes), 3)
    )
    exact_rgba = sum(
        rgba_bytes[i] == 0 and rgba_bytes[i + 1] == 0 and
        rgba_bytes[i + 2] == 0 and rgba_bytes[i + 3] == 0
        for i in range(0, len(rgba_bytes), 4)
    )
    total = reference.width * reference.height
    return {
        "mae_0_to_255": mean,
        "alpha_mae_0_to_255": rgba_channel_means[3],
        "rgba_mae_0_to_255": sum(rgba_channel_means) / 4,
        "exact_rgb_pixels": exact,
        "exact_rgba_pixels": exact_rgba,
        "exact_rgb_percent": 100 * exact / total,
        "exact_rgba_percent": 100 * exact_rgba / total,
        "identical": exact_rgba == total,
    }


def compare_one(
    ref: Path, before: Path, after: Path, bottom_start: float,
    regions: dict[str, tuple[int, int, int, int]] | None = None,
) -> dict:
    with Image.open(ref) as original, Image.open(before) as prior, Image.open(after) as next_image:
        if not (original.size == prior.size == next_image.size):
            raise ValueError("Source, before and after must have identical native dimensions")
        # Do not discard source alpha here. In particular, the transparent
        # 1254px wordmark/launcher can change without changing a single RGB
        # channel, and such a regression must fail strict 1:1 equality.
        before_full = measure_rgb(original, prior)
        after_full = measure_rgb(original, next_image)
        y0 = int(original.height * bottom_start)
        roi = (0, y0, original.width, original.height)
        before_bottom = measure_rgb(original.crop(roi), prior.crop(roi))
        after_bottom = measure_rgb(original.crop(roi), next_image.crop(roi))
        region_rows = []
        for region_name, bounds in (regions or {}).items():
            x0, r0, x1, r1 = bounds
            if not (0 <= x0 < x1 <= original.width and
                    0 <= r0 < r1 <= original.height):
                raise ValueError(f"Invalid fixed ROI {region_name}: {bounds}")
            previous = measure_rgb(original.crop(bounds), prior.crop(bounds))
            current = measure_rgb(original.crop(bounds), next_image.crop(bounds))
            region_rows.append({
                "region": region_name,
                "x0": x0, "y0": r0, "x1": x1, "y1": r1,
                "before_rgb_mae": previous["mae_0_to_255"],
                "after_rgb_mae": current["mae_0_to_255"],
                "rgb_mae_delta": current["mae_0_to_255"] - previous["mae_0_to_255"],
                "before_exact_rgb_pct": previous["exact_rgb_percent"],
                "after_exact_rgb_pct": current["exact_rgb_percent"],
            })
    return {
        "before_full_mae": before_full["mae_0_to_255"],
        "after_full_mae": after_full["mae_0_to_255"],
        "full_mae_delta": after_full["mae_0_to_255"] - before_full["mae_0_to_255"],
        "before_alpha_mae": before_full["alpha_mae_0_to_255"],
        "after_alpha_mae": after_full["alpha_mae_0_to_255"],
        "before_rgba_mae": before_full["rgba_mae_0_to_255"],
        "after_rgba_mae": after_full["rgba_mae_0_to_255"],
        "alpha_mae_delta": after_full["alpha_mae_0_to_255"] - before_full["alpha_mae_0_to_255"],
        "before_bottom_mae": before_bottom["mae_0_to_255"],
        "after_bottom_mae": after_bottom["mae_0_to_255"],
        "bottom_mae_delta": after_bottom["mae_0_to_255"] - before_bottom["mae_0_to_255"],
        "before_exact_rgb_percent": before_full["exact_rgb_percent"],
        "after_exact_rgb_percent": after_full["exact_rgb_percent"],
        "before_exact_rgba_percent": before_full["exact_rgba_percent"],
        "after_exact_rgba_percent": after_full["exact_rgba_percent"],
        "before_exact_1_to_1": before_full["identical"],
        "after_exact_1_to_1": after_full["identical"],
        "region_rows": region_rows,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--reference-dir", type=Path, required=True)
    parser.add_argument("--before-dir", type=Path, required=True)
    parser.add_argument("--after-dir", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--bottom-start", type=float, default=.79)
    parser.add_argument("--home-resume", action="store_true")
    parser.add_argument(
        "--fail-on-regression", action="store_true",
        help="Fail on raw full-screen or fixed-region RGB/alpha regression; "
             "only use with truly identical deterministic screenshot states.",
    )
    parser.add_argument(
        "--regression-tolerance", type=float, default=0.0,
        help="Allowed MAE increase on any full screen or fixed region (default: 0).",
    )
    args = parser.parse_args()
    if not .5 <= args.bottom_start < 1:
        parser.error("--bottom-start must be within [0.5, 1.0)")
    if not 0 <= args.regression_tolerance <= 255:
        parser.error("--regression-tolerance must be in [0, 255]")

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
    per_region = []
    for name, reference, before, after in inputs:
        row = compare_one(reference, before, after, args.bottom_start,
                          regions=REGIONS.get(name))
        per_region.extend({"screen": name, **r} for r in row.pop("region_rows"))
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
    with (args.output_dir / "visual-iteration-regions.csv").open(
        "w", newline="", encoding="utf-8"
    ) as fh:
        writer = csv.DictWriter(fh, fieldnames=list(per_region[0]))
        writer.writeheader()
        writer.writerows(per_region)
    (args.output_dir / "visual-iteration.json").write_text(
        json.dumps({"bottom_start": args.bottom_start, "screens": rows,
                    "regions": per_region,
                    "exact_matches": sum(r["after_exact_1_to_1"] for r in rows)},
                   indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
    )
    if args.fail_on_regression:
        regressions = []
        for row in rows:
            if row["full_mae_delta"] > args.regression_tolerance:
                regressions.append(f'{row["screen"]} whole RGB: +{row["full_mae_delta"]:.6f}')
            if row["alpha_mae_delta"] > args.regression_tolerance:
                regressions.append(f'{row["screen"]} whole alpha: +{row["alpha_mae_delta"]:.6f}')
        for region in per_region:
            if region["rgb_mae_delta"] > args.regression_tolerance:
                regressions.append(
                    f'{region["screen"]}/{region["region"]} RGB: '
                    f'+{region["rgb_mae_delta"]:.6f}'
                )
        for item in regressions:
            print(f"REGRESSION: {item}")
        if regressions:
            print(f"FAIL: {len(regressions)} measured raw image regressions")
            return 3
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
