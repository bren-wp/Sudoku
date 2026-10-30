# Sudoku v1.0.47 — Postignuća (screen 7/8): real pixel QA

## Owner reference
- Original uploaded design: `Slika ChatGPT 24. lis 2026. 20_47_19-9(9).png`, 941×1672.
- Real unmodified emulator snapshot: `07-achievements.png` from v1.0.46 (workflow 41130008761), 941×1672.
- The reference is an **illustrative** state with 420 points / eight unlocked achievements. The real emulator may have only 25 points and one unlocked. Production code MUST NOT substitute sample data.

### Baseline measured mean absolute RGB difference (0..255, lower is better)

| Region, x/y pixels | v1.0.46 baseline |
|---|---:|
| Full image | **30.986** |
| Header y0..160 | 24.720 |
| Summary x38..894 y160..319 | 31.365 |
| First row x36..895 y333..642 | 40.694 |
| Second row x36..895 y650..956 | 36.900 |
| Third row x36..895 y974..1274 | 26.642 |
| Fourth row x36..895 y1285..1485 | 34.372 |
| Bottom footer y1485..1672 | 26.557 |

These values are the **before** measurements and are not evidence of final 1:1 parity.

## Implementation
- Existing 24 real achievement definitions, point calculations, unlocked state, progress and detail dialog retained without migrations or seeded mock data.
- Custom native Compose background surfaces for gold sunset, blue lunar night, cyan/teal logic, and muted locked achievements. Native scenery is purely decorative; text, badges, reward points, semantics and taps remain Compose.
- Warm filled local vector trophy, filled rating stars, blue/orange tinted reward chips, matched side padding and percentage-based progress bars. Progress is clamped only visually.
- Header summary uses distinct night glass with warm left trophy and bright cyan unlock star.
- Eight featured cards remain a two-column four-row layout at 941×1672. The additional 16 appear via 'Pogledaj svih 24 postignuća'; smaller devices and large-font settings preserve scroll and accessibility.
- New Android UI test checks actual first/second/third card placement on reference and compact screens.
- No external image services, trackers or new privileges; dark theme/local offline game is unchanged.

## Acceptance
- AGP 9.5.0, Gradle 9.6.0, JDK17, compile/target SDK 36, minSdk 26, applicationId `com.brendigo.sudoku` preserved.
- Version 1.0.47 / versionCode 48.
- Full JUnit, Android Lint, security/privacy, Android Debug/Release APK and AAB build, and UI smoke at 941×1672 and 720×1280.
- Capture fresh real `07-achievements.png`, measure global and regional difference. Report regressions, not merely green builds.
- Keep publisher key outside the repository. If unavailable, APK is test-only and AAB unsigned, not Play-ready.

**1:1 pixel-perfect cannot be claimed without real screenshot/geometry verification**. Differences in user state (25 points vs reference 420), native font rasterization and bottom scenery are expected but should be kept distinct from correctable layout differences.
