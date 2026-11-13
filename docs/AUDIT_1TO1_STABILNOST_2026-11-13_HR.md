# Sudoku by BRENDIGO — detaljni audit, backlog i dokazni kriteriji

**Datum pregleda:** 13. studenoga 2026.  
**Osnova:** javni main commit e98f90d1c09b3a82d3e79fae54b96b3a0efc87c7, posljednje objavljeno izdanje v1.0.72, postojeći PR #79 s predloženom v1.0.73.  
**Metoda:** izravan pregled izvora (Kotlin, Gradle, AndroidManifest, dokumentacija, skripte, workflows), GitHub povijesti PR-ova, postojećih QA rezultata i popisa objavljenih izdanja. Ovaj pregled nije zamjena za novu lokalno izvedenu Android emulatorsku provjeru ni za neovisni pentest.

## Sažetak provjerenog stanja

- **Dokazana 1:1 jednakost: NE.** U postojećem izvještaju docs/pixel-qa-owner-v1.0.71-measured.md *0/10* vlasnikovih originalnih PNG referenci strogo se poklapa s **v1.0.71** stvarnim Android snimkama. To su povijesne mjere: nisu validan rezultat za aktualni PR #79 ili buduće izdanje.
- **Korisnički zasloni:** 8 glavnih Compose zaslona (Splash, Home, New Game, Game, Daily, Statistics, Achievements, Settings), plus launcher i wordmark te dijalozi i dodatna stanja.
- **Generator:** 4×4, 6×6 i 9×9, 6 razina gustoće tragova, koristi SudokuEngine.countSolutions za jedinstvenost. Trenutna imena težina **ne znače** logički ocijenjenu solver težinu. Prethodno su validatorske metode dopuštale alternativne nepripadajuće dimenzije okvira iste ukupne veličine; ovaj PR dodaje kanonsku provjeru 2×2, 2×3, 3×3 i testove.
- **Offline arhitektura:** Manifest nema INTERNET dozvolu; cleartext isključen; Android backup isključen; aktivnost ima potrebni exported=true za launcher. SharedPreferences snapshot writer je serijaliziran s checkpoints; opsežni instrumentacijski testovi pokrivaju autosave, Undo, dnevne igre, backup i Activity recreation.
- **Kriptografske sigurnosne kopije:** AES-GCM, PBKDF2WithHmacSHA256 s 240.000 iteracija, provjera strukture, autentikacije i duljina te rollback pri grešci. Potrebna je daljnja procjena KDF parametara, sigurnosti memorije, velikih ulaza i dugoročnih migracija.
- **Build:** main AGP 9.5.0, Gradle 9.6.0, Kotlin Compose plugin 2.4.21, compileSdk/targetSdk 36, minSdk 26, Java 17; verzija u main 1.0.72 / versionCode 73.
- **Sigurnosni proces:** CycloneDX releaseRuntimeClasspath, OSV querybatch i R8 usage/mapping audit su integrirani u Android CI. Dokumentirani izlaz CI #43341674597: 90/90 release Maven modula ispitano i 0 poznatih OSV nalaza na datum skena. To **nije** potpuni dokaz odsutnosti ranjivosti.
- **Izdavanje:** posljednje javno izdanje v1.0.72 objavljeno 13.11.2026 kao **prerelease**, s testnim APK, nepotpisanim AAB i SHA256SUMS. PR #79 je zasebno otvoren za v1.0.73, a ne javno izdanje; stanje njegovog CI-ja treba provjeriti neposredno prije spajanja.
- **Postojeća QA infrastruktura:** 11 emulatorskih CI poslova, ali reference job prvenstveno snima ekran, četiri tabletna sharda nose pun instrumentacijski suite, dok ostali profili izvode kraći flow. To nije isto što i potpuni functional test *svakog zaslona* na *svakom* profilu.
- **Stari master prompt:** prije ovog PR-a docs/MASTER_PROMPT_HR.md upućivao je na PR #55, iako je noviji PR #79 otvoren. Ažurirano ovim PR-om.

## Mjerljive razlike na posljednjoj potvrđenoj puni-referentnoj osnovi

Izvor: docs/pixel-qa-owner-v1.0.71-measured.md. RGB MAE je intenzitet greške na skali 0–255, **ne** postotak odstupanja. Broj „točno jednakih piksela” mjeri strogi RGB uvjet, uključujući razlike koje mogu dolaziti od dinamičkog sadržaja.

