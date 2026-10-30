# Sudoku v1.0.48 — Dnevni izazov, završni zaslon 5/10 (PR #55)

## Izvorna referenca i stvarni prethodni rezultat

- Original korisnika: `Slika ChatGPT 24. lis 2026. 20_47_18-7.png`, **941 × 1672 px**, SHA-256 `b558b297b49be88e858b0cab6f9978263a48c8e4acb0a8aab6e24a4e8365e01d`.
- Stvarna ranija emulatorska snimka `05-daily.png`, iz uspješnog [Android visual smoke #41132281271](https://github.com/bren-wp/Sudoku/actions/runs/41132281271) za commit `91db54bba5e156df3a2db5e81731c010eb653b6f` na prethodnoj v1.0.47 grani. SHA-256 `068998a074ba6379b2bbe2b08178a52666ae843a407ac406201eec4487c0116b`.
- Uspoređeni su originalni **RGB pikseli** na jednakoj rezoluciji, bez preskaliranja ili generiranja novog screenshot-a. Ovo je **povijesni baseline**, ne rezultat aktualnog commita.

| Regija, fiksne granice (x0,y0,x1,y1) | Povijesni RGB MAE 0–255 |
|---|---:|
| Cijeli ekran `0,0,941,1672` | **23.594** |
| Zaglavlje `0,0,941,273` | 25.554 |
| Datum i nagrada `30,270,900,475` | 23.122 |
| Hero izazova `30,480,900,900` | 26.602 |
| Desni krajolik unutar hero kartice `480,480,890,747` | 23.287 |
| Tri metričke kartice `57,745,891,872` | 26.911 |
| Tjedni niz `30,900,895,1185` | 17.211 |
| Gumb izazova `110,1185,820,1315` | 29.888 |
| **Lokalna rang-lista** `180,1310,755,1425` | **40.695** |
| Donji krajolik `0,1440,941,1672` | 22.057 |

Iznosi su izmjereni iz stvarnih PNG-ova prethodnog Android artefakta, a ne prepisani iz ranijih različitih v1.0.44 baselinea.

## Ispravci unutar istog PR-a

1. `Pages.kt`: akcija `daily-leaderboard` ima vanjski ljubičasti obrub (umjesto generičkog plavog), **radijus 25dp** i najmanje **48dp** stvarnog dodirnog područja. Trofej je dodatno osvijetljen prigušenim ljubičastim gradijentom. Klik i dalje otvara **lokalnu osobnu rang-listu**, bez tvrdnje da postoji globalna online tablica.
2. `DailyVisuals.kt`: `DailyRewardEmblem` sada crta originalu bliži zlatni medaljon s obrubljenom posudicom i zvijezdom isključivo nativnim Compose Canvas elementima, umjesto generičkog poklon-luka. Nema slike UI-a, vanjskih resursa ni mijenjanja stanja nagrade.
3. `Pages.kt`: dodani `daily-weekday-0` do `daily-weekday-6` te `daily-reward-emblem` testTagovi za svih sedam pravih lokalnih datuma i nativnu značku. Funkcija za čitanje korisnikova napretka i ispravni prekidi u nizu nisu mijenjani.
4. `SudokuVisualSmokeTest.kt`: novi instrumentacijski test provjerava sedam dana bez horizontalnog preklapanja, minimalnu 48dp površinu lokalne rang-liste, prikaz lokalnih podataka i stvarni dijalog bez globalne rang-liste.
5. `scripts/daily_pixel_qa.py`: mjerenje punog kadra i devet unaprijed zadanih područja; izlazi `regions.csv`, `overlay.png`, `difference.png`. Granice se ne mijenjaju radi bolje numeričke ocjene.
6. `scripts/test_pixel_compare.py`: regresijski test koji otkriva razliku samo u području rang-liste, čuva punokadarski MAE i odbija pokušaj usporedbe manjeg emulatora 720×1280 s originalom 941×1672.

## Zašto se ne kopiraju brojevi s referentnog screenshota

Ilustracija prikazuje **28. travnja 2025.**, niz od **7 dana** i akciju **Pokreni izazov**. Android screenshot prethodne sesije prikazuje **30. listopada 2026.**, niz **0** i opciju **Nastavi današnji izazov**, jer je to stvarno spremljeno stanje. To nisu zamjenjive vrijednosti. Forsiranje izvornih numeričkih podataka u aplikaciju samo radi 1:1 slike poništilo bi ispravnu igru. Provjeravati geometriju i ikonografiju uz determinističko testno stanje gdje je opravdano; ne mijenjati korisnikove stvarne podatke.

## Reprodukcija završne provjere aktualnog HEAD-a

1. Za **isti aktualni commit** provjeriti `Android CI` (Kotlin, Python Pixel QA, Android Lint, APK/AAB) i `Android visual smoke` na **941×1672** i **720×1280**.
2. Iz aktualnog uspješnog `sudoku-emulator-screen-references` artefakta preuzeti stvarni `05-daily.png` u `screenshots/`; izvornu korisnikovu sliku bez promjene spremiti pod `references/05-daily.png`.
3. Pokrenuti:

```bash
python scripts/daily_pixel_qa.py \
  --reference references/05-daily.png \
  --actual screenshots/05-daily.png \
  --output-dir qa-pixel-diff/daily \
  --allow-imperfect
```

4. Pregledati `regions.csv`, overlay i diff, prave tjedne markere, preostale datume, prilagodbu na uskoj rezoluciji, boje i radijus rang-liste. Provjeriti pokretanje/nastavak dnevne igre, trajnost zapisa, onemogućavanje već riješenog izazova, osobnu rang-listu i sve tokove bez interneta.

**Status:** izmjene su commitane u PR #55. Pixel 1:1 trenutačnog HEAD-a **još nije dokazan** novom stvarnom emulatorskom snimkom i prolazom svih testova. Nije izdan novi Play release.

**Sljedeći pojedinačni zaslon:** 6/10 — Statistika.
