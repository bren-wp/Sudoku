<div align="center">

<img src="branding/sudoku-logo.svg" alt="Sudoku by Brendigo — logotip s brojevima 7, 3 i 9" width="620" />

### Više od igre. Vježba za um.

**Premium Sudoku za Android • Na hrvatskom • Bez interneta • Bez oglasa**

**Izdanje 1.0.31** · Android 8.0+ · Kotlin + Jetpack Compose

<img src="branding/sudoku-feature-graphic.svg" alt="Sudoku by Brendigo — plava neonska mreža, noćne planine i mjesečina" width="880" />

<br />

<a href="https://github.com/bren-wp/Sudoku/releases"><strong>Preuzmi najnovije izdanje</strong></a>
&nbsp;·&nbsp;
<a href="#-snimke-aplikacije">Pogledaj svih 7 ekrana</a>
&nbsp;·&nbsp;
<a href="#-što-sudoku-nudi">Mogućnosti</a>
&nbsp;·&nbsp;
<a href="#-razvoj-i-testiranje">Za razvojne programere</a>

</div>

---

## 🧩 Sudoku by Brendigo

Upoznaj klasičnu logičku igru u suvremenom neonsko-plavom izdanju.
**Sudoku** kombinira 4×4, 6×6 i 9×9 mreže, šest razina, dnevne izazove,
napredak i postignuća — sve lokalno na uređaju, bez registracije.

<table>
<tr>
<td width="28%" align="center">
<img src="branding/sudoku-icon.svg" alt="Originalna ikonica Sudoku 7–3–9" width="180" />
<br /><strong>Originalan 7–3–9 vizualni identitet</strong>
</td>
<td>

**Zašto igrati?**

- 🔹 **Tri veličine mreže** — 4×4, 6×6 i 9×9.
- ⚡ **Šest razina** — od Vrlo lako do Majstor.
- 🗓️ **Dnevni izazovi** — jedinstvena zagonetka za svaki datum.
- 🏆 **24 postignuća** — stvarne nagrade i praćenje napretka.
- 🧠 **Pomoć pri učenju** — bilješke, savjeti, poništavanje, označavanje brojeva.
- 🔒 **Privatnost** — bez računa, oglasa, praćenja i internetske dozvole.

</td>
</tr>
</table>

## 📱 Snimke aplikacije

Ovdje su **stvarne snimke funkcionirajuće Android aplikacije** iz automatskog
UI testiranja (941 × 1672 px), smanjene radi bržeg učitavanja stranice.
Nisu prikazane statične imitacije sučelja niti izmišljeni rezultati igrača.
Ilustrirane dizajnerske reference i dalje su cilj daljnjeg 1:1 usklađivanja.

<table align="center">
<tr><th>Početni ekran</th><th>Nova igra</th><th>Sudoku ploča</th></tr>
<tr>
<td><a href="docs/screenshots/01-home.webp"><img alt="Početni ekran: nastavak igre, izazovi i nagrade" src="docs/screenshots/01-home.webp" width="230" /></a></td>
<td><a href="docs/screenshots/02-new-game.webp"><img alt="Odabir veličine i težine nove Sudoku igre" src="docs/screenshots/02-new-game.webp" width="230" /></a></td>
<td><a href="docs/screenshots/03-game.webp"><img alt="Sudoku igra s mrežom, bilješkama i numeričkom tipkovnicom" src="docs/screenshots/03-game.webp" width="230" /></a></td>
</tr>
<tr><th>Dnevni izazov</th><th>Statistika</th><th>Postignuća</th></tr>
<tr>
<td><a href="docs/screenshots/05-daily.webp"><img alt="Dnevni izazov s prikazom tjednog niza i nagrade" src="docs/screenshots/05-daily.webp" width="230" /></a></td>
<td><a href="docs/screenshots/06-statistics.webp"><img alt="Statistika: dovršene igre, grafikon i postignuća" src="docs/screenshots/06-statistics.webp" width="230" /></a></td>
<td><a href="docs/screenshots/07-achievements.webp"><img alt="Kartice otključanih i zaključanih postignuća" src="docs/screenshots/07-achievements.webp" width="230" /></a></td>
</tr>
<tr><th colspan="3">Postavke</th></tr>
<tr><td colspan="3" align="center"><a href="docs/screenshots/04-settings.webp"><img alt="Postavke: tema, bilješke, provjera pogrešaka i privatnost" src="docs/screenshots/04-settings.webp" width="230" /></a></td></tr>
</table>

