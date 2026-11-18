"""Regression tests for native PNG integrity and 10-screen evidence manifest."""
from __future__ import annotations

import tempfile
import unittest
from pathlib import Path

from PIL import Image

from validate_android_capture import (
    BRANDING, SCREENS, inspect_png, validate_capture,
)


class CaptureEvidenceTests(unittest.TestCase):
    def test_rejects_corrupted_and_truncated_pngs(self):
        with tempfile.TemporaryDirectory() as root:
            p = Path(root) / "01-home.png"
            Image.new("RGB", (8, 6), (1, 2, 3)).save(p)
            result = inspect_png(p)
            self.assertEqual((8, 6, 2), (
                result["width"], result["height"], result["color_type"]
            ))
            valid_bytes = p.read_bytes()
            p.write_bytes(valid_bytes[:-7])
            with self.assertRaises(ValueError):
                inspect_png(p)
            p.write_bytes(valid_bytes[:-12] + b"xxxx" + valid_bytes[-8:])
            with self.assertRaises(ValueError):
                inspect_png(p)

    def test_complete_native_reference_evidence_includes_all_15_captures(self):
        with tempfile.TemporaryDirectory() as root:
            folder = Path(root)
            for name in SCREENS + ("01-home-resume",) + (
                "fixture-03-game", "fixture-06-statistics",
                "fixture-07-achievements",
            ):
                Image.new("RGB", (941, 1672), (2, 11, 39)).save(
                    folder / f"{name}.png"
                )
            for name in BRANDING:
                Image.new("RGBA", (1254, 1254), (2, 11, 39, 255)).save(
                    folder / f"{name}.png"
                )
            images = validate_capture(folder, "reference")
            self.assertEqual(15, len(images))
            self.assertEqual(15, len({item["sha256"] + item["name"]
                                     for item in images}))

    def test_reference_viewport_is_strict_not_rescaled(self):
        with tempfile.TemporaryDirectory() as root:
            folder = Path(root)
            for name in SCREENS[:2] + ("08-splash",):
                Image.new("RGB", (720, 1280)).save(folder / f"{name}.png")
            self.assertEqual(3, len(validate_capture(folder, "focused")))
            with self.assertRaises(ValueError):
                validate_capture(folder, "reference")

    def test_branding_alpha_is_required_even_when_dimensions_match(self):
        with tempfile.TemporaryDirectory() as root:
            folder = Path(root)
            for name in SCREENS:
                Image.new("RGB", (941, 1672)).save(folder / f"{name}.png")
            for name in BRANDING:
                Image.new("RGB", (1254, 1254)).save(folder / f"{name}.png")
            with self.assertRaises(ValueError):
                validate_capture(folder, "full")


if __name__ == "__main__":
    unittest.main()
