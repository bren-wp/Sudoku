# Sudoku v1.0.48 — dodatna provjera zaslona Splash i početnog brendinga

## Opseg

Nastavak na **postojećoj grani PR-a #55**, bez promjene arhitekture, aplikacijskog ID-a, formata lokalne pohrane, Gradle/AGP verzija ili dozvola.

Originalne reference korisnika ostaju izvor istine:
- `08-splash.png`, **941×1672 px**;
- `09-wordmark.png`, **1254×1254 px**;
- `10-launcher-icon.png`, **1254×1254 px**.

Originalne PNG datoteke nisu uključene u repozitorij; nema osnove za tvrdnju da su poslije ovog commita postignuti pikseli 1:1. Ranije zabilježeni **Splash RGB MAE 13.692** u `docs/splash-qa-v1.0.41.md` jest povijesni baseline i nije rezultat ove iteracije.

## Izmjene izvornog koda

1. `SplashPage` zadržava dimenzije i tekst iz reference na referentnoj rezoluciji pri standardnom fontu: logotip **122dp**, naslov **79sp** i polazni vertikalni offset **25.5%** visine.
2. Uži uređaji (širina ispod 385dp) i niži zasloni (visina ispod 700dp) koriste kompaktni raspored; naziv i slogan ostaju stvaran Compose tekst.
3. Kod povećanog fonta preko 135% naslov zadržava čitljivost bez horizontalnog prelijevanja. Dugačko slovkano geslo prelazi u kompaktni puni tekst na malom zaslonu ili pri većem fontu.
4. `splash-brand`, `splash-logo`, `splash-title`, `splash-byline`, `splash-tagline` i `splash-loading` označavaju stvarne Compose elemente za automatizirano mjerenje geometrije.
5. Android 12+ koristi istu lokalnu 7–3–9 vektorsku ikonu i tamnu launcher pozadinu u **sustavnom** prikazu pokretanja. Na API-jevima ispod 31 ostaje izvorna tema.
6. Nije dodana mrežna ovisnost, vanjski servis niti novi pristup korisničkim podacima.

## Instrumentacijski testovi

- `captureRealSplashScreenForReferenceQA` — stvarna `08-splash.png` iz aplikacije, ne referentni bitmap prikazan kao UI.
- `splashBrandDoesNotOverlapLoadingIndicatorAtLargeFontScale` — **1.8×** font, provjera položaja i granica za logotip, naslov, byline, slogan i loader.
- `android12SystemSplashUsesSudokuBrandedIcon` — provjera teme i lokalnog resursa na API 31+.
- `settingsButtonsAreVisibleAndFunctional` i ostatak Android UI suitea ostaju aktivni kao regresijska provjera prethodnih zaslona.

## Provjera prije tvrdnje 1:1 i spajanja PR-a

- Pokrenuti `Android CI` i `Android visual smoke` za aktualni HEAD; **oba** emulatora (941×1672 i 720×1280) moraju proći.
- Provjeriti `Refresh Sudoku marketing screenshots` i koristiti samo stvarne izlazne PNG/WebP artefakte.
- Preuzeti stvarni `08-splash.png` i usporediti ga s izvornim PNG-om `scripts/pixel_compare.py --include-splash --allow-imperfect`, uz pojedinačno mjerenje logotipa, teksta, gornje mreže, pejzaža i spinnera.
- Samo uz vlasnikove stvarne reference dodatno usporediti `09-wordmark.png` i `10-launcher-icon.png`; izvoz aplikacijskih resursa nikad ne smije biti kopirana referenca.
- Provjeriti portrait/landscape, 320dp širinu, font scale 1.0–2.0 te sistemski splash uživo na Androidu 12–16.

## Status

Promjene su commitane na otvoreni PR. **Nisu potvrđeni 1:1 pikseli, rezultat novog CI-a, Google Play potpis ni novo izdanje.** Naredni dio po redu: samostalni wordmark, adaptive/monochrome launcher ikona i usporedba sa zadnje dvije originalne reference.
