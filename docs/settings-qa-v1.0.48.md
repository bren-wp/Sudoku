# Sudoku v1.0.48 — Postavke, zaslon 8/8

## Izvorna referenca i početno stanje

- Original vlasnika: `Slika ChatGPT 24. lis 2026. 20_47_20-10(9).png`, 941 × 1672 px.
- Stvarni emulator (prethodno izdanje): `04-settings.png` iz finalnog Android UI artefakta za v1.0.46, 941 × 1672 px.
- Izmjerena početna prosječna RGB apsolutna razlika (MAE; 0 je potpuno isto, niže je bolje):

| Područje | Baseline MAE |
|---|---:|
| Cijeli zaslon | 24.522 |
| Zaglavlje y0–206 | 22.720 |
| Izgled x40–893, y205–535 | 23.668 |
| Igra x40–893, y545–988 | 25.361 |
| Zvuk i vibracija x40–893, y995–1120 | 39.366 |
| Jezik x40–893, y1130–1258 | 29.641 |
| O aplikaciji x40–893, y1265–1520 | 23.336 |
| Donji krajolik y1518–1672 | 25.025 |

## Popravci u Compose kodu

1. Vlasnikova struktura pet zasebnih tamnoplavih Settings panela s tankim obojenim obrubom, bez dekorativne mreže koja prolazi kroz kontrole.
2. Izgled: naslov s vlastitom tirkiznom ikonom, dva stvarna 48dp pristupačna prekidača i diskretna razdjelna crta.
3. Igra: ista struktura za automatske bilješke, provjeru grešaka i isticanje brojeva.
4. Zvuk i vibracija: lokalna ljubičasta ikona s plavim rubom panela. Klik otvara postojeći dijalog sa zvukom i vibracijom.
5. Jezik: lokalna tirkizna ikona i obrubljeni `Hrvatski` selektor sa strelicom dolje; dijalog i dalje istinito govori da je trenutačno podržan samo hrvatski.
6. O aplikaciji: manje visine i ujednačeni razdjelnici za pravila privatnosti i stvarnu BuildConfig verziju.
7. Šifrirane offline sigurnosne kopije ostaju dostupne skrolanjem ispod prvog kadra. Njihova se funkcionalnost i korisnički podaci ne mijenjaju.
8. Pozadina za postavke koristi prigušenu mrežu u kutovima gornjeg dijela; dekorativna ilustracija i jezero pomaknuti su prema dnu kako ne bi prolazili kroz kartice.
9. Novi Android UI smoke test provjerava poredak, nepreklapanje i bočne margine svih pet panela, uz pristup offline sigurnosnim kopijama.

## Tehnička provjera

- Novi kandidat: v1.0.48, versionCode 49; `com.brendigo.sudoku`, minSdk 26, target/compileSdk 36.
- Zadržati postojeći Gradle 9.6.0 / Android Gradle Plugin 9.5.0 i tri CI workflowa.
- JUnit, postojeći pixel-QA skripti, Android Lint, debug/release APK i AAB build.
- Emulatorske snimke: 941 × 1672 i 720 × 1280; ponovno usporediti `04-settings.png` po gore opisanim regijama.
- Provjeriti pristupačnost i sve stvarne opcije, dijaloge privatnosti, zvuka, jezika, podatke o aplikaciji te uvoz/izvoz sigurnosne kopije.
- Bez tvrdnje o potpunom 1:1 dok ne prođu stvarna usporedba i testovi. Ne simulirati vrijednosti opcija iz reference.
- Potpisano Google Play izdanje samo uz valjani izdavački ključ. U suprotnom testni APK i nepotpisani AAB.
