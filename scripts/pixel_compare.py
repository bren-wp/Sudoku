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

from PIL import Image, ImageChops, ImageEnhance, ImageOps, ImageStat

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
BRANDING_SIZE = (1254, 1254)
BRANDING = ("09-wordmark", "10-launcher-icon")

def compare(
    reference: Path, actual: Path, destination: Path, ui_mask: Path | None = None,
    expected_size: tuple[int, int] = REFERENCE_SIZE,
    volatile_mask: Path | None = None,
) -> dict[str, str | float | None]:
    with Image.open(reference) as r, Image.open(actual) as a:
        # Keep alpha for 1254px transparent logo/icon references: flattening
        # both images to RGB silently misses opaque-background regressions.
        reference_rgba = r.convert("RGBA")
        actual_rgba = a.convert("RGBA")
        expected = reference_rgba.convert("RGB")
        observed = actual_rgba.convert("RGB")
    if expected.size != expected_size:
        raise ValueError(f"{reference.name}: expected {expected_size}, found {expected.size}")
    if observed.size != expected_size:
        raise ValueError(f"{actual.name}: actual resolution {observed.size}, expected {expected_size}")

    difference = ImageChops.difference(expected, observed)
    mean_r, mean_g, mean_b = ImageStat.Stat(difference).mean
    mae = (mean_r + mean_g + mean_b) / 3
    pixel_count = expected.width * expected.height

    def within_tolerance(threshold: int) -> float:
        # A pixel is within tolerance only when EACH RGB channel is close,
        # not merely when the grayscale brightness of the difference is low.
        near = [difference.getchannel(channel).point(
            lambda value: 255 if value <= threshold else 0
        ) for channel in ("R", "G", "B")]
        return ImageChops.multiply(
            ImageChops.multiply(near[0], near[1]), near[2]
        ).histogram()[255] / pixel_count

    within_12 = within_tolerance(12)
    within_24 = within_tolerance(24)
    alpha_mae = None
    rgba_mae = None
    rgba_within_12 = None
    navy_composite_mae = None
    if expected_size == BRANDING_SIZE:
        alpha_difference = ImageChops.difference(
            reference_rgba.getchannel("A"), actual_rgba.getchannel("A")
        )
        alpha_mae = round(ImageStat.Stat(alpha_difference).mean[0], 3)
        rgba_mae = round((mean_r + mean_g + mean_b +
                          alpha_mae) / 4, 3)
        close_alpha = alpha_difference.point(
            lambda value: 255 if value <= 12 else 0
        )
        rgb_close = [difference.getchannel(c).point(
            lambda value: 255 if value <= 12 else 0
        ) for c in ("R", "G", "B")]
        full_close = close_alpha
        for channel in rgb_close:
            full_close = ImageChops.multiply(full_close, channel)
        rgba_within_12 = round(
            full_close.histogram()[255] * 100 / pixel_count, 2
        )
        # A second, practical view of the artwork on its real navy backing.
        # Report both raw RGBA differences and rendered-on-background RGB;
        # neither is a substitute for a real emulator screenshot.
        navy = Image.new("RGBA", expected_size, (2, 21, 56, 255))
        ref_navy = Image.alpha_composite(navy, reference_rgba).convert("RGB")
        actual_navy = Image.alpha_composite(navy, actual_rgba).convert("RGB")
        navy_diff = ImageChops.difference(ref_navy, actual_navy)
        navy_composite_mae = round(
            sum(ImageStat.Stat(navy_diff).mean) / 3, 3
        )
    ui_error = None
    scenery_error = None
    if ui_mask is not None:
        with Image.open(ui_mask) as mask_image:
            if mask_image.size != expected_size:
                raise ValueError(
                    f"{ui_mask.name}: mask size {mask_image.size} != {expected_size}"
                )
            mask = mask_image.convert("L").point(
                lambda value: 255 if value >= 128 else 0
            )
        def masked_mae(region: Image.Image) -> float | None:
            if region.histogram()[255] == 0:
                return None
            return round(sum(ImageStat.Stat(difference, mask=region).mean) / 3, 3)

        ui_error = masked_mae(mask)
        scenery_error = masked_mae(ImageOps.invert(mask))

    # Optional QA-only volatile-area mask (white = differing date, timer,
    # generated puzzle, or other changing content). The full-screen MAE
    # remains reported separately so these masks cannot disguise regressions.
    stable_error = None
    if volatile_mask is not None:
        with Image.open(volatile_mask) as mask_image:
            if mask_image.size != expected_size:
                raise ValueError(
                    f"{volatile_mask.name}: mask size {mask_image.size} != {expected_size}"
                )
            volatile = mask_image.convert("L").point(
                lambda value: 255 if value >= 128 else 0
            )
        stable = ImageOps.invert(volatile)
        if stable.histogram()[255] == 0:
            raise ValueError("Volatile mask cannot exclude the entire image")
        stable_error = round(sum(ImageStat.Stat(difference, mask=stable).mean) / 3, 3)

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
        "ui_region_mae": ui_error,
        "scenery_region_mae": scenery_error,
        "stable_region_mae": stable_error,
        "alpha_mae_0_to_255": alpha_mae,
        "rgba_mae_0_to_255": rgba_mae,
        "rgba_within_12_pct": rgba_within_12,
        "navy_composited_rgb_mae": navy_composite_mae,
    }

