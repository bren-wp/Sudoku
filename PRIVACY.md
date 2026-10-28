# Privatnost — Sudoku by Brendigo

**Ažurirano:** 28. listopada 2026. · Aplikacija za Android

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

## Sigurnosne kopije i gubitak podataka

Aplikacija u Android manifestu isključuje standardni sigurnosni backup i
izuzela je lokalnu pohranu iz Androidovih pravila za cloud backup i prijenos
između uređaja. **Napredak se trenutačno ne sinkronizira i ne može se vratiti
s poslužitelja.** Brisanje podataka aplikacije, deinstalacija, kvar ili zamjena
uređaja mogu trajno ukloniti taj napredak.

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
