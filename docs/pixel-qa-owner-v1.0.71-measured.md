# Sudoku v1.0.72 — mjerenje svih 10 originalnih referenci

**Izvorne reference:** vlasnikovi PNG-ovi datirani 24. listopada 2026., pronađeni u privatnoj biblioteci i provjereni SHA-256 otiscima. Oni se **ne objavljuju u GitHubu**.  
**Stvarni Android izlaz:** GitHub Actions [Refresh Sudoku marketing screenshots #43338358264](https://github.com/bren-wp/Sudoku/actions/runs/43338358264), objavljena aplikacija v1.0.71, snimke 13. studenoga 2026.  
**Referentni profil:** 941×1672 px za osam zaslona; 1254×1254 px za wordmark i launcher. Nije bilo interpolacije, skaliranja ni prikazivanja reference umjesto Compose UI-ja. Početna stranica mjerena je prema **stvarnoj 01-home-resume.png** snimci, ne praznom početnom ekranu.

## Rezultati mjerenja

RGB MAE (mean absolute error) mjeri razliku intenziteta **na skali 0–255**, ne postotak vizualnog odstupanja. 'Točno jednaki RGB pikseli' zahtijevaju jednakost R, G i B kanala, bez tolerancije. Za logotip i ikonu dodatno se provjerava alfa (RGBA); nijedna zaokružena MAE vrijednost ne može zamijeniti strogu usporedbu.

| Referenca | RGB MAE 0–255 | Jednaki RGB pikseli | Jednaki RGBA pikseli | 1:1 |
|---|---:|---:|---:|---|
| 01 Naslovnica, aktivni Resume | 24,115176 | 0,016462 % | 0,016462 % | NE |
| 02 Nova igra | 22,622370 | 0,055487 % | 0,055487 % | NE |
| 03 Gameplay | 20,133552 | 0,061080 % | 0,061080 % | NE |
| 04 Postavke | 19,419634 | 0,036737 % | 0,036737 % | NE |
| 05 Dnevni izazov | 24,779138 | 0,086376 % | 0,086376 % | NE |
| 06 Statistika | 26,583405 | 0,035275 % | 0,035275 % | NE |
| 07 Postignuća | 29,862714 | 0,026059 % | 0,026059 % | NE |
| 08 Splash | 10,384231 | 0,710712 % | 0,710712 % | NE |
| 09 Wordmark | 18,833920 | 77,048755 % | 75,808386 % | NE |
| 10 Launcher ikona | 16,174683 | 30,429770 % | 29,392706 % | NE |

**Niti jedna od 10 referenci nije potpuno identična.** Postotak točno jednakih piksela izuzetno je strog i sam po sebi **nije** percepcijska podudarnost, osobito zbog tekstura i anti-aliasinga. Pogrešan je zaključak da je npr. MAE 29,86 = 29,86 % odstupanja. Najveće RGB razlike među ekranima su Postignuća, Statistika i Dnevni izazov.

### Odvojiti stvarne UI razlike od promjenjivog sadržaja

- Gameplay: različite zagonetke, odabrana ćelija, sekunde, pogreške, bilješke i preostali savjeti mogu podići cijelokupni MAE bez promjene geometrije.
- Dnevni izazov: današnji datum i zadaci nisu jednaki vlasnikovoj ilustrativnoj referenci.
- Statistika/Postignuća: korisnički rezultati i status otključavanja nisu nužno jednaki.
- Font rendering, gustoća zaslona, rasterizacija glifa i animacijska faza mogu rezultirati različitim pikselima čak i ako su komponente na istim mjestima.
- Wordmark i launcher: važna je i alfa; naknadno kompozitiranje na tamnu pozadinu može imati drukčiji vizualni rezultat od usporedbe sirovih, potpuno transparentnih RGB kanala.

Iz tog razloga **1:1 kriterij je stroga jednakost ulaznih piksela, ali ne tvrdi se da je cjelokupna vizualna nepodudarnost posljedica greške Compose geometrije**. Za eliminiranje svih odstupanja treba izjednačiti stanje sadržaja i mjeriti/korigirati pojedinačne regije.

## Autentičnost izvornika i mjerljiva ponovljivost

Alat `scripts/final_ten_reference_qa.py` sada zna **SHA-256 svih deset izvornika** (ne samo dvije branding slike). Ako se bilo koji PNG zamijeni drugim ili nenamjerno promijeni, usporedba pada. PNG datoteke ostaju privatne. Kod mjerenja i testovi su javni.

Iz privatnih izvornika i stvarnog emulatorskog zipa:

```bash
python scripts/final_ten_reference_qa.py \
  --reference-dir /putanja/originalni-PNG \
  --actual-dir /putanja/emulatorski-PNG \
  --output-dir qa-pixel-diff/v1.0.71 \
  --allow-imperfect
```

`--allow-imperfect` omogućuje izradu numeričkog izvještaja s odstupanjima i **nikada nije 1:1 certifikat**. Bez tog parametra, usporedba nepodudarnih PNG-ova vraća status 2.

Vlasnik je dobio i zasebne CSV/JSON datoteke s izvornim SHA-256 i snimkama; one su priložene razgovoru, **nisu dodane u GitHub radi privatnosti**.

## Granice ovoga ciklusa

- Sada je dovršeno **kvantitativno mjerenje svih 10 originalnih PNG-ova prema jednom poznatom buildu**, ne vizualno dovođenje svih ekrana na 0 odstupanja.
- Brojevi u gornjoj tablici pripadaju točno **v1.0.71** snimkama, ne smiju se predstavljati kao rezultati v1.0.72 bez nove snimke.
- Svako buduće izdanje zahtijeva novu stvarnu emulatorsku snimku i ponovljenu usporedbu s istim izvorom.
- Ostali sigurnosni i R8 rezultati nastaju iz stvarnih GitHub Actions release artefakata; pogledati `docs/r8-osv-audit-v1.0.72.md`.
