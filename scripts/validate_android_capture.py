#!/usr/bin/env python3
"""Verify native Android PNG evidence without depending on Pillow.

This script validates exported emulator images, not the private owner's
reference PNGs. Matching geometry is NECESSARY, never sufficient for 1:1.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import struct
import zlib
from pathlib import Path

PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"
SCREENS = tuple(f"{i:02d}-" + name for i, name in enumerate(
    ("home", "new-game", "game", "settings", "daily", "statistics",
     "achievements", "splash"), 1
))
BRANDING = ("09-wordmark", "10-launcher-icon", "11-launcher-monochrome")
FIXTURES = ("fixture-03-game", "fixture-06-statistics", "fixture-07-achievements")
REFERENCE_SIZE = (941, 1672)
BRANDING_SIZE = (1254, 1254)


def inspect_png(path: Path) -> dict:
    data = path.read_bytes()
    if not data.startswith(PNG_SIGNATURE):
        raise ValueError(f"{path.name}: invalid PNG signature")
    cursor = len(PNG_SIGNATURE)
    width = height = color = None
    has_idat = has_iend = False
    while cursor < len(data):
        if len(data) - cursor < 12:
            raise ValueError(f"{path.name}: truncated PNG chunk")
        length = struct.unpack_from(">I", data, cursor)[0]
        kind = data[cursor + 4:cursor + 8]
        end = cursor + 12 + length
        if end > len(data):
            raise ValueError(f"{path.name}: incomplete {kind!r} chunk")
        body = data[cursor + 8:cursor + 8 + length]
        actual_crc = struct.unpack_from(">I", data, cursor + 8 + length)[0]
        expected_crc = zlib.crc32(kind + body) & 0xFFFFFFFF
        if expected_crc != actual_crc:
            raise ValueError(f"{path.name}: corrupt {kind!r} chunk CRC")
        if cursor == len(PNG_SIGNATURE):
            if kind != b"IHDR" or length != 13:
                raise ValueError(f"{path.name}: invalid IHDR")
            width, height, depth, color, _, _, _ = struct.unpack(">IIBBBBB", body)
            if not 0 < width <= 8192 or not 0 < height <= 8192:
                raise ValueError(f"{path.name}: invalid dimensions")
            if color not in (0, 2, 3, 4, 6):
                raise ValueError(f"{path.name}: invalid color type {color}")
        elif kind == b"IHDR":
            raise ValueError(f"{path.name}: duplicate IHDR")
        if kind == b"IDAT":
            has_idat = True
        if kind == b"IEND":
            if length or end != len(data):
                raise ValueError(f"{path.name}: invalid PNG end")
            has_iend = True
        cursor = end
    if not (has_idat and has_iend):
        raise ValueError(f"{path.name}: missing IDAT/IEND")
    return {
        "width": width, "height": height, "color_type": color,
        "sha256": hashlib.sha256(data).hexdigest(),
        "bytes": len(data),
    }


def validate_capture(folder: Path, mode: str) -> list[dict]:
    if mode not in ("focused", "full", "reference"):
        raise ValueError(f"Unsupported capture mode: {mode}")
    names = list(SCREENS[:2]) + ["08-splash"] if mode == "focused" else (
        list(SCREENS) + list(BRANDING)
    )
    if mode == "reference":
        names += ["01-home-resume"] + list(FIXTURES)
    manifest = []
    for name in names:
        path = folder / f"{name}.png"
        if not path.is_file():
            raise FileNotFoundError(f"Missing native Android PNG: {path}")
        info = inspect_png(path)
        dimensions = info["width"], info["height"]
        if name in BRANDING:
            if dimensions != BRANDING_SIZE or info["color_type"] not in (4, 6):
                raise ValueError(f"{name}: branding must be 1254x1254 PNG with alpha")
        elif mode == "reference" and dimensions != REFERENCE_SIZE:
            raise ValueError(f"{name}: reference viewport must be 941x1672, got {dimensions}")
        manifest.append({"name": name, **info})
    return manifest


def main() -> int:
    parser = argparse.ArgumentParser(description="Verify exported Android screenshot PNGs")
    parser.add_argument("--dir", type=Path, required=True)
    parser.add_argument("--mode", choices=("focused", "full", "reference"), required=True)
    args = parser.parse_args()
    manifest = validate_capture(args.dir, args.mode)
    (args.dir / "capture-evidence.json").write_text(
        json.dumps({"mode": args.mode, "native_png_count": len(manifest),
                    "exact_pixel_parity_verified": False,
                    "screens": manifest}, indent=2) + "\n",
        encoding="utf-8",
    )
    print(f"Verified {len(manifest)} native PNGs and saved SHA-256 capture evidence")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
