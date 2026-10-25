## 1.0.23 — Statistics and marketing README

- Fine-tuned Statistics header vertical spacing and numeric tile typography.
- Added vector chart icons to native bar-chart and completion-ring headings.
- Introduced Croatian marketing README with original SVG logo, feature artwork,
  adaptive brand icon preview, release/CI badges and screenshot artifact links.
- Screenshot links point to real emulator captures; no fabricated image previews.
- Preserves all local games, 24 achievements, difficulty modes and statistics.
- Pixel-perfect fidelity is not asserted until normalized comparisons pass (#11).

## 1.0.22 — Achievements screen reference pass

- Tightened the achievements header-to-score spacing for the owner reference.
- Aligned four featured rows with a taller top half and more compact lower half.
- Gold awards retain warm sunset illustration while a native scrim expresses lock.
- Enlarged reward icons, point badges, progress track, and outlined completion.
- Distinct scalable calendar-seven icon matches the seven-day award.
- Added geometry regression checks; preserved all 24 functional awards and data.
- Pixel-perfect match is not certified until normalized 941×1672 image QA (#11).

## 1.0.21 — Home screen reference alignment

- Reworked the first Home screen's vertical composition and resume panel.
- Restored a native vector mini Sudoku illustration behind the resume button.
- Tuned two large and three compact cards with active chevrons and proper copy.
- Improved real-time streak and next reward layout; no mock statistics.
- Added UI screenshot and geometry assertions for Home elements.
- Saved state and other screens unchanged. Pixel parity still to be measured.

## 1.0.20

- Added graph icon to Statistics title and vector arrows in summary cards.
- Completion donut now shows completed/abandoned legend with real counts.
- Replaced Unicode resume timer with branded scalable icon.
- Linked weekly progress only between adjacent completed daily challenges.
- Improved Sudoku selection rim, group separators and TalkBack state.
- Exposed the completed state of weekly circles to accessibility.
- Recolored locked achievements progression by individual reward tier.
- Added subtle 7/9 glows to adaptive icon while keeping 7-3-9 geometry.
- UI regressions for game selection, statistics legend and weekday labels.
- No save format migrations. Pixel-perfect remains unverified (issue #11).

## 1.0.19

- Improved Settings vertical layout and About panel so the real version row fits in the original 941×1672 screenshot viewport.
- Preserved footer isolation, white-thumb cyan switches, palette icon and achievement color identity from v1.0.18.
- No user-data migration or removed features; revalidate Android CI and both UI emulator sizes before release.
- Numeric reference matching is not yet pixel-perfect (issue #11).

## 1.0.18

### Visual alignment / accessibility
- Corrected footer overlap: every major page reserves a separate non-scrolling
  footer slot outside its fully scrollable content.
- Replaced Material default switches with native cyan-blue/white-thumb controls
  while preserving a single accessible and persisted switch per setting.
- Added a dedicated palette vector icon for Appearance.
- Locked achievements show meaningful dimmed award symbols alongside small
  lock status icons; unlocked cards gain explicit completion checks.
- New instrumentation compares footer top with the last actionable settings
  and achievements bounds, guarding compact/reference viewport sizes.

### QA
- Retain save format; gate publication on JUnit, Android Lint, debug APK,
  unsigned AAB and both emulator smoke tests.
- Reference artwork remains the target: numerical MAE=0 not claimed.

## 1.0.17

- Recalibrated native night scenery, mountains, background Sudoku grid and sakura palette across all seven screens.
- Improved launcher icon center cyan digit 3 glow, retaining 7-3-9 adaptive-safe geometry.
- Enlarged splash mark/title and game HUD, improved selection-cell glow.
- Home reward now shows gift icon; difficulty cards use directional icons.
- Android size/difficulty controls provide real accessible selected semantics.
- Settings About section includes info icon and version row.
- Added instrumented assertions for 9x9/4x4 and difficulty selection.
- No claim of pixel-perfect parity; open issue #11 remains.

## 1.0.16

- Enlarged statistics chart geometry, labels and circular completion indicator.
- Zero-win categories now render a minimal baseline rather than a misleading bar.
- Emulator smoke test asserts both statistics visualizations are present.
- Original reference pixel parity remains a work in progress (issue #11).

## 1.0.15

### Native UI reference refinements
- Daily Challenge headline aligned independently of the back button.
- Calendar, streak and golden reward segmented into balanced panels with
  a dedicated vector gift glyph; three bordered challenge metrics.
- Seven-day progress shows connected circles and actual device calendar days.
- Primary challenge and secondary ranking controls match narrower reference widths.
- Achievements score header split by divider, with real totals and reward iconography.
- Larger achievement cards, points badges, icons, progress bars and descriptions.
- Night landscape and warm reward cards use darker/navier gradients for text clarity.
- Instrumented tests cover daily ranking and achievement component dimensions.

### Quality gates
- CI: JUnit, Android Lint, APK/AAB build, 941x1672 and 720x1280 emulators.
- Previous v1.0.14 measured full-frame mean RGB error: 34.010 / 255.
- Dynamic dates, sample progress and scenic image details still differ from artwork.
- Never declare pixel-perfect without normalized evidence; tracking issue #11.
## 1.0.14

### Pixel-alignment and interactions
- Corrected misaligned/stretched second row of the 9×9 numeric keypad:
  all key widths match and the final four are centered.
- Candidate notes now occupy stable micro-grid positions (9×9: 3×3,
  6×6: 3×2, 4×4: 2×2) and have accessible spoken labels.
- Premium circular home-menu vector icon framing, brighter primary buttons
  with dedicated cyan outlines, and more consistent Settings section tiles.
- Four real achievement preview cards in Statistics with progress and
  unlocked/locked vectors, linked to all 24 achievements.
- Cool-blue daily date header with separate gold achievement accents.
- Updated emulator tests for notes and equal-width keypad key geometry.

### QA
- Preserve user saves and settings. Require green Android CI and both
  941×1672/720×1280 emulator smoke tests before release.
- Screenshot pixel parity is not certified. Issue #11 remains open.

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
