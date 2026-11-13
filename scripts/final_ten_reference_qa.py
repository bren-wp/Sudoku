#!/usr/bin/env python3
"""End-to-end pixel audit of ALL TEN owner references against REAL Android PNGs.

This does not build or invent images. It fails closed for missing screenshots,
wrong resolution, missing transparent alpha, or missing Android themed icon.
A numerical success from --allow-imperfect is *not* a 1:1 success.

Usage:
 python scripts/final_ten_reference_qa.py \
   --reference-dir references --actual-dir screenshots \
   --output-dir qa-pixel-diff/final --allow-imperfect

Input names 01-home through 10-launcher-icon, plus 11-launcher-monochrome
for the actual APK only. 01-08 are 941x1672, 09-11 are 1254x1254.
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
from pathlib import Path

from PIL import Image

from pixel_compare import BRANDING_SIZE, REFERENCE_SIZE, compare
from home_pixel_qa import HOME_REGIONS, measure_regions
from new_game_pixel_qa import REGIONS as NEW_GAME
from gameplay_pixel_qa import REGIONS as GAME
from settings_pixel_qa import REGIONS as SETTINGS
from daily_pixel_qa import REGIONS as DAILY
from statistics_pixel_qa import REGIONS as STATISTICS
from achievements_pixel_qa import REGIONS as ACHIEVEMENTS
from splash_pixel_qa import REGIONS as SPLASH
from wordmark_pixel_qa import wordmark_report
from launcher_icon_pixel_qa import launcher_report, verify_monochrome

REGIONS = {
    "01-home": HOME_REGIONS,
    "02-new-game": NEW_GAME,
    "03-game": GAME,
    "04-settings": SETTINGS,
    "05-daily": DAILY,
    "06-statistics": STATISTICS,
    "07-achievements": ACHIEVEMENTS,
    "08-splash": SPLASH,
}
TEN = tuple(REGIONS) + ("09-wordmark", "10-launcher-icon")
THEMED = "11-launcher-monochrome"

# The ten owner's original PNGs (24 Oct 2026) are private and MUST NOT
# be committed. These SHA-256 fingerprints authenticate all eight screens
# and both brand assets. Prevent passing the audit using a substituted or
# accidentally mismatched image named "01-home.png", etc.
OWNER_REFERENCE_SHA256 = {
    "01-home": "f930940fe9ecece5f955b25c76b019c7c310313bf0f0e70bcb17c1a6aba89694",
    "02-new-game": "ff7a66b6a58285443e6a70f764d5a19d877aa986e948a2a0962b9df10039330e",
    "03-game": "ebcb1e1f5d5fc3f081894f048ade85ef59e6cbed015d6f6e6132c89b6820e9dc",
    "04-settings": "4a97ac27d3ecca33d7ef8068a30ada888c8a349fe9718d24a996f43239027a9c",
    "05-daily": "b558b297b49be88e858b0cab6f9978263a48c8e4acb0a8aab6e24a4e8365e01d",
    "06-statistics": "64c2f4e6c090bbd693801a965fdeb46dbde02fb22dbae06b7a288e6b0523dafa",
    "07-achievements": "461e193d686e3b778c97d50ec2a661317d5115be465c909a85bd8405e5e3b5c8",
    "08-splash": "795992a79de684f67b3f5924fce7377ed429f4e5c272124c16115ad9885f636c",
    "09-wordmark": "7534ad4d8bbfef116ad7d092cb91027f5d393a6008b7ecb37a7a33aed7047fd1",
    "10-launcher-icon": "0b7a48a510a6e86cfb2121b24b758d7de7cd4541fd50ac3f1f555801f7763ae9",
}


def sha256(path: Path) -> str:
    result = hashlib.sha256()
    with path.open("rb") as handle:
        for block in iter(lambda: handle.read(1024 * 1024), b""):
            result.update(block)
    return result.hexdigest()


def validate_image(path: Path, size: tuple[int, int], require_alpha: bool = False) -> None:
    if not path.is_file():
        raise FileNotFoundError(f"Missing real PNG input: {path}")
    with Image.open(path) as image:
        if image.format != "PNG":
            raise ValueError(f"{path}: actual file format is {image.format}, not PNG")
        if image.size != size:
            raise ValueError(f"{path}: wrong native dimensions {image.size}, expected {size}")
        if require_alpha and "A" not in image.getbands():
            raise ValueError(f"{path}: transparent branding MUST be an RGBA PNG")


def actual_capture_name(name: str, home_resume: bool = False) -> str:
    """Select the real saved-session Home capture for the owner's reference."""
    return "01-home-resume" if home_resume and name == "01-home" else name


