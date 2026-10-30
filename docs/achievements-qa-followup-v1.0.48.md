# SUDOKU v1.0.48 — Postignuća, završni vizualni prolaz 7/10 (PR #55)

## Referenca i stvarna povijesna usporedba

- Original korisnika: `Slika ChatGPT 24. lis 2026. 20_47_19-9.png`, točno **941 × 1672 px**, SHA-256: `461e193d686e3b778c97d50ec2a661317d5115be465c909a85bd8405e5e3b5c8`.
- Stvarna emulatorska snimka **ranije v1.0.47 grane**, `07-achievements.png` iz uspješnog [Android visual smoke runa 41132281271](https://github.com/bren-wp/Sudoku/actions/runs/41132281271), SHA-256: `7edafdc748ecbbb962d794fd66a289a5d66563d24bb527576835bfba6bb138a5`.
- Ova usporedba obrađena je na neizmijenjenim RGB slikama **iste** rezolucije, bez reskaliranja, bez automatskog poravnanja i bez maskiranja promjenjivih podataka.

| Fiksna regija, desni/donji rub isključiv | Povijesni RGB MAE, 0–255 |
|---|---:|
| Cijeli zaslon `0,0,941,1672` | **28.930** |
| Zaglavlje `0,0,941,160` | 24.716 |
| Bodovi i otključano `38,160,894,319` | 30.602 |
| Prvi red `36,333,895,642` | **38.326** |
| Drugi red `36,650,895,956` | **39.605** |
| Treći red `36,974,895,1274` | 23.099 |
| Četvrti red `36,1285,895,1485` | 23.652 |
| Krajolik `0,1485,941,1672` | 25.624 |

**Izričita ograničenja:** prethodni `docs/achievements-qa-v1.0.47.md` bilježi drugi (stariji v1.0.46) baseline od 30.986. Ovdje su novi povijesni rezultati iz v1.0.47; nisu rezultat trenutačnih PR izmjena. Originalna ilustracija prikazuje **420 bodova, 8/24 otključanih** i primjerene napretke, dok stvarni Android korisnik ima vlastite spremljene podatke. Pokušaj umjetnog postavljanja 420/8, 5/7 ili punog napretka ne bi predstavljao stvarni proizvod. Niti jedan test ne smije krivotvoriti trajno stanje kako bi poboljšao pikselnu metriku.

## Konkretne promjene u istom PR-u

1. `Pages.kt`: osam istaknutih kartica, svih **24 stvarnih postignuća**, bodovi, spremanje napretka, zaključavanja i lokalna evidencija ostaju stvarni Compose UI, ne pozadinska slika niti testni podaci.
2. Ikone na karticama imaju diskretan **radijalni sjaj prilagođen vlastitoj boji nagrade**. Kod zaključanih ikona sjaj je prigušen, a lokot ostaje vidljiv. To približava svjetleće vektorske ikone izvornom dizajnu bez simuliranja otključavanja.
3. Svaki napredak dobio je stabilan `achievement-progress-<naziv>` testTag i čitljiv semantički opis **stvarnog napretka**. Granice grafičkog punjenja ostaju ograničene na 0–100%.
4. `showAll` i trenutno otvoreno postignuće koriste `rememberSaveable`. Sprema se samo **stabilan naslov odabrane nagrade**, a podaci o statusu/bodovima uvijek se iznova čitaju iz `SudokuController`, ne iz zastarjele kopije objekta.
5. `SudokuVisualSmokeTest.kt`: novi Android test otvara svih 24 postignuća, potvrđuje pristupačan prikaz napretka, otvara detalje, rekonstruira Activity i provjerava očuvanje otvorenog dijaloga i proširene liste.
6. `scripts/achievements_pixel_qa.py` uspoređuje osam nepromjenjivih izvornokoordinatnih područja, izrađuje `regions.csv`, `overlay.png`, `difference.png`, mjeri RGB MAE i postotak kanala unutar ±12.
7. `scripts/test_pixel_compare.py`: novi testovi dokazuju da se razlika iz prvog reda registrira ondje, ne u drugom redu ili footeru, te da se 720×1280 screenshot ne preskalira na originalnih 941×1672.

## Potrebna aktualna provjera

1. Za **isti aktualni HEAD**, potvrditi uspješan `Android CI`: Kotlin testovi, Pixel QA Python regresije, lint, debug/release build i AAB.
2. Potvrditi `Android visual smoke` na 941×1672 i 720×1280, uključujući novi test rekreacije Activity i sva 24 postignuća.
3. Preuzeti **novu stvarnu** `07-achievements.png` iz artefakta `sudoku-emulator-screen-references`, lokalno staviti u `screenshots/`. Originalni PNG zadržati u `references/07-achievements.png`; reference ne commitati.
4. Izvršiti:

```bash
python scripts/achievements_pixel_qa.py \
  --reference references/07-achievements.png \
  --actual screenshots/07-achievements.png \
  --output-dir qa-pixel-diff/achievements \
  --allow-imperfect
```

5. Provjeriti ukupnu razliku i prvih osam kartica, pozicije ikona/lokota, natpise, napredak, nagradne bodove, krajolik, mogućnost otvaranja svih 24 postignuća i interakciju pri povećanom fontu.

**Status:** izmjene su commitane u isti otvoreni PR #55. Rezultat 1:1 novog HEAD-a nije dokazan dok nova stvarna emulatorska slika i svi testovi ne prođu. Nije spajan PR niti objavljen release.

**Sljedeći zaslon:** 8/10 — Splash (završna provjera na stvarnim emulatorima), a nakon njega reference 9/10 i 10/10 (wordmark i ikona).
