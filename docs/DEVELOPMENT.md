# Sudoku by Brendigo — razvoj i verifikacija

Projekt je postojeća Kotlin / Jetpack Compose Android aplikacija. Nije potrebna
nova arhitektura za pojedinačne vizualne ili stabilnosne popravke.

## Lokalna izgradnja

- Android SDK 36, JDK 17 i Gradle 9.6.0 (kao u GitHub Actions workflowima).
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

PR [#55](https://github.com/bren-wp/Sudoku/pull/55) spojen je u `main` 30.10.2026. (UTC). Svih deset referenci sada je ponovno izmjereno na
odgovarajućem posljednjem uspješnom emulatorskom kodu; nijedna nije RGBA-identična.
Točne vrijednosti, izvori i preostala ograničenja: [završni QA](final-ten-screen-qa-v1.0.48.md).

## Potpisivanje i izdanja

Izdavačke ključeve nikada ne dodavati u repozitorij. Koristiti zaštićene
GitHub Actions tajne koje release workflow prepoznaje. Ako nisu dostupne,
AAB ostaje nepotpisan, a debug APK služi samo za testnu instalaciju.

Release workflow samostalno testira, lint-a i gradi aplikaciju prije objave ako oznaka verzije još ne postoji; nije serijski uvjetovan završetkom odvojenog Android CI push workflowa. Za novu Play verziju u pravilu je potreban nov, jedinstven `versionCode` i ispravan izdavački ključ.

Na izdanju [v1.0.48](https://github.com/bren-wp/Sudoku/releases/tag/v1.0.48) dostupni su samo testni APK i nepotpisani AAB. GitHub trenutno prikazuje stabilni release metapodatak za njih, što **ne znači** da su spremni za produkciju. Prije distribucije provjeriti potpis i oznaku izdanja.
