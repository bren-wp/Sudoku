# Sudoku v1.0.68 — pregled pohrane, sigurnosti, dead-code i stvarnih zaslona

**Predmet:** Sudoku by Brendigo, grana `fix/sudoku-storage-queue-visual-qa-v1068`, Android 8+ / API 26+, offline Jetpack Compose. **Datum:** 12. 11. 2026.

## Utvrđeni problemi

1. **Performanse (P1):** prethodni off-main-thread writer je za svaki brzi pritisak tipke stvarao zasebni `Deferred`/diskovni `commit()`. Time nije blokirao glavnu nit, ali je mogao proizvoditi previše IO operacija i trošiti bateriju.
2. **Vizualni test (P1):** `SudokuVisualSmokeTest.shot()` uzorkovao je osam RGB točaka i odbijao snimku kada su tri bile gotovo bijele. Stvarna `v1.0.67` Android main `reference` matrica prijavila je: `Light system overlay obscures 08-splash, but no system ANR action was found`. JUnit ZIP pokazao je da je to bio jedini pad između četiri testa. Sama svjetlina nije dokaz Android modalnog prozora.
3. **Oporavak (v1.0.67 provjera):** prethodni pad `tablet-shard-2` nastao je zbog nepotrebnog zapisa migracije. Na PR-u za v1.0.67 ponovljeni `tablet-shard-2` je završen uspješno, potvrđujući raniji hotfix.

## Ispravci

- Writer koristi jedan procesni `Dispatchers.IO` red i zamjenjuje *samo* još neizvršene, uzastopne **automatske** snimke iste SharedPreferences instance novijim stanjem.
- Transakcijski `checkpoint` uvijek je zasebna operacija s `CompletableDeferred<Boolean>`, nikada se ne objedinjuje s budućim ili prijašnjim zapisom i vraća `false` nakon neuspjelog diska.
- `pendingSnapshotFor` gleda najnoviji zahtjev u redu, odnosno aktivni zahtjev za iste preference, pri stvarnoj rekreaciji aktivnosti.
- `SudokuVisualSmokeTest` provjerava *stvarni Android ANR* preko UI hijerarhije (naslov i `Wait`), bez lažnih iznimaka za legitimne bijele ili svijetle 3D elemente. Provjerava i prednji paket; neočekivano otvorena druga aplikacija i dalje ruši test. Test i dalje snima stvarni prikaz Compose UI-a, ne maketu.
- `SudokuAsyncPersistenceTest` proširen je simulacijom 51 brzog spremanja s blokiranim IO zapisom. Provjerava broj fizičkih upisa, odsutnost pisanja na UI niti i ispravan posljednji podatak nakon nove instance kontrolera.

## Sigurnosni audit — provjerena područja

| Stavka | Nalaz | Granica provjere |
|---|---|---|
| Mreža/dozvole | Aplikacija deklarira bez internetskih dozvola; automatski Android backup isključen | Statički pregled manifesta i postojećeg instrumentacijskog testa |
| Integritet pohrane | Sprema igru, Undo i oba statistička zapisa jednim `Editor.commit()` izvan UI niti | Disk-full i race scenariji pokriveni postojećim Android testovima |
| Šifrirani izvoz/uvoz | Postojeća AES-GCM zaštita i provjera stanja prije vraćanja nisu uklonjene | Nije proveden vanjski kriptografski audit |
| Race condition | Kritične provjere odvojene od autosave spajanja; stare aktivnosti ne trebaju prepisati restored podatke | Testovi s kontroliranim usporavanjem diska |
| Objavljivanje | APK/AAB i SHA256SUMS potvrđuju GitHub Actions, nepotpisani AAB mora biti prerelease | Certifikat vlasnika nije raspoloživ za Play-ready provjeru |

## Dead-code pregled

- Stari `Deferred` lanac zamijenjen je jednim redom s jasno odvojenom `enqueue()` i `checkpoint()` granom; uklonjeni su bespotrebni `Deferred` i `async` importi.
- U vizualnom testu uklonjeni su pomoćna RGB heuristika i ponovljeni screenshot poslije lažne detekcije ANR-a; sada se ANR može obraditi samo temeljem vidljivog teksta sistemskog dijaloga.
- Statički je pregledana **21 Kotlin datoteka** glavnog izvornog koda (podatkovni sloj, engine i Compose). Uklonjeno je sedam potvrđeno nekorištenih importova: `Stroke` iz AchievementVisuals; `drawIntoCanvas`, `nativeCanvas`, `selected`, `DpSize` iz DailyVisuals; `Options` iz Pages; `fillMaxSize` iz StatisticsVisuals. Uvezeni `getValue`/`setValue` zadržani su jer omogućuju Kotlin property delegation, unatoč tome što se ne pojavljuju kao doslovni pozivi u izvoru.
- Nisu uklanjani Compose elementi ni resursi na temelju nepouzdane tekstualne pretrage.
- Za potpuni audit svih transitive dependencyja, R8 reachabilityja i neupotrebljenih sredstava nužni su Android lint, gradle dependencies i analiza rezultata CI-a. **Ne tvrdi se da je cjelokupni dead-code audit dovršen.**

## Plan validacije

- `Android CI`: unit testovi, pixel QA metric regression, Android lint, debug APK i release APK/AAB build.
- `Android visual smoke`: 11 emulatorskih profila, osobito **reference** i četiri tableta. Nije dopušteno proglasiti vizualni QA uspješnim ako neki profil ima neobjašnjen pad.
- Potvrda novog `SudokuAsyncPersistenceTest` za objedinjavanje zapisa i obnovu posljednje vrijednosti.
- GitHub Release v1.0.68: stvarni APK, AAB, SHA256SUMS; točna klasifikacija testnog izdanja bez službenog potpisa.

**Preostalo:** potpuno ručno mjerenje 1:1 za sve originalne vlasnikove reference i uređaje, stvarni Play-signing, CVE/SBOM analiza svih ovisnosti te test na fizičkim Android uređajima. Trenutačna verzija ne smije tvrditi 0% vizualnog odstupanja bez zasebnih mjerenja.
