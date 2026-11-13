package com.brendigo.sudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.sudoku.data.Screen
import com.brendigo.sudoku.data.SudokuController
import kotlinx.coroutines.launch

/** Consistent checked/unchecked weekly calendar marker for home and daily. */
@Composable
private fun StreakMarker(
    checked: Boolean,
    diameter: androidx.compose.ui.unit.Dp = 20.dp,
    dayLabel: String = "",
) {
    Box(
        Modifier.size(diameter)
            .semantics {
                contentDescription = "$dayLabel: " +
                    (if (checked) "dnevni izazov riješen" else "dnevni izazov nije riješen")
            }
            .background(
                if (checked) Ink.cyan.copy(alpha = .18f) else Color(0xFF061D48),
                RoundedCornerShape(50),
            )
            .border(
                1.5.dp,
                if (checked) Ink.cyan else Ink.blue.copy(alpha = .65f),
                RoundedCornerShape(50),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) BrandIcon("check", Modifier.size(diameter * .66f), Ink.cyan)
    }
}

/**
 * Decorative Sudoku board for the home resume card.
 * Canvas-only artwork: the underlying card and Compose action are real UI.
 */
@Composable
private fun ResumeBoardArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        // The reference keeps the decorative board behind the right half of
        // the card; no puzzle values or UI actions are ever baked into artwork.
        drawRect(
            Brush.horizontalGradient(
                listOf(Color.Transparent, Color(0x33010B27), Color(0x99010B27)),
                startX = w * .35f, endX = w,
            ),
        )
        fun boardPoint(col: Float, row: Float) = Offset(
            w * (.61f + col * .142f - row * .043f),
            h * (-.035f + row * .355f),
        )
        val lineColor = Color(0xFF238EFF).copy(alpha = .44f)
        for (line in 0..3) {
            val a = boardPoint(line.toFloat(), 0f)
            val b = boardPoint(line.toFloat(), 3f)
            drawLine(lineColor.copy(alpha = .20f), a, b, strokeWidth = 5f)
            drawLine(lineColor, a, b, strokeWidth = 2.0f)
            val c = boardPoint(0f, line.toFloat())
            val d = boardPoint(3f, line.toFloat())
            drawLine(lineColor.copy(alpha = .75f), c, d, strokeWidth = 2.0f)
        }
        val glow = Offset(w * .785f, h * .37f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Ink.cyan.copy(alpha = .27f), Color.Transparent),
                center = glow, radius = w * .15f,
            ),
            radius = w * .15f, center = glow,
        )
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                textSize = h * .145f
                typeface = android.graphics.Typeface.create(
                    android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL,
                )
                textAlign = android.graphics.Paint.Align.CENTER
            }
            listOf(
                Triple("5", .73f, .17f), Triple("3", .79f, .40f),
                Triple("1", .70f, .59f), Triple("6", .87f, .67f),
                Triple("8", .73f, .85f),
            ).forEach { (digit, x, y) ->
                paint.color = if (digit == "3")
                    android.graphics.Color.rgb(27, 204, 255)
                    else android.graphics.Color.rgb(81, 150, 245)
                paint.setShadowLayer(
                    if (digit == "3") 18f else 0f, 0f, 0f,
                    android.graphics.Color.rgb(0, 165, 255),
                )
                canvas.nativeCanvas.drawText(digit, w * x, h * y, paint)
            }
            paint.clearShadowLayer()
        }
    }
}

