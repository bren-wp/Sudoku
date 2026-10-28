# Privatnost — Sudoku by Brendigo

**Ažurirano:** 29. listopada 2026. · Aplikacija za Android

Sudoku by Brendigo radi **bez registracije i internetske veze**. Aplikacija ne
traži dozvolu `INTERNET` i ne sadrži oglase, analitiku, oglasne identifikatore
ni SDK-ove za praćenje. Ne šalje korisničke podatke Brendigu niti drugim
poslužiteljima.

## Podaci pohranjeni na uređaju

- Trenutačna Sudoku mreža i njezino rješenje, korisnički upisi, bilješke i potezi.
- Odigrane i završene igre, trajanja, dnevni izazovi, bodovi i postignuća.
- Postavke teme, zvuka, vibracije i pomoći.

Ti se podaci spremaju u **privatnu lokalnu pohranu aplikacije** na uređaju
(Android SharedPreferences). Nisu šifrirani zasebnim ključem aplikacije, ali
su pod kontrolom Androidove izolacije aplikacija i zaštite uređaja. Zato
preporučujemo zaključavanje uređaja i ažuriranje Androida. Kod rutiranih ili
kompromitiranih uređaja izolacija pohrane nije zajamčena.

## Pouzdanost lokalnog spremanja

Podaci o aktivnoj partiji i statistika sada se zapisuju zajedno u jednoj
Androidovoj lokalnoj transakciji spremanja. Statistika ima i drugi lokalni
zapis za oporavak u slučaju oštećenog primarnog zapisa. Oba su zapisa u istom
privatnom prostoru aplikacije, **nisu cloud sigurnosne kopije** i ne štite od
gubitka ili kvara cijelog uređaja.

## Sigurnosne kopije i gubitak podataka

Aplikacija u Android manifestu isključuje standardni automatski backup.
Nema cloud sinkronizacije, korisničkog računa ni pristupa poslužitelju.

U **Postavke → Sigurnosna kopija** korisnik može dobrovoljno izvesti
šifriranu lokalnu datoteku pomoću Androidova odabira dokumenta. Kopija sadrži
podatke o igrama, uključujući cjelovita rješenja zagonetki, statistiku i
postavke, ali je zaštićena AES-256-GCM enkripcijom i korisničkom lozinkom
(PBKDF2-HMAC-SHA256; nasumična sol i nonce). Lozinka se ne pohranjuje
u aplikaciji, ne šalje se mrežom i nije ju moguće obnoviti.

Izvoz može završiti u direktoriju ili servisu koji korisnik sam odabere,
uključujući eventualnu cloud pohranu pružatelja dokumenata izvan
kontrole aplikacije. Brendigo datoteku ne šalje samoinicijativno.

Prije uvoza aplikacija provjerava format, integritet i podatke te zahtijeva
jasnu potvrdu zamjene postojećih rezultata. Kriva lozinka ili nevaljan format
ne smiju uzrokovati namjernu promjenu lokalnog stanja. Gubitak lozinke,
datoteke, brisanje aplikacije bez izvoza ili kvar uređaja i dalje mogu
uzrokovati trajni gubitak napretka.

Nadogradnja aplikacije s istim identifikatorom paketa i valjanim kompatibilnim
potpisom inače zadržava lokalne podatke. Novi kod nastoji oporaviti valjane
spremljene partije bez brisanja podataka. Ne može jamčiti povrat podataka
nakon fizičkog gubitka uređaja ili kvara pohrane.

## Kontrola i kontakt

Podatke možete ukloniti u **Postavke Androida → Aplikacije → Sudoku →
Pohrana → Izbriši podatke** ili deinstaliranjem aplikacije. Ta se radnja ne
može poništiti.

**Izdavač:** Brendigo  
**Web:** https://brendigo.com

Prije svakog izdanja potrebno je usporediti ovu izjavu sa stvarnim kodom i
obrascem *Sigurnost podataka* u Google Play Consoleu. Testne snimke za README
snimaju se na izoliranom Android emulatoru i ne potječu s korisničkog uređaja.
