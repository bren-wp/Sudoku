# Sudoku v1.0.66 — pohrana, sigurnost, dead-code i vizualni QA

Datum: 12. studenoga 2026. Grana: `fix/async-persistence-audit-v1066`.

## P0: glavni Android thread i trajnost

**Potvrđeni problem u v1.0.65:** `SudokuController.flush()` i
`checkpointBeforeSwitch()` pozivali su `SharedPreferences.Editor.commit()`
iz pozivateljeve niti; `MainActivity.onStop()` pozivao je `flush()` na UI
niti. Svaki potez dodatno je serijalizirao `JSONObject` / `JSONArray`
na UI niti prije `apply()`.

**v1.0.66 promjena:**
- `SudokuStateWriter` je procesno jedinstven IO red koji obavlja JSON
  serijalizaciju i diskovni `commit()` izvan glavne niti.
- Na UI niti kopiraju se samo mali nizovi ploče i Undo potezi; sesije se ne
  mijenjaju in-place tijekom pozadinskog zapisivanja.
- `flush()` je `suspend` i čeka svoj zapis **nakon svih starijih zapisa**;
  istu potvrdu koriste promjena vrste partije i izvoz.
- `prepareForBackupRestore()` blokira novo spremanje **prije** čekanja
  redoslijeda, pa se staro stanje ne može zapisati nakon vraćenih podataka.
- `onPause()` samo zakazuje spremanje, ne čeka datotečni sustav. Nova
  instanca kontrolera može preuzeti trenutačni in-process snapshot prije
  dovršetka upisa tijekom rotacije/rekreacije.
- Partija, Undo, dvije kopije statistike i opcije zapisane su u jednoj
  `SharedPreferences` transakciji.

**Ograničenje:** Android ne jamči da će bilo koji asinkroni posao završiti
ako OS trenutačno prekine proces. Uobičajeni potezi zakazuju se odmah, a
svjesne kritične operacije (zamjena partije, izvoz, povrat kopije) čekaju
durabilni checkpoint. Dugoročna alternativa je Room + eksplicitni
transakcijski model ako veličina lokalnih podataka bitno naraste.

## P1: sigurnosni pregled izvornog koda

| Područje | Stanje | Dokaz / ograničenje |
| --- | --- | --- |
| AndroidManifest | Provjereno statički | Nema deklaracije internetske ni storage dozvole. Jedina eksportirana aktivnost je launcher; `android:allowBackup=false`, `usesCleartextTraffic=false`. |
| Android backup/transfer | Provjereno statički | `data_extraction_rules.xml` isključuje lokalne `sharedpref`, datoteke i baze iz automatskog transfera. |
| Šifrirane kopije | Pregledano | `SudokuBackup.kt`: AES-256-GCM, nasumični salt/nonce, PBKDF2-HMAC-SHA256 (240k), ograničene veličine i provjera prije vraćanja. Izvoz je opt-in putem SAF-a. |
| Računanje na UI | Sanirano za spremanje | Sinkroni `commit()` uklonjen iz lifecycle/callback kontrolera. Backup šifriranje ostaje na `Dispatchers.IO`. |
| Race: export/restore | Sanirano u toku kontrolera | Uvoz zaključava stari kontroler prije čekanja. Ponovljeni `commit` ne može preskočiti prethodne zapise. |
| Osjetljivi sadržaj | Preostali rizik | Lokalna partija sadrži rješenje u aplikacijskom privatnom SharedPreferences. To nije sigurnosni token; ne izvoziti ga nešifrirano i ne zapisivati u logove. |
| Ovisnosti i CVE | Potrebna CI/provjera izvještaja | Bez nove runtime biblioteke. Sam pregled `build.gradle.kts` nije zamjena za provjeru svih tranzitivnih ovisnosti i aktualnih CVE-a. |
| Fizički uređaji / penetracijski test | Nepotvrđeno | Automatizirani testovi ne dokazuju ponašanje na svim OEM uređajima i Android verzijama. |

## P1: dead-code i kvaliteta

- U novom writeru nema neupotrebljivih pomoćnih ovisnosti, a stari
  `writeStats(editor)`, duplicirana JSON serializacija iz `saveGame()`
  i izravno `options.apply()` uklonjeni su iz kontrolera.
- Statička pretraga ne nalazi `TODO`, `android.util.Log`,
  `android.permission.INTERNET` ni `java.net` u indeksiranom kodu.
  Rezultat pretrage nije dokaz nepostojanja u svakoj binarnoj ovisnosti.
- Preostali širi audit poziva i neiskorištenih resursa mora se temeljiti
  na Android lint/R8 izgradnji te analizi rezultata, ne na uklanjanju
  funkcija samo zbog izostanka tekstualne reference (Compose/refleksija).

## P1: QA matrica i kriteriji za izdanje

1. `gradle :app:testDebugUnitTest :app:lintDebug :app:lintRelease`.
2. Android instrumentacijski `SudokuAsyncPersistenceTest`: usporeni zapis
   ne blokira UI, a redoslijed dvaju poteza i checkpoint je očuvan.
3. Postojeći `SudokuSessionSwitchSafetyTest`,
   `SudokuStatsDurabilityTest`, `SudokuBackupSafetyRegressionTest`
   i testovi oporavka moraju ostati zeleni.
4. Vizualni smoke na 11 emulatorskih profila prema postojećem workflowu.
   Vizualni UI nije diran; to **nije** dokaz potpune 1:1 podudarnosti s
   vlasnikovih deset slikovnih referenci.
5. `publish-release.yml` mora objaviti `v1.0.66` s APK, AAB i SHA256SUMS
   te potvrditi potpise i integritet. Bez publisher ključa jasno označiti
   `prerelease` i ne deklarirati Google Play-ready AAB.

**Otvoreno za daljnji razvoj:** stvarno mjerenje glavnih/malih zaslona
pixel-by-pixel prema referencama, audit svih transitive CVE-a i
sveobuhvatni dead-code/resources audit nakon uspješnog Android CI-a.
