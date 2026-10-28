# Sudoku by Brendigo — razvoj i verifikacija

Projekt je postojeća Kotlin / Jetpack Compose Android aplikacija. Nije potrebna
nova arhitektura za pojedinačne vizualne ili stabilnosne popravke.

## Lokalna izgradnja

- Android SDK 36, JDK 17 i Gradle 8.13.
- Android minSdk 26, targetSdk 36.

```bash
gradle :app:testDebugUnitTest
gradle :app:lintDebug
gradle :app:assembleDebug
gradle :app:bundleRelease
```

## CI i emulatorske provjere

`android.yml` pokreće Kotlin/JUnit, Python testove pixel-compare metrike,
Android Lint i APK/AAB buildove. `visual-smoke.yml` provjerava interakcije
i ekran na 941×1672 i 720×1280 emulatorima. Potvrda prolaska funkcionalnog
testa **nije** dokaz pixel-perfect 1:1 jednakosti.

Snimke aplikacije u README nastaju iz stvarnog Android emulatora i osvježavaju
se pomoću `marketing-screenshots.yml`.

- Izgled i dizajnerske odluke: [DESIGN.md](DESIGN.md)
- Reference, metode i točne mjerne vrijednosti: [pixel-qa.md](pixel-qa.md)
- Povijest promjena: [CHANGELOG.md](../CHANGELOG.md)
- Mjerna osnovica i ograničenja: [visual-parity-report.md](visual-parity-report.md)

## Otvorena vizualna usklađivanja

Nekadašnji issue #11 je obrisan (GitHub vraća HTTP 410); nadzor se nastavlja
kroz [PR #38](https://github.com/bren-wp/Sudoku/pull/38). Sedam referentnih
ekrana i splash nisu još objektivno potvrđeni kao 1:1.

## Potpisivanje i izdanja

Izdavačke ključeve nikada ne dodavati u repozitorij. Koristiti zaštićene
GitHub Actions tajne koje release workflow prepoznaje. Ako nisu dostupne,
AAB ostaje nepotpisan, a debug APK služi samo za testnu instalaciju.

Glavna grana automatski objavljuje izdanje tek nakon uspješnog Android CI
push testa i samo ako odgovarajući tag još ne postoji. Verziju i versionCode
povećati prije spajanja, a PR spojiti tek nakon oba uspješna emulatorska testa.
