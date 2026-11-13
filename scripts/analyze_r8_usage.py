#!/usr/bin/env python3
"""Summarize actual R8 usage.txt without treating its line count as dead-code count.

R8 usage lists removed classes and members. Mapping lists retained/renamed
classes; neither alone proves semantic dead code or supports deleting source.
Only the app's own removed symbols are exported verbatim.
"""
from __future__ import annotations

import argparse
from collections import Counter
from datetime import datetime, timezone
import json
from pathlib import Path
import re

APP = "com.brendigo.sudoku."


def category(classname: str) -> str:
    if classname.startswith(APP):
        return "brendigo_application"
    if classname.startswith("androidx.compose."):
        return "androidx_compose"
    if classname.startswith("androidx."):
        return "other_androidx"
    if classname.startswith("kotlin.") or classname.startswith("kotlinx."):
        return "kotlin_or_coroutines"
    if classname.startswith("com.google."):
        return "google"
    return "other_dependency"


def analyze(usage: Path, mapping: Path) -> tuple[dict, list[str]]:
    if not usage.is_file() or not mapping.is_file():
        raise FileNotFoundError("Both R8 usage.txt and mapping.txt are mandatory")
    text = usage.read_text(encoding="utf-8", errors="replace")
    if not text.strip():
        raise ValueError("R8 usage.txt is empty")
    top_level = re.compile(r"^[\w$][\w.$/-]*:?$")
    removed_classes = Counter()
    removed_members = Counter()
    samples = []
    current_class = ""
    current_kind = ""
    unknown_lines = 0
    for line in text.splitlines():
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        if line[:1].isspace():
            if not current_class:
                unknown_lines += 1
                continue
            removed_members[category(current_class)] += 1
            if current_class.startswith(APP):
                samples.append(current_class + " :: " + line.strip())
        elif top_level.fullmatch(line.strip()):
            current_class = line.strip().rstrip(":")
            current_kind = category(current_class)
            removed_classes[current_kind] += 1
            if current_class.startswith(APP):
                samples.append(current_class)
        else:
            unknown_lines += 1
            current_class = ""
            current_kind = ""
    mapping_lines = mapping.read_text(encoding="utf-8", errors="replace").splitlines()
    mappings = sum(bool(re.match(r"^\S.+ -> \S+:$", line)) for line in mapping_lines)
    if not mappings:
        raise ValueError("No retained R8 mappings found")
    return {
        "generated_at_utc": datetime.now(timezone.utc).isoformat(timespec="seconds"),
        "r8_input_usage_bytes": usage.stat().st_size,
        "r8_usage_total_lines": len(text.splitlines()),
        "r8_retained_class_mapping_entries": mappings,
        "removed_class_entries": dict(sorted(removed_classes.items())),
        "removed_member_entries": dict(sorted(removed_members.items())),
        "removed_class_total": sum(removed_classes.values()),
        "removed_member_total": sum(removed_members.values()),
        "unparsed_lines": unknown_lines,
        "app_removed_symbol_count": len(samples),
        "app_removed_symbol_examples": samples[:100],
        "interpretation": (
            "These symbols are listed by R8 as removed from this release variant. "
            "It does not prove that matching source declarations can be deleted "
            "from all build variants or that runtime reflection is unaffected."
        ),
    }, samples


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--mapping-dir", type=Path, required=True)
    parser.add_argument("--json", type=Path, required=True)
    parser.add_argument("--app-symbols", type=Path, required=True)
    args = parser.parse_args()
    report, symbols = analyze(args.mapping_dir / "usage.txt",
                              args.mapping_dir / "mapping.txt")
    args.json.parent.mkdir(parents=True, exist_ok=True)
    args.json.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n")
    args.app_symbols.write_text("\n".join(symbols) + ("\n" if symbols else ""))
    print(json.dumps({k: v for k, v in report.items()
                      if k not in ("app_removed_symbol_examples", "interpretation")},
                     ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