| ID | Objekt | RGB MAE v1.0.71 | Potpuno 1:1 | Prioritet dijagnostike |
| --- | --- | ---: | --- | --- |
| 07 | Postignuća | 29,862714 | ne | P0 vizualni |
| 06 | Statistika | 26,583405 | ne | P0 vizualni |
| 05 | Dnevni izazov | 24,779138 | ne | P0 vizualni |
| 01 | Naslovnica (Resume) | 24,115176 | ne | P0 vizualni |
| 02 | Nova igra | 22,622370 | ne | P1 vizualni |
| 03 | Gameplay | 20,133552 | ne | P1 vizualni |
| 04 | Postavke | 19,419634 | ne | P1 vizualni |
| 08 | Splash | 10,384231 | ne | P1 vizualni |
| 09 | Wordmark, sirovi RGBA | 18,833920 | ne | P1 branding |
| 10 | Launcher ikona, sirovi RGBA | 16,174683 | ne | P1 branding |

**Ograničenja ove rang-liste:** istaknute razlike između gameplay sadržaja, datuma i stvarnog korisničkog napretka mogu dramatično utjecati na puni MAE; razlike anti-aliasinga i transparentnih RGB vrijednosti treba zasebno pregledati. Za prioritet stvarnog mijenjanja koda koristiti regionalni MAE pri istom testnom stanju, ne samo ukupni broj.

## P0 — potvrđeni problemi i blokatori

| Problem | Stanje dokaza | Potreban zahvat i acceptance test |
| --- | --- | --- |
| 1:1 nije postignut ni za jednu od 10 referenci na v1.0.71 | Potvrđeno izmjerenim izvještajem | Osigurati **stvarni novi** 941×1672 Android screenshot za točan head SHA i izvornu referencu s SHA-256 provjerom; regionalne izmjere, vizualno otklanjanje i ponavljanje bez tvrdnji o 0 % prije nulte greške. |
| Nejednak sadržaj između referenci i produkcijskih podataka | Potvrđeno metodologijom mjerenja | Dodati **androidTest-only deterministički fixture** za datum, sat, igru/seed, odabranu ćeliju, stvarnu statističku instancu, stanje postignuća i Resume. Nikad ne injektirati lažne podatke u produkcijsku logiku ili aplikaciju koja se isporučuje. |
| Nije dokumentiran automatizirani runtime smoke za optimizirani R8 release APK | Potvrđeno pregledom Android CI i visual-smoke workflowa: release se gradi i statički analizira, emulator koristi debug instrumentacijski setup | Dodati instalaciju i pokretanje **stvarnog minificiranog release APK-a**, reproducibilno potpisanog samo testnim QA ključem na emulatoru, uz početni ekran, početak partije, unos, zapis i ponovno pokretanje. Bez Play signing tvrdnji. |
| Zastarjela glavna razvojna uputa na stari PR | Potvrđeno u prethodnom docs/MASTER_PROMPT_HR.md | Zamijeniti prompt PR-first postupkom, trenutno aktivne PR-ove otkrivati prije izmjena; promjena ovoga PR-a. |

## P1 — detaljan backlog po modulima

### Splash i početna stranica
- Uskladiti Splash logo, naslov, slogan, razmak elemenata, pozadinske 3D refleksije i byline; testirati Android 12+ sustavni splash te launch cold/warm i privremeno prikazane sistemske barove.
- Na Home stranici usporediti dinamičko stanje **Nastavi** s referentnim Home, a ne prazni Home; mjeriti širinu CTA-a, kartice, 2+3 navigaciju, sedmodnevni niz, dno, sjene, mikroikonice i vertikalne linije; testirati zadržavanje session statea.
- PR #79 već mijenja neke dimenzije na Home i Splash; **ne duplicirati** te zahvate bez novog dokaza i provjere ishoda PR-a.

### Nova igra i generiranje
- Provjeriti svaki format 4×4, 6×6, 9×9 i svih 6 razina; ispravnost brojeva/okvira/jedinstvenosti, deterministic seed, vrijeme stvaranja i responzivnu konfiguraciju.
- Trenutni SudokuEngine.create koristi postotak uklanjanja 35/43/50/57/64/70 te countSolutions, ne solver rating. Ako marketinški tekst obećava stvarnu težinu, dizajnirati solver koji ocjenjuje skup potrebnih strategija i dodati instrumentacijska mjerenja, bez rušenja seed stabilnosti dnevnih igara.
- Ovaj PR očvršćuje kanonske dimenzije u isValidCompleted/countSolutions; dodaje negativne 1×4/3×2/1×9 scenarije.

