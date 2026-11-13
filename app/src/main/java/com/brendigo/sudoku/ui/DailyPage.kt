package com.brendigo.sudoku.ui

import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.sudoku.data.Screen
import com.brendigo.sudoku.data.SudokuController
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
private fun DailyMetric(
    icon: String, label: String, value: String,
    color: Color, modifier: Modifier = Modifier,
) {
    // The three data tiles in the reference have recessed navy surfaces,
    // not bright cyan outlines competing with the main illustration.
    // The reference uses three recessed dark surfaces with minimal blue
    // outlines, not three strongly raised individual neon buttons.
    DailyFrame(modifier.heightIn(min = 59.dp), Ink.blue.copy(alpha = .32f), radius = 10.dp) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandIcon(
                icon,
                Modifier.size(30.dp).drawBehind {
                    val center = Offset(size.width * .5f, size.height * .5f)
                    drawCircle(
                        Brush.radialGradient(
                            listOf(color.copy(alpha = .27f), Color.Transparent),
                            center = center, radius = size.width * .80f,
                        ),
                        center = center, radius = size.width * .80f,
                    )
                },
                color,
            )
            Spacer(Modifier.width(7.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(label, color = Ink.muted, fontSize = 9.sp,
                    lineHeight = 11.sp, maxLines = 2)
                Text(value, color = Ink.white, fontSize = 14.sp,
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
    PageContent(topPadding = 21.dp, showFooter = false, horizontalPadding = 18.dp) {
        SectionTitle("Dnevni izazov", "Današnji izazov", centered = true,
            onBack = { controller.go(Screen.HOME) }, titleOffset = 16.dp)
        Spacer(Modifier.height(25.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val compactSummary = maxWidth < 355.dp ||
                LocalDensity.current.fontScale >= 1.3f
            DailyFrame(Modifier.fillMaxWidth().heightIn(min = if (compactSummary) 146.dp else 98.dp)
                .testTag("daily-summary")) {
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
                                AchievementBadgeArt("Vrući niz", Modifier.size(30.dp).testTag("daily-streak-3d-badge"), Ink.gold)
                                Spacer(Modifier.width(5.dp))
                                Column {
                                    Text("${controller.currentStreak(today)}", color = Ink.gold,
                                        fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                                    Text("DANA U NIZU", color = Ink.muted, fontSize = 9.sp)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DailyRewardEmblem(
                                    Modifier.size(30.dp).testTag("daily-reward-emblem")
                                )
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
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        Row(Modifier.weight(1.43f), verticalAlignment = Alignment.CenterVertically) {
                            BrandIcon("calendar", Modifier.size(27.dp), Ink.blue)
                            Spacer(Modifier.width(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("D A N A S", color = Ink.muted, fontSize = 10.sp)
                                Text(formattedDate, color = Ink.white,
                                    fontWeight = FontWeight.Bold, fontSize = 13.sp,
                                    lineHeight = 15.sp, maxLines = 2)
                            }
                        }
                        Box(Modifier.width(1.dp).height(60.dp)
                            .background(Ink.blue.copy(alpha = .66f)))
                        Row(Modifier.weight(.97f), verticalAlignment = Alignment.CenterVertically) {
                            AchievementBadgeArt("Vrući niz", Modifier.size(43.dp).testTag("daily-streak-3d-badge"), Ink.gold)
                            Spacer(Modifier.width(2.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("${controller.currentStreak(today)}", color = Ink.gold,
                                    fontSize = 37.sp, fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 39.sp)
                                Text("DANA\nU NIZU", color = Ink.muted,
                                    fontSize = 9.sp, lineHeight = 11.sp, maxLines = 2)
                            }
                        }
                        Row(Modifier.weight(1.15f), verticalAlignment = Alignment.CenterVertically) {
                            DailyRewardEmblem(
                                Modifier.size(52.dp).testTag("daily-reward-emblem")
                            )
                            Spacer(Modifier.width(5.dp))
                            Column {
                                Text("DANAŠNJA\nNAGRADA", color = Ink.gold,
                                    fontSize = 9.sp, lineHeight = 11.sp, maxLines = 2)
                                Spacer(Modifier.height(2.dp))
                                Text("Zlatna značka", color = Ink.white,
                                    fontSize = 11.sp, lineHeight = 13.sp, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val compactHero = maxWidth < 355.dp ||
                LocalDensity.current.fontScale >= 1.3f
        DailyFrame(Modifier.fillMaxWidth().heightIn(min = 202.dp)
            .testTag("daily-hero"), Ink.blue) {
            DailyHeroBackdrop()
            Column(
                Modifier.fillMaxWidth().heightIn(min = 202.dp)
                    // On the full-width reference there is only a narrow
                    // gap below the three metric chips, not 12dp of empty
                    // card. Retain the accessible compact spacing.
                    .padding(start = 16.dp, top = 16.dp, end = 8.dp,
                        bottom = if (compactHero) 12.dp else 3.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text("D A N A Š N J I   I Z A Z O V",
                    color = Ink.muted, fontSize = 11.sp, maxLines = 1)
                Text(
                    "Misteriozna planina",
                    fontSize = if (compactHero) 24.sp else 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink.white,
                    maxLines = if (compactHero) 2 else 1,
                )
                Text("Riješi današnju sudoku zagonetku i osvoji posebnu nagradu!",
                    modifier = Modifier.fillMaxWidth(if (compactHero) 1f else .62f),
                    color = Ink.muted, fontSize = 14.sp, lineHeight = 19.sp,
                    maxLines = 2)
                // Pixel diff from the actual 941×1672 emulator showed a
                // +0.83 RGB MAE regression and a +2.27 bottom-third shift
                // when this spacer was reduced to 18dp. Restore the better
                // measured vertical cadence; keep the subtle metric edges.
                Spacer(Modifier.height(if (compactHero) 32.dp else 30.dp))
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
                        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                            DailyMetric("statistics", "TEŽINA", "Srednja",
                                Ink.gold, Modifier.weight(.98f).testTag("daily-difficulty-metric"))
                            DailyMetric("timer", "PROCIJENJENO VRIJEME", "12 min",
                                Ink.cyan, Modifier.weight(1.10f).testTag("daily-time-metric"))
                            DailyMetric("star-solid", "NAGRADA", "50 bodova",
                                Ink.gold, Modifier.weight(1.10f).testTag("daily-reward-metric"))
                        }
                    }
                }
            }
        }
        }
        DailyFrame(Modifier.fillMaxWidth().heightIn(min = 126.dp)
            .testTag("daily-streak-panel")) {
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
                // The 941px reference keeps seven equal-width native day cells.
                // At 240dp a fixed 31dp circle would require 217dp before
                // dates, exceeding the available width after page/card gutters.
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val dayCellWidth = maxWidth / 7
                    val markerDiameter = minOf(31.dp,
                        (dayCellWidth - 2.dp).coerceAtLeast(18.dp))
                    val dayLabelSize = when {
                        dayCellWidth < 28.dp -> 8.sp
                        dayCellWidth < 35.dp -> 9.sp
                        else -> 10.sp
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
                            Column(
                                Modifier.weight(1f).testTag("daily-weekday-$index"),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(label, color = Ink.muted,
                                    fontSize = dayLabelSize, maxLines = 1)
                                Spacer(Modifier.height(3.dp))
                                DailyWeekMarker(date.toString() in completedDays,
                                    dayLabel = label, diameter = markerDiameter)
                                Spacer(Modifier.height(3.dp))
                                Text("${date.dayOfMonth}.${date.monthValue}.",
                                    color = if (date == today) Ink.white else Ink.muted,
                                    fontSize = dayLabelSize, maxLines = 1,
                                    fontWeight = if (date == today)
                                        FontWeight.Bold else FontWeight.Normal)
                            }
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
            DailyStartAction(
                title = when {
                    controller.dailyAlreadyCompleted() -> "✓ Današnji izazov riješen"
                    controller.isGenerating -> "Učitavanje…"
                    controller.hasSavedDailyFor(today) -> "Nastavi današnji izazov"
                    else -> "Pokreni izazov"
                },
                onClick = { scope.launch { controller.startNew(9, 2, daily = true) } },
                modifier = Modifier.fillMaxWidth(.80f),
                enabled = !controller.isGenerating && !controller.dailyAlreadyCompleted(),
            )
        }
        BoxWithConstraints(Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center) {
            // The owner's normal-width reference keeps its compact 63% pill.
            // Small devices and enlarged fonts need room for the actual label.
            val compactLeaderboard = maxWidth < 350.dp ||
                LocalDensity.current.fontScale >= 1.3f
            DailyFrame(
                Modifier.fillMaxWidth(if (compactLeaderboard) .95f else .63f)
                    .heightIn(min = 48.dp)
                    .testTag("daily-leaderboard")
                    .semantics {
                        contentDescription = "Otvori osobnu rang-listu"
                        role = Role.Button
                    }
                    .clickable { showRanking = true },
                // Rounded violet accent matches the source leaderboard pill
                // while retaining the actual offline ranking dialog.
                // The source pill outlines in a bright cobalt blue,
                // while the trophy itself remains violet.
                highlight = Ink.blue,
                radius = 25.dp,
            ) {
                Row(
                    Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Box(
                        Modifier.size(33.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(Ink.purple.copy(alpha = .30f), Color.Transparent)
                                ),
                                RoundedCornerShape(50),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        BrandIcon("trophy", Modifier.size(29.dp), Ink.purple)
                    }
                    Spacer(Modifier.width(9.dp))
                    Text("Pogledaj rang-listu", color = Ink.white,
                        modifier = Modifier.testTag("daily-leaderboard-label"),
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compactLeaderboard) 14.sp else 16.sp,
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
