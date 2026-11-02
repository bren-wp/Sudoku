# Splash — referentno usklađivanje (2. studenoga 2026.)

## Izvorni materijal

Korisnikova referenca: uspravni `Splash` PNG (941 × 1672 px).
Postojeća stvarna emulatorska snimka: `docs/screenshots/08-splash.webp`.
Ostali zasloni i originalni logo/ikona su zasebne reference: nisu pozadine za lažiranje interaktivnog sučelja.

## Izmjene u ovoj iteraciji

- Sekundarni neonski odsjaj pozadine znaka 7–3–9, dok sam znak ostaje izvorni Compose sadržaj.
- Pomak bylinea i slogana 10 dp prema gore **samo** na referentnom portretnom rasporedu, bez forsiranja na uskim, kratkim i landscape zaslonima.
- Indikator učitavanja podignut je za 10 dp na referentnom rasporedu; ostaje pravi Canvas element s accessibility opisom.
- Nema novih mrežnih dozvola, vanjskih fontova, ugradnje screenshota kao zaslona ili promjene spremljenog napretka.

## Kriteriji prihvaćanja

1. CI: `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug` i instrumentacijski vizualni testovi.
2. Vizualna provjera: stvarni emulatorski PNG pri 941 × 1672 i 720 × 1280, bez skaliranja ili prikrivanja sadržaja, usporediti s vlasnikovim originalom pomoću `scripts/pixel_compare.py`.
3. Posebno pregledati pozicije: logo, natpis Sudoku, byline, slogan, indikator, donja ilustracija.
4. Osigurati da sadržaj ne izlazi iz zaslona na manjim uređajima i pri povećanoj veličini fonta.
5. Dok usporedba i svi navedeni testovi nisu potvrđeni, **ne tvrditi** da je Splash doslovno pixel-perfect 1:1.

## Sljedeći korak

Nakon stvarne QA provjere nastaviti na `01-home` (početni zaslon), bez mijenjanja podataka korisnika samo radi izgleda reference.
