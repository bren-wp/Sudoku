## 1.0.13

### Seven-screen UI and accessibility
- Anchored shared footer to the lower scenic background while preserving
  full content scrolling and avoiding controls hidden beneath it.
- Daily challenge shows real Monday–Sunday calendar dates and a named
  reward, with revised calendar captions.
- Entire statistics history header is clickable and has a 48dp touch target.
- Statistics achievement preview now links to actual award names.
- Responsive Sudoku board now fits narrow multi-window device widths.
- Notes cannot be toggled during pause or completed/failed games.
- Settings vector icons use explicit icon keys rather than ignored emoji
  parameters and inferred titles.
- TalkBack description for the Sudoku 7–3–9 brand icon; additional subtle
  cyan glow in adaptive launcher artwork.
- Reference-size and compact emulator smoke runs test 941×1672 and 720×1280.

### Verification
- Real emulator and static checks required before merge and version release.
- The original source artwork remains the target: 100% 1:1 and MAE=0
  are **not claimed** until normalized screenshot comparison proves them.

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
