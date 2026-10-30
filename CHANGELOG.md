## 1.0.42 — Početni zaslon (Home 2/8), pixel QA

- Dorada **isključivo Home UI-ja** prema korisnikovoj originalnoj referenci 941×1672. Postojeći Compose gumbi, stvarni podaci napretka, navigacija i spremanje partija ostaju funkcionalni.
- Razdvojena ukrasna Sudoku mreža u gornje kutove Home zaslona; ne prelazi više preko središnjeg naslova i kartica. Pozadine ostalih zaslona zadržavaju prethodni prikaz.
- Preciznije poravnati logotip i naslov, slogan, tekst i gumb kartice za nastavak, dekorativna ploča te tipografija velikih i malih navigacijskih kartica.
- Dodatna skalabilna, isključivo neinteraktivna Compose Canvas ilustracija u karticama Nova igra i Dnevni izazov, s mjesečinom, planinama i pagodom, bez lijepljenja statičnih screenshotova preko UI elemenata.
- Ikone izravno uspoređene s referencom: pravi zupčanik za Postavke umjesto sunčevih zraka; običan plus, kalendar sa zvjezdicom i ispunjeni plamen na početnom zaslonu.
- Dodatna instrumentirana provjera jednakih visina i nepreklapanja dviju primarnih Home kartica. Postojeći instrumentirani testovi provjeravaju raspored 2+3 i responsivnu alternativu.
- Ostaju AGP **9.5.0**, Gradle **9.6.0** u četiri CI workflowa, target/compile SDK **36** te `com.brendigo.sudoku`; `versionCode=43`, `versionName=1.0.42`.
- Dok usporedba novih stvarnih screenshotova nije potvrđena, **ne tvrdi se** da je cijeli zaslon 1:1 niti da je izdanje spremno za Google Play. Vidi `docs/home-qa-v1.0.42.md`.

## 1.0.41 — prvi fokusirani pixel QA prolaz: Splash + AGP 9.5

- **Zaslon 1/8 (Splash):** povećan tekst logotipa `Sudoku`, smanjeni razmaci ispod 7–3–9 znaka i natpisa, te bolje usklađen slogan na referentnih 941×1672 piksela. Izvorni prikaz ostaje pravi Jetpack Compose UI.
- Umjesto animiranog Material indikatora, koji se na emulatoru s isključenim animacijama može prikazati samo kao točka, uveden je deterministički iscrtani svijetleći luk dostupan čitačima zaslona.
- Dodan je opcionalni cyan glow oko 7–3–9 logotipa samo na splash zaslonu, bez neželjenog utjecaja na početni zaslon.
- Android Gradle Plugin 8.10.1 → **9.5.0**, Kotlin migriran na ugrađenu AGP podršku, Compose compiler plugin → **2.4.21** i `compilerOptions.jvmTarget=17`.
- Gradle **9.6.0** u četiri postojeća GitHub Actions workflowa (Android CI, visual smoke, publish-release i marketing screenshots). Nije uvedena nova Android dozvola, oglas ili mrežna ovisnost.
- `versionCode=42`, `versionName=1.0.41`; `applicationId` i `targetSdk=36` ostaju nepromijenjeni. Produkcijski potpis i dalje zahtijeva postojeći izdavački ključ.
- **Status:** ovo je prvi zaslon po zaslon; ne tvrdi se da je 1:1 jednakost potvrđena prije novih stvarnih emulator snimki i normalizirane usporedbe. Vidi `docs/splash-qa-v1.0.41.md`.

## 1.0.40 — zaštita spremljenih partija pri promjeni načina igre

- Prije zamjene postojeće Sudoku partije novom, kao i prije prijelaza između redovne i dnevne igre, obavezan je uspješan sinkroni zapis. Ako zapis ne uspije, trenutačna mreža, potezi i statistika ostaju u memoriji, a korisnik dobiva jasnu poruku.
- Ako je primarni zapis već završen, ali postoji druga nedovršena partija, pri ponovnom pokretanju prednost dobiva partija koju je moguće nastaviti. Undo se učitava iz odgovarajućeg zapisa iste partije.
- Dodani su regresijski JUnit testovi odabira spremljene partije i instrumentirani testovi s namjerno odbijenim `SharedPreferences.commit()` pozivima za običnu i dnevnu igru.
- Paket ostaje `com.brendigo.sudoku`; `versionCode=41`, `versionName=1.0.40`. Ovo nije potvrda da je app 100% pixel-perfect ili produkcijski potpisan.
- Prije spajanja i izdavanja obavezni su prolazak CI-ja, instrumentacijskih testova te provjera vizualnih referenci; rezultat tih provjera ne pretpostavlja se unaprijed.