### Gameplay
- Provjeriti ime, brojke, 3D ploču, HUD (vrijeme, pogreške, savjeti, napredak), visinu i redoslijed tipkovnice, aktivne/isključene kontrole, touch semantics, rasterizaciju na xhdpi i male ekrane.
- Rubni slučajevi: Erase prazne automatske bilješke, Undo nakon hint-a, prebacivanje autoNotes, usporedba rješenja, stvarna završna statistika, točno 3 pogreške, game over, početna selekcija, rotacija, background/foreground i dvostruki klik.
- Istražiti performanse recompose i Canvas draw; mjeriti jank, cold start, memory peak, trajanje generiranja i fsync učestalost, umjesto da se optimizacija pretpostavlja.

### Dnevni izazov
- Datum/seed/zone, ponoć i DST, generiranje tijekom promjene lokalnog dana, stari dnevni zapis nasuprot novome, odvajanje Save i Undo normalne/dnevne igre.
- Dnevni streak i kalendar, retroaktivno dovršenje, sprječavanje duplih nagrada, povrat nakon process death, kombinacije lokalnog datuma i promjene time zone.
- Usporediti 3 gornje metrike, kalendar, CTA, tjedni niz i 3D art bez lažnih igraćih podataka. PR #79 mijenja razmake: usporediti nakon merge/capture.

### Statistika
- Provjeriti strogu konzistentnost played/finished, elapsed i totalPlayedSeconds, completed, winByLevel/size, dnevne bodove, streak, history; posebno incident pri disk-full i restoreu.
- Provjeriti kako se učitavaju nevaljane vrijednosti iz legacy lokalnih podataka bez rušenja grafa ili ekrana; validirati granice history i clamp display metrika.
- Uskladiti KPI kartice, grafove i empty state sa slikom, uz odvojene fixtures; PR #79 već mijenja visinu empty history panela.

### Postignuća
- Za svih 24 ciljeva napraviti tablicu: okidač, izvorni podatak, prag, status, progress, ikonica, nagrada i pripadni UI test.
- Uskladiti omjere kartica, 3D umjetnički sloj, font, lock/unlock, zaobljenja i refleksije. Prioritet prema najvećoj dokumentiranoj RGB razlici, ali uz regionalni dokaz.
- Usporediti normalan viewport, 320dp, 270dp, 240dp, tablet i 200 % font. PR #79 mijenja visinu četvrtog reda; provjeriti accessibility.

### Postavke, spremanje i backup
- Dokazati učinak svih prekidača i povratak u igru, uključujući pretjerano brzo mijenjanje opcija, Activity recreation i reset procesa.
- SudokuStateWriter: analizirati queued autosaves, checkpoint redoslijed, potencijalni slučaj neuspjelog automatskog commita i korisniku vidljiv signal; bez blokiranja glavne niti.
- SudokuBackup: analizirati AEAD autentikaciju, KDF i resurse, 1 MB limit, rollback, tampered JSON, nevaljane givens, Undo i daily dates te kompatibilnost izdanja.
- SAF dijalozi: lozinka, CANCEL, nevaljana datoteka, pregled prije prepisivanja, error poruke bez otkrivanja lozinke i stvarnih rješenja.

### Launcher, resursi, branding i README
- Usporediti 1254×1254 RGBA original za ikonu i wordmark; kontrolirati transparentni rub, adaptive icon crop, round/themed Android 13+ i različite launcher maske.
- Stvarne 8 WebP snimki iz docs/screenshots postoje u README-ju; poslije UI zahvata potvrditi njihovu verziju i generirati novu galeriju iz stvarne aplikacije. Ne stavljati konceptne slike.
- Play store PNG, bundle ZIP, 512×512 ikona, 1024×500 feature grafika i metapodaci: provjeriti kvalitativno, tehničke dimenzije i identičan BRENDIGO branding.

## P1 — sigurnost, stabilnost i potpuni dead-code audit

| Obuhvat | Što konkretno pregledati | Dokaz završetka |
| --- | --- | --- |
| Manifest i privacy | INTERNET i druge dozvole, exported activity, Backup/Transfer, cleartext, privacy copy | aapt2/dump final APK + manifest diff i testovi |
| Supply chain | Sve releaseRuntimeClasspath Maven koordinate, CycloneDX, OSV/GHSA/CVE, datumi | verzijski SBOM, online API izvještaj, 100 % queries, poznati nalazi blokiraju izdanje |
| R8/dead-code | usage.txt, mapping.txt, stvarni call graph, reflection i Compose retention | točan popis uklonjenih simbola i ručno potvrđenih suvišnih datoteka; test prije i poslije brisanja |
| GitHub Actions | Permissions, tajne, third-party action pinning, artefakti, cancellation, release provenance | pregled workflowa + simulacija neuspjeha/uspjeha i verified release assets |
| Persistence | Poništavanje, background writer, crash, disk-full, restore, stara migracija, dvije sesije | Android instrumentacijski testovi, timeline podataka, nema gubitka progressa |
| Memory/performance | generator latency 9×9, Compose allocations, Canvas, grafike, snimka, ANR/jank | reproduktibilni benchmark profili i before/after podaci |

