#!/usr/bin/env bash
set -euo pipefail
OUT="$GITHUB_WORKSPACE/screenshots"
mkdir -p "$OUT"
adb pull /sdcard/Pictures/SudokuQA/. "$OUT/"
count=0
if [[ "${1:-}" == "--splash-home-only" ]]; then
  pictures=(01-home 02-new-game 08-splash)
else
  pictures=(01-home 02-new-game 03-game 04-settings 05-daily 06-statistics 07-achievements 08-splash 09-wordmark 10-launcher-icon 11-launcher-monochrome)
fi
for picture in "${pictures[@]}"; do
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
if [[ "${1:-}" == "--reference-capture" ]]; then
  # A separate capture keeps the fresh-install Home intact. A valid 9x9
  # session must be started by the test itself to expose the Resume card.
  resume="$OUT/01-home-resume.png"
  test -s "$resume" || { echo "::error::Missing resume-state Home PNG"; exit 1; }
  actual="$(od -An -N8 -tx1 "$resume" | tr -d ' \\n')"
  [[ "$actual" == "89504e470d0a1a0a" ]] || {
    echo "::error::Invalid resume-state Home PNG"; exit 1;
  }
  count=$((count + 1))
  # These are separate TEST-ONLY deterministic-state captures. They must
  # never replace the ten authentic live-state reference screenshots.
  for fixture in fixture-03-game fixture-06-statistics fixture-07-achievements; do
    file="$OUT/$fixture.png"
    test -s "$file" || { echo "::error::Missing isolated-state $fixture PNG"; exit 1; }
    signature="$(od -An -N8 -tx1 "$file" | tr -d ' \\n')"
    [[ "$signature" == "89504e470d0a1a0a" ]] || {
      echo "::error::Invalid isolated-state PNG $fixture"; exit 1;
    }
    dimensions="$(python3 - "$file" <<'PY'
import struct, sys
with open(sys.argv[1], "rb") as handle:
    handle.seek(16)
    print("x".join(map(str, struct.unpack(">II", handle.read(8)))))
PY
)"
    [[ "$dimensions" == "941x1672" ]] || {
      echo "::error::Wrong isolated-state reference resolution: $fixture ($dimensions)"; exit 1;
    }
    count=$((count + 1))
  done
fi
# A separate, standard-library PNG verifier checks full chunk CRCs, SHA-256,
# exact reference-screen geometry and transparent branding. The generated
# capture-evidence.json lives beside the true Android screenshots in Actions.
case "${1:-}" in
  --splash-home-only) capture_mode=focused ;;
  --reference-capture) capture_mode=reference ;;
  *) capture_mode=full ;;
esac
python3 scripts/validate_android_capture.py --dir "$OUT" --mode "$capture_mode"
echo "Exported and verified $count real PNG screens from Android emulator"
