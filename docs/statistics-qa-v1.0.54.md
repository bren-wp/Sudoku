# Sudoku v1.0.54 — Statistika, zaslon 6/8

## Original i usporedba

Referentni PNG vlasnika: `Slika ChatGPT 24. lis 2026. 20_47_19-8.png`, `references/06-statistics.png`, **941×1672**. Reference služe samo za vizualnu usporedbu; nisu zamijenile interaktivne Compose kontrole.

- Prije: `06-statistics.png` iz [Android visual smoke 41362633624](https://github.com/bren-wp/Sudoku/actions/runs/41362633624), v1.0.53, SHA `12a0b76aa205be2a3b3b6c2b203ff70ea5b9f77d`.
- Poslije: `06-statistics.png` iz [Android visual smoke 41363509152](https://github.com/bren-wp/Sudoku/actions/runs/41363509152), SHA `7095f9b9adb1c233ee492ec4409a172edd0599db`.
- Mjera: srednja apsolutna razlika RGB vrijednosti (0–255) na **istoj izvornoj rezoluciji**; bez reskaliranja, odabiranja boljeg kadra, maskiranja datuma, tipografije ili podataka. Niža vrijednost znači bolju globalnu pixel-diff podudarnost, ali sama nije dokaz funkcionalnosti ni 1:1 dizajna.

| Regija (x0,y0,x1,y1) | Prije MAE | Nakon MAE | Delta |
|---|---:|---:|---:|
| Cijeli zaslon (0,0,941,1672) | 26.56719 | **26.45646** | −0.11073 |
| Zaglavlje (0,0,941,185) | 23.01314 | 23.01314 | 0 |
| Četiri metričke kartice (30,185,909,550) | 33.75332 | **33.06711** | −0.68621 |
| Grafikon i krug (30,550,909,1030) | 27.57403 | 27.68292 | +0.10889 |
| Nedavni napredak (30,1040,909,1320) | 20.15334 | 20.15334 | 0 |
| Postignuća (30,1320,909,1550) | 30.24255 | 30.24255 | 0 |
| Donji pejzaž (0,1550,941,1672) | 25.65209 | 25.65209 | 0 |
| Gornje kartice (30,190,909,368) | 32.46381 | **31.62799** | −0.83582 |
| Donje kartice (30,370,909,550) | 36.14784 | **35.58290** | −0.56494 |
| Krug dovršenosti (520,550,909,942) | 29.37140 | 29.67270 | +0.30130 |
| Najbolje vrijeme (474,195,893,362) | 36.58020 | **35.07672** | −1.50348 |
| Odigrane igre (36,195,456,362) | 30.64699 | **30.28241** | −0.36458 |
| Ukupno vrijeme (36,375,456,542) | 36.67527 | **36.60336** | −0.07191 |
| Dnevni niz (474,375,893,542) | 39.20457 | **37.99924** | −1.20533 |

## Što je promijenjeno

1. `Pages.kt`: veći razmak između metričkih ikona i stvarnih podataka u četiri kartice, s odvojenim veličinama za kompaktnu i širu referentnu širinu.
2. `BrandIcon.kt`: novi lokalni vektorski `pie-chart` za oznaku kruga dovršenosti; postojeći stupčasti grafikon i dalje se koristi za razine težine. Na samoj slici prikazuje se njegov stvaran postotak, pa mjerena razlika u chart regiji ostaje pozitivna (+0.10889); to nije skriveno.
3. Nisu se mijenjali stvarna statistika, zaključana postignuća, prazno stanje povijesti, dijalozi ili pohrana. Izvor prikazuje 124 igre i 92 %, a emulator prikazuje svoje stvarne podatke; nigdje se ne umeću ilustrativni rezultati radi vizualne jednakosti.

## Potvrđeni CI testovi

- [Android CI 41363509138](https://github.com/bren-wp/Sudoku/actions/runs/41363509138): **SUCCESS**, unit testovi, Python metrike, lint i APK/AAB build.
- [Android visual smoke 41363509152](https://github.com/bren-wp/Sudoku/actions/runs/41363509152): **SUCCESS** na emulatorima 941×1672 i 720×1280, uključujući geometriju 4 ili 2×2 kartice, povijest, dijaloge i kontrolne akcije.
- Ovo su testovi UI commita `7095f9b9adb1c233ee492ec4409a172edd0599db`. Izdavački commit `v1.0.54` zasebno mora proći vlastite CI i emulatorske testove.

## Preostala odstupanja

Nema potvrđene potpune pixel-perfect 1:1 jednakosti. Posebno se razlikuju dinamičke brojke, odsutna povijest/nagrade na novom emulatoru, izgled grafikona i dio pozadinskog pejzaža. Sljedeći zaslon za zasebni QA: **Postignuća**.
