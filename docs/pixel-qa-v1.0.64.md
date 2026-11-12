# Sudoku by Brendigo — Pixel QA / v1.0.64

## Provjerljive slike, ne generirane makete

Za usporedbu su korištene korisnikove originalne reference iz 24. listopada 2026. i stvarni Android PNG-ovi sa [screenshot workflowa #43126063674](https://github.com/bren-wp/Sudoku/actions/runs/43126063674), iz izvornog commita `0779a8fd41a5edaf3faa7cca4fd312f1f3bdd156`. Osam zaslona snimljeno je pri 941×1672 px, bez povećavanja ili prilagođavanja veličine. Marketinške WebP kopije objavljene su u `docs/screenshots`.

### Globalni RGB MAE, skala 0–255 (ništa nije izraženo u postocima)

| Zaslon | Prethodna v1.0.63 snimka | Snimka nakon dorada | Razlika |
| :--- | ---: | ---: | ---: |
| Naslovnica sa stvarnom spremljenom igrom | 24,1152 | 24,1152 | 0,0000 |
| Nova igra | 22,6224 | 22,6224 | 0,0000 |
| Igra | 20,2383 | 20,2529 | +0,0146 |
| Postavke | 19,4239 | 19,4239 | 0,0000 |
| Dnevni izazov | 24,7852 | 24,7777 | −0,0075 |
| Statistika | 26,5834 | 26,5834 | 0,0000 |
| Postignuća | 29,8627 | 29,8627 | 0,0000 |
| Splash | 10,3842 | 10,3842 | 0,0000 |

Važno: stvarno stanje emulatorske igre, datum, rezultati i postignuća razlikuju se od ilustrativnih korisnikovih referenci. Sirovi RGB MAE stoga uključuje razliku u sadržaju, a ne samo UI geometriji. Ne treba ga proglašavati postotkom usklađenosti ili dokazom 1:1 jednakosti. Pozitivan pomak RGB MAE ne znači nužno regresiju grafike, a mali negativan pomak nije garancija pixel-perfect izgleda.

### Sadržaj izmjena

- Dnevni tjedni niz: sedam stupaca jednakih širina, kružnice i datum prilagođavaju se gustoći dostupnog prostora; na referentnoj širini 941 px ostaje krug do 31 dp.
- Statistika: iznad 390 dp unutarnje širine prikazuju se 4 nagrade u nizu; u užem prostoru kartice prelaze u 2×2.
- Dodani su instrumentacijski testovi dodirnih područja, preklapanja i poravnanja stvarnih Compose komponenti. Renderirani screenshoti nisu zamjena za stvarnu igru.
- Hrvatska službena Google Play značka u README-u povezana je s `https://play.google.com/store/apps/details?id=com.brendigo.sudoku`. To je dokumentacijsko/marketinška izmjena, nije Android ekran.

### Preostali posao

Pojedinačni položaji ikona, tekstura i boja i dalje odstupaju od dizajnerskih izvornika, najviše na Postignućima, Statistici i Dnevnom izazovu. Pošteno je reći da je poboljšana responzivnost i provjerena funkcionalnost, ali **0% odstupanja i potpuna 1:1 sukladnost svih zaslona nisu potvrđeni**. Android visual smoke [#43126063768](https://github.com/bren-wp/Sudoku/actions/runs/43126063768) provjerava 11 emulatorskih profila; konačni status treba čitati izravno iz workflowa.
