# Sudoku v1.0.77 — provjerljiva preciznost svih deset referenci

**Status: razvojni PR, nije objavljeno izdanje; 1:1 nije potvrđen.**

## Potvrđeno početno stanje (18. 11. 2026.)
- Polazni `main` SHA: `3e7be1dc1eaf121a91142dc9228c07b3cd6e25f4`.
- Posljednje objavljeno izdanje: **v1.0.76 test prerelease**, APK testno potpisan, AAB nepotpisan. [Izdanje](https://github.com/bren-wp/Sudoku/releases/tag/v1.0.76).
- `main` Android CI [#43771211981](https://github.com/bren-wp/Sudoku/actions/runs/43771211981): uspješan.
- Android visual smoke [#43771211987](https://github.com/bren-wp/Sudoku/actions/runs/43771211987): svih **11 profila uspješno**. Referentni artefakt `sudoku-emulator-screen-references` (#14184363950) ima 15 testnih i referentnih PNG-ova u odgovarajućim mapama.
- Deset privatnih vlasnikovih originala (8×941×1672, 2×1254×1254) **nije dostupno za novu usporedbu**. Povijesno posljednje potvrđeno stanje stroge jednakosti jest **0/10**; to nije novo mjerenje v1.0.77.

## Inventar zahvata

| Prioritet | Potvrđeni nedostatak | Datoteka | Zahvat / regresijski test | Rizik |
| --- | --- | --- | --- | --- |
| P0 QA | Three-way prikaz prije/poslije pretvarao PNG u RGB prije mjerenja, pa je gubio promjene alfa kanala | `scripts/compare_visual_iteration.py` | Puni RGB i RGBA mjere se iz izvornih slika; novi test alfa-only promjena | Nizak, samo QA |
| P1 QA | Prije/poslije zapisivao je samo cijeli zaslon i jedan donji ROI | `scripts/compare_visual_iteration.py` | Svi fiksni ROI-jevi **svih osam zaslona** u CSV-u i JSON-u; registrira i pogoršanje pojedinog panela | Nizak |
| P1 QA | Veličine glavnih screenshotova, integritet PNG-a i alfa branding nisu bili zajednički potvrđeni u CI snimkama | `scripts/validate_android_capture.py`, `.github/scripts/export-emulator-screenshots.sh` | PNG CRC/struktura, 941×1672 referentni viewport, 1254×1254 RGBA branding i SHA-256 manifest | Srednji: otkrit će loš capture |
| P1 QA | Bez mehanizma za odbijanje determinističke regionalne regresije | `scripts/compare_visual_iteration.py` | Opcija `--fail-on-regression` za iste fiksne testne podatke, s podesivom tolerancijom | Nizak, opcionalno |

## Primjena i evidencija

1. Provjeriti originalne vlasničke PNG-ove preko hardkodiranih SHA-256 otisaka. Ne prenositi originale u repozitorij.
2. Iz Android reference workflowa preuzeti stvarne snimke `01-home-resume.png` i svih deset PNG-ova (uključujući branding). `capture-evidence.json` bilježi geometriju, tip PNG-a i SHA-256 svakog exporta.
3. Pokrenuti (u lokalnom, privatnom QA okruženju):

```bash
python scripts/compare_visual_iteration.py \
  --reference-dir /private/owner-originals \
  --before-dir /private/baseline-screens \
  --after-dir /private/candidate-screens \
  --output-dir qa-output \
  --home-resume \
  --fail-on-regression
```

4. Analizirati `visual-iteration.csv`, `visual-iteration-regions.csv` i `visual-iteration.json`. Skripta ne interpolira original i uvijek prikazuje nemaskirani cijeli zaslon; ROI nije zamjena za cijeli prikaz.
5. Ako je izvorno stanje drugačije (datum, partija, timer, izbor polja, statistika), usporediti jednake determinističke testne fixture snimke prije uključivanja `--fail-on-regression`. U suprotnom bi provjera mogla pogrešno označiti dinamičke promjene kao vizualne regresije.
6. Ne proglašavati 1:1 samo na temelju nula pogrešaka emulatora ili poboljšanog MAE; potrebna je **stroga jednakost svih relevantnih piksela** na odgovarajućim izvorima.

## Preostali release blokatori

- Novi Android CI i svih 11 emulator profila za konačni PR SHA.
- Vlasnikovih deset autentičnih PNG referenci i usporedba istog stanja zaslona, uključujući boje, fontove, 3D obrube, sjene i sve interaktivne prikaze.
- Pregled razlika po ROI-u i dokumentiranje svake regresije.
- Produkcijsko Google Play potpisivanje / AAB samo s izdavačevim ključem. Bez ključa samo jasno označeni testni artefakti.

Nema izmjena korisničkih podataka, spremljenih igara, enkripcije, lokalizacije ili gameplay logike.