def main() -> int:
    parser = argparse.ArgumentParser(description="Pixel comparison of native Sudoku UI")
    parser.add_argument("--reference-dir", required=True, type=Path)
    parser.add_argument("--actual-dir", required=True, type=Path)
    parser.add_argument("--output-dir", default=Path("qa-pixel-diff"), type=Path)
    parser.add_argument("--ui-mask-dir", type=Path, default=None,
        help="Optional 941x1672 binary masks: white = interactive UI, black = scenery.")
    parser.add_argument("--include-splash", action="store_true",
        help="Include 08-splash when a real launch capture is available.")
    parser.add_argument("--include-branding", action="store_true",
        help="Also compare real 1254x1254 PNG exports for logo and adaptive icon, including alpha.")
    parser.add_argument("--volatile-mask-dir", type=Path, default=None,
        help="Optional 941x1672 masks: white = changing QA-only content.")
    parser.add_argument("--baseline-report", type=Path, default=None,
        help="Previous report.csv; fail if any screen raw MAE regresses.")
    parser.add_argument("--allow-imperfect", action="store_true",
        help="Exit 0 for a valid non-exact audit (never claim pixel-perfect).")
    args = parser.parse_args()
    args.output_dir.mkdir(parents=True, exist_ok=True)
    rows = []
    screens = SCREENS + (("08-splash",) if args.include_splash else ()) + (
        BRANDING if args.include_branding else ()
    )
    for name in screens:
        ref = args.reference_dir / f"{name}.png"
        actual = args.actual_dir / f"{name}.png"
        if not ref.is_file() or not actual.is_file():
            raise FileNotFoundError(f"Missing reference or screenshot for {name}: {ref} / {actual}")
        is_branding = name in BRANDING
        ui_mask = args.ui_mask_dir / f"{name}.png" if args.ui_mask_dir and not is_branding else None
        volatile_mask = (args.volatile_mask_dir / f"{name}.png"
                         if args.volatile_mask_dir and not is_branding else None)
        for mask in (ui_mask, volatile_mask):
            if mask is not None and not mask.is_file():
                raise FileNotFoundError(f"Missing QA mask for {name}: {mask}")
        row = compare(
            ref, actual, args.output_dir / name, ui_mask,
            BRANDING_SIZE if is_branding else REFERENCE_SIZE, volatile_mask,
        )
        rows.append(row)
        print(f'{name}: mean channel error {row["mean_error_0_to_255"]} / 255; '
              f'within 12 = {row["pixels_within_12_pct"]}%')
    with (args.output_dir / "report.csv").open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)
    exact = all(
        float(row["mean_error_0_to_255"]) == 0 and
        (row["alpha_mae_0_to_255"] is None or
         float(row["alpha_mae_0_to_255"]) == 0)
        for row in rows
    )
    print("Exact pixel match across all screens:", exact)
    if args.baseline_report is not None:
        with args.baseline_report.open(newline="", encoding="utf-8") as source:
            baseline = {r["screen"]: float(r["mean_error_0_to_255"])
                        for r in csv.DictReader(source)}
        missing = set(screens) - baseline.keys()
        if missing:
            raise ValueError(f"Baseline missing screens: {sorted(missing)}")
        regressed = [(r["screen"], r["mean_error_0_to_255"], baseline[r["screen"]])
                     for r in rows
                     if float(r["mean_error_0_to_255"]) > baseline[r["screen"]]]
        if regressed:
            print("Pixel QA regression(s):", regressed)
            return 3
    return 0 if exact or args.allow_imperfect else 2

if __name__ == "__main__":
    raise SystemExit(main())
