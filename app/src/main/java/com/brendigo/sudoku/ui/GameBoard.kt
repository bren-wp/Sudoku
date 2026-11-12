package com.brendigo.sudoku.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.sudoku.data.Session

@Composable
internal fun SudokuBoard(game: Session, highlight: Boolean, showErrors: Boolean,
                        animations: Boolean, modifier: Modifier = Modifier, onSelect: (Int) -> Unit) {
    val n = game.size
    val selected = game.selected
    val selectionNumber = if (selected >= 0) game.board[selected] else 0
    Box(
        modifier.aspectRatio(1f).testTag("sudoku-board")
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFA03132D))
            .border(1.5.dp, Ink.cyan.copy(alpha = .77f), RoundedCornerShape(14.dp))
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
                            // The reference shows a deep navy selected tile
                            // outlined by neon cyan, not an opaque turquoise
                            // block that obscures neighbouring number contrast.
                            isSelected -> Color(0xFF006B87).copy(alpha = .34f)
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
                            if (isSelected) {
                                // Illuminated cyan focus cell; live text, notes
                                // and click/selection semantics remain above it.
                                Canvas(Modifier.matchParentSize()) {
                                    val center = Offset(size.width * .5f, size.height * .5f)
                                    drawRect(Brush.radialGradient(
                                        listOf(Color(0x5538EEFF), Color.Transparent),
                                        center = center, radius = size.width * .77f,
                                    ))
                                }
                            }
                            if (digit > 0) {
                                Text(
                                    digit.toString(),
                                    color = when {
                                        wrong -> Ink.red
                                        fixed -> Ink.white
                                        else -> Color(0xFF65A6FF)
                                    },
                                    fontSize = when (n) {
                                        9 -> 25.sp
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
                drawLine(Color(0xFF478FDB).copy(alpha = .87f),
                    Offset(x, 0f), Offset(x, size.height), strokeWidth = 2.dp.toPx())
            }
            for (r in 1 until n) if (r % game.boxRows == 0) {
                val y = r * cellH
                drawLine(Color(0xFF478FDB).copy(alpha = .87f),
                    Offset(0f, y), Offset(size.width, y), strokeWidth = 2.dp.toPx())
            }
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
