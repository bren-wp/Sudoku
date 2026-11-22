# Sudoku v1.0.78 — provjera Sudoku enginea i produkcijskog kandidata

**Izvor:** `bren-wp/Sudoku`, issue #85. Ovaj dokument navodi što je implementirano i što tek mora potvrditi CI, bez nagađanja o rezultatima.

## Implementirano

- `SudokuEngine.countSolutions`: postojeći minimum-remaining-values (MRV) solver sada održava bit-maske zauzetih znamenki za svaki redak, stupac i kanonsku podmrežu tijekom rekurzivnog pretraživanja. Prije svakog novog izbora ne prolazi ponovno kroz sve susjedne kućice.
- `SudokuEngine.isValidCompleted`: provjera jednakosti sada koristi bit-maske i niti jedan privremeni Kotlin `List` / `Set` po redu, stupcu ili podmreži.
- Ne mijenjaju se `SudokuEngine.create` javni argumenti, `Puzzle` struktura, generatorov redoslijed RNG poziva, identitet seedova niti format spremanja podataka.
- JUnit regresije obuhvaćaju 1.854 reproducibilne seed / veličina / težina kombinacije, validnost i jedinstvenost, nemijenjanje ulaza, prazne ploče i solver limit, oštećene znamenke, kratke ploče te puni završeni Sudoku.

## Testiranje

Izvršiti:

```bash
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:lintRelease \
  :app:assembleDebug :app:assembleRelease :app:bundleRelease :app:cyclonedxDirectBom
```

Po spajanju na `main`, objava je dopuštena tek nakon uspješnog Android CI i svih 11 emulatorskih profila na **istom SHA-u**, OSV skeniranja i release potpisivanja/provjere. U testovima se trajanje generiranja bilježi u milisekundama, ali uspješna funkcionalna provjera **nije** mjereni dokaz poboljšanja p95/p99 trajanja na fizičkim uređajima. Vlasnikove vizualne PNG reference nisu priložene ovom commitu; stoga nije potvrđena stroga 1:1 jednakost zaslona.

## Izdavanje

- Kandidat: `1.0.78` / `versionCode=79`.
- Ako je `sudoku-release` environment ispravno konfiguriran s postojećim Play upload ključem i provjerenim certifikatom, može nastati potpisana produkcijska gradnja.
- Ako izdavačke tajne nisu prisutne, workflow može izdati **isključivo testni prerelease** (`-test.apk` i `-unsigned.aab`), nikada ga lažno označiti Google Play-ready.
- PR se ne spaja automatski. Stvarno GitHub izdanje mora biti potvrđeno na Releases stranici nakon svih uvjeta.

## Preostali audit #85

Kalibracija stvarne težine pomoću logičkih tehnika, još opsežniji seed/performance stress testovi, puna provjera oporavka nakon disk-failure scenarija, verificirani Gradle wrapper, 10 autentičnih pixel-perfect referenci i testiranje na fizičkim uređajima ostaju otvoreni zadaci.