@Composable
fun HomePage(controller: SudokuController) {
    val scope = rememberCoroutineScope()
    val today = rememberToday()
    PageContent(
        topPadding = 22.dp,
        footerText = "B I S T R I J I   U M  ·  B O L J I   D A N",
    ) {
        Spacer(Modifier.height(14.dp))
        BrandHeader(Modifier.fillMaxWidth())
        Spacer(Modifier.height(0.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            Text(
                "V I Š E  O D  I G R E  ·  V J E Ž B A  Z A  U M",
                color = Ink.muted,
                fontSize = if (maxWidth < 375.dp) 9.sp else 10.sp,
                letterSpacing = if (maxWidth < 375.dp) 1.sp else 2.sp,
                modifier = Modifier.fillMaxWidth().testTag("home-slogan"),
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
        Spacer(Modifier.height(0.dp))
        val game = controller.game
        if (game != null && !game.completed && !game.failed) {
            NeonPanel(Modifier.fillMaxWidth().testTag("resume-game")
                .semantics { contentDescription = "Nastavi spremljenu Sudoku igru"; role = Role.Button }
                .clickable { controller.go(Screen.GAME) }
                .heightIn(min = 196.dp), highlight = Ink.blue) {
                // Decorative, non-interactive canvas behind the real resume action.
                // It recreates the illuminated Sudoku-board motif, not a bitmap UI.
                ResumeBoardArtwork(Modifier.matchParentSize())
                Column(Modifier.padding(start = 20.dp, top = 9.dp, end = 12.dp, bottom = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("N A S T A V I  I G R U", color = Ink.muted,
                        fontSize = 10.sp, letterSpacing = 2.sp)
                    Text("Prekinuta igra", color = Ink.white,
                        fontWeight = FontWeight.ExtraBold, fontSize = 23.sp)
                    Text(difficultyTitle(game.level),
                        color = Ink.muted, fontSize = 13.sp)
                    // Keep the reference single-line metadata on standard phones.
                    // At narrow widths, wrap the live empty-cell count below the timer
                    // instead of clipping it beneath the decorative Sudoku artwork.
                    BoxWithConstraints(Modifier.fillMaxWidth().testTag("home-resume-meta")) {
                        val stacked = maxWidth < 320.dp ||
                            LocalDensity.current.fontScale >= 1.3f
                        if (stacked) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    BrandIcon("timer", Modifier.size(16.dp), Ink.muted)
                                    Text(formatTime(game.elapsed),
                                        color = Ink.muted, fontSize = 12.sp)
                                }
                                Text("Još " + game.emptyCells + " praznih polja",
                                    color = Ink.muted, fontSize = 12.sp)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                BrandIcon("timer", Modifier.size(16.dp), Ink.muted)
                                Text(formatTime(game.elapsed), color = Ink.muted, fontSize = 12.sp)
                                Text("·  Još " + game.emptyCells + " praznih polja",
                                    color = Ink.muted, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    SolidAction("▶   Nastavi", { controller.go(Screen.GAME) },
                        Modifier.fillMaxWidth(.47f), minHeight = 42.dp)
                }
            }
        }
        if (game?.daily == true && controller.hasSavedRegularGame()) {
            NeonPanel(
                Modifier.fillMaxWidth().testTag("resume-classic")
                    .semantics { contentDescription = "Nastavi spremljenu klasičnu igru"; role = Role.Button }
                    .clickable { scope.launch { controller.resumeRegularGame() } }
                    .heightIn(min = 72.dp),
                highlight = Ink.teal
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    BrandIcon("grid", Modifier.size(28.dp), Ink.teal)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Nastavi klasičnu igru", color = Ink.white,
                            fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Subtitle("Vrati se na spremljenu Sudoku partiju")
                    }
                    BrandIcon("next", Modifier.size(20.dp), Ink.cyan)
                }
            }
        }
        // Keep the reference 2+3 card grid on full-size phones. Compact
        // widths cannot fit three readable tiles and their chevrons in one row.
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 300.dp) {
                // At narrow widths the longer Croatian title wraps. Give both
                // primary cards the same taller minimum, preserving a level row.
                val primaryHeight = if (maxWidth < 410.dp) 184.dp else 146.dp
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeTile("Nova igra", "Odaberi težinu\ni započni novu igru",
                        Ink.teal, Modifier.weight(1f).heightIn(min = primaryHeight)) {
                        controller.go(Screen.NEW_GAME)
                    }
                    HomeTile("Dnevni izazov", "Svaki dan nova\nzagonetka",
                        Ink.gold, Modifier.weight(1f).heightIn(min = primaryHeight)) {
                        controller.go(Screen.DAILY)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HomeTile("Nova igra", "Odaberi težinu\ni započni novu igru",
                        Ink.teal, Modifier.fillMaxWidth()) { controller.go(Screen.NEW_GAME) }
                    HomeTile("Dnevni izazov", "Svaki dan nova\nzagonetka",
                        Ink.gold, Modifier.fillMaxWidth()) { controller.go(Screen.DAILY) }
                }
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val secondaryTilesFitTwoColumns = maxWidth >= 300.dp
            if (maxWidth >= 370.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HomeTile("Statistika", "Prati svoj napredak",
                        Ink.blue, Modifier.weight(1f), compact = true) {
                        controller.go(Screen.STATS)
                    }
                    HomeTile("Postignuća", "Otkrij sve nagrade",
                        Ink.purple, Modifier.weight(1f), compact = true) {
                        controller.go(Screen.ACHIEVEMENTS)
                    }
                    HomeTile("Postavke", "Prilagodi igru",
                        Ink.muted, Modifier.weight(1f), compact = true) {
                        controller.go(Screen.SETTINGS)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (secondaryTilesFitTwoColumns) {
                        // Two columns remain legible at 300-369dp content
                        // widths. On very narrow phones each tile gets a
                        // full-width touch target and readable Croatian text.
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            HomeTile("Statistika", "Prati svoj napredak",
                                Ink.blue, Modifier.weight(1f), compact = true) {
                                controller.go(Screen.STATS)
                            }
                            HomeTile("Postignuća", "Otkrij sve nagrade",
                                Ink.purple, Modifier.weight(1f), compact = true) {
                                controller.go(Screen.ACHIEVEMENTS)
                            }
                        }
                    } else {
                        HomeTile("Statistika", "Prati svoj napredak",
                            Ink.blue, Modifier.fillMaxWidth(), compact = true) {
                            controller.go(Screen.STATS)
                        }
                        HomeTile("Postignuća", "Otkrij sve nagrade",
                            Ink.purple, Modifier.fillMaxWidth(), compact = true) {
                            controller.go(Screen.ACHIEVEMENTS)
                        }
                    }
                    HomeTile("Postavke", "Prilagodi igru",
                        Ink.muted, Modifier.fillMaxWidth(), compact = true) {
                        controller.go(Screen.SETTINGS)
                    }
                }
            }
        }
        NeonPanel(
            Modifier.fillMaxWidth().heightIn(min = 80.dp)
                .testTag("home-streak"),
            highlight = Ink.blue,
        ) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth < 370.dp) {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            BrandIcon("flame-solid", Modifier.size(34.dp), Ink.gold)
                            Column {
                                Text("Trenutni niz", fontSize = 10.sp, color = Ink.muted)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${controller.currentStreak(today)}",
                                        fontSize = 25.sp, color = Ink.gold,
                                        fontWeight = FontWeight.ExtraBold)
                                    Text(" dana", fontSize = 11.sp, color = Ink.muted)
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            BrandIcon("gift", Modifier.size(24.dp), Ink.gold)
                            Text("Sljedeća\nnagrada", fontSize = 10.sp,
                                color = Ink.muted, lineHeight = 12.sp)
                        }
                        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
                        val completed = controller.stats.completedDailyDates.toSet()
                        val weekdays = listOf("Pon", "Uto", "Sri", "Čet", "Pet", "Sub", "Ned")
                        val fullDays = listOf("Ponedjeljak", "Utorak", "Srijeda",
                            "Četvrtak", "Petak", "Subota", "Nedjelja")
                        Row(
                            Modifier.fillMaxWidth().testTag("home-week-days"),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            weekdays.forEachIndexed { index, day ->
                                val checked = monday.plusDays(index.toLong()).toString() in completed
                                Column(
                                    Modifier.testTag("home-weekday-$index"),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    StreakMarker(checked, diameter = 18.dp,
                                        dayLabel = fullDays[index])
                                    Spacer(Modifier.height(5.dp))
                                    Text(day, fontSize = 9.sp, color = Ink.muted)
                                }
                            }
                        }
                    }
                } else {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BrandIcon("flame-solid", Modifier.size(38.dp), Ink.gold)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Trenutni niz", fontSize = 10.sp, color = Ink.muted)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${controller.currentStreak(today)}", fontSize = 29.sp,
                            color = Ink.gold, fontWeight = FontWeight.ExtraBold)
                        Text(" dana", fontSize = 11.sp, color = Ink.muted)
                    }
                }
                Spacer(Modifier.width(9.dp))
                Row(
                    Modifier.weight(1f)
                        .testTag("home-week-days")
                        .drawBehind {
                            // The source reference includes a fine baseline
                            // beneath seven separate, real daily markers.
                            val y = 18.dp.toPx() + 5.dp.toPx()
                            drawLine(
                                Ink.line.copy(alpha = .60f),
                                Offset(size.width * .045f, y),
                                Offset(size.width * .955f, y),
                                strokeWidth = 1.dp.toPx(),
                            )
                        },
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
                    val completed = controller.stats.completedDailyDates.toSet()
                    val weekdays = listOf("Pon", "Uto", "Sri", "Čet", "Pet", "Sub", "Ned")
                    val fullDays = listOf("Ponedjeljak", "Utorak", "Srijeda",
                        "Četvrtak", "Petak", "Subota", "Nedjelja")
                    weekdays.forEachIndexed { index, day ->
                        val checked = monday.plusDays(index.toLong()).toString() in completed
                        Column(
                            Modifier.testTag("home-weekday-$index"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            StreakMarker(checked, diameter = 18.dp, dayLabel = fullDays[index])
                            Spacer(Modifier.height(6.dp))
                            Text(day, fontSize = 9.sp, color = Ink.muted)
                        }
                    }
                }
                Box(Modifier.width(1.dp).height(44.dp).background(Ink.line.copy(alpha = .60f)))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Box(
                        Modifier.size(31.dp)
                            .background(Ink.cyan.copy(alpha = .10f), RoundedCornerShape(50))
                            .border(1.3.dp, Ink.cyan, RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center,
                    ) {
                        BrandIcon("gift", Modifier.size(21.dp), Ink.gold)
                    }
                    Text("Sljedeća\nnagrada", fontSize = 9.sp,
                        color = Ink.muted, lineHeight = 11.sp, textAlign = TextAlign.Center)
                }
            }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

