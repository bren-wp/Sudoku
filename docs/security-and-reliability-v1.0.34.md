# Sudoku v1.0.34 — sigurnost, privatnost i stabilnost

## Opseg revizije

Pregledan je postojeći Android manifest, backup pravila, sigurnosno relevantni
dio `SudokuController`, konfiguracija Gradlea, pravila privatnosti, testovi
i workflowi za izgradnju. Ovo je **fokusirani audit**; nije potvrda da je
cijela aplikacija bez sigurnosnih propusta ili da je iscrpno uklonjen sav
mrtvi kod.

## Nalazi i konkretne izmjene

| Područje | Nalaz u v1.0.33 | Korekcija za v1.0.34 |
|---|---|---|
| Spremanje partije | Ako se zajednički ključ `game` ne može pročitati, valjani `regular_game` ili `daily_game` nisu se koristili pri pokretanju | Siguran odabir valjanog nedovršenog zapisa i pripadajućeg undo ključa |
| Android lifecycle | `onStop` je koristio asinkroni `SharedPreferences.apply()` | Sada koristi sinkroni `commit()` na izlasku, uz normalni `apply()` tijekom igre |
| Pokretanje nove igre | Posljednja odabrana ćelija i sekunde mogle su biti nespremljene pri zamjeni aktivne partije | Trenutačna partija se prije zamjene sinkrono snima |
| Undo podaci | Neočekivani bitovi u spremljenim bilješkama mogli su preživjeti povrat poteza | Undo bilješke ograničene su na dopuštene brojeve |
| Mrežni promet | Aplikacija nema `INTERNET` dozvolu, ali nije izričito zabranila cleartext transport | `usesCleartextTraffic=false` kao dodatna zaštita |
| Backup | `allowBackup=false` i isključenje `sharedpref` | Eksplicitna isključenja `file`, `database`, `sharedpref` i `external` za cloud i device transfer |
| Izjava o privatnosti | Nije dovoljno naglašena nemogućnost oporavka bez sinkronizacije | Jasno dokumentirano da promjena/kvar uređaja ili deinstalacija mogu uzrokovati trajan gubitak napretka |
| README snimke | Workflow je bio vezan uz staru razvojnu granu i proizvodio umanjene slike | Snimanje stvarnih 941×1672 UI ekrana i pregled promjena u novom PR-u |
| Splash | Mjesec, planine i jezero dijelili su pozicije sa sedam interaktivnih ekrana | Zasebni položaji pejzaža samo za splash, bez statičnog screenshota |

## Provjere prije spajanja

- `gradle :app:testDebugUnitTest` (uključujući `SavedGameRecoveryTest`)
- `gradle :app:lintDebug`
- Izgradnja debug APK i release AAB (potpis samo preko zaštićenih tajni)
- Android instrumentacijski testovi na 941×1672 i 720×1280
- Instrumentacijska provjera manifest dozvola, backupa i cleartext zastavice
- Stvarne WebP snimke svih sedam navigacijskih ekrana u README
- Provjera da PR nije spojen bez zelenih provjera

## Preostala ograničenja i rizici

1. **Nema cloud sinkronizacije ili lokalne funkcije izvoza korisničkog napretka.**
   Androidov `allowBackup=false` čuva privatnost, ali bez alternativne
   mogućnosti kopiranja podataka ne može se jamčiti oporavak nakon gubitka
   uređaja. Takvu funkciju treba projektirati zasebno, uz eksplicitan izbor
   korisnika i testove kompatibilnosti.
2. **Potpuna pixel-perfect 1:1 jednakost nije potvrđena.** Bitmap scena iz
   izvornog koncepta znatno je detaljnija od proceduralne Compose pozadine.
   Nemoj koristiti cijele zaslonske reference kao interaktivnu podlogu.
3. **Nema jamstva odsutnosti padova.** CI i dva emulatora provjeravaju određene
   putanje, ne svaki uređaj, Android verziju ili uvjet nedostatka memorije.
4. **Nema dokaza iscrpnog dead-code audita.** Zadržane su funkcije i resursi
   čije uklanjanje ne bi bilo dokazano sigurno.
5. **Android SharedPreferences nije zasebno enkriptiran.** Podaci se čuvaju u
   sandboxu aplikacije, a korisnik može zaštititi uređaj zaključavanjem; ne
   predstavljati lokalnu pohranu kao end-to-end šifriranu.
6. **AAB bez izdavačkog potpisa nije paket spreman za Google Play.**

## Vizualna provjera

Primarne reference su deset korisničkih PNG datoteka (sedam interaktivnih
ekrana, splash, wordmark, ikona). Kao provjerljive stvarne snimke služe
`docs/screenshots/*.webp` iz emulatora, a mjerenja se izvode prema
`docs/pixel-qa.md`. Splash nema još automatiziran usporediv screenshot, pa
njegove nove pozicije zahtijevaju zasebnu vizualnu provjeru.
