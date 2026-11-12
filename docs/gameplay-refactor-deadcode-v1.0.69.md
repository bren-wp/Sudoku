# Sudoku by Brendigo — v1.0.69: gameplay, odvajanje datoteka i dead-code audit

**Datum:** 13. studenoga 2026.  
**Radna grana:** `refactor/sudoku-gameplay-screens-deadcode-v1069`  
**PR:** https://github.com/bren-wp/Sudoku/pull/75

## Što je stvarno promijenjeno

1. **Razdvajanje `Pages.kt`** (~155 KB, 3.040 redaka): javne Compose funkcije i njihove postojeće oznake za testiranje zadržane su. Pojedini zasloni izdvojeni su u `HomePage.kt`, `NewGamePage.kt`, `DailyPage.kt`, `StatsPage.kt`, `AchievementsPage.kt`, `SettingsPage.kt`, `SplashPage.kt`. U `Pages.kt` ostaju zajednički `PageContent`, `SectionTitle`, `SolidAction` i kalendarska pomoćna funkcija. Pomoćni elementi imaju privatni opseg u pripadajućem zaslonu.
2. **Razdvajanje `GamePage.kt`** (~35 KB): tok gameplaya i dijalozi ostaju u `GamePage.kt`, prikaz Sudoku mreže i bilješki u `GameBoard.kt`, a numerička tipkovnica i gumbi u `GameControls.kt`. Imena, stilizacija, oznake za UI testove i funkcionalne povratne funkcije zadržani su. Dvije funkcije korištene iz drugih Kotlin datoteka imaju `internal` vidljivost, umjesto nepotrebno široke javne.
3. **Modeli izvan kontrolera:** `Screen`, `Options`, `Completed`, `Statistics` i `Session` premješteni su u `data/SudokuModels.kt`; naziv paketa i postojeća javna API površina nisu promijenjeni.
4. **Ispravak gameplaya:** `SudokuController.select()` sada sprema stvarnu promjenu odabranog polja asinkronim redom za disk, ali izbjegava novo spremanje pri odabiru istog polja. Onemogućuje interakciju tijekom pauze i nakon završetka partije kao i ranije. Nov `SudokuSelectionPersistenceTest` testira preživljavanje odabira kroz potpuno novi kontroler i zabranu promjene tijekom pauze.
5. **Čitljivost i uski uređaji:** veličina prikazanih znamenki mreže ograničava se raspoloživom širinom ćelije i Android skalom fonta. Polaznih 25/29/39 sp ostaje na referentnoj veličini kada ima dovoljno mjesta. Pauzirana ploča ne izlaže klikabilne ćelije.

## Dead-code audit — granice zaključka

Statički pregledani su **svih 31 Kotlin izvornih datoteka u `app/src/main`**, uključujući novonastale datoteke. Provjereni su uvozi tipova, funkcija i Compose proširenja te privatne deklaracije s jednim leksičkim pojavljivanjem. Za `getValue` i `setValue` posebno je provjeren Kotlin `by` delegacijski obrazac. **Nema potvrđenih novih nekorištenih importova koje bi trebalo ukloniti**; lažni kandidat `BoxScope` je primatelj proširene funkcije, nije mrtvi kod. Velika monolitna UI datoteka uklonjena je razdvajanjem, bez brisanja resursa i funkcija koje mogu biti dostupne samo kroz testove ili Compose.

Ovo **nije** dokaz da nema mrtvog koda u cijeloj aplikaciji. Cjelovit audit zahtijeva Android lint, R8 izvještaje, resursni shrinker, pokrivenost testovima, pregled refleksijskih referenci i dinamičkih scenarija. Ne smije se uklanjati nekorišteni resurs isključivo prema broju tekstualnih pojavljivanja.

## Provjere koje moraju proći prije spajanja

- Android CI: Gradle kompilacija, unit testovi, Android lint, pixel-QA regresija i izrada APK/AAB.
- Android visual smoke: 11 stvarnih emulatorskih profila, posebno `reference`, `narrow`, `compact`, `tiny` i tableti.
- Instrumentacija: nova regresija za selection persistence i postojeći testovi asinkronog spremanja, Undo oporavka, dnevnih sesija, sigurnosnih kopija i zaslona.
- Izdanje: `versionName=1.0.69`, `versionCode=70`; GitHub release se verificira tek nakon završetka workflowa. Bez vlasničkog potpisnog ključa APK je testni, a AAB nepotpisan.

## Granice 1:1 i sigurnosne provjere

Na referentnoj dimenziji cilj je sačuvati postojeću geometriju i tipografiju; razdvajanje izvornih datoteka samo po sebi ne mijenja rendering. **Potpuna pixel-by-pixel jednakost s korisnikovim dizajnerskim izvornicima nije verificirana.** Starije RGB MAE metrike iz `docs/pixel-qa-v1.0.64.md` ne pokazuju 0 odstupanja, a postojeći screenshotovi u README-u ostaju stvarni prikazi aplikacije, ne generirane slike. Potrebna je odvojena usporedba referentnih PNG-ova i novih emulatorskih PNG-ova pri istom stanju partije.

Nije dodana internetska dozvola, oglasni SDK, nova Gradle ovisnost niti promjena `applicationId`-a. Ovim commitima nije proveden puni CVE/SBOM audit, vanjski sigurnosni pregled niti testiranje na svim fizičkim Android uređajima.
