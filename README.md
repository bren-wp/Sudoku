# Sudoku by Brendigo

A native **Android Sudoku** game written in **Kotlin + Jetpack Compose**, branded **Sudoku** (publisher: Brendigo). The app has original vector graphics inspired by the supplied design reference: midnight-blue scenery, cyan and blue glow, golden rewards, clean cards and a Croatian-first UI.

## Status

Active implementation on the `main` branch with pull-request validation, automated Gradle unit/lint builds, emulator UI smoke tests, and versioned GitHub Releases. UI reference screens remain **design targets**, not claimed pixel-perfect reproductions. No third-party source or visual assets from SudokuNova are included; the engine and UI in this repository are newly written. This repository remains MIT licensed (see LICENSE).

## Features

- Fully offline 4×4, 6×6 and 9×9 Sudoku; six clue-density difficulty tiers
- Random valid solved grids + uniqueness-checked puzzle removal
- Tap to select, number entry, notes, erase, undo, reveal hint
- Mistake limit, optional error checking, highlighted peers/equals
- Pause/resume, local autosave and persistence between launches
- Deterministic daily puzzle, day streak and simple achievements
- Play statistics, history, offline settings and custom vector visual theme
- Responsive scrolling on smaller screens; Android 8.0+ (API 26)
- No account, analytics, ads, internet permission, trackers or third-party Sudoku code

## Build

Install Android SDK 36, JDK 17 and Gradle 8.13, then:

```bash
gradle :app:testDebugUnitTest
gradle :app:lintDebug
gradle :app:assembleDebug
gradle :app:bundleRelease
```

GitHub Actions runs the checks for main and pull requests. A successful main build publishes a versioned GitHub Release with an APK and an AAB. Without protected publisher signing secrets, these are a **debug test APK** and an **unsigned AAB**, neither of which should be represented as a production Play release. For Play-ready files configure the four `SUDOKU_*` signing secrets on GitHub, verify the certificate fingerprint and inspect the final signed artifacts. Never commit signing keys or credentials.

## Package and brand

- App name: **Sudoku**
- Publisher: **Brendigo**
- Application ID: `com.brendigo.sudoku`
- Version: `1.0.14`
- Primary language: Croatian

## Implementation notes

App state is stored using private SharedPreferences as compact JSON; backup and transfer are disabled. Completed game statistics are calculated from local saves. Daily challenges use the Android device date; changing the device date can affect challenge availability. Tier labels reflect removed clue counts, **not an independent solver-rated complexity analysis**. Work remains to validate visual parity on real devices, accessibility contrast/font scaling, notifications (if introduced), large tablet layouts, review runtime test results and release signing.

## Privacy

See [PRIVACY.md](PRIVACY.md). No user account or network permission is requested by this implementation.


## Android visual QA

The `Android visual smoke` workflow tests seven actual app screens using an
Android emulator and saves **real, PNG-verified screenshots** in the GitHub Actions
artifact `sudoku-emulator-screen-references`. The original owner mockups remain
the target; a passing smoke test establishes navigation and valid captures, not
pixel-by-pixel visual equivalence. Compare layout, typography, artwork, touch
targets and accessibility on multiple device sizes before a production release.

## Signing

GitHub version tags are published by `.github/workflows/publish-release.yml`
after a successful main-branch build. Without a publisher keystore, Releases
contain a debug-test APK and **unsigned AAB**, clearly marked as a preview.
For production signing use protected GitHub Actions secrets:
`SUDOKU_KEYSTORE_BASE64`, `SUDOKU_SIGNING_KEY_ALIAS`,
`SUDOKU_SIGNING_STORE_PASSWORD`, `SUDOKU_SIGNING_KEY_PASSWORD`.
Do not commit or share the private signing keystore.

## Version 1.0.3 validation

- Emulator uses the AOSP system image to avoid the Pixel Launcher ANR overlay seen in previous QA captures.
- A screenshot is rejected if representative central pixels indicate a light system dialog; successful tests alone are not accepted as evidence of clean screenshots.
- Daily challenge completions grant a persisted 50-point reward, including a compatibility migration for existing local save data.
- True 1:1 pixel-perfect fidelity is still not certified; source screen images remain visual targets.

## Sudoku 1.0.5 — igra i privatnost

