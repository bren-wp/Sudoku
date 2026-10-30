package com.brendigo.sudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.sudoku.data.Options
import com.brendigo.sudoku.data.Screen
import com.brendigo.sudoku.data.Statistics
import com.brendigo.sudoku.data.SudokuController
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Refresh the visible calendar date at local midnight, and within one minute
 * after manual clock/time-zone changes, without invalidating the UI each tick.
 */
@Composable
private fun rememberToday(): LocalDate {
    val today by produceState(initialValue = LocalDate.now()) {
        while (true) {
            delay(DailyDateTicker.delayUntilDayRefresh(ZonedDateTime.now()))
            value = LocalDate.now()
        }
    }
    return today
}

@Composable
fun PageContent(
    topPadding: androidx.compose.ui.unit.Dp = 28.dp,
    footerText: String = "V I Š E   O D   I G R E  ·  V J E Ž B A   Z A   U M",
    showFooter: Boolean = true,
    horizontalPadding: androidx.compose.ui.unit.Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Footer occupies its own layout slot below the scroll viewport.
    // Keeping it outside the scrolling content prevents overlays on cards,
    // dialogs, settings actions and the "show all achievements" control.
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding)
                .padding(top = topPadding, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            content()
        }
        if (showFooter) Text(
            footerText,
            color = Ink.muted, fontSize = 9.sp, letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 13.dp)
                .testTag("brand-footer"),
            maxLines = 1,
        )
    }
}

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

@Composable
fun SectionTitle(title: String, description: String = "", onBack: () -> Unit,
                 centered: Boolean = false,
                 titleOffset: androidx.compose.ui.unit.Dp = 0.dp) {
    if (!centered) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).testTag("navigate-back")
                    .semantics { contentDescription = "Natrag"; role = Role.Button }
                    .background(Color(0x99082150), RoundedCornerShape(14.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                BrandIcon("back", Modifier.size(23.dp), Ink.muted)
            }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f).testTag(
                "screen-" + title.lowercase().replace(" ", "-")
            )) {
                AccentText(title, if (title.length > 16) 26 else 30)
                if (description.isNotBlank()) Subtitle(description)
            }
        }
    } else {
        Box(Modifier.fillMaxWidth().heightIn(min = 63.dp)) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 50.dp)
                    .offset(y = titleOffset).testTag("screen-" + title.lowercase().replace(" ", "-"))
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    if (title == "Statistika") {
                        BrandIcon("statistics", Modifier.size(29.dp), Ink.cyan)
                        Spacer(Modifier.width(8.dp))
                    }
                    AccentText(title, if (title.length > 16) 35 else 39)
                }
                if (description.isNotBlank()) {
                    if (title == "Statistika" || title == "Postavke") {
                        Text(
                            description.uppercase(Locale("hr", "HR")),
                            modifier = Modifier.fillMaxWidth(),
                            color = Ink.muted,
                            fontSize = 9.sp,
                            letterSpacing = 3.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                        )
                    } else {
                        Subtitle(description, Modifier.fillMaxWidth(), centered = true)
                    }
                }
            }
            Box(
                Modifier.size(48.dp).testTag("navigate-back")
                    .semantics { contentDescription = "Natrag"; role = Role.Button }
                    .align(Alignment.CenterStart)
                    .background(Color(0x99082150), RoundedCornerShape(14.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                BrandIcon("back", Modifier.size(23.dp), Ink.muted)
            }
        }
    }
}

