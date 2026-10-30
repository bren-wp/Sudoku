# Sudoku v1.0.48 — wordmark i adaptivna/monokromatska ikona

## Izvorne reference (stvarne datoteke vlasnika)

Dvije **izvorne** transparentne PNG datoteke, 1254 × 1254 px, identificirane su prema stvarnom sadržaju, a ne prema redoslijedu Android ekrana:

| Referenca | Original | SHA-256 izvornog PNG-a | Alpha >= 128 — okvir (x0,y0,x1,y1) |
|---|---|---|---|
| `09-wordmark.png` | `Slika ChatGPT 24. lis 2026. 20_47_13-1.png` | `7534ad4d8bbfef116ad7d092cb91027f5d393a6008b7ecb37a7a33aed7047fd1` | `63,443,1204,779` |
| `10-launcher-icon.png` | `Slika ChatGPT 24. lis 2026. 20_47_14-2.png` | `0b7a48a510a6e86cfb2121b24b758d7de7cd4541fd50ac3f1f555801f7763ae9` | `123,100,1131,1119` |

Usporedba se mora provoditi s **originalnim PNG-ovima, bez povećavanja, kompresije ili zamjene referentnim renderom**. Zbog privatnosti i veličine nisu commitani u repozitorij. SVG je implementacija brendinga, a ne zamjena za izvorni PNG u testu.

## Što je promijenjeno

1. `app/src/main/res/drawable/ic_sudoku.xml`: dodatna 3×3 struktura devet pojedinačnih tamnoplavih, zaobljenih polja sa svijetlim središtem, dok lokalni vektorski znakovi 7–3–9 ostaju u resursu.
2. `branding/sudoku-logo.svg`: dodani slojevi polja i osvjetljenja; ostaje samostalan editabilan SVG i prozirni brand znak.
3. `branding/sudoku-icon.svg`: uklonjena puna neprozirna pozadina cijelog platna, kako bi prostor oko zaobljenog znaka ostao proziran kao u originalnoj referenci.
4. `SudokuVisualSmokeTest.kt`: `09-wordmark.png` nastaje iz stvarne Compose komponente, a crna ispitna pozadina se samo u QA izvozu pretvara u prozirnu; `10-launcher-icon.png` se crta preko stvarno prozirnog ARGB platna. Dodatno se izvodi `11-launcher-monochrome.png` iz stvarnog Android themed resursa (bez vlasnikove 11. reference).
5. `.github/scripts/export-emulator-screenshots.sh`: traži svih 11 nativnih PNG izlaza i provjerava 1254×1254 px za sve tri branding slike.
6. `scripts/pixel_compare.py`: za square reference odvojeno prijavljuje RGB MAE, alfa MAE, RGBA MAE, ±12 RGBA postotak i RGB MAE nakon sastavljanja preko tamnoplave `#021538` pozadine. Usporedba koja je prije ignorirala alpha kanal više ne može dokazivati lažnu jednakost prozirne i neprozirne crne podloge.
7. `scripts/test_pixel_compare.py`: regresijski testovi prozirne reference i namjerno neprozirne crne slike te identičnih transparentnih rendera.

## Reprodukcija

Pokrenuti emulatorsku provjeru na **941×1672** i **720×1280**. Stvarni QA artefakt mora sadržavati `09-wordmark.png`, `10-launcher-icon.png`, `11-launcher-monochrome.png`, sva tri **1254×1254 px**. Posebno vizualno pregledati oblik zaokruženja adaptivne ikone i monokromatsku alpha-masku.

Originale spremiti lokalno pod `references/09-wordmark.png` i `references/10-launcher-icon.png`; spremiti **stvarne** emulatorske PNG-ove pod `actual/`. Pokrenuti:

```bash
python scripts/pixel_compare.py --reference-dir references --actual-dir actual \
  --include-branding --allow-imperfect --output-dir qa-pixel-diff
```

Naredba također očekuje sedam ostalih referentnih snimki (`01`–`07`), zato ih treba osigurati u oba direktorija; `08` samo uz `--include-splash`. Monokromatski `11` prikaz treba zasebno testirati funkcionalno jer nema vlasnikove odgovarajuće originalne slike.

## Ograničenja i kriterij prihvaćanja

- Android maska adaptivne ikone ovisi o launcheru i ne mora izgledati jednako kao kvadratna originalna 1254px ilustracija. Mjeri originalnu transparentnu ilustraciju, stvarni Android adaptive render i monokromatski resurs **odvojeno**; ne mijenjaj original.
- Dokument navodi **izvorne** SHA-256 i njihove granice prozirnosti, ali **ne proglašava rezultat implementacije 1:1**. Prije takve tvrdnje mora se preuzeti aktualni CI artefakt, izmjeriti svih deset originalnih referenci, pregledati diff i potvrditi na uređaju.
- Bez promjene `com.brendigo.sudoku`, igre, lokalnih korisničkih podataka, verzije, signing postavki ili Play dozvola.
