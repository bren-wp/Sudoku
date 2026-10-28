# Sudoku by Brendigo — v1.0.35 audit stabilnosti, privatnosti i vizualne vjernosti

## Polazno stanje

Projekt je nastao nad postojećim `main` commitom
`faaaa46599a0c065bcd153957c2d33c435062d69` (v1.0.34). Zadržani su
postojeći naziv paketa, način pohrane i Kotlin/Jetpack Compose arhitektura.

Originalne korisničke reference: osam zaslona (Početni, Nova igra, Sudoku,
Postavke, Dnevni izazov, Statistika, Postignuća, Splash), logotip i ikona.
Cilj je interaktivni 1:1 izgled, **ne** prikaz neinteraktivnih bitmap kopija.

## Kritična korekcija: partija i bodovi

U v1.0.34 `recordWin()` je pozivao `saveStats()` prije spremanja dovršene
partije, pa prekid procesa između dva odvojena SharedPreferences upisa
teoretski može ostaviti već uknjiženu pobjedu i nedovršenu partiju.

U v1.0.35 `saveGame()` u **istoj SharedPreferences.Editor transakciji**
pohranjuje: zadnju partiju, posebno spremljenu običnu/dnevnu partiju,
njihovu Undo povijest, statistiku i identičnu rezervnu kopiju statistike.
Android `onStop()` ostaje sinkron; ostatak upisa je asinkron s automatizmom
spremanja. To smanjuje rizik nekonzistentnih podataka, ali nije zamjena
za neovisnu sigurnosnu kopiju uređaja.

Kod oporavka statistike provjerava se primarni zapis pa `stats_backup`.
Preveliki ili nevaljani JSON zapisi ne izazivaju pad aplikacije. Povijest je
ograničena na 50, a datumi na 4000 zapisa. Za spremljenu partiju dodatno
se provjeravaju broj korištenih savjeta i ograničenje veličine JSON-a.
Nevaljane Undo bilješke se filtriraju prije obnove.

## Ispravnost statistike

- `totalSeconds` ostaje povijesno vrijeme **dovršenih** partija za
  kompatibilnost starog postignuća.
- `totalPlayedSeconds` broji i nedovršene partije te se koristi za brojku
  „Ukupno vrijeme” uz objašnjenje da se pauza ne računa.
- Nadogradnja koristi `totalSeconds` kao početni iznos samo za stare
  instalacije koje nemaju novu vrijednost.
- U načinu Sudoku 4×4/6×6/9×9 odbacuju se nevaljane dimenzije regija.

## Vizualna dorada

Zadržani su nativni elementi: Sudoku mreža, tipkovnica, gumbi, kartice,
dinamički napredak, animacije te stvarni podaci na svim ekranima.

Ispitana je izraženija mjesečina, veći paviljon i veći vektorski cvjetovi.
Prava 941×1672 usporedba iz artefakta `12858047041` pokazala je regresiju
prosječnog globalnog RGB MAE za sedam glavnih zaslona s **28.907 na 30.557**
(+1.650), pa je ova promjena u cijelosti **vraćena na provjereni v1.0.34
vizualni kod**. Nakon ponovljenog snimanja očekuje se isti osnovni izgled,
bez zamjene interaktivnih Compose elemenata bitmapom. Zajednička pozadina
i dalje je najveća razlika prema izvornim ilustracijama, a njezino
izjednačavanje ostaje zaseban QA zadatak.

Prethodno dokumentiran baseline v1.0.34 — globalni nenormalizirani RGB MAE
po zaslonima: Home 29.311, New Game 29.061, Game 23.387, Settings 27.729,
Daily 29.687, Stats 30.988, Achievements 32.184 i Splash 22.485.
Razlike uključuju različite iznose, vrijeme i nasumične zagonetke;
takve brojke nisu dovoljne da same potvrde 1:1.

## Testni kriteriji i otvorena ograničenja

1. `gradle :app:testDebugUnitTest` — postojeći Sudoku generator,
   pravila, datumi, te test neispravnih dimenzija solvera.
2. `gradle :app:lintDebug` — statička analiza.
3. `gradle :app:assembleDebug :app:assembleRelease :app:bundleRelease`.
4. Emulator 941×1672 i 720×1280 — navigacija i interakcije, test
   `SudokuStatsDurabilityTest` (pobjeda, restart, kvar primarne statistike).
5. Workflow `marketing-screenshots.yml` — stvarne WebP snimke osam
   zaslona iz emulatora, provjera dimenzija i PNG potpisa prije objave.
6. Potpuna vizualna provjera pomoću `scripts/pixel_compare.py` i
   originalnih referenci. Razlike u dinamičkom sadržaju treba izolirati.

**Preostalo:** nije implementiran izvoz/uvoz sigurnosne kopije na uređaj,
nema globalne online ljestvice, nije dokazana pokrivenost svakog mogućeg
crasha/ANR-a ili punih 1:1 vizualnih podudaranja. AAB bez izdavačkog
potpisa ne može se izravno objaviti na Google Playu. Tvrdnje o sigurnosti
moraju ostati proporcionalne stvarnim testovima.

Dodatni kontrolni dokument: `PRIVACY.md`. Postojeća otvorena pitanja
vizualne vjernosti i izvoza napretka prate se u GitHub Issues #39 i #41.