- Dnevni izazovi knjiže se pod datumom zagonetke, čak i kada igra prijeđe ponoć, bez dvostrukog dodjeljivanja nagrada.
- Tjedni niz prikazuje stvarno riješene datume, a postojeći napredak migrira se bez brisanja.
- Postavke uključuju dostupnu offline obavijest o privatnosti; svi glavni zasloni dobili su ujednačen brendirani podnožak.
- Dodatni JUnit testovi pokrivaju duple pobjede, promjenu dana i niz dnevnih izazova.
- GitHub Release sadrži APK i AAB. Bez sigurnog izdavačkog potpisa APK je testni, a AAB nepotpisan.

## Pixel-level visual QA

The `Android visual smoke` workflow captures seven screens at the 941×1672
reference resolution. See [pixel QA instructions](docs/pixel-qa.md) for the
optional per-pixel comparison and difference overlays. Exact 1:1 fidelity
has not yet been certified: source screenshots contain reference-specific
puzzles, scores and system bar layouts.

## Visual parity 1.0.7

The first eight achievements now appear in the reference 4 × 2 arrangement, with all
24 still available using a real "show all" action. Daily challenge cards, game
statistics, completion chart, recent-game history and the achievement preview
have been resized for the 941×1672 reference layout. Android UI tests verify
the featured legend card is actually visible and that the remaining awards
are accessible without losing navigation.

The images are **not** passed off as exact screenshots of a finished app.
Pixel comparison is still necessary; the baseline mismatch in the reference
emulator captures is documented in docs/visual-parity-report.md.

## Sudoku 1.0.8 — full-screen interface, icons and UX audit

- Rebuilt the adaptive app foreground to keep the branded **7 / 3 / 9** motif
  inside the Android launcher mask safe zone; both normal and round launcher
  variants reference the same drawable.
- Added a consistent Compose vector icon component used by the home screen,
  difficulty selector, daily awards, statistics, game tool bar and settings.
  OS-dependent emoji appearance is no longer relied upon for essential controls.
- Corrected hit targets to at least 48dp where practical, added TalkBack
  descriptions and button semantics, and reduced oversized reference-screen
  typography without replacing any functional UI with a screenshot.
- Settings language selector clearly discloses that Croatian is currently the
  supported language, instead of presenting an inert label.
- Interactive statistic tiles now explain their meanings instead of showing
  nonfunctional navigation chevrons.
- Emulator QA checks Android icon availability and that settings, language,
  sound and privacy controls can be opened and closed.

Remaining: reference illustration fidelity, zero-difference pixel QA and
publisher-signed Google Play AAB are *not* certified by these checks.

## Sudoku 1.0.9 UI/UX and adaptive icon refinement

- Whole settings rows now toggle options with standard accessibility `Role.Switch`,
  not only the small visual thumb; real Compose state and preferences still persist.
- Board-size selector adapts to narrow Android screens without fixed-width overflow.
- Daily challenge header has three icon-led groups: date, streak, and reward.
- Daily ranking button has a 48dp target and accessible role.
- The optional brighter theme uses contrast-safe deep-blue scenery behind
  the existing light text and dark translucent cards.
- Launcher icons now provide a native **monochrome themed icon** for Android 13+
  (alongside standard and round 7/3/9 foregrounds).
- Emulator integration tests cover tapping entire setting rows, daily navigation
  and launcher themed icon resource.
- Pixel-perfect 1:1 comparison to the user's supplied art remains open in issue #11;
  functional Android test success must never be called zero pixel difference.

## Sudoku 1.0.10: premium UI polish and visual QA

- Night backdrop: more faithful cobalt/purple cherry blossoms, layered
  mountains, a three-tier moonlit pagoda with real rooftop silhouettes,
  warm windows and lake reflections. These remain native vector scenes,
  behind the functional UI rather than replacing screens with images.
- Game status: deterministic, scalable **filled and outlined heart** icons
  for remaining mistakes (with a complete TalkBack description).
- Achievements: visibly muted locked cards, functional detail dialog with
  full description, current progress and reward on tapping any card.
- Calendar: consistently rendered seven-day circular markers on home and daily;
  award metadata uses the same native iconography as the rest of the app.
- Launcher: corrected the themed adaptive **alpha mask** for Android 13+
  and added subtle 7-3-9 neon glints to the full-color foreground.
- Instrumented QA checks clickable award cards and rendered icon transparency.
- Android CI releases a test APK and an unsigned AAB unless publisher
  signing secrets are configured.
