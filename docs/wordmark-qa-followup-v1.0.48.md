# SUDOKU v1.0.48 — Wordmark, zaslon 9/10 (postojeći PR #55)

## Izvorna referenca i nepovredivi podatci

- **Original korisnika**: `Slika ChatGPT 24. lis 2026. 20_47_13-1.png` (neprepravljeni **1254 × 1254 px RGBA PNG**).
- **Provjereni SHA-256 originala**: `7534ad4d8bbfef116ad7d092cb91027f5d393a6008b7ecb37a7a33aed7047fd1`.
- Alfa-bounding box (x0,y0,x1,y1), pri `alpha >= 128`: **(63, 443, 1204, 779)**. Pri `alpha >= 12`: **(45, 422, 1220, 799)**. Primjetan je stvarni svjetleći rub izvan neprozirnog jezgrenog logotipa.
- Original prikazuje **7–3–9** u zasebnoj svjetlećoj matrici lijevo, naziv **Sudoku** s bijelo/plavog prijelaza na cijan/tirkiznu desno te potpis **by Brendigo poravnat s desnim rubom naziva**. Ispred potpisa **nema vodoravne crtice**.
- Original ostaje privatan i izvan GitHub repozitorija. **Zabranjeno** ugrađivati kopiju cjelokupne slike kao lažni UI.

## Izmjene postojeće implementacije

1. `app/src/main/java/com/brendigo/sudoku/ui/SudokuDesign.kt`: `BrandHeader` i dalje sastoji **stvarne Compose znakove, nativni 3×3 SudokuLogo i dva prava teksta**; promijenjeni su potpis poravnat desno, uklonjena dodatna crtica, bijelo–cijan–tirkizni gradijent riječi i dodan mekši glow oko znaka. Dostupni su stabilni `wordmark-container`, `wordmark-symbol`, `wordmark-title` i `wordmark-byline` testTagovi. Kompaktna širina i fontovi ostaju prilagodljivi.
2. `SudokuVisualSmokeTest.kt`: instrumentirani `captureActualComposeWordmarkForTenReferenceQa` više ne pretvara proizvoljno svaki skoro-crni RGB piksel u potpuno proziran, što je brisalo anti-aliased i shadow rubove.
3. Umjesto toga na **istom emulatoru** snima nativni Compose brand na crnoj i bijeloj pozadini. S obzirom na dvije opažene snimke izračunava približan alfa kanal: `alpha = 255 - mean(whiteRGB - blackRGB)`; prema crnoj snimci rekonstruira prednji RGB. Ovakva rekonstrukcija je QA-only i nije savršen fizikalni inverz zbog gamma korekcije i anti-aliasinga. Izvorni slojevi, izlazne dimenzije i alfa rubovi nisu izrađeni iz korisnikove slike.
4. Rezultat je **transparentna** RGBA slika `09-wordmark.png`, strogo **1254 × 1254 px**, postavljena na unaprijed zadano referentno platno s početkom x=63 i sredinom oko y=611; nativni omjer širine/visine se **ne deformira**. Ovi uvjeti izvoza služe usporedbi kanonskog branding resursa, nisu dokaz da je riječ pixel-perfect. Posebno se računaju stvarne granice alfa-piksela da se vidi zaostala razlika pozicije i oblika.
5. Novi Android testni uvjeti provjeravaju stvarnu geometriju elemenata: 7–3–9 je lijevo, naziv desno, potpis ispod njega i desno poravnat; bez preklapanja.
6. `scripts/wordmark_pixel_qa.py`: samostalno uspoređuje dva **stvarna** 1254² RGBA PNG-a; daje puni RGB/RGBA/alpha MAE, vidljivu alfa-bounding box poziciju za obje slike, `bounds_delta_xyxy`, navy-composited RGB MAE za `symbol_739`, `title_sudoku`, `byline_brendigo`, te `wordmark-regions.csv`, `overlay.png`, `difference.png` i `alpha-difference.png`. Ne treba svih ostalih sedam screenshota za ovaj izolirani test.
7. `scripts/test_pixel_compare.py` uključuje regresiju gdje **isti RGB uz drugačiji alfa-kanal potpisa** mora biti prijavljen kao razlika; također odbija krive dimenzije.

## Ponovljiva završna provjera aktualnog HEAD-a

1. Provjeriti **Android CI** i **Android visual smoke** (941×1672 i 720×1280) za **isti commit**. Provjeriti Android instrumentacijski test dual-background exporta i 1254² transparentne rubove.
2. Originalni vlasnikov PNG spremiti lokalno u `references/09-wordmark.png`, bez re-encodinga; preuzeti novi **stvarni emulatorski** `09-wordmark.png` iz `sudoku-emulator-screen-references` artefakta u `screenshots/09-wordmark.png`.
3. Pokrenuti:

```bash
python scripts/wordmark_pixel_qa.py \
  --reference references/09-wordmark.png \
  --actual screenshots/09-wordmark.png \
  --output-dir qa-pixel-diff/wordmark --allow-imperfect
```

4. Posebno pregledati rub i iluminaciju 7–3–9, prijelaz boje slova, tipografiju, poravnanje potpisa, četiri stvarna ugla prozirnog PNG-a, alfa-bounding box i stvarni izgled na navy pozadini. Pregledati i umanjenu verziju na kompaktnom emulatoru.

## Status i razgraničenja

**Izmjene su commitane u isti otvoreni PR #55**. Nemamo dokaz da je aktualni PNG jednak originalu 1:1 dok aktualni CI ne dovrši export i točni PNG-ovi ne budu uspoređeni. Ne treba kopirati alfa-podatke iz reference na rezultat ni ručno maskirati razlike. Prethodna stvarna QA vrijednost za aktualni 09 export **nije dostupna** pa se ne izmišlja MAE.

Izmjene ne diraju lokalne spremljene igre, `applicationId`, verziju, ključeve, dozvole, produkcijsko potpisivanje ni Gradle konfiguraciju. PR nije spojen; novo izdanje nije objavljeno.

**Sljedeća referenca: 10/10 — adaptivna launcher ikona 7–3–9.**
