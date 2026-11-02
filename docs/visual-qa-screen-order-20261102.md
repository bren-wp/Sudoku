# Sudoku by Brendigo — pregled ekrana, pixel QA, 2. 11. 2026.

## Referentni materijal (vlasnikova izvorna grafika)
- Ekrani: 941 × 1672 px, bez skaliranja i bez skrivenih status/navigacijskih traka.
- Logotip i launcher ikona: 1254 × 1254 px, provjera alfa-kanala.
- Redoslijed implementacije: **08 Splash → 01 Home → 02 Nova igra → 03 Igra → 05 Dnevni izazov → 06 Statistika → 07 Postignuća → 04 Postavke**.
- Svaka kontrola mora biti stvarna interaktivna komponenta Kotlin / Jetpack Compose, ne isječak ekrana s nevidljivim klikom.

## Trenutni prolaz (PR #58)
| Korak | Vizualni kriteriji | Provjera |
| --- | --- | --- |
| 1. Splash | Znak 7–3–9, neon rubovi i središnja 3D ćelija, pozicija logotipa, natpis, slogan, indikator i noćni krajolik | `captureRealSplashScreenForReferenceQA`; inkluzivni 1.8× font; `08-splash.png` |
| 2. Home | Header, kartica „Nastavi”, gumbi, mali prikazi Statistika/Postignuća/Postavke, tjedni niz | `captureRealHomeWithResumeCardForReferenceQA`; `01-home-resume.png`; stvarna pohrana i otvaranje igre |
| 3. Nova igra | Odabir 9×9/6×6/4×4, šest kategorija, obrubi 3D ikona, primarna akcija | `newGameActionsRemainLegibleOnEveryResponsiveProfile`, `02-new-game.png` |
| 4. Sudoku ploča | 9×9 linije i odabir, bilješke, savjet, poništi, izbriši, brojčane tipke, igra/pauza | funkcionalni UI suite i stvarni `03-game.png`; vizualno ponovno poravnanje još slijedi |
| 5. Dnevni izazov | Niz, zlatni prsten nagrade, stakleni obrubi, 3D sjaj gumba i akcije | Nativni Compose prolaz; `05-daily.png` za ponovnu emulatorsku usporedbu, 1:1 još nije potvrđeno |
| 6. Statistika | Grafikon, prsten, brojke, kartice, povijest | `06-statistics.png`; vizualno ponovno poravnanje još slijedi |
| 7. Postignuća | 2 stupca, transparentni/kartični 3D prikaz, napredak i zaključane nagrade | `07-achievements.png`; vizualno ponovno poravnanje još slijedi |
| 8. Postavke | 3D prekidači, radio/jezični odabir, informacije, privatnost, izgled | `04-settings.png`; vizualno ponovno poravnanje još slijedi |

## Poštena usporedba referentne Home slike
Vlasnikova Home slika prikazuje postojeću prekinutu igru. `01-home.png` na čistoj instalaciji nema taj element, što je **ispravno ponašanje aplikacije**, ali je neusporedivo kao pixel QA. PR #58 dodaje `01-home-resume.png`, snimljenu nakon pokretanja stvarne igre i povratka na Home, bez produkcijskih lažnih podataka. Za izmjerenu usporedbu:

```bash
python scripts/pixel_compare.py --reference-dir references --actual-dir screenshots --output-dir qa-pixel-diff --include-splash --home-resume --allow-imperfect
```

Odgovarajući `references/01-home.png` mora biti originalna korisnikova slika. Izvještaj čuva izvorno ime zaslona i novo ime stvarnog snimka; volatilni sadržaj (datum/niz/brojač) vrednovati odvojeno ili s jasno dokumentiranom maskom. Nikada ne prešutjeti punu RGB MAE vrijednost.

## Obvezni izlazni uvjeti
1. Android CI: unit testovi + pixel QA script testovi + lint + debug/release APK i AAB build.
2. Android visual smoke: reference, compact, narrow, landscape, tiny, medium, 4 tabletna sharda i tablet capture.
3. Usporedba *stvarnih* PNG-a, regionalna geometrija i RTL/accessibility/touch testovi.
4. Bez gubitka spremljenih igara, mrežnih ovisnosti, lažnih UI screenshotova ili izmjena certifikata za potpisivanje.
5. **Ne označiti 1:1 dovršeno** bez demonstriranih odgovarajućih rezultata i primarne funkcionalnosti svih gumba.

## Prolaz 3. 11. 2026. — dorade u PR-u #58
- Splash se pri prvom pokretanju prikazuje 2,5 s; zadržani su pravi Compose logotip, indikator i zaštita obnovljene navigacije.
- Dnevni izazov: suptilni stakleni sjaj kartica, višeslojni zlatni prsten, 3D gornji odsjaj primarnog gumba. Datum, napredak i svi gumbi ostaju stvarni i dinamični.
- Instrumentacijski test razlikuje 2+1 i tri stupca pomoću istog praga **355dp sadržaja** kao produkcijska komponenta, nakon margina od 18dp po strani.
- Potrebni su novi stvarni PNG snimci i instrumentacijski rezultati prije zaključka o pixel-perfect podudarnosti.

### Statistika — nativna 3D dorada (3. 11. 2026.)
- Uveden diskretan odsjaj gornjeg ruba statističkih kartica, svjetlosni halo oko četiri stvarne vektorske ikonice, obrubi stupaca i halo oko prstena dovršenosti.
- Brojke, grafikon, postotak, povijest i dodiri i dalje dolaze iz stvarnog stanja aplikacije. Sljedeći korak ostaje usporedba `06-statistics.png` s izvornom referencom na emulatorskim profilima.
