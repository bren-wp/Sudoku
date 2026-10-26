package com.brendigo.sudoku.ui

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.sudoku.data.Screen
import com.brendigo.sudoku.data.Session
import com.brendigo.sudoku.data.SudokuController

@Composable
fun GamePage(controller: SudokuController) {
    val game = controller.game ?: run {
        PageContent {
            SectionTitle("Sudoku", "Nema aktivne igre", { controller.go(Screen.HOME) })
            SolidAction("Nova igra", { controller.go(Screen.NEW_GAME) }, Modifier.fillMaxWidth())
        }
        return
    }
    val view = LocalView.current
    fun feedback() {
        if (controller.options.sound) view.playSoundEffect(SoundEffectConstants.CLICK)
        if (controller.options.vibration) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < 730.dp
        val narrowWidth = maxWidth < 340.dp
        val largeText = LocalDensity.current.fontScale >= 1.4f
        val horizontal = if (compact) 12.dp else 20.dp
        // Width is the hard limit: a 216dp minimum previously overflowed
        // narrow multi-window and foldable screens.
        val availableWidth = (maxWidth - horizontal * 2).coerceAtLeast(1.dp)
        val availableHeight = (maxHeight - if (compact) 255.dp else 300.dp)
            .coerceAtLeast(216.dp)
        val boardSize = minOf(availableWidth, availableHeight)
        val toolHeight = if (compact) 51.dp else 65.dp
        val numberHeight = if (compact) 48.dp else 54.dp
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = horizontal, vertical = if (compact) 6.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp),
        ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).testTag("game-back")
                .semantics { contentDescription = "Natrag na početni ekran"; role = Role.Button }.background(Color(0x770B2256), RoundedCornerShape(14.dp))
                .clickable { controller.go(Screen.HOME) }, contentAlignment = Alignment.Center) {
                BrandIcon("back", Modifier.size(24.dp), Ink.muted)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                AccentText("Sudoku", when {
                    maxWidth < 275.dp -> 22
                    narrowWidth || largeText -> 28
                    compact -> 36
                    else -> 40
                })
                Subtitle(if (game.daily) "Dnevni izazov" else difficultyLabels[game.level],
                    centered = true)
            }
            Box(Modifier.size(48.dp).testTag("game-pause")
                .semantics { contentDescription = "Pauziraj ili nastavi igru"; role = Role.Button }
                .background(Color(0x770B2256), RoundedCornerShape(14.dp))
                .clickable { controller.togglePause() }, contentAlignment = Alignment.Center) {
                BrandIcon(if (game.paused) "play" else "pause", Modifier.size(23.dp), Ink.muted)
            }
            Spacer(Modifier.width(if (narrowWidth) 4.dp else 8.dp))
            Box(Modifier.size(48.dp).testTag("game-settings")
                .semantics { contentDescription = "Postavke igre"; role = Role.Button }.background(Color(0x770B2256), RoundedCornerShape(14.dp))
                .clickable { controller.go(Screen.SETTINGS) }, contentAlignment = Alignment.Center) {
                BrandIcon("settings", Modifier.size(24.dp), Ink.muted)
            }
        }
        Spacer(Modifier.height(if (compact) 1.dp else 4.dp))
        BoxWithConstraints(Modifier.fillMaxWidth().testTag("game-hud")) {
            val stackedHud = maxWidth < 305.dp || largeText
            if (stackedHud) {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BrandIcon("timer", Modifier.size(19.dp), Ink.muted)
                            Spacer(Modifier.width(5.dp))
                            Text(formatTime(game.elapsed), color = Ink.muted,
                                fontSize = 14.sp, modifier = Modifier.testTag("game-time"))
                        }
                        Text(
                            if (controller.options.checkMistakes)
                                "Greške: ${game.mistakes}/3" else "Bez ograničenja",
                            fontSize = 12.sp, color = Ink.muted,
                            modifier = Modifier.testTag("game-mistakes"))
                    }
                    val correct = game.correctCount()
                    val progress = correct.toFloat() / game.board.size
                    Column(Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Još ${game.board.size - correct} polja · ${(progress * 100).toInt()}%",
                            fontSize = 12.sp, color = Ink.cyan)
                        Spacer(Modifier.height(4.dp))
                        Box(Modifier.fillMaxWidth().height(6.dp).testTag("game-progress")
                            .background(Ink.blue.copy(alpha = .27f), RoundedCornerShape(10.dp))) {
                            Box(Modifier.fillMaxWidth(progress).fillMaxHeight()
                                .background(Ink.cyan, RoundedCornerShape(10.dp)))
                        }
                    }
                }
            } else {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandIcon("timer", Modifier.size(21.dp), Ink.muted)
                Spacer(Modifier.width(5.dp))
                Text(formatTime(game.elapsed), fontSize = if (compact) 14.sp else 16.sp,
                    color = Ink.muted, modifier = Modifier.testTag("game-time"))
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                val correct = game.correctCount()
                val percentage = correct.toFloat() / game.board.size
                Text("Još ${game.board.size - correct} polja", color = Ink.muted, fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth().height(6.dp).testTag("game-progress")
                    .background(Ink.blue.copy(alpha = .27f), RoundedCornerShape(10.dp))) {
                    Box(Modifier.fillMaxWidth(percentage).fillMaxHeight()
                        .background(Ink.cyan, RoundedCornerShape(10.dp)))
                }
                Text("${(percentage * 100).toInt()}%", fontSize = 11.sp, color = Ink.cyan)
            }
            Column(horizontalAlignment = Alignment.End) {
                if (controller.options.checkMistakes) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.semantics {
                            contentDescription = "${(3 - game.mistakes).coerceIn(0, 3)} života preostalo"
                        },
                    ) {
                        repeat(3) { index ->
                            BrandIcon(
                                if (index < (3 - game.mistakes).coerceIn(0, 3)) "heart" else "heart-outline",
                                Modifier.size(19.dp),
                                if (index < 3 - game.mistakes) Ink.red else Ink.muted.copy(alpha = .6f),
                            )
                        }
                    }
                } else {
                    Text("∞", color = Ink.cyan, fontSize = 20.sp)
                }
                Text(if (controller.options.checkMistakes) "Greške: ${game.mistakes}/3"
                    else "Bez ograničenja", color = Ink.muted, fontSize = 10.sp,
                    modifier = Modifier.testTag("game-mistakes"))
            }
        }
            }
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SudokuBoard(game, controller.options.highlightEquals, controller.options.checkMistakes,
                controller.options.animations, Modifier.size(boardSize)) {
                controller.select(it)
                feedback()
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 8.dp)) {
            GameTool("Bilješke", if (game.notesMode) Ink.teal else Ink.line,
                Modifier.weight(1f), height = toolHeight, active = game.notesMode,
                enabled = !game.paused && !game.completed && !game.failed,
                tag = "game-notes") { controller.toggleNotes(); feedback() }
            GameTool("Savjet", Ink.cyan, Modifier.weight(1f), height = toolHeight,
                badge = "${(SudokuController.MAX_HINTS - game.hints).coerceAtLeast(0)}",
                enabled = controller.canHint(), tag = "game-hint") { controller.hint(); feedback() }
            GameTool("Poništi", Ink.blue, Modifier.weight(1f), height = toolHeight,
                enabled = controller.canUndo(), tag = "game-undo") { controller.undo(); feedback() }
            GameTool("Izbriši", Ink.blue, Modifier.weight(1f), height = toolHeight,
                enabled = controller.canErase(), tag = "game-erase") { controller.erase(); feedback() }
        }
        val numbers = (1..game.size).toList()
        val split = if (game.size == 9) 5 else if (game.size == 6) 3 else 4
        NumberRow(numbers.take(split), numberHeight, controller.canEnter(), split) {
            controller.enter(it); feedback()
        }
        if (numbers.size > split) {
            NumberRow(numbers.drop(split), numberHeight, controller.canEnter(), split) {
                controller.enter(it); feedback()
            }
        }
        // On shorter screens, the board and keypad remain usable through scroll.
        // The landscape and brand footer remain visible on the main pages.
        }
    }


    if (game.paused) {
        AlertDialog(
            onDismissRequest = { controller.togglePause() },
            containerColor = Color(0xFF081A41),
            title = { Text("Igra je pauzirana", color = Ink.white) },
            text = { Text("Vrijeme je zaustavljeno. Nastavi kada budeš spreman.", color = Ink.muted) },
            confirmButton = {
                TextButton(onClick = { controller.togglePause() }) {
                    Text("▶ Nastavi", color = Ink.cyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { controller.go(Screen.HOME) }) {
                    Text("Glavni izbornik", color = Ink.muted)
                }
            },
        )
    }
    if (game.completed || game.failed) {
        AlertDialog(
            onDismissRequest = { controller.go(Screen.HOME) },
            containerColor = Color(0xFF081A41),
            title = {
                Text(if (game.completed) "Čestitamo!" else "Pokušaj ponovno",
                    color = if (game.completed) Ink.gold else Ink.white)
            },
            text = {
                Text(
                    if (game.completed) "Riješio si Sudoku za ${formatTime(game.elapsed)}!" +
                        if (game.daily) " Dnevni izazov je završen." else ""
                    else "Iskorištene su tri pogreške. Pokreni novu zagonetku.",
                    color = Ink.muted,
                )
            },
            confirmButton = {
                TextButton(onClick = { controller.go(Screen.NEW_GAME) }) {
                    Text("Nova igra", color = Ink.cyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { controller.go(Screen.HOME) }) {
                    Text("Glavni izbornik", color = Ink.muted)
                }
            }
        )
    }
}

@Composable
private fun SudokuBoard(game: Session, highlight: Boolean, showErrors: Boolean,
                        animations: Boolean, modifier: Modifier = Modifier, onSelect: (Int) -> Unit) {
    val n = game.size
    val selected = game.selected
    val selectionNumber = if (selected >= 0) game.board[selected] else 0
    Box(
        modifier.aspectRatio(1f).testTag("sudoku-board")
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xF8041336))
            .border(2.dp, Ink.blue, RoundedCornerShape(14.dp))
            .padding(6.dp),
    ) {
        if (game.paused) {
            // Conceal digits and notes while the stopwatch is stopped.
            Column(
                Modifier.fillMaxSize().testTag("sudoku-board-paused")
                    .semantics { contentDescription = "Sudoku ploča skrivena tijekom pauze" },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BrandIcon("lock", Modifier.size(38.dp), Ink.cyan)
                Spacer(Modifier.height(12.dp))
                Text("Ploča je skrivena", color = Ink.white,
                    fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Nastavi igru za prikaz brojeva", color = Ink.muted,
                    fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        } else {
        Column(Modifier.fillMaxSize()) {
            repeat(n) { r ->
                Row(Modifier.weight(1f)) {
                    repeat(n) { c ->
                        val index = r * n + c
                        val digit = game.board[index]
                        val fixed = game.givens[index] != 0
                        val isSelected = index == selected
                        val isPeer = selected >= 0 && (
                            r == selected / n || c == selected % n ||
                            (r / game.boxRows == selected / n / game.boxRows &&
                             c / game.boxColumns == selected % n / game.boxColumns)
                        )
                        val equal = highlight && selectionNumber > 0 && digit == selectionNumber
                        val wrong = showErrors && digit > 0 && digit != game.solution[index]
                        val bg = when {
                            isSelected -> Color(0xFF006D93).copy(alpha = .72f)
                            wrong -> Ink.red.copy(alpha = .17f)
                            equal -> Ink.blue.copy(alpha = .27f)
                            isPeer -> Ink.blue.copy(alpha = .12f)
                            else -> Color.Transparent
                        }
                        val highlightColor by animateColorAsState(
                            targetValue = bg,
                            animationSpec = if (animations) tween(durationMillis = 150) else snap(),
                            label = "Sudoku cell highlight"
                        )
                        Box(
                            Modifier.weight(1f).fillMaxHeight()
                                .testTag("sudoku-cell-$index")
                                .background(highlightColor)
                                .border(if (isSelected) 2.3.dp else .35.dp,
                                    if (isSelected) Color(0xFF6EF9FF)
                                    else Ink.line.copy(alpha = .55f))
                                .semantics {
                                    role = Role.Button
                                    this.selected = isSelected
                                    contentDescription = (if (isSelected) "Odabrano. " else "") +
                                        "Redak ${r + 1}, stupac ${c + 1}: " + when {
                                            digit == 0 && game.notes[index] != 0 ->
                                                "prazno polje, bilješke " +
                                                    (1..n).filter { game.notes[index] and (1 shl it) != 0 }
                                                        .joinToString(", ")
                                            digit == 0 -> "prazno polje"
                                            fixed -> "zadano ${digit}"
                                            wrong -> "netočno ${digit}"
                                            else -> "uneseno ${digit}"
                                        }
                                }
                                .clickable(enabled = !game.completed && !game.failed) {
                                    onSelect(index)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (digit > 0) {
                                Text(
                                    digit.toString(),
                                    color = when {
                                        wrong -> Ink.red
                                        fixed -> Ink.white
                                        else -> Color(0xFF65A6FF)
                                    },
                                    fontSize = when (n) {
                                        9 -> 21.sp
                                        6 -> 29.sp
                                        else -> 39.sp
                                    },
                                    fontWeight = if (fixed) FontWeight.Medium else FontWeight.Bold,
                                )
                            } else if (game.notes[index] != 0) {
                                CandidateGrid(game.notes[index], n)
                            }
                        }
                    }
                }
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            val cellW = size.width / n
            val cellH = size.height / n
            for (c in 1 until n) if (c % game.boxColumns == 0) {
                val x = c * cellW
                drawLine(Ink.blue.copy(alpha = .78f),
                    Offset(x, 0f), Offset(x, size.height), strokeWidth = 2.2.dp.toPx())
            }
            for (r in 1 until n) if (r % game.boxRows == 0) {
                val y = r * cellH
                drawLine(Ink.blue.copy(alpha = .78f),
                    Offset(0f, y), Offset(size.width, y), strokeWidth = 2.2.dp.toPx())
            }
        }
        }
    }
}

@Composable
private fun GameTool(
    name: String, accent: Color, modifier: Modifier,
    height: androidx.compose.ui.unit.Dp = 73.dp,
    active: Boolean = false, badge: String? = null,
    enabled: Boolean = true, tag: String = "", onClick: () -> Unit,
) {
    NeonPanel(
        modifier.testTag(tag)
            .alpha(if (enabled) 1f else .53f)
            .semantics {
                contentDescription = if (badge != null) "$name, preostalo $badge" else name
                role = Role.Button
                selected = active
            }
            .heightIn(min = 48.dp).height(height)
            .clickable(enabled = enabled, onClick = onClick),
        if (active) Ink.cyan else accent, corner = 14.dp,
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val narrowTool = maxWidth < 72.dp
        Column(
            Modifier.fillMaxSize().padding(5.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BrandIcon(
                when (name) {
                    "Bilješke" -> "note"
                    "Savjet" -> "hint"
                    "Poništi" -> "undo"
                    "Izbriši" -> "erase"
                    else -> "grid"
                },
                Modifier.size(if (narrowTool) 20.dp else 25.dp),
                if (active) Ink.cyan else Ink.muted,
            )
            Spacer(Modifier.height(3.dp))
            Text(name, color = Ink.white,
                fontSize = if (narrowTool) 10.sp else 11.sp, maxLines = 1)
        }
        }
        if (badge != null) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(4.dp).size(19.dp)
                    .background(Ink.blue, RoundedCornerShape(50))
                    .border(.8.dp, Ink.cyan, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center,
            ) {
                Text(badge, color = Ink.white, fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

/**
 * Stable candidate slots prevent notes from shifting when a second digit
 * is toggled. The reference board uses a 3×3 micro-grid for 9×9 play.
 */
@Composable
private fun CandidateGrid(mask: Int, size: Int) {
    val columns = when (size) {
        9, 6 -> 3
        else -> 2
    }
    val rows = (size + columns - 1) / columns
    Column(Modifier.fillMaxSize().padding(1.dp)) {
        repeat(rows) { row ->
            Row(Modifier.weight(1f).fillMaxWidth()) {
                repeat(columns) { column ->
                    val digit = row * columns + column + 1
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        if (digit <= size && mask and (1 shl digit) != 0) {
                            Text(
                                digit.toString(),
                                color = Ink.muted,
                                fontSize = when (size) {
                                    9 -> 7.sp
                                    6 -> 10.sp
                                    else -> 12.sp
                                },
                                lineHeight = 10.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Five uniform keys per row on 9×9, with the last four centered. */
@Composable
private fun NumberRow(
    values: List<Int>, height: androidx.compose.ui.unit.Dp,
    enabled: Boolean, columns: Int, onClick: (Int) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val gap = 7.dp
        val keyWidth = (maxWidth - gap * (columns - 1)) / columns
        val keyFontSize = when {
            keyWidth < 42.dp -> 20.sp
            keyWidth < 56.dp -> 25.sp
            else -> 29.sp
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
        ) {
            values.forEach { value ->
                NeonPanel(
                    Modifier.width(keyWidth).testTag("number-$value")
                        .alpha(if (enabled) 1f else .50f)
                        .semantics { contentDescription = "Unesi broj $value"; role = Role.Button }
                        .heightIn(min = 48.dp).height(height)
                        .clickable(enabled = enabled) { onClick(value) },
                    highlight = Ink.blue, corner = 13.dp,
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(value.toString(), color = Ink.white,
                            fontSize = keyFontSize, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
