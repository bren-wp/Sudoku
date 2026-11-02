# Sudoku v1.0.55 — Postignuća + Statistika, pixel QA

## Odabir prioriteta

Korisnik traži zaslon po zaslon 1:1 i poboljšanje zaslona s najvećim odstupanjima. Sve izvorne referentne slike i stvarni Android screenshotovi uspoređeni su na identičnih **941×1672 px** bez preskaliranja, izrezivanja ili skrivanja dinamičkih podataka.

| Zaslon | Polazni RGB MAE / 255 |
|---|---:|
| Postignuća `07-achievements.png` | **28.85024** |
| Statistika `06-statistics.png` | **26.45647** |
| Početni `01-home.png` | 23.9902 |
| Dnevni izazov `05-daily.png` | 23.5594 |
| Nova igra `02-new-game.png` | 22.3279 |
| Aktivna igra `03-game.png` | 19.3346 |
| Postavke `04-settings.png` | 19.2902 |
| Splash `08-splash.png` | 12.0331 |

Ovo su sirove usporedbe RGB kanala, koje uključuju podatke emulatorske sesije. Ilustrativni izvor sadrži 420 bodova i osam otključanih nagrada, a stvarna testna aplikacija drukčije vrijednosti. **Ne smiju se fabricirati rezultati, datumi ni nagrade radi smanjenja MAE.**

## Izvor i nakon

- Prije: realni `06-statistics.png` i `07-achievements.png` iz uspješnog [Android visual smoke 41364134155](https://github.com/bren-wp/Sudoku/actions/runs/41364134155), v1.0.54, SHA `a045a79c3a3f88e442f793f7bafd162e0fb6cf14`.
- Poslije: realne snimke iz [Android visual smoke 41366273931](https://github.com/bren-wp/Sudoku/actions/runs/41366273931), završni UI commit `72e1605ca1810d0d229d3d74305f1fcdcbd7cd11`.
- Korisnikove izvorne reference `references/06-statistics.png`, `references/07-achievements.png` nisu kopirane u UI, mijenjane ni zamijenjene.
- Priložena odvojena QA arhiva sadrži izvorne slike, stvarne snimke prije/poslije, vizualne razlike i mjerljive `regions.csv`, `measurements.json`.

## Potvrđene regionalne metrike

| Regija | Prije | Poslije | Razlika |
|---|---:|---:|---:|
| Postignuća — cijeli zaslon | 28.85024 | **28.78820** | −0.06204 |
| Postignuća — prvi red | 38.19531 | **38.02878** | −0.16653 |
| Postignuća — drugi red | 39.28587 | **39.08268** | −0.20319 |
| Postignuća — treći red | 23.07345 | 23.07345 | 0 |
| Postignuća — četvrti red | 23.65153 | 23.65153 | 0 |
| Statistika — cijeli zaslon | 26.45647 | **26.03790** | −0.41857 |
| Statistika — četiri metričke kartice | 33.06711 | **31.01450** | −2.05261 |
| Statistika — Dnevni niz | 37.99924 | **29.12590** | −8.87334 |
| Statistika — grafikoni | 27.68293 | 27.68292 | ≈0 |

Metrika je aritmetička sredina apsolutnih razlika sva tri RGB kanala, skalirana 0–255. Niža vrijednost označava manju vizualnu razliku. **Doslovno 1:1 nije ostvareno.**

## Izmjene i regresija

1. `Pages.kt`: dodatna svjetlina i radijalni sjaj na ikonama u prva dva reda, uz očuvanje lokota i pravog broja osvojenih bodova. Prva verzija primijenila je sjaj na previše kartica, što je pogoršalo red 3 i 4; korekcija `919600e` vratila je iste piksele na tim redovima.
2. Opći novi sjaj na četiri metričke kartice pogoršao je Statistiku (`26.4565 → 26.5148`) i uklonjen je. Zadržane su izvorne ikone, razmaci, interakcije i podaci.
3. `Pages.kt`: `Canvas(Modifier.matchParentSize())` crta mjesec i više slojeva planina iza **Dnevnog niza**, s malom pagodom. To je vektorska pozadina, ne preslika screenshota. `currentStreak()` i dalje određuje broj dana.

## Funkcionalnost i CI

- [Android CI 41366273955](https://github.com/bren-wp/Sudoku/actions/runs/41366273955): **SUCCESS**, jedinice, lint, metrike i Android build.
- [Android visual smoke 41366273931](https://github.com/bren-wp/Sudoku/actions/runs/41366273931): **SUCCESS**, oba emulatora **941×1672** i **720×1280**, uključujući 24 postignuća, rekreaciju Activity, povijest, četiri kartice statistike i prikaz podataka.
- Testovi su vezani uz UI commit `72e1605`. Završni izdavački commit `v1.0.55` također mora proći vlastite provjere.
- Ne mijenjaju se lokalna pohrana, sigurnosne postavke, `applicationId` ni korisnički napredak.

## Nastavak

Prioritet sljedeće zasebne dorade: **Početni zaslon**, zatim preostala odstupanja ilustrativnih kartica i namjenski launcher icon (koji ima visoku neovisnu MAE). Nema lažiranja otključanosti i povijesti radi reference.
