# Splash — referentno usklađivanje (2. studenoga 2026.)

## Izvorni materijal

Korisnikova referenca: uspravni `Splash` PNG (941 × 1672 px).
Postojeća stvarna emulatorska snimka: `docs/screenshots/08-splash.webp`.
Ostali zasloni i originalni logo/ikona su zasebne reference: nisu pozadine za lažiranje interaktivnog sučelja.

## Izmjene u ovoj iteraciji

- Sekundarni neonski odsjaj pozadine znaka 7–3–9, dok sam znak ostaje izvorni Compose sadržaj.
- Prvi pokušaj pomicanja bylinea i slogana od -10 dp uzrokovao je **stvarno preklapanje** i pad instrumentacijskih testova na tabletu, referentnom emulatoru i pri 1.8x fontu. Negativni pomaci su uklonjeni; redoslijed ostaje prirodni Compose Column flow.
- Indikator učitavanja podignut je za 10 dp na referentnom rasporedu; ostaje pravi Canvas element s accessibility opisom.
- Glowing logotip dobio je zaseban stakleni gradijentni okvir i dublji središnji 3D sjaj (obična ikona ne mijenja veličinu).
- Primarni gumbi dobili su stakleni gornji odsjaj i dvotonski obrub; tekst, ikone, klik i accessibility semantika ostaju izvorni Compose elementi.
- Nema novih mrežnih dozvola, vanjskih fontova, ugradnje screenshota kao zaslona ili promjene spremljenog napretka.

## Kriteriji prihvaćanja

1. CI: `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug` i instrumentacijski vizualni testovi.
2. Vizualna provjera: stvarni emulatorski PNG pri 941 × 1672 i 720 × 1280, bez skaliranja ili prikrivanja sadržaja, usporediti s vlasnikovim originalom pomoću `scripts/pixel_compare.py`.
3. Posebno pregledati pozicije: logo, natpis Sudoku, byline, slogan, indikator, donja ilustracija.
4. Osigurati da sadržaj ne izlazi iz zaslona na manjim uređajima i pri povećanoj veličini fonta.
5. Dok usporedba i svi navedeni testovi nisu potvrđeni, **ne tvrditi** da je Splash doslovno pixel-perfect 1:1.

## Sljedeći korak

Nakon stvarne QA provjere nastaviti na `01-home` (početni zaslon), bez mijenjanja podataka korisnika samo radi izgleda reference.

## Analiza stvarnog pada iz PR #57

- Android visual smoke run 41519151253: `tablet-capture`, `reference` i `medium` pali su zbog `Real Splash logo, heading, subtitle, slogan and loader overlap` (SudokuVisualSmokeTest.kt:116). Pri povećanoj veličini fonta padao je i `splashBrandDoesNotOverlapLoadingIndicatorAtLargeFontScale` (linija 335).
- `tablet-shard-1` pao je prije pokretanja aplikacije tijekom instalacije emulatora: `Android Emulator: Error on ZipFile unknown archive`. To je odvojeni infrastrukturni kvar; ne dokaz kvara sučelja.
- Uklonjeni su uzročni negativni tekstualni offseti. 
- **Novo testiranje nakon popravka je obavezno**. Nema zatvaranja provjere ni spajanja samo zato što je kod izmijenjen.