@Composable
fun SolidAction(title: String, onClick: () -> Unit, modifier: Modifier = Modifier,
                enabled: Boolean = true, minHeight: androidx.compose.ui.unit.Dp = 49.dp,
                tag: String = "primary-action") {
    Box(
        modifier.testTag(tag)
            .semantics { contentDescription = title; role = Role.Button }
            .heightIn(min = 48.dp).heightIn(min = minHeight)
            .background(
                Brush.horizontalGradient(if (enabled)
                    listOf(Color(0xFF04BFFF), Ink.blue, Color(0xFF0755EE))
                    else listOf(Color.DarkGray, Color.Gray)),
                RoundedCornerShape(30.dp)
            )
            .border(1.4.dp, if (enabled) Ink.cyan else Color.Gray, RoundedCornerShape(30.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            val glyph = when {
                title.trimStart().startsWith("▶") -> "play"
                title.trimStart().startsWith("✓") -> "check"
                else -> null
            }
            if (glyph != null) {
                BrandIcon(glyph, Modifier.size(21.dp), Ink.white)
                Spacer(Modifier.width(10.dp))
            }
            Text(
                title.trimStart().removePrefix("▶").removePrefix("✓").trim(),
                color = Ink.white, fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp, maxLines = 1,
            )
        }
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
                Triple("5", .73f, .17f), Triple("3", .78f, .40f),
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
                letterSpacing = 1.sp,
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
                    Text("N A S T A V I  I G R U", color = Ink.cyan,
                        fontSize = 10.sp, letterSpacing = 2.sp)
                    Text("Prekinuta igra", color = Ink.white,
                        fontWeight = FontWeight.ExtraBold, fontSize = 23.sp)
                    Text(difficultyTitle(game.level),
                        color = Ink.muted, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        BrandIcon("timer", Modifier.size(16.dp), Ink.muted)
                        Text(formatTime(game.elapsed), color = Ink.muted, fontSize = 12.sp)
                        Text("·  Još " + game.emptyCells + " praznih polja",
                            color = Ink.muted, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    SolidAction("▶   Nastavi", { controller.go(Screen.GAME) },
                        Modifier.fillMaxWidth(.45f), minHeight = 42.dp)
                }
            }
        }
        if (game?.daily == true && controller.hasSavedRegularGame()) {
            NeonPanel(
                Modifier.fillMaxWidth().testTag("resume-classic")
                    .semantics { contentDescription = "Nastavi spremljenu klasičnu igru"; role = Role.Button }
                    .clickable { controller.resumeRegularGame() }
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
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    Modifier.weight(1f),
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
    if (compact) return
    Canvas(Modifier.matchParentSize()) {
        val w = size.width
        val h = size.height
        if (accent == Ink.gold) {
            drawRect(Brush.horizontalGradient(listOf(
                Color(0xFF16283B), Color(0xFF493829), Color(0xFF624524),
            )))
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
            val focus = Offset(w * .83f, h * .10f)
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
        highlight = accent,
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
                // The reference uses unboxed icons on the smaller three cards.
                BrandIcon(icon, Modifier.size(29.dp), accent)
            } else {
                Box(
                    Modifier.size(44.dp)
                        .background(accent.copy(alpha = .13f), RoundedCornerShape(50))
                        .border(1.5.dp, accent, RoundedCornerShape(50)),
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

@Composable
fun NewGamePage(controller: SudokuController) {
    val scope = rememberCoroutineScope()
    // Keep the selected board and difficulty across Android activity recreation
    // (rotation, display size change and process-state restoration).
    var size by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableIntStateOf(9)
    }
    var level by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableIntStateOf(2)
    }
    PageContent(topPadding = 22.dp, showFooter = false, horizontalPadding = 21.dp) {
        // Reference hierarchy: back + board size on the same first row,
        // followed by the large title and the six full-width difficulty cards.
        BoxWithConstraints(Modifier.fillMaxWidth().testTag("new-game-size-header")) {
            // 192dp is the reference width. The selector shrinks with the
            // viewport instead of pushing the 48dp Back target off-screen.
            val selectorWidth = minOf(192.dp, (maxWidth - 56.dp).coerceAtLeast(0.dp))
            val compactSelector = selectorWidth < 175.dp
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(46.dp).testTag("navigate-back")
                        .semantics { contentDescription = "Natrag"; role = Role.Button }
                        .background(Color(0xB0082150), RoundedCornerShape(12.dp))
                        .border(1.dp, Ink.line.copy(alpha = .80f), RoundedCornerShape(12.dp))
                        .clickable { controller.go(Screen.HOME) },
                    contentAlignment = Alignment.Center,
                ) {
                    BrandIcon("back", Modifier.size(24.dp), Ink.muted)
                }
                NewGameSizeSelector(
                    selectedSize = size,
                    enabled = !controller.isGenerating,
                    onSelect = { size = it },
                    modifier = Modifier.width(selectorWidth),
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Column(Modifier.fillMaxWidth().testTag("screen-nova-igra")
            .padding(start = 5.dp)) {
            AccentText("Nova igra", 55)
            Text("Odaberi težinu", color = Ink.muted, fontSize = 21.sp)
        }
        Spacer(Modifier.height(1.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val accessibilityCards = maxWidth < 300.dp ||
                LocalDensity.current.fontScale >= 1.3f
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                difficultyLabels.forEachIndexed { i, label ->
                    NewGameDifficultySurface(
                        i,
                        Modifier.fillMaxWidth().testTag("difficulty-$i")
                            .heightIn(min = if (accessibilityCards) 96.dp else 74.dp)
                            .semantics {
                                contentDescription = "Težina $label"
                                role = Role.RadioButton
                                selected = level == i
                            }
                            .clickable(enabled = !controller.isGenerating) { level = i },
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(
                                horizontal = if (accessibilityCards) 12.dp else 18.dp,
                                vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val iconSize = if (accessibilityCards) 41.dp else 47.dp
                            Box(
                                Modifier.size(iconSize)
                                    .background(newGameAccents[i].copy(alpha = .11f),
                                        RoundedCornerShape(50))
                                    .border(2.dp, newGameAccents[i],
                                        RoundedCornerShape(50)),
                                contentAlignment = Alignment.Center,
                            ) {
                                BrandIcon(
                                    when (i) {
                                        0 -> "leaf-solid"
                                        1 -> "star-solid"
                                        2 -> "double-star-solid"
                                        3 -> "triple-star-solid"
                                        4 -> "crown-solid"
                                        else -> "crown-royal"
                                    },
                                    Modifier.size(if (accessibilityCards) 25.dp else 28.dp),
                                    newGameAccents[i],
                                )
                            }
                            Spacer(Modifier.width(if (accessibilityCards) 9.dp else 22.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    label,
                                    fontSize = if (accessibilityCards) 17.sp else 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink.white,
                                    maxLines = 2,
                                )
                                Text(
                                    listOf(
                                        "Savršeno za početnike",
                                        "Lagani izazov za vježbu",
                                        "Uravnotežen izazov",
                                        "Za iskusne igrače",
                                        "Ozbiljan izazov",
                                        "Samo za najbolje",
                                    )[i],
                                    color = Ink.muted,
                                    fontSize = if (accessibilityCards) 12.sp else 14.sp,
                                    lineHeight = if (accessibilityCards) 15.sp else 17.sp,
                                    maxLines = if (accessibilityCards) 3 else 2,
                                )
                            }
                            BrandIcon(
                                "next",
                                Modifier.size(if (accessibilityCards) 19.dp else 23.dp),
                                if (level == i) Ink.cyan else newGameAccents[i],
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(7.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val fullWidthAction = maxWidth < 290.dp
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                NewGameStartAction(
                    title = if (controller.isGenerating) "Pripremam zagonetku…" else "Započni igru",
                    onClick = { scope.launch { controller.startNew(size, level) } },
                    modifier = Modifier.fillMaxWidth(if (fullWidthAction) 1f else .74f),
                    enabled = !controller.isGenerating,
                )
            }
        }
        if (controller.isGenerating) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally),
            color = Ink.cyan)
    }
}

@Composable
private fun DailyMetric(
    icon: String, label: String, value: String,
    color: Color, modifier: Modifier = Modifier,
) {
    NeonPanel(modifier.heightIn(min = 64.dp), Ink.blue, corner = 13.dp) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandIcon(icon, Modifier.size(25.dp), color)
            Spacer(Modifier.width(7.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(label, color = Ink.muted, fontSize = 8.sp,
                    lineHeight = 10.sp, maxLines = 2)
                Text(value, color = Ink.white, fontSize = 12.sp,
                    fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@Composable
fun DailyPage(controller: SudokuController) {
    val today = rememberToday()
    val scope = rememberCoroutineScope()
    var showRanking by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(false)
    }
    val formattedDate = remember(today) {
        today.format(DateTimeFormatter.ofPattern("d. MMMM yyyy.", Locale("hr", "HR")))
    }
    PageContent {
        SectionTitle("Dnevni izazov", "Današnji izazov", centered = true,
            onBack = { controller.go(Screen.HOME) }, titleOffset = 18.dp)
        Spacer(Modifier.height(32.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val compactSummary = maxWidth < 355.dp ||
                LocalDensity.current.fontScale >= 1.3f
            NeonPanel(Modifier.fillMaxWidth().heightIn(min = if (compactSummary) 146.dp else 98.dp)
                .testTag("daily-summary"), Ink.blue) {
                if (compactSummary) {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 11.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            BrandIcon("calendar", Modifier.size(25.dp), Ink.blue)
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Text("D A N A S", color = Ink.muted, fontSize = 10.sp)
                                Text(formattedDate, color = Ink.white,
                                    fontWeight = FontWeight.Bold, fontSize = 12.sp,
                                    lineHeight = 15.sp, maxLines = 2)
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BrandIcon("flame", Modifier.size(27.dp), Ink.gold)
                                Spacer(Modifier.width(5.dp))
                                Column {
                                    Text("${controller.currentStreak(today)}", color = Ink.gold,
                                        fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                                    Text("DANA U NIZU", color = Ink.muted, fontSize = 9.sp)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BrandIcon("gift", Modifier.size(27.dp), Ink.gold)
                                Spacer(Modifier.width(6.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("DANAŠNJA NAGRADA", color = Ink.gold,
                                        fontSize = 9.sp, maxLines = 1)
                                    Text("Zlatna značka", color = Ink.white,
                                        fontSize = 12.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                } else {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                Row(Modifier.weight(1.4f), verticalAlignment = Alignment.CenterVertically) {
                    BrandIcon("calendar", Modifier.size(27.dp), Ink.blue)
                    Spacer(Modifier.width(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("D A N A S", color = Ink.muted, fontSize = 10.sp)
                        Text(formattedDate, color = Ink.white,
                            fontWeight = FontWeight.Bold, fontSize = 12.sp,
                            lineHeight = 14.sp, maxLines = 2)
                    }
                }
                Box(Modifier.width(1.dp).height(57.dp)
                    .background(Ink.blue.copy(alpha = .65f)))
                Column(Modifier.weight(1.0f),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandIcon("flame", Modifier.size(33.dp), Ink.gold)
                        Spacer(Modifier.width(4.dp))
                        Text("${controller.currentStreak(today)}", color = Ink.gold,
                            fontSize = 34.sp, fontWeight = FontWeight.ExtraBold,
                            lineHeight = 38.sp)
                    }
                    Text("DANA U NIZU", color = Ink.muted,
                        fontSize = 9.sp, maxLines = 1)
                }
                Column(Modifier.weight(1.18f),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    BrandIcon("gift", Modifier.size(34.dp), Ink.gold)
                    Text("DANAŠNJA NAGRADA", color = Ink.gold,
                        fontSize = 8.sp, textAlign = TextAlign.Center,
                        lineHeight = 10.sp, maxLines = 2)
                    Text("Zlatna značka", color = Ink.white,
                        fontSize = 11.sp, maxLines = 1)
                }
            }
                }
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val compactHero = maxWidth < 355.dp ||
                LocalDensity.current.fontScale >= 1.3f
        NeonPanel(Modifier.fillMaxWidth().heightIn(min = 202.dp)
            .testTag("daily-hero"), Ink.gold, warmGlowAlpha = .25f) {
            Column(
                Modifier.fillMaxWidth().heightIn(min = 202.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text("D A N A Š N J I   I Z A Z O V",
                    color = Ink.muted, fontSize = 11.sp, maxLines = 1)
                Text(
                    "Misteriozna planina",
                    fontSize = if (compactHero) 23.sp else 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink.white,
                    maxLines = if (compactHero) 2 else 1,
                )
                Text("Riješi današnju sudoku zagonetku i osvoji posebnu nagradu!",
                    color = Ink.muted, fontSize = 14.sp, lineHeight = 19.sp,
                    maxLines = 2)
                Spacer(Modifier.height(13.dp))
                Column(
                    Modifier.fillMaxWidth().testTag("daily-metrics"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (compactHero) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DailyMetric("statistics", "TEŽINA", "Srednja",
                                Ink.gold, Modifier.weight(1f).testTag("daily-difficulty-metric"))
                            DailyMetric("timer", "PROCIJENJENO VRIJEME", "12 min",
                                Ink.cyan, Modifier.weight(1f).testTag("daily-time-metric"))
                        }
                        DailyMetric("star", "NAGRADA", "50 bodova",
                            Ink.gold, Modifier.fillMaxWidth().testTag("daily-reward-metric"))
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DailyMetric("statistics", "TEŽINA", "Srednja",
                                Ink.gold, Modifier.weight(1f).testTag("daily-difficulty-metric"))
                            DailyMetric("timer", "PROCIJENJENO VRIJEME", "12 min",
                                Ink.cyan, Modifier.weight(1f).testTag("daily-time-metric"))
                            DailyMetric("star", "NAGRADA", "50 bodova",
                                Ink.gold, Modifier.weight(1f).testTag("daily-reward-metric"))
                        }
                    }
                }
            }
        }
        }
        NeonPanel(Modifier.fillMaxWidth().heightIn(min = 122.dp), Ink.blue) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("T V O J   N I Z", color = Ink.muted, fontSize = 11.sp,
                        modifier = Modifier.weight(.40f))
                    Text("Riješi izazove svaki dan za više nagrada!",
                        color = Ink.muted, fontSize = 10.sp,
                        textAlign = TextAlign.End, maxLines = 2,
                        modifier = Modifier.weight(.60f))
                }
                val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
                // Include legacy lastDaily even when newer history entries exist.
                val completedDays = (controller.stats.completedDailyDates +
                    controller.stats.lastDaily).filter { it.isNotBlank() }.toSet()
                // Draw a segment only when BOTH neighboring days are complete.
                // Counting all completions produces misleading fills after missed days.
                val completedSegments = (0..5).filter { i ->
                    monday.plusDays(i.toLong()).toString() in completedDays &&
                        monday.plusDays((i + 1).toLong()).toString() in completedDays
                }
                Row(
                    Modifier.fillMaxWidth().testTag("daily-weekly-streak").drawBehind {
                        val lineY = size.height * .48f
                        drawLine(Ink.blue.copy(alpha = .65f),
                            Offset(size.width * .07f, lineY),
                            Offset(size.width * .93f, lineY),
                            strokeWidth = 2.dp.toPx())
                        completedSegments.forEach { i ->
                            drawLine(Ink.cyan.copy(alpha = .90f),
                                Offset(size.width * (.07f + .86f * i / 6f), lineY),
                                Offset(size.width * (.07f + .86f * (i + 1) / 6f), lineY),
                                strokeWidth = 2.dp.toPx())
                        }
                    },
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    listOf("PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED")
                        .forEachIndexed { index, label ->
                            val date = monday.plusDays(index.toLong())
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(label, color = Ink.muted, fontSize = 10.sp)
                                Spacer(Modifier.height(3.dp))
                                StreakMarker(date.toString() in completedDays,
                                    diameter = 31.dp, dayLabel = label)
                                Spacer(Modifier.height(3.dp))
                                Text("${date.dayOfMonth}.${date.monthValue}.",
                                    color = if (date == today) Ink.white else Ink.muted,
                                    fontSize = 10.sp,
                                    fontWeight = if (date == today)
                                        FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                }
            }
        }
        if (controller.dailyAlreadyCompleted()) {
            Text("✓ Današnji izazov već je riješen.", color = Ink.teal,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SolidAction(
                when {
                    controller.dailyAlreadyCompleted() -> "✓   Današnji izazov riješen"
                    controller.isGenerating -> "Učitavanje…"
                    controller.hasSavedDailyFor(today) -> "▶   Nastavi današnji izazov"
                    else -> "▶   Pokreni izazov"
                },
                { scope.launch { controller.startNew(9, 2, daily = true) } },
                Modifier.fillMaxWidth(.80f),
                !controller.isGenerating && !controller.dailyAlreadyCompleted(),
                minHeight = 56.dp,
            )
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            NeonPanel(
                Modifier.fillMaxWidth(.64f).heightIn(min = 53.dp)
                    .testTag("daily-leaderboard")
                    .semantics {
                        contentDescription = "Otvori osobnu rang-listu"
                        role = Role.Button
                    }
                    .clickable { showRanking = true },
                Ink.blue,
            ) {
                Row(
                    Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    BrandIcon("trophy", Modifier.size(25.dp), Ink.purple)
                    Spacer(Modifier.width(9.dp))
                    Text("Pogledaj rang-listu", color = Ink.white,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        maxLines = 1)
                }
            }
        }
    }
    if (showRanking) {
        val records = controller.stats.history.filter { it.daily }.sortedBy { it.seconds }
        AlertDialog(
            onDismissRequest = { showRanking = false },
            containerColor = Color(0xFF081A41),
            title = { Text("Osobna rang-lista", color = Ink.white) },
            text = {
                Column(Modifier.heightIn(max = 330.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Subtitle("Offline rezultati na ovom uređaju. Nema globalne rang-liste.")
                    if (records.isEmpty()) Subtitle("Još nema riješenih dnevnih izazova.")
                    records.take(30).forEachIndexed { index, record ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("${index + 1}.", color = Ink.gold, modifier = Modifier.width(32.dp))
                            Text(record.date, color = Ink.white, modifier = Modifier.weight(1f))
                            Text(formatTime(record.seconds), color = Ink.cyan)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRanking = false }) { Text("Zatvori", color = Ink.cyan) }
            },
        )
    }
}

private data class PreviewAward(
    val title: String, val icon: String,
    val current: Int, val goal: Int, val tint: Color,
)

@Composable
fun StatsPage(controller: SudokuController) {
    val stats = controller.stats
    var metricInfo by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var showHistory by remember { androidx.compose.runtime.mutableStateOf(false) }
    PageContent(topPadding = 18.dp) {
        SectionTitle("Statistika", "Tvoj Sudoku put u brojkama", centered = true, onBack = { controller.go(Screen.HOME) })
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            StatTile("Odigrane igre", stats.played.toString(), Ink.teal, Modifier.weight(1f)) {
                metricInfo = "Ukupan broj pokrenutih Sudoku partija. Dovršene igre prikazane su zasebno."
            }
            StatTile("Najbolje vrijeme", formatTime(stats.best), Ink.purple, Modifier.weight(1f)) {
                metricInfo = "Najbrže vrijeme dovršene Sudoku partije."
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            StatTile("Ukupno vrijeme", formatTime(stats.totalPlayedSeconds.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()),
                Ink.blue, Modifier.weight(1f)) {
                metricInfo = "Ukupno aktivno vrijeme igranja, uključujući nedovršene partije. Vrijeme pauze nije uključeno."
            }
            StatTile("Dnevni niz", controller.currentStreak().toString(), Ink.gold, Modifier.weight(1f)) {
                metricInfo = "Broj uzastopnih dana s riješenim dnevnim izazovom."
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 348.dp) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DifficultyChart(stats, Modifier.weight(1.25f))
                    CompletionRing(stats, Modifier.weight(1f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DifficultyChart(stats, Modifier.fillMaxWidth())
                    CompletionRing(stats, Modifier.fillMaxWidth())
                }
            }
        }
        NeonPanel(Modifier.fillMaxWidth().heightIn(min = 132.dp), Ink.blue) {
            Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .testTag("open-game-history")
                        .semantics { contentDescription = "Prikaži povijest dovršenih igara"; role = Role.Button }
                        .clickable { showHistory = true },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BrandIcon("grid", Modifier.size(19.dp), Ink.cyan)
                    Spacer(Modifier.width(8.dp))
                    Text("Nedavni napredak", color = Ink.white, fontSize = 16.sp,
                        modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("Pogledaj sve", color = Ink.cyan, fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    BrandIcon("next", Modifier.size(15.dp), Ink.cyan)
                }
                if (stats.history.isEmpty()) Subtitle("Još nema dovršenih igara.")
                stats.history.take(4).forEach {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandIcon("grid", Modifier.size(21.dp), difficultyColors[it.level])
                        Spacer(Modifier.width(9.dp))
                        Text(difficultyLabels[it.level], color = Ink.white, modifier = Modifier.weight(1f))
                        Text(formatTime(it.seconds), color = Ink.muted, fontSize = 13.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(it.date, color = Ink.muted, fontSize = 11.sp)
                    }
                }
            }
        }
        NeonPanel(Modifier.fillMaxWidth().heightIn(min = 109.dp)
            .testTag("stats-achievements")
            .semantics {
                contentDescription = "Otvori svih 24 postignuća"
                role = Role.Button
            }
            .clickable { controller.go(Screen.ACHIEVEMENTS) }, Ink.blue) {
            Column(
                Modifier.fillMaxWidth().padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BrandIcon("trophy", Modifier.size(19.dp), Ink.cyan)
                    Spacer(Modifier.width(8.dp))
                    Text("Postignuća", color = Ink.white, fontSize = 16.sp,
                        modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("Pogledaj sve", color = Ink.cyan, fontSize = 11.sp)
                    BrandIcon("next", Modifier.size(15.dp), Ink.cyan)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf(
                        PreviewAward("Prva pobjeda", "star", stats.finished, 1, Ink.teal),
                        PreviewAward("Brzinac", "timer", stats.fastWins, 1, Ink.blue),
                        PreviewAward("Sedam dana", "flame", stats.bestStreak, 7, Ink.purple),
                        PreviewAward("Majstor logike", "brain", stats.winsByLevel[3], 50, Ink.cyan),
                    ).forEach { award ->
                        val fraction = (award.current.toFloat() / award.goal).coerceIn(0f, 1f)
                        NeonPanel(
                            Modifier.weight(1f).heightIn(min = 67.dp),
                            if (fraction >= 1f) award.tint else Ink.line,
                            corner = 10.dp,
                        ) {
                            Column(
                                Modifier.fillMaxWidth().padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                BrandIcon(
                                    if (fraction >= 1f) award.icon else "lock",
                                    Modifier.size(20.dp),
                                    if (fraction >= 1f) award.tint else Ink.muted,
                                )
                                Text(award.title, color = Ink.white, fontSize = 9.sp,
                                    maxLines = 1, textAlign = TextAlign.Center)
                                Box(
                                    Modifier.fillMaxWidth().height(4.dp)
                                        .background(Ink.blue.copy(alpha = .30f),
                                            RoundedCornerShape(8.dp)),
                                ) {
                                    Box(
                                        Modifier.fillMaxWidth(fraction).fillMaxHeight()
                                            .background(award.tint, RoundedCornerShape(8.dp)),
                                    )
                                }
                                Text("${award.current.coerceAtMost(award.goal)}/${award.goal}",
                                    color = Ink.muted, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }
    if (metricInfo != null) {
        AlertDialog(
            onDismissRequest = { metricInfo = null },
            containerColor = Color(0xFF081A41),
            title = { Text("Statistika", color = Ink.white) },
            text = { Text(metricInfo ?: "", color = Ink.muted) },
            confirmButton = {
                TextButton(onClick = { metricInfo = null }) {
                    Text("Zatvori", color = Ink.cyan)
                }
            },
        )
    }
    if (showHistory) {
        AlertDialog(
            onDismissRequest = { showHistory = false },
            containerColor = Color(0xFF081A41),
            title = { Text("Povijest igara", color = Ink.white) },
            text = {
                Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Subtitle("Prikaz posljednjih 50 dovršenih igara.")
                    if (stats.history.isEmpty()) Subtitle("Još nema dovršenih igara.")
                    stats.history.forEach { record ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(difficultyLabels[record.level], color = Ink.white,
                                modifier = Modifier.weight(1f), fontSize = 13.sp)
                            Text(formatTime(record.seconds), color = Ink.cyan, fontSize = 13.sp)
                            Spacer(Modifier.width(9.dp))
                            Text(record.date, color = Ink.muted, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistory = false }) { Text("Zatvori", color = Ink.cyan) }
            },
        )
    }
}

@Composable
private fun DifficultyChart(stats: Statistics, modifier: Modifier) {
    // Four visual groups match the reference design while preserving all six
    // internal difficulty tiers (the first two and last two are aggregated).
    val counts = listOf(
        stats.winsByLevel[0] + stats.winsByLevel[1],
        stats.winsByLevel[2],
        stats.winsByLevel[3],
        stats.winsByLevel[4] + stats.winsByLevel[5],
    )
    val colors = listOf(Ink.teal, Ink.blue, Ink.purple, Ink.gold)
    val labels = listOf("Lako", "Srednje", "Teško", "Stručno")
    val ceiling = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
    NeonPanel(modifier.heightIn(min = 210.dp), Ink.blue) {
        Column(Modifier.fillMaxSize().padding(horizontal = 11.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandIcon("statistics", Modifier.size(20.dp), Ink.cyan)
                Spacer(Modifier.width(6.dp))
                Text("Riješene igre po težini", color = Ink.white,
                    fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2)
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth().height(148.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom) {
                counts.forEachIndexed { index, amount ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(amount.toString(), color = Ink.white, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier.fillMaxWidth(.75f)
                                .height(if (amount == 0) 2.dp else (12 + 85 * amount / ceiling).dp)
                                .background(
                                    Brush.verticalGradient(listOf(
                                        colors[index], colors[index].copy(alpha = .35f)
                                    )),
                                    RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                                )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(labels[index], color = Ink.muted, fontSize = 9.sp,
                            maxLines = 1, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionRing(stats: Statistics, modifier: Modifier) {
    val fraction = if (stats.played > 0)
        (stats.finished.toFloat() / stats.played).coerceIn(0f, 1f)
    else 0f
    val percentage = (fraction * 100).toInt()
    NeonPanel(modifier.heightIn(min = 210.dp), Ink.cyan) {
        Column(Modifier.fillMaxSize().padding(11.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandIcon("statistics", Modifier.size(19.dp), Ink.cyan)
                Spacer(Modifier.width(5.dp))
                Text("Postotak dovršenosti", color = Ink.white,
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2)
            }
            Spacer(Modifier.height(5.dp))
            Box(Modifier.fillMaxWidth().height(126.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(105.dp)) {
                    drawArc(Ink.blue.copy(alpha = .27f), 0f, 360f, false,
                        style = Stroke(9.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(Ink.cyan, -90f, fraction * 360f, false,
                        style = Stroke(12.dp.toPx(), cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$percentage%", fontSize = 29.sp, color = Ink.white,
                        fontWeight = FontWeight.ExtraBold)
                    Text("Dovršene igre", color = Ink.muted, fontSize = 10.sp, maxLines = 1)
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(Ink.cyan, RoundedCornerShape(50)))
                Spacer(Modifier.width(6.dp))
                Text("Dovršene igre", color = Ink.muted,
                    fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text("${stats.finished}", color = Ink.white, fontSize = 12.sp,
                    fontWeight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp)
                    .background(Ink.blue.copy(alpha = .50f), RoundedCornerShape(50)))
                Spacer(Modifier.width(6.dp))
                Text("Prekinute igre", color = Ink.muted,
                    fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text("${(stats.played - stats.finished).coerceAtLeast(0)}", color = Ink.white, fontSize = 12.sp,
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, color: Color,
                     modifier: Modifier, onClick: () -> Unit) {
    val tileId = when (label) {
        "Odigrane igre" -> "played"
        "Najbolje vrijeme" -> "best"
        "Ukupno vrijeme" -> "total"
        "Dnevni niz" -> "streak"
        else -> "other"
    }
    NeonPanel(modifier.heightIn(min = 86.dp)
        .testTag("stat-tile-$tileId")
        .semantics { contentDescription = "$label: $value"; role = Role.Button }
        .clickable(onClick = onClick), color) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            // The 941px reference and compact emulator give each tile less
            // than 180dp. Scale decorations instead of truncating the label.
            val compact = maxWidth < 180.dp
            Row(
                Modifier.fillMaxWidth().padding(
                    horizontal = if (compact) 7.dp else 10.dp,
                    vertical = 8.dp,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BrandIcon(when (label) {
                    "Odigrane igre" -> "gamepad"
                    "Najbolje vrijeme" -> "timer"
                    "Ukupno vrijeme" -> "timer"
                    "Dnevni niz" -> "flame"
                    else -> "statistics"
                }, Modifier.size(if (compact) 27.dp else 37.dp), color)
                Spacer(Modifier.width(if (compact) 5.dp else 9.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        label, color = Ink.muted,
                        fontSize = if (compact) 10.sp else 12.sp,
                        lineHeight = if (compact) 12.sp else 14.sp,
                        maxLines = 2,
                    )
                    Text(
                        value, color = Ink.white,
                        fontSize = if (compact && value.length > 7) 14.sp
                            else if (compact) 18.sp else 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                    )
                }
                BrandIcon("next", Modifier.size(if (compact) 12.dp else 17.dp), color)
            }
        }
    }
}


private data class Achievement(
    val icon: String, val title: String, val text: String, val goal: Int,
    val current: Int, val reward: Int, val tint: Color,
)

@Composable
fun AchievementsPage(controller: SudokuController) {
    val s = controller.stats
    var showAll by remember { androidx.compose.runtime.mutableStateOf(false) }
    var selectedAchievement by remember { androidx.compose.runtime.mutableStateOf<Achievement?>(null) }
    // 23 measured awards + the Sudoku legend award = 24 distinct achievements.
    val basic = listOf(
        Achievement("🏆", "Prva pobjeda", "Završi svoju prvu igru.", 1, s.finished, 50, Ink.gold),
        Achievement("▣", "Sedam dana zaredom", "Riješi dnevne izazove sedam dana zaredom.", 7, s.bestStreak, 100, Ink.blue),
        Achievement("✿", "Majstor logike", "Završi 50 igara na teškoj razini.", 50, s.winsByLevel[3], 150, Ink.teal),
        Achievement("ϟ", "Bez greške", "Završi igru bez ijedne pogreške.", 1, s.flawless, 100, Ink.gold),
        Achievement("🔥", "Vrući niz", "Riješi dnevni izazov 30 dana zaredom.", 30, s.bestStreak, 150, Ink.cyan),
        Achievement("◷", "Brzinac", "Završi klasični 9×9 sudoku za manje od 5 minuta.", 1, s.fastWins, 75, Ink.purple),
        Achievement("▦", "Rješavač izazova", "Završi 10 dnevnih izazova.", 10, s.dailyWins, 100, Ink.blue),
        Achievement("★", "Prvi korak", "Pokreni svoju prvu igru.", 1, s.played, 25, Ink.teal),
        Achievement("✦", "Redoviti igrač", "Pokreni 10 Sudoku igara.", 10, s.played, 50, Ink.cyan),
        Achievement("🎖", "Iskusni igrač", "Završi 50 igara.", 50, s.finished, 100, Ink.gold),
        Achievement("💯", "Stotka", "Završi 100 igara.", 100, s.finished, 150, Ink.gold),
        Achievement("✓", "Besprijekoran niz", "Završi 10 igara bez greške.", 10, s.flawless, 125, Ink.teal),
        Achievement("◇", "Samostalni mislilac", "Završi 10 igara bez savjeta.", 10, s.noHintWins, 100, Ink.purple),
        Achievement("▣", "Prvi izazov", "Riješi prvi dnevni izazov.", 1, s.dailyWins, 50, Ink.blue),
        Achievement("☀", "Dnevna rutina", "Završi sedam dnevnih izazova.", 7, s.dailyWins, 80, Ink.cyan),
        Achievement("◷", "Deset sati logike", "Provedi ukupno 10 sati u riješenim igrama.", 36000, s.totalSeconds.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), 120, Ink.blue),
        Achievement("◴", "Strpljivi mislilac", "Završi igru nakon sat vremena razmišljanja.", 1, s.longWins, 80, Ink.purple),
        Achievement("♜", "Ekspert", "Završi 10 igara razine Ekspert.", 10, s.winsByLevel[4], 140, Ink.purple),
        Achievement("♛", "Velemajstor", "Završi pet igara razine Majstor.", 5, s.winsByLevel[5], 180, Ink.red),
        Achievement("4", "Mala mreža", "Završi 10 igara 4×4.", 10, s.winsBySize[0], 70, Ink.teal),
        Achievement("6", "Srednja mreža", "Završi 10 igara 6×6.", 10, s.winsBySize[1], 90, Ink.blue),
        Achievement("9", "Klasična mreža", "Završi 10 igara 9×9.", 10, s.winsBySize[2], 120, Ink.cyan),
        Achievement("🔥", "Zlatni niz", "Dovrši 14 dnevnih izazova zaredom.", 14, s.bestStreak, 150, Ink.gold),
    )
    val otherPoints = basic.filter { it.current >= it.goal }.sumOf { it.reward }
    val data = basic + Achievement("♛", "Sudoku legenda",
        "Osvoji 1000 bodova iz ostalih postignuća.", 1000, otherPoints, 200, Ink.gold)
    val points = data.filter { it.current >= it.goal }.sumOf { it.reward } + s.dailyPoints
    // The reference shows seven highlighted awards and Sudoku legend in the
    // first four rows; the remaining 16 remain accessible on demand.
    val featured = basic.take(7) + data.last()
    val visible = if (showAll) featured + basic.drop(7) else featured
    PageContent(topPadding = 18.dp) {
        SectionTitle("Postignuća", centered = true, onBack = { controller.go(Screen.HOME) })
        NeonPanel(Modifier.fillMaxWidth().heightIn(min = 75.dp)
            .testTag("achievement-summary"), Ink.blue) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val compactSummary = maxWidth < 375.dp || LocalDensity.current.fontScale >= 1.4f
                Row(
                    Modifier.fillMaxWidth().padding(
                        horizontal = if (compactSummary) 9.dp else 17.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        if (compactSummary) 6.dp else 13.dp),
                ) {
                BrandIcon("trophy", Modifier.size(if (compactSummary) 30.dp else 40.dp), Ink.gold)
                Column(Modifier.weight(1f)) {
                    Subtitle("Ukupno bodova")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$points", fontSize = if (compactSummary) 22.sp else 27.sp,
                            color = Ink.gold, fontWeight = FontWeight.ExtraBold)
                        Text(" / 1000", fontSize = if (compactSummary) 10.sp else 13.sp, color = Ink.muted)
                    }
                }
                Box(Modifier.width(1.dp).height(49.dp)
                    .background(Ink.blue.copy(alpha = .80f)))
                BrandIcon("star", Modifier.size(if (compactSummary) 27.dp else 38.dp), Ink.cyan)
                Column(Modifier.weight(.75f)) {
                    Subtitle("Otključano")
                    Text("${data.count { it.current >= it.goal }} / ${data.size}",
                        fontSize = if (compactSummary) 20.sp else 26.sp,
                        color = Ink.cyan, fontWeight = FontWeight.ExtraBold)
                }
                }
            }
        }
        // The 941x1672 reference keeps two columns; narrow phones and large
        // accessibility fonts get cards that can grow without clipping text.
        val fontScale = LocalDensity.current.fontScale
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val stackCards = maxWidth < 300.dp || fontScale >= 1.4f
            val compactCards = maxWidth < 375.dp || stackCards
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                visible.chunked(if (stackCards) 1 else 2).forEachIndexed { rowIndex, pair ->
                    val rowHeight = when {
                        compactCards && rowIndex < 2 -> 186.dp
                        compactCards && rowIndex < 4 -> 174.dp
                        compactCards -> 184.dp
                        rowIndex < 2 -> 151.dp
                        rowIndex < 4 -> 137.dp
                        else -> 151.dp
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { achievement ->
                    val unlocked = achievement.current >= achievement.goal
                    val compactFeatured = rowIndex in 2..3
                    NeonPanel(Modifier.weight(1f).then(
                            if (compactCards) Modifier.heightIn(min = rowHeight)
                            else Modifier.height(rowHeight)
                        )
                        .testTag("achievement-" + achievement.title.lowercase().replace(" ", "-"))
                        .semantics {
                            contentDescription = achievement.title +
                                (if (unlocked) ", otključano" else ", zaključano")
                            role = Role.Button
                        }
                        .clickable { selectedAchievement = achievement },
                        // Preserve the warm sunset treatment on gold awards.
                        if (achievement.tint == Ink.gold) Ink.gold
                        else if (unlocked) achievement.tint
                        else achievement.tint.copy(alpha = .70f),
                        warmPagoda = false) {
                        if (!unlocked) {
                            Box(Modifier.matchParentSize().background(Color(0x66051530)))
                        }
                        Column(
                            Modifier.fillMaxWidth()
                                .then(if (compactCards) Modifier.heightIn(min = rowHeight)
                                      else Modifier.fillMaxHeight())
                                .padding(horizontal = 9.dp,
                                    vertical = if (rowIndex < 2) 9.dp else 7.dp),
                            verticalArrangement = Arrangement.spacedBy(
                                if (compactCards) 4.dp else 2.dp),
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Box(
                                    Modifier.size(if (compactFeatured) 36.dp else 43.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    // Keep each award recognizable when locked.
                                    BrandIcon(achievementIconFor(achievement.title),
                                        Modifier.size(if (compactFeatured) 35.dp else 42.dp),
                                        if (unlocked) achievement.tint
                                        else achievement.tint.copy(alpha = .60f))
                                    if (!unlocked) {
                                        Box(
                                            Modifier.size(if (compactFeatured) 18.dp else 20.dp)
                                                .align(Alignment.BottomEnd)
                                                .background(Color(0xEE081936), RoundedCornerShape(7.dp)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BrandIcon("lock",
                                                Modifier.size(if (compactFeatured) 15.dp else 17.dp),
                                                Ink.muted)
                                        }
                                    }
                                }
                                Row(
                                    Modifier.background(Ink.blue.copy(alpha = .23f),
                                            RoundedCornerShape(30.dp))
                                        .border(1.dp, if (unlocked) achievement.tint
                                            else Ink.muted.copy(alpha = .55f),
                                            RoundedCornerShape(30.dp))
                                        .padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    BrandIcon("star", Modifier.size(13.dp),
                                        if (unlocked) achievement.tint else Ink.muted)
                                    Spacer(Modifier.width(3.dp))
                                    Text("${achievement.reward}",
                                        color = if (unlocked) achievement.tint
                                            else achievement.tint.copy(alpha = .75f),
                                        fontSize = 12.sp)
                                }
                            }
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(achievement.title, color = Ink.white,
                                    fontSize = if (compactCards) 13.sp else 15.sp,
                                    modifier = Modifier.weight(1f).testTag(
                                        "achievement-title-" + achievement.title.lowercase().replace(" ", "-")),
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = if (compactCards) 16.sp else 18.sp,
                                    maxLines = if (compactCards) 3 else 2)
                                if (unlocked) {
                                    Spacer(Modifier.width(4.dp))
                                    Box(
                                        Modifier.size(23.dp)
                                            .border(1.2.dp, achievement.tint, RoundedCornerShape(50)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BrandIcon("check", Modifier.size(15.dp), achievement.tint)
                                    }
                                }
                            }
                            Text(
                                achievement.text, color = Ink.muted,
                                fontSize = if (compactFeatured || compactCards) 10.sp else 11.sp,
                                lineHeight = if (compactFeatured || compactCards) 12.sp else 13.sp,
                                modifier = (if (compactCards) Modifier
                                    else Modifier.weight(1f)).testTag(
                                        "achievement-description-" +
                                            achievement.title.lowercase().replace(" ", "-")),
                                maxLines = if (compactCards) 4 else 2,
                            )
                            val progress = (achievement.current.toFloat() /
                                achievement.goal.toFloat()).coerceIn(0f, 1f)
                            Box(Modifier.fillMaxWidth().height(8.dp).background(Ink.blue.copy(alpha = .25f),
                                RoundedCornerShape(12.dp))) {
                                Box(Modifier.fillMaxWidth(progress).fillMaxHeight()
                                    .background(if (unlocked) achievement.tint else
                                        achievement.tint.copy(alpha = .58f),
                                    RoundedCornerShape(12.dp)))
                            }
                            Text("${achievement.current.coerceAtMost(achievement.goal)} / ${achievement.goal}",
                                color = Ink.muted, fontSize = 11.sp)
                        }
                    }
                }
                    }
                }
            }
        }
        // Available by scrolling, not partially rendered over the first
        // 941×1672 reference view or the landscape / fixed footer.
        Spacer(Modifier.height(24.dp))
        NeonPanel(Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .testTag("achievements-show-all")
            .semantics {
                contentDescription = if (showAll) "Prikaži istaknuta postignuća"
                    else "Pogledaj svih 24 postignuća"
                role = Role.Button
            }
            .clickable { showAll = !showAll }, Ink.blue) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (showAll) "Prikaži istaknuta postignuća"
                     else "Pogledaj svih 24 postignuća", color = Ink.cyan, fontSize = 13.sp)
            }
        }
    }
    selectedAchievement?.let { award ->
        AlertDialog(
            onDismissRequest = { selectedAchievement = null },
            modifier = Modifier.testTag("achievement-detail"),
            containerColor = Color(0xFF081A41),
            title = { Text(award.title, color = Ink.white) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(award.text, color = Ink.muted)
                    Text(
                        "Napredak: ${award.current.coerceAtMost(award.goal)} / ${award.goal}",
                        color = Ink.cyan,
                    )
                    Text(
                        if (award.current >= award.goal)
                            "Postignuće otključano · +${award.reward} bodova"
                        else "Osvoji postignuće i otključaj ${award.reward} bodova.",
                        color = Ink.gold,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAchievement = null }) {
                    Text("Zatvori", color = Ink.cyan)
                }
            },
        )
    }
}

@Composable
fun SettingsPage(
    controller: SudokuController,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
) {
    val opts = controller.options
    // Android destroys and recreates Activities for rotation, multi-window,
    // font scale and theme changes. Preserve any open Settings dialog.
    var showPrivacy by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(false)
    }
    var showSound by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(false)
    }
    var showLanguage by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(false)
    }
    var showAbout by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(false)
    }
    PageContent(topPadding = 20.dp) {
        SectionTitle("Postavke", "Prilagodi igru svojim željama", centered = true, onBack = { controller.back() })
        NeonPanel(Modifier.fillMaxWidth(), Ink.blue) {
            Column(Modifier.padding(9.dp)) {
                SettingHeader("palette", "Izgled", "Prilagodi vizualni doživljaj igre")
                SettingToggle("moon", "Tamna tema", "Koristi tamni izgled aplikacije", opts.dark) {
                    controller.updateOptions(opts.copy(dark = it))
                }
                SettingToggle("sparkles", "Animacije", "Prikaži animacije i vizualne efekte", opts.animations) {
                    controller.updateOptions(opts.copy(animations = it))
                }
            }
        }
        NeonPanel(Modifier.fillMaxWidth(), Ink.blue) {
            Column(Modifier.padding(9.dp)) {
                SettingHeader("gamepad", "Igra", "Postavke igrivosti i pomoći")
                SettingToggle("note", "Automatske bilješke", "Automatski dodaj moguće brojeve u prazna polja", opts.autoNotes) {
                    controller.updateOptions(opts.copy(autoNotes = it))
                }
                SettingToggle("warning", "Provjera greške", "Odmah označi netočne unose", opts.checkMistakes) {
                    controller.updateOptions(opts.copy(checkMistakes = it))
                }
                SettingToggle("grid", "Isticanje brojeva", "Naglasi sve iste brojeve na ploči", opts.highlightEquals) {
                    controller.updateOptions(opts.copy(highlightEquals = it))
                }
            }
        }
        NeonPanel(
            Modifier.fillMaxWidth().heightIn(min = 58.dp)
                .testTag("sound-vibration")
                .semantics { contentDescription = "Zvuk i vibracija"; role = Role.Button }
                .clickable { showSound = true },
            Ink.purple,
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BrandIcon("speaker", Modifier.size(25.dp), Ink.purple)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Zvuk i vibracija", fontSize = 17.sp,
                        fontWeight = FontWeight.Bold, color = Ink.white)
                    Subtitle("Uključi ili isključi zvučne efekte i vibracije", centered = false)
                }
                BrandIcon("next", Modifier.size(20.dp), Ink.muted)
            }
        }
        NeonPanel(Modifier.fillMaxWidth().heightIn(min = 54.dp)
            .testTag("language-settings")
            .semantics { contentDescription = "Jezik aplikacije, hrvatski"; role = Role.Button }
            .clickable { showLanguage = true }, Ink.teal) {
            Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                BrandIcon("globe", Modifier.size(25.dp), Ink.teal)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Jezik", color = Ink.white, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Subtitle("Odaberi jezik aplikacije")
                }
                Text("Hrvatski", color = Ink.muted, fontSize = 13.sp)
                Spacer(Modifier.width(6.dp))
                BrandIcon("next", Modifier.size(16.dp), Ink.teal)
            }
        }
        NeonPanel(Modifier.fillMaxWidth(), Ink.blue) {
            Column(
                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 58.dp)
                        .testTag("about-app")
                        .semantics {
                            contentDescription = "O aplikaciji Sudoku by Brendigo"
                            role = Role.Button
                        }
                        .clickable { showAbout = true },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SettingHeader("info", "O aplikaciji", "Sudoku by Brendigo",
                        Modifier.weight(1f))
                    BrandIcon("next", Modifier.size(20.dp), Ink.muted)
                }
                NeonPanel(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .testTag("privacy-policy")
                        .semantics { contentDescription = "Otvori pravila privatnosti"; role = Role.Button }
                        .clickable { showPrivacy = true },
                    Ink.blue,
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BrandIcon("shield", Modifier.size(20.dp), Ink.cyan)
                        Spacer(Modifier.width(12.dp))
                        Text("Pravila privatnosti", color = Ink.white,
                            fontSize = 15.sp, modifier = Modifier.weight(1f))
                        BrandIcon("next", Modifier.size(18.dp), Ink.cyan)
                    }
                }
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BrandIcon("grid", Modifier.size(20.dp), Ink.muted)
                    Spacer(Modifier.width(12.dp))
                    Text("Verzija " + com.brendigo.sudoku.BuildConfig.VERSION_NAME,
                        color = Ink.muted, fontSize = 13.sp)
                }
            }
        }
        // Keep the screenshot-matched Settings sections intact and append
        // user-initiated offline backup actions after the existing content.
        NeonPanel(Modifier.fillMaxWidth(), Ink.teal) {
            Column(Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingHeader("shield", "Sigurnosna kopija",
                    "Šifrirano spremanje i vraćanje napretka na uređaju")
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f)) {
                        SolidAction("Izvezi kopiju", onExportBackup,
                            Modifier.fillMaxWidth(), minHeight = 48.dp, tag = "backup-export")
                    }
                    Box(Modifier.weight(1f)) {
                        SolidAction("Uvezi kopiju", onImportBackup,
                            Modifier.fillMaxWidth(), minHeight = 48.dp, tag = "backup-import")
                    }
                }
                Subtitle("Za uvoz je potrebna lozinka. Podaci ne napuštaju uređaj " +
                    "osim datoteke koju sami odaberete.")
            }
        }
    }
    if (showLanguage) {
        AlertDialog(
            onDismissRequest = { showLanguage = false },
            containerColor = Color(0xFF081A41),
            title = { Text("Jezik", color = Ink.white) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandIcon("check", Modifier.size(20.dp), Ink.cyan)
                        Spacer(Modifier.width(8.dp))
                        Text("Hrvatski", color = Ink.cyan, fontWeight = FontWeight.Bold)
                    }
                    Subtitle("Aplikacija trenutačno podržava hrvatski jezik.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguage = false }) {
                    Text("Zatvori", color = Ink.cyan)
                }
            },
        )
    }
    if (showSound) {
        AlertDialog(
            onDismissRequest = { showSound = false },
            containerColor = Color(0xFF081A41),
            title = { Text("Zvuk i vibracija", color = Ink.white) },
            text = {
                Column {
                    SettingToggle("speaker", "Zvuk", "Zvučni efekt tijekom igre", opts.sound) {
                        controller.updateOptions(controller.options.copy(sound = it))
                    }
                    SettingToggle("sparkles", "Vibracija", "Vibracija pri unosu broja", opts.vibration) {
                        controller.updateOptions(controller.options.copy(vibration = it))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSound = false }) {
                    Text("Zatvori", color = Ink.cyan)
                }
            }
        )
    }
    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            containerColor = Color(0xFF081A41),
            title = { Text("O aplikaciji", color = Ink.white) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Sudoku by Brendigo", color = Ink.white,
                        fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Subtitle("Treniraj mozak svaki dan.")
                    Subtitle("Verzija " + com.brendigo.sudoku.BuildConfig.VERSION_NAME)
                    Subtitle("Igra radi bez registracije i internetske veze.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) {
                    Text("Zatvori", color = Ink.cyan)
                }
            },
        )
    }
    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            containerColor = Color(0xFF081A41),
            title = { Text("Pravila privatnosti", color = Ink.white) },
            text = {
                Column(
                    Modifier.heightIn(max = 370.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Text(
                        "Sudoku by Brendigo možeš igrati bez registracije i internetske veze.",
                        color = Ink.white, fontSize = 14.sp,
                    )
                    Subtitle("Podaci o odigranim igrama, postavkama, napretku i postignućima " +
                        "spremaju se lokalno u privatnu pohranu aplikacije na uređaju.")
                    Subtitle("Aplikacija ne koristi oglase, analitiku, praćenje korisnika niti " +
                        "internetsku dozvolu. Podaci se ne šalju na poslužitelj.")
                    Subtitle("U postavkama možeš ručno izvesti šifriranu sigurnosnu kopiju " +
                        "lozinkom i vratiti je uz svoju potvrdu. Izgubljenu lozinku nije moguće obnoviti.")
                    Subtitle("Deinstalacija bez sigurnosne kopije može trajno ukloniti napredak. " +
                        "Ako odabereš vanjsku/cloud pohranu, njome upravlja odabrani pružatelj.")
                    Subtitle("Izdavač: Brendigo · verzija " +
                        com.brendigo.sudoku.BuildConfig.VERSION_NAME)
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacy = false }) {
                    Text("Zatvori", color = Ink.cyan)
                }
            },
        )
    }
}

@Composable
private fun SettingHeader(icon: String, title: String, subtitle: String,
                          modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(39.dp)
                .background(Ink.cyan.copy(alpha = .13f), RoundedCornerShape(12.dp))
                .border(1.dp, Ink.cyan.copy(alpha = .85f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            BrandIcon(icon, Modifier.size(25.dp), Ink.cyan)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 18.sp, color = Ink.white,
                fontWeight = FontWeight.Bold, maxLines = 2)
            Subtitle(subtitle)
        }
    }
}

@Composable
private fun SettingToggle(icon: String, title: String, caption: String, checked: Boolean, change: (Boolean) -> Unit) {
    val tag = "setting-" + title.lowercase().replace(" ", "-")
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag(tag)
            .toggleable(value = checked, role = Role.Switch, onValueChange = change)
            .semantics { contentDescription = "$title: ${if (checked) "uključeno" else "isključeno"}" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BrandIcon(icon, Modifier.size(21.dp), Ink.cyan)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f).padding(end = 7.dp)) {
            Text(title, fontSize = 14.sp, color = Ink.white,
                fontWeight = FontWeight.SemiBold, maxLines = 2, lineHeight = 18.sp)
            Text(caption, color = Ink.muted, fontSize = 11.sp,
                lineHeight = 14.sp, maxLines = 3)
        }
        // The Material3 default thumb inherited the app's purple theme.
        // Render the brand switch ourselves; the parent remains the one
        // accessible/clickable control and persists its state via change().
        Box(
            Modifier.width(55.dp).height(31.dp)
                .background(
                    if (checked) Brush.horizontalGradient(
                        listOf(Color(0xFF00D8F7), Color(0xFF0868F5)),
                    ) else Brush.horizontalGradient(
                        listOf(Color(0xFF243657), Color(0xFF152444)),
                    ),
                    RoundedCornerShape(25.dp),
                )
                .border(1.dp,
                    if (checked) Ink.cyan else Ink.muted.copy(alpha = .65f),
                    RoundedCornerShape(25.dp)),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(
                Modifier.padding(horizontal = 4.dp).size(23.dp)
                    .background(Color.White, RoundedCornerShape(50)),
            )
        }
    }
}

@Composable
fun SplashPage() {
    // The reference places the 7-3-9 mark at roughly 26% of the viewport
    // height and the spinner close to the bottom. Anchoring both independently
    // avoids moving the logo when the display becomes taller.
    BoxWithConstraints(Modifier.fillMaxSize().testTag("splash-screen")) {
        val shortScreen = maxHeight < 700.dp
        Column(
            Modifier.align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = maxHeight * if (shortScreen) .19f else .255f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Reference: mark near 26% viewport height, enlarged wordmark
            // and a shorter icon/title gap. Preserve fluid scaling on small screens.
            SudokuLogo(if (shortScreen) 92.dp else 122.dp, glowing = true)
            Spacer(Modifier.height(if (shortScreen) 13.dp else 15.dp))
            AccentText("Sudoku", if (shortScreen) 53 else 79)
            Text("by Brendigo", color = Ink.muted, letterSpacing = 5.sp,
                fontSize = if (shortScreen) 13.sp else 16.sp)
            Spacer(Modifier.height(if (shortScreen) 12.dp else 11.dp))
            Text("T R E N I R A J   M O Z A K   S V A K I   D A N",
                color = Ink.cyan, fontSize = if (shortScreen) 9.sp else 15.sp,
                textAlign = TextAlign.Center, maxLines = 2)
        }
        // Android's "remove animations" accessibility preference can reduce
        // the Material indeterminate spinner to a single pixel. Draw a native,
        // deterministic arc so the loading state remains legible in screenshot
        // tests and for users who disable animations.
        Canvas(
            Modifier.align(Alignment.BottomCenter)
                .padding(bottom = if (shortScreen) 24.dp else 70.dp)
                .size(if (shortScreen) 30.dp else 36.dp)
                .testTag("splash-loading")
                .semantics { contentDescription = "Učitavanje igre" }
        ) {
            drawArc(
                color = Ink.blue.copy(alpha = .40f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
            drawArc(
                color = Ink.cyan,
                startAngle = -93f,
                sweepAngle = 145f,
                useCenter = false,
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}
