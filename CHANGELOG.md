## 1.0.29 — Igra: sigurna pauza i pristupačan HUD

- Sudoku brojevi i bilješke više nisu vidljivi dok je igra pauzirana; ploča pokazuje oznaku pauze bez mijenjanja spremljene partije.
- Nakon nastavka vraćaju se ista mreža, potezi i interaktivne numeričke tipke.
- HUD prelazi u dva retka na iznimno uskim ekranima ili uz uvećani sustavni font, kako bi mjerač, napredak i greške ostali čitljivi.
- Zaglavlje igre prilagođava širinu naslova, a tipkovnica smanjuje font brojeva samo na vrlo uskim tipkama.
- Akcije „Bilješke”, „Savjet”, „Poništi” i „Izbriši” prilagođavaju ikone/tekst uskim karticama; semantički status bilježaka i broj preostalih savjeta dostupni su čitačima zaslona.
- Pojedinačne Sudoku ćelije ne predstavljaju se kao aktivne nakon završetka ili neuspjeha igre.
- Regresijski test pokreće pravu partiju, provjerava HUD, pauzu, skrivanje ćelija i nastavak sa svim kontrolama.
- Mehanika igre, generator zagonetki, podaci, statistika i logotip 7–3–9 nisu mijenjani.
- Pixel-perfect 1:1 nije dokazano jer izvorne dizajnerske PNG reference nisu u GitHub repozitoriju (#11).

## 1.0.28 — Nova igra: prilagodljiv odabir veličine i težine

- Zadržan postojeći 192 dp odabir 9×9 / 6×6 / 4×4 na referentnom zaslonu; na iznimno uskim uređajima odabir se sužava i ne preklapa gumb za povratak.
- Kartice svih šest težina dobivaju prilagodljiv unutarnji raspored, veličine ikonica i više prostora za tekst pri maloj širini ili uvećanom sustavnom fontu.
- Interaktivne opcije veličine i težine označene su pristupačnom radio-ulogom i stvarnim stanjem odabira.
- Aktivnost vraća ekran „Nova igra” nakon rotacije, dok Compose `rememberSaveable` vraća odabranu veličinu i težinu; generiranje tijekom pokretanja onemogućuje naknadnu promjenu postavki.
- Gumb pokretanja širi se na vrlo uskim ekranima, a referentna širina ostaje 75 % kartice.
- UI testovi provjeravaju granice odabira veličine, svih šest težina, rotaciju i stvarno pokretanje igre 6×6.
- Nema promjene logike generatora, pravila Sudoku, lokalno spremljenih partija, pozadina ni originalnog brenda 7–3–9.
- Potpuna pixel-perfect jednakost s korisnikovim PNG-ovima i dalje nije mjerena jer referentne datoteke nedostaju u repozitoriju (#11).

## 1.0.27 — Početni ekran: responzivna navigacija i tjedni niz

- Očuvan referentni raspored dviju velikih i triju malih navigacijskih kartica na širim Android zaslonima.
- Na kompaktnim telefonima tri manje kartice složene su u dva retka (2 + 1), bez preklapanja naziva i strelica.
- Na izrazito uskim uređajima dvije velike kartice mogu se složiti u jedan stupac.
- Tjedni niz na uskim ekranima prikazuje oznake sedam dana u zasebnom redu; stanje je i dalje stvarni lokalno spremljeni napredak.
- Slogan skaliran na kompaktnom zaslonu te dodani pristupačni opisi nastavka igre.
- Instrumentacijski testovi potvrđuju geometriju kartica, očuvanje referentnog rasporeda i granice tjednog niza.
- Nisu mijenjani mehanika igre, pohrana, postojeće pozadinske ilustracije, ikonice 7–3–9 ni sedam marketinških WebP snimki.
- 1:1 rezultat nije dokazan bez normaliziranih originalnih referentnih PNG-ova (issue #11).

## 1.0.26 — Prilagodljiv ekran Postignuća

- Sačuvan izvorni dvostupčani raspored i redoslijed osam istaknutih postignuća na referentnom zaslonu.
- Na kompaktnim Android ekranima kartice se mogu proširiti kako opis i napredak ne bi bili odrezani.
- Za uske uređaje ili povećani sistemski font prikazuje se jedan stupac pristupačnijih kartica.
- Sažetak bodova i broja otključanih nagrada prilagođen je užim uređajima; gumb za proširenje ima jasan opis za čitače zaslona.
- UI regresijski testovi provjeravaju smještaj opisa i prisutnost svih 24 postignuća.
- Nisu mijenjani bodovi, kriteriji nagrada, lokalni spremljeni podaci ni originalne SVG/WebP datoteke.
- Pixel-perfect 1:1 nije potvrđen bez izvornih referentnih PNG-ova (issue #11).

## 1.0.25 — Statistics compact-screen readability

- Scale statistics tile icons and text at widths below 180dp per tile.
- Allow labels to wrap onto two lines, preserving readable time values.
- Add compact-emulator regression coverage for metric card separation and info dialog.
- Keep real statistics, offline game state, seven-screen marketing gallery and original branding unchanged.
- Pixel-perfect parity remains unverified without normalized 941×1672 owner references (#11).

## 1.0.24 — Statistics reference polish

- Kept the seven real emulator screenshots and branded marketing README from 1.0.23.
- Reduced Statistics title offset and adjusted metric card typography.
- Added branded vector chart-heading symbols, retaining truthful live values.
- No modification to the saved state, score calculations, game logic or achievements.
- Pixel-perfect not yet certified; issue #11 continues to track reference parity.

## 1.0.23 — marketing README and multi-screen visual audit

- Replaced the lengthy development-focused homepage with a Croatian marketing
  README featuring the existing Sudoku/Brendigo original SVG logo, 7-3-9 icon,
  feature banner and a complete seven-screen gallery of real emulator captures.
- Added an isolated GitHub Actions job to capture the actual Compose app on a
  941×1672 emulator and commit optimized WebP screenshots to docs/screenshots.
- Centered reference headings for Statistics and Settings now use uppercase,
  letter-spaced Croatian descriptors; they remain accessible native text.
- Reduced the oversized gap above the Daily Challenge summary and improved its
  warm hero contrast and title styling to more closely match the owner artwork.
- Refined native scenic moon and cherry blossom positions/colors across screens.
- Preserved real offline data, 24 achievements, gameplay mechanics and saved
  state; no static screen backgrounds masquerading as live controls.
- No claim of full 1:1 parity: all screen areas still require normalized
  numeric comparison with the owner's design files (issue #11).
- APK/AAB signing state follows CI secrets; unsigned release is not Play-ready.

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
