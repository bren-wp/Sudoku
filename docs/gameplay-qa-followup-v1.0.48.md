# Sudoku v1.0.48 — Gameplay, završni zaslon 3/10 (PR #55)

## Stvarni izvorni prikaz i usporedba

- Izvorni korisnikov PNG: `Slika ChatGPT 24. lis 2026. 20_47_17-6.png`, **941×1672 px**. Original nije ugrađen u aplikaciju i ne postaje lažna interaktivna pozadina.
- Ranija **stvarna emulatorska** snimka `03-game.png`: uspješni Android visual smoke [run 41132281271](https://github.com/bren-wp/Sudoku/actions/runs/41132281271) na prethodnoj v1.0.47 grani, commit `91db54bba5e156df3a2db5e81731c010eb653b6f`.
- Ove brojke su zasebno izmjerene na oba RGB PNG-a originalne razlučivosti, bez reskaliranja, i predstavljaju **povijesni baseline**, a ne rezultat aktualnog PR HEAD-a.

| Fiksno područje | Povijesni RGB MAE 0–255 |
|---|---:|
| Cijeli ekran `0,0,941,1672` | **19.381** |
| Zaglavlje `0,0,941,150` | 23.354 |
| HUD `0,140,941,250` | 23.803 |
| Ploča `40,250,890,1110` | **17.579** |
| Alatna traka `38,1100,899,1250` | 22.960 |
| Tipkovnica `40,1240,900,1485` | **18.634** |
| Krajolik `0,1490,941,1672` | 23.828 |

Ova sirova metrika uključuje drugačiji **stvarni** raspored zadataka, zadane brojeve, pogreške, označenu ćeliju, istaknute jednake brojeve, vrijeme i napredak. Ne kopirati ilustrativni raspored ili vrijednosti sa screenshot-a u radnu sesiju: to bi narušilo stvarnu igru.

## Implementacija u postojećem PR-u

1. `GamePage.kt`: odabrana ćelija koristi tamniju, prigušenu tirkiznu ispunu, dok **cijan obrub i stvarna selekcija ostaju**. Prethodna ispuna `#007A9A` s alfa .68 bila je vizualno snažnija od originala; nova `#006B87` s alfa .34 bolje čuva kontrast numeričkih vrijednosti.
2. `GamePage.kt`: `game-progress-group` i `game-tool-row` Compose tagovi omogućuju mjerenje stvarnih HUD/toolbar granica, bez ikakve slike umjesto gumba.
3. `SudokuVisualSmokeTest.kt`: novi instrumentacijski test provjerava **81 stvarno polje**, kvadratnu ploču, relativne granice susjednih ćelija, četiri poravnata 48dp alata, svih devet tipki jednake širine, te da je donji red od četiri tipke centriran ispod gornjih pet.
4. `scripts/gameplay_pixel_qa.py`: nepromjenjive izvorne regije, novi zaseban `board_top` / `board_bottom` presjek i reproducibilna mjerenja, s `regions.csv`, `overlay.png` i `difference.png`.
5. `scripts/test_pixel_compare.py`: regresijski testovi da se promjene ograničene na ploču jasno registriraju, a pogrešna 720×1280 snimka se nikad ne preskalira u referencu.

## Kako provjeriti izmjene za aktualni HEAD

1. Potreban je potpuno uspješan **Android CI** (unit testovi, QA metric testovi, lint, APK/AAB build) i **Android visual smoke** na oba emulatora (941×1672 i 720×1280) za **isti aktualni commit**.
2. Preuzeti novi `sudoku-emulator-screen-references` artefakt i koristiti stvarni `03-game.png`, nikako stari screenshot ili referentnu sliku kao zamjenu.
3. Originalnu PNG referencu bez promjena lokalno preimenovati u `references/03-game.png` i pokrenuti:

```bash
python scripts/gameplay_pixel_qa.py \
  --reference references/03-game.png \
  --actual screenshots/03-game.png \
  --output-dir qa-pixel-diff/gameplay \
  --allow-imperfect
```

4. Vizualno pregledati međusobne odnose zaglavlja, HUD-a, kvadrata ploče, 3×3 debljih razdjelnika, selekcije, bilježaka i alata te stvarno centriran red tipki 6–9. Kontrolirati drugačije dinamičke vrijednosti i uspoređivati njihovu **geometriju**, a ne forsirati jednake brojke.
5. Na 720×1280 provjeriti dosezanje svih funkcija skrolanjem, 48dp ciljeve, unos broja, bilješke, savjet, poništavanje, brisanje, pauziranje, automatsko spremanje i nastavak prethodne igre.

## Status i sljedeći zaslon

Izmjene su commitane na **postojeći PR #55**, bez promjene `com.brendigo.sudoku`, lokalne povijesti, ključeva, verzije, ovisnosti ili dozvola. **Nema tvrdnje o dokazanoj 1:1 jednakosti ili produkcijskom izdanju** prije potvrđene nove emulatorske snimke i CI provjera.

Sljedeći vizualni ekran je **4/10 — Postavke**, s ponovnim provjerama pristupačnosti, pet panela i donjeg krajolika.
