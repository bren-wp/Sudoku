# Sudoku v1.0.52 — Gameplay, zaslon 4/8

## Izvor i reproducibilne snimke

Originalni vlasnikov referentni PNG: `Slika ChatGPT 24. lis 2026. 20_47_17-6.png`, odnosno `03-game.png`, 941×1672. Original nikada nije umetnut kao aktivno korisničko sučelje.

- Prije: `03-game.png` iz [Android visual smoke 41358649146](https://github.com/bren-wp/Sudoku/actions/runs/41358649146), izdanje 1.0.51, izvorni SHA `afdca9413ddf6f2bb8151c2077b7b80f38d79869`.
- Poslije: `03-game.png` iz [Android visual smoke 41359855322](https://github.com/bren-wp/Sudoku/actions/runs/41359855322), UI commit `6483625b98971dd8f527c1b0b5a035b1a4109347`.
- Metrika: RGB mean absolute error svih kanala na skali 0–255, točno ista rezolucija, bez cropanja, reskaliranja ili maskiranja dinamičkog sadržaja. Manje je bolje, ali nula nije nužan funkcionalni kriterij kada se stvarni brojevi partije razlikuju.

| Fiksna ROI regija | Prije | Nakon | Promjena |
|---|---:|---:|---:|
| Cijeli zaslon (0,0,941,1672) | 19.42462 | **19.00803** | -0.41658 |
| Zaglavlje (0,0,941,150) | 23.35356 | **21.23357** | -2.11999 |
| HUD (0,140,941,250) | 23.80307 | 23.80307 | 0 |
| Ploča (40,250,890,1110) | 17.67285 | **17.24404** | -0.42881 |
| Gornji dio ploče (40,250,890,680) | 17.64714 | 17.68775 | +0.04062 |
| Donji dio ploče (40,680,890,1110) | 17.69857 | **16.80033** | -0.89824 |
| Alati (38,1100,899,1250) | 22.96020 | **22.62929** | -0.33091 |
| Tipkovnica (40,1240,900,1485) | 18.63386 | 18.64424 | +0.01038 |
| Krajolik (0,1490,941,1672) | 23.82817 | 23.82817 | 0 |

Poboljšanje ukupne sirove metrike ~2.14%. Očuvani su stvarni nizovi brojeva, vrijeme, napredak, dovršavanje, bilješke, savjet, poništavanje, brisanje i lokalna pohrana. Ploča i brojčanik nisu zamijenjeni referentnom slikom.

## Testni pad i ispravak

- Početni commit [a63ac77](https://github.com/bren-wp/Sudoku/commit/a63ac7794c15cb1322a28b45b45ae6930ad538cd) pomaknuo je zaglavlje i pokušao osloboditi prostor za Bilješke.
- Commit [304bd26](https://github.com/bren-wp/Sudoku/commit/304bd261b21fa1f608dfd27e3a1c1f39b6ff2c63) uveo je provjeru nepreklapanja. [Test 41359362757](https://github.com/bren-wp/Sudoku/actions/runs/41359362757) je pošteno **pao** na obje rezolucije: semantički pravokutnici naslova i statusa presijecali su se.
- Commit [6483625](https://github.com/bren-wp/Sudoku/commit/6483625b98971dd8f527c1b0b5a035b1a4109347) zamijenio je prethodno preklapajuće slojeve odvojenim redcima ikonice, naziva i prekidača. Nije zaobišao test.
- [Android CI 41359855285](https://github.com/bren-wp/Sudoku/actions/runs/41359855285): **SUCCESS** (unit, Python pixel QA regresije, lint, APK/AAB build).
- [Android visual smoke 41359855322](https://github.com/bren-wp/Sudoku/actions/runs/41359855322): **SUCCESS** na 941×1672 i 720×1280, uključujući test semantičkog nepreklapanja.
- Završni izdavački commit v1.0.52 zahtijeva zaseban status. Izdavački privatni ključ nije pohranjen u repozitoriju.

## Preostale razlike

Doslovna pixel-perfect 1:1 jednakost još nije postignuta. Dinamičko stanje ploče, tipografija brojeva, HUD, pozadinski pejzaž i mali detalji brojčanika ostaju mjerljivo različiti. Ne lažirati primjer partije iz reference.

**Sljedeća iteracija:** Dnevni izazov.