## 1.0.39 — provjera sigurnosnih kopija i precizniji vizualni QA

- **Issue #41:** Prije izvoza obvezna ponovna potvrda lokalne lozinke (12–128 znakova). Dešifrirana kopija prikazuje provjerene brojeve odigranih i završenih igara i dnevnih pobjeda, usporedno s lokalnim napretkom, uz posebno upozorenje na starije kopije. I dalje je potreban izričit pristanak za potpunu zamjenu.
- **Issue #41:** Stroža provjera duplih datuma dnevnih izazova, povijesti, formata datuma i granica statistike; odbacivanje prije upisa. Dodatni instrumentirani testovi provjeravaju pregled i kompatibilnost starijeg v1 formata.
- **Issue #39:** Pixel QA sada može zasebno usporediti sve 10 originalnih referenci, uključujući 1254×1254 logotip i adaptive ikonu, kao i maskirati dinamične vrijednosti *samo u dijagnostičkoj metrici*. Nezaklonjeni globalni RGB MAE ostaje dostupan, s provjerom regresije prema prethodnom CSV-u.
- Korisnički napredak se ne mijenja tijekom QA usporedbe. Više u `docs/pixel-qa.md` i `docs/encrypted-offline-backup-v1.0.36.md`.
- Issue #39 ostaje otvoren dok se ne potvrdi sva 1:1 vizualna geometrija na stvarnim snimkama, bez podmetanja screenshotova umjesto UI-ja.

## 1.0.38 — Mjerljiva vizualna usklađenost s izvornim 1:1 referencama

- Značajnija revizija svih osam Compose zaslona bez uklanjanja postojećih funkcija i bez screenshotova umjesto interaktivnih elemenata.
- Korišten optimizirani originalni **neinteraktivni donji krajolik** iz korisnikove referentne slike (236 × 162 WebP, 2692 B): mjesec, planine, jezero, cvjetne grane, paviljon. Gornji rub postepeno prelazi u Compose proceduralnu pozadinu.
- Preoblikovane dekorativne Sudoku mreže na dvije naglašene ukošene skupine. Zatamnjene zajedničke plave kartice, planine i jezero prema izmjerenim bojama iz originalnih PNG-ova.
- Poboljšan stvarni RGB MAE na 941×1672 na **svih osam** snimljenih zaslona; konkretne izmjerene vrijednosti u `docs/pixel-parity-v1.0.38.md`.
- Izvorni Android UI ostaje interaktivan i responzivan, uključujući 4×4/6×6/9×9 igre, statistiku, postignuća, Settings i sigurnosne kopije.
- Android CI i oba emulatora prošli su s modificiranom vizualnom implementacijom. Raspored i ponašanje na malim zaslonima dodatno su pokriveni smoke testovima.
- `versionName=1.0.38`, `versionCode=39`. Postizanje točnog pixel-perfect 1:1 nije potvrđeno; ne predstavljati MAE=0.

## 1.0.37 — Zaštita napretka pri uvozu i izvozu

- Šifrirana sigurnosna kopija više ne smije biti prazna ili sadržavati samo postavke bez provjerljive statistike; takve datoteke ne mogu izbrisati postojeći napredak.
- Odbijaju se nepotpuni ili proturječni primarni i rezervni zapisi statistike prije zamjene lokalnog stanja.
- Uvoz zaključava lokalno spremanje **prije** upisivanja vraćene kopije, kako asinkroni Android `onStop` ne bi prepisao obnovljenu partiju starom memorijskom verzijom.
- Neuspjeli uvoz ponovno omogućuje automatsko spremanje. Neuspješan sinkroni zapis trenutačne igre blokira uvoz i izvoz.
- Novi instrumentacijski regresijski testovi: prazna i međusobno proturječna kopija, očuvanje trenutačne igre nakon odbijenog uvoza te zaštita od kasnog upisa stare Activity.
- Osam stvarnih screenshotova iz Android emulatora i nastavak mjerljive QA usporedbe. Izgled i postojeća funkcionalnost zadržani; puni pixel-perfect 1:1 još nije potvrđen.
- `versionName=1.0.37`, `versionCode=38`. Objavljeni APK/AAB ostaju testni ako nema pouzdanog izdavačkog potpisa.

## 1.0.36 — Sigurnosna kopija, privatnost i oporavak

