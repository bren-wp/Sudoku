package com.brendigo.sudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.sudoku.data.Screen
import com.brendigo.sudoku.data.Statistics
import com.brendigo.sudoku.data.SudokuController

private data class PreviewAward(
    val title: String, val icon: String,
    val current: Int, val goal: Int, val tint: Color,
)

@Composable
fun StatsPage(controller: SudokuController) {
    val stats = controller.stats
    // Keep open Statistics dialogs stable across device rotation and
    // activity recreation; actual data always comes from SudokuController.
    var metricInfo by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf<String?>(null)
    }
    var showHistory by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(false)
    }
    PageContent(topPadding = 23.dp, horizontalPadding = 18.dp) {
        SectionTitle("Statistika", "Tvoj Sudoku put u brojkama", centered = true,
            onBack = { controller.go(Screen.HOME) }, titleOffset = 5.dp)
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
            if (maxWidth >= 348.dp && LocalDensity.current.fontScale < 1.3f) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DifficultyChart(stats, Modifier.weight(1.18f))
                    CompletionRing(stats, Modifier.weight(.92f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DifficultyChart(stats, Modifier.fillMaxWidth())
                    CompletionRing(stats, Modifier.fillMaxWidth())
                }
            }
        }
        StatisticsSurface(
            Modifier.fillMaxWidth().heightIn(
                min = if (stats.history.isEmpty()) 140.dp else 136.dp
            ).testTag("stats-history-panel")
        ) {
            Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .testTag("open-game-history")
                        .semantics { contentDescription = "Prikaži povijest dovršenih igara"; role = Role.Button }
                        .clickable { showHistory = true },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BrandIcon("statistics", Modifier.size(21.dp), Ink.cyan)
                    Spacer(Modifier.width(8.dp))
                    Text("Nedavni napredak", color = Ink.white, fontSize = 16.sp,
                        modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("Pogledaj sve", color = Ink.cyan, fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    BrandIcon("next", Modifier.size(15.dp), Ink.cyan)
                }
                if (stats.history.isEmpty()) {
                    Box(Modifier.testTag("stats-history-empty")) {
                        Subtitle("Još nema dovršenih igara.")
                    }
                }
                stats.history.take(4).forEach { record ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 26.dp)
                            .background(Color(0xA6061A42), RoundedCornerShape(7.dp))
                            .border(.5.dp, Ink.blue.copy(alpha = .38f),
                                RoundedCornerShape(7.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BrandIcon("grid", Modifier.size(17.dp), difficultyColors[record.level])
                        Spacer(Modifier.width(8.dp))
                        Text(difficultyLabels[record.level],
                            color = difficultyColors[record.level],
                            modifier = Modifier.weight(1f), fontSize = 11.sp,
                            fontWeight = FontWeight.Bold, maxLines = 1)
                        BrandIcon("timer", Modifier.size(15.dp), Ink.muted)
                        Spacer(Modifier.width(4.dp))
                        Text(formatTime(record.seconds), color = Ink.muted, fontSize = 10.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(record.date, color = Ink.muted, fontSize = 10.sp, maxLines = 1)
                        BrandIcon("next", Modifier.size(12.dp), Ink.cyan)
                    }
                }
            }
        }
        StatisticsSurface(Modifier.fillMaxWidth().heightIn(min = 108.dp)
            .testTag("stats-achievements")
            .semantics {
                contentDescription = "Otvori svih 24 postignuća"
                role = Role.Button
            }
            .clickable { controller.go(Screen.ACHIEVEMENTS) }) {
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
                val previews = listOf(
                    // Reference-like progression previews with real stats.
                    // These are targets, not fabricated achieved badges:
                    // finished games, fast wins, daily streak and master wins.
                    PreviewAward("Početnik", "star", stats.finished, 50, Ink.teal),
                    PreviewAward("Brzi um", "timer", stats.fastWins, 10, Ink.blue),
                    PreviewAward("Pobjednički niz", "flame-solid", stats.bestStreak, 10, Ink.purple),
                    PreviewAward("Majstor", "lock", stats.winsByLevel[5], 1, Ink.muted),
                )
                // Four across on the owner's 941px reference; a legible 2x2
                // grid on narrower devices or when the system font is enlarged.
                BoxWithConstraints(Modifier.fillMaxWidth().testTag("stats-award-grid")) {
                    // Four previews match the reference at full phone width;
                    // 350–389dp need two rows to preserve icon/title legibility.
                    val compactAwards = maxWidth < 390.dp ||
                        LocalDensity.current.fontScale >= 1.3f
                    if (compactAwards) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            previews.chunked(2).forEachIndexed { row, pair ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    pair.forEachIndexed { column, award ->
                                        StatsAwardPreview(
                                            award,
                                            Modifier.weight(1f)
                                                .testTag("stats-award-preview-${row * 2 + column}"),
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            previews.forEachIndexed { index, award ->
                                StatsAwardPreview(
                                    award,
                                    Modifier.weight(1f).testTag("stats-award-preview-$index"),
                                )
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


/** A real, locked-or-earned achievement tile; never fabricate progress. */
@Composable
private fun StatsAwardPreview(award: PreviewAward, modifier: Modifier) {
    val fraction = (award.current.toFloat() / award.goal).coerceIn(0f, 1f)
    val earned = fraction >= 1f
    StatisticsSurface(
        modifier.heightIn(
            min = if (LocalDensity.current.fontScale >= 1.3f) 92.dp else 72.dp
        ),
        // Original's four previews retain their independent teal, blue,
        // purple and cyan accents even when the underlying award is locked.
        accent = award.tint.copy(alpha = if (earned) 1f else .72f),
        background = listOf(
            award.tint.copy(alpha = if (earned) .27f else .17f),
            Color(0xFF081C40),
        ),
        corner = 9.dp,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            // The four reference cards pair their distinctive award artwork
            // with the title on one row. Keep the *actual* vector visible
            // even when locked; a small corner padlock conveys status.
            // Never replace four different awards with four identical locks.
            Row(
                // The preview column is measured in a scrollable parent:
                // using weight here could collapse the icon/title row to 0dp
                // on some devices. A bounded native row always renders it.
                Modifier.fillMaxWidth().heightIn(min = 31.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(Modifier.size(29.dp), contentAlignment = Alignment.Center) {
                    if (award.title == "Pobjednički niz") {
                        AchievementBadgeArt(
                            "Vrući niz",
                            Modifier.size(28.dp).testTag("stats-award-streak-3d-badge"),
                            award.tint,
                        )
                    } else {
                        BrandIcon(
                        award.icon,
                        Modifier.size(28.dp).drawBehind {
                            if (earned || fraction > 0f) {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                drawCircle(
                                    Brush.radialGradient(
                                        listOf(award.tint.copy(alpha = if (earned) .36f else .19f),
                                            Color.Transparent),
                                        center = center, radius = size.width * .75f,
                                    ),
                                    center = center, radius = size.width * .75f,
                                )
                            }
                        },
                        award.tint.copy(alpha = if (earned) 1f else .83f),
                        )
                    }
                    if (!earned && award.icon != "lock") {
                        Box(
                            Modifier.align(Alignment.BottomEnd)
                                .size(12.dp)
                                .background(Color(0xF0041636), RoundedCornerShape(3.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BrandIcon("lock", Modifier.size(10.dp), Ink.muted)
                        }
                    }
                }
                Text(
                    award.title,
                    modifier = Modifier.weight(1f),
                    color = Ink.white,
                    fontSize = 10.sp,
                    lineHeight = 11.sp,
                    maxLines = 2,
                )
            }
            Box(
                Modifier.fillMaxWidth().height(4.dp)
                    .background(Ink.blue.copy(alpha = .30f),
                        RoundedCornerShape(8.dp)),
            ) {
                if (fraction > 0f) {
                    Box(
                        Modifier.fillMaxWidth(fraction).fillMaxHeight()
                            .then(if (earned) Modifier.shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(8.dp),
                                ambientColor = award.tint.copy(alpha = .39f),
                                spotColor = award.tint.copy(alpha = .41f),
                            ) else Modifier)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(award.tint.copy(alpha = .69f), award.tint)
                                ),
                                RoundedCornerShape(8.dp),
                            ),
                    )
                }
            }
            Text(
                "${award.current.coerceAtMost(award.goal)}/${award.goal}",
                color = Ink.muted, fontSize = 9.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DifficultyChart(stats: Statistics, modifier: Modifier) {
    val counts = listOf(
        stats.winsByLevel[0] + stats.winsByLevel[1],
        stats.winsByLevel[2],
        stats.winsByLevel[3],
        stats.winsByLevel[4] + stats.winsByLevel[5],
    )
    val colors = listOf(Ink.teal, Ink.blue, Ink.purple, Ink.gold)
    val names = listOf("Lako", "Srednje", "Teško", "Stručno")
    val ceiling = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
    StatisticsSurface(
        modifier.height(192.dp).testTag("stats-difficulty-chart")
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandIcon("statistics", Modifier.size(20.dp), Ink.cyan)
                Spacer(Modifier.width(7.dp))
                Text("Riješene igre po težini", color = Ink.white,
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2)
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(116.dp)) {
                StatisticsChartGrid(Modifier.fillMaxSize().padding(bottom = 16.dp))
                Row(
                    Modifier.fillMaxSize().padding(horizontal = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    counts.forEachIndexed { index, count ->
                        Column(Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(count.toString(), color = Ink.white, fontSize = 11.sp, maxLines = 1)
                            Spacer(Modifier.height(3.dp))
                            val heightDp = if (count == 0) 2.dp else
                                (10 + 61 * count / ceiling).dp
                            Box(
                                Modifier.fillMaxWidth(.73f).height(heightDp)
                                    .then(if (count > 0) Modifier.shadow(
                                        elevation = 3.dp,
                                        shape = RoundedCornerShape(
                                            topStart = 5.dp, topEnd = 5.dp),
                                        ambientColor = colors[index].copy(alpha = .41f),
                                        spotColor = colors[index].copy(alpha = .34f),
                                    ) else Modifier)
                                    .background(
                                        Brush.verticalGradient(listOf(
                                            colors[index].copy(alpha = .98f),
                                            colors[index].copy(alpha = .52f),
                                        )),
                                        RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp),
                                    )
                                    // Narrow glossy lip, not a static chart image.
                                    .border(
                                        .6.dp,
                                        colors[index].copy(alpha = .82f),
                                        RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp),
                                    )
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(names[index], color = Ink.muted, fontSize = 9.sp,
                                maxLines = 1, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionRing(stats: Statistics, modifier: Modifier) {
    val fraction = if (stats.played == 0) 0f else
        (stats.finished.toFloat() / stats.played).coerceIn(0f, 1f)
    val percentage = (fraction * 100).toInt()
    StatisticsSurface(
        modifier.height(192.dp).testTag("stats-completion-ring")
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BrandIcon("pie-chart", Modifier.size(20.dp), Ink.cyan)
                Spacer(Modifier.width(6.dp))
                Text("Postotak dovršenosti", color = Ink.white,
                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    maxLines = 1, lineHeight = 12.sp)
            }
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(98.dp),
                contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(94.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Ink.cyan.copy(alpha = .10f), Color.Transparent),
                            center = center, radius = size.width * .75f,
                        ),
                        radius = size.width * .75f,
                        center = center,
                    )
                    drawArc(Ink.blue.copy(alpha = .28f), 0f, 360f, false,
                        style = Stroke(9.dp.toPx(), cap = StrokeCap.Round))
                    if (fraction > 0f) {
                        drawArc(
                            Ink.cyan.copy(alpha = .19f),
                            -90f, fraction * 360f, false,
                            style = Stroke(15.dp.toPx(), cap = StrokeCap.Round),
                        )
                        drawArc(
                            Brush.sweepGradient(listOf(Ink.blue, Ink.cyan, Ink.blue)),
                            -90f, fraction * 360f, false,
                            style = Stroke(11.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$percentage%", color = Ink.white, fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("stats-completion-percent"))
                    Text("Dovršene igre", color = Ink.muted, fontSize = 9.sp, maxLines = 1)
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(9.dp).background(Ink.cyan, RoundedCornerShape(50)))
                Spacer(Modifier.width(5.dp))
                Text("Dovršene igre", color = Ink.muted,
                    fontSize = 10.sp, modifier = Modifier.weight(1f), maxLines = 1)
                Text("${stats.finished}", color = Ink.white, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(9.dp).background(Ink.blue.copy(alpha = .52f),
                    RoundedCornerShape(50)))
                Spacer(Modifier.width(5.dp))
                Text("Prekinute igre", color = Ink.muted,
                    fontSize = 10.sp, modifier = Modifier.weight(1f), maxLines = 1)
                Text("${(stats.played - stats.finished).coerceAtLeast(0)}",
                    color = Ink.white, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String, value: String, color: Color, modifier: Modifier,
    onClick: () -> Unit,
) {
    val id = when (label) {
        "Odigrane igre" -> "played"
        "Najbolje vrijeme" -> "best"
        "Ukupno vrijeme" -> "total"
        "Dnevni niz" -> "streak"
        else -> "other"
    }
    StatisticsSurface(
        modifier.height(if (LocalDensity.current.fontScale >= 1.3f) 110.dp else 84.dp)
            .testTag("stat-tile-$id")
            .semantics { contentDescription = "$label: $value"; role = Role.Button }
            .clickable(onClick = onClick),
        accent = color, background = metricCardColors(id), corner = 12.dp,
    ) {
        if (id == "streak") {
            // Reference-inspired native scenery, independently composited
            // BEHIND real counters, labels and the clickable StatTile.
            // No copied screenshot, no simulated user progression.
            Canvas(Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val moon = Offset(w * .83f, h * .30f)
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0x99F6A83E), Color(0x34F5AB4D),
                            Color.Transparent),
                        center = moon, radius = w * .43f,
                    ),
                    radius = w * .43f, center = moon,
                )
                drawCircle(Color(0xFFFBCE65).copy(alpha = .78f),
                    radius = h * .18f, center = moon)
                repeat(3) { layer ->
                    val y = h * (.62f + layer * .09f)
                    val ridge = Path().apply {
                        moveTo(w * .34f, h)
                        lineTo(w * .34f, y)
                        for (index in 0..32) {
                            val x = w * (.34f + .66f * index / 32f)
                            val wave = kotlin.math.sin(index * .56f + layer * 1.1f) * .07f +
                                kotlin.math.sin(index * .19f + 1.4f) * .045f
                            lineTo(x, y - h * wave)
                        }
                        lineTo(w, h)
                        close()
                    }
                    drawPath(
                        ridge, Color(0xFF1D2023)
                            .copy(alpha = .22f + layer * .16f),
                    )
                }
                val roof = Path().apply {
                    moveTo(w * .897f, h * .54f)
                    quadraticBezierTo(w * .942f, h * .46f, w * .985f, h * .54f)
                    lineTo(w * .973f, h * .56f)
                    lineTo(w * .908f, h * .56f)
                    close()
                }
                drawPath(roof, Color(0xFF161B23).copy(alpha = .90f))
                drawRect(Color(0xFF161B23).copy(alpha = .86f),
                    topLeft = Offset(w * .925f, h * .55f),
                    size = androidx.compose.ui.geometry.Size(w * .032f, h * .22f))
            }
        }
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val compact = maxWidth < 176.dp
            Row(
                Modifier.fillMaxSize().padding(
                    horizontal = if (compact) 9.dp else 13.dp,
                    vertical = 5.dp,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(if (compact) 35.dp else 46.dp)
                        .drawBehind {
                            val center = Offset(size.width * .5f, size.height * .5f)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(color.copy(alpha = .30f), Color.Transparent),
                                    center = center, radius = size.width * .78f,
                                ),
                                radius = size.width * .78f,
                                center = center,
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (id == "streak") {
                        AchievementBadgeArt(
                            "Vrući niz",
                            Modifier.fillMaxSize().testTag("stats-streak-3d-badge"),
                            color,
                        )
                    } else {
                        BrandIcon(
                            when (id) {
                                "played" -> "gamepad"
                                "best", "total" -> "timer"
                                else -> "statistics"
                            },
                            Modifier.fillMaxSize(), color,
                        )
                    }
                }
                Spacer(Modifier.width(if (compact) 13.dp else 14.dp))
                Column(Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(label, color = Ink.muted,
                        fontSize = if (compact) 11.sp else 13.sp,
                        lineHeight = if (compact) 13.sp else 15.sp, maxLines = 2)
                    Text(value, color = Ink.white,
                        fontSize = if (compact && value.length > 7) 15.sp
                            else if (compact) 20.sp
                            else if (value.length > 7) 19.sp else 25.sp,
                        fontWeight = FontWeight.ExtraBold, maxLines = 1,
                    )
                    Text(
                        when (id) {
                            "played" -> "Pokrenute igre"
                            "best" -> "Najbrži rezultat"
                            "total" -> "Provedeno u igri"
                            "streak" -> "Trenutni niz"
                            else -> ""
                        },
                        color = Ink.muted, fontSize = if (compact) 9.sp else 10.sp,
                        maxLines = 1,
                    )
                }
                BrandIcon("next", Modifier.size(if (compact) 12.dp else 15.dp), color)
            }
        }
    }
}