- Pixel-for-pixel parity is **not certified**; see issue #11 and docs/pixel-qa.md.

## Sudoku 1.0.11: date-boundary stability

- Home streak and daily challenge now refresh the local calendar date without
  requiring the player to leave and re-enter the screen. The date ticker fires
  at midnight and checks for device date/time-zone changes at most one minute apart.
- Date formatting and daily completion availability stay tied to real saved
  progress; no hard-coded screenshot data is used in the actual application.
- Added unit coverage for local midnight, the Europe/Zagreb daylight-saving
  transition, New Year's Eve, and clock-change polling.
- Full 941×1672 design parity is still pending normalized emulator image comparison
  under issue #11; this patch does not assert a new MAE score.

## Sudoku 1.0.12: seven-screen native UI refinement

- Increased 7–3–9 brand mark clarity and adaptive/round/themed launcher foreground
  scale, keeping paths centered inside the native icon masks.
- Improved native nightscape and glass-like panel accents; no screenshot UI is
  substituted for interactive Compose content.
- Rebalanced titles, statistics tile heights, daily challenge date/reward and
  seven-day markers, and 24 achievement cards with larger vector icons.
- Corrected the “Odigrane igre” statistic to count started sessions (`played`),
  instead of incorrectly showing only finished sessions.
- New-game difficulty cards now include bordered circular vector leaf,
  star, multi-star and crown glyphs using stable Croatian labels.
- Selected Sudoku cell gets an electric-cyan outline. New puzzles preselect a
  central editable cell. Undo, erase, hints and keypad clearly disable when
  the action cannot be performed.
- Emulator UI tests verify disabled/enabled tools around move, undo, notes and
  erase; existing gameplay, history, settings and achievements are retained.
- **Not a certified 1:1 match**: exact reference background textures, type
  rasterization and dynamic fixture data need another normalized 941×1672
  pixel-diff cycle. Follow issue #11.

## Sudoku 1.0.13: responsive UI and full-size touch targets

- Shared screen footer is anchored above the landscape instead of drifting
  directly beneath short content. Scrollable screens reserve sufficient bottom
  space so the footer cannot cover actionable controls.
- Daily challenge shows the real dates for the seven weekdays and a clearly
  named gold badge reward; these are live calendar-derived labels, not static
  screenshots or hard-coded days.
- Statistics history opens from the entire accessible 48dp header; achievement
  previews now use names that exist in the 24-achievement catalog.
- Game board sizing respects available viewport width in narrow multi-window
  layouts. The notes toggle is inactive for paused or finished games.
- Settings explicitly supply native vector icon kinds, removing unnecessary
  text-label-to-icon switching; 7/3/9 branding now has TalkBack semantics.
- Adaptive launcher icon gains a restrained cyan line glow inside its
  existing safe mask.
- Emulator QA runs at both 941×1672 (design reference) and 720×1280 (compact
  display), recording actual Android screenshots separately.
- Original artwork still has unmeasured differences to the Compose rendering.
  **Pixel parity is not certified**; issue #11 stays open.

## Sudoku 1.0.14: Gameplay layout and pixel-accurate component geometry

- Live candidate pencil marks are now rendered in fixed micro-grid slots.
  For 9×9 puzzles this is a 3×3 mini-board inside every empty cell; 6×6
  uses 3×2 and 4×4 uses 2×2. Empty candidate positions no longer shift
  nearby digits when a note is toggled.
- Sudoku's 9-key number pad now uses five equal-width keys on row one and
  four **equal-width, centered** keys on row two, matching the reference
  instead of stretching the final four across the available width.
- The entire board cell's accessibility description includes entered
  candidate digits.
- Home cards use accented circular vector icon outlines, primary actions
  use a brighter bounded cyan-blue gradient, and settings section icons
  have a defined bordered tile.
- Statistics includes four functional achievement previews with vector
  icons, locked/unlocked states and live progress bars, all opening the
  full 24-achievement screen.
- The daily date/streak panel uses the original screen's cool-blue surface,
  while warm-gold rewards and special cards remain emphasized.
- Regression coverage checks note semantics and number-key layout on the
  real emulator at reference and compact sizes.
- Data storage keys and saved game progress remain unchanged.
- **MAE=0 is not yet established:** original 941×1672 reference artworks
  have dynamic sample data and richer scenic illustration. See issue #11.
