## 1.0.12

### UI and branding
- Stronger 7–3–9 adaptive and themed launcher assets and larger home/splash logo.
- Enriched Compose midnight scene, lake reflection, blossoms and neon card sheen.
- Tuned titles, statistics, daily header/weekly markers and achievement rows.
- Color-preserving difficulty cards with native circled leaf, star and crown icons.

### Functionality and stability
- “Odigrane igre” now displays started sessions, not finished sessions.
- New games select a central editable cell to make the keypad immediately usable.
- Contextual undo/erase/hint/keypad availability and brighter selected-cell border.
- Added emulator assertions for actions before/after a move, undo and notes.
- Preserves existing local saves and statistics; no new permissions.

### QA and limitations
- Builds must pass JVM tests, Lint, debug APK and release AAB, and emulator UI
  navigation/screenshot capture before merge. Package signing status is documented
  in each release.
- Original 941×1672 pixel-for-pixel equality **not yet established**. Issue #11
  remains open.

# Changelog

## 1.0.11

### Fixed
- On the Home and Daily Challenge screens, the displayed calendar, current
  streak and today's challenge availability refresh after the device crosses
  local midnight, without requiring navigation away and back.
- The refresh also checks for manual device clock/time-zone changes at least
  once per minute, including across daylight-saving transitions.
- Removed unused `HomeTile` icon arguments; visible icons remain native
  `BrandIcon` vectors and screen navigation behavior is unchanged.

### Quality
- Added deterministic JVM tests for midnight rollover, Europe/Zagreb
  daylight-saving time, calendar year rollover and periodic refresh bounds.
- Preserved existing game saves, statistics, daily progress and settings.
- Pixel-perfect matching against the supplied 941×1672 references remains
  unverified, tracked in GitHub issue #11.
- APK/AAB publication is gated on successful GitHub Actions checks. Without
  publisher signing secrets, files are test APK and unsigned AAB.

## 1.0.10
- Refined vector mountain, moonlit pagoda, blossom and lake scenery.
- Corrected Android 13+ monochrome themed launcher icon alpha mask.
- Added native hearts and accessible achievement detail dialogs.
- Improved weekly streak markers, reward visuals and UI test coverage.

## 1.0.9
- Improved adaptive launcher icon, tap targets, theme contrast and
  full-row setting switches.
