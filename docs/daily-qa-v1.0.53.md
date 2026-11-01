# Sudoku v1.0.53 — Dnevni izazov, iteracija 5/8

## Izvor, stvarni emulator i pravila usporedbe

Izvorni vlasnikov PNG `Slika ChatGPT 24. lis 2026. 20_47_18-7.png`, referentna kopija `references/05-daily.png`, **941×1672**. Podloga i slike koriste se isključivo za mjerljivu provjeru, nikad kao zamjena za interaktivni Compose zaslon. U izvornom konceptu su 28. travnja 2025., 7 dana u nizu i pokretanje izazova; pravi uređaj mora prikazati svoj **stvarni** datum, niz, spremanje i kontekst gumba.

- Prije: stvarni Android screenshot `05-daily.png` iz [Android visual smoke 41360418869](https://github.com/bren-wp/Sudoku/actions/runs/41360418869), izdanje 1.0.52, commit `fc9b8e6c2c44ac4f5e2dc8e3b143406cb40b0270`.
- Poslije: stvarni Android screenshot `05-daily.png` iz [Android visual smoke 41362054376](https://github.com/bren-wp/Sudoku/actions/runs/41362054376), commit `e716bd035bed55b61175eb573d58828fe016df3f`.
- Mjera: `RGB MAE` po RGB kanalu od 0 do 255 na apsolutno istim fiksnim koordinatama, bez preskaliranja, premještanja, zamjene ili uklanjanja dinamičkih područja. Manje znači bolji globalni pixel-diff, a ne funkcionalni dokaz 1:1.

| Referentno područje, (x0,y0,x1,y1) | Prije | Poslije | Promjena |
|---|---:|---:|---:|
| Cijeli zaslon (0,0,941,1672) | 23.640022 | **23.559421** | −0.080601 |
| Zaglavlje (0,0,941,273) | 25.553670 | 25.553670 | 0 |
| Datum/nagrada (30,270,900,475) | 23.226612 | 23.226612 | 0 |
| Hero (30,480,900,900) | 26.602302 | 26.602302 | 0 |
| Prizor (480,480,890,747) | 23.286818 | 23.286818 | 0 |
| Metrike (57,745,891,872) | 26.911010 | 26.911010 | 0 |
| Tjedni niz (30,900,895,1185) | 17.146652 | 17.146652 | 0 |
| Akcija (110,1185,820,1315) | 29.888400 | 29.888400 | 0 |
| Lokalna rang-lista (180,1310,755,1425) | 40.670336 | **39.559330** | −1.111006 |
| Krajolik (0,1440,941,1672) | 22.056664 | 22.056664 | 0 |

## Odbijena vizualna regresija

Prvi commit [1eb130f](https://github.com/bren-wp/Sudoku/commit/1eb130fc8ead31258a0c25b4bc777cf987dfb263) pomaknuo je planinu i mjesec kroz širi dio kartice. Stvarni snimak sa [CI 41361495951](https://github.com/bren-wp/Sudoku/actions/runs/41361495951) dokazao je pogoršanje: 23.640→23.759 ukupno, a umjetnički dio 23.287→25.214. Commit [e716bd0](https://github.com/bren-wp/Sudoku/commit/e716bd035bed55b61175eb573d58828fe016df3f) vratio je izvorne proporcije krajolika i promijenio **samo** obrub offline rang-liste u izvorno plavlje osvjetljenje. Konačni screenshot potvrdio je niži RGB MAE bez pomicanja drugih fiksnih regija.

## Funkcionalni i CI status

- [Android CI 41362054311](https://github.com/bren-wp/Sudoku/actions/runs/41362054311): **SUCCESS** (unit, Python QA, lint, APK/AAB build).
- [Android visual smoke 41362054376](https://github.com/bren-wp/Sudoku/actions/runs/41362054376): **SUCCESS**, stvarne snimke emulatora **941×1672** i **720×1280**.
- Stvarni kalendar, odrađeni dani, sedmodnevni niz, nagradni medaljon, aktivni daily gumb, izbor 9×9, lokalna osobna rang-lista, no-network dizajn i sigurnost napretka nisu promijenjeni.
- Testni release mora koristiti `1.0.53` / `versionCode 54`; nepotpisan AAB **nije Play-ready**. Provjeriti izdavački workflow za konačni commit.

## Preostale razlike

**Pixel-perfect 1:1 nije postignut**: stvaran datum i dnevni niz odstupaju od ilustrativnih vrijednosti, hero umjetnički prikaz još se razlikuje, a original prikazuje kraći natpis gumba nego aplikacija sa spremljenom partijom. Reference su dizajnerski cilj, ne lažni podaci. Sljedeća iteracija: **Statistika**.
