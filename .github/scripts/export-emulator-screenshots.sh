#!/usr/bin/env bash
set -euo pipefail
OUT="$GITHUB_WORKSPACE/screenshots"
mkdir -p "$OUT"
adb pull /sdcard/Pictures/SudokuQA/. "$OUT/"
count=0
for picture in 01-home 02-new-game 03-game 04-settings 05-daily 06-statistics 07-achievements 08-splash 09-wordmark 10-launcher-icon 11-launcher-monochrome; do
  path="$OUT/$picture.png"
  test -s "$path" || { echo "::error::Missing $picture screenshot"; exit 1; }
  actual="$(od -An -N8 -tx1 "$path" | tr -d ' \n')"
  if [[ "$actual" != "89504e470d0a1a0a" ]]; then
    echo "::error::Not a valid PNG: $picture"
    exit 1
  fi
  if [[ "$picture" == "09-wordmark" || "$picture" == "10-launcher-icon" || "$picture" == "11-launcher-monochrome" ]]; then
    # Branding assets are real instrumented PNG renders at 1254².
    dimensions="$(python3 - "$path" <<'PY'
import struct, sys
with open(sys.argv[1], "rb") as handle:
    handle.seek(16)
    print("x".join(map(str, struct.unpack(">II", handle.read(8)))))
PY
)"
    [[ "$dimensions" == "1254x1254" ]] || {
      echo "::error::Wrong branding QA resolution: $picture ($dimensions)"; exit 1;
    }
  fi
  count=$((count + 1))
done
echo "Exported and verified $count real PNG screens from Android emulator"