def audit(reference_dir: Path, actual_dir: Path, output_dir: Path,
          home_resume: bool = False) -> dict:
    # Validate every input first: partial screenshots are not a successful audit.
    source_hashes = {}
    actual_hashes = {}
    for name in TEN:
        reference = reference_dir / f"{name}.png"
        actual = actual_dir / f"{actual_capture_name(name, home_resume)}.png"
        expected_size = BRANDING_SIZE if name.startswith(("09-", "10-")) else REFERENCE_SIZE
        validate_image(reference, expected_size, name.startswith(("09-", "10-")))
        validate_image(actual, expected_size, name.startswith(("09-", "10-")))
        original = sha256(reference)
        if original != OWNER_REFERENCE_SHA256[name]:
            raise ValueError(
                f"{name}: NOT the owner's unmodified original; SHA256={original}"
            )
        source_hashes[name] = original
        actual_hashes[name] = sha256(actual)

    monochrome = actual_dir / f"{THEMED}.png"
    validate_image(monochrome, BRANDING_SIZE, require_alpha=True)
    mono_result = verify_monochrome(monochrome)
    actual_hashes[THEMED] = sha256(monochrome)
    output_dir.mkdir(parents=True, exist_ok=True)

    rows = []
    for name in TEN:
        source = reference_dir / f"{name}.png"
        actual = actual_dir / f"{actual_capture_name(name, home_resume)}.png"
        folder = output_dir / name
        if name in REGIONS:
            report = compare(source, actual, folder, expected_size=REFERENCE_SIZE)
            region_rows = measure_regions(source, actual, folder, REGIONS[name])
        elif name == "09-wordmark":
            result = wordmark_report(source, actual, folder)
            report = result["summary"]
            region_rows = result["regions"]
        else:
            result = launcher_report(source, actual, folder)
            report = result["summary"]
            region_rows = result["regions"]

        alpha = report["alpha_mae_0_to_255"]
        # The pixel-level boolean is not rounded to three decimal places.
        exact = report["exact_1_to_1"]
        rows.append({
            "screen": name,
            "actual_capture": actual_capture_name(name, home_resume),
            "reference_sha256": source_hashes[name],
            "actual_sha256": actual_hashes[name],
            "exact_1_to_1": exact,
            "exact_rgb_pixels": report["exact_rgb_pixels"],
            "exact_rgb_pixels_pct": report["exact_rgb_pixels_pct"],
            "exact_rgba_pixels_pct": report["exact_rgba_pixels_pct"],
            "rgb_mae_0_to_255": report["mean_error_0_to_255"],
            "alpha_mae_0_to_255": alpha,
            "rgba_mae_0_to_255": report["rgba_mae_0_to_255"],
            "navy_composited_rgb_mae": report["navy_composited_rgb_mae"],
            "regions_measured": len(region_rows),
        })
        print(
            f'{name}: EXACT={exact}, RGB MAE={report["mean_error_0_to_255"]}, '
            f'ALPHA MAE={alpha}; {len(region_rows)} fixed regions'
        )

    with (output_dir / "all-ten-report.csv").open(
        "w", newline="", encoding="utf-8"
    ) as handle:
        writer = csv.DictWriter(handle, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)
    all_exact = all(row["exact_1_to_1"] for row in rows)
    final = {
        "reference_count": len(TEN),
        "actual_count": len(TEN) + 1,
        "all_ten_exact_1_to_1": all_exact,
        "themed_mask_validation": mono_result,
        "themed_mask_sha256": actual_hashes[THEMED],
        "screens": rows,
    }
    (output_dir / "all-ten-report.json").write_text(
        json.dumps(final, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
    )
    return final


def main() -> int:
    parser = argparse.ArgumentParser(description="All 10 original Sudoku pixel QA")
    parser.add_argument("--reference-dir", type=Path, required=True)
    parser.add_argument("--actual-dir", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, default=Path("qa-pixel-diff/final"))
    parser.add_argument("--allow-imperfect", action="store_true")
    parser.add_argument("--home-resume", action="store_true",
                        help="Compare the owner's Home-with-Resume against 01-home-resume.png")
    args = parser.parse_args()
    result = audit(args.reference_dir, args.actual_dir, args.output_dir,
                   home_resume=args.home_resume)
    print("All TEN actually pixel-exact 1:1:", result["all_ten_exact_1_to_1"])
    return 0 if result["all_ten_exact_1_to_1"] or args.allow_imperfect else 2


if __name__ == "__main__":
    raise SystemExit(main())
