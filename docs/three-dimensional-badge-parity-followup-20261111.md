# Sudoku — nastavak usklađivanja 3D ikonica (11. 11. 2026.)

## Prioritet na temelju prethodnog stvarnog Android pixel-QA audita

- Postignuća: RGB MAE 29,27 / 255 (najveće odstupanje među ekranima).
- Statistika: RGB MAE 26,43 / 255.
- Dnevni izazov: RGB MAE 24,96 / 255.

Brojevi su **povijesna** mjerenja iz `docs/PIXEL_QA_HR.md`; nisu novo mjerenje na ovom commitu. Nula nije potvrđena.

## Ciljana promjena

Umjesto plosnatih vektorskih zamjena, postojeći lokalni atlas vlasnikovih izvornih 3D oznaka (`achievement-badges-atlas.webp`) sada se koristi i na ovim mjestima:

1. Postignuća — trofej u sažetku bodova, korištenjem istog stvarnog umjetničkog sloja kao kartica „Prva pobjeda”.
2. Statistika — ilustracija kartice „Dnevni niz” i pregled nagrade „Pobjednički niz”.
3. Dnevni izazov — oznaka niza u oba prilagodljiva rasporeda sažetka.

To mijenja samo dekorativnu ikonu. Podaci, broj bodova, stanje otključavanja, stvarni datumi, napredak, klikne površine i navigacija ostaju stvarni Compose UI. Izvorne reference nisu pretvorene u pozadinske snimke cijelog ekrana. Nisu dodane vanjske ovisnosti.

## Provjera

Dodana je instrumentacijska provjera `ownerThreeDimensionalBadgesFitAchievementsStatisticsAndDailyPanels` koja provjerava da lokalni atlas postoji i da se svaki uvedeni 3D element nalazi unutar roditeljskog panela. Postojeći `Android visual smoke` na 11 profila i Python `final_ten_reference_qa.py` ostaju glavni regresijski i pixel-QA alati.

**Nije dokazano 1:1.** Bez neizmijenjenih korisničkih PNG originala i odgovarajućih novih emulatorskih PNG snimaka ne smije se prijaviti novi MAE, SSIM niti smanjenje postotka odstupanja. Novi prije/poslije rezultat mora se izmjeriti na identičnim 941×1672 stanjima, uz zasebne regije za statičke i dinamičke vrijednosti. Ako je neka regionalna metrika pogoršana, potrebno je povući samo taj vizualni zahvat.
