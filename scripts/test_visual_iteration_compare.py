"""Regression of unscaled same-state pixel comparison: RGB exactness and MAE."""
import tempfile
import unittest
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
