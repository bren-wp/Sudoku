# Pixel-parity revizija — Sudoku by Brendigo 1.0.38

**Izvor vizuala:** korisnikovi originalni PNG-ovi (941 × 1672) dostupni kao
referentne datoteke, ne screenshotovi nove aplikacije. Za 09-wordmark i
10-launcher-icon postoje zasebne originalne reference (1254 × 1254);
one se ovim commitom **ne proglašavaju riješenima**.

**Izvor stvarnih screenshotova:** Android emulator API 35, 941 × 1672,
320 dpi, screenshot smoke test. Rezolucija 720 × 1280 i dalje služi za
smoke test responzivnosti, ne za direktnu usporedbu s 941 × 1672 referencama.

**Metrika:** nenormalizirana apsolutna RGB razlika, prosjek po kanalu,
0–255; niže je bolje. Bez izmišljanja korisničke statistike ili
upisivanja rezultata u produkcijsku aplikaciju.

| Zaslon | v1.0.37 MAE | v1.0.38 kandidat MAE | Promjena |
|---|---:|---:|---:|
| Početni | 29,311 | 26,381 | −2,930 |
| Nova igra | 29,061 | 26,552 | −2,509 |
| Igra | 23,150 | 21,608 | −1,542 |
| Postavke | 27,726 | 24,171 | −3,555 |
| Dnevni izazov | 29,687 | 26,936 | −2,751 |
| Statistika | 30,988 | 28,813 | −2,175 |
| Postignuća | 32,184 | 30,528 | −1,656 |
| Splash | 22,485 | 14,223 | −8,262 |

Prosjek osam ekrana: **28,074 → 24,902**, pad od približno 11,3 %.
Na sedam interaktivnih ekrana prosjek je **28,872 → 26,427**.

Za točnu reprodukciju usporedbe koristite postojeći
`scripts/pixel_compare.py` s lokalnim originalnim PNG-ovima
i snimkama iz workflowa Android visual smoke, uključujući `--include-splash`.
Nije dozvoljeno dodavati zamrznute ili izmišljene korisničke podatke
kako bi izgled bio bolje ocijenjen.

## Konkretne promjene

1. Jedinstvena dekorativna mreža zamijenjena je dvjema ugaonim
   skupinama mrežnih crta na lijevom i desnom gornjem dijelu.
2. Tamni plavi gradijenti kartica, planinski slojevi, voda i latice
   približeni su izvornim tamnijim tonovima, bez brisanja njihovih
   funkcionalnih Compose kontrola.
3. Dodana je mala **236 × 162 WebP (2.692 B) izvorna ilustracija**
   koja sadrži samo donji noćni krajolik, izrezan iz korisnikove
   originalne splash reference. Statični spinner iz originala
   uklonjen je iz dekorativnog izreza; pravi Compose spinner
   ostaje funkcionalan.
4. Izrez ima postupno transparentan gornji rub, skalira se sa
   zaslonom i crta se iza svih tipki, teksta i Sudoku ploča.
   Nije uveden nijedan screenshot interaktivnog zaslona.

## Ograničenja i iduća dorada

- Vizualni identitet i sve funkcije ostali su očuvani, ali **MAE nije 0**:
  ova iteracija nije puna pixel-perfect 1:1 vjernost.
- Najveće preostale razlike su unutarnje ilustracije nagradnih
  kartica (postignuća, statistika), precizne ikone i font rendering.
- Različiti brojevi Sudoku zagonetki, vrijeme i stvarna statistika
  pridonose RGB MAE u igri i podacima; za UI-only metriku
  potrebna je kontrolirana debug snimka i maskiranje takvih regija.
- Za sljedeću iteraciju korisne su zasebne originalne logo i adaptive
  icon reference, uz nastavak optimizacije bez bespotrebnih ovisnosti.
- Reproducirani prikaz ekrana mora ostati ovisan o stvarnom stanju
  kontrolera i nastavku spremljene partije, ne o PNG overlayu.

## Kontrola regresija

- Kotlin/JUnit, Android Lint i Android release bundle build
- Instrumentirani UI testovi i stvarne snimke 941 × 1672 / 720 × 1280
- `SudokuSceneryAssetTest` potvrđuje da originalni WebP pravilno
  dekodira i transparentno prelazi u Compose pozadinu
- `SudokuVisualSmokeTest` potvrđuje navigaciju i stvarne ekrane

Podaci predstavljaju izmjerene snimke konkretne razvojne grane, a
završni release treba izgraditi iz spojenog i provjerenog `main` commita.
