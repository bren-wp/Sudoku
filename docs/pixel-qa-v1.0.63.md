# Sudoku by Brendigo — Pixel-QA v1.0.63 (12. studenoga 2026.)

## Provjereni izvori i metoda

Svih 10 korisnikovih izvornih PNG-ova iz 24. listopada 2026. ponovno je pronađeno u korisničkoj zbirci. Svih deset SHA-256 početaka odgovara onima iz postojećeg zadatka. Originalne dimenzije: 941×1672 za osam aplikacijskih zaslona; 1254×1254 za wordmark i launcher.

Stvarni Android PNG-ovi snimljeni su na 941×1672 px preko instrumentacijskog workflowa [Refresh Sudoku marketing screenshots #43122906385](https://github.com/bren-wp/Sudoku/actions/runs/43122906385), iz izvornog koda commita `ca6986be9194c5e0bad3d5e079a5b4efa8bb1e66`. WebP marketinške slike nakon toga su automatski objavljene u `docs/screenshots`. Usporedba koristi izravni RGB MAE svih kanala (raspon 0–255), bez reskaliranja, maskiranja ili fotografskog uljepšavanja.

**Važno:** slika korisnikove reference sadrži ilustrativne rezultate, datum, statuse postignuća i vrijeme. U emulatoru su prikazani stvarni podaci testne partije. Stoga je sirovi globalni MAE samo gruba usporedba, **nije postotak vizualnog odstupanja** niti izolirana ocjena geometrije UI-a.

## Globalni RGB MAE

| Zaslon | Ranije snimke (11.11.) | Novi emulatorski PNG | Razlika (novi − raniji) |
| --- | ---: | ---: | ---: |
| Naslovnica sa spremljenom igrom | 23,9463 | 24,1152 | +0,1689 |
| Nova igra | 22,6101 | 22,6224 | +0,0123 |
| Aktivna igra | 20,8222 | 20,2655 | −0,5567 |
| Postavke | 19,4215 | 19,4239 | +0,0024 |
| Dnevni izazov | 24,8353 | 24,7803 | −0,0550 |
| Statistika | 26,6392 | 26,5834 | −0,0558 |
| Postignuća | 29,3214 | 29,8627 | +0,5413 |
| Splash | 10,3842 | 10,3842 | 0,0000 |

Wordmark (RGB) iz obje serije: 18,8339. Launcher foreground (RGB): 16,1747. Za ova dva transparentna izvora potrebno je dodatno tumačenje alfa-kanala i maski; navedeni RGB nije samostalna mjera kvalitete adaptivne launcher ikonice.

**Rezultati koji su viši nisu skrivani.** Sirova odstupanja mogu se promijeniti bez regresije dizajna jer reference i emulator ne sadržavaju iste podatke, spremljene partije ili datume. Nije potvrđeno 1:1 podudaranje nijednog kompletnog zaslona.

## Mjerena lokalna dorada krajolika

Dnevni izazov koristi originalnu dekorativnu WebP sliku planina i pagode iza živog Compose UI-a. Na lijevom rubu WebP-a primijenjena je postupna alfa-maska, koja uklanja dio naglog spoja.

| Područje Dnevnog izazova (native px) | Prije alfa-prijelaza | Nakon | Razlika |
| --- | ---: | ---: | ---: |
| Cijeli zaslon | 24,7849 | 24,7803 | −0,0046 |
| Glavna hero kartica (x40–902, y548–956) | 25,4804 | 25,4643 | −0,0161 |
| Krajolik (x550–900, y555–818) | 21,6974 | 21,6158 | −0,0816 |
| Rub spoja (x530–675, y552–800) | **17,2712** | **17,0535** | **−0,2177** |
| Donji dio kartice | 22,6479 | 22,6479 | 0,0000 |

ROI koordinate odnose se na snimke 941×1672 px. Mjereni su stvarni PNG-ovi prije i nakon korekcije, bez omekšavanja slike pri izračunu.

## Što je promijenjeno

- Funkcionalne native Compose kontrole Dnevnog izazova ostale su interaktivne; ne koristi se lažna screenshot-pozadina.
- Uski Android zasloni i veliki fontovi dobili su prilagođenu širinu rang-liste i čitljiviji natpis glavnog gumba.
- Kartica nastavka igre na početnoj stranici izbjegava preklapanje živih podataka o preostalim poljima i vremenu.
- Novi Android UI test provjerava geometriju natpisa Dnevnog izazova u odnosu na stvarne dodirne kartice.

## Preostali otvoreni rad

1. Najveći globalni MAE imaju Postignuća, Statistika i Dnevni izazov, a dio razlike dolazi od različitih podataka.
2. Za pouzdaniji pixel-perfect proces potrebne su fiksne i dokumentirane emulatorske postavke i testna stanja, uz odvojene ocjene statičnih regija i dinamičkog sadržaja.
3. Potrebno je nastaviti ručno detaljno podešavanje komponenti svih osam ekrana te zasebno alfa/mask analizu logotipa i launcher ikonice.
4. Prolazak unit testova ili screenshot capture workflowa ne dokazuje 0% odstupanja; status svih 11 profila provjerava se zasebno kroz Android visual smoke.

**Zaključak:** ostvarena je lokalna, mjerljiva korekcija pozadine i poboljšanje responzivnosti. Potpuna 1:1 jednakost i 0% odstupanja nisu postignuti.
