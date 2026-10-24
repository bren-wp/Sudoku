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
- Version: `1.0.5`
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
