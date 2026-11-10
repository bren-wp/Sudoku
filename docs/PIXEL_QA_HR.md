# Pixel QA — usporedba s vlasnikovim referencama

Izvor: deset izvornih PNG referenci (Android zasloni 941 × 1672 px; logotip i ikona 1254 × 1254 px) uspoređenih sa stvarnim emulatorom Sudoku **v1.0.59**. RGB MAE mjeri prosječnu apsolutnu razliku po kanalu u rasponu 0–255; manje je bolje.

| Prioritet | Zaslon | RGB MAE v1.0.59 | Najkritičnije područje (MAE) |
|---|---|---:|---|
| 1 | Launcher ikona | 36,20 | 3×3 mreža 51,96 |
| 2 | Postignuća | 29,44 | Drugi red kartica 39,85 |
| 3 | Statistika | 26,43 | Gornje KPI kartice 34,08 |
| 4 | Dnevni izazov | 24,92 | CTA i rang-lista 36,56 |
| 5 | Home s nastavkom | 23,95 | Niz i nagrada 30,41 |
| 6 | Nova igra | 22,61 | Ekspert / Majstor 27,18 |
| 7 | Gameplay | 20,23 | Numerička tipkovnica 25,46 |
| 8 | Postavke | 19,42 | Podnožje / pozadina 24,31 |
| 9 | Wordmark | 16,25 | Središnji pojas 37,74 |
| 10 | Splash | 10,38 | Sudoku naziv / autor 35,30 |

## Obvezna pravila rada

- Oznaka **1:1** zabranjena je dok se ne potvrdi podudarnost na istim stanjima, gustoći piksela, fontovima, dimenzijama, lokalizaciji, izgledu statusne trake i režimu sustava.
- Testne statistike (420 bodova, 8/24, 124 odigrane, 92 % i datum 28. 4. 2025.) nisu produkcijski podaci: za pošten MAE potrebno je postaviti deterministički **isključivo androidTest** fixture. Ne mijenjati stvarne rezultate igrača.
- Vanjski izgled: boje, tipografija, radijusi, 3D osvjetljenje, dimenzije ikonica, progress-trake, pojedinačne hit-zone i fokus. Obavezno sačuvati touch, accessibility, scrolling, sudokugenerator, navigaciju i offline pohranu.
- Nepodudarna stanja, datumi i brojke ne smiju se „ispravljati” lažnim hardkodiranim vrijednostima. QA mora dati zasebnu regiju za statički i dinamički sadržaj.
- Svaki PR: Android CI + vizualni emulator u 11 profila, stvarni PNG iz `Android UI (reference)`, regionalni MAE/SSIM prije i poslije. Revertirati pogoršanja po regijama.
- Izdanja: APK i AAB uz SHA256SUMS; nepotpisani AAB nije Play-ready i mora biti prerelease.

## Promjene u v1.0.60

- Dnevni izazov: metrike zbijenije, nepotrebna vertikalna praznina u hero-panelu uklonjena radi položaja iz referentne snimke; bez promjene izazova ili datuma.
- Statistika: gornji KPI vektori i brojke približeni referentnoj skali uz očuvanje širine teksta i klikabilnih regija; bez manipulacije statistikama.
- Postignuća: pet naprednih ilustriranih nagrada ima zadržanu prepoznatljivu vektorsku ikonu i vidljiv stvarni progress, a sivi lokot ostaje rezerviran za zaključane milestone-nagrade. Status otključavanja i screen-reader opis ostaju iz stvarnih podataka.

**Launcher v1.0.60:** vlasnikova izvorna transparentna 3D ikona konvertirana je u lokalni WebP foreground (trenutačno optimizirana mobilna rezolucija 96 × 96). Zamijenjen je samo foreground u standardnoj i okrugloj adaptivnoj ikoni; Androidova maska i `launcher_background` ostaju. Nakon novog emulator screenshot QA obavezno izmjeriti SSIM/MAE i provjeriti izrezivanje rubova na različitim uređajima, zatim po potrebi povećati razlučivost resursa. Wordmark je zaseban živi Compose element i još nije 1:1. Ne objavljivati „0 % odstupanja” bez dokazive mjere.
