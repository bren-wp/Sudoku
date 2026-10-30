# Sudoku v1.0.48 — Statistika, završni vizualni prolaz 6/10 u PR-u #55

## Izvorne datoteke i provjerena povijesna usporedba

- **Original korisnika:** `Slika ChatGPT 24. lis 2026. 20_47_19-8.png`, 941 × 1672 px, SHA-256 `64c2f4e6c090bbd693801a965fdeb46dbde02fb22dbae06b7a288e6b0523dafa`. Nije precrtana ni generirana zamjenska slika.
- **Stvarni Android emulator, PRETHODNO stanje:** `06-statistics.png` iz [uspješnog visual smoke runa 41132281271](https://github.com/bren-wp/Sudoku/actions/runs/41132281271), commit `91db54bba5e156df3a2db5e81731c010eb653b6f` (prethodna razvojna grana v1.0.47). SHA-256 `986288b19b46091843236a4e9364fb431e865ebbd779c42784374e1b0044c9cd`.
- Usporedba je izvršena na neizmijenjenim RGB slikama **iste rezolucije**, bez automatskog poravnanja, crop-a ili ponovnog prikaza dizajna kao lažne Android aplikacije.

| Fiksna regija (x0,y0,x1,y1) | Povijesni RGB MAE 0–255 |
|---|---:|
| Cijeli kadar (0,0,941,1672) | **26.637** |
| Zaglavlje (0,0,941,185) | 23.013 |
| Četiri metričke kartice (30,185,909,550) | **33.797** |
| Grafikon i krug dovršenosti (30,550,909,1030) | 27.582 |
| Povijest igara (30,1040,909,1320) | 20.154 |
| Četiri postignuća (30,1320,909,1550) | **30.698** |
| Donja pozadina (0,1550,941,1672) | 25.652 |

Prethodni `docs/statistics-qa-v1.0.46.md` ima *drukčiji* raniji baseline iz v1.0.45 (MAE 29.193). Nemoj miješati rezultate dviju emulatorskih verzija. Nijedna ovdje navedena brojka **nije** rezultat novog commita.

## Koje se razlike smiju korigirati, a koje ne?

Original je konceptualno **popunjen**: npr. 124 odigrane igre, 92% dovršenosti, rezultat 18:42:10, tjedni niz 7, četiri ilustrativne povijesne igre i nagrade. Stvarni emulator ima manje odigranih partija i dovršenost 0%. **Aplikacija mora zadržati stvarne vrijednosti iz lokalne pohrane.** Pixel MAE predstavlja i legitimne razlike podataka: zbog njih se ne smiju ugrađivati lažne igre, postoci, evidencije ni lažni otključani trofeji.

Zadržavaju se stvarni grafikoni, zaključana stanja postignuća, sažetak, povijest i svi Compose interaktivni elementi. Treba usporediti pozicije, boje, radijuse, hijerarhiju, kontraste i pristupačnost uz deterministički QA profil podataka kada je moguć, odvojeno od stvarnih korisničkih podataka.

## Izmjene u postojećem PR-u

1. `Pages.kt`: kartice četiri postignuća izdvojene su u zajedničku stvarnu Compose komponentu `StatsAwardPreview`. Svaka koristi svoju zasebnu nijansu (tirkizna, plava, ljubičasta, cijan) čak i kad je postignuće **zaključano**; ključ i pravi napredak ostaju vidljivi. Prije su sve zaključane kartice dobivale generički plavi okvir i izgledale jednolično.
2. Velika referentna širina zadržava jedan red od **četiri** kartice. Na uskim uređajima ili pri povećanom fontu postignuća su u **2 × 2** mreži; svaki element ima stabilan `stats-award-preview-0` … `stats-award-preview-3` tag, a roditelj `stats-award-grid`.
3. Visina kartica jamči najmanje 84dp na uobičajenom fontu i najmanje 100dp pri pristupačnom povećanju fonta, bez odbacivanja opisa ili lažiranja vrijednosti.
4. Dijalozi za objašnjenja metrika i povijest sada koriste `rememberSaveable` kako bi ostali otvoreni nakon promjene orijentacije/rekreacije aktivnosti; izvor statistike i dalje je `SudokuController`.
5. Stvarni postotak dovršenosti dobio je `stats-completion-percent` tag; prazno stanje povijesti dobilo je `stats-history-empty`, ali se povijest **ne popunjava izmišljenim unosima**.
6. Instrumentacijski test `statisticsAwardPreviewGeometryWorksAcrossBothViewports` provjerava četiri kartice, raspored 4 ili 2×2, nepreklapanje, čitljivost, indikator postotka i funkcioniranje povijesti igara.
7. `scripts/statistics_pixel_qa.py` i regresijski testovi uspoređuju original s Android PNG-om preko sedam nepromjenjivih izvornokoordinatnih regija; stvaraju `regions.csv`, `overlay.png` i `difference.png` te odbijaju pretvaranje 720×1280 u 941×1672.

## Ponovljiva provjera aktualne verzije

Nakon stvarnog prolaza Android CI-ja i dva emulatorska posla **za isti aktualni HEAD**, preuzeti novi `sudoku-emulator-screen-references` artefakt. Original bez izmjena spremiti kao `references/06-statistics.png`, a **novi** stvarni screenshot kao `screenshots/06-statistics.png`.

```bash
python scripts/statistics_pixel_qa.py \
  --reference references/06-statistics.png \
  --actual screenshots/06-statistics.png \
  --output-dir qa-pixel-diff/statistics \
  --allow-imperfect
```

Proći kompletan `regions.csv`, `overlay.png`, `difference.png`, Android instrumentirane testove na **941×1672** i **720×1280**, scenarij s nulom odigranih igara, scenarij s ispravnim povijesnim podacima, dijalog metrika i povijesti te otvaranje svih 24 postignuća. Ne navoditi konačni MAE niti tvrditi 1:1 dok novi stvarni screenshot nije preuzet i izmjeren.

**Status:** poboljšanja spremljena u isti PR #55; **aktualni 1:1 i CI tek trebaju biti potvrđeni**. Nije objavljena nova verzija niti izmijenjen ključ, paket, lokalna baza, internetske dozvole ili Gradle konfiguracija.

**Sljedeći pojedinačni zaslon: 7/10 — Postignuća.**
