# Sudoku v1.0.48 — 10/10: adaptivna launcher ikona 7–3–9

## Izvorni PNG i stvarna povijesna emulatorska snimka

- **Original korisnika:** `Slika ChatGPT 24. lis 2026. 20_47_14-2.png`, **1254×1254 RGBA**, izvorni SHA-256 `0b7a48a510a6e86cfb2121b24b758d7de7cd4541fd50ac3f1f555801f7763ae9`.
- **Originalna alfa-bounding box (prag ≥128):** `(123, 100, 1131, 1119)`. Slabi vanjski glow postoji i pri alfa pragu 12: `(122, 99, 1132, 1120)`.
- **Stvarna prethodna emulatorska snimka:** `10-launcher-icon.png` iz uspješnog [Android visual smoke #41132281271](https://github.com/bren-wp/Sudoku/actions/runs/41132281271), commit `91db54bba5e156df3a2db5e81731c010eb653b6f` (ranija v1.0.47 grana), SHA-256 `3eb01db5297199c4bac21dfb14cd4eac84d5883761057e27bd09eae8b0976131`.
- U prethodnoj emulatorskoj PNG slici **sva četiri ugla imaju alpha=255**, a alfa-bounding box obuhvaća cijeli canvas `(0,0,1254,1254)`. Izvoz je prikazivao cijeli Androidov platformni adaptivni sloj s neprozirnom pozadinom, dok original sadrži prozirne rubove. Takav izvoz nije valjana izravna alfa-usporedba prozirnog izvornog brandinga.

| Povijesni rezultat (stvarni PNG, bez preskaliranja) | Vrijednost |
|---|---:|
| Cijeli alfa MAE, 0–255 | **97.079** |
| RGB MAE nakon komponiranja oba PNG-a preko `#021538` | **38.967** |
| Izvorna transparentna bbox pri alfa≥128 | (123,100,1131,1119) |
| Stara emulatorska bbox pri alfa≥128 | (0,0,1254,1254) |

**Ove brojke nisu rezultati ovog PR-a**. Stvarni novi 1:1 i poboljšanje tek treba potvrditi nakon uspješnog emulatorskog izvoza aktualnog HEAD-a.

## Izmjene u istom otvorenom PR-u #55

1. `app/src/main/res/drawable/ic_sudoku.xml`: stvarni 108×108 Android VectorDrawable poboljšan je diskretnim dodatnim cijan rubom, nijansama i zaobljenim vektorskim obrubom devet pojedinačnih polja, intenzivnijim cijan-tirkiznim središnjim poljem i čišćim glint detaljima. Broj **9** više ne ide daleko izvan donjeg desnog polja; linija je unutar same ćelije.
2. `app/src/main/res/drawable/ic_sudoku_monochrome.xml`: istovjetan pomak broja 9 i zaobljeno središnje polje za Android 13+ themed masku. Ikona i dalje sadrži samo bijelu alfa-grafiku, nikada neprozirnu pozadinu koja bi kod themed ikona postala puni blok.
3. `app/src/androidTest/java/com/brendigo/sudoku/SudokuVisualSmokeTest.kt`: ispravljen izvoz `10-launcher-icon.png`. Umjesto `AdaptiveIconDrawable.draw()` zajedno s platformskom neprozirnom pozadinom, izvorni brand-ready PNG nastaje crtanjem **stvarnog foreground VectorDrawable resursa iz instaliranog APK-a** na transparentnom 1254² platnu, u deklariranom referentnom okviru (`[-230,-230,1484,1484]` u QA canvas koordinatama). Okvir nije zamjena za provjeru stvarne adaptivne maske, koja i dalje zasebno renderira pravi instalirani `AdaptiveIconDrawable` u 108dp.
4. `11-launcher-monochrome.png` sada također prikazuje stvarni lokalni monokromatski VectorDrawable preko prozirnog platna, u istom referentnom okviru. Eksplicitno se provjerava prozirnost uglova i prisutnost središnjeg broja, bez kopiranja RGB reference.
5. `scripts/launcher_icon_pixel_qa.py`: zaseban 1254² QA alat uspoređuje **stvarni** screenshot i vlasnikov original po RGBA/alpha MAE, navy-composited RGB MAE, alfa-bounding box granicama, i sedam fiksnih područja (`whole`, `outer_bevel`, `nine_cells`, `digit_7`, `digit_3`, `digit_9`, `cyan_center_tile`). Izlazi `launcher-regions.csv`, `alpha-difference.png`, `overlay.png`, `difference.png`. Original je izvan repozitorija.
6. Monokromatski `11` nema originalnu zasebnu referencu; testira se kao **stvarna Android alfa maska** (prozirni rubovi, bijele vidljive RGB vrijednosti, razumna pokrivenost, središnji broj). Nikada se ne uspoređuje sa šarenim `10` kao da ima istu semantiku.
7. `scripts/test_pixel_compare.py`: nove regresije otkrivaju netočnu neprozirnu marginu, odvojene pikselne regije i provjeravaju monokromatsku alpha masku te rezoluciju 1254×1254.

## Granice platformskog prikaza

Android launcher sam oblikuje ikonu preko različitih maski (krug, squircle, OEM oblici), čita `@color/launcher_background`, `foreground` i `monochrome`. Kompozit cijele platformske ikone **ne mora** imati iste transparentne rubove kao originalno promotivno umjetničko djelo. Zato QA jasno razlikuje (a) usporedivi **transparentni foreground branding** od (b) instaliranog **platformnog adaptivnog rendera**. Export vektorskog foregrounda također ne dokazuje da su gradijenti i tekstura originalne ilustracije identični; stvarni vektorski resurs je namjerno skalabilan i samostalan.

## Koraci za stvarnu završnu provjeru

1. Provesti **Android CI** i oba Android emulatorska workflowa za **isti aktualni HEAD** (941×1672 i 720×1280). Ne koristiti otkazane runove sa starijih commitova kao dokaz.
2. Preuzeti novi **`sudoku-emulator-screen-references`** i osigurati `10-launcher-icon.png`, `11-launcher-monochrome.png`, oba dimenzija 1254×1254, iz stvarnog instaliranog APK-a.
3. Original korisnika bez izmjene staviti lokalno kao `references/10-launcher-icon.png`. Izvoz mora biti `screenshots/10-launcher-icon.png`, a themed kao `screenshots/11-launcher-monochrome.png`.
4. Pokrenuti:

```bash
python scripts/launcher_icon_pixel_qa.py \
  --reference references/10-launcher-icon.png \
  --actual screenshots/10-launcher-icon.png \
  --monochrome screenshots/11-launcher-monochrome.png \
  --output-dir qa-pixel-diff/launcher --allow-imperfect
```

5. Pročitati `launcher-regions.csv`, alfa bounding boxes i `alpha-difference.png`, te zasebno vizualno potvrditi platformsku adaptivnu i themed ikonu na launcheru. Za **svih 10** referenci i 11. masku ostaju obvezni stvarni Android testovi, regresijski Python testovi, lint, APK/AAB build i analiza neslaganja, bez tvrdnje o potpunom 1:1 prije te validacije.

**Status:** kod i testovi su commitani u isti otvoreni PR #55; nema dokaza o završnoj 1:1 jednakosti ili spremnosti za objavu dok novi CI / stvarni PNG nije potvrđen. Ne mijenjati `applicationId`, korisničku pohranu, verziju, signing ključeve niti dozvole. Release nije objavljen.

**Sljedeći korak nakon 10/10:** objedinjeni CI, sigurnosni/lint/test audit, pregled diffa svih 10 referenci i izdavanje **tek nakon** potpunog zelenog rezultata te raspoloživog izdavačkog potpisa.
