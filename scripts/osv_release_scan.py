#!/usr/bin/env python3
"""Query OSV.dev for EVERY resolved Maven component in the release CycloneDX SBOM.

Fail closed on incomplete/malformed API answers. A known-vulnerable Maven canary
confirms OSV is not silently answering empty results to all Maven queries.
A clean OSV result means "no OSV advisories matched at scan time", not a
guarantee that the dependency has no undiscovered security flaws.
"""
from __future__ import annotations

import argparse
from datetime import datetime, timezone
import csv
import hashlib
import json
from pathlib import Path
import time
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

API = "https://api.osv.dev/v1/"
CANARY = {"package": {"ecosystem": "Maven",
                      "name": "org.apache.logging.log4j:log4j-core"},
          "version": "2.14.1"}


def fetch_json(url: str, payload: dict | None = None) -> dict:
    data = json.dumps(payload).encode("utf-8") if payload is not None else None
    for attempt in range(4):
        try:
            request = Request(
                url, data=data,
                headers={"Accept": "application/json",
                         "User-Agent": "Brendigo-Sudoku-release-audit/1.0",
                         **({"Content-Type": "application/json"} if data else {})},
                method="POST" if data else "GET",
            )
            with urlopen(request, timeout=50) as response:
                parsed = json.load(response)
            if not isinstance(parsed, dict):
                raise ValueError("OSV returned a non-object JSON document")
            return parsed
        except (HTTPError, URLError, TimeoutError, OSError) as error:
            if isinstance(error, HTTPError) and error.code not in (429, 500, 502, 503, 504):
                raise
            if attempt == 3:
                raise RuntimeError(f"OSV request failed after retries: {type(error).__name__}") from error
            time.sleep(2 ** attempt)
    raise AssertionError("unreachable")


def sbom_components(path: Path) -> tuple[list[dict], str]:
    data = path.read_bytes()
    bom = json.loads(data)
    if bom.get("bomFormat") != "CycloneDX":
        raise ValueError("Expected a CycloneDX BOM")
    components = bom.get("components")
    if not isinstance(components, list) or not components:
        raise ValueError("Resolved release SBOM is empty")
    result = []
    for item in components:
        group = item.get("group")
        name = item.get("name")
        version = item.get("version")
        purl = item.get("purl")
        if not all(isinstance(x, str) and x for x in (group, name, version, purl)):
            raise ValueError("An SBOM component has missing Maven coordinates")
        if not purl.startswith("pkg:maven/"):
            raise ValueError("Unexpected non-Maven release dependency: " + purl)
        result.append({"group": group, "artifact": name, "version": version,
                       "purl": purl, "maven_name": group + ":" + name})
    if len({(x["maven_name"], x["version"]) for x in result}) != len(result):
        raise ValueError("Duplicate Maven release component in SBOM")
    return result, hashlib.sha256(data).hexdigest()


def query_all(queries: list[dict], api=fetch_json) -> list[list[dict]]:
    """Follow all OSV per-package page tokens; no partial reports on API failure."""
    matches: list[list[dict]] = [[] for _ in queries]
    remaining = [(i, query, None) for i, query in enumerate(queries)]
    seen_tokens: set[tuple[int, str]] = set()
    while remaining:
        chunk = remaining[:100]
        remaining = remaining[100:]
        payload = {"queries": [
            dict(query, **({"page_token": token} if token else {}))
            for _, query, token in chunk
        ]}
        response = api(API + "querybatch", payload)
        results = response.get("results")
        if not isinstance(results, list) or len(results) != len(chunk):
            raise ValueError("OSV batch did not return one result for every package")
        for (idx, query, _), result in zip(chunk, results):
            if not isinstance(result, dict):
                raise ValueError("Malformed OSV batch entry")
            vulns = result.get("vulns", [])
            if not isinstance(vulns, list):
                raise ValueError("Malformed OSV vulnerability list")
            for vulnerability in vulns:
                if not isinstance(vulnerability, dict) or not vulnerability.get("id"):
                    raise ValueError("OSV record has no vulnerability ID")
                matches[idx].append(vulnerability)
            token = result.get("next_page_token")
            if token:
                if not isinstance(token, str) or (idx, token) in seen_tokens:
                    raise ValueError("Invalid or repeated OSV pagination token")
                seen_tokens.add((idx, token))
                remaining.append((idx, query, token))
            if len(matches[idx]) > 5000:
                raise ValueError("OSV response exceeded protective per-package limit")
    return matches


