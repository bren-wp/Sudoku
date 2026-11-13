# SUDOKU by BRENDIGO — glavni razvojni prompt (aktualizirano 13.11.2026.)

> Ovaj prompt je operativna specifikacija razvoja postojeće Android aplikacije, a ne potvrda da su svi zadaci već izvršeni. Cilj je strogi 1:1 prikaz u unaprijed definiranim referentnim stanjima i stabilan, pristupačan, siguran i u cijelosti funkcionalan proizvod. Nulta odstupanja nikad se ne smiju izmišljati.

## Uloga, repozitorij i način rada

Radi kao senior Android/Kotlin/Jetpack Compose inženjer, inženjer za testiranje, sigurnost i dizajnersku usklađenost. Repozitorij: https://github.com/bren-wp/Sudoku ; aplikacija: SUDOKU by BRENDIGO ; web: https://brendigo.com ; applicationId: com.brendigo.sudoku.

1. Prije izmjena pročitaj aktualni main, otvorene PR-ove, stvarne GitHub Actions rezultate, issue backlog, README, CHANGELOG, manifest, Gradle, sve Kotlin datoteke, resurse, skripte i dokumentaciju.
2. Obavezno radi na zasebnoj grani i otvori **pull request prema main**. Nastavi u istoj razvojnoj grani i PR-u za isti opseg; ne dupliciraj nedovršene UI promjene iz paralelnih PR-ova. Posebno provjeri otvoreni PR #79 (v1.0.73), rebase ili integracijski redoslijed i konflikte. Ne mijenjaj main izravno i ne spajaj PR bez testova.
3. Sačuvaj sve postojeće značajke, package/application ID, lokalan korisnički napredak, datoteke spremljenih partija, statistiku, postignuća, save/backup formate i korisničke postavke. Svaku migraciju učini unatrag kompatibilnom i testiraj je.
4. Na početku PR-a prikaži inventar: potvrđeni problem, putanja/datoteka, dokaz ili reprodukcija, prioritet (P0/P1/P2), predloženi zahvat, test, status i rizik regresije.
5. Ne prijavljuj neprovedene testove kao uspješne, ni buduće izdanje kao već objavljeno. Bez generiranja konceptnih slika u razgovoru: koristi izvorne referentne PNG-ove i stvarne screenshotove iz Android emulatora.

## 1 — Stroga 1:1 vizualna usklađenost

Izvorni dizajnerski originali: osam 941 × 1672 px PNG referenci (01 Naslovnica s aktivnim Nastavi, 02 Nova igra, 03 Gameplay, 04 Postavke, 05 Dnevni izazov, 06 Statistika, 07 Postignuća, 08 Splash), 1254 × 1254 px PNG wordmark (09) i launcher ikona (10). Privatne originale ne objavljuj u javnom repozitoriju. Provjeri njihov SHA-256 protiv scripts/final_ten_reference_qa.py; ne zamjenjuj ih WebP snimkama iz README-ja niti rasteriziranom imitacijom funkcionalnog ekrana.

**Stvarni referentni output:** pokreni odgovarajući commit na Android emulatoru, 941 × 1672 px / 320 dpi, hrvatski lokalitet, usklađeni font scale, sistemska navigacija, tema, osvjetljenje i animacije. Za branding izvozi stvarni adaptivni, okrugli i monokromatski prikaz na 1254 × 1254. Uskladi zagonetku, odabrano polje, datum, statistiku, stanje postignuća, Undo, timer i prazna stanja. Za screenshot QA koristi isključivo izolirane instrumentacijske podatke koji ne ulaze u produkcijsku aplikaciju i ne mijenjaju stvarne korisničke podatke.

**Za svaki zaslon i podstanje** izmjeri punu RGB/RGBA preciznu jednakost piksela (bez zaokruživanja), RGB MAE 0–255, α MAE gdje je primjenjivo, regionalne MAE vrijednosti, točno jednake piksele, vizualni overlay/diff, geometriju, tipografiju, 3D ikonografiju, gradijente, sjene, boje, radijuse, razmake, HUD, hit target i kontrast. Odvoji fiksni UI od dinamičkog sadržaja, ali uvijek sačuvaj globalni unmasked izvještaj. Ne skaliraj originalne PNG-ove radi uljepšavanja rezultata. CI PASS bez svih izvornih PNG-ova nije potvrda 1:1.

**Referentni redoslijed:** prioritet prema zadnjem pouzdanom izmjerenom MAE za stvarne ekrane: Postignuća, Statistika, Dnevni izazov, Naslovnica, Nova igra, Gameplay, Postavke, Splash; zasebno launcher/wordmark. Ovo je redoslijed za dijagnostiku, a ne lažni postotak odstupanja. Nakon svake iteracije izmjeri novi SHA protiv istih izvornika; usporedi prije/poslije i odbaci regresije bez funkcionalnog opravdanja.

