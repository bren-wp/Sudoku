# Sudoku 1.0.51 — Nova igra, zaslon 3/8

## Cilj i izvori

Izvorna referenca vlasnika: `Slika ChatGPT 24. lis 2026. 20_47_17-5.png` odnosno `02-new-game.png`, 941×1672 PNG. Interaktivni UI je stvarni Compose; original nije zalijepljen preko gumba i kartica.

- Prije: stvarni Android emulator `02-new-game.png` iz izdanja 1.0.50, [Android visual smoke 41356218209](https://github.com/bren-wp/Sudoku/actions/runs/41356218209), kodni commit `26db0396bcf7f46139ab8776e1aed2bd9e196f85`.
- Nakon: stvarni Android emulator `02-new-game.png`, [Android visual smoke 41358012930](https://github.com/bren-wp/Sudoku/actions/runs/41358012930), commit `d446870140c04fab9ba065d366cdb683ce8be87c`.
- Metrika: RGB mean absolute error po kanalu na skali 0–255, **bez reskaliranja, retuširanja, pomicanja ili izbacivanja neprikladnih područja**. Niže vrijednosti znače manje odstupanje.

| Regija (fiksne koordinate na 941×1672) | Prije | Nakon | Delta |
|---|---:|---:|---:|
| Cijeli ekran (0,0,941,1672) | 22.3559 | **22.3279** | −0.0280 |
| Zaglavlje (0,0,941,360) | 18.8481 | 18.8481 | 0 |
| Selektor (465,28,900,150) | 20.5693 | 20.5693 | 0 |
| Naslov/podnaslov (35,162,630,360) | 31.3469 | 31.3469 | 0 |
| Šest kartica (38,366,902,1332) | 25.2476 | **25.1949** | −0.0527 |
| Prve tri (38,366,902,855) | 23.2532 | **23.1490** | −0.1041 |
| Posljednje tri (38,855,902,1332) | 27.2923 | 27.2923 | 0 |
| CTA (100,1330,845,1505) | 21.0148 | 21.0148 | 0 |
| Donji pejzaž (0,1490,941,1672) | 23.5878 | 23.5878 | 0 |

## Izmjene i kontrolirani povrat neuspjele dorade

- `NewGameArt.kt`: lokalizirane osvijetljene vektorske ćelije na gornje tri kartice, uz postojeće stvarne oznake, ikone i semantiku. Bez novih PNG-ova, eksternih resursa i ovisnosti.
- Prvi kandidat [5f0151c](https://github.com/bren-wp/Sudoku/commit/5f0151cadbc341e9ba5a063e3b35cb7f13f5c888) poboljšao je prve tri kartice, ali pogoršao posljednje tri: kompletan kadar 22.3559→22.3788. Ova regresija **nije prihvaćena**.
- Konačni kod [d446870](https://github.com/bren-wp/Sudoku/commit/d446870140c04fab9ba065d366cdb683ce8be87c) ograničava osvjetljenje na gornje kartice. Popravak je potvrđen novim snimkama obje emulator konfiguracije.

## Verifikacija i ograničenja

- [Android CI 41358012995](https://github.com/bren-wp/Sudoku/actions/runs/41358012995): **SUCCESS**, unit, pixel metrike, lint, APK/AAB.
- [Android visual smoke 41358012930](https://github.com/bren-wp/Sudoku/actions/runs/41358012930): **SUCCESS** na 941×1672 i 720×1280.
- Kodni commit `d446870` je potvrđen; izdavački commit `1.0.51` traži zasebni CI.
- Funkcionalnost: veličine 4×4/6×6/9×9, šest razina, odabir, pokretanje igre, `rememberSaveable`, postojeće sigurnosne i privatnosne funkcije nisu mijenjane.
- **Doslovni pixel-perfect 1:1 nije ostvaren.** Najviše preostalih odstupanja imaju ilustracija neonske mreže, font i donji krajolik. Regresiju ne prikrivati prezentacijskom obradom.

## Nastavak

Sljedeća zasebna iteracija: **Aktivna igra (Gameplay)**.
