package com.brendigo.sudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PageContent(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 42.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

@Composable
fun SectionTitle(title: String, description: String = "", onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(46.dp).testTag("navigate-back")
                .background(Color(0x99082150), RoundedCornerShape(14.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) { Text("‹", fontSize = 37.sp, color = Ink.muted, textAlign = TextAlign.Center) }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            AccentText(title, if (title.length > 16) 29 else 34)
            if (description.isNotBlank()) Subtitle(description)
        }
    }
}

@Composable
fun SolidAction(title: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier.testTag("primary-action").heightIn(min = 58.dp)
            .background(
                Brush.horizontalGradient(if (enabled) listOf(Ink.cyan, Ink.blue) else listOf(Color.DarkGray, Color.Gray)),
                RoundedCornerShape(30.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) { Text(title, color = Ink.white, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) }
}

@Composable
fun HomePage(controller: SudokuController) {
    PageContent {
        Spacer(Modifier.height(12.dp))
        BrandHeader(Modifier.fillMaxWidth())
        Text(
            "V I Š E  O D  I G R E  ·  V J E Ž B A  Z A  U M",
            color = Ink.muted, fontSize = 10.sp, modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        val game = controller.game
        if (game != null && !game.completed && !game.failed) {
            NeonPanel(Modifier.fillMaxWidth().clickable { controller.go(Screen.GAME) }
                .heightIn(min = 170.dp), highlight = Ink.blue) {
                Column(Modifier.padding(19.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("N A S T A V I  I G R U", color = Ink.cyan, fontSize = 12.sp, letterSpacing = 2.sp)
                    Text("Prekinuta igra", color = Ink.white, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                    Subtitle(difficultyLabels[game.level] + " · " + game.size + "×" + game.size)
                    Subtitle("◷ " + formatTime(game.elapsed) + "  ·  Još " +
                        game.board.indices.count { game.board[it] != game.solution[it] } + " polja")
                    Spacer(Modifier.height(6.dp))
                    SolidAction("▶   Nastavi", { controller.go(Screen.GAME) }, Modifier.fillMaxWidth(.6f))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeTile("✚", "Nova igra", "Odaberi težinu", Ink.teal, Modifier.weight(1f)) {
                controller.go(Screen.NEW_GAME)
            }
            HomeTile("▣", "Dnevni izazov", "Nova zagonetka svaki dan", Ink.gold, Modifier.weight(1f)) {
                controller.go(Screen.DAILY)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HomeTile("▥", "Statistika", "Prati napredak", Ink.blue, Modifier.weight(1f), compact = true) {
                controller.go(Screen.STATS)
            }
            HomeTile("★", "Postignuća", "Osvoji nagrade", Ink.purple, Modifier.weight(1f), compact = true) {
                controller.go(Screen.ACHIEVEMENTS)
            }
            HomeTile("⚙", "Postavke", "Prilagodi igru", Ink.muted, Modifier.weight(1f), compact = true) {
                controller.go(Screen.SETTINGS)
            }
        }
        NeonPanel(Modifier.fillMaxWidth(), highlight = Ink.blue) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🔥", fontSize = 28.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Subtitle("Trenutni niz")
                    Text("${controller.currentStreak()} dana", fontSize = 25.sp,
                        color = Ink.gold, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.weight(1f))
                Text("PON  UTO  SRI  ČET  PET", fontSize = 10.sp, color = Ink.cyan)
            }
        }
        Text("B I S T R I J I  U M  ·  B O L J I  D A N", color = Ink.muted,
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
            textAlign = TextAlign.Center, fontSize = 10.sp)
    }
}

@Composable
private fun HomeTile(
    icon: String, title: String, subtitle: String, accent: Color,
    modifier: Modifier = Modifier, compact: Boolean = false, onClick: () -> Unit,
) {
    NeonPanel(modifier.testTag("home-" + title.lowercase().replace(" ", "-")).heightIn(min = if (compact) 134.dp else 179.dp).clickable(onClick = onClick), highlight = accent) {
        Column(Modifier.padding(if (compact) 12.dp else 17.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 7.dp)) {
            Text(icon, color = accent, fontWeight = FontWeight.Bold, fontSize = if (compact) 29.sp else 38.sp)
            Text(title, color = Ink.white, fontWeight = FontWeight.Bold,
                fontSize = if (compact) 16.sp else 21.sp, maxLines = 2)
            Text(subtitle, color = Ink.muted, fontSize = if (compact) 11.sp else 13.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
fun NewGamePage(controller: SudokuController) {
    val scope = rememberCoroutineScope()
    var size by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(9) }
    var level by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(2) }
    PageContent {
        SectionTitle("Nova igra", "Odaberi težinu", { controller.go(Screen.HOME) })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(9, 6, 4).forEach { option ->
                NeonPanel(Modifier.weight(1f).height(53.dp)
                    .clickable { size = option }, highlight = if (size == option) Ink.cyan else Ink.line) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("$option×$option", color = if (size == option) Ink.white else Ink.muted,
                            fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        listOf("✿", "★", "★★", "★★★", "♛", "♛").forEachIndexed { i, symbol ->
            NeonPanel(
                Modifier.fillMaxWidth().heightIn(min = 86.dp)
                    .clickable { level = i },
                highlight = if (level == i) Ink.cyan else difficultyColors[i],
            ) {
                Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(symbol, fontSize = 25.sp, color = difficultyColors[i])
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(difficultyLabels[i], fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Ink.white)
                        Subtitle(listOf(
                            "Savršeno za početnike", "Lagani izazov za vježbu",
                            "Uravnotežen izazov", "Za iskusne igrače",
                            "Ozbiljan izazov", "Samo za najbolje",
                        )[i])
                    }
                    Text(if (level == i) "✓" else "›", fontSize = 29.sp, color = difficultyColors[i])
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
fun DailyPage(controller: SudokuController) {
    val scope = rememberCoroutineScope()
    val formattedDate = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("d. MMMM yyyy.", Locale("hr", "HR")))
    }
    PageContent {
        SectionTitle("Dnevni izazov", "Današnji izazov", { controller.go(Screen.HOME) })
        NeonPanel(Modifier.fillMaxWidth(), Ink.gold) {
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Subtitle("DANAS")
                    Text(formattedDate, color = Ink.white, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                }
                Text("🔥", fontSize = 30.sp)
                Spacer(Modifier.width(6.dp))
                Column {
                    Text("${controller.currentStreak()}", color = Ink.gold, fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold)
                    Subtitle("dana u nizu")
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        NeonPanel(Modifier.fillMaxWidth(), Ink.gold) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("DANAŠNJI IZAZOV", color = Ink.gold, fontSize = 12.sp, letterSpacing = 2.sp)
                AccentText("Misteriozna planina", 29)
                Subtitle("Riješi današnju sudoku zagonetku i osvoji posebnu nagradu!")
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Column(Modifier.weight(1f)) { Subtitle("TEŽINA"); Text("Srednja", color = Ink.white, fontWeight = FontWeight.Bold) }
                    Column(Modifier.weight(1f)) { Subtitle("NAGRADA"); Text("50 bodova", color = Ink.gold, fontWeight = FontWeight.Bold) }
                }
            }
        }
        NeonPanel(Modifier.fillMaxWidth(), Ink.cyan) {
            Column(Modifier.padding(19.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("TVOJ NIZ", color = Ink.muted, letterSpacing = 3.sp, fontSize = 11.sp)
                Text("Riješi izazove svaki dan za više nagrada!", color = Ink.white)
                val latest = runCatching { LocalDate.parse(controller.stats.lastDaily) }.getOrNull()
                val today = LocalDate.now()
                val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
                val completedDays = (0 until controller.currentStreak().coerceAtMost(7)).mapNotNull {
                    latest?.minusDays(it.toLong())
                }.toSet()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED").forEachIndexed { i, label ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (monday.plusDays(i.toLong()) in completedDays) "✓" else "○",
                                color = Ink.cyan, fontSize = 23.sp)
                            Text(label, color = Ink.muted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        if (controller.dailyAlreadyCompleted()) {
            Text("✓ Današnji izazov već je riješen.", color = Ink.teal,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
        SolidAction(
            when {
                controller.dailyAlreadyCompleted() -> "✓   Današnji izazov riješen"
                controller.isGenerating -> "Učitavanje…"
                else -> "▶   Pokreni izazov"
            },
            { scope.launch { controller.startNew(9, 2, daily = true) } },
            Modifier.fillMaxWidth(), !controller.isGenerating && !controller.dailyAlreadyCompleted()
        )
    }
}

@Composable
fun StatsPage(controller: SudokuController) {
    val stats = controller.stats
    PageContent {
        SectionTitle("Statistika", "Tvoj Sudoku put u brojkama", { controller.go(Screen.HOME) })
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            StatTile("◈", "Odigrane igre", stats.finished.toString(), Ink.teal, Modifier.weight(1f))
            StatTile("◷", "Najbolje vrijeme", formatTime(stats.best), Ink.purple, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            StatTile("◷", "Ukupno vrijeme", formatTime(stats.totalSeconds.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()),
                Ink.blue, Modifier.weight(1f))
            StatTile("🔥", "Dnevni niz", controller.currentStreak().toString(), Ink.gold, Modifier.weight(1f))
        }
        NeonPanel(Modifier.fillMaxWidth(), Ink.blue) {
            Column(Modifier.padding(18.dp)) {
                Text("Riješene igre po težini", color = Ink.white, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                val maximum = (stats.winsByLevel.maxOrNull() ?: 0).coerceAtLeast(1)
                Row(Modifier.fillMaxWidth().height(170.dp), verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                    stats.winsByLevel.forEachIndexed { index, amount ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$amount", color = Ink.white, fontSize = 12.sp)
                            Spacer(Modifier.height(5.dp))
                            Box(
                                Modifier.width(28.dp).height((12 + 105 * amount / maximum).dp)
                                    .background(Brush.verticalGradient(
                                        listOf(difficultyColors[index], difficultyColors[index].copy(alpha = .45f))),
                                        RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp)),
                            )
                            Spacer(Modifier.height(6.dp))
                            Text((index + 1).toString(), color = Ink.muted, fontSize = 11.sp)
                        }
                    }
                }
                Subtitle("Razine težine 1–6", Modifier.fillMaxWidth(), centered = true)
            }
        }
        NeonPanel(Modifier.fillMaxWidth(), Ink.cyan) {
            Column(Modifier.padding(18.dp)) {
                Text("Postotak dovršenosti", color = Ink.white,
                    fontWeight = FontWeight.Bold, fontSize = 18.sp)
                val percentage = if (stats.played > 0) stats.finished * 100 / stats.played else 0
                Box(Modifier.fillMaxWidth().height(125.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(110.dp)) {
                        drawArc(Ink.blue.copy(alpha = .26f), 0f, 360f, false,
                            style = Stroke(12.dp.toPx(), cap = StrokeCap.Round))
                        drawArc(Ink.cyan, -90f, percentage * 3.6f, false,
                            style = Stroke(12.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Text("$percentage%", fontSize = 29.sp, color = Ink.white, fontWeight = FontWeight.ExtraBold)
                }
                Subtitle("${stats.finished} dovršeno · ${stats.played} pokrenuto",
                    Modifier.fillMaxWidth(), centered = true)
            }
        }
        NeonPanel(Modifier.fillMaxWidth(), Ink.blue) {
            Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Nedavni napredak", color = Ink.white, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (stats.history.isEmpty()) Subtitle("Još nema dovršenih igara.")
                stats.history.take(6).forEach {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("▦", color = difficultyColors[it.level], fontSize = 22.sp)
                        Spacer(Modifier.width(9.dp))
                        Text(difficultyLabels[it.level], color = Ink.white, modifier = Modifier.weight(1f))
                        Text(formatTime(it.seconds), color = Ink.muted, fontSize = 13.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(it.date, color = Ink.muted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(icon: String, label: String, value: String, color: Color, modifier: Modifier) {
    NeonPanel(modifier.heightIn(min = 124.dp), color) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(icon, fontSize = 24.sp, color = color)
            Subtitle(label)
            Text(value, color = Ink.white, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
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
    val points = data.filter { it.current >= it.goal }.sumOf { it.reward }
    PageContent {
        SectionTitle("Postignuća", "Skupljaj nagrade rješavanjem", { controller.go(Screen.HOME) })
        NeonPanel(Modifier.fillMaxWidth(), Ink.gold) {
            Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column { Subtitle("Ukupno bodova"); Text("$points", fontSize = 30.sp,
                    color = Ink.gold, fontWeight = FontWeight.ExtraBold) }
                Column { Subtitle("Otključano"); Text("${data.count { it.current >= it.goal }} / ${data.size}",
                    fontSize = 26.sp, color = Ink.cyan, fontWeight = FontWeight.Bold) }
            }
        }
        data.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { achievement ->
                    NeonPanel(Modifier.weight(1f).heightIn(min = 205.dp), achievement.tint) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(achievement.icon, fontSize = 32.sp)
                                Text("★ ${achievement.reward}", color = achievement.tint, fontSize = 12.sp)
                            }
                            Text(achievement.title, color = Ink.white, fontSize = 17.sp,
                                fontWeight = FontWeight.Bold, lineHeight = 21.sp)
                            Text(achievement.text, color = Ink.muted, fontSize = 12.sp,
                                lineHeight = 16.sp, modifier = Modifier.weight(1f))
                            val progress = (achievement.current.toFloat() /
                                achievement.goal.toFloat()).coerceIn(0f, 1f)
                            Box(Modifier.fillMaxWidth().height(7.dp).background(Ink.blue.copy(alpha = .25f),
                                RoundedCornerShape(12.dp))) {
                                Box(Modifier.fillMaxWidth(progress).fillMaxHeight()
                                    .background(achievement.tint, RoundedCornerShape(12.dp)))
                            }
                            Text("${achievement.current.coerceAtMost(achievement.goal)} / ${achievement.goal}",
                                color = Ink.muted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsPage(controller: SudokuController) {
    val opts = controller.options
    PageContent {
        SectionTitle("Postavke", "Prilagodi igru svojim željama", { controller.back() })
        NeonPanel(Modifier.fillMaxWidth(), Ink.blue) {
            Column(Modifier.padding(15.dp)) {
                SettingHeader("◉", "Izgled", "Prilagodi vizualni doživljaj igre")
                SettingToggle("☾", "Tamna tema", "Koristi tamni izgled aplikacije", opts.dark) {
                    controller.updateOptions(opts.copy(dark = it))
                }
                SettingToggle("✧", "Animacije", "Prikaži animacije i vizualne efekte", opts.animations) {
                    controller.updateOptions(opts.copy(animations = it))
                }
            }
        }
        NeonPanel(Modifier.fillMaxWidth(), Ink.blue) {
            Column(Modifier.padding(15.dp)) {
                SettingHeader("▣", "Igra", "Postavke igrivosti i pomoći")
                SettingToggle("▤", "Automatske bilješke", "Ukloni riješene kandidate iz bilješki", opts.autoNotes) {
                    controller.updateOptions(opts.copy(autoNotes = it))
                }
                SettingToggle("⚠", "Provjera greške", "Označi netočne unose i prati pogreške", opts.checkMistakes) {
                    controller.updateOptions(opts.copy(checkMistakes = it))
                }
                SettingToggle("▦", "Isticanje brojeva", "Naglašavanje jednakih brojeva na ploči", opts.highlightEquals) {
                    controller.updateOptions(opts.copy(highlightEquals = it))
                }
            }
        }
        NeonPanel(Modifier.fillMaxWidth(), Ink.purple) {
            Column(Modifier.padding(15.dp)) {
                SettingHeader("♫", "Zvuk i vibracija", "Povratne informacije tijekom igre")
                SettingToggle("♪", "Zvuk", "Zvučni efekti pri pritisku gumba", opts.sound) {
                    controller.updateOptions(opts.copy(sound = it))
                }
                SettingToggle("◈", "Vibracija", "Vibracija pri unosu broja", opts.vibration) {
                    controller.updateOptions(opts.copy(vibration = it))
                }
            }
        }
        NeonPanel(Modifier.fillMaxWidth(), Ink.teal) {
            Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("◎", fontSize = 31.sp, color = Ink.teal)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Jezik", color = Ink.white, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Subtitle("Jezik aplikacije")
                }
                Text("Hrvatski", color = Ink.muted)
            }
        }
        NeonPanel(Modifier.fillMaxWidth(), Ink.blue) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("O aplikaciji", fontSize = 20.sp, color = Ink.white, fontWeight = FontWeight.Bold)
                Subtitle("Sudoku by Brendigo")
                Subtitle("Verzija 1.0.0")
                Subtitle("Igra radi potpuno offline; napredak ostaje na uređaju.")
                Subtitle("Izvorni kod i uvjeti: MIT licenca u GitHub repozitoriju.")
            }
        }
    }
}

@Composable
private fun SettingHeader(icon: String, title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 29.sp, color = Ink.cyan)
        Spacer(Modifier.width(14.dp))
        Column { Text(title, fontSize = 22.sp, color = Ink.white, fontWeight = FontWeight.Bold); Subtitle(subtitle) }
    }
}

@Composable
private fun SettingToggle(icon: String, title: String, caption: String, checked: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 73.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, color = Ink.cyan, fontSize = 24.sp)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = Ink.white, fontWeight = FontWeight.SemiBold)
            Text(caption, color = Ink.muted, fontSize = 12.sp, lineHeight = 15.sp)
        }
        Switch(checked, onCheckedChange = change)
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
