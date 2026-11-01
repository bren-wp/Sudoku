# Splash pixel-QA — Sudoku v1.0.49

## Cilj

Prva od osam slijednih iteracija zaslona, nakon spajanja PR-a #55. Originalna vlasnikova referenca: `08-splash.png` (941×1672), bez promjene podataka igre i bez lažnog screenshot UI-ja. Sljedeći zaslon: Home.

## Stvarno izmjerena usporedba

Izvorni stvarni Android emulator prije izmjena: [Android visual smoke 41144169319](https://github.com/bren-wp/Sudoku/actions/runs/41144169319), commit `484c1faaa48953d0f42e019b5977b51357b2b56e`.

Izvorni stvarni Android emulator nakon promjena: [Android visual smoke 41353117084](https://github.com/bren-wp/Sudoku/actions/runs/41353117084), commit `38686da413741a3e91e389aa69e03fad9647b702`.

Isti originalni PNG, ista rezolucija **941×1672**, isti kanali RGB, bez resizea, cropanja ili umjetnih vrijednosti. Metrika: mean absolute error svih RGB kanala na ljestvici 0–255.

| Regija | Prije | Nakon | Razlika (nakon−prije) |
|---|---:|---:|---:|
| Cijeli Splash | 12.364 | 12.033 | -0.331 |
| Gornja pozadina (y=0..419) | 10.481 | 9.923 | -0.558 |
| Dekoracija i gornji dio (y=0..699) | 13.415 | 12.928 | -0.487 |
| Znak i wordmark (x=145..794, y=400..927) | 24.853 | 23.872 | -0.981 |
| Slogan (x=100..839, y=910..999) | 19.635 | 20.599 | +0.964 |
| Noćni krajolik (y=995..1671) | 6.690 | 6.690 | 0.000 |
| Indikator (x=408..519, y=1420..1529) | 12.890 | 12.890 | 0.000 |

Globalna greška je **smanjena ~2.7%**. Slogan ima veću lokalnu grešku, a stari komprimirani prizor ostaje glavni estetski kompromis: ne deklarirati 1:1.

## Testni dokazi

Android CI i oba emulatorska testa uspješno su završili na commitu `38686da`, nakon korekcije preklapanja (prethodni `b679cc0` vizualni test imao je tri pada, ispravljena bez preskakanja testova). Dopuna za uske zaslone i bump verzije u izdanju 1.0.49 traže nove rezultate na završnom SHA.

## Preostalo

- Dodatno uskladiti tipografiju slogana, boje i sjaj.
- Usporediti kvalitetu izvorne ilustracije s lokalnim pejzažnim resursom bez ugrađivanja statičnih UI screenshotova.
- Ponovno snimiti oba emulatora za završni izdavački commit i usporediti nativnu referentnu rezoluciju.
- Tek nakon toga nastaviti redom s Home / početnim zaslonom.

## Potvrđena završna verifikacija v1.0.49

Objavljeni kod: `f94633c827df65703840f1258754a97bd924538b`.

- [Android CI 41353666041](https://github.com/bren-wp/Sudoku/actions/runs/41353666041): **SUCCESS** (unit tests, Python pixel QA regressions, lint, debug/release APK i AAB build).
- [Android visual smoke 41353665989](https://github.com/bren-wp/Sudoku/actions/runs/41353665989): **SUCCESS na oba stvarna emulatora** (941×1672 i 720×1280); izvezeni PNG artefakti su dostupni.
- [Publish Sudoku release 41353666218](https://github.com/bren-wp/Sudoku/actions/runs/41353666218): **SUCCESS**, [v1.0.49](https://github.com/bren-wp/Sudoku/releases/tag/v1.0.49) objavljeno kao *prerelease* s debug-potpisanim testnim APK-om, nepotpisanim AAB-om i SHA256SUMS. Nije potpisano za Google Play.
- Ponovljeni MAE na samom izdanju `f94633c` ostaje **12.033/255** (RGB, 941×1672); isječci uže rezolucije nisu preskalirani ili prikazivani kao 1:1 dokaz.

Zaostala odstupanja (slova slogana i fotografski pejzaž) i dalje blokiraju tvrdnju o doslovnoj pixel-perfect jednakosti.
