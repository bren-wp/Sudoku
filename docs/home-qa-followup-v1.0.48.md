# SUDOKU — Home, završni QA prolaz 1/10 (PR #55)

## Original i provjerljivi prethodni rezultat

- Vlasnikova **stvarna** referenca: `Slika ChatGPT 24. lis 2026. 20_47_15-4.png`, **941 × 1672 px**.
- Stvarni Android screenshot prethodnog uspješnog CI izdanja: `01-home.png`, `Android visual smoke` [run 41132281271](https://github.com/bren-wp/Sudoku/actions/runs/41132281271), commit `91db54bba5e156df3a2db5e81731c010eb653b6f` (prethodna razvojna verzija v1.0.47).
- Referenca i stvarna snimka uspoređene su **bez reskaliranja**. Ove brojke nisu mjerenje trenutačnog PR HEAD-a.

| Regija (x0,y0,x1,y1) | Povijesni RGB MAE (0–255) |
|---|---:|
| Cijeli kadar `0,0,941,1672` | **23.976** |
| Zaglavlje `0,0,941,360` | 18.931 |
| Nastavi igru `35,363,896,751` | 25.339 |
| Glavne kartice `37,766,900,1063` | **28.556** |
| Kompaktne kartice `38,1073,898,1265` | 29.304 |
| Tjedni niz `38,1275,897,1437` | **30.832** |
| Donji krajolik `0,1450,941,1672` | 21.241 |

Referenca prikazuje ilustrativni dnevni niz i spremajuću partiju; stvarni UI mora pokazivati **stvarne korisničke podatke**, pa sadržaj brojeva/dana/igara ne smije biti krivotvoren samo radi slike 1:1. Pri procjeni geometrije i kvalitete koristiti iste determinističke podatke.

## Izmjene ovog prolaza

- Uklopljena tanko naglašena vodoravna linija **ispod** niza stvarnih kružnih markera u širokom Home rasporedu, kao na referenci; linija je dekorativna i ne mijenja interakciju ni podatke.
- Svih sedam dana ima stabilan `home-weekday-0` … `home-weekday-6` Compose testTag; na referentnom i kompaktnom rasporedu sedmodnevni red ima `home-week-days`.
- Proširen Android instrumentacijski test da provjerava sedam stvarnih markera unutar panela, bez presijecanja, s očuvanim ispravnim redoslijedom.
- Dodan `scripts/home_pixel_qa.py` za točno sedam fiksnih regija referentnog zaslona, uključujući puni RGB MAE, postotak kanala unutar ±12, `regions.csv`, `overlay.png` i `difference.png`. Regije se **nikad ne pomiču** radi kozmetički boljeg rezultata.
- Dodani Python regresijski testovi koji otkrivaju odstupanja u karticama i odbijaju nevaljanu veličinu 720×1280 u usporedbi s 941×1672 referencom.

## Izvođenje aktualne provjere (potrebna nova CI snimka)

1. Nakon prolaska oba posla `Android visual smoke` preuzeti aktualni artefakt `sudoku-emulator-screen-references`, ne povijesni iz runa 41132281271.
2. Lokalno osigurati vlasnikov **neizmijenjeni** `references/01-home.png`; nije dio aplikacije niti javnog repozitorija.
3. Pokrenuti `python scripts/home_pixel_qa.py --reference references/01-home.png --actual screenshots/01-home.png --output-dir qa-pixel-diff/home --allow-imperfect`.
4. Pregledati original, nativni screenshot, `overlay.png`, `difference.png` i `regions.csv`. Zabilježiti stvarna odstupanja i je li razlika u ilustrativnom stanju ili geometriji.
5. Usporediti i kompaktnu emulatorsku snimku vizualno (720×1280) **bez** lažnog preskaliranja na referencu.
6. Potvrditi unit test, Pixel QA test, Android lint, debug/release APK/AAB i instrumentirane UI testove za aktualni HEAD.

**Status:** novi kod jest dodan postojećem PR-u, ali mjerljiva 1:1 jednakost trenutačnog commita nije potvrđena dok nova stvarna CI snimka i testovi nisu gotovi. Novi release nije kreiran.

**Sljedeći ekran po izvornom redoslijedu:** 2/10 **Nova igra** (`02-new-game.png`).
