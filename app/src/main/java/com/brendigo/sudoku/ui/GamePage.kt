package com.brendigo.sudoku.ui

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
    PageContent {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(45.dp).testTag("game-back").background(Color(0x770B2256), RoundedCornerShape(14.dp))
                .clickable { controller.go(Screen.HOME) }, contentAlignment = Alignment.Center) {
                Text("‹", color = Ink.muted, fontSize = 34.sp)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                AccentText("Sudoku", 37)
                Subtitle(if (game.daily) "Dnevni izazov" else difficultyLabels[game.level],
                    centered = true)
            }
            Box(Modifier.size(43.dp).background(Color(0x770B2256), RoundedCornerShape(14.dp))
                .clickable { controller.togglePause() }, contentAlignment = Alignment.Center) {
                Text("Ⅱ", color = Ink.muted, fontSize = 22.sp)
            }
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(43.dp).testTag("game-settings").background(Color(0x770B2256), RoundedCornerShape(14.dp))
                .clickable { controller.go(Screen.SETTINGS) }, contentAlignment = Alignment.Center) {
                Text("⚙", color = Ink.muted, fontSize = 22.sp)
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("◷ ${formatTime(game.elapsed)}", fontSize = 16.sp, color = Ink.muted)
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
            }
            Text("♥".repeat((3 - game.mistakes).coerceIn(0, 3)) +
                "♡".repeat(game.mistakes.coerceIn(0, 3)),
                color = Ink.red, fontSize = 20.sp)
        }
        SudokuBoard(game, controller.options.highlightEquals) {
            controller.select(it)
            feedback()
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GameTool("✎", "Bilješke", if (game.notesMode) Ink.teal else Ink.line,
                Modifier.weight(1f), active = game.notesMode) { controller.toggleNotes(); feedback() }
            GameTool("✧", "Savjet", Ink.cyan, Modifier.weight(1f)) { controller.hint(); feedback() }
            GameTool("↶", "Poništi", Ink.blue, Modifier.weight(1f)) { controller.undo(); feedback() }
            GameTool("⌫", "Izbriši", Ink.blue, Modifier.weight(1f)) { controller.erase(); feedback() }
        }
        val numbers = (1..game.size).toList()
        val split = if (game.size == 9) 5 else if (game.size == 6) 3 else 4
        NumberRow(numbers.take(split)) { controller.enter(it); feedback() }
        if (numbers.size > split) NumberRow(numbers.drop(split)) { controller.enter(it); feedback() }
        Spacer(Modifier.height(8.dp))
        Text("B I S T R I J I  U M  ·  B O L J I  D A N", color = Ink.muted,
            fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
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
private fun SudokuBoard(game: Session, highlight: Boolean, onSelect: (Int) -> Unit) {
    val n = game.size
    val selected = game.selected
    val selectionNumber = if (selected >= 0) game.board[selected] else 0
    Box(
        Modifier.fillMaxWidth().aspectRatio(1f).testTag("sudoku-board")
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
                        val wrong = digit > 0 && digit != game.solution[index]
                        val bg = when {
                            isSelected -> Color(0xFF007B9C).copy(alpha = .65f)
                            wrong -> Ink.red.copy(alpha = .17f)
                            equal -> Ink.blue.copy(alpha = .27f)
                            isPeer -> Ink.blue.copy(alpha = .12f)
                            else -> Color.Transparent
                        }
                        Box(
                            Modifier.weight(1f).fillMaxHeight()
                                .background(bg)
                                .border(.35.dp, Ink.line.copy(alpha = .55f))
                                .semantics {
                                    contentDescription = "Redak ${r + 1}, stupac ${c + 1}: " +
                                        when {
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
                     active: Boolean = false, onClick: () -> Unit) {
    NeonPanel(modifier.height(73.dp).clickable(onClick = onClick),
        if (active) Ink.cyan else accent, corner = 14.dp) {
        Column(Modifier.fillMaxSize().padding(5.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, color = if (active) Ink.cyan else Ink.muted, fontSize = 26.sp)
            Text(name, color = Ink.white, fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable
private fun NumberRow(values: List<Int>, onClick: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        values.forEach { value ->
            NeonPanel(Modifier.weight(1f).height(62.dp).clickable { onClick(value) },
                highlight = Ink.blue, corner = 13.dp) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(value.toString(), color = Ink.white,
                        fontSize = 29.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