def scan(path: Path, api=fetch_json) -> dict:
    components, digest = sbom_components(path)
    canary = query_all([CANARY], api)[0]
    if not canary:
        raise RuntimeError("OSV Maven positive control returned zero results; cannot trust the scan")
    queries = [{"package": {"ecosystem": "Maven", "name": p["maven_name"]},
                "version": p["version"]} for p in components]
    found = query_all(queries, api)
    vuln_ids = sorted({v["id"] for item in found for v in item})
    advisories = {}
    for vid in vuln_ids:
        from urllib.parse import quote
        item = api(API + "vulns/" + quote(vid, safe=""))
        if item.get("id") != vid:
            raise ValueError("Mismatched OSV advisory detail for " + vid)
        advisories[vid] = {
            "id": vid,
            "aliases": sorted(set(item.get("aliases") or [])),
            "summary": (item.get("summary") or "")[:600],
            "modified": item.get("modified"),
            "withdrawn": item.get("withdrawn"),
            "url": "https://osv.dev/vulnerability/" + vid,
        }
    rows = []
    for component, vulnerabilities in zip(components, found):
        ids = sorted({v["id"] for v in vulnerabilities})
        aliases = sorted({a for vid in ids for a in advisories[vid]["aliases"]
                          if a.startswith("CVE-")})
        rows.append(dict(component, osv_ids=ids, cve_aliases=aliases,
                         matched_advisories=len(ids),
                         status="MATCHED" if ids else "NO_MATCH_IN_OSV"))
    return {
        "source": "https://api.osv.dev/v1/querybatch",
        "scanned_at_utc": datetime.now(timezone.utc).isoformat(timespec="seconds"),
        "source_sbom_sha256": digest,
        "scope": "all exact Maven versions in releaseRuntimeClasspath",
        "positive_control": "org.apache.logging.log4j:log4j-core:2.14.1",
        "positive_control_verified": True,
        "component_count": len(rows),
        "queried_component_count": len(found),
        "components_with_matches": sum(bool(x["osv_ids"]) for x in rows),
        "distinct_osv_advisories": len(advisories),
        "advisories": list(advisories.values()),
        "components": rows,
        "limitation": "OSV matching at scan time; no proof of absence of unknown or non-indexed vulnerabilities.",
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--sbom", type=Path, required=True)
    parser.add_argument("--report", type=Path, required=True)
    parser.add_argument("--csv", type=Path, required=True)
    parser.add_argument("--fail-on-findings", action="store_true")
    args = parser.parse_args()
    report = scan(args.sbom)
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n")
    with args.csv.open("w", newline="", encoding="utf-8") as output:
        writer = csv.DictWriter(output, fieldnames=[
            "maven_name", "version", "purl", "status", "osv_ids", "cve_aliases",
        ])
        writer.writeheader()
        for row in report["components"]:
            writer.writerow({key: ", ".join(row[key]) if isinstance(row[key], list) else row[key]
                             for key in writer.fieldnames})
    print(f"OSV: verified {report['queried_component_count']}/{report['component_count']} "
          f"release dependencies; {report['components_with_matches']} package versions "
          f"have {report['distinct_osv_advisories']} distinct OSV advisories.")
    for component in report["components"]:
        if component["osv_ids"]:
            print(component["maven_name"], component["version"], component["osv_ids"])
    return 2 if args.fail_on_findings and report["components_with_matches"] else 0


if __name__ == "__main__":
    raise SystemExit(main())
