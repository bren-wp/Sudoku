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

- Dnevni izazov: recessed tamnoplavi rubovi triju podatkovnih kartica. Rani pomak `Spacer(32.dp)` na `18.dp` povećao je ukupnu RGB razliku s 24,924 na 25,756, stoga je vraćen izmjereno bolji `32.dp` i minimalna visina 59dp.
- Statistika: testirana je promjena vektora 46→54dp, naziva 13→14sp i kratkih brojeva 25→27sp. Novi stvarni emulator pokazao je **regresiju MAE 26,431→26,823**, stoga je sve vraćeno na izmjereno bolje izvorne veličine; ne proglašavati poboljšanje dok se ne pripreme isti testni rezultati i ne napravi nova regionalna kalibracija.
- Postignuća: pet naprednih ilustriranih nagrada ima zadržanu prepoznatljivu vektorsku ikonu i vidljiv stvarni progress, a sivi lokot ostaje rezerviran za zaključane milestone-nagrade. Status otključavanja i screen-reader opis ostaju iz stvarnih podataka.

**Launcher v1.0.60:** vlasnikova izvorna transparentna 3D ikona konvertirana je u lokalni WebP foreground (trenutačno optimizirana mobilna rezolucija 96 × 96). Zamijenjen je samo foreground u standardnoj i okrugloj adaptivnoj ikoni; Androidova maska i `launcher_background` ostaju. Nakon novog emulator screenshot QA obavezno izmjeriti SSIM/MAE i provjeriti izrezivanje rubova na različitim uređajima, zatim po potrebi povećati razlučivost resursa. Wordmark je zaseban živi Compose element i još nije 1:1. Ne objavljivati „0 % odstupanja” bez dokazive mjere.

## Stvarni PR #64 emulatorski QA nakon prvog prolaza

| Element | MAE prethodni | MAE PR #64 (prvi screenshot) | Promjena |
|---|---:|---:|---:|
| Launcher 3D ikona | 37,200 | 16,175 | **−21,025** |
| Postignuća | 29,444 | 29,268 | **−0,176** |
| Statistika | 26,431 | 26,431 | 0,000 |
| Dnevni izazov | 24,924 | 25,756 | **+0,831** (regresija; vraćen razmak) |

Ovo su 941×1672 PNG-ovi iz stvarnog Android emulatora osim launchera koji ima izvornih 1254×1254 piksela. Statistika i Dnevni izazov nakon ciljane korekcije zahtijevaju **novo** mjerenje na novom SHA. Vrijednosti iz igraćih snimaka nisu deterministički usklađene s referentnima pa MAE ne predstavlja doslovnu vizualnu podudarnost niti postotak zadovoljstva.

## Konačni QA nakon reverzija

- Izvorna 3D launcher grafika u adaptivnom foregroundu: **37,200→16,175 MAE** na stvarnom izvozu 1254×1254; transparentni rubovi potvrđeni instrumentacijskim testom.
- Postignuća: **29,444→29,268** (malo poboljšanje, različita stanja nagrada).
- Dnevni izazov: 24,924→24,964 (razlika +0,040, praktički na prethodnoj osnovi nakon povratka razmaka).
- Statistika: privremeni povećani KPI elementi 26,431→26,823; povećanja su **povučena** i očekuje se povratak na prethodni MAE nakon novog builda. Stanje testnih podataka uvijek mora biti usklađeno da bismo mogli tvrditi 1:1.