**Svi vidljivi dijelovi moraju biti stvarne Compose kontrole**, pristupačni, responzivni i klikabilni. Nikad ne postavljaj screenshot preko gumba ili HUD-a; dekorativni lokalni bitmap/sprite dopušten je samo za stvarne ilustrativne elemente.

## 2 — Ekrani, radna stanja i funkcionalni testovi

- **Splash / inicijalizacija:** identitet BRENDIGO, stvarni lokalni logo, Android 12+ splash API i legacy start, font, byline, pravilan prelazak na Home, bez bijelog bljeska i preklapanja sa sustavnim barovima; portrait, landscape, cold/warm start, activity recreation.
- **Naslovnica:** precizne kartice, 3D obrubi, raspored, tipografija, status spremljene partije, ispravna tipka Nastavi i linkovi Nova igra/Dnevni izazov/Statistika/Postignuća/Postavke; svaka navigacija testirana.
- **Nova igra:** 4×4/6×6/9×9, šest težina, valjana jedinstvena rješenja, rad selektora, odabir razine, zabrana dvostrukog paralelnog starta, pravilan nastanak igre nakon povratka i promjene orijentacije. Razlikovati *gustoću zadanih brojeva* od stvarne logičke težine koju solver mjeri.
- **Gameplay:** 9×9, 6×6 i 4×4; kvadratna mreža, numerička tipkovnica, označavanje retka/stupca/okvira, brojke i pencil marks, automatske bilješke, undo, erase, hint, pogreške/3 života, pauza, timer, dovršetak i povratak. Testirati brze dodire, rubne ćelije, uzastopni Undo, broj već u ćeliji, rješavanje posljednjim unosom i hintom, 3. pogrešku, rotaciju, odlazak u Settings i povratak.
- **Dnevni izazov:** deterministički puzzle po datumu, datum i zona, midnight/DST, utrka generiranja preko ponoći, nastavak nedovršene dnevne igre, odvojeni normal/daily saveovi, samo jedna nagrada po danu, streak, kalendar i promjena datuma tokom igre.
- **Statistika:** played, finished, vremena, best, postotci, six-level breakdown, historie, progress, empty state, dodatni prikazi; nikad izmišljene brojke. Osigurati integritet brojača nakon crasha, restorea i zamjene game sessiona.
- **Postignuća:** provjeriti svih 24 stvarnih uvjeta, otključavanje, napredovanje, redoslijed, ikonice, 3D kartice, dijalozi, navigaciju, tipografiju i responsivni prikaz; bez lažnog napretka.
- **Postavke:** stvarni efekt svake opcije, spremanje, čuvanje stanja, vraćanje iz igre, zvuk/vibracija, reduced motion, opis pristupačnosti, privatnost i šifrirani izvoz/uvoz uz pregled prije zamjene.
- **Launcher i branding:** 1:1 ikona, adaptivni maskable safe zone, crop na različitim launchers, monokromatska tematizirana ikona Android 13+, round icon, bez bijelih rubova i artefakata; stvarni Play grafički materijali.
- **Dodatna stanja:** bez podataka, error, paused, game over, completed, achievement detail, potvrde, SAF picker, backup password/error, TalkBack focus, hardverska tipkovnica, rotacija i različiti sistemski fontovi.

## 3 — Responsivnost i pristupačnost

Provjeri stvarne CI profile: 941×1672/320dpi referenca, 720×1280, 540×1080, 1280×720 landscape, četiri 1200×1920/240dpi tabletna shard posla, tablet capture, 480×800 i 800×1440. Na svim ekranima provjeri najmanje 1.0x/1.3x/2.0x font scale, dynamic type, talkback labels i navigaciju, minimalnu dodirnu površinu u granicama praktične pristupačnosti, kontrast, edge-to-edge/insets, izbjegavanje preklapanja, rotaciju i vraćanje aktivnosti. QA na sporednim uređajima mjeri funkcionalnu prilagodbu, **ne** uspoređuje različite rezolucije sa slikom reference kao da je ista matrica piksela.

## 4 — Ispravnost podataka, performanse i sigurnost

