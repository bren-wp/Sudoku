# Sudoku 1.0.36 — lokalna šifrirana kopija i povrat

## Za igrače

1. Otvori **Postavke → Sigurnosna kopija → Izvezi kopiju**.
2. Unesi lozinku od najmanje 12 znakova. Sačuvaj je izvan aplikacije.
3. Androidovim odabirom dokumenata spremi `.sudoku` datoteku na željeno mjesto.
4. Na drugom uređaju, u istoj aplikaciji, izaberi **Uvezi kopiju** i
   označi svoju datoteku. Unesi izvornu lozinku.
5. Aplikacija prvo provjerava autentičnost i strukturu datoteke. **Tek nakon
   izričite potvrde** zamjenjuje lokalni napredak i učitava ga ponovno.

**Važno:** zamjena nije spajanje dviju povijesti. Zato se preporučuje izvesti
i postojeću kopiju prije povrata. Kopija sadrži i rješenja Sudoku zagonetki;
ne dijeli lozinku. Ako je zaboraviš, sadržaj se ne može dešifrirati.
Nije moguće jamčiti oporavak izgubljene datoteke ili neispravne pohrane.

## Kriptografski format

- ASCII zaglavlje: `BRENDIGO-SUDOKU-BACKUP-1\n`.
- Nasumična sol 16 B + AES-GCM nonce 12 B + šifrat s 128-bitnim tagom.
- PBKDF2-HMAC-SHA256, 240 000 iteracija, ključ AES-256.
- Zaglavlje je AAD; izmijenjeni šifrat ili pogrešna lozinka odbacuju se.
- JSON v1 payload s `format` i `state`, unutar autentificiranog šifrata.
- Maksimalno 1 MB šifriranog ulaza i 600 KB dešifriranih podataka.
- Izvođenje ključa i čitanje/pisanje datoteke rade na `Dispatchers.IO`.
- Koristi se isključivo Android Storage Access Framework.
- Nema Android storage dozvola, slanja na vlastiti poslužitelj ili
  trajnog spremanja korisničke lozinke.

## Zaštita od kvarova

Od verzije 1.0.37 prazne i međusobno proturječne kopije statistike odbacuju se prije bilo kakve zamjene. Ako je aktualnu partiju nemoguće sinkrono spremiti, uvoz ili izvoz se otkazuje. Zaštita od upisa starog Activity stanja uključuje se **prije** vraćanja datoteke, a nakon neuspjeha ponovno se omogućuje normalno lokalno spremanje.

Prije promjene lokalnog stanja provjeravaju se duljine, Sudoku dimenzije
(4×4, 6×6, 9×9), valjana rješenja, konzistentnost zadanih polja,
bilješke, pogreške, savjeti, datumi, statistika, granice
Undo povijesti i verzija formata.

Zamjena se provodi jednim `SharedPreferences.Editor.commit()` za
poznate ključeve, bez mreže i bez nepotrebnog brisanja drugih postavki.
Controller stare Activity zaustavlja pisanje prije `recreate()`, kako
`onStop()` ne bi prebrisao upravo vraćenu partiju.

## Provjere prije produkcije

- Android instrumentirani `SudokuBackupTest`: ispravan round-trip,
  statičke bilješke, Undo, oporavak dnevnih nagrada, odbacivanje lozinke
  i izmijenjene datoteke.
- `SudokuVisualSmokeTest`: otvaranje i odustajanje od izvoza/uvoza;
  provjera nastanka pravog Settings prikaza na oba emulatora.
- Android CI: unit test, Lint, debug APK, release APK/AAB.
- `docs/screenshots`: osam stvarnih WebP screenshotova Android emulatora,
  ne konceptnih PNG referenci.
- Vizualna 1:1 provjera: zasebni zadatak #39. Dodana opcija u Postavke
  nije dio starog referentnog dizajna, ali ne smije narušiti postojeće
  kartice ili prekriti navigaciju.

**Rezidualni rizici:** zaboravljena lozinka, gubitak fizičke kopije,
kvar uređaja, zlonamjeran pružatelj dokumenata, potpuno kompromitiran Android
i niski prostor pohrane. Osoba koja kontrolira otključani uređaj ima pristup
dešifriranim podacima aplikacije. Bez nezavisnog potpisnog certifikata nema
produkcijski potpisanog AAB-a.
