#!/usr/bin/env python3
"""Real Android runtime smoke of the R8-minified release APK, QA-signed only.

Verifies accessible Home -> New Game -> Gameplay -> Pause -> Resume and cold-start
recovery. It does NOT certify 1:1 screenshot parity with owner references.
"""
import argparse
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

BOUNDS = re.compile(r"^\[(\d+),(\d+)\]\[(\d+),(\d+)\]$")


def run(*args: str, timeout: int = 40) -> str:
    result = subprocess.run(
        list(args), check=True, capture_output=True, text=True, timeout=timeout
    )
    return result.stdout


def parse_visible_nodes(xml: str) -> list[tuple[str, tuple[int, int]]]:
    """Extract actual accessible labels and touch coordinates from UIAutomator."""
    root = ET.fromstring(xml)
    nodes = []
    for node in root.iter("node"):
        label = " ".join(filter(None, (
            node.get("content-desc", ""), node.get("text", "")
        ))).strip()
        match = BOUNDS.fullmatch(node.get("bounds", ""))
        if label and match:
            x1, y1, x2, y2 = map(int, match.groups())
            if x2 > x1 and y2 > y1:
                nodes.append((label, ((x1 + x2) // 2, (y1 + y2) // 2)))
    return nodes


def device_nodes() -> list[tuple[str, tuple[int, int]]]:
    run("adb", "shell", "uiautomator", "dump", "/sdcard/sudoku-release-ui.xml")
    return parse_visible_nodes(
        run("adb", "exec-out", "cat", "/sdcard/sudoku-release-ui.xml")
    )


def find_label(nodes: list[tuple[str, tuple[int, int]]], phrase: str):
    target = phrase.casefold()
    return next(((label, point) for label, point in nodes
                 if target in label.casefold()), None)


def wait_for(phrase: str, seconds: int = 90) -> tuple[int, int]:
    deadline = time.monotonic() + seconds
    recent = ""
    while time.monotonic() < deadline:
        try:
            nodes = device_nodes()
            match = find_label(nodes, phrase)
            if match:
                print(f"VISIBLE: {phrase!r} -> {match[0]!r}", flush=True)
                return match[1]
            recent = "; ".join(label for label, _ in nodes)[:600]
        except (subprocess.CalledProcessError, subprocess.TimeoutExpired,
                ET.ParseError) as exc:
            recent = str(exc)
        time.sleep(1.5)
    raise AssertionError(f"Not visible within {seconds}s: {phrase!r}; last: {recent}")


def tap(phrase: str, seconds: int = 90) -> None:
    x, y = wait_for(phrase, seconds)
    run("adb", "shell", "input", "tap", str(x), str(y))


def screenshot(folder: Path, name: str) -> None:
    folder.mkdir(parents=True, exist_ok=True)
    with (folder / name).open("wb") as output:
        subprocess.run(["adb", "exec-out", "screencap", "-p"],
                       stdout=output, check=True, timeout=40)


def start_app() -> None:
    run("adb", "shell", "am", "start", "-W", "-n",
        "com.brendigo.sudoku/.MainActivity", timeout=60)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--apk", type=Path, required=True)
    parser.add_argument("--screenshots", type=Path, default=Path("screenshots"))
    args = parser.parse_args()
    if not args.apk.is_file():
        parser.error(f"QA-signed release APK missing: {args.apk}")

    try:
        # CI emulator is disposable: debug APK signed with another key can
        # safely be uninstalled here, never on a user's actual Android device.
        subprocess.run(["adb", "uninstall", "com.brendigo.sudoku"],
                       capture_output=True, text=True, timeout=30)
        run("adb", "install", "-r", str(args.apk.resolve()), timeout=120)
        start_app()
        tap("Nova igra, Odaberi težinu")
        tap("Započni igru")
        wait_for("Unesi broj 1", seconds=100)
        screenshot(args.screenshots, "release-runtime-game.png")
        tap("Unesi broj 1")
        tap("Pauziraj ili nastavi igru")
        wait_for("Igra je pauzirana")
        tap("Nastavi")
        wait_for("Unesi broj 1")
        time.sleep(3)  # Allow the coalesced autosave to reach disk.
        run("adb", "shell", "am", "force-stop", "com.brendigo.sudoku")
        start_app()
        tap("Nastavi spremljenu Sudoku igru")
        wait_for("Unesi broj 1")
        screenshot(args.screenshots, "release-runtime-resumed.png")
        print("PASS: optimized release APK installed, played, paused, "
              "resumed and recovered a saved game after force-stop.", flush=True)
        return 0
    except Exception as exc:
        print(f"RELEASE RUNTIME SMOKE FAILED: {exc}", file=sys.stderr)
        try:
            screenshot(args.screenshots, "release-runtime-failure.png")
        except Exception:
            pass
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
