# Sudoku v1.0.75 — mjerljivi pregled dekorativnog pejzaža i svih deset originala

**Status:** PR u razvoju; ne označavati 1:1 ni objaviti bez dokaza.

## Osnova

- Izvori: 10 vlasnikovih privatnih PNG-ova (8 × 941×1672, 2 × 1254×1254), SHA-256 provjera u `scripts/final_ten_reference_qa.py`. Datoteke se ne prenose u javni repozitorij.
- Aktualni `main`: `a17799a19d56f94d0e2d72c8dacdc1eade75927b` (v1.0.74, testni prerelease).
- Prije izmjene: stvarne Android snimke prethodnog PR-a #81 iz `sudoku-emulator-screen-references`, Actions run #43515302886, SHA `33e10b8`; produkcijski Compose UI jednak je v1.0.74.
- Razlika izvornik ↔ stvarna snimka **nije samo geometrijska**: referentne slike sadrže različit datum, broj završenih igara, napredak, odabir polja i Sudoku zagonetku. Statistika i Postignuća moraju ostati vezani uz stvarne korisničke podatke.

## Intervencija, stvarno mjerenje i uklonjena regresija

**P1 vizualni — dekorativni donji sloj:** pomak početka neinteraktivnog `scenery-reference.webp` sloja s `0.730h` na `0.790h` isproban je na Home, Daily, Statistics i Achievements. Prvi stvarni screenshotovi iz [Android visual smoke #43639001341](https://github.com/bren-wp/Sudoku/actions/runs/43639001341) na SHA `9865735` potvrđuju bolji donji ROI za Daily (−0,438641 MAE), Statistics (−0,538098) i Achievements (−0,658204).

**Home regresija odbijena:** isti pomak pogoršao je donji ROI Naslovnice za +0,468180 MAE, premda je ukupni MAE nešto pao. Zato kod na SHA `4f7a560` eksplicitno vraća **Home na 0.730h**, a zadržava **Daily/Statistics/Achievements na 0.790h**. Točno stanje nakon te korekcije mora se potvrditi novim screenshotom aktualnog PR heada.

Splash, Gameplay, New Game i Settings imaju svoje postojeće prijelaze i nisu mijenjani; svi gumbi, kartice, tipografija, 3D bedževi, HUD, spremanje i statistika ostaju stvarne Compose/logičke komponente. 

## Stvarna Before / After usporedba prvog kandidata

Svi RGB MAE brojevi su u intenzitetu 0–255. Ovo je **izravna usporedba stvarnih Android PNG-ova** (baseline SHA `33e10b8`, kandidat SHA `9865735`) protiv istih privatnih vlasnikovih originala. Originali su SHA-256 verificirani, bez interpolacije. Brojevi za 986... nisu automatski rezultati **naknadnog** korekcijskog SHA `4f7a560`.

| Zaslon | Prije puni MAE | Prvi kandidat puni MAE | Promjena donjeg ROI | 1:1 |
| --- | ---: | ---: | ---: | --- |
| Home / Resume | 23.903772 | 23.816400 | **+0.468180 — regresija, vraćeno u kodu** | ne |
| New Game | 22.622370 | 22.622370 | +0.000000 | ne |
| Gameplay | 20.361789 | 20.167486 | +0.000000 | ne |
| Settings | 19.421990 | 19.422299 | +0.001468 | ne |
| Daily | 24.743339 | 24.682672 | **−0.438641** | ne |
| Statistics | 26.541994 | 26.300123 | **−0.538098** | ne |
| Achievements | 29.749992 | 29.514219 | **−0.658204** | ne |
| Splash | 10.370861 | 10.370861 | +0.000000 | ne |
| Wordmark | 18.833920 | 18.833920 | +0.000000 | ne |
| Launcher | 16.174683 | 16.174683 | +0.000000 | ne |

**Strogi rezultat: 0/10.** Razlike na ekranima bez kôdnih izmjena (npr. gameplay) nastaju iz različitog dinamičkog stanja; nisu dokaz poboljšanja aplikacijskog layouta. Nakon vraćanja Home u prethodno stanje treba ponoviti mjerenje s najnovijim SHA.

## Druga stvarna provjera: Home rollback i izolirane fixture snimke

Referentni emulator [#43641921157](https://github.com/bren-wp/Sudoku/actions/runs/43641921157), SHA `237c7b84`, uspješno je izvezao sve standardne snimke i tri nove testne snimke (`fixture-03-game.png`, `fixture-06-statistics.png`, `fixture-07-achievements.png`) u artefaktu `14132438635`. To su stvarni `UiAutomation.takeScreenshot()` PNG-ovi; vlasnički originali ostali su privatni i autentificirani SHA-256 otiscima.

- **Home**: povratak krajolika uspio je — globalni RGB MAE ponovno je **23.903772**, potpuno isti kao izvorna snimka prije pokušaja. Donji ROI također se vratio na izvornu geometriju.
- **Daily**: ukupni MAE **24.682672**, poboljšanje prethodne dekorativne korekcije ostaje.
- **Achievements**: ukupni MAE **29.514219**, poboljšanje prethodne dekorativne korekcije ostaje.
- **Statistics**: globalni MAE bio je 26.310780 dok je nakratko isprobana nova dubinska linija; **nije** proglašen boljim jer su pojedinačni gamepad/štoperica ROI-jevi regresirali (+0.651631, +0.676698 i +0.412566). Naknadnim commitom `6c016994` dodatna linija vraćena je na prethodni, bolji single-vector prikaz. Čeka se autentična provjera **tog** commita.
- **Gameplay**: ukupni MAE na 237c7b84 iznosi 20.136121 (promjenjiva zagonetka); to nije dokaz geometrijskog poboljšanja.
- **Wordmark/Launcher**: i dalje 18.833920 i 16.174683 MAE; bez lažne tvrdnje da je logo ili ikona 1:1.

Sva **deset** originala na SHA 237c7b84 i dalje su **0/10** strogo RGBA/piksel točna. Te vrijednosti ne dokazuju rezultat nakon naknadnog povrata statističkih vektora. Potrebna je ponovna emulatorska provjera trenutnog PR heada i svih 11 profila.

## Reproducibilna stroga provjera

`scripts/compare_visual_iteration.py` zahtijeva svih deset originala i oba skupa screenshotova, njihove stvarne dimenzije i svih deset izvornik SHA-256. Uz globalni RGB MAE bilježi MAE za stabilniji donji ROI `y ∈ [0.79h, h]`, točne RGB **i RGBA** piksele i SHA-256 svih snimki. Razlika MAE ne može nadomjestiti `exact_1_to_1`. Komanda:

```bash
python scripts/compare_visual_iteration.py \
  --reference-dir /private/owner-originals \
  --before-dir /private/v1074-android-real \
  --after-dir /private/v1075-android-real \
  --output-dir /private/qa-v1075 \
  --home-resume
```

Na svim drugim uređajima ostaju zasebni Compose i funkcionalni testovi. Brojke iz 941×1672 slike ne primjenjuju se na 240dp, tablet ili landscape.

## Testovi i release

**Referentna stanja za statičnu grafiku:** dodatni test u `SudokuVisualSmokeTest` reproducira stvarne Compose stranice Statistika, Postignuća i Gameplay s konzistentnim naprednim sintetičkim podacima kroz odvojeni testni `SharedPreferences`. Odvojene snimke `fixture-06-statistics.png`, `fixture-07-achievements.png`, `fixture-03-game.png` nikad ne zamjenjuju 10 live-state referenci; zatim se testni zapis briše. Pojedinačni 3D badge ROI-jevi i granice native gumba dodatno su mjerljive. [Metoda i ograničenja](isolated-reference-fixture-qa-v1.0.75.md).

- [ ] Tri stvarna fixture PNG-a i dimenzije 941×1672 potvrđeni na referentnom emulatoru
- [ ] Test APK kompiliran u Android CI (ne samo :app:testDebugUnitTest)
- [ ] Python kontrolni testovi (RGB/RGBA alfa, nepripadajuće dimenzije, ROI) na head SHA
- [ ] Android JUnit, lint, debug/release APK i AAB, release runtime smoke
- [ ] CycloneDX, online OSV, R8, compiled manifest i security audit
- [ ] 11/11 Android emulator poslova
- [ ] Stvarni screenshotovi s točnog commita, ponovna globalna/regionalna MAE provjera bez skaliranja i bez izmišljenog statističkog napretka
- [ ] Verzija `1.0.75`, code `76`: **nije merge, nije release** dok se ne ispune gateovi. Bez izdavačkog ključa: samo testni prerelease.
