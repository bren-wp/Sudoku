# SUDOKU by Brendigo — glavni razvojni prompt

> **Namjena:** ovaj tekst može se zalijepiti kao zadatak razvojnom AI agentu ili koristiti kao trajna definicija kvalitete projekta. Obveze su provjerljive: ne smije se napisati „gotovo”, „1:1”, „produkcijski spremno” ili „objavljeno” bez odgovarajućeg dokaza.

## Glavni zadatak

Nastavi razvoj postojeće **Android** aplikacije **SUDOKU by Brendigo** u repozitoriju **https://github.com/bren-wp/Sudoku**. Radi na **postojećem otvorenom PR-u #55** (`feature/sudoku-v148-settings-pixel-qa`) dok god je otvoren; nemoj otvarati paralelne PR-ove, resetirati arhitekturu, brisati postojeće značajke, mijenjati `applicationId` (`com.brendigo.sudoku`) niti gubiti postojeće podatke korisnika.

Središnji prioritet: **za svaki zaslon i svako stanje posebno postići vizualno što vjernije, mjerljivo poklapanje s izvornom referencom — pixel po pixel, cilj 1:1**. Sve kontrole moraju ostati stvarne i funkcionalne u Kotlin/Jetpack Compose UI-ju, ne slika cijelog zaslona s lažnim gumbima. Ne generiraj konceptne slike u razgovoru umjesto stvarnih zaslona aplikacije.

## Redoslijed zaslona

1. **Splash / pokretanje** — brand, lokalni logo, sustavni splash, edge-to-edge, pravilno učitavanje.
2. **Početna** — naslov, nastavljanje igre, kartice, dnevni niz, footer i navigacija.
3. **Nova igra** — 4×4, 6×6, 9×9, izbor razine i početak valjane nove partije.
4. **Igra** — Sudoku mreža, bilješke, kandidati, unos, brisanje, poništavanje, pomoć, pogreške, timer, pauza i dovršetak.
5. **Dnevni izazov** — stabilan izazov po lokalnom datumu, dnevni niz i očuvanje napretka.
6. **Statistika** — stvarni podaci, ispravna agregacija, prazna stanja i prikaz.
7. **Postignuća** — stvarni kriteriji otključavanja, ikone, kartice i prikaz svih nagrada.
8. **Postavke** — izgled, igra, zvuk/vibracija, jezik, informacije, privatnost, izvoz/uvoz šifrirane sigurnosne kopije.
9. **Branding** — adaptivna, monokromatska i Play ikona te wordmark.
10. **Sva dodatna stanja** — dijalozi, promjena orijentacije, manji ekrani, veći font, bez podataka, pogreške i potvrde.

Za svaki zaslon napravi stvarnu snimku iz Android emulatora, usporedi je s odgovarajućom izvornom referencom, ispravi geometriju, fontove, boje, sjene, rubove, ikone, gradijente, razmake i tipografiju. Nakon svake dovršene provjere napiši **što je promijenjeno, koji su testovi prošli, mjerljivu razliku i koji je idući zaslon po redu**. Ne proglašavaj 1:1 ako nije izmjereno.

## Vizualni QA i dokaz

- Izvorne reference zaslona su **941×1672 px**, a izvornici logotipa i ikone **1254×1254 px**. Referentni PNG-ovi nisu dio repozitorija; uzmi ih samo iz vlasnikovih originalnih datoteka.
- Usporedi determinističko stanje istog zaslona s istim datumom, sadržajem, jezikom, gustoćom i fontom. Mjere: puni RGB MAE, regijski MAE, postotak piksela unutar ±12/±24 kanala, overlay i diff.
- Koristi postojeći `scripts/pixel_compare.py`; ne skrivaj regresije maskama. U reportu razdvoji promjenjive podatke i stabilnu geometriju.
- Provjeri emulator **941×1672 @320dpi** i kompaktni **720×1280 @320dpi**; dodaj provjere 320dp širine, većeg fonta i landscape/tablet stanja gdje je primjenjivo.
- Ne mijenjaj arhitekturu radi lakšeg lažiranja snimaka. Fiksne slike referenci nikada nisu zamjena za Android UI.
- Ne mijenjaj originalne reference i ne tvrdi da su marketinške WebP snimke izvornici.