## ✨ Što Sudoku nudi

| Igra i način rada | Pojedinosti |
| --- | --- |
| Veličine ploče | **4×4, 6×6 i 9×9**, s provjerom jedinstvenog rješenja |
| Razina težine | **Vrlo lako, Lako, Srednje, Teško, Ekspert, Majstor** |
| Pomoć pri igranju | Bilješke, savjet, brisanje, poništavanje, isticanje brojeva |
| Osobni napredak | Povijest, najbolje vrijeme, rezultati i niz |
| Izazovi | Dnevna zagonetka vezana uz lokalni datum |
| Nagrade | 24 postignuća i bodovi |
| Pristupačnost | Opisi interaktivnih elemenata i prilagodljiv raspored |
| Povezivost | Potpuno offline, bez registracije |

> **Napomena o težini:** razine su trenutačno temeljene na količini uklonjenih
> početnih brojeva. Nisu zasebno ocijenjene naprednim solverom.

## ⬇️ Preuzimanje i instalacija

Najnoviji APK i Android App Bundle nalaze se na
**[GitHub Releases](https://github.com/bren-wp/Sudoku/releases)**.
Bez konfiguriranih izdavačkih ključeva, objavljeni APK je **testni APK**, a AAB
**nepotpisan**. Takvi artefakti nisu produkcijski potpisana Google Play izdanja.

**Android 8.0+** (API 26) · Paket `com.brendigo.sudoku` · Izdavač **Brendigo**.

## 🛠️ Razvoj i testiranje

Izvorni kod: **Kotlin + Jetpack Compose**, Android SDK 36, JDK 17, Gradle 8.13.
Reproduciranje provjera lokalno:

```bash
gradle :app:testDebugUnitTest
gradle :app:lintDebug
gradle :app:assembleDebug
gradle :app:bundleRelease
```

GitHub Actions testira i snima sedam ekrana na **941×1672** i kompaktnom
**720×1280** emulatoru. Snimke u ovoj galeriji nastaju automatski iz stvarne
aplikacije i mogu se osvježiti postupkom
[`marketing-screenshots.yml`](.github/workflows/marketing-screenshots.yml).

**Vizualna 1:1 jednakost još nije potvrđena.** Rezultati i preostale razlike
prate se u [issueu #11](https://github.com/bren-wp/Sudoku/issues/11).
Prolazak funkcionalnih UI testova ne znači nultu pikselnu razliku.

Za tehnički dizajn vidi [docs/DESIGN.md](docs/DESIGN.md), za usporedbe
[docs/pixel-qa.md](docs/pixel-qa.md), a za promjene
[CHANGELOG.md](CHANGELOG.md).

## 🔐 Privatnost i sigurnost

Napredak i postavke spremaju se lokalno u privatnom prostoru aplikacije.
Nema korisničkog računa, oglasa, analitike ni mrežnih dozvola.
Detalji su u dokumentu [PRIVACY.md](PRIVACY.md). Privatne ključeve
za potpisivanje treba držati u zaštićenim GitHub Actions tajnama; nikad u repozitoriju.

---

<div align="center">

**Sudoku by Brendigo** · *Bistriji um. Bolji dan.*

<img src="branding/sudoku-logo.svg" alt="Sudoku by Brendigo" width="320" />

[Izdanja](https://github.com/bren-wp/Sudoku/releases) ·
[Prijava problema](https://github.com/bren-wp/Sudoku/issues) ·
[Licencija](LICENSE)

</div>
