# Sudoku v1.0.48 — Splash, završni zaslon 8/10 (postojeći PR #55)

## Izvori i izravno izmjerena prethodna snimka

- **Korisnikov original:** `Slika ChatGPT 24. lis 2026. 20_47_14-3.png`, **941 × 1672 px**, SHA-256 `795992a79de684f67b3f5924fce7377ed429f4e5c272124c16115ad9885f636c`.
- **Stvarna Android emulatorska snimka prethodne razvojne v1.0.47 grane:** `08-splash.png`, iz uspješnog [Android visual smoke runa 41132281271](https://github.com/bren-wp/Sudoku/actions/runs/41132281271), SHA-256 `96e1776c51c5e820d076a7133becd90b8fda95c8c263bde459880aa3b3141566`.
- Oba PNG-a su uspoređena **u izvornoj rezoluciji bez resizea ili pomicanja**, kao RGB slike. Razlike su **povijesni baseline**, ne novi rezultat aktualnog PR HEAD-a.

| Fiksna regija (x0,y0,x1,y1), right/bottom exclusive | Povijesni RGB MAE (0–255) |
|---|---:|
| Cijeli kadar `0,0,941,1672` | **12.498** |
| Gornja dekorativna mreža `0,0,941,430` | 10.565 |
| Logotip i sjaj `280,400,665,810` | **33.352** |
| Natpis Sudoku, byline i slogan `160,675,780,1000` | **28.856** |
| Donji krajolik `0,1100,941,1672` | 6.976 |
| Indikator učitavanja `365,1450,580,1610` | 8.250 |

Prethodni `docs/splash-qa-v1.0.41.md` izvještava drukčiji stariji baseline MAE 13.692 iz runa 40802093754; povijesne mjere iz dvaju emulatora se ne smiju miješati s rezultatom nove implementacije. Krajolik stvarnog Android rendera ima manje detalja nego izvorni PNG; test to neće maskirati.

## Promjene u istom PR-u #55

1. `Pages.kt`: iza **stvarnog** `SudokuLogo` na Splashu dodan je zaseban dekorativni `drawBehind` s koncentriranim cijan i plavim radijalnim sjajem, pomaknutim prema donje-lijevom rubu znaka kao u originalnoj 941px referenci. Logo ostaje nativni `Composable` 3×3 s dinamičkom geometrijom; nijedna slika screenshota nije postavljena preko UI-a.
2. `SudokuDesign.kt`: za `glowing=true` znak postavljeni su referenci bliži zaobljeni rubovi `19dp` izvana / `17dp` iznutra. Mali logotipi na ostalim zaslonima ostaju sa starim `13dp` / `11dp` rubovima.
3. `SudokuVisualSmokeTest.kt`: već postojeća stvarna `08-splash.png` emulatorska snimka sada dodatno testira centriranost 7–3–9 znaka, minimalnu veličinu, vertikalni redoslijed pravog naziva/byline/slogana, odsutnost preklapanja i centrirani učitavač na **941×1672** i **720×1280**.
4. `scripts/splash_pixel_qa.py`: mjeri šest unaprijed određenih, nepromjenjivih izvornih područja te sprema `regions.csv`, `overlay.png`, `difference.png`, RGB MAE i postotak piksela s kanalnim odstupanjem najviše 12.
5. `scripts/test_pixel_compare.py`: Python regresija za izolirani drift logotipa i eksplicitno odbijanje 720×1280 snimke kao lažne 941px reference.

## Kako reproducirati novu završnu usporedbu

1. Za **isti posljednji HEAD** potvrditi uspješan `Android CI` (testovi, lint, APK/AAB) i `Android visual smoke` za oba emulatora.
2. Preuzeti **novi** `sudoku-emulator-screen-references` artefakt s pravom `08-splash.png`. Ne uspoređivati novi kod sa starim screenshotom i taj stari rezultat pripisivati novom commitu.
3. Neizmijenjeni vlasnikov PNG spremiti u `references/08-splash.png`; novi stvarni screenshot u `screenshots/08-splash.png`, i pokrenuti:

```bash
python scripts/splash_pixel_qa.py \
  --reference references/08-splash.png \
  --actual screenshots/08-splash.png \
  --output-dir qa-pixel-diff/splash \
  --allow-imperfect
```

4. Pregledati overlay, raw diff, sve šest ROI regija, originalnu 941px kompoziciju, mali 720px ekran i font-scale 1.8x, te funkcionalnost nativnog Android 12+ system splash-a i no-motion stanja.

**Status:** stvarne izmjene su commitane u **postojeći PR #55**. Potpuno poklapanje **1:1 nije potvrđeno** sve dok se ne dovrši nova CI provjera i ne usporede stvarne snimke. Nije objavljeno novo Play izdanje.

**Sljedeće reference:** 9/10 — transparentni wordmark i 10/10 — adaptivna 7–3–9 ikona; obje su originali 1254×1254 s RGBA provjerom dokumentiranom u `docs/branding-qa-v1.0.48.md`.
