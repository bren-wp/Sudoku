# Sudoku • Brendigo — design implementation notes

Reference images are product-art mockups provided by the repository owner. Each screen should retain the brand and layout hierarchy without baking buttons, labels or puzzle digits into full-screen pictures.

## Visual tokens

| Token | Hex |
| --- | --- |
| Midnight base | `#020B27` |
| Card surface | `#06183E` |
| Bright blue | `#0764FA` |
| Neon cyan | `#11D9FA` |
| Teal | `#00E4C2` |
| Gold | `#FFD058` |
| Text | `#F5F9FF` |
| Secondary text | `#B0C8EF` |

## Screens

1. Splash/system launch theme and icon
2. Home / resume, new game, daily, statistics, achievements, settings
3. New game: grid 9×9 / 6×6 / 4×4, six difficulties
4. In-game board with stateful controls, mistakes, clock, progress
5. Daily challenge
6. Statistics
7. Achievements
8. Settings

All screens use live Compose elements and are vertically scrollable; Sudoku grid remains square and responds to device width. The original landscape layers are drawn with Canvas instead of using the supplied reference images as non-interactive layouts.

## Next validation gates

- Pixel-by-pixel visual comparison against owner-supplied images on reference portrait size (not yet complete)
- Dynamic font scale 1.0–2.0; narrow 320 dp and wide 600 dp
- TalkBack content descriptions for individual grid cells and controls
- Large-screen orientation, foldables, contrast and reduced-motion setting
- Run Android CI checks, verify APK installations on actual devices
- Signed release and Google Play review with publisher credentials
