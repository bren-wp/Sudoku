"""Offline deterministic regression tests for OSV and R8 release scanners."""
from pathlib import Path
import json
import tempfile
import unittest

from osv_release_scan import CANARY, query_all, scan
from analyze_r8_usage import analyze, category


class OsvAuditTests(unittest.TestCase):
    def test_batch_checks_every_resolved_maven_version(self):
        with tempfile.TemporaryDirectory() as temp:
            bom = Path(temp) / "bom.json"
            bom.write_text(json.dumps({
                "bomFormat": "CycloneDX", "specVersion": "1.6",
                "components": [
                    {"group": "androidx.activity", "name": "activity-compose",
                     "version": "1.10.1",
                     "purl": "pkg:maven/androidx.activity/activity-compose@1.10.1"},
                    {"group": "androidx.compose.ui", "name": "ui",
                     "version": "1.8.0",
                     "purl": "pkg:maven/androidx.compose.ui/ui@1.8.0"},
                ],
            }))
            calls = []
            def fake_api(url, payload=None):
                if url.endswith("querybatch"):
                    qs = payload["queries"]
                    calls.extend(qs)
                    if qs == [CANARY]:
                        return {"results": [{"vulns": [{"id": "GHSA-canary"}]}]}
                    if len(qs) == 2:
                        return {"results": [
                            {"vulns": [{"id": "GHSA-example"}]}, {"vulns": []}
                        ]}
                    raise AssertionError("Unexpected queries")
                if url.endswith("GHSA-example"):
                    return {"id": "GHSA-example", "aliases": ["CVE-2026-12345"],
                            "summary": "Test advisory"}
                raise AssertionError("Unknown URL " + url)
            report = scan(bom, fake_api)
            self.assertEqual(2, report["component_count"])
            self.assertEqual(2, report["queried_component_count"])
            self.assertTrue(report["positive_control_verified"])
            self.assertEqual(1, report["components_with_matches"])
            self.assertEqual(["CVE-2026-12345"],
                             report["components"][0]["cve_aliases"])
            self.assertEqual("NO_MATCH_IN_OSV",
                             report["components"][1]["status"])
            self.assertEqual("1.10.1", calls[1]["version"])
            self.assertEqual("1.8.0", calls[2]["version"])

    def test_known_vulnerable_canary_must_not_silently_return_empty(self):
        with tempfile.TemporaryDirectory() as temp:
            bom = Path(temp) / "bom.json"
            bom.write_text(json.dumps({"bomFormat": "CycloneDX", "components": [
                {"group": "androidx.compose", "name": "runtime", "version": "1",
                 "purl": "pkg:maven/androidx.compose/runtime@1"}
            ]}))
            with self.assertRaisesRegex(RuntimeError, "positive control"):
                scan(bom, lambda url, payload: {"results": [{"vulns": []}]})

    def test_querybatch_rejects_partial_and_follows_pagination(self):
        query = {"package": {"ecosystem": "Maven", "name": "example:lib"},
                 "version": "1.0"}
        def incomplete(_url, _payload):
            return {"results": []}
        with self.assertRaisesRegex(ValueError, "one result for every"):
            query_all([query], incomplete)
        def with_pages(_url, payload):
            first = payload["queries"][0].get("page_token")
            if first:
                self.assertEqual("page-2", first)
                return {"results": [{"vulns": [{"id": "GHSA-last"}]}]}
            return {"results": [{"vulns": [{"id": "GHSA-first"}],
                                 "next_page_token": "page-2"}]}
        self.assertEqual(["GHSA-first", "GHSA-last"],
                         [v["id"] for v in query_all([query], with_pages)[0]])


class R8AuditTests(unittest.TestCase):
    def test_categories_and_app_removed_symbols(self):
        self.assertEqual("brendigo_application",
                         category("com.brendigo.sudoku.data.UnusedController"))
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            (root / "usage.txt").write_text(
                "com.brendigo.sudoku.data.UnusedController\n"
                "    void oldMethod()\n"
                "androidx.compose.ui.UnusedNode\n"
                "    int value\n"
                "kotlin.collections.UnusedHelper\n",
                encoding="utf-8")
            (root / "mapping.txt").write_text(
                "com.brendigo.sudoku.MainActivity -> a:\n"
                "androidx.compose.runtime.Composer -> b:\n")
            report, symbols = analyze(root / "usage.txt", root / "mapping.txt")
            self.assertEqual(3, report["removed_class_total"])
            self.assertEqual(2, report["removed_member_total"])
            self.assertEqual(2, report["r8_retained_class_mapping_entries"])
            self.assertEqual(2, report["app_removed_symbol_count"])
            self.assertTrue(any("oldMethod" in item for item in symbols))


if __name__ == "__main__":
    unittest.main()