- Dodan korisnički izvoz i uvoz **šifrirane offline kopije** putem Android Storage Access Frameworka, bez internetske veze ili širokih dozvola za pohranu.
- AES-256-GCM s nasumičnim sol/nonce vrijednostima, PBKDF2-HMAC-SHA256 (240 000 iteracija), integritetom i verzioniranim formatom datoteke.
- Sigurni limiti veličine, provjera svih spremljenih Sudoku partija, Undo poteza, statistike i dnevnih izazova **prije** uvoza.
- Obavezna zasebna potvrda zamjene lokalnog napretka; pogrešna lozinka i neispravan sadržaj ne smiju pokrenuti vraćanje.
- Spriječeno da Activity onStop odmah nakon uvoza prepiše vraćeno stanje starim podacima.
- Nova Android instrumentacijska ispitivanja za round-trip 4×4 i dnevne nagrade, odbacivanje izmijenjene kopije i pogrešne lozinke.
- Postavke su dopunjene funkcionalnim gumbima, uz zadržavanje postojećih vizualnih elemenata. README koristi stvarne emulator screenshotove.
- `versionName=1.0.36`, `versionCode=37`. Za objavu na Google Playu i dalje je potreban valjan izdavački certifikat.

## 1.0.35 — Pouzdanost napretka, točno vrijeme i vizualna dorada

- Dovršena Sudoku partija, statistika i postignuća spremaju se jednim atomskim lokalnim zapisom kako bi se smanjio rizik dvostrukog rezultata pri rušenju procesa.
- Statistika se može oporaviti iz drugog valjanog lokalnog zapisa ako je prvi oštećen; učitavanje povijesti i Undo zapisa ograničeno je i provjeravano.
- Ukupno vrijeme igranja sada uključuje i nedovršene partije. Stari rezultati migriraju bez brisanja; postignuće za 10 sati u dovršenim igrama koristi dosadašnji podatak.
- Sigurno se odbijaju neispravne veličine Sudoku mreže i regije (0×0 i negativne dimenzije), uz dodatne JUnit testove.
- Nova Android regresija provjerava dovršenje 4×4 partije, zajedničko spremanje partije i statistike, ponovno pokretanje, oporavak oštećene statistike i neponavljanje nagrade.
- Izvršena je stvarna 941×1672 pixel QA usporedba osam ekrana. Vizualna promjena pozadine povećala je RGB MAE (prosjek sedam glavnih zaslona 28.907 → 30.557), zato je **poništena**. Zadržan je dosadašnji nativni UI dok novi dizajn ne pokaže mjerljivo poboljšanje.
- README sadrži stvarnu galeriju osam zaslona; workflow osvježava slike iz Android emulatora za ovu verziju.
- Android `versionCode=36`, `versionName=1.0.35`. Potpisano izdanje ovisi o sigurnoj konfiguraciji izdavačkog ključa.
- Workflow izdavanja može pratiti i ručno pokrenutu, uspješnu Android CI provjeru na `main`, ako se automatski push workflow ne aktivira.

**Napomena:** potpuno pixel-perfect 1:1 podudaranje i odsutnost svih mogućih padova ne mogu se potvrditi samo prolaskom testova.

## 1.0.34 — Oporavak napretka, sigurnost i stvarne snimke aplikacije

- Ako je zajednički zapis partije oštećen ili nedostaje, aplikacija pokušava oporaviti valjanu nedovršenu običnu ili današnju dnevnu igru iz postojećih odvojenih zapisa. Za svaki oporavak koristi odgovarajuću povijest poništavanja poteza.
- Očuvanje aktualne partije prije generiranja nove igre i sinkrono spremanje na `onStop` smanjuju rizik gubitka zadnjih poteza pri prestanku procesa.
- Spremljene Undo bilješke ograničene su na dopuštene brojeve mreže; dodani su Kotlin/JUnit regresijski testovi za logiku oporavka.
- Android manifest izričito isključuje cleartext mrežni promet. Data extraction pravila dodatno isključuju internu pohranu, baze i vanjsku pohranu iz cloud backup i device transfer procesa.
- Dodan instrumentacijski test odsutnosti internetske dozvole, dopuštenja backupa i cleartext prometa.
- Pravila privatnosti pojašnjavaju što se sprema lokalno, ograničenja Android sandboxa te mogućnost trajnog gubitka podataka bez izvoza/sinkronizacije.
- Dekorativni dijelovi splasha (mjesec, planine, jezero, japanski paviljon i grane) zasebno su poravnani prema referenci; interaktivni ekrani koriste dosadašnje pozicije.
- Workflow za README galeriju usmjeren je na aktualnu razvojnu granu i sprema stvarne screenshotove svih osam ekrana (uključujući splash) u punoj referentnoj rezoluciji u WebP formatu. README ne predstavlja konceptualne prikaze kao stvarno korisničko sučelje.
- Verzija `1.0.34`, `versionCode=35`. Release se objavljuje tek nakon uspješnih provjera.
- Ograničenja, neprovjerena 1:1 jednakost, i preostali sigurnosni rizici dokumentirani su u `docs/security-and-reliability-v1.0.34.md`.