## P2 — polish, održivost i kompatibilnost

- Centralizirati vizualne tokene, smanjiti duplicirane 3D efekte samo uz dokaz da je rasterizacija jednaka referenci.
- Normalizirati dinamičke tekstove i ContentDescription; provjeriti TalkBack, pristupačne zone dodira, fokus, skrol i velik font.
- Konsolidirati mnoštvo povijesnih QA izvještaja u jedan **aktualni** pregled, uz zadržavanje arhive kao dokaza; jasno označiti verziju, datum i SHA mjerenja.
- Dodati stabilan reference screenshot pipeline: original SHA256 / stvarna snimka / fixture / locale / density / git SHA / per-region report / prethodna iteracija; ne omogućiti ručno namještanje „zelenih” pragova bez evidencije.
- Selektivno osvježiti ovisnosti i testirati Android API 26–36, bez automatskog pretvaranja najnovijih verzija u tvrdnju da su kompatibilne.

## Obavezan 1:1 testni protokol

1. Zaključati referentnu datoteku: 10 izvornih SHA-256; provjeriti veličinu i alpha.
2. Izvršiti stvarni Android APK/commit na uređaju s referentnim screen size/density/locale/font scale.
3. Reproducirati isto funkcionalno stanje (Home Resume, seeded board, stats, postignuća, datum/timer) kroz **test-only** fixture.
4. Snimiti PNG iz aplikacije, ne iz dizajnerske reference, i sačuvati screenshot uz Git SHA.
5. Izvršiti scripts/final_ten_reference_qa.py bez --allow-imperfect kao **strogi** certifikacijski gate; opciju --allow-imperfect koristiti samo za numeričku dijagnostiku, nikad kao proof of parity.
6. Analizirati RGB/RGBA MAE, exact count, per-region diff, alpha/composited branding, bitmap/canvas optiku, tipografiju i baseline.
7. Ispraviti najveću stabilnu regiju u pravom Compose kodu, pokrenuti funkcionalne i emulatorske testove, usporediti s prethodnim SHA, zatim nastaviti.
8. Za različite ekran uređaje ocijeniti pravilnu **adaptaciju**, ne matematičku istovjetnost s portretnim originalom.

## CI i izdanja — minimalna kontrolna lista

- [ ] JUnit i instrumentacijski testovi novog kanonskog validatora — **dodan test, CI rezultat još treba potvrditi**.
- [ ] :app:testDebugUnitTest, :app:lintDebug, assembleDebug, assembleRelease, bundleRelease — provjeriti na head SHA ovog PR-a.
- [ ] CycloneDX / OSV / R8 na head SHA ovog PR-a.
- [ ] 11/11 Android emulator jobs na head SHA ovog PR-a (ako se mijenja app).
- [ ] Stvarni minified release APK runtime smoke (dodati ako nije pokriven).
- [ ] Pixel audit svih 10 originala na novom SHA; zero error samo ako je stvarno izmjeren.
- [ ] Re-capture svih README i Play snimaka nakon promjena UI-ja.
- [ ] Pregled i merge bez konflikta s PR #79 (bez izravnih upisa u main).
- [ ] Novo izdanje tek nakon mergea i kompletnih provjera, signed AAB samo s pravim izdavačkim ključem.

## Status i granice ove konkretne isporuke

**Implementirano u ovoj PR grani:** kanonska validacija 2×2/2×3/3×3 u SudokuEngine za kompletne ploče i countSolutions, tri negativna regresijska primjera, ažurirani master prompt i ovaj sveobuhvatni audit.

**Nije u ovom PR-u proglašeno dovršenim:** 1:1 svaki ekran, svi ručni uređaji, novi screenshotovi, runtime release APK provjera, kompletno brisanje mrtvog koda, sigurnosna certifikacija, nova objava Google Play ili GitHub stable izdanja. To su otvoreni zadaci s kriterijima, ne prikrivena dovršenja.

