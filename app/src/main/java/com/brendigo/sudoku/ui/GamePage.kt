package com.brendigo.sudoku.ui

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.sudoku.data.Screen
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
        val tinyTitle = maxWidth < 275.dp
        val largeText = LocalDensity.current.fontScale >= 1.4f
        // Balance the two controls on the right against a single Back
        // control on the left. The previous weighted column centered the
        // wordmark inside an asymmetrical row instead of on the viewport.
        val headingShift = when {
            largeText || narrowWidth -> 0.dp
            maxWidth >= 400.dp -> 39.dp
            maxWidth >= 350.dp -> 26.dp
            else -> 0.dp
        }
        val horizontal = if (compact) 12.dp else 21.dp
        // Width is the hard limit: a 216dp minimum previously overflowed
        // narrow multi-window and foldable screens.
        val availableWidth = (maxWidth - horizontal * 2).coerceAtLeast(1.dp)
        val availableHeight = (maxHeight - if (compact) 255.dp else 300.dp)
            .coerceAtLeast(216.dp)
        val boardSize = (minOf(availableWidth, availableHeight) - if (compact) 0.dp else 4.dp)
            .coerceAtLeast(1.dp)
        val toolHeight = if (compact) 51.dp else 66.dp
        val numberHeight = if (compact) 48.dp else 47.dp
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = horizontal, vertical = if (compact) 6.dp else 7.dp),
            // The 941×1672 reference places the board directly below a compact
            // HUD. 8dp gaps across seven children shifted the entire game down.
            verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 2.dp),
        ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).testTag("game-back")
                .semantics { contentDescription = "Natrag na početni ekran"; role = Role.Button }.background(Color(0xB0092050), RoundedCornerShape(14.dp))
                .border(1.dp, Ink.blue.copy(alpha = .68f), RoundedCornerShape(14.dp))
                .clickable { controller.go(Screen.HOME) }, contentAlignment = Alignment.Center) {
                BrandIcon("back", Modifier.size(24.dp), Ink.muted)
            }
            Column(Modifier.weight(1f).offset(x = headingShift)
                .testTag("game-heading"),
                horizontalAlignment = Alignment.CenterHorizontally) {
                AccentText("Sudoku", when {
                    tinyTitle -> 22
                    narrowWidth || largeText -> 28
                    compact -> 36
                    else -> 40
                })
                Subtitle(if (game.daily) "Dnevni izazov" else difficultyTitle(game.level),
                    centered = true)
            }
            Box(Modifier.size(48.dp).testTag("game-pause")
                .semantics { contentDescription = "Pauziraj ili nastavi igru"; role = Role.Button }
                .background(Color(0xB0092050), RoundedCornerShape(14.dp))
                .border(1.dp, Ink.blue.copy(alpha = .68f), RoundedCornerShape(14.dp))
                .clickable { controller.togglePause() }, contentAlignment = Alignment.Center) {
                BrandIcon(if (game.paused) "play" else "pause", Modifier.size(23.dp), Ink.muted)
            }
            Spacer(Modifier.width(if (narrowWidth) 4.dp else 8.dp))
            Box(Modifier.size(48.dp).testTag("game-settings")
                .semantics { contentDescription = "Postavke igre"; role = Role.Button }.background(Color(0xB0092050), RoundedCornerShape(14.dp))
                .border(1.dp, Ink.blue.copy(alpha = .68f), RoundedCornerShape(14.dp))
                .clickable { controller.go(Screen.SETTINGS) }, contentAlignment = Alignment.Center) {
                BrandIcon("settings", Modifier.size(24.dp), Ink.muted)
            }
        }
        Spacer(Modifier.height(if (compact) 1.dp else 0.dp))
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
                        Text("Još ${game.emptyCells} praznih polja · ${(progress * 100).toInt()}%",
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
            Column(
                Modifier.weight(1f).testTag("game-progress-group"),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val correct = game.correctCount()
                val percentage = correct.toFloat() / game.board.size
                Text("Još ${game.emptyCells} praznih polja", color = Ink.muted, fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f).height(6.dp).testTag("game-progress")
                        .background(Ink.blue.copy(alpha = .27f), RoundedCornerShape(10.dp))) {
                        Box(Modifier.fillMaxWidth(percentage).fillMaxHeight()
                            .background(Ink.cyan, RoundedCornerShape(10.dp)))
                    }
                    Spacer(Modifier.width(7.dp))
                    Text("${(percentage * 100).toInt()}%", fontSize = 11.sp,
                        color = Ink.cyan, maxLines = 1)
                }
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
        Spacer(Modifier.height(if (compact) 2.dp else 6.dp))
        Row(
            Modifier.fillMaxWidth().testTag("game-tool-row"),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 8.dp),
        ) {
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
        Spacer(Modifier.height(if (compact) 2.dp else 6.dp))
        val numbers = (1..game.size).toList()
        val split = if (game.size == 9) 5 else if (game.size == 6) 3 else 4
        Column(
            Modifier.fillMaxWidth().testTag("game-number-panel")
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xB4031A43))
                .border(1.dp, Ink.blue.copy(alpha = .65f), RoundedCornerShape(14.dp))
                .padding(horizontal = if (compact) 8.dp else 12.dp,
                    vertical = if (compact) 5.dp else 6.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            NumberRow(numbers.take(split), numberHeight, controller.canEnter(), split) {
                controller.enter(it); feedback()
            }
            if (numbers.size > split) {
                NumberRow(numbers.drop(split), numberHeight, controller.canEnter(), split) {
                    controller.enter(it); feedback()
                }
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