## Neobjavljeno — priprema 1.0.33 (provjera u tijeku)

- Splash zaslon zadržava originalni logo 7–3–9, ali ispravlja vertikalno pozicioniranje: logo više nije vezan uz indikator učitavanja, koji je smješten bliže donjem rubu prema referenci.
- Učitavanje spremljene partije otpornije je na nepotpune ili nevaljane pomoćne bilješke. Valjana Sudoku mreža ostaje dostupna, a nevaljani bitovi kandidata se čiste bez destruktivne migracije.
- Regresijski JUnit testovi pokrivaju nedostajuće, skraćene, preduge i oštećene bilješke.
- Pixel-compare sada nudi odvojeno mjerenje UI-regije i pozadine uz opcionalne maske; postotak bliskih piksela temelji se na sva tri RGB kanala.
- Python testovi QA metrike uključeni su u Android CI, a dokumentirana je stvarna v1.0.32 osnovica prema artefaktu emulatora.
- Nema promjene verzije, produkcijskog potpisa ili tvrdnje o pixel-perfect jednakosti prije uspješnog CI-ja, novih snimki i pregledanog PR-a.

## 1.0.32 — Revizija prema deset izvornih 1:1 dizajnerskih referenci

- Preuzete su stvarne screenshot datoteke Android emulatora za prethodnu verziju 1.0.31 i uspoređene sa sedam korisničkih referenci dimenzije 941×1672.
- Mjerenja prosječne apsolutne razlike RGB (MAE; skala 0–255) prije ove revizije: Home 29,27; Nova igra 29,06; Igra 23,28; Postavke 27,80; Dnevni izazov 29,68; Statistika 30,99; Postignuća 32,18. To nisu savršena poklapanja i sadrže razlike podataka/datumâ, generatora i pozadinskih ilustracija.
- Referentna kartica „Bilješke” na ekranu igre sada prikazuje i stvarni status uključenosti te malen vizualni prekidač; na uskim zaslonima koristi kompaktne dimenzije.
- Usklađena hrvatska oznaka težine („Srednja težina”) na ekranu igre i početnom nastavku igre, bez promjene odabranih težina.
- HUD i kartica nastavka prikazuju stvarni broj praznih polja, ne broj ćelija koje se ne podudaraju s rješenjem.
- HUD na referentnom zaslonu ima postotak uz traku napretka, a razmaci između kontrola smanjeni su s 8dp na 4dp kako bi Sudoku mreža bila bliže referentnoj vertikalnoj poziciji.
- Usklađeni su opisi više redaka Postavki prema korisnikovoj referentnoj snimci. Sekcija „O aplikaciji” dobila je pravi gumb i informativni dijalog kao što sugerira referentna strelica.
- UI testovima dodana je provjera referentne pozicije ploče, četiriju kontrola, naziva težine, statusa bilježaka i interaktivnog „O aplikaciji”.
- Stvarne originalne slike su sačuvane zasebno za pixel-compare. Ne potvrđuje se MAE=0 ni pixel-perfect dok nisu normalizirani podaci, fontovi, proceduralna pozadina i snimke nakon izmjena; otvoreni #11 ostaje aktivan.

## 1.0.31 — Postavke: stabilni prekidači, dijalozi i povratak u igru

