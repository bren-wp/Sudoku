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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
        val horizontal = if (compact) 12.dp else 20.dp
        val boardSize = minOf(maxWidth - horizontal * 2,
            maxHeight - if (compact) 255.dp else 300.dp).coerceAtLeast(216.dp)
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
                AccentText("Sudoku", if (compact) 32 else 37)
                Subtitle(if (game.daily) "Dnevni izazov" else difficultyLabels[game.level],
                    centered = true)
            }
            Box(Modifier.size(48.dp).testTag("game-pause")
                .semantics { contentDescription = "Pauziraj ili nastavi igru"; role = Role.Button }
                .background(Color(0x770B2256), RoundedCornerShape(14.dp))
                .clickable { controller.togglePause() }, contentAlignment = Alignment.Center) {
                BrandIcon(if (game.paused) "play" else "pause", Modifier.size(23.dp), Ink.muted)
            }
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(48.dp).testTag("game-settings")
                .semantics { contentDescription = "Postavke igre"; role = Role.Button }.background(Color(0x770B2256), RoundedCornerShape(14.dp))
                .clickable { controller.go(Screen.SETTINGS) }, contentAlignment = Alignment.Center) {
                BrandIcon("settings", Modifier.size(24.dp), Ink.muted)
            }
        }
        Spacer(Modifier.height(if (compact) 1.dp else 4.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandIcon("timer", Modifier.size(18.dp), Ink.muted)
                Spacer(Modifier.width(5.dp))
                Text(formatTime(game.elapsed), fontSize = if (compact) 14.sp else 16.sp, color = Ink.muted)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                val correct = game.correctCount()
                val percentage = correct.toFloat() / game.board.size
                Text("Još ${game.board.size - correct} polja", color = Ink.muted, fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth().height(6.dp)
                    .background(Ink.blue.copy(alpha = .27f), RoundedCornerShape(10.dp))) {
                    Box(Modifier.fillMaxWidth(percentage).fillMaxHeight()
                        .background(Ink.cyan, RoundedCornerShape(10.dp)))
                }
                Text("${(percentage * 100).toInt()}%", fontSize = 11.sp, color = Ink.cyan)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (controller.options.checkMistakes)
                        "♥".repeat((3 - game.mistakes).coerceIn(0, 3)) +
                            "♡".repeat(game.mistakes.coerceIn(0, 3))
                    else "∞",
                    color = Ink.red, fontSize = 19.sp,
                )
                Text(if (controller.options.checkMistakes) "Greške: ${game.mistakes}/3"
                    else "Bez ograničenja", color = Ink.muted, fontSize = 10.sp)
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
            GameTool("✎", "Bilješke", if (game.notesMode) Ink.teal else Ink.line,
                Modifier.weight(1f), height = toolHeight, active = game.notesMode,
                tag = "game-notes") { controller.toggleNotes(); feedback() }
            GameTool("✧", "Savjet", Ink.cyan, Modifier.weight(1f), height = toolHeight,
                badge = "${(SudokuController.MAX_HINTS - game.hints).coerceAtLeast(0)}",
                tag = "game-hint") { controller.hint(); feedback() }
            GameTool("↶", "Poništi", Ink.blue, Modifier.weight(1f), height = toolHeight,
                tag = "game-undo") { controller.undo(); feedback() }
            GameTool("⌫", "Izbriši", Ink.blue, Modifier.weight(1f), height = toolHeight,
                tag = "game-erase") { controller.erase(); feedback() }
        }
        val numbers = (1..game.size).toList()
        val split = if (game.size == 9) 5 else if (game.size == 6) 3 else 4
        NumberRow(numbers.take(split), numberHeight) { controller.enter(it); feedback() }
        if (numbers.size > split) NumberRow(numbers.drop(split), numberHeight) { controller.enter(it); feedback() }
        if (!compact) {
            Text("B I S T R I J I  U M  ·  B O L J I  D A N", color = Ink.muted,
                fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
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
                Text(if (game.completed) "🏆 Čestitamo!" else "Pokušaj ponovno",
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
                            isSelected -> Color(0xFF007B9C).copy(alpha = .65f)
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
                                .border(.35.dp, Ink.line.copy(alpha = .55f))
                                .semantics {
                                    role = Role.Button
                                    contentDescription = (if (isSelected) "Odabrano. " else "") +
                                        "Redak ${r + 1}, stupac ${c + 1}: " + when {
                                            digit == 0 -> "prazno polje"
                                            fixed -> "zadano ${digit}"
                                            wrong -> "netočno ${digit}"
                                            else -> "uneseno ${digit}"
                                        }
                                }
                                .clickable { onSelect(index) },
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
                                val present = (1..n).filter { game.notes[index] and (1 shl it) != 0 }
                                val grouped = present.chunked(if (n == 9) 3 else 2)
                                    .joinToString("\n") { it.joinToString(" ") }
                                Text(grouped, fontSize = if (n == 9) 7.sp else 10.sp,
                                    lineHeight = if (n == 9) 8.sp else 11.sp,
                                    textAlign = TextAlign.Center, color = Ink.muted)
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
                drawLine(Ink.cyan.copy(alpha = .60f),
                    Offset(x, 0f), Offset(x, size.height), strokeWidth = 2.5.dp.toPx())
            }
            for (r in 1 until n) if (r % game.boxRows == 0) {
                val y = r * cellH
                drawLine(Ink.cyan.copy(alpha = .60f),
                    Offset(0f, y), Offset(size.width, y), strokeWidth = 2.5.dp.toPx())
            }
        }
    }
}

@Composable
private fun GameTool(icon: String, name: String, accent: Color, modifier: Modifier,
                     height: androidx.compose.ui.unit.Dp = 73.dp, active: Boolean = false,
                     badge: String? = null, tag: String = "", onClick: () -> Unit) {
    NeonPanel(modifier.testTag(tag)
        .semantics { contentDescription = name; role = Role.Button }
        .heightIn(min = 48.dp).height(height).clickable(onClick = onClick),
        if (active) Ink.cyan else accent, corner = 14.dp) {
        Column(Modifier.fillMaxSize().padding(5.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                BrandIcon(when (name) {
                    "Bilješke" -> "note"; "Savjet" -> "hint"
                    "Poništi" -> "undo"; "Izbriši" -> "erase"
                    else -> "grid"
                }, Modifier.size(23.dp), if (active) Ink.cyan else Ink.muted)
                if (badge != null) {
                    Spacer(Modifier.width(2.dp))
                    Text(badge, color = Ink.cyan, fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold)
                }
            }
            Text(name, color = Ink.white, fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable
private fun NumberRow(values: List<Int>, height: androidx.compose.ui.unit.Dp, onClick: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        values.forEach { value ->
            NeonPanel(Modifier.weight(1f).testTag("number-$value")
                .semantics { contentDescription = "Unesi broj $value"; role = Role.Button }
                .heightIn(min = 48.dp).height(height).clickable { onClick(value) },
                highlight = Ink.blue, corner = 13.dp) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(value.toString(), color = Ink.white,
                        fontSize = 29.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
