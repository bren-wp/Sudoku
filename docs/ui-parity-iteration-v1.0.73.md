# Sudoku v1.0.73 — stvarna geometrija i provjera 1:1

## Izvori i metodologija

Deset izvornih vlasnikovih PNG-ova od 24.10.2026. provjereno je SHA-256 otiscima u `scripts/final_ten_reference_qa.py`. Originali su privatni i nisu dodani u javni repozitorij. Početno mjerenje napravljeno je prema stvarnim Android snimkama v1.0.72 iz GitHub Actions (941×1672 za osam zaslona, 1254×1254 za branding). Naslovnica se uspoređuje sa **01-home-resume.png** zato što original sadrži spremljenu partiju.

Ovaj ciklus mijenja samo **izvorne Compose komponente i njihove dimenzije**, ne umeće slike kontrola ni fiktivni korisnički napredak. Zaslon mora i dalje raditi na malim telefonima, landscape položaju, tabletima i povećanim sistemskim fontovima.

## Konkretni korektivni zahvati

| Zaslon | Mjereni problem na referentnom 941px prikazu | Korektivni Compose zahvat |
|---|---|---|
| Naslovnica | Gumb Nastavi oko 20px preširok na smanjenom vizualnom pregledu od 425px | `fillMaxWidth(.52f)` → `.47f`; visina i klik ostaju |
| Statistika | Prazni Nedavni napredak zatvara se oko 10px prerano i pomiče Postignuća prema gore | `history.empty minHeight` 140 → 152dp; stvarni podatci ostaju iz kontrolera |
| Postignuća | Posljednji istaknuti red kartica završava oko 11px prenisko | Standardni četvrti red 137 → 125dp; ostali redovi i kompaktne fleksibilne kartice nepromijenjeni |
| Dnevni izazov | Ispod tri metrika hero kartice suvišan prazan prostor; tjedni red i gumb vizualno niže | Donji padding standardnog hero sadržaja 12 → 3dp, razmak 32 → 30dp, tjedni minimum 122 → 126dp |
| Splash | Tekst `by Brendigo` ~10px, tagline ~8px niže od originalnog izgleda u 425px pregledu | Mali kontrolirani pomaci `-2dp` bylinea i taglinea u standardnom portraitu, bez dodira s naslovom ili loaderom. Veći pomaci `-11dp`/`-8dp` dokazano su pali na Android testu zbog preklapanja semantičkih granica te su odbačeni |

*Koordinate u srednjem stupcu vizualno su procijenjene iz usporednog prikaza 941px originala i Android PNG-a smanjenog na istih 425px **samo za ručni pregled**. QA mjerenje i izvještaj uvijek koriste originalne 941/1254px datoteke bez interpolacije. Provjera na novih 10 snimki potvrđuje je li MAE zaista bolji; smanjenje razmaka nije dokaz 0 %.*

## Početna mjerljiva odstupanja v1.0.72

| Referenca | RGB MAE (0–255) |
|---|---:|
| 01 Home/Resume | 24,115176 |
| 02 Nova igra | 22,622370 |
| 03 Gameplay | 20,086813 |
| 04 Postavke | 19,422018 |
| 05 Dnevni izazov | 24,779138 |
| 06 Statistika | 26,583405 |
| 07 Postignuća | 29,862714 |
| 08 Splash | 10,384231 |
| 09 Wordmark | 18,833920 |
| 10 Launcher | 16,174683 |

**Početni strogi rezultat 0/10**. MAE je prosječna razlika po RGB kanalu na skali 0–255, ne postotak dizajnerskog odstupanja. Drugačiji datum, zagonetka, ukupan broj partija i otključana postignuća ne smiju se lažno namještati zbog screenshot QA. Čak i nakon poravnanja geometrije pikselna jednakost nije automatski 1:1.

## Testni uvjeti i otvoreni poslovi

- Android unit + instrumentacijski testovi, lint, svi 11 emulatorskih profila (reference, compact, narrow, landscape, tiny, medium, tablet capture, shard 0–3).
- Aktualni CycloneDX SBOM i 100 % online OSV provjera točnih Maven verzija; R8 unused-symbol analiza, stvarni APK permission pregled.
- Po završetku builda: ponoviti stvarnu 10-PNG usporedbu sa SHA-256 izvornicima; usporediti MAE u području statičnih kartica i strogi exact RGB/RGBA. Usporedbe stanja moraju biti jednake. Ako neki izmijenjeni zaslon ima regresiju, korigirati je prije tvrdnje da je bolje.
- Za stvarnih **0 odstupanja** potrebno je uskladiti i sve tekstualne fontove, anti-aliasing, ikone, osvjetljenja i scenografiju te stvoriti usporedive korisničke podatke u **isključivo testnom** okruženju. Nikad ne upisivati demonstracijske rezultate u produkcijsku statistiku.

## Izdanje

`versionName=1.0.73`, `versionCode=74`. Ne proglašavati 1:1 i ne objavljivati release dok CI/regresije ne prođu. Ako publisher signing key nije konfiguriran, GitHub paket je testni APK + nepotpisani AAB (prerelease), uz SHA256SUMS.