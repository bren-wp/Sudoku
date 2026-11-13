# Sudoku v1.0.71 — gameplay, pixel parity i release security audit

Datum: 13. studenoga 2026.  
Grana: `fix/sudoku-gameplay-pixel-sbom-r8-v1071`  
Autor i izdavač: **BRENDIGO**, brendigo.com

## 1. Gameplay: uklanjanje lažne radnje Erase

Kada je uključeno `autoNotes`, automatske bilješke za prazno polje regeneriraju se odmah nakon svake radnje brisanja. Prethodna implementacija nudila je aktivan gumb **Izbriši** za polje koje sadrži samo automatske bilješke. Pritiskom je stvoren Undo zapis i nepotreban diskovni autosave bez stvarne izmjene ploče.

Sada su `canErase()` i `erase()` dosljedni: na praznom polju s automatskim bilješkama gumb je onemogućen, a direktni poziv ostaje bez učinka. Brisanje upisanog broja i ručnih bilješki (kada je `autoNotes=false`) i dalje funkcionira. Novi `SudokuAutoNotesEraseTest` provjerava No-Op, Undo, stvarno brisanje, ručne bilješke i ponovno učitavanje iz diska.

## 2. Točnost 1:1 pixel QA — stvarni bug u mjerilu

Dosadašnji `scripts/pixel_compare.py` za ocjenu potpunog podudaranja uspoređivao je **prosječnu RGB MAE zaokruženu na tri decimale**. Razlika samo **jednog piksela u jednom kanalu** na 941×1672 može dati prikazani MAE `0.000`, a ipak ne biti 1:1. Isto se događalo kod jednog promijenjenog alfa piksela u PNG-ovima za brend.

Sada se broj točno jednakih RGB piksela računa cjelobrojno po svim kanalima; za transparentni 1254×1254 brending računa se i RGBA podudarnost. `exact_1_to_1` dopušta prolaz **isključivo ako su svi pikselski kanali jednaki**, a `scripts/final_ten_reference_qa.py` koristi upravo to polje. Izvještaji bilježe `exact_rgb_pixels`, `exact_rgb_pixels_pct` i `exact_rgba_pixels_pct`. Dva nova Python regresijska testa koriste namjerno samo jedan promijenjeni kanal i dokazuju da se ne može lažno proći 1:1.

### Povijesne vrijednosti — nisu mjerenja za v1.0.71

Posljednja dokumentirana usporedba originalnih korisnikovih referenci i stvarnih emulatora nalazi se u `docs/pixel-qa-v1.0.64.md`:

| Zaslon | RGB MAE 0–255 | Mjereno u verziji |
| --- | ---: | --- |
| Početna sa spremljenom igrom | 24.1152 | v1.0.64 |
| Nova igra | 22.6224 | v1.0.64 |
| Gameplay | 20.2529 | v1.0.64 |
| Postavke | 19.4239 | v1.0.64 |
| Dnevni izazov | 24.7777 | v1.0.64 |
| Statistika | 26.5834 | v1.0.64 |
| Postignuća | 29.8627 | v1.0.64 |
| Splash | 10.3842 | v1.0.64 |

**Ograničenje:** izvorni privatni PNG-ovi za svih deset dizajnerskih referenci namjerno nisu u GitHub repozitoriju. Bez dostupnih originala i aktualnih *jednakostanjskih* izvornih PNG snimki nije moguće pošteno izmjeriti novo odstupanje svih zaslona u v1.0.71 niti tvrditi 0 %. Stvarni screenshotovi iz repozitorija nisu zamjena za originalne vlasnikove reference. Potpuno mjerenje izvesti naredbom:

```bash
python scripts/final_ten_reference_qa.py \
  --reference-dir /putanja/do/izvornih-PNG \
  --actual-dir /putanja/do/stvarnih-emulatorskih-PNG \
  --output-dir qa-pixel-diff/v1.0.71 --allow-imperfect
```

Rezultat s `--allow-imperfect` **ne smije** se predstavljati kao potvrđeni 1:1 prolaz.

## 3. Release R8 / dead-code evidencija

Release konfiguracija već koristi `isMinifyEnabled=true` i `isShrinkResources=true`. Dodana je **provjera stvarnog kompajliranog release APK-a** i R8 mapping artefakta. `scripts/audit_android_release.py` provjerava da je `mapping.txt` stvarno proizveden i sadrži R8 class mappings. Ako R8 generira `usage.txt`, izvještava o njegovoj dostupnosti i broju redaka, **ali se ne pretvara da taj broj sam znači potpunu odsutnost mrtvog koda**.

Ne uklanjaju se nasumično klase, Compose funkcije, Android resursi ili `-keep` pravila zbog opasnosti od rušenja i refleksijskih referenci. Debug i release artefakti ostaju različiti.

## 4. SBOM i sigurnosne provjere

U Gradle build integriran je CycloneDX plugin **3.5.1** (build-time; nije dio APK-a), uz konfiguraciju **isključivo `releaseRuntimeClasspath`**. `app:cyclonedxDirectBom` stvara JSON s direktnim i tranzitivnim Maven komponentama i njihovim verzijama.

Novi CI korak strogo provjerava:

- postoji valjan, neprazan CycloneDX SBOM s razriješenim Maven identitetima;
- u release inventaru nema testnih artefakata poput `androidx.test` / `junit` ili Google Ads SDK-a;
- R8 mapping stvarno postoji i sadrži mapirane klase;
- izdani APK se otvara kroz Android `aapt2 dump permissions` i nema INTERNET, kontakata, lokacije, SMS-a, `QUERY_ALL_PACKAGES` ili obavijesti;
- JSON izvještaj i CycloneDX BOM objavljuju se kao **GitHub Actions security artifact**. R8 mapping nije namjerno objavljen kao javni artifact jer sadrži detalje internih klasa.

**Izričito preostaje** vanjski OSV/CVE pregled inventara, R8 reachability po aplikacijskim funkcijama, skeniranje osjetljivih podataka, provjera API-ja i testiranje fizičkih uređaja. Izostanak zabranjenih dozvola i valjan SBOM **ne dokazuju** da nema svih ranjivosti.

## 5. Izdanje

Pripremljeno `versionName=1.0.71`, `versionCode=72`. APK/AAB izdanje moguće je potvrditi tek nakon prolaska Android CI + 11 emulatorskih profila + publish GitHub Actions. Bez vlasničkog signing ključa release ostaje testni `prerelease` i AAB je nepotpisan.
