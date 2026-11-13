package com.brendigo.sudoku.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.sudoku.data.Screen
import com.brendigo.sudoku.data.SudokuController

private data class Achievement(
    val icon: String, val title: String, val text: String, val goal: Int,
    val current: Int, val reward: Int, val tint: Color,
)

@Composable
fun AchievementsPage(controller: SudokuController) {
    val s = controller.stats
    // Preserve the actual user's 24-award view and open details through
    // rotation or process recreation. Save only the stable title, not a
    // stale Achievement snapshot containing old progress data.
    var showAll by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(false)
    }
    var selectedAchievementTitle by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf<String?>(null)
    }
    // 23 measured awards + the Sudoku legend award = 24 distinct achievements.
    val basic = listOf(
        Achievement("🏆", "Prva pobjeda", "Završi svoju prvu igru u bilo kojoj težini.", 1, s.finished, 50, Ink.gold),
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
    val selectedAchievement = data.firstOrNull { it.title == selectedAchievementTitle }
    PageContent(topPadding = 18.dp, horizontalPadding = 19.dp) {
        SectionTitle("Postignuća", centered = true, onBack = { controller.go(Screen.HOME) })
        AchievementSummarySurface(Modifier.fillMaxWidth().heightIn(min = 75.dp)
            .testTag("achievement-summary")) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val compactSummary = maxWidth < 375.dp || LocalDensity.current.fontScale >= 1.4f
                Row(
                    Modifier.fillMaxWidth().padding(
                        horizontal = if (compactSummary) 9.dp else 17.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        if (compactSummary) 6.dp else 13.dp),
                ) {
                AchievementBadgeArt(
                    "Prva pobjeda",
                    Modifier.size(if (compactSummary) 36.dp else 43.dp)
                        .testTag("achievement-summary-3d-badge")
                        .drawBehind {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            drawCircle(
                                Brush.radialGradient(
                                    listOf(Ink.gold.copy(alpha = .37f), Color.Transparent),
                                    center = center, radius = size.width * .95f,
                                ),
                                center = center, radius = size.width * .95f,
                            )
                        },
                    Ink.gold,
                )
                Column(Modifier.weight(1f)) {
                    Subtitle("Ukupno bodova")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$points", fontSize = if (compactSummary) 22.sp else 29.sp,
                            color = Ink.gold, fontWeight = FontWeight.ExtraBold)
                        Text(" / 1000", fontSize = if (compactSummary) 10.sp else 13.sp, color = Ink.muted)
                    }
                }
                Box(Modifier.width(1.dp).height(49.dp)
                    .background(Ink.blue.copy(alpha = .80f)))
                BrandIcon(
                    "star-solid",
                    Modifier.size(if (compactSummary) 29.dp else 39.dp)
                        .drawBehind {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            drawCircle(
                                Brush.radialGradient(
                                    listOf(Ink.cyan.copy(alpha = .27f), Color.Transparent),
                                    center = center, radius = size.width * .88f,
                                ),
                                center = center, radius = size.width * .88f,
                            )
                        },
                    Ink.cyan,
                )
                Column(Modifier.weight(.75f)) {
                    Subtitle("Otključano")
                    Text("${data.count { it.current >= it.goal }} / ${data.size}",
                        fontSize = if (compactSummary) 20.sp else 29.sp,
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
                        rowIndex == 2 -> 137.dp
                        // The owner's last featured row ends well above the
                        // scenery; the old 137dp height extended it ~11px.
                        // Small-screen accessible cards retain flexible mins.
                        rowIndex == 3 -> 125.dp
                        else -> 151.dp
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { achievement ->
                    val unlocked = achievement.current >= achievement.goal
                    val compactFeatured = rowIndex in 2..3
                    // Only the five colourful reference illustrations
                    // keep their 3D hue while still locked. "Brzinac",
                    // "Rješavač" and "Sudoku legenda" stay muted until earned.
                    val vividFeatured = achievement.title in setOf(
                        "Prva pobjeda", "Sedam dana zaredom", "Majstor logike",
                        "Bez greške", "Vrući niz",
                    )
                    AchievementSurface(
                        title = achievement.title,
                        tint = achievement.tint,
                        completed = unlocked,
                        modifier = Modifier.weight(1f).then(
                            if (compactCards) Modifier.heightIn(min = rowHeight)
                            else Modifier.height(rowHeight)
                        )
                        .testTag("achievement-" + achievement.title.lowercase().replace(" ", "-"))
                        .semantics {
                            contentDescription = achievement.title +
                                (if (unlocked) ", otključano" else ", zaključano")
                            role = Role.Button
                        }
                        .clickable { selectedAchievementTitle = achievement.title },
                    ) {
                        Column(
                            Modifier.fillMaxWidth()
                                .then(if (compactCards) Modifier.heightIn(min = rowHeight)
                                      else Modifier.fillMaxHeight())
                                .padding(
                                    start = if (compactCards) 8.dp else 13.dp,
                                    end = if (compactCards) 8.dp else 13.dp,
                                    top = if (compactCards) 9.dp else 12.dp,
                                    // The reference has two real description
                                    // lines; the old 12dp bottom gutter left
                                    // room unused while clipping line two.
                                    bottom = if (compactCards) 9.dp else 4.dp,
                                ),
                            verticalArrangement = Arrangement.spacedBy(
                                if (compactCards) 4.dp else 2.dp),
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Box(
                                    Modifier.size(if (compactFeatured) 38.dp else 48.dp)
                                        .drawBehind {
                                            val mid = Offset(size.width / 2f, size.height / 2f)
                                            drawCircle(
                                                Brush.radialGradient(
                                                    listOf(
                                                        achievement.tint.copy(
                                                            alpha = if (unlocked) .57f
                                                            else if (vividFeatured) .36f else .14f
                                                        ),
                                                        Color.Transparent,
                                                    ),
                                                    center = mid,
                                                    radius = size.width * if (rowIndex < 2) .79f else .68f,
                                                ),
                                                radius = size.width * if (rowIndex < 2) .79f else .68f,
                                                center = mid,
                                            )
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    // Keep each award recognizable when locked.
                                    AchievementBadgeArt(
                                        title = achievement.title,
                                        modifier = Modifier
                                            .size(if (compactFeatured) 37.dp else 47.dp)
                                            .graphicsLayer {
                                                // Only art scales; the real
                                                // badge text and hit area stay
                                                // independent native UI.
                                                val scale = if (vividFeatured) 1.19f else 1f
                                                scaleX = scale
                                                scaleY = scale
                                            },
                                        tint = if (unlocked) achievement.tint
                                            else achievement.tint.copy(
                                                alpha = if (vividFeatured) .97f else .61f
                                            ),
                                    )
                                    // The five reference progression cards
                                    // show coloured awards BEFORE completion.
                                    // Their genuine progress bar and explicit
                                    // accessibility status remain the source
                                    // of truth. Only milestone-style awards
                                    // use the grey lock overlay in artwork.
                                    if (!unlocked && !vividFeatured) {
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
                                    Modifier.then(if (unlocked) Modifier.shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(30.dp),
                                        ambientColor = achievement.tint.copy(alpha = .38f),
                                        spotColor = achievement.tint.copy(alpha = .48f),
                                    ) else Modifier)
                                        .background(Color(0xBB041B3E),
                                            RoundedCornerShape(30.dp))
                                        .border(1.dp, if (unlocked) achievement.tint
                                            else Ink.muted.copy(alpha = .55f),
                                            RoundedCornerShape(30.dp))
                                        .padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    BrandIcon("star-solid", Modifier.size(14.dp),
                                        if (unlocked) achievement.tint else Ink.muted)
                                    Spacer(Modifier.width(3.dp))
                                    Text("${achievement.reward}",
                                        color = if (unlocked) achievement.tint
                                            else achievement.tint.copy(alpha = .75f),
                                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(achievement.title, color = Ink.white,
                                    fontSize = if (compactCards) 14.sp else 16.sp,
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
                                fontSize = if (compactFeatured || compactCards) 11.sp else 12.sp,
                                lineHeight = if (compactFeatured || compactCards) 13.sp else 14.sp,
                                // The source artwork leaves deliberate open
                                // space to the right of the description.
                                // Let the five coloured badges wrap into two
                                // short lines instead of one clipped long
                                // line across the whole scenery panel.
                                modifier = (if (compactCards) Modifier
                                    else Modifier.weight(1f))
                                    // Reserve two full line-heights on the
                                    // fixed-height reference cards. Smaller
                                    // viewports keep flexible growing cards.
                                    .then(if (compactCards) Modifier else Modifier.heightIn(min = 28.dp))
                                    .fillMaxWidth(
                                        if (vividFeatured && !stackCards) .76f else 1f
                                    ).testTag(
                                        "achievement-description-" +
                                            achievement.title.lowercase().replace(" ", "-")),
                                maxLines = if (compactCards) 4 else 2,
                            )
                            val progress = (achievement.current.toFloat() /
                                achievement.goal.toFloat()).coerceIn(0f, 1f)
                            Box(
                                Modifier.fillMaxWidth().height(8.dp)
                                    .testTag(
                                        "achievement-progress-" +
                                            achievement.title.lowercase().replace(" ", "-")
                                    )
                                    .semantics {
                                        contentDescription =
                                            "${achievement.current.coerceAtMost(achievement.goal)}" +
                                            " od ${achievement.goal}"
                                    }
                                    .background(Color(0xDD041B3B), RoundedCornerShape(12.dp))
                                .border(.6.dp, achievement.tint.copy(alpha = .45f),
                                    RoundedCornerShape(12.dp))) {
                                if (progress > 0f) Box(
                                    Modifier.fillMaxWidth(progress).fillMaxHeight()
                                        .then(if (unlocked) Modifier.shadow(
                                            elevation = 3.dp,
                                            shape = RoundedCornerShape(12.dp),
                                            ambientColor = achievement.tint.copy(alpha = .4f),
                                            spotColor = achievement.tint.copy(alpha = .44f),
                                        ) else Modifier)
                                        .background(
                                            Brush.horizontalGradient(listOf(
                                                achievement.tint.copy(alpha = .64f),
                                                achievement.tint,
                                                achievement.tint.copy(alpha = .92f)
                                            )),
                                            RoundedCornerShape(12.dp),
                                        )
                                )
                            }
                            Text("${achievement.current.coerceAtMost(achievement.goal)} / ${achievement.goal}",
                                color = if (unlocked) achievement.tint else Ink.muted, fontSize = 11.sp)
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
            onDismissRequest = { selectedAchievementTitle = null },
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
                TextButton(onClick = { selectedAchievementTitle = null }) {
                    Text("Zatvori", color = Ink.cyan)
                }
            },
        )
    }
}
