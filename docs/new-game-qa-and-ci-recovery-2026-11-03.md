# Sudoku — Nova igra / popravljanje tablet CI-ja (3. studenoga 2026.)

## Izvor i cilj

- Vlasnikova izvorna PNG referenca `02-new-game.png`: **941 × 1672 px**.
- Stvarna snimka Android emulatora prije ovih novih zahvata: `02-new-game.png` iz [Android visual smoke 41553737511](https://github.com/bren-wp/Sudoku/actions/runs/41553737511), profil 941 × 1672 px / 320 dpi.
- Početni prosjek RGB MAE za Nova igra (prije ovog prolaza): **22,35/255**. Taj broj ne smije se predstavljati kao rezultat nakon novih izmjena niti kao dokaz doslovnog 1:1.
- Scenski motiv i radio-kartice su nativne Compose/Canvas komponente; ništa interaktivno nije pretvoreno u sliku zaslona.

## Dijagnoza i ispravak pada PR #59

Pao je isključivo `Android UI (tablet-shard-3)` u runu `41553737511`:
`SudokuSceneryAssetTest.originalArtworkLoadsAndFadesIntoTheNativeBackdrop`, `expected:<236> but was:<941>`.

Razlog: slika `scenery-reference.webp` pravilno je zamijenjena kvalitetnijim lokalnim izrezom **941 × 647** px, a instrumentacijski test nastavio je zahtijevati dimenzije starog komprimiranog izreza **236 × 162**. Test sada provjerava **941 × 647**, zatim i dalje provjerava dekodiranje, alfa-kanal, prozirni gornji red te potpuno vidljiv donji kraj. Testne tvrdnje nisu uklonjene niti ignorirane.

Preostalih deset emulatorskih profila bilo je zeleno, a Android CI je prošao za prethodni SHA. Svi testovi za završni SHA moraju se zasebno provjeriti.

## Nova igra — usmjereni nativni pixel pass

1. Naslov `Nova igra`: odvojena bijela i cijan nijansa u istom stvarnom `Text` čvoru kroz `AnnotatedString` i `SpanStyle` (nije statičan bitmap).
2. Okrugle oznake šest težina: obojeni halo, podignut 3D obrub i svijetli rub, s postojećim vektorskim ikonama.
3. Pozadinski oblici kartica: diskretna osvijetljena Sudoku polja, izraženije mrežne linije, stakleni gornji rub i mala obojena sjena.
4. Odabir 9×9/6×6/4×4: osvijetljeni aktivni segment i gornja refleksija uz očuvanu `Role.RadioButton` semantiku.
5. `Započni igru`: 3D gradientni odsjaj i osvjetljenje bez diranja `onClick`, veličine dodira ili generatora zagonetki.

### Obvezni funkcionalni testovi

- `newGameActionsRemainLegibleOnEveryResponsiveProfile`
- `newGameDoesNotClipAtLargeAccessibilityFontScale`
- `fourAndSixCellGridModesArePlayable`
- `newGameCardGeometryRespectsBothEmulatorViewports`
- `newGameSetupSurvivesActivityRecreation`
- `originalArtworkLoadsAndFadesIntoTheNativeBackdrop`

Također moraju proći svi Gradle unit/lint/assemble i **11** UI profila u CI-ju. Ako padne bilo koja tvrdnja, PR nije spreman za merge.

## Sljedeće prihvaćanje

Iz stvarnog, novog emulator runa preuzeti `02-new-game.png`; usporediti s istim originalom bez resizea, maskiranja tipografije ili lažnih podataka. Zabilježiti globalni i regionalni RGB MAE te posebno provjeriti naslov, kartice, vektorske značke, 3D odsjaj i CTA. Bez mjerenja i zelenog CI-ja nema deklariranja pixel-perfect 1:1.

Nakon New Game slijedi **03-game**: stvarna 9×9 ploča, linije 3×3, status, srca, akcije `Bilješke`, `Savjet`, `Poništi`, `Izbriši`, tipke 1–9, stanja pauze i nastavka.
