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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
fun PageContent(content: @Composable ColumnScope.() -> Unit) {
    // The reference footer is anchored over the landscape. Keep the page
    // scrollable with enough bottom padding so the footer hides no controls.
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 28.dp, bottom = 68.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            content()
        }
        Text(
            "V I Š E   O D   I G R E  ·  V J E Ž B A   Z A   U M",
            color = Ink.muted, fontSize = 9.sp, letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp, vertical = 15.dp)
                .testTag("brand-footer"),
            maxLines = 1,
        )
    }
}

/** Consistent checked/unchecked weekly calendar marker for home and daily. */
@Composable
private fun StreakMarker(checked: Boolean, diameter: androidx.compose.ui.unit.Dp = 20.dp) {
    Box(
        Modifier.size(diameter)
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
                AccentText(title, if (title.length > 16) 35 else 39)
                if (description.isNotBlank()) {
                    Subtitle(description, Modifier.fillMaxWidth(), centered = true)
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
                enabled: Boolean = true, minHeight: androidx.compose.ui.unit.Dp = 49.dp) {
    Box(
        modifier.testTag("primary-action")
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

@Composable
fun HomePage(controller: SudokuController) {
    val today = rememberToday()
    PageContent {
        Spacer(Modifier.height(18.dp))
        BrandHeader(Modifier.fillMaxWidth())
        Text(
            "V I Š E  O D  I G R E  ·  V J E Ž B A  Z A  U M",
            color = Ink.muted, fontSize = 10.sp, modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(30.dp))
        val game = controller.game
        if (game != null && !game.completed && !game.failed) {
            NeonPanel(Modifier.fillMaxWidth().testTag("resume-game").clickable { controller.go(Screen.GAME) }
                .heightIn(min = 132.dp), highlight = Ink.blue) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("N A S T A V I  I G R U", color = Ink.cyan,
                        fontSize = 10.sp, letterSpacing = 2.sp)
                    Text("Prekinuta igra", color = Ink.white,
                        fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(difficultyLabels[game.level] + " · " + game.size + "×" + game.size,
                        color = Ink.muted, fontSize = 13.sp)
                    Text("◷ " + formatTime(game.elapsed) + "  ·  Još " +
                        game.board.indices.count { game.board[it] != game.solution[it] } + " polja",
                        color = Ink.muted, fontSize = 12.sp)
                    Spacer(Modifier.height(3.dp))
                    SolidAction("▶   Nastavi", { controller.go(Screen.GAME) },
                        Modifier.fillMaxWidth(.55f), minHeight = 38.dp)
                }
            }
        }
        if (game?.daily == true && controller.hasSavedRegularGame()) {
            NeonPanel(
                Modifier.fillMaxWidth().testTag("resume-classic")
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeTile("Nova igra", "Odaberi težinu", Ink.teal, Modifier.weight(1f)) {
                controller.go(Screen.NEW_GAME)
            }
            HomeTile("Dnevni izazov", "Nova zagonetka svaki dan", Ink.gold, Modifier.weight(1f)) {
                controller.go(Screen.DAILY)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HomeTile("Statistika", "Prati napredak", Ink.blue, Modifier.weight(1f), compact = true) {
                controller.go(Screen.STATS)
            }
            HomeTile("Postignuća", "Osvoji nagrade", Ink.purple, Modifier.weight(1f), compact = true) {
                controller.go(Screen.ACHIEVEMENTS)
            }
            HomeTile("Postavke", "Prilagodi igru", Ink.muted, Modifier.weight(1f), compact = true) {
                controller.go(Screen.SETTINGS)
            }
        }
        NeonPanel(Modifier.fillMaxWidth().heightIn(min = 77.dp), highlight = Ink.blue) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                BrandIcon("flame", Modifier.size(28.dp), Ink.gold)
                Column {
                    Text("Trenutni niz", fontSize = 11.sp, color = Ink.muted)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${controller.currentStreak(today)}", fontSize = 26.sp,
                            color = Ink.gold, fontWeight = FontWeight.Bold)
                        Text(" dana", fontSize = 12.sp, color = Ink.muted)
                    }
                }
                Spacer(Modifier.width(5.dp))
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
                    val completed = controller.stats.completedDailyDates.toSet()
                    listOf("P", "U", "S", "Č", "P", "S", "N").forEachIndexed { index, weekday ->
                        val checked = monday.plusDays(index.toLong()).toString() in completed
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            StreakMarker(checked)
                            Spacer(Modifier.height(4.dp))
                            Text(weekday, fontSize = 9.sp, color = Ink.muted)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BrandIcon("trophy", Modifier.size(21.dp), Ink.gold)
                    Text("Nagrada", fontSize = 9.sp, color = Ink.muted)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun HomeTile(
    title: String, subtitle: String, accent: Color,
    modifier: Modifier = Modifier, compact: Boolean = false, onClick: () -> Unit,
) {
    NeonPanel(modifier.testTag("home-" + title.lowercase().replace(" ", "-"))
        .semantics { contentDescription = "$title, $subtitle"; role = Role.Button }
        .heightIn(min = if (compact) 102.dp else 140.dp).clickable(onClick = onClick), highlight = accent) {
        Column(Modifier.padding(if (compact) 10.dp else 15.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 5.dp)) {
            val iconSize = if (compact) 30.dp else 43.dp
            Box(
                Modifier.size(iconSize)
                    .background(accent.copy(alpha = .13f), RoundedCornerShape(50))
                    .border(1.3.dp, accent.copy(alpha = .95f), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center,
            ) {
                BrandIcon(homeIconFor(title),
                    Modifier.size(if (compact) 22.dp else 29.dp), accent)
            }
            Text(title, color = Ink.white, fontWeight = FontWeight.Bold,
                fontSize = if (compact) 14.sp else 19.sp, maxLines = 2)
            Text(subtitle, color = Ink.muted, fontSize = if (compact) 10.sp else 12.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
fun NewGamePage(controller: SudokuController) {
    val scope = rememberCoroutineScope()
    var size by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(9) }
    var level by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(2) }
    PageContent {
        // Reference hierarchy: back + board size on the same first row,
        // followed by the large title and the six full-width difficulty cards.
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(48.dp).testTag("navigate-back")
                    .semantics { contentDescription = "Natrag"; role = Role.Button }
                    .background(Color(0x99082150), RoundedCornerShape(14.dp))
                    .clickable { controller.go(Screen.HOME) },
                contentAlignment = Alignment.Center,
            ) {
                BrandIcon("back", Modifier.size(24.dp), Ink.muted)
            }
            Row(Modifier.weight(1f).widthIn(max = 204.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf(9, 6, 4).forEach { option ->
                    NeonPanel(
                        Modifier.weight(1f).testTag("size-$option")
                            .heightIn(min = 48.dp)
                            .semantics {
                                contentDescription = "Veličina $option puta $option"
                                role = Role.Button
                            }
                            .clickable { size = option },
                        highlight = if (size == option) Ink.cyan else Ink.line,
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("$option×$option",
                                color = if (size == option) Ink.white else Ink.muted,
                                fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(25.dp))
        Column(Modifier.fillMaxWidth().testTag("screen-nova-igra")) {
            AccentText("Nova igra", 43)
            Text("Odaberi težinu", color = Ink.muted, fontSize = 18.sp)
        }
        Spacer(Modifier.height(5.dp))
        difficultyLabels.forEachIndexed { i, _ ->
            NeonPanel(
                Modifier.fillMaxWidth().testTag("difficulty-$i").heightIn(min = 75.dp)
                    .semantics { contentDescription = "Težina ${difficultyLabels[i]}"; role = Role.Button }
                    .clickable { level = i },
                highlight = difficultyColors[i],
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(48.dp)
                            .background(difficultyColors[i].copy(alpha = .11f), RoundedCornerShape(50))
                            .border(1.5.dp, difficultyColors[i], RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center,
                    ) {
                        BrandIcon(
                            when (i) {
                                0 -> "leaf"
                                1 -> "star"
                                2 -> "double-star"
                                3 -> "triple-star"
                                else -> "crown"
                            },
                            Modifier.size(28.dp), difficultyColors[i],
                        )
                    }
                    Spacer(Modifier.width(17.dp))
                    Column(Modifier.weight(1f)) {
                        Text(difficultyLabels[i], fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Ink.white)
                        Text(listOf(
                            "Savršeno za početnike", "Lagani izazov za vježbu",
                            "Uravnotežen izazov", "Za iskusne igrače",
                            "Ozbiljan izazov", "Samo za najbolje",
                        )[i], color = Ink.muted, fontSize = 13.sp, maxLines = 2)
                    }
                    BrandIcon(if (level == i) "check" else "next", Modifier.size(23.dp), difficultyColors[i])
                }
            }
        }
        SolidAction(
            if (controller.isGenerating) "Pripremam zagonetku…" else "▶   Započni igru",
            { scope.launch { controller.startNew(size, level) } },
            Modifier.fillMaxWidth(), !controller.isGenerating
        )
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
    var showRanking by remember { androidx.compose.runtime.mutableStateOf(false) }
    val formattedDate = remember(today) {
        today.format(DateTimeFormatter.ofPattern("d. MMMM yyyy.", Locale("hr", "HR")))
    }
    PageContent {
        SectionTitle("Dnevni izazov", "Današnji izazov", centered = true,
            onBack = { controller.go(Screen.HOME) }, titleOffset = 18.dp)
        Spacer(Modifier.height(32.dp))
        NeonPanel(Modifier.fillMaxWidth().heightIn(min = 98.dp)
            .testTag("daily-summary"), Ink.blue) {
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
        Spacer(Modifier.height(2.dp))
        NeonPanel(Modifier.fillMaxWidth().heightIn(min = 202.dp)
            .testTag("daily-hero"), Ink.gold) {
            Column(
                Modifier.fillMaxWidth().heightIn(min = 202.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("D A N A Š N J I   I Z A Z O V",
                    color = Ink.muted, fontSize = 11.sp, maxLines = 1)
                AccentText("Misteriozna planina", 28)
                Text("Riješi današnju sudoku zagonetku i osvoji posebnu nagradu!",
                    color = Ink.muted, fontSize = 14.sp, lineHeight = 19.sp,
                    maxLines = 2)
                Spacer(Modifier.height(13.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DailyMetric("statistics", "TEŽINA", "Srednja",
                        Ink.gold, Modifier.weight(1f))
                    DailyMetric("timer", "PROCIJENJENO VRIJEME", "12 min",
                        Ink.cyan, Modifier.weight(1f))
                    DailyMetric("star", "NAGRADA", "50 bodova",
                        Ink.gold, Modifier.weight(1f))
                }
            }
        }
        NeonPanel(Modifier.fillMaxWidth().heightIn(min = 122.dp), Ink.blue) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 13.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
                val completedDays = controller.stats.completedDailyDates.toSet().ifEmpty {
                    setOf(controller.stats.lastDaily)
                }
                val completedInWeek = (0..6).count {
                    monday.plusDays(it.toLong()).toString() in completedDays
                }
                Row(
                    Modifier.fillMaxWidth().drawBehind {
                        val lineY = size.height * .48f
                        drawLine(Ink.blue.copy(alpha = .65f),
                            Offset(size.width * .07f, lineY),
                            Offset(size.width * .93f, lineY),
                            strokeWidth = 2.dp.toPx())
                        if (completedInWeek > 1) {
                            drawLine(Ink.cyan.copy(alpha = .85f),
                                Offset(size.width * .07f, lineY),
                                Offset(size.width * (.07f +
                                    .86f * (completedInWeek - 1) / 6f), lineY),
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
                                Spacer(Modifier.height(7.dp))
                                StreakMarker(date.toString() in completedDays,
                                    diameter = 31.dp)
                                Spacer(Modifier.height(7.dp))
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
    PageContent {
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
            StatTile("Ukupno vrijeme", formatTime(stats.totalSeconds.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()),
                Ink.blue, Modifier.weight(1f)) {
                metricInfo = "Ukupno vrijeme provedeno u dovršenim Sudoku partijama."
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
    NeonPanel(modifier.heightIn(min = 172.dp), Ink.blue) {
        Column(Modifier.fillMaxSize().padding(horizontal = 9.dp, vertical = 10.dp)) {
            Text("Riješene igre po težini", color = Ink.white,
                fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2)
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth().height(119.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom) {
                counts.forEachIndexed { index, amount ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(amount.toString(), color = Ink.white, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier.fillMaxWidth(.75f)
                                .height((8 + 65 * amount / ceiling).dp)
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
    NeonPanel(modifier.heightIn(min = 172.dp), Ink.cyan) {
        Column(Modifier.fillMaxSize().padding(9.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Postotak dovršenosti", color = Ink.white,
                fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2)
            Spacer(Modifier.height(5.dp))
            Box(Modifier.fillMaxWidth().height(106.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(84.dp)) {
                    drawArc(Ink.blue.copy(alpha = .27f), 0f, 360f, false,
                        style = Stroke(9.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(Ink.cyan, -90f, fraction * 360f, false,
                        style = Stroke(12.dp.toPx(), cap = StrokeCap.Round))
                }
                Text("$percentage%", fontSize = 24.sp, color = Ink.white,
                    fontWeight = FontWeight.ExtraBold)
            }
            Subtitle("${stats.finished} dovršeno", Modifier.fillMaxWidth(), centered = true)
            Subtitle("${(stats.played - stats.finished).coerceAtLeast(0)} nedovršeno",
                Modifier.fillMaxWidth(), centered = true)
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, color: Color,
                     modifier: Modifier, onClick: () -> Unit) {
    NeonPanel(modifier.heightIn(min = 86.dp)
        .semantics { contentDescription = "$label: $value"; role = Role.Button }
        .clickable(onClick = onClick), color) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandIcon(when (label) {
                "Odigrane igre" -> "gamepad"
                "Najbolje vrijeme" -> "timer"
                "Ukupno vrijeme" -> "timer"
                "Dnevni niz" -> "flame"
                else -> "statistics"
            }, Modifier.size(37.dp), color)
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, color = Ink.muted, fontSize = 12.sp, maxLines = 1)
                Text(value, color = Ink.white, fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold, maxLines = 1)
            }
            Text("›", color = color, fontSize = 17.sp)
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
    PageContent {
        SectionTitle("Postignuća", centered = true, onBack = { controller.go(Screen.HOME) })
        NeonPanel(Modifier.fillMaxWidth().heightIn(min = 75.dp)
            .testTag("achievement-summary"), Ink.blue) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                BrandIcon("trophy", Modifier.size(37.dp), Ink.gold)
                Column(Modifier.weight(1f)) {
                    Subtitle("Ukupno bodova")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$points", fontSize = 27.sp,
                            color = Ink.gold, fontWeight = FontWeight.ExtraBold)
                        Text(" / 1000", fontSize = 13.sp, color = Ink.muted)
                    }
                }
                Box(Modifier.width(1.dp).height(49.dp)
                    .background(Ink.blue.copy(alpha = .80f)))
                BrandIcon("star", Modifier.size(34.dp), Ink.cyan)
                Column(Modifier.weight(.75f)) {
                    Subtitle("Otključano")
                    Text("${data.count { it.current >= it.goal }} / ${data.size}", fontSize = 26.sp,
                        color = Ink.cyan, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        visible.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { achievement ->
                    val unlocked = achievement.current >= achievement.goal
                    NeonPanel(Modifier.weight(1f).height(149.dp)
                        .testTag("achievement-" + achievement.title.lowercase().replace(" ", "-"))
                        .semantics {
                            contentDescription = achievement.title +
                                (if (unlocked) ", otključano" else ", zaključano")
                            role = Role.Button
                        }
                        .clickable { selectedAchievement = achievement },
                        if (unlocked) achievement.tint else Ink.blue.copy(alpha = .52f)) {
                        Column(Modifier.fillMaxSize().padding(9.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                BrandIcon(if (unlocked) achievementIconFor(achievement.title) else "lock",
                                    Modifier.size(41.dp),
                                    if (unlocked) achievement.tint else Ink.muted.copy(alpha = .73f))
                                Row(
                                    Modifier.background(Ink.blue.copy(alpha = .23f),
                                            RoundedCornerShape(30.dp))
                                        .border(1.dp, if (unlocked) achievement.tint
                                            else Ink.muted.copy(alpha = .55f),
                                            RoundedCornerShape(30.dp))
                                        .padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    BrandIcon("star", Modifier.size(13.dp),
                                        if (unlocked) achievement.tint else Ink.muted)
                                    Spacer(Modifier.width(3.dp))
                                    Text("${achievement.reward}",
                                        color = if (unlocked) achievement.tint else Ink.muted,
                                        fontSize = 11.sp)
                                }
                            }
                            Text(achievement.title, color = Ink.white, fontSize = 15.sp,
                                fontWeight = FontWeight.Bold, lineHeight = 18.sp, maxLines = 2)
                            Text(achievement.text, color = Ink.muted, fontSize = 11.sp,
                                lineHeight = 13.sp, modifier = Modifier.weight(1f), maxLines = 2)
                            val progress = (achievement.current.toFloat() /
                                achievement.goal.toFloat()).coerceIn(0f, 1f)
                            Box(Modifier.fillMaxWidth().height(7.dp).background(Ink.blue.copy(alpha = .25f),
                                RoundedCornerShape(12.dp))) {
                                Box(Modifier.fillMaxWidth(progress).fillMaxHeight()
                                    .background(if (unlocked) achievement.tint else Ink.muted,
                                    RoundedCornerShape(12.dp)))
                            }
                            Text("${achievement.current.coerceAtMost(achievement.goal)} / ${achievement.goal}",
                                color = Ink.muted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        NeonPanel(Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .testTag("achievements-show-all")
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
fun SettingsPage(controller: SudokuController) {
    val opts = controller.options
    var showPrivacy by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showSound by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showLanguage by remember { androidx.compose.runtime.mutableStateOf(false) }
    PageContent {
        SectionTitle("Postavke", "Prilagodi igru svojim željama", centered = true, onBack = { controller.back() })
        NeonPanel(Modifier.fillMaxWidth(), Ink.blue) {
            Column(Modifier.padding(9.dp)) {
                SettingHeader("sparkles", "Izgled", "Prilagodi vizualni doživljaj igre")
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
                SettingToggle("note", "Automatske bilješke", "Ukloni riješene kandidate iz bilješki", opts.autoNotes) {
                    controller.updateOptions(opts.copy(autoNotes = it))
                }
                SettingToggle("warning", "Provjera greške", "Označi netočne unose i prati pogreške", opts.checkMistakes) {
                    controller.updateOptions(opts.copy(checkMistakes = it))
                }
                SettingToggle("grid", "Isticanje brojeva", "Naglašavanje jednakih brojeva na ploči", opts.highlightEquals) {
                    controller.updateOptions(opts.copy(highlightEquals = it))
                }
            }
        }
        NeonPanel(
            Modifier.fillMaxWidth().heightIn(min = 58.dp)
                .testTag("sound-vibration")
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
                    Subtitle("Uključi ili isključi efekte", centered = false)
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
                    Subtitle("Jezik aplikacije")
                }
                Text("Hrvatski", color = Ink.muted, fontSize = 13.sp)
                Spacer(Modifier.width(6.dp))
                BrandIcon("next", Modifier.size(16.dp), Ink.teal)
            }
        }
        NeonPanel(Modifier.fillMaxWidth(), Ink.blue) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("O aplikaciji", fontSize = 18.sp, color = Ink.white, fontWeight = FontWeight.Bold)
                Subtitle("Sudoku by Brendigo")
                Subtitle("Verzija " + com.brendigo.sudoku.BuildConfig.VERSION_NAME)
                NeonPanel(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .testTag("privacy-policy").clickable { showPrivacy = true },
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
                    Subtitle("Deinstaliranjem aplikacije lokalni se podaci u pravilu brišu. " +
                        "Postavke Android sustava mogu utjecati na ponašanje pohrane.")
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
private fun SettingHeader(icon: String, title: String, subtitle: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
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
        Column {
            Text(title, fontSize = 18.sp, color = Ink.white, fontWeight = FontWeight.Bold)
            Subtitle(subtitle)
        }
    }
}

@Composable
private fun SettingToggle(icon: String, title: String, caption: String, checked: Boolean, change: (Boolean) -> Unit) {
    val tag = "setting-" + title.lowercase().replace(" ", "-")
    Row(
        Modifier.fillMaxWidth().heightIn(min = 54.dp).testTag(tag)
            .toggleable(value = checked, role = Role.Switch, onValueChange = change)
            .semantics { contentDescription = "$title: ${if (checked) "uključeno" else "isključeno"}" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BrandIcon(icon, Modifier.size(21.dp), Ink.cyan)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = Ink.white, fontWeight = FontWeight.SemiBold)
            Text(caption, color = Ink.muted, fontSize = 11.sp, lineHeight = 13.sp)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
fun SplashPage() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            SudokuLogo(112.dp)
            Spacer(Modifier.height(30.dp))
            AccentText("Sudoku", 58)
            Text("by Brendigo", color = Ink.muted, letterSpacing = 5.sp, fontSize = 16.sp)
            Spacer(Modifier.height(32.dp))
            Text("T R E N I R A J   M O Z A K   S V A K I   D A N",
                color = Ink.cyan, fontSize = 11.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(90.dp))
            CircularProgressIndicator(color = Ink.cyan, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
        }
    }
}
