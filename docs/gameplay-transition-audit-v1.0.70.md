# Sudoku v1.0.70 — serializacija gameplay prijelaza i oporavak iz memorije

**Datum:** 13. 11. 2026.  
**Grana:** `fix/sudoku-session-transition-guard-v1070`  
**Platforma:** Android 8+ (API 26), offline Jetpack Compose, BRENDIGO.

## Nalaz i ispravak

### 1. Dva zahtjeva za početak partije tijekom sporog spremanja

Prethodni `SudokuController.startNew()` postavljao je `isGenerating = true` *nakon* prvog čekanja na trajni checkpoint za dnevnu partiju. Igrač je mogao brzo ponoviti akciju ili aktivirati drugi zahtjev dok je prva korutina još čekala disk. To je stvaralo utrku između učitavanja stare partije, postavljanja aktivne partije i ažuriranja statistike.

**Ispravak:** zajednički `isGenerating` lock postavlja se prije *svake* potencijalne suspend točke. Zaštićeni su i `startNew` i `resumeRegularGame`; `try/finally` vraća stanje nakon uspjeha, pogreške ili otkaza. Pokretanje partije tijekom povrata sigurnosne kopije izričito se odbija. Drugi suvišni trajni checkpoint pri povratku u postojeći dnevni izazov uklonjen je jer između prvog checkpointa i sinkronog čitanja nije bilo promjene stanja.

### 2. Stari `regular_game` na disku može nadjačati noviji gameplay

Automatski zapisi izvršavaju se na `Dispatchers.IO`; glavna nit nastavlja raditi. `resumeRegularGame()` mogao je učitati stariji `regular_game` sa diska iako je aktivna obična partija u memoriji već sadržavala najnovije poteze. Sličan rizik postojali su u funkciji `hasSavedRegularGame()`: nakon dovršetka partije stari checkpoint mogao je kratko nuditi nastavak već dovršene igre.

**Ispravak:** kada je aktivna redovna partija, nastavak zadržava istu in-memory `Session` i Undo stanje, umjesto vraćanja starog diska. Dovršena ili neuspjela obična partija ne može se ponovno ponuditi kao aktivna dok se njen zapis dovršava. Kada se prelazi s dnevne na običnu partiju, i dalje se čeka trajni checkpoint dnevne sesije prije učitavanja drugog spremanja.

## Regresijski testovi

Dodana je klasa `SudokuTransitionSerializationTest` s četiri stvarna Android instrumentacijska scenarija pomoću `SharedPreferences` proxyja, blokiranog `commit()` i odvojenih testnih postavki:

1. Dnevni izazov čeka disk; drugi start i Resume ne mogu zamijeniti prvi prijelaz niti poduplati statistiku.
2. Nastavljanje obične partije s aktivne dnevne partije čeka disk; konkurentna nova partija ne može se ubaciti.
3. Aktivni redovni gameplay i posljednji broj ostaju sačuvani i kad je diskovna snimka starija.
4. Dovršena partija ne može ponovno postati igra u tijeku iz zastarjele snimke diska.

Testovi ne zaobilaze čekanje na disk; nakon otpuštanja kontrolirane blokade provjeravaju finalni `flush()` i obnovu preko novog `SudokuController` objekta.

## UI, stabilnost, dead-code i sigurnosni opseg

- Nisu mijenjani `GamePage`, `GameBoard`, ostali zasloni, boje, 3D resursi niti Compose `testTag` oznake. Prethodne reference i stvarni marketinški screenshotovi ostaju netaknuti.
- Nisu dodane nove ovisnosti, internetske dozvole, oglasi niti analitički SDK-ovi.
- Uklonjena je jedna suvišna operacija spremanja. Čuvaju se svi javni pozivi, izvorni format SharedPreferences/JSON, AES-GCM arhive, povijest i Undo.
- Ciljani dead-code audit nije zamjena za cjelovit R8 reachable-method i CVE/SBOM audit svih tranzitivnih ovisnosti. Nisam brisao potencijalno upotrebljive Compose elemente ili resurse prema samoj tekstualnoj pretrazi.
- **Pixel-perfect 1:1 nije potvrđen ovim popravkom:** cilj je regresijska sigurnost postojećeg izgleda; postojeći Android CI i 11 emulatorskih profila moraju proći prije spajanja.

## Isporuka

`versionName=1.0.70`, `versionCode=71`. Novo izdanje se provjerava preko uspješnog publish workflowa, stvarnog APK/AAB sadržaja i SHA256SUMS. Bez vlasničkog potpisa AAB mora ostati **unsigned**, a GitHub izdanje označeno kao **prerelease**. Nije primjereno tvrditi potpunu produkcijsku certifikaciju bez vanjskog security audita i fizičkih uređaja.
