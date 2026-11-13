#!/usr/bin/env python3
"""Fail-closed release evidence for R8, Gradle-resolved SBOM and APK permissions.

The report does NOT certify that components are free of known CVEs: the OSV
database must be queried separately. No secrets, keystores or private files
are read or printed.
"""
from __future__ import annotations

import argparse
import json
from pathlib import Path
import re
import shutil
import subprocess

FORBIDDEN_PERMISSIONS = (
    "android.permission.INTERNET",
    "android.permission.READ_CONTACTS",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.READ_SMS",
    "android.permission.QUERY_ALL_PACKAGES",
    "android.permission.POST_NOTIFICATIONS",
)
FORBIDDEN_RUNTIME_GROUPS = ("androidx.test", "junit")
FORBIDDEN_RUNTIME_PREFIXES = ("com.google.android.gms.ads",)


def inspect_sbom(path: Path) -> dict:
    document = json.loads(path.read_text(encoding="utf-8"))
    if document.get("bomFormat") != "CycloneDX" or not document.get("specVersion"):
        raise ValueError("Not a CycloneDX SBOM")
    components = document.get("components")
    if not isinstance(components, list) or not components:
        raise ValueError("SBOM must contain resolved release dependencies")
    modules = []
    for component in components:
        if not isinstance(component, dict):
            raise ValueError("Invalid CycloneDX component")
        group = component.get("group", "")
        name = component.get("name", "")
        version = component.get("version", "")
        purl = component.get("purl", "")
        if not (name and version and purl.startswith("pkg:maven/")):
            raise ValueError(f"Component lacks resolved Maven coordinates: {name}")
        if group in FORBIDDEN_RUNTIME_GROUPS or any(
            group.startswith(prefix) for prefix in FORBIDDEN_RUNTIME_PREFIXES
        ):
            raise ValueError(f"Unexpected test/ads dependency in release: {group}:{name}")
        modules.append((group, name, version))
    return {
        "sbom_format": f"CycloneDX {document['specVersion']}",
        "resolved_maven_modules": len(modules),
        "distinct_maven_modules": len(set(modules)),
        "dependency_database_scanned": False,
        "cve_status": "NOT_EVALUATED_BY_THIS_OFFLINE_CHECK",
    }


def inspect_r8(mapping_dir: Path) -> dict:
    mapping = mapping_dir / "mapping.txt"
    if not mapping.is_file() or mapping.stat().st_size < 128:
        raise ValueError("Missing or empty R8 release mapping.txt")
    # R8's top-level class mappings have no indentation, unlike methods.
    class_count = sum(
        bool(re.match(r"^\\S.* -> [^ ]+:$", line))
        for line in mapping.read_text(encoding="utf-8", errors="replace").splitlines()
    )
    if class_count == 0:
        raise ValueError("R8 mapping has no class remappings")
    usage = mapping_dir / "usage.txt"
    usage_count = (
        sum(1 for line in usage.open(encoding="utf-8", errors="replace"))
        if usage.is_file() else None
    )
    return {
        "r8_mapping_present": True,
        "r8_mapping_bytes": mapping.stat().st_size,
        "r8_class_mapping_entries": class_count,
        "r8_unused_code_report_present": usage.is_file(),
        "r8_usage_report_lines": usage_count,
    }


def inspect_permissions(aapt2: str, apk: Path) -> dict:
    if not apk.is_file() or apk.stat().st_size < 1024:
        raise ValueError("Missing release APK")
    result = subprocess.run(
        [aapt2, "dump", "permissions", str(apk)],
        text=True, capture_output=True, check=True, timeout=30,
    )
    permissions_text = result.stdout
    found = sorted(set(re.findall(r"android\\.permission\\.[A-Z_]+", permissions_text)))
    forbidden = sorted(set(found).intersection(FORBIDDEN_PERMISSIONS))
    if forbidden:
        raise ValueError(f"Unexpected release APK permissions: {', '.join(forbidden)}")
    return {
        "release_apk_bytes": apk.stat().st_size,
        "declared_android_permissions": found,
        "forbidden_permissions_found": forbidden,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="Auditable Sudoku Android release gates")
    parser.add_argument("--apk", type=Path, required=True)
    parser.add_argument("--mapping-dir", type=Path, required=True)
    parser.add_argument("--sbom", type=Path, required=True)
    parser.add_argument("--aapt2", default=shutil.which("aapt2"))
    parser.add_argument("--report", type=Path, required=True)
    args = parser.parse_args()
    if not args.aapt2:
        raise RuntimeError("Android aapt2 is mandatory for permission verification")
    report = {
        "project": "Sudoku by BRENDIGO",
        "audit_scope": "offline release artifact inspection",
        "r8": inspect_r8(args.mapping_dir),
        "dependencies": inspect_sbom(args.sbom),
        "apk": inspect_permissions(args.aapt2, args.apk),
        "security_certification": False,
        "pixel_perfect_certification": False,
    }
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(
        json.dumps(report, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
    )
    print(json.dumps(report, indent=2, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
