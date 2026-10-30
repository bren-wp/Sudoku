# Sudoku v1.0.48 — Nova igra, završni 2/10 QA prolaz (isti PR #55)

## Referenca i stvarni prethodni Android rezultat

- Original vlasnika: `Slika ChatGPT 24. lis 2026. 20_47_17-5.png` — **941 × 1672 px**, bez izmjena.
- Stvarna snimka `02-new-game.png`: uspješan `Android visual smoke` run [41132281271](https://github.com/bren-wp/Sudoku/actions/runs/41132281271), commit `91db54bba5e156df3a2db5e81731c010eb653b6f`, prethodna v1.0.47 razvojna grana. Nije render ili lažna snimka.
- Pixel diff napravljen na RGB slikama iste razlučivosti, bez rastezanja. **Ovo je povijesna referenca prije izmjena u ovom PR-u; ne predstavlja rezultat aktualnog HEAD-a.**

| Fiksna regija (941 × 1672) | Povijesni RGB MAE 0–255 |
|---|---:|
| `whole` — cijeli zaslon | **25.017** |
| `size_selector` — 465,28,900,150 | 20.569 |
| `title_and_subtitle` — 35,162,630,360 | 31.347 |
| `header` — 0,0,941,360 | 18.848 |
| `six_cards` — 38,366,902,1332 | **30.178** |
| `upper_three_cards` — 38,366,902,855 | 27.280 |
| `lower_three_cards` — 38,855,902,1332 | **33.149** |
| `cta` — 100,1330,845,1505 | 21.594 |
| `bottom_scenery` — 0,1490,941,1672 | 23.588 |

Najizraženije su vizualne razlike šest kartica: postojeći gradijenti na emulatorskoj snimci bili su previše jednolično svijetli u odnosu na izvorne tamne plohe i neon detalje, osobito u žutoj i crvenoj kartici. Područja su unaprijed zadana i ne smiju se pomicati radi ljepše metrike.

## Promjene u postojećem PR #55

1. `NewGameArt.kt`: tamnije, zasebno kalibrirane palete za **Vrlo lako, Lako, Srednje, Teško, Ekspert, Majstor**; smanjen višak radijalnog sjaja. Ostaju stvarni Compose gumbi/ikone i isti dinamički podaci.
2. `Pages.kt`: dodani stabilni `new-game-title`, `new-game-subtitle` i `new-game-difficulty-list` testTagovi; uklonjena neiskorištena varijabla `compactSelector`. Zadržani aktivni odabiri veličine 9×9, 6×6 i 4×4, `rememberSaveable` i kontroler.
3. `SudokuVisualSmokeTest.kt`: novi UI test provjerava svih šest kartica na stvarnom emulatorskom zaslonu, širine, granice, minimalnu visinu, aktivne odabire te funkcionalni CTA od najmanje 48dp.
4. `home_pixel_qa.py`: zajednički `measure_regions` za mjerenje fiksnih regija; postojeći `measure_home` ostaje kompatibilan.
5. `new_game_pixel_qa.py`: deterministički QA originala i stvarnog screenshot-a uz `regions.csv`, `overlay.png` i `difference.png`, bez pohrane originalnog korisničkog PNG-a u repozitorij.
6. `test_pixel_compare.py`: novi regresijski testovi detektiraju razlike u području kartica i odbijaju lažnu usporedbu 720×1280 sa 941×1672.

## Ponovljiva provjera aktualnog HEAD-a

Nakon završetka oba Android emulator workflowa preuzeti novi `sudoku-emulator-screen-references` artefakt i original na lokalno računalo:

```bash
python scripts/new_game_pixel_qa.py \
  --reference references/02-new-game.png \
  --actual screenshots/02-new-game.png \
  --output-dir qa-pixel-diff/new-game \
  --allow-imperfect
```

Pregledati geometriju 9×9/6×6/4×4 selektora, svih šest kartica i prohodnost CTA-a, početak igre i kasniji povratak, te uz regresijske testove provjeriti i 720×1280 zaslon (bez pokušaja da se mala slika uspoređuje s velikom preko reskaliranja).

**Status:** promjene su commitane u PR #55, ali 1:1 nije dokazano dok aktualni CI ne prođe i ne postoji nova usporedba stvarne snimke. Originalni screenshot i njegovi lažni ilustrativni brojevi ne smiju se prikazivati kao funkcionirajući UI. Sljedeći zaslon za pojedinačni prolaz je **3/10 — aktivna igra**.
