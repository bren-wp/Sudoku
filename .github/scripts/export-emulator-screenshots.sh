#!/usr/bin/env bash
set -euo pipefail

OUT="$GITHUB_WORKSPACE/screenshots"
mkdir -p "$OUT"
for picture in 01-home 02-new-game 03-game 04-settings 05-daily 06-statistics 07-achievements; do
  if adb exec-out run-as com.brendigo.sudoku cat "files/screenshots/$picture.png" > "$OUT/$picture.png" 2>/dev/null; then
    test -s "$OUT/$picture.png" || rm -f "$OUT/$picture.png"
  else
    rm -f "$OUT/$picture.png"
  fi
done

if ! find "$OUT" -maxdepth 1 -name '*.png' -size +0c -print -quit | grep -q .; then
  adb shell am start -n com.brendigo.sudoku/.MainActivity
  sleep 3
  adb exec-out screencap -p > "$OUT/01-home-fallback.png"
fi
test "$(find "$OUT" -maxdepth 1 -name '*.png' -size +0c | wc -l)" -ge 1
