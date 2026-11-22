"""Regression contracts for post-merge 11-lane visual QA cancellation policy."""
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
WORKFLOW = ROOT / ".github/workflows/visual-smoke.yml"
RELEASE_GATE = ROOT / "scripts/require-release-ci-gates.sh"


class MainVisualSmokeContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.source = WORKFLOW.read_text(encoding="utf-8")
        cls.release_gate = RELEASE_GATE.read_text(encoding="utf-8")

    def test_main_pushes_do_not_cancel_each_other(self):
        self.assertRegex(
            self.source,
            r"(?m)^  cancel-in-progress: \$\{\{ github\.event_name == 'pull_request' \}\}$",
        )
        self.assertNotIn("cancel-in-progress: true", self.source)
        self.assertRegex(
            self.source,
            r"(?m)^  push:\n    branches: \[main\]\n  pull_request:",
        )

    def test_all_named_release_gate_lanes_exist(self):
        expected = {
            "reference", "compact", "narrow", "landscape",
            "tablet-shard-0", "tablet-shard-1",
            "tablet-shard-2", "tablet-shard-3",
            "tablet-capture", "tiny", "medium",
        }
        actual = re.findall(r"(?m)^          - layout: ([a-z0-9-]+)\s*$", self.source)
        self.assertEqual(set(actual), expected)
        self.assertEqual(len(actual), len(expected))
        self.assertIn("fail-fast: false", self.source)
        self.assertIn("matrix:", self.source)
        self.assertIn(' -ne 11 ]]', self.release_gate)
        for layout in expected:
            self.assertIn(layout, self.release_gate)


if __name__ == "__main__":
    unittest.main()
