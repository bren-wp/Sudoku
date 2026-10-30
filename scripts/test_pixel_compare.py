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

    def test_branding_uses_real_square_reference_resolution(self):
        from pixel_compare import BRANDING_SIZE
        with tempfile.TemporaryDirectory() as tmp:
            folder = Path(tmp)
            Image.new("RGB", BRANDING_SIZE, (8, 16, 64)).save(
                folder / "wordmark.png"
            )
            measured = compare(
                folder / "wordmark.png", folder / "wordmark.png",
                folder / "brand-result", expected_size=BRANDING_SIZE,
            )
            self.assertEqual(0, measured["mean_error_0_to_255"])
            with self.assertRaises(ValueError):
                compare(
                    folder / "wordmark.png", folder / "wordmark.png",
                    folder / "wrong-size-result",
                )

    def test_transparent_logo_rejects_opaque_black_even_when_rgb_is_identical(self):
        from pixel_compare import BRANDING_SIZE
        with tempfile.TemporaryDirectory() as tmp:
            folder = Path(tmp)
            reference = Image.new("RGBA", BRANDING_SIZE, (0, 0, 0, 0))
            observed = Image.new("RGBA", BRANDING_SIZE, (0, 0, 0, 255))
            reference.save(folder / "ref.png")
            observed.save(folder / "observed.png")
            measured = compare(
                folder / "ref.png", folder / "observed.png",
                folder / "result", expected_size=BRANDING_SIZE,
            )
            # The old RGB-only comparator falsely reported a perfect match.
            self.assertEqual(0, measured["mean_error_0_to_255"])
            self.assertEqual(255, measured["alpha_mae_0_to_255"])
            self.assertEqual(63.75, measured["rgba_mae_0_to_255"])
            self.assertEqual(0, measured["rgba_within_12_pct"])
            self.assertGreater(measured["navy_composited_rgb_mae"], 0)

    def test_identical_transparent_branding_has_zero_alpha_error(self):
        from pixel_compare import BRANDING_SIZE
        with tempfile.TemporaryDirectory() as tmp:
            folder = Path(tmp)
            reference = Image.new("RGBA", BRANDING_SIZE, (0, 0, 0, 0))
            reference.putpixel((500, 500), (1, 210, 240, 255))
            reference.save(folder / "logo.png")
            measured = compare(
                folder / "logo.png", folder / "logo.png",
                folder / "result", expected_size=BRANDING_SIZE,
            )
            self.assertEqual(0, measured["alpha_mae_0_to_255"])
            self.assertEqual(0, measured["rgba_mae_0_to_255"])
            self.assertEqual(100, measured["rgba_within_12_pct"])
            self.assertEqual(0, measured["navy_composited_rgb_mae"])

    def test_volatile_mask_measures_stable_content_without_hiding_global_error(self):
        with tempfile.TemporaryDirectory() as tmp:
            folder = Path(tmp)
            reference = Image.new("RGB", REFERENCE_SIZE, (0, 0, 0))
            observed = reference.copy()
            observed.paste((90, 0, 0), (0, 0, 80, 80))
            mask = Image.new("L", REFERENCE_SIZE, 0)
            mask.paste(255, (0, 0, 80, 80))
            reference.save(folder / "ref.png")
            observed.save(folder / "actual.png")
            mask.save(folder / "volatile.png")
            measured = compare(
                folder / "ref.png", folder / "actual.png",
                folder / "result", volatile_mask=folder / "volatile.png",
            )
            self.assertEqual(0, measured["stable_region_mae"])
            self.assertGreater(measured["mean_error_0_to_255"], 0)

    def test_rejects_mask_excluding_every_pixel(self):
        with tempfile.TemporaryDirectory() as tmp:
            folder = Path(tmp)
            Image.new("RGB", REFERENCE_SIZE).save(folder / "ref.png")
            Image.new("L", REFERENCE_SIZE, 255).save(folder / "all.png")
            with self.assertRaisesRegex(ValueError, "entire image"):
                compare(
                    folder / "ref.png", folder / "ref.png",
                    folder / "result", volatile_mask=folder / "all.png",
                )

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
