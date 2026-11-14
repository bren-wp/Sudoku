# Sudoku v1.0.75 — mjerljivi pregled dekorativnog pejzaža i svih deset originala

**Status:** PR u razvoju; ne označavati 1:1 ni objaviti bez dokaza.

## Osnova

- Izvori: 10 vlasnikovih privatnih PNG-ova (8 × 941×1672, 2 × 1254×1254), SHA-256 provjera u `scripts/final_ten_reference_qa.py`. Datoteke se ne prenose u javni repozitorij.
- Aktualni `main`: `a17799a19d56f94d0e2d72c8dacdc1eade75927b` (v1.0.74, testni prerelease).
- Prije izmjene: stvarne Android snimke prethodnog PR-a #81 iz `sudoku-emulator-screen-references`, Actions run #43515302886, SHA `33e10b8`; produkcijski Compose UI jednak je v1.0.74.
- Razlika izvornik ↔ stvarna snimka **nije samo geometrijska**: referentne slike sadrže različit datum, broj završenih igara, napredak, odabir polja i Sudoku zagonetku. Statistika i Postignuća moraju ostati vezani uz stvarne korisničke podatke.

## Hipoteza i stvarni kod

**P1 vizualni — dekorativni donji sloj:** na Home, Daily, Statistics i Achievements stvarni scenografski sloj `scenery-reference.webp` pojavljuje se previsoko u odnosu na original: pozadinski cvjetovi i vodena površina ulaze iza kartica. U `SudokuDesign.kt` pomaknut je isključivo početak **dekorativnog**, neinteraktivnog sloja s `0.730 × visina` na `0.790 × visina`, bez izmjene ploče, HUD-a, gumba, kartica, resursa ili podataka. Splash, Gameplay, New Game i Settings imaju svoje zasebne postojeće prijelaze i nisu mijenjani.

To je **hipoteza za verifikaciju**, ne tvrdnja da se MAE poboljšao: potrebno je ponoviti autentični screenshot PR heada i odbaciti izmjenu ako donji ROI ili cijeli zaslon regresira bez opravdanja.

## Before / After (za popunjavanje nakon Android CI)

Svi RGB MAE brojevi su u intenzitetu 0–255. To nisu postoci. Prije su dokazane vrijednosti iz PR-a #81; nakon je zasad **neizmjereno**.

| Zaslon | Prije: cijeli RGB MAE | Poslije | Prije: 1:1 | Poslije |
| --- | ---: | --- | --- | --- |
| Home (Resume) | 23.903772 | čeka stvarni PNG | ne | čeka |
| New Game | 22.622370 | čeka stvarni PNG | ne | čeka |
| Gameplay | 20.361789 | čeka stvarni PNG | ne | čeka |
| Settings | 19.421990 | čeka stvarni PNG | ne | čeka |
| Daily | 24.743339 | čeka stvarni PNG | ne | čeka |
| Statistics | 26.541994 | čeka stvarni PNG | ne | čeka |
| Achievements | 29.749992 | čeka stvarni PNG | ne | čeka |
| Splash | 10.370861 | čeka stvarni PNG | ne | čeka |
| Wordmark | 18.833920 | čeka stvarni PNG | ne | čeka |
| Launcher | 16.174683 | čeka stvarni PNG | ne | čeka |

## Reproducibilna stroga provjera

`scripts/compare_visual_iteration.py` zahtijeva svih deset originala i oba skupa screenshotova, njihove stvarne dimenzije i svih deset izvornik SHA-256. Uz globalni RGB MAE bilježi MAE za stabilniji donji ROI `y ∈ [0.79h, h]`, točne RGB **i RGBA** piksele i SHA-256 svih snimki. Razlika MAE ne može nadomjestiti `exact_1_to_1`. Komanda:

```bash
python scripts/compare_visual_iteration.py \
  --reference-dir /private/owner-originals \
  --before-dir /private/v1074-android-real \
  --after-dir /private/v1075-android-real \
  --output-dir /private/qa-v1075 \
  --home-resume
```

Na svim drugim uređajima ostaju zasebni Compose i funkcionalni testovi. Brojke iz 941×1672 slike ne primjenjuju se na 240dp, tablet ili landscape.

## Testovi i release

- [ ] Python kontrolni testovi (RGB/RGBA alfa, nepripadajuće dimenzije, ROI) na head SHA
- [ ] Android JUnit, lint, debug/release APK i AAB, release runtime smoke
- [ ] CycloneDX, online OSV, R8, compiled manifest i security audit
- [ ] 11/11 Android emulator poslova
- [ ] Stvarni screenshotovi s točnog commita, ponovna globalna/regionalna MAE provjera bez skaliranja i bez izmišljenog statističkog napretka
- [ ] Verzija `1.0.75`, code `76`: **nije merge, nije release** dok se ne ispune gateovi. Bez izdavačkog ključa: samo testni prerelease.
