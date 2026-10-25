# Sudoku by Brendigo

A native **Android Sudoku** game written in **Kotlin + Jetpack Compose**, branded **Sudoku** (publisher: Brendigo). The app has original vector graphics inspired by the supplied design reference: midnight-blue scenery, cyan and blue glow, golden rewards, clean cards and a Croatian-first UI.

<div align="center">

![Sudoku by Brendigo — original vector logotype](branding/sudoku-logo.svg)

### Više od igre. Vježba za um.

**Hrvatska Sudoku igra za Android — bez oglasa, praćenja i internetske veze.**

![Sudoku — promotional feature graphic](branding/sudoku-feature-graphic.svg)

[![Preuzmi najnovije izdanje](https://img.shields.io/github/v/release/bren-wp/Sudoku?label=Preuzmi%20APK&color=0c9ef4)](https://github.com/bren-wp/Sudoku/releases/latest)
[![Android CI](https://github.com/bren-wp/Sudoku/actions/workflows/android.yml/badge.svg)](https://github.com/bren-wp/Sudoku/actions/workflows/android.yml)
[![Vizualni QA](https://github.com/bren-wp/Sudoku/actions/workflows/visual-smoke.yml/badge.svg)](https://github.com/bren-wp/Sudoku/actions/workflows/visual-smoke.yml)

</div>

## Igra koja trenira koncentraciju

| Odaberi izazov | Igraj bez ometanja | Prati vlastiti napredak |
| :-- | :-- | :-- |
| Mreže **4×4, 6×6, 9×9** i šest razina težine | Bilješke, savjeti, poništavanje poteza i automatsko spremanje | Dnevni izazovi, statistika i **24 postignuća** |

### Identitet aplikacije

<img src="branding/sudoku-icon.svg" alt="Sudoku 7–3–9 — plavo cijan vektorska ikonica" width="136" />

Tamna noćna paleta, cijan/plavi neonski naglasci i prepoznatljiva mreža **7–3–9**. Sve funkcionalne kontrole implementirane su u Kotlinu i Jetpack Composeu — bez lažnih slika zaslona.

### Stvarne snimke aplikacije

Snimke svih sedam ekrana izravno se snimaju s Android emulatora u GitHub Actions procesu. Pregledaj [zadnje stvarne snimke (941×1672 i 720×1280)](https://github.com/bren-wp/Sudoku/actions/workflows/visual-smoke.yml) u artefaktima uspješnog izvršavanja. Snimke nisu zamijenjene marketinškim mockupovima; za prikaz galerije unutar README-ja potrebno je zasebno dodati provjerene PNG-ove u repozitorij.

**Ekrani:** Početna · Nova igra · Igra · Dnevni izazov · Statistika · Postignuća · Postavke.

### Preuzimanje i provjera

**[GitHub Releases — APK i AAB](https://github.com/bren-wp/Sudoku/releases/latest)**

Bez izdavačkog ključa APK je **testno potpisan**, a AAB **nepotpisan**. Nemoj ih tretirati kao gotove Google Play produkcijske pakete. Funkcionalnost se provjerava JUnitom, Android Lintom, Gradle buildom i instrumentacijskim testovima.

**Vizualna preciznost:** originalne reference od 941×1672 služe kao cilj; automatizirani testovi i usporedbe bilježe preostala odstupanja u [issueu #11](https://github.com/bren-wp/Sudoku/issues/11). Ne tvrdi se potpuna podudarnost piksela dok mjerenje to ne potvrdi.

---

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
- Version: `1.0.23`
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

## Sudoku 1.0.15 — vizualna dorada prema originalnim referencama

- Dnevni izazov: preciznije poravnanje naslova i povratka, tri informacijska bloka,
  ikonica poklona, povezani sedmodnevni napredak i uži centrirani gumbi.
- Postignuća: veće kartice, izraženije vektorske ikone, značke bodova,
  deblji napredak i dvodijelno zaglavlje sa simbolima.
- Zajednički UI: prilagođeni vertikalni razmaci i tamniji noćni krajolik.
- Prošireni emulator testovi za dnevne nagrade, interakcije i kartice.
- Provjere na 941 × 1672 i 720 × 1280 px; korisnička spremanja ostaju sačuvana.
- Potpuna 1:1 jednakost piksela još nije dokazana; detalji u issueu #11.

## Sudoku 1.0.16 — statistika

- Povećani grafikoni i naslovi prema referentnoj slici.
- Nulta vrijednost u grafikonu težina više ne prikazuje lažni stupac.
- Veći kružni grafikon i postotak, uz očuvane stvarne rezultate igrača.
- Testovi provjeravaju oba grafikona na Android emulatorima.
- Potpuna 1:1 jednakost nije potvrđena; issue #11 ostaje otvoren.

## Sudoku 1.0.17 — UI, ikonice i pixel audit

- Usklađeni su tamnoplavi tonovi neba, Sudoku mreža u pozadini, planine,
  jezero i cvjetne grane na svih sedam ekrana.
- Android adaptive ikonica dobila je vidljiviji svijetlocijan broj 3,
  uz očuvane brojeve 7–3–9 i standardne Android maske.
- Veći splash logotip, naslov igre, HUD srca, sat i istaknuto polje.
- Poklon u prikazu dnevne nagrade, odgovarajuće strelice u karticama
  težine te stvarno pristupačno odabrano stanje veličine/težine igre.
- Sređen odjeljak O aplikaciji, odvojena verzija i vektorska info ikona.
- Dodani emulator testovi odabira veličine i razine težine.
- Pixel-perfect 1:1 nije potvrđen bez novog usporednog mjerenja svih
  sedam 941×1672 referenci (issue #11).

## Sudoku 1.0.18 — premium settings and achievement fixes

- The shared footer now owns a layout slot **outside** each scroll viewport.
  On short devices, users can scroll to the final action without text overlays.
- Appearance settings use a dedicated scalable palette vector icon.
- Settings toggles match the cyan-to-blue track and white thumb in the
  original reference; the full row remains the only interactive switch.
- Achievement cards retain their distinct icon and accent even while locked,
  with a compact lock badge. Unlocked achievements show a checkmark.
- Instrumented pixel-geometry guards verify the final achievements action
  and privacy row never overlap the footer at 941x1672 and 720x1280.
- No storage migration or changes to puzzle persistence.
- 1:1 pixel equality still requires normalized screenshots and measured MAE;
  issue #11 remains open.

## Sudoku 1.0.19 — Settings viewport alignment

- The About section uses compact spacing so the actual app version appears in the initial 941×1672 reference view.
- The fixed footer has its own layout slot and cannot obscure scrollable settings or achievement actions.
- Cyan/white switches remain accessible and saved through the existing controller.
- QA verifies 941×1672 and 720×1280 emulator workflows. Pixel parity remains incomplete (issue #11).

## Sudoku 1.0.20 — vizualna dorada po izvornim referencama

- Početni ekran: sat uz nastavak igre sada je prava vektorska ikona;
  sedmodnevni niz ima pristupačna stanja po danu.
- Nova igra: zadržani svi odabiri i vizualno označavanje aktivne težine.
- Igra: jasniji neonski okvir odabranog polja, pravilnije plave granice
  blokova i pristupačno odabrano polje u Android sučelju.
- Dnevni izazov: linije niza povezuju samo stvarno uzastopne riješene dane.
- Statistika: vektorski grafikon uz naslov, strelice u karticama,
  dovršene/prekinute igre prikazane uz kružni grafikon.
- Postignuća: indikatori napretka i bodova zaključanih nagrada
  zadržavaju odgovarajuće nijanse umjesto jednolične sive.
- Android adaptive ikonica: diskretan cijan/plavi sjaj uz brojeve 7 i 9.
- Dodane regresijske provjere na emulatorima 941×1672 i 720×1280.
- Postojeće spremljene igre, podaci i korisničke postavke ostaju sačuvani.
- Potpuna 1:1 jednakost piksela nije potvrđena; issue #11 ostaje otvoren.

## Sudoku 1.0.21 — first-screen Home pixel review

- Rebalanced logo, slogan and resume hero spacing for the 941x1672 artwork.
- Recreated the decorative Sudoku board in the right half of the Resume Game card
  with a lightweight native Compose Canvas, never with a fake screenshot UI.
- Enlarged the saved-game heading/CTA. Time, difficulty and empty cells remain
  connected to the real saved game and its controller.
- Added consistent right-arrow icons, card heights, paddings and Croatian
  descriptions to all five navigational cards.
- Refined the streak summary, weekday labels, separator and next-reward gift.
- UI regression tests cover the resume hero, all five links and reward strip.
- No game data, database, settings or functionality removed or migrated.
- Pixel-perfect 1:1 requires new real screenshots and measured comparisons.
  Track residual mismatches in issue #11.

## Sudoku 1.0.22 — Postignuća, ekran po ekran

- Focused reference alignment of Achievements (941 × 1672): reduced top offset,
  tuned title/summary dimensions and deliberately shorter lower featured rows.
- Preserved four rows / two columns and every existing achievement, score,
  milestone, detail dialog, and all-24 expansion.
- Restored warm native sunset color treatment on gold rewards without faking
  unlocked states, plus a restrained locked-state scrim.
- Improved native trophy/star scales, award points badges, progress-track height
  and outlined unlock indicators.
- Added a genuine vector calendar with the numeral 7 for the seven-day award.
- Added instrumented bounds assertions for card alignment, score separation,
  and lower-row geometry. Saved puzzles, streaks and user preferences unchanged.
- Exact pixel parity remains unverified: live scores and dates differ from the
  user's illustration and native scenery still needs further comparison (#11).