## Funkcionalnost i trajnost podataka

- Revidiraj generator i validator Sudoku zagonetki: dimenzije, broj rješenja (jedinstvenost gdje je zahtijevana), težinu, postojeće brojeve, valjane poteze i otkrivanje završetka.
- Osiguraj da svaka akcija igre radi, uključujući brzo višestruko tapkanje, rotaciju, povratak iz pozadine, obnovu procesa, prebacivanje dnevne/klasične igre i povratak kroz navigaciju.
- Provjeri spremanje igre, bilješki, postavki, statistike, postignuća, dnevnih izazova i šifriranih offline sigurnosnih kopija. Nema nenamjernih brisanja ni koruptivnih migracija; testiraj prekid tijekom pisanja i neispravne podatke.
- Nove opcije moraju imati stvarno ponašanje, ne prazne handlere, izmišljene vrijednosti ili „uskoro dostupno” bez zahtjeva.
- Sve što je moguće radi **offline**, bez nepotrebnih mrežnih servisa, praćenja ili novih dozvola.

## Android, kvaliteta koda i sigurnost

- Zadrži i testiraj postojeći skup dok je kompatibilan: **Android Gradle Plugin 9.5.0, Gradle 9.6.0, Kotlin Compose plugin 2.4.21, compile/target SDK 36, minSdk 26, Java 17**. Ovisnosti nadograđuj pojedinačno, tek nakon provjere kompatibilnosti i testova.
- Primijeni aktualne Android/Google Play zahtjeve: edge-to-edge, sistemske insets, API 36, pristupačnost (TalkBack, jasni opisi i dostupna područja dodira), font scale, svijetli/tamni prikaz ako je podržan i ispravan back-navigation.
- Napravi **full dead-code audit, security audit, dependency audit, lint audit, performance audit i resource audit**. Ukloni nepotrebne klase, duplikate, zastarjele resurse i dokumentirane rizike bez funkcionalnih regresija.
- Sigurnosne kopije validiraj prije uvoza; šifrirani podaci moraju ostati šifrirani i prijenos mora biti bez gubitka podataka. Ne stavljaj tajne, lozinke, signing key ili osobne podatke u repozitorij/logove.
- Ne dodavaj vanjske biblioteke, permisije, SDK-ove ili mrežne pozive bez jasnog opravdanja. Koristi lokalne grafičke resurse.

## Testiranje, CI i izdanje

- Za svaku promjenu osiguraj ciljane JUnit/Android instrumentacijske testove. Pokreni ili provjeri `gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease :app:bundleRelease`.
- Zadrži usklađene workflowe **Android CI**, **Android visual smoke** i **Refresh Sudoku marketing screenshots**, uz odvojeni workflow za **Publish Sudoku release**. Dokumentiraj sve padove, uzrok i potvrdu popravka.
- Stvarne snimke zaslona moraju nastati iz pokrenute aplikacije. Objavi ih kao CI artefakte i, kad je dozvoljeno, ažuriraj `docs/screenshots/`.
- GitHub release izdaj **tek nakon potvrđenih provjera i spajanja u main**, s ispravnim `versionCode`/`versionName`, changelogom, opisom izdanja, APK/AAB artefaktima i SHA-256 provjerama. **Google Play AAB mora biti potpisan valjanim upload ključem**; nepotpisani AAB ili debug APK nikad ne predstavljaj kao spreman za Play.
- Ažuriraj README, razvojne upute, QA dokumente i changelog. Ukloni zastarjele i lažne tvrdnje o 1:1 ili produkcijskoj spremnosti.

## Kriterij završetka

Zadatak nije završen dok: (1) sve kontrole svih osam zaslona i dodatnih stanja nisu funkcionalno provjerene; (2) vizualni izvještaj za sve dostupne originalne reference nije izveden na **stvarnim** screenshotovima, bez nedokazanog 1:1; (3) testovi, lint, build i Android emulator CI nisu zeleni; (4) oporavak spremljenih podataka i šifriranih sigurnosnih kopija nije potvrđen; (5) release nije stvarno objavljen sa stvarnim artefaktima, bez lažne potvrde potpisa; i (6) dokumentacija točno opisuje stvarno stanje. Ako nešto nije izvedivo, navedi konkretno ograničenje, dokaz i idući korak.
