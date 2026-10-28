"""Regression tests for reference screenshot metrics; no Android emulator required."""
from __future__ import annotations

import tempfile
import unittest
from pathlib import Path

from PIL import Image

from pixel_compare import REFERENCE_SIZE, compare


class PixelCompareTest(unittest.TestCase):
    def test_identical_screens_have_zero_error(self):
        with tempfile.TemporaryDirectory() as tmp:
            folder = Path(tmp)
            source = Image.new("RGB", REFERENCE_SIZE, (4, 19, 49))
            source.save(folder / "reference.png")
            source.save(folder / "actual.png")
            measured = compare(
                folder / "reference.png", folder / "actual.png", folder / "result"
            )
            self.assertEqual(0, measured["mean_error_0_to_255"])
            self.assertEqual(100.0, measured["pixels_within_12_pct"])
            self.assertIsNone(measured["ui_region_mae"])

    def test_separate_ui_and_scenery_masks_without_hiding_full_error(self):
        with tempfile.TemporaryDirectory() as tmp:
            folder = Path(tmp)
            reference = Image.new("RGB", REFERENCE_SIZE, (0, 0, 0))
            actual = reference.copy()
            actual.paste((120, 0, 0), (0, 0, 100, 100))
            mask = Image.new("L", REFERENCE_SIZE, 0)
            mask.paste(255, (0, 0, 100, 100))
            reference.save(folder / "reference.png")
            actual.save(folder / "actual.png")
            mask.save(folder / "mask.png")
            measured = compare(
                folder / "reference.png", folder / "actual.png",
                folder / "result", folder / "mask.png",
            )
            self.assertEqual(40.0, measured["ui_region_mae"])
            self.assertEqual(0.0, measured["scenery_region_mae"])
            self.assertGreater(measured["mean_error_0_to_255"], 0)
            self.assertLess(measured["pixels_within_12_pct"], 100)

    def test_rejects_masks_at_incorrect_resolution(self):
        with tempfile.TemporaryDirectory() as tmp:
            folder = Path(tmp)
            Image.new("RGB", REFERENCE_SIZE).save(folder / "reference.png")
            Image.new("RGB", REFERENCE_SIZE).save(folder / "actual.png")
            Image.new("L", (200, 200)).save(folder / "mask.png")
            with self.assertRaises(ValueError):
                compare(
                    folder / "reference.png", folder / "actual.png",
                    folder / "result", folder / "mask.png",
                )


if __name__ == "__main__":
    unittest.main()
