"""Regression of unscaled same-state pixel comparison: RGB exactness and MAE."""
import tempfile
import unittest
import warnings
from pathlib import Path

from PIL import Image

from compare_visual_iteration import compare_one, measure_rgb


class VisualIterationTests(unittest.TestCase):
    def test_exact_is_not_rounded_mae(self):
        source = Image.new("RGB", (4, 4), (10, 20, 30))
        changed = source.copy()
        changed.putpixel((3, 3), (11, 20, 30))
        result = measure_rgb(source, changed)
        self.assertEqual(15, result["exact_rgb_pixels"])
        self.assertFalse(result["identical"])
        self.assertGreater(result["mae_0_to_255"], 0)

    def test_exact_pixel_count_does_not_emit_pillow_deprecation(self):
        reference = Image.new("RGB", (2, 2), (0, 0, 0))
        actual = reference.copy()
        actual.putpixel((0, 0), (1, 0, 0))
        with warnings.catch_warnings():
            warnings.simplefilter("error", DeprecationWarning)
            result = measure_rgb(reference, actual)
        self.assertEqual(3, result["exact_rgb_pixels"])
        self.assertEqual(3, result["exact_rgba_pixels"])

    def test_alpha_only_difference_is_not_claimed_pixel_identical(self):
        reference = Image.new("RGBA", (4, 4), (0, 20, 80, 0))
        actual = reference.copy()
        actual.putpixel((2, 3), (0, 20, 80, 1))
        result = measure_rgb(reference, actual)
        self.assertEqual(16, result["exact_rgb_pixels"])
        self.assertEqual(15, result["exact_rgba_pixels"])
        self.assertGreater(result["alpha_mae_0_to_255"], 0)
        self.assertGreater(result["rgba_mae_0_to_255"], 0)
        self.assertFalse(result["identical"])

    def test_compare_one_preserves_branding_alpha_across_full_and_roi(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root)
            original = Image.new("RGBA", (6, 6), (20, 40, 60, 0))
            before = original.copy()
            before.putpixel((2, 5), (20, 40, 60, 255))
            for name, image in (
                ("r.png", original), ("b.png", before), ("a.png", original)
            ):
                image.save(path / name)
            report = compare_one(path / "r.png", path / "b.png",
                                 path / "a.png", bottom_start=.5)
            self.assertEqual(100, report["before_exact_rgb_percent"])
            self.assertEqual(100, report["after_exact_rgb_percent"])
            self.assertLess(report["before_exact_rgba_percent"], 100)
            self.assertEqual(100, report["after_exact_rgba_percent"])
            self.assertGreater(report["before_alpha_mae"], 0)
            self.assertEqual(0, report["after_alpha_mae"])
            self.assertLess(report["alpha_mae_delta"], 0)
            self.assertFalse(report["before_exact_1_to_1"])
            self.assertTrue(report["after_exact_1_to_1"])

    def test_fixed_screen_regions_report_real_nonmasked_regressions(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root)
            original = Image.new("RGB", (6, 6), (0, 0, 0))
            before = original.copy()
            after = original.copy()
            before.putpixel((0, 0), (70, 0, 0))
            after.putpixel((0, 0), (10, 0, 0))
            after.putpixel((5, 5), (120, 0, 0))
            for name, image in (
                ("r.png", original), ("b.png", before), ("a.png", after)
            ):
                image.save(path / name)
            report = compare_one(
                path / "r.png", path / "b.png", path / "a.png",
                bottom_start=.5,
                regions={"header": (0, 0, 3, 3), "footer": (3, 3, 6, 6)},
            )
            sections = {row["region"]: row for row in report["region_rows"]}
            self.assertLess(sections["header"]["rgb_mae_delta"], 0)
            self.assertGreater(sections["footer"]["rgb_mae_delta"], 0)
            self.assertEqual(2, len(report["region_rows"]))
            self.assertFalse(report["after_exact_1_to_1"])

    def test_invalid_region_coordinates_fail_instead_of_clipping(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root)
            original = Image.new("RGB", (5, 5))
            for name in ("r.png", "b.png", "a.png"):
                original.save(path / name)
            with self.assertRaises(ValueError):
                compare_one(
                    path / "r.png", path / "b.png", path / "a.png",
                    bottom_start=.5, regions={"invalid": (0, 0, 6, 5)}
                )

    def test_region_reports_improvement_without_faking_full_match(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root)
            original = Image.new("RGB", (10, 10), (0, 0, 0))
            before = original.copy()
            after = original.copy()
            for y in range(7, 10):
                for x in range(10):
                    before.putpixel((x, y), (80, 0, 0))
                    after.putpixel((x, y), (20, 0, 0))
            original.save(path / "r.png")
            before.save(path / "b.png")
            after.save(path / "a.png")
            result = compare_one(path / "r.png", path / "b.png",
                                 path / "a.png", bottom_start=.7)
            self.assertLess(result["bottom_mae_delta"], 0)
            self.assertLess(result["full_mae_delta"], 0)
            self.assertFalse(result["after_exact_1_to_1"])

    def test_rejects_different_sizes_instead_of_rescaling(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root)
            for filename, size in [("r.png", (8, 8)), ("b.png", (8, 8)),
                                   ("a.png", (9, 8))]:
                Image.new("RGB", size, (0, 0, 0)).save(path / filename)
            with self.assertRaises(ValueError):
                compare_one(path / "r.png", path / "b.png",
                            path / "a.png", bottom_start=.79)


if __name__ == "__main__":
    unittest.main()
