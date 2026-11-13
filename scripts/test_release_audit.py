"""Offline tests for production release evidence; no Android SDK needed."""
import json
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

from audit_android_release import inspect_sbom, inspect_r8, inspect_permissions


class ReleaseSecurityAuditTest(unittest.TestCase):
    def test_r8_mapping_required_and_reports_reachable_classes(self):
        with tempfile.TemporaryDirectory() as temp:
            folder = Path(temp)
            with self.assertRaisesRegex(ValueError, "mapping"):
                inspect_r8(folder)
            (folder / "mapping.txt").write_text(
                "com.brendigo.sudoku.MainActivity -> a:\n"
                "    void onCreate() -> b\n"
                "com.brendigo.sudoku.GamePage -> c:\n" + ("# metadata\n" * 30),
                encoding="utf-8",
            )
            (folder / "usage.txt").write_text("com.example.DeadClass\n")
            report = inspect_r8(folder)
            self.assertEqual(2, report["r8_class_mapping_entries"])
            self.assertTrue(report["r8_unused_code_report_present"])
            self.assertEqual(1, report["r8_usage_report_lines"])

    def test_sbom_must_include_release_maven_dependencies(self):
        with tempfile.TemporaryDirectory() as temp:
            path = Path(temp) / "bom.json"
            path.write_text(json.dumps({"bomFormat": "CycloneDX",
                                        "specVersion": "1.6", "components": []}))
            with self.assertRaisesRegex(ValueError, "dependencies"):
                inspect_sbom(path)
            path.write_text(json.dumps({
                "bomFormat": "CycloneDX", "specVersion": "1.6",
                "components": [
                    {"type": "library", "group": "androidx.compose.ui",
                     "name": "ui-android", "version": "1.8.0",
                     "purl": "pkg:maven/androidx.compose.ui/ui-android@1.8.0"}
                ]
            }))
            report = inspect_sbom(path)
            self.assertEqual(1, report["resolved_maven_modules"])
            self.assertFalse(report["dependency_database_scanned"])

    def test_test_and_advertising_sdk_must_not_enter_release_sbom(self):
        with tempfile.TemporaryDirectory() as temp:
            path = Path(temp) / "bom.json"
            for group in ("androidx.test", "junit", "com.google.android.gms.ads"):
                path.write_text(json.dumps({
                    "bomFormat": "CycloneDX", "specVersion": "1.6",
                    "components": [
                        {"group": group, "name": "sample", "version": "1.0",
                         "purl": f"pkg:maven/{group}/sample@1.0"}
                    ],
                }))
                with self.assertRaisesRegex(ValueError, "Unexpected test/ads"):
                    inspect_sbom(path)

    def test_apk_must_have_no_internet_or_sensitive_permissions(self):
        with tempfile.TemporaryDirectory() as temp:
            apk = Path(temp) / "release.apk"
            apk.write_bytes(b"APK" * 500)
            with patch("audit_android_release.subprocess.run",
                       return_value=subprocess.CompletedProcess(
                           ["aapt2"], 0, stdout="package: com.brendigo.sudoku\n",
                           stderr="")):
                report = inspect_permissions("aapt2", apk)
                self.assertEqual([], report["declared_android_permissions"])
            with patch("audit_android_release.subprocess.run",
                       return_value=subprocess.CompletedProcess(
                           ["aapt2"], 0,
                           stdout="uses-permission: name='android.permission.INTERNET'\n",
                           stderr="")):
                with self.assertRaisesRegex(ValueError, "INTERNET"):
                    inspect_permissions("aapt2", apk)


if __name__ == "__main__":
    unittest.main()