/**
 * Home-only illustrated card treatment. This is scalable, non-interactive
 * Canvas scenery underneath real Compose labels, buttons and accessibility.
 * In particular the gold panel keeps the reference's moonlit pavilion instead
 * of drawing a flat yellow disc behind the text.
 */
@Composable
private fun BoxScope.HomeTileArtwork(accent: Color, compact: Boolean) {
    // Comparison against the actual 941x1672 emulator proved that extra
    // compact-card halos increased regional RGB MAE. Preserve the original
    // restrained treatment and never hide a measured regression.
    if (compact) return
    // Reuse a bundled, text-free crop of the owner's reference mountains.
    // The Compose card and its navigation remain fully interactive.
    val context = LocalContext.current
    val mountainArt = remember(context) {
        runCatching {
            context.assets.open("achievement-gold-art.webp").use {
                BitmapFactory.decodeStream(it)?.asImageBitmap()
            }
        }.getOrNull()
    }
    Canvas(Modifier.matchParentSize()) {
        val w = size.width
        val h = size.height
        if (accent == Ink.gold) {
            drawRect(Brush.horizontalGradient(listOf(
                Color(0xFF27292D), Color(0xFF473E36), Color(0xFF80522A),
            )))
            mountainArt?.let { art ->
                // Subtle photographic texture is behind the original
                // native moon, mountains and illuminated pavilion.
                drawImage(
                    image = art,
                    dstOffset = IntOffset((w * .36f).roundToInt(), 0),
                    dstSize = IntSize((w * .64f).roundToInt(), h.roundToInt()),
                    alpha = .66f,
                    filterQuality = FilterQuality.High,
                )
            }
            // Staggered silhouettes and amber mist create the reference's
            // layered dusk atmosphere, with no text or UI baked into artwork.
            for (cloud in 0..14) {
                val cx = w * (.36f + (cloud * 29 % 65) / 100f)
                val cy = h * (.085f + (cloud * 17 % 39) / 100f)
                val radius = w * (.055f + (cloud % 4) * .011f)
                drawCircle(
                    Color(0xFFB47D42).copy(alpha = .075f + (cloud % 3) * .024f),
                    radius = radius,
                    center = Offset(cx, cy),
                )
            }
            val moon = Offset(w * .76f, h * .19f)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xAAFFCD65), Color.Transparent),
                    center = moon, radius = w * .37f,
                ), radius = w * .37f, center = moon,
            )
            drawCircle(Color(0xFFFFDD83), radius = h * .115f, center = moon)
            // Two hand-drawn mountain ridges are restricted to the card's
            // decorative right side, preserving contrast for navigation copy.
            for (ridge in 0..1) {
                val mountain = Path().apply {
                    moveTo(w * .41f, h)
                    lineTo(w * .41f, h * (.63f + ridge * .12f))
                    for (index in 0..28) {
                        val x = w * (.41f + index / 28f * .59f)
                        val wave = kotlin.math.sin(index * .55f + ridge) * .10f +
                            kotlin.math.sin(index * .19f + 2f * ridge) * .07f
                        lineTo(x, h * (.62f + ridge * .16f - wave))
                    }
                    lineTo(w, h)
                    close()
                }
                drawPath(mountain, if (ridge == 0) Color(0xB21B2535)
                    else Color(0xD10B1A29))
            }
            // A third foreground ridge anchors the tiny right-side temple.
            // The outlines are scaled to the card dimensions, including when
            // a tablet or landscape viewport expands the available width.
            val foregroundRidge = Path().apply {
                moveTo(w * .48f, h)
                lineTo(w * .48f, h * .74f)
                for (index in 0..32) {
                    val x = w * (.48f + index / 32f * .52f)
                    val height = .745f -
                        kotlin.math.sin(index * .28f + 1.1f) * .034f -
                        kotlin.math.sin(index * .92f + .6f) * .018f
                    lineTo(x, h * height)
                }
                lineTo(w, h)
                close()
            }
            drawPath(foregroundRidge, Color(0xB9091928))
            // Natural tree clusters on the reference's far right, behind the
            // interactive title/chevron. Pure Canvas decoration.
            for (tree in 0..9) {
                val x = w * (.73f + tree * .029f)
                val bottom = h * (.76f + (tree % 3) * .025f)
                val crown = h * (.40f + (tree * 7 % 16) * .012f)
                drawLine(
                    Color(0xF0081723), Offset(x, bottom), Offset(x, crown),
                    strokeWidth = w * .005f,
                )
                for (leaf in 0..4) {
                    val y = crown + (bottom - crown) * (.10f + leaf * .135f)
                    drawCircle(
                        Color(0xC6081622),
                        radius = w * (.014f + (tree % 3) * .005f),
                        center = Offset(x + w * ((leaf % 2) * .018f - .009f), y),
                    )
                }
            }
            val px = w * .89f
            val py = h * .80f
            val u = w * .034f
            drawRect(Color(0xFF05142A),
                topLeft = Offset(px - u, py - u * 3f),
                size = androidx.compose.ui.geometry.Size(u * 2f, u * 3f))
            for (tier in 0..2) {
                val y = py - u * (1f + tier * 1.1f)
                val roof = Path().apply {
                    moveTo(px - u * 2.0f, y)
                    quadraticTo(px, y - u * 1.22f, px + u * 2.0f, y)
                    lineTo(px + u * 1.5f, y + u * .28f)
                    quadraticTo(px, y - u * .41f, px - u * 1.5f, y + u * .28f)
                    close()
                }
                drawPath(roof, Color(0xFF041027))
            }
            drawRect(Color(0xFFF4B65C).copy(alpha = .65f),
                Offset(px - u * .32f, py - u * 2.20f),
                androidx.compose.ui.geometry.Size(u * .64f, u * .70f))
            drawRect(Color(0xFFF4B65C).copy(alpha = .38f),
                Offset(px - u * .28f, py - u * 1.05f),
                androidx.compose.ui.geometry.Size(u * .56f, u * .43f))
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, Color(0xDF26252A)),
                    startY = h * .27f, endY = h * .83f,
                ),
            )
        } else if (accent == Ink.teal) {
            drawRect(
                Brush.horizontalGradient(
                    listOf(Color(0xFF002E38), Color(0xFF003D47), Color(0xFF004651)),
                ),
            )
            // Individual translucent puzzle tiles behind the real "Nova
            // igra" action, matching the soft diagonal vignette in the art.
            for (row in 0..4) for (col in 0..4) {
                val x = w * (.44f + col * .13f - row * .028f)
                val y = h * (-.14f + row * .20f)
                if ((row + col) % 3 == 0) {
                    drawRect(
                        Ink.teal.copy(alpha = .022f + col * .009f),
                        Offset(x, y),
                        androidx.compose.ui.geometry.Size(w * .11f, h * .18f),
                    )
                }
            }
            val focus = Offset(w * .78f, h * .13f)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Ink.teal.copy(alpha = .56f), Color.Transparent),
                    center = focus, radius = h * .43f,
                ), radius = h * .43f, center = focus,
            )
            for (i in -1..5) {
                val x = w * (.56f + i * .12f)
                drawLine(Ink.teal.copy(alpha = .27f),
                    Offset(x, 0f), Offset(x - w * .20f, h), 1.6f)
                val y = h * (.13f + i * .18f)
                drawLine(Ink.teal.copy(alpha = .17f),
                    Offset(w * .38f, y), Offset(w, y + h * .10f), 1.5f)
            }
            drawRect(Brush.verticalGradient(
                listOf(Color.Transparent, Color(0xAA001B31)),
                startY = h * .45f, endY = h,
            ))
        }
    }
}

