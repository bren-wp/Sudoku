# Sudoku v1.0.74 — datum dnevnog izazova i izvršavanje R8 release APK-a

## Osnova i dokaz

- Pregled 13.11.2026.: PR #79 i #80 već su spojeni. Zadnja stvarno objavljena oznaka je \`v1.0.73\` (test APK, unsigned AAB, SHA256SUMS).
- Ovaj PR polazi od main commita \`66b151274364ade2a33c7b0d0dcc3f6a469b814f\`.
- Ne mijenja Compose geometriju, launcher, reference, screenshotove iz README-ja, shemu lokalnih spremljenih podataka, manifest, dozvole niti release biblioteke.
- Nikakve tvrdnje o izmjerenoj 1:1 podudarnosti iz ovog PR-a: zadnje dokumentirano uspoređivanje za v1.0.73 i dalje ima **0/10** potpuno identičnih originala.

## Nalazi i stvarne korekcije

| Prioritet | Datoteka | Problem i uzrok | Ispravak | Provjera |
| --- | --- | --- | --- | --- |
| P1 | \`app/src/main/java/com/brendigo/sudoku/data/SudokuController.kt\` | Generator provjerava lokalni datum po završetku rada, ali kasniji \`checkpointBeforeSwitch()\` suspendira. Ponoć ili promjena vremenske zone u međuvremenu može aktivirati igru sa starim datumom. | Ponovna provjera dana poslije durability checkpointa i prije dodjele \`game\`. | \`DailyPuzzleGenerationTest.midnightAfterGenerationButDuringCheckpointInvalidatesDailySession\` provjerava drugi guard; Android integracija još mora biti potvrđena. |
| P1 | \`.github/workflows/visual-smoke.yml\`, \`.github/scripts/run-visual-smoke.sh\` | 11 emulatorskih UI poslova testiraju debug build. Izgradnja release APK-a i statička R8 analiza **ne dokazuju** da se optimizirana aplikacija može pokrenuti i koristiti. | U postojećem \`reference\` poslu, nakon pohrane debug referentnih snimki, izgradi R8 \`assembleRelease\`, privremeno QA potpiši i instaliraj na izolirani emulator. | ADB/UIAutomator: otvaranje Home, Nova igra, broj, pauza, nastavak, force-stop, povrat spremljene igre; \`release-runtime-game.png\` i \`release-runtime-resumed.png\`. |
| P2 | \`scripts/release_runtime_smoke.py\` | Slučajni uspješan shell launch može sakriti neupotrebljive kontrole. | Test traži stvarne Android accessibility oznake i koordinate tipki iz XML-a, ne lažira dodir niti statičnim slikama zamjenjuje UI. | \`scripts/test_release_runtime_smoke.py\`: parser i fail-closed granice. |

## Potpis, sigurnost i privatnost

- APK je isti \`assembleRelease\` rezultat s minifikacijom i shrinkResources, ali se za emulatorski test **privremeno potpisuje jednokratnim QA certifikatom**. Takav APK nije ni službeni release asset ni Play-ready izdanje. Nakon posla QA ključ se briše.
- Nikakav \`SUDOKU_SIGNING_*\` produkcijski ključ ne koristi se za ovu provjeru.
- Aplikacija ostaje potpuno offline, bez novih Android dozvola ili SDK-ova.
- Existing Android CI mora zasebno potvrditi release manifest, OSV, SBOM, R8 simboličku analizu, gradle testove i 11/11 postojećih emulatorskih poslova.

## Pixel QA

Bez izmjene produkcijskog UI-ja **nema novih rezultata prije/poslije za zaslone**. Ne prenose se raniji MAE brojevi kao nova mjerenja. Za strogi 1:1 potreban je zaseban reproducibilan test s privatnim izvornim 941×1672 i 1254×1254 PNG-ovima, poznatim SHA-256 vrijednostima i slikama istog commita; \`release-runtime-*.png\` su samo dokazi rada release builda.

## Gate i release

- [ ] \`:app:testDebugUnitTest\` — čeka rezultat na PR head SHA
- [ ] Android lint, \`assembleDebug\`, \`assembleRelease\`, \`bundleRelease\` — čeka CI
- [ ] CycloneDX i online OSV svih točnih release Maven verzija — čeka CI
- [ ] R8 analiza i security evidence — čeka CI
- [ ] 11/11 emulator poslova, uključujući stvarni release smoke u reference poslu — čeka CI
- [ ] Odvojena kvantitativna 10-referentna usporedba — nije obavljena u ovoj iteraciji
- [ ] Merge i izdanje v1.0.74 — **nije obavljeno**. Nakon zelenog, provjerenog mergea: samo testni prerelease ako nema vlasničkog signing ključa; APK, AAB i SHA256SUMS.

Nemoj zaključivati da je UI 1:1 niti da je Play-ready samo zato što gornji funkcionalni testovi prolaze.