- Android ponovno stvaranje aktivnosti sada čuva trenutno otvoren ekran Postavke i porijeklo navigacije; povratak iz postavki pozvanih tijekom igre vraća istu Sudoku partiju umjesto početnog ekrana.
- Ekran igre vraća se nakon rekreacije kada postoji spremljena partija; već postojeći odabir nove igre ostaje podržan.
- Otvoreni dijalozi zvuka/vibracije, jezika i pravila privatnosti zadržavaju stanje tijekom rotacije, promjene izgleda i rekreacije aktivnosti.
- Redovi „Zvuk i vibracija” i „Pravila privatnosti” imaju jasnu semantičku ulogu gumba za pristupačnost.
- Redovi kategorija i prekidači dobili su sigurno prelamanje opisa i razmak ispred prekidača, bez promjene referentnih boja, visine kontrola ili teksta stranica.
- Instrumentacijski testovi prebacuju svih sedam stvarnih postavki (tema, animacije, automatske bilješke, provjera greške, isticanje brojeva, zvuk, vibracija) te provjeravaju trajni zapis nakon rekreacije i vraćaju izvorne vrijednosti.
- Dodatni test potvrđuje očuvanu navigaciju iz igre → Postavke → rekreacija → natrag u istu Sudoku mrežu.
- Bez izmjena pravila igre, generatora, statistika, privatnosti, brenda i tekstova stranica.
- Točna pixel-perfect jednakost s izvornim PNG referencama još nije mjerena (#11).

## 1.0.30 — Dnevni izazov: čitljiv kalendar i nastavak partije

- Na referentnom 941×1672 zaslonu zadržani su postojeći stupci i kartice dnevnog izazova.
- Na kompaktnim ekranima ili uz veći sustavni font sažetak prikazuje datum iznad niza i nagrade, bez preklapanja sadržaja.
- Tri metrike izazova raspoređene su u dva retka (2 + 1), uz puni tekst težine, procijenjenog vremena i bodova.
- Naslov „Misteriozna planina” može zauzeti dva retka na uskom ekranu, bez rezanja teksta.
- Gumb na zaslonu Dnevni izazov prikazuje „Nastavi današnji izazov” kada je za taj datum već spremljena nedovršena partija. Nakon dovršetka ostaje zaključan, bez druge nagrade.
- Tjedni kalendar uključuje i stariji zapis `lastDaily` kada postoji, radi kompatibilnosti s prethodnim statistikama, a segmenti se osvjetljavaju samo između uzastopno dovršenih dana.
- Prikaz osobne rang-liste čuva otvoreno stanje pri ponovnom stvaranju aktivnosti.
- Instrumentacijski testovi provjeravaju raspored kartica, svih sedam oznaka tjedna te nastavak iste spremljene partije uz očuvane bilješke nakon Android rekreacije.
- Nisu mijenjani generator, dodjela bodova, postojeći sadržaj, prava privatnosti ni vizualni identitet.
- Nema dokazane potpune pikselne jednakosti s originalnim dizajnerskim PNG-ovima; pitanje #11 ostaje otvoreno.

## 1.0.29 — Igra: sigurna pauza i pristupačan HUD

- Sudoku brojevi i bilješke više nisu vidljivi dok je igra pauzirana; ploča pokazuje oznaku pauze bez mijenjanja spremljene partije.
- Nakon nastavka vraćaju se ista mreža, potezi i interaktivne numeričke tipke.
- HUD prelazi u dva retka na iznimno uskim ekranima ili uz uvećani sustavni font, kako bi mjerač, napredak i greške ostali čitljivi.
- Zaglavlje igre prilagođava širinu naslova, a tipkovnica smanjuje font brojeva samo na vrlo uskim tipkama.
- Akcije „Bilješke”, „Savjet”, „Poništi” i „Izbriši” prilagođavaju ikone/tekst uskim karticama; semantički status bilježaka i broj preostalih savjeta dostupni su čitačima zaslona.
- Pojedinačne Sudoku ćelije ne predstavljaju se kao aktivne nakon završetka ili neuspjeha igre.
- Regresijski test pokreće pravu partiju, provjerava HUD, pauzu, skrivanje ćelija i nastavak sa svim kontrolama.
- Mehanika igre, generator zagonetki, podaci, statistika i logotip 7–3–9 nisu mijenjani.
- Pixel-perfect 1:1 nije dokazano jer izvorne dizajnerske PNG reference nisu u GitHub repozitoriju (#11).

## 1.0.28 — Nova igra: prilagodljiv odabir veličine i težine

- Zadržan postojeći 192 dp odabir 9×9 / 6×6 / 4×4 na referentnom zaslonu; na iznimno uskim uređajima odabir se sužava i ne preklapa gumb za povratak.
- Kartice svih šest težina dobivaju prilagodljiv unutarnji raspored, veličine ikonica i više prostora za tekst pri maloj širini ili uvećanom sustavnom fontu.
- Interaktivne opcije veličine i težine označene su pristupačnom radio-ulogom i stvarnim stanjem odabira.
- Aktivnost vraća ekran „Nova igra” nakon rotacije, dok Compose `rememberSaveable` vraća odabranu veličinu i težinu; generiranje tijekom pokretanja onemogućuje naknadnu promjenu postavki.
- Gumb pokretanja širi se na vrlo uskim ekranima, a referentna širina ostaje 75 % kartice.
- UI testovi provjeravaju granice odabira veličine, svih šest težina, rotaciju i stvarno pokretanje igre 6×6.
- Nema promjene logike generatora, pravila Sudoku, lokalno spremljenih partija, pozadina ni originalnog brenda 7–3–9.
- Potpuna pixel-perfect jednakost s korisnikovim PNG-ovima i dalje nije mjerena jer referentne datoteke nedostaju u repozitoriju (#11).

## 1.0.27 — Početni ekran: responzivna navigacija i tjedni niz

- Očuvan referentni raspored dviju velikih i triju malih navigacijskih kartica na širim Android zaslonima.
- Na kompaktnim telefonima tri manje kartice složene su u dva retka (2 + 1), bez preklapanja naziva i strelica.
- Na izrazito uskim uređajima dvije velike kartice mogu se složiti u jedan stupac.
- Tjedni niz na uskim ekranima prikazuje oznake sedam dana u zasebnom redu; stanje je i dalje stvarni lokalno spremljeni napredak.
- Slogan skaliran na kompaktnom zaslonu te dodani pristupačni opisi nastavka igre.
- Instrumentacijski testovi potvrđuju geometriju kartica, očuvanje referentnog rasporeda i granice tjednog niza.
- Nisu mijenjani mehanika igre, pohrana, postojeće pozadinske ilustracije, ikonice 7–3–9 ni sedam marketinških WebP snimki.
- 1:1 rezultat nije dokazan bez normaliziranih originalnih referentnih PNG-ova (issue #11).

## 1.0.26 — Prilagodljiv ekran Postignuća

- Sačuvan izvorni dvostupčani raspored i redoslijed osam istaknutih postignuća na referentnom zaslonu.
- Na kompaktnim Android ekranima kartice se mogu proširiti kako opis i napredak ne bi bili odrezani.
- Za uske uređaje ili povećani sistemski font prikazuje se jedan stupac pristupačnijih kartica.
- Sažetak bodova i broja otključanih nagrada prilagođen je užim uređajima; gumb za proširenje ima jasan opis za čitače zaslona.
- UI regresijski testovi provjeravaju smještaj opisa i prisutnost svih 24 postignuća.
- Nisu mijenjani bodovi, kriteriji nagrada, lokalni spremljeni podaci ni originalne SVG/WebP datoteke.
- Pixel-perfect 1:1 nije potvrđen bez izvornih referentnih PNG-ova (issue #11).

## 1.0.25 — Statistics compact-screen readability

- Scale statistics tile icons and text at widths below 180dp per tile.
- Allow labels to wrap onto two lines, preserving readable time values.
- Add compact-emulator regression coverage for metric card separation and info dialog.
- Keep real statistics, offline game state, seven-screen marketing gallery and original branding unchanged.
- Pixel-perfect parity remains unverified without normalized 941×1672 owner references (#11).

## 1.0.24 — Statistics reference polish

- Kept the seven real emulator screenshots and branded marketing README from 1.0.23.
- Reduced Statistics title offset and adjusted metric card typography.
- Added branded vector chart-heading symbols, retaining truthful live values.
- No modification to the saved state, score calculations, game logic or achievements.
- Pixel-perfect not yet certified; issue #11 continues to track reference parity.

## 1.0.23 — marketing README and multi-screen visual audit

- Replaced the lengthy development-focused homepage with a Croatian marketing
  README featuring the existing Sudoku/Brendigo original SVG logo, 7-3-9 icon,
  feature banner and a complete seven-screen gallery of real emulator captures.
- Added an isolated GitHub Actions job to capture the actual Compose app on a
  941×1672 emulator and commit optimized WebP screenshots to docs/screenshots.
- Centered reference headings for Statistics and Settings now use uppercase,
  letter-spaced Croatian descriptors; they remain accessible native text.
- Reduced the oversized gap above the Daily Challenge summary and improved its
  warm hero contrast and title styling to more closely match the owner artwork.
- Refined native scenic moon and cherry blossom positions/colors across screens.
- Preserved real offline data, 24 achievements, gameplay mechanics and saved
  state; no static screen backgrounds masquerading as live controls.
- No claim of full 1:1 parity: all screen areas still require normalized
  numeric comparison with the owner's design files (issue #11).
- APK/AAB signing state follows CI secrets; unsigned release is not Play-ready.

## 1.0.22 — Achievements screen reference pass

- Tightened the achievements header-to-score spacing for the owner reference.
- Aligned four featured rows with a taller top half and more compact lower half.
- Gold awards retain warm sunset illustration while a native scrim expresses lock.
- Enlarged reward icons, point badges, progress track, and outlined completion.
- Distinct scalable calendar-seven icon matches the seven-day award.
- Added geometry regression checks; preserved all 24 functional awards and data.
- Pixel-perfect match is not certified until normalized 941×1672 image QA (#11).

## 1.0.21 — Home screen reference alignment

- Reworked the first Home screen's vertical composition and resume panel.
- Restored a native vector mini Sudoku illustration behind the resume button.
- Tuned two large and three compact cards with active chevrons and proper copy.
- Improved real-time streak and next reward layout; no mock statistics.
- Added UI screenshot and geometry assertions for Home elements.
- Saved state and other screens unchanged. Pixel parity still to be measured.

## 1.0.20

- Added graph icon to Statistics title and vector arrows in summary cards.
- Completion donut now shows completed/abandoned legend with real counts.
- Replaced Unicode resume timer with branded scalable icon.
- Linked weekly progress only between adjacent completed daily challenges.
- Improved Sudoku selection rim, group separators and TalkBack state.
- Exposed the completed state of weekly circles to accessibility.
- Recolored locked achievements progression by individual reward tier.
- Added subtle 7/9 glows to adaptive icon while keeping 7-3-9 geometry.
- UI regressions for game selection, statistics legend and weekday labels.
- No save format migrations. Pixel-perfect remains unverified (issue #11).

## 1.0.19

- Improved Settings vertical layout and About panel so the real version row fits in the original 941×1672 screenshot viewport.
- Preserved footer isolation, white-thumb cyan switches, palette icon and achievement color identity from v1.0.18.
- No user-data migration or removed features; revalidate Android CI and both UI emulator sizes before release.
- Numeric reference matching is not yet pixel-perfect (issue #11).

## 1.0.18

### Visual alignment / accessibility
- Corrected footer overlap: every major page reserves a separate non-scrolling
  footer slot outside its fully scrollable content.
- Replaced Material default switches with native cyan-blue/white-thumb controls
  while preserving a single accessible and persisted switch per setting.
- Added a dedicated palette vector icon for Appearance.
- Locked achievements show meaningful dimmed award symbols alongside small
  lock status icons; unlocked cards gain explicit completion checks.
- New instrumentation compares footer top with the last actionable settings
  and achievements bounds, guarding compact/reference viewport sizes.

### QA
- Retain save format; gate publication on JUnit, Android Lint, debug APK,
  unsigned AAB and both emulator smoke tests.
- Reference artwork remains the target: numerical MAE=0 not claimed.

## 1.0.17

- Recalibrated native night scenery, mountains, background Sudoku grid and sakura palette across all seven screens.
- Improved launcher icon center cyan digit 3 glow, retaining 7-3-9 adaptive-safe geometry.
- Enlarged splash mark/title and game HUD, improved selection-cell glow.
- Home reward now shows gift icon; difficulty cards use directional icons.
- Android size/difficulty controls provide real accessible selected semantics.
- Settings About section includes info icon and version row.
- Added instrumented assertions for 9x9/4x4 and difficulty selection.
- No claim of pixel-perfect parity; open issue #11 remains.

## 1.0.16

- Enlarged statistics chart geometry, labels and circular completion indicator.
- Zero-win categories now render a minimal baseline rather than a misleading bar.
- Emulator smoke test asserts both statistics visualizations are present.
- Original reference pixel parity remains a work in progress (issue #11).

## 1.0.15

### Native UI reference refinements
- Daily Challenge headline aligned independently of the back button.
- Calendar, streak and golden reward segmented into balanced panels with
  a dedicated vector gift glyph; three bordered challenge metrics.
- Seven-day progress shows connected circles and actual device calendar days.
- Primary challenge and secondary ranking controls match narrower reference widths.
- Achievements score header split by divider, with real totals and reward iconography.
- Larger achievement cards, points badges, icons, progress bars and descriptions.
- Night landscape and warm reward cards use darker/navier gradients for text clarity.
- Instrumented tests cover daily ranking and achievement component dimensions.

### Quality gates
- CI: JUnit, Android Lint, APK/AAB build, 941x1672 and 720x1280 emulators.
- Previous v1.0.14 measured full-frame mean RGB error: 34.010 / 255.
- Dynamic dates, sample progress and scenic image details still differ from artwork.
- Never declare pixel-perfect without normalized evidence; tracking issue #11.
## 1.0.14

### Pixel-alignment and interactions
- Corrected misaligned/stretched second row of the 9×9 numeric keypad:
  all key widths match and the final four are centered.
- Candidate notes now occupy stable micro-grid positions (9×9: 3×3,
  6×6: 3×2, 4×4: 2×2) and have accessible spoken labels.
- Premium circular home-menu vector icon framing, brighter primary buttons
  with dedicated cyan outlines, and more consistent Settings section tiles.
- Four real achievement preview cards in Statistics with progress and
  unlocked/locked vectors, linked to all 24 achievements.
- Cool-blue daily date header with separate gold achievement accents.
- Updated emulator tests for notes and equal-width keypad key geometry.

### QA
- Preserve user saves and settings. Require green Android CI and both
  941×1672/720×1280 emulator smoke tests before release.
- Screenshot pixel parity is not certified. Issue #11 remains open.

## 1.0.13

### Seven-screen UI and accessibility
- Anchored shared footer to the lower scenic background while preserving
  full content scrolling and avoiding controls hidden beneath it.
- Daily challenge shows real Monday–Sunday calendar dates and a named
  reward, with revised calendar captions.
- Entire statistics history header is clickable and has a 48dp touch target.
- Statistics achievement preview now links to actual award names.
- Responsive Sudoku board now fits narrow multi-window device widths.
- Notes cannot be toggled during pause or completed/failed games.
- Settings vector icons use explicit icon keys rather than ignored emoji
  parameters and inferred titles.
- TalkBack description for the Sudoku 7–3–9 brand icon; additional subtle
  cyan glow in adaptive launcher artwork.
- Reference-size and compact emulator smoke runs test 941×1672 and 720×1280.

### Verification
- Real emulator and static checks required before merge and version release.
- The original source artwork remains the target: 100% 1:1 and MAE=0
  are **not claimed** until normalized screenshot comparison proves them.

## 1.0.12

### UI and branding
- Stronger 7–3–9 adaptive and themed launcher assets and larger home/splash logo.
- Enriched Compose midnight scene, lake reflection, blossoms and neon card sheen.
- Tuned titles, statistics, daily header/weekly markers and achievement rows.
- Color-preserving difficulty cards with native circled leaf, star and crown icons.

### Functionality and stability
- “Odigrane igre” now displays started sessions, not finished sessions.
- New games select a central editable cell to make the keypad immediately usable.
- Contextual undo/erase/hint/keypad availability and brighter selected-cell border.
- Added emulator assertions for actions before/after a move, undo and notes.
- Preserves existing local saves and statistics; no new permissions.

### QA and limitations
- Builds must pass JVM tests, Lint, debug APK and release AAB, and emulator UI
  navigation/screenshot capture before merge. Package signing status is documented
  in each release.
- Original 941×1672 pixel-for-pixel equality **not yet established**. Issue #11
  remains open.

# Changelog

## 1.0.11

### Fixed
- On the Home and Daily Challenge screens, the displayed calendar, current
  streak and today's challenge availability refresh after the device crosses
  local midnight, without requiring navigation away and back.
- The refresh also checks for manual device clock/time-zone changes at least
  once per minute, including across daylight-saving transitions.
- Removed unused `HomeTile` icon arguments; visible icons remain native
  `BrandIcon` vectors and screen navigation behavior is unchanged.

### Quality
- Added deterministic JVM tests for midnight rollover, Europe/Zagreb
  daylight-saving time, calendar year rollover and periodic refresh bounds.
- Preserved existing game saves, statistics, daily progress and settings.
- Pixel-perfect matching against the supplied 941×1672 references remains
  unverified, tracked in GitHub issue #11.
- APK/AAB publication is gated on successful GitHub Actions checks. Without
  publisher signing secrets, files are test APK and unsigned AAB.

## 1.0.10
- Refined vector mountain, moonlit pagoda, blossom and lake scenery.
- Corrected Android 13+ monochrome themed launcher icon alpha mask.
- Added native hearts and accessible achievement detail dialogs.
- Improved weekly streak markers, reward visuals and UI test coverage.

## 1.0.9
- Improved adaptive launcher icon, tap targets, theme contrast and
  full-row setting switches.