@Composable
private fun HomeTile(
    title: String, subtitle: String, accent: Color,
    modifier: Modifier = Modifier, compact: Boolean = false, onClick: () -> Unit,
) {
    NeonPanel(
        modifier.testTag("home-" + title.lowercase().replace(" ", "-"))
            .semantics { contentDescription = "$title, $subtitle"; role = Role.Button }
            .heightIn(min = if (compact) 94.dp else 146.dp)
            .clickable(onClick = onClick),
        highlight = if (compact && accent == Ink.muted)
            Ink.line.copy(alpha = .85f) else accent,
        corner = 13.dp,
    ) {
        HomeTileArtwork(accent = accent, compact = compact)
        Column(
            Modifier.fillMaxWidth()
                .padding(start = if (compact) 20.dp else 21.dp,
                    top = if (compact) 12.dp else 19.dp,
                    end = if (compact) 16.dp else 20.dp,
                    bottom = if (compact) 10.dp else 15.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 6.dp),
        ) {
            val icon = homeIconFor(title)
            if (compact) {
                // Reference: the three small cards use unboxed, luminous
                // glyphs, not a flat solid-color vector or an extra button.
                BrandIcon(
                    icon,
                    Modifier.size(29.dp).drawBehind {
                        val center = Offset(size.width * .5f, size.height * .5f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(accent.copy(alpha = .27f), Color.Transparent),
                                center = center, radius = size.width * .78f,
                            ),
                            center = center, radius = size.width * .78f,
                        )
                    },
                    accent,
                )
            } else {
                Box(
                    Modifier.size(44.dp)
                        .shadow(
                            elevation = 7.dp,
                            shape = RoundedCornerShape(50),
                            ambientColor = accent.copy(alpha = .56f),
                            spotColor = accent.copy(alpha = .71f),
                        )
                        // A restrained radial glass well adds depth while
                        // retaining the original accessible full-card click.
                        .background(
                            Brush.radialGradient(listOf(
                                accent.copy(alpha = .37f),
                                accent.copy(alpha = .16f),
                                Color(0xCC03152F),
                            )),
                            RoundedCornerShape(50),
                        )
                        .border(
                            1.5.dp,
                            Brush.linearGradient(listOf(
                                Color.White.copy(alpha = .76f),
                                accent,
                                accent.copy(alpha = .55f),
                            )),
                            RoundedCornerShape(50),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    BrandIcon(icon, Modifier.size(30.dp), accent)
                }
            }
            Text(title, color = Ink.white, fontWeight = FontWeight.ExtraBold,
                fontSize = if (compact) 15.sp else 19.sp, maxLines = 2)
            Text(subtitle, color = Ink.muted,
                fontSize = if (compact) 10.sp else 12.sp,
                lineHeight = if (compact) 13.sp else 17.sp)
        }
        BrandIcon(
            "next",
            Modifier.align(Alignment.CenterEnd).padding(end = 9.dp).size(19.dp),
            if (compact) Ink.muted else accent,
        )
    }
}