- Audit cijelog Game/Controller/Writer/Backup toka. Checkpoint prije zamjene normal/daily partije, otkazivanje coroutine poslova, serijalizacija JSON-a izvan UI threada, istodobni brzi tapovi, proces death i povrat Activityja, disk-full i corrupt JSON, rollback nakon neuspjelog restorea. Postojeće podatke nikad ne brisati preventivno.
- Šifrirane kopije: AES-GCM i PBKDF2 parametre, salt/nonce, provjeru autentikacije, limit veličine, dopuštene ključeve, provjeru statistike, prihvatljivu migraciju formata, odbijanje tampered arhive, pogrešne lozinke i izvanrednih ulaza; ne logirati plaintext, tajne ni puzzle solution.
- Full **security audit**: manifest/izvezene komponente, Android permisije i network calls, backup pravila, cleartext, dynamic dependencies, supply chain i OSV/CVE/GHSA, CycloneDX SBOM, release R8 i keep rules, GitHub Actions permissions, third-party action version pinning, tajne, signing identitet, Play policies. Datumi i rezultati skena moraju biti vezani za commit.
- Full **dead-code audit**: detektirati neiskorištene Kotlin simbole, Compose grane, resurse, drawables i duplikate, ali ne brisati po R8 usage popisu bez dokazivanja izvornog reachabilityja i scenarija. Analizirati stvarni release R8 output i zadržati funkcionalna UI ponašanja.
- Full **performance audit**: smanjiti redundantne Compose recomposition, Canvas draw allocation, prevelike WebP bitmape, nepotrebne disk commitove, JSON churn, ANR, hladno pokretanje, trajanje generiranja 9×9, jank i memoriju na najsporijim podržanim uređajima. Mjeriti prije/poslije.
- Ovisnosti ažurirati selektivno uz potvrdu kompatibilnosti. Trenutačno u main na datum pregleda: AGP 9.5.0, Gradle 9.6.0, Compose Kotlin plugin 2.4.21, SDK 36, minSdk 26, Java 17. Ne upisivati izmišljene nove verzije bez stvarne provjere.

## 5 — Testovi, CI i release ugovor

Za svaki P0/P1 problem dodaj JUnit ili instrumentacijski test koji reproducira grešku, zatim popravak i dokaz zelenog rezultata. Potrebno: :app:testDebugUnitTest, :app:lintDebug, :app:assembleDebug, :app:assembleRelease, :app:bundleRelease, CycloneDX release-only SBOM, OSV za 100 % release Maven verzija, R8 mapping/usage, analiza izgrađenih permisija, svih 11 emulatorskih poslova te **stvarno instaliranje i pokretanje minificiranog release APK-a** (ako ga sadašnji CI ne pokriva, dodaj novi gate).

Snimke ekrana iz stvarnog Android emulatora moraju pratiti aktualni commit. Re-capture README galerije i Play screenshotova nakon UI izmjena, uz dokaz da nisu statični mockupovi. U PR opis unijeti putanje izmijenjenih datoteka, test rezultate, regionalne pixel razlike i otvorene rizike; review komentare riješiti prije spajanja.

Release izlazi **poslije mergea i zelenih provjera** s povećanim versionCode i odgovarajućim versionName, realnim changelogom, APK, AAB i SHA256SUMS, GitHub release tagom i provjerenim potpisima. Ako nema originalnog izdavačkog ključa, ne predstavljati -test.apk i -unsigned.aab kao Play-ready ni stable produkcijsko izdanje; ostaviti prerelease. Nema lažne potvrde da je release izdan.

## 6 — Kriteriji prihvaćanja

1. Na svakoj izvornoj 941×1672 i 1254×1254 referenci: iste referentne datoteke i deterministički emulator screenshot, nulta stroga RGB/RGBA razlika ako se tvrdi da je pojedini prikaz 1:1.
2. Svi uređaji i veliki fontovi: nema clippinga, neupotrebljivih kontrola ni gubitka podataka; sva funkcionalna stanja prolaze.
3. Nijedna greška logike/snimke/nagrade/backup restorea i nema neobjašnjenih padova CI-ja.
4. Nema poznatih neriješenih P0/P1 sigurnosnih nalaza u opsegu produkcije; ostali nalazi javno i precizno dokumentirani.
5. Tek potvrđeni CI, screenshot, SBOM/OSV, runtime release i release asset rezultati smiju biti označeni [x].
6. Ako se 0 odstupanja ne može postići zbog dinamike, font rasterizacije ili razloga izvornika, prikazati točne preostale razlike i plan, bez uljepšavanja.

**Operativni izlaz svakog ciklusa:** stanje trenutnog PR-a, ostvarene izmjene s putanjama, tablica prije/poslije pixel MAE po ekranu i stabilnim regijama, testovi sa stvarnim ishodom, status sigurnosti, regresije, sljedeći P0/P1 zahvat, status izdanja. Ne postavljaj nepotrebna pitanja ako je repozitorij već dostupan; radi dalje kroz PR i svaki nalaz veži za dokaz.
