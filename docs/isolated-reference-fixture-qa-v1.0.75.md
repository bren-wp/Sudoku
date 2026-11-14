# SUDOKU v1.0.75 — izolirano snimanje referentnih stanja i provjera 3D grafike

**Status:** PR #82, razvojna grana. Ovaj test je nova stvarna instrumentacija; nije zamjena za svih 10 privatnih vlasnikovih originala niti tvrdnja o 1:1.

## Zašto postoji izolirana reprodukcija

Vlasničke reference Statistike/Postignuća ilustriraju već aktivnog igrača, dok običan `connectedDebugAndroidTest` pokreće čistu instalaciju. Usporedba 124 odigrane igre s praznim stanjem 1/0 nije čista usporedba grafike. Gameplay dodatno mijenja zagonetku, trajanje i odabrano polje. Zato su **statični vizualni elementi** (ikone, 3D slojevi, tipografija, položaj i graničnici) izdvojeni u dodatne ROI-je, a postavljen je zaseban test za ponovljivo prikazivanje realnog Compose sučelja.

## Implementacija: samo instrumentacija, bez produkcijskih promjena statistike

`SudokuVisualSmokeTest.captureReferenceLikeStatsAchievementsAndGameplayWithoutPlayerData` radi ovim redom:

1. Kreira privatni testni `ContextWrapper` čije `getSharedPreferences()` vraća novu prefiksiranu testnu datoteku, **nikad** normalni `sudoku_state_v1` profil.
2. U testni kontekst upisuje konzistentnu, sintetičku statistiku: 124 pokrenute, 114 dovršenih partija, 02:18 najbolje vrijeme, 18:42:10 ukupno aktivno igranje, niz 7 dana, povijest 4 realistična zapisa, zajedno s napretkom razina i veličina mreže.
3. Sprema valjanu poznatu, jedinstveno rješivu 9×9 zagonetku kao `game` i `regular_game`: odabrano polje 40, 08:42, jedna pogreška, jedan savjet. `SudokuController` je stvarno parsira i validira kao svaku legitimnu sesiju.
4. Stvarni `StatsPage`, `AchievementsPage`, `GamePage`, `SudokuTheme` i `ScenicBackdrop` renderiraju testno stanje. Test provjerava stvarni sadržaj brojača, postoji li 3D bedž te jesu li sve četiri vektorske ikone unutar pripadnih kartica.
5. Iz Android `UiAutomation.takeScreenshot()`, bez rasterizacije dijelova aplikacije u mock, snima tri dodatna PNG-a: `fixture-06-statistics.png`, `fixture-07-achievements.png`, `fixture-03-game.png`.
6. U `finally` briše prefiksiranu testnu datoteku i testno stanje. Nikakva operacija prodajnog profila, mrežnog servisa, korisničkog računa ili vjernih podataka nije potrebna.

Datum `lastDaily` u testu postavlja se na lokalni datum emulatora kako bi pravi kontroler izračunao aktivni niz 7; povijesni datumi četiri završenih testnih igara fiksni su. Nije dopušteno kopirati ilustrativno stanje u produkcijske podatke, prikazivati screenshot kao klikabilnu aplikaciju ili zamijeniti datum korisnika starim referentnim datumom.

**Reference nisu nužno doslovno modelno konzistentne:** ilustrativnih „420 bodova / 8 otključanih” nije moguće nametnuti kroz realnu scoring logiku uz istovremeno 124 odigrane i 114 dovršenih partija bez krivotvorenja izračuna. Test stoga provjerava geometriju i grafiku, ne obećava taj nepostojeći fizički sadržaj.

## Dodatna evidencija i ulaz u CI

- Referentni emulator 941×1672 sada izvodi test izoliranog stanja u `--reference-capture` uz **postojeća** 01–11 screenshot snimanja i zasebnu sesiju s karticom Nastavi.
- `export-emulator-screenshots.sh` odbija nedostajuće, nepodobne ili krive dimenzije sva tri posebna PNG-a.
- Postojeći tablet shardovi (4 dijela) pokrivaju test kao dio svih instrumentiranih testova, osim kada se izričito promijeni podjela.
- `statistics_pixel_qa.py`: dodatni statični ROI-jevi gamepada, dvaju štoperica i bedža niza.
- `achievements_pixel_qa.py`: dodatni ROI-jevi 3D trofeja, kalendara, mozga i munje.
- `gameplay_pixel_qa.py`: ROI-jevi stvarnih native Back/Pause/Settings kontrola, ne dinamičkog Sudoku polja.
- `StatsPage`: potvrđene su mjerljive zasebne ikone svih četiriju kartica. Pokušaj dodavanja drugog sub-2dp 3D obruba **povučen** je nakon stvarnog referentnog snimanja: RGB MAE povećao se za +0,651631 (gamepad), +0,676698 (najbolja štoperica) i +0,412566 (ukupna štoperica). Vraćen je kvalitetniji prethodni single-vector prikaz; testne oznake i geometrijski testovi zadržani.
- `audit_android_release.py`: zatvoren `usage.txt` stream u R8 sigurnosnom auditu; uklonjen potencijalni `ResourceWarning`.
- Android CI provjerava i Bash sintaksu instrumentacijskog pokretanja i izvoza screenshotova.

## Nužni kriteriji zatvaranja

- [ ] Build, JUnit, Android lint, sigurnosne provjere i statični testovi na **točnom završnom SHA** (prije posljednjeg povrata ikona prošli su na `237c7b8`, sada čeka novi SHA)
- [ ] **11/11** emulatorskih poslova završilo s `success` (ne `cancelled`, `skipped` ili `queued`)
- [x] Tri fixture PNG-a dokazana na stvarnom referentnom emulatoru [run #43641921157](https://github.com/bren-wp/Sudoku/actions/runs/43641921157), artifact `14132438635`, 941×1672; **naknadna** korekcija ikona zahtijeva ponovnu provjeru novog SHA
- [ ] Izmjeriti prije/poslije lokalne ROI-jeve 3D ikona i vidjeti je li novi vektorski sloj stvarno bolji; povući ako regresira
- [ ] Ponoviti SHA-256 autentificiranu usporedbu svih 10 vlasničkih originala za trenutačni PR head, bez skaliranja i bez izmišljanja korisničkog stanja
- [ ] Dovršiti ostale razlike 3D ilustracija, obruba, tipografije, ekrana, modala, logotipa i launchera
- [ ] **Bez spajanja i bez v1.0.75 releasea** dok sve obvezne provjere nisu potvrđene; bez pravog izdavačkog ključa samo testni prerelease
