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
Android Lint i nepotpisane APK/AAB buildove. `visual-smoke.yml` izvršava
11 emulatorskih QA profila (uključujući referentni 941×1672 i tablet
shardove); za svaki commit na `main` potrebno je svih 11 uspješnih
poslova za izdavanje. Potvrda prolaska funkcionalnog testa **nije** dokaz
pixel-perfect 1:1 jednakosti.

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

Sigurnost izdavačkih ključeva obvezna je. Na GitHubu konfigurirati
`Settings → Environments → sudoku-release` s obveznim odobravateljima
(required reviewers), ograničenim deployment granama i sljedećim tajnama
**isključivo unutar tog zaštićenog environmenta**:

- `SUDOKU_KEYSTORE_BASE64` — sadržaj postojećeg izdavačkog keystorea.
- `SUDOKU_SIGNING_KEY_ALIAS`
- `SUDOKU_SIGNING_STORE_PASSWORD`
- `SUDOKU_SIGNING_KEY_PASSWORD`

U istom environmentu postaviti varijablu
`SUDOKU_EXPECTED_CERT_SHA256` na **provjereni SHA-256 otisak postojećeg
Google Play upload certifikata**, ne na otisak Googleova zasebnog
App Signing certifikata (ako je uključen Play App Signing). Otisak
potvrditi u Play Console i prema važećem keystoreu. Nijedan privatni ključ
ili lozinku ne smije se predati unutar PR-a.

`android.yml` za PR i `main` nikad ne čita izdavačke tajne.
`publish-release.yml` reagira na završetak Android CI i Android visual
smoke workflowa ili ručno pokretanje na `main`. `gate` posao sa samo
read pristupom provjerava da oba workflowa imaju **uspješan zaključak na
istom aktualnom SHA-u** te da je svih 11 imenovanih emulatorskih poslova
uspjelo. Zatim zaštićeni `publish` posao opet gradi i skenira stvarne
release ovisnosti. Neposredno prije izdavanja ponovno provjerava aktualni
`main`.

Ako je dostupan odgovarajući postojeći ključ, nakon gradnje provjerava
potpis APK-a i AAB-a te podudarnost oba otiska s pinanim certifikatom
*prije* `gh release create`. Neusklađen, djelomičan ili nepotvrđen
izdavački potpis blokira produkcijsku objavu. Ako izdavačkih tajni nema,
može nastati samo jasno označen **testni prerelease** s debug APK-om i
nepotpisanim AAB-om. Testni APK **nije** prikladan za Google Play objavu.

Kontrolni offline testovi:

```bash
bash scripts/test_release_ci_gates.sh
bash scripts/test_publisher_certificate.sh
bash scripts/test_verify_release_assets.sh
```

Za stvarnu Android gradnju potreban je Gradle 9.6.0, Java 17 i Android SDK
36. Verificirani Gradle wrapper još nije dodan: to je zaseban otvoreni
zadak iz [audita #85](https://github.com/bren-wp/Sudoku/issues/85).
Dodavanje environmenta samo po sebi **nije** dovoljno: vlasnik
repozitorija mora stvarno uključiti zaštitna pravila i branch protection.
Izdanje ne smije biti proglašeno spremnim za produkciju bez prođenog
CI-ja, valjanog certifikata i stvarne Play provjere.

Povijesni `v1.0.77` je testni prerelease; izmjena workflowa sama po
sebi ne stvara novu produkcijsku verziju.
