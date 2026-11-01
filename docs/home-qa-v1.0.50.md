# Sudoku v1.0.50 — Home / početni zaslon, iteracija 2/8

## Izvori i cilj

Vlasnikov izvorni PNG: `01-home.png`, **941×1672**, iz serije `Slika ChatGPT 24. lis 2026. 20_47_15-4(9).png`. Uspoređeno s **pravom** Android emulatorskom snimkom bez promjene rezolucije, preslagivanja slojeva ili maskiranja dinamičkog sadržaja.

- Prije: stvarni Android emulator [run 41353665989](https://github.com/bren-wp/Sudoku/actions/runs/41353665989), Sudoku v1.0.49 (941×1672).
- Nakon: stvarni Android emulator [run 41355612738](https://github.com/bren-wp/Sudoku/actions/runs/41355612738), commit `b1c8bb035022649250acaea9c029ce2a8f80128a`, artefakt `sudoku-emulator-screen-references`.
- Python/Pillow izračun: RGB mean absolute error svih piksela u fiksnom području, ljestvica 0–255. Nula je potpuna RGB jednakost.

| Područje (koordinate originala) | Prije (RGB MAE) | Nakon (RGB MAE) | Promjena |
|---|---:|---:|---:|
| Cijeli Home (0,0,941,1672) | 24.0846 | 23.9902 | −0.0944 |
| Zaglavlje (0,0,941,360) | 19.4292 | 19.4292 | 0 |
| Nastavak (35,363,896,751) | 25.3394 | 25.3227 | −0.0167 |
| Glavne kartice (37,766,900,1063) | 28.5560 | 27.9973 | −0.5587 |
| Tri kartice (38,1073,898,1265) | 29.3040 | 29.3040 | 0 |
| Tjedni niz (38,1275,897,1437) | 30.8460 | 30.8460 | 0 |
| Krajolik (0,1450,941,1672) | 21.2407 | 21.2407 | 0 |

### Pokušaj s regresijom i korekcija

Prvi kodni commit [a044b4d](https://github.com/bren-wp/Sudoku/commit/a044b4dd3307f283bef05f60b9df30f9ae6b9dfb) imao je globalni RGB MAE **24.4309**, odnosno nepovoljnu promjenu **+0.3463**. Dodatno osvjetljenje triju manjih kartica povisilo je njihov regionalni MAE za **+4.1588**. To je vraćeno u commitu [b1c8bb0](https://github.com/bren-wp/Sudoku/commit/b1c8bb035022649250acaea9c029ce2a8f80128a), s novim **24.0846 → 23.9902** rezultatom. Nije upotrijebljen prošli screenshot kao lažno novi niti je tvrdnja o 1:1 izvedena iz samog zelenog CI-ja.

## Provjere

- [Android CI 41355612707](https://github.com/bren-wp/Sudoku/actions/runs/41355612707): **SUCCESS** — unit testovi, QA regresije, lint i APK/AAB build na `b1c8bb0`.
- [Android visual smoke 41355612738](https://github.com/bren-wp/Sudoku/actions/runs/41355612738): **SUCCESS**, stvarni emulatori **941×1672** i **720×1280**, sa stvarnim PNG izvozom.
- UI/UX: postojeće stvarne radnje Nastavi, Nova igra, Dnevni izazov, Statistika, Postignuća, Postavke; bez dodavanja pristupa internetu, bez lažiranja niza i bez zamjene kompletnih zaslona slikama.
- Izdanje `v1.0.50` mora biti zasebno potvrđeno nakon bumpanja verzije. Ako izdavački ključ nije konfiguriran, objaviti samo testni APK i nepotpisani AAB, uz oznaku **prerelease**.

## Preostala odstupanja

Nisu potvrđeni **pixel-perfect 1:1** ni identični dinamički podaci. Posebno treba dodatno uskladiti stil ilustracija, svjetlo na kartici nastavka i donji krajolik, a ilustrativne podatke igrača odvojiti od stalnih vizualnih elemenata. Sljedeća faza: **Nova igra**.
