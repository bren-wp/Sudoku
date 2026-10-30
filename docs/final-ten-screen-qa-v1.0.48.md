# Sudoku v1.0.48 — objedinjeni audit 10 izvornih referenci (PR #55)

## Status i cilj

Svih deset vizualnih referenci obrađeno je u **postojećem PR-u #55**, grana `feature/sudoku-v148-settings-pixel-qa`. To je **stanje koda i kontrolnih testova**, ne potvrda da je UI doista pixel-perfect. Do zelenih Android CI + Android visual smoke rezultata i mjerenja **novih stvarnih PNG-ova aktualnog HEAD-a** nije dopušteno tvrditi da je 1:1 provjera završena ili izdavati produkcijski release.

### Povijesni, reproducirani baseline iz pravog Android emulatora

Izvor: `sudoku-emulator-screen-references` iz [uspješnog Android visual smoke runa 41132281271](https://github.com/bren-wp/Sudoku/actions/runs/41132281271), commit `91db54bba5e156df3a2db5e81731c010eb653b6f` (raniji v1.0.47). Svaki original uspoređen je s **pravom** odgovarajućom emulatorskom PNG datotekom pomoću Pillow/ImageChops/ImageStat, u njegovoj originalnoj rezoluciji, bez resizea i bez pomicanja slikovnih podataka.

| Redni broj / prikaz | Izvorne dimenzije | Povijesni puni RGB MAE (0–255) | Alfa MAE | Navy composited RGB MAE |
|---|---|---:|---:|---:|
| 01 — Home | 941 × 1672 | 23.976 | — | — |
| 02 — Nova igra | 941 × 1672 | 25.017 | — | — |
| 03 — Aktivna igra | 941 × 1672 | 19.381 | — | — |
| 04 — Postavke | 941 × 1672 | 24.519 | — | — |
| 05 — Dnevni izazov | 941 × 1672 | 23.594 | — | — |
| 06 — Statistika | 941 × 1672 | 26.637 | — | — |
| 07 — Postignuća | 941 × 1672 | 28.930 | — | — |
| 08 — Splash | 941 × 1672 | **12.498** | — | — |
| 09 — Transparentni wordmark | 1254 × 1254 | 18.152 | **216.408** | 36.477 |
| 10 — Transparentna launcher ikona | 1254 × 1254 | 47.935 | **97.079** | 38.967 |

Najmanji povijesni puni RGB MAE ima Splash; **alfa-kanal** starog branding izvoza izrazito odudara zbog neprozirnog studijskog ili adaptivnog platna. Stari rezultati nisu rezultat ispravljenog dual-background wordmark izvoza niti novog transparentnog Android foreground izvoza. Razlika od 0 nije postignuta. Za ilustrativne ekrane s korisnički ovisnim stanjem (puzzle, statistika, dnevni izazov i postignuća) sirovi MAE uključuje i legitimno drukčije stvarne podatke.

## Jedinstvena završna provjera dodana u PR

Dodana je `scripts/final_ten_reference_qa.py`: jedan alat koji mora dobiti **svih 10 originala**, svih **10 stvarnih Android izlaza** i **11. monokromatsku Android ikonu**. Ne proizvodi reference ni sintetičke snimke, ne preskalira ni jedan PNG i provjerava:

- native PNG format i **941×1672** za ekrane 01–08 / **1254×1254 RGBA** za branding 09–10;
- neizmijenjene izvornike za `09-wordmark` i `10-launcher-icon` putem SHA-256, bez zamjene novim renderom;
- neovisni Android `11-launcher-monochrome.png` vektorski alpha mask s prozirnim uglovima i bijelim stvarnim slojevima;
- isti puni RGB MAE za 01–08, puni RGBA/alpha MAE za 09–10, kompozit preko `#021538`, nepromjenjive regionalne usporedbe i dva stvarna alfa bounding boxa;
- osobni `overlay.png`, `difference.png`, `regions.csv` i `alpha-difference.png` po prikazu, gdje je primjenjivo;
- objedinjene `all-ten-report.csv` i `all-ten-report.json` sa SHA-256 originala/izvoza, **točnim** indikatorom `all_ten_exact_1_to_1` te kontrolom zasebnog themed resursa.

Alat **prekida audit** ako samo jedan obvezni PNG nedostaje, ako su mu dimenzije pogrešne ili ako se pokuša podmetnuti drugi branding PNG kao original. `--allow-imperfect` samo omogućuje izvoz izvještaja uz izlazni kod 0; **ne** proglašava 1:1 jednakost.

Regresijski Python testovi sada provjeravaju obvezni komplet datoteka te strogo odbijanje neprozirnog ili pogrešno dimenzioniranog branding PNG-a. Testovi se izvršavaju u postojećem `Android CI` Python `unittest discover` koraku.

## Reprodukcija na aktualnom PR HEAD-u

1. Na [GitHub Actions](https://github.com/bren-wp/Sudoku/actions) čekati **iste SHA vrijednosti** za uspješan Android CI i Android visual smoke (reference **941×1672**, compact **720×1280**).
2. Iz uspješnog **novog** `sudoku-emulator-screen-references` artefakta preuzeti PNG-ove 01–11 u lokalni direktorij `screenshots/`. Nije dopušteno zamijeniti ih starim v1.0.47 PNG-ovima.
3. Neizmijenjene vlastite originale preimenovati u `references/01-home.png` ... `references/10-launcher-icon.png`. Originali **nisu** i ne smiju biti ugrađeni u APK ni spremljeni kao lažni stvarni screenshot.
4. Pokrenuti (potreban Python + Pillow):

```bash
python scripts/final_ten_reference_qa.py \
  --reference-dir references \
  --actual-dir screenshots \
  --output-dir qa-pixel-diff/final \
  --allow-imperfect
```

5. Pregledati `qa-pixel-diff/final/all-ten-report.csv`, `all-ten-report.json`, vizualne `overlay`/RGB/alpha razlike i testove native UI geometrije na obje gustoće. Razdvojiti nepoklapanje dinamičkog stanja od stvarnog UI buga.
6. Provjeriti aplikacijsku funkcionalnost (4×4/6×6/9×9, nastavak/spremanje, dnevni izazov, statistika, 24 postignuća), dostupnost barem 48dp ciljeva, čuvanje opcija pri rekreaciji Activityja i ispravno pokretanje Android 12+ / Android 13+ launchera.
7. Prije izdavanja: potvrditi zelen JUnit, Pixel QA, lint, release APK/AAB, signed artefakt stvarnim izdavačkim ključem, verziju koda i nepostojanje nesigurnih dozvola ili mrežnih poziva. **Ako izdavački ključ nije dostupan, unsigned AAB nije Play-ready.** Ne spajati PR/objavljivati na temelju samih povijesnih mjerenja.

## Zaključak audita

Prijašnje kolo dovršilo je **implementacijske prolaske 1/10 do 10/10** i ove se provjere sada mogu pokrenuti u jednom potezu. **Aktualni rezultat stvarnog 1:1, zelenog Android CI-ja i spremnosti za produkciju još nije potvrđen.** Status u ovom dokumentu ne zamjenjuje stvarne rezultate aktualnih GitHub workflowa.

Popis lokalnih dokumenata po ekranu: `docs/home-qa-followup-v1.0.48.md`, `docs/new-game-qa-followup-v1.0.48.md`, `docs/gameplay-qa-followup-v1.0.48.md`, `docs/settings-qa-followup-v1.0.48.md`, `docs/daily-qa-followup-v1.0.48.md`, `docs/statistics-qa-followup-v1.0.48.md`, `docs/achievements-qa-followup-v1.0.48.md`, `docs/splash-qa-followup-v1.0.48.md`, `docs/wordmark-qa-followup-v1.0.48.md` i `docs/launcher-qa-followup-v1.0.48.md`.
