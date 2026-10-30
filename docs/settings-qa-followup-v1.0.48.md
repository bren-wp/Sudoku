# Sudoku v1.0.48 — završni pixel QA zaslona 4/10: Postavke

## Izvorna slika i provjerljivi povijesni rezultat

- **Original korisnika:** `Slika ChatGPT 24. lis 2026. 20_47_20-10.png`, **941 × 1672 px**, izvorni SHA-256: `4a97ac27d3ecca33d7ef8068a30ada888c8a349fe9718d24a996f43239027a9c`.
- **Stvarna emulatorska snimka prethodne v1.0.47 grane:** `04-settings.png` iz uspješnog [Android visual smoke workflowa #41132281271](https://github.com/bren-wp/Sudoku/actions/runs/41132281271), commit `91db54bba5e156df3a2db5e81731c010eb653b6f`. SHA-256 PNG-a: `c06128e818206cb269ddb8580005c15034a451b5b8f63ab5e44028c7a897f7c1`.
- Obje slike uspoređene su u **izvornoj rezoluciji** RGB/PNG, bez rastezanja i bez zamjene stvarnog Android sadržaja izvornom ilustracijom.
- To su **povijesne početne vrijednosti**, ne rezultat aktualnog PR HEAD-a.

| Fiksno područje (x0,y0,x1,y1) | RGB MAE, 0–255 |
|---|---:|
| Cijeli zaslon (0,0,941,1672) | **24.519** |
| Zaglavlje (0,0,941,206) | 22.720 |
| Izgled (40,205,893,535) | 23.668 |
| Igra (40,545,893,988) | 25.361 |
| Zvuk i vibracija (40,995,893,1120) | **39.366** |
| Jezik (40,1130,893,1258) | 29.641 |
| O aplikaciji (40,1265,893,1520) | 23.336 |
| Donji krajolik (0,1518,941,1672) | 24.988 |

## Stvarni nalazi i izmjene u PR-u #55

1. Referenca za panel **Jezik** ima **plavi obrub** i posebno **tirkiznu ikonu globusa**. Aktualni `SettingsPage` prije ovog prolaza koristio je tirkiznu vanjsku granicu. Vanjski obrub usklađen je s plavom; realni odabir `Hrvatski` i dijalog nisu mijenjani.
2. Dodani stabilni Compose tagovi `settings-language-value` i `settings-version` radi provjere unutrašnjeg poravnanja te stvarnog broja verzije iz `BuildConfig.VERSION_NAME`, umjesto kopiranja verzije ilustracije.
3. Dodan `settingsReferenceRowsKeepTouchTargetsAndCorrectContent`: **pet stvarnih dostupnih prekidača** mora imati najmanje 48dp dodirne površine; hrvatski selektor mora ostati unutar odgovarajućeg panela; verzija i pravila privatnosti moraju ostati čitljivi; šifrirane offline sigurnosne kopije ostaju dostupne skrolanjem.
4. `scripts/settings_pixel_qa.py` uspoređuje točno osam unaprijed zadanih područja (ukupni ekran + naslov + pet panela + krajolik), čuva `regions.csv`, `overlay.png` i `difference.png`, bilježi RGB MAE i udio piksela unutar ±12 po kanalu.
5. `scripts/test_pixel_compare.py` provjerava da razlika ograničena na **Zvuk i vibracija** bude registrirana u tom panelu, ali ne i u `Izgled`/`Jezik`, te da kriva rezolucija 720×1280 ne bude automatski rastegnuta na referencu.

**Važno za 1:1:** referentna slika prikazuje određene položaje prekidača i `Verzija 1.0.0`. Stvarna aplikacija prikazuje spremljene korisničke opcije i istinitu trenutnu verziju. Vizualno se uspoređuje struktura i dizajn, a ne lažiraju stanje postavki i podaci samo radi identične slike. Različit sistemski font, API razina, statusna traka i anti-aliasing također mogu utjecati na sirovi MAE.

## Kako ponoviti stvarnu provjeru trenutačnog HEAD-a

1. Pričekati uspješne `Android CI` i `Android visual smoke` **za isti commit** (941×1672 i 720×1280).
2. Preuzeti **novi** `sudoku-emulator-screen-references` artefakt i iz njega uzeti stvarni `04-settings.png`; povijesni `04-settings.png` iz runa 41132281271 smije se koristiti samo kao baseline.
3. Neizmijenjeni original spremiti lokalno kao `references/04-settings.png` (ne commitati original u GitHub).
4. Izvršiti:

```bash
python scripts/settings_pixel_qa.py \
  --reference references/04-settings.png \
  --actual screenshots/04-settings.png \
  --output-dir qa-pixel-diff/settings \
  --allow-imperfect
```

5. Provjeriti `regions.csv`, sve pet obojene ikone, veličine, razmake, razdjelnike, toggle površine, zvuk/vibraciju, stvarnu jezičnu opciju, tekst privatnosti i pristup sigurnosnim kopijama. Na 720×1280 provjeriti skrolanje umjesto usporedbe bitmapa različitih dimenzija.

## Granica dovršenosti

U ovom prolazu **stvarne izmjene su commitane u postojeći PR #55**, bez mijenjanja `applicationId`, baze, lokalne pohrane, Android/Gradle ovisnosti, mrežnih dozvola ili ključeva. Stvarno poklapanje 1:1 nakon ovih izmjena **još nije potvrđeno** dok novi testovi i svježa emulatorska snimka nisu provjereni. Nije stvoreno novo Play izdanje.

**Sljedeći zaslon po kontrolnom popisu: 5/10 — Dnevni izazov.**
